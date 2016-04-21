package nmea.server.ctx;


import coreutilities.sql.SQLUtil;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.Serializable;

import java.sql.Connection;

import java.text.DecimalFormat;

import java.util.ArrayList;
import java.util.ConcurrentModificationException;
import java.util.HashMap;
import java.util.Hashtable;
import java.util.List;

import nmea.event.NMEAReaderListener;

import nmea.ui.viewer.spot.utils.SpotParser.SpotLine;

import ocss.nmea.api.NMEAEvent;
import ocss.nmea.api.NMEAListener;

import ocss.nmea.parser.GeoPos;

import oracle.xml.parser.v2.DOMParser;


/**
 * A singleton
 */
public class NMEAContext implements Serializable
{
  public static enum WindScale
  {
    _00_05( 1.5f,  5, "05 knots"),
    _05_10( 3,    10, "10 knots"),
    _10_15( 4.5f, 15, "15 knots"),
    _15_20( 6,    20, "20 knots"),
    _20_25( 7.5f, 25, "25 knots"),
    _25_30( 9,    30, "30 knots"),
    _30_40(12,    40, "40 knots"),
    _40_50(15,    50, "50 knots"),
    _50_60(18,    60, "60 knots");
    
    private final float scale;
    private final double speed;
    private final String label;
    
    WindScale(float scale, double speed, String label)
    {
      this.scale = scale;
      this.speed = speed;
      this.label = label;
    }
    
    public float scale() { return scale; }
    public double speed() { return speed; }
    public String label() { return label; }
    
  }
  
  private static NMEAContext applicationContext = null;  
  private transient List<NMEAReaderListener> NMEAReaderListeners = null;
  private transient List<NMEAListener> NMEAListeners = null;
  private NMEADataCache dataCache = null;
  private transient NMEADataCache frozenDataCache = null;
  private static List<double[]> deviation = null;
  
  public final static DecimalFormat DF22 = new DecimalFormat("00.00");
  public final static DecimalFormat DF3  = new DecimalFormat("000");
  
  public final static int DEFAULT_BUFFER_SIZE = 3600;
  
  private boolean fromFile = false;
  private boolean autoScale = false;
  private String replayFile = null;
  private long replayFileRecNum = 0L;
  private long replayFileSize = 0L;
  
  private float currentWindScale = 0f;
  
  private transient DOMParser parser = null;
  private transient Connection dbConnection = null;  
  
  private NMEAContext()
  {
    NMEAReaderListeners = new ArrayList<NMEAReaderListener>(2); // 2: Initial Capacity
    NMEAListeners = new ArrayList<NMEAListener>();
    parser = new DOMParser();
    boolean isOriginalCache = "true".equals(System.getProperty("original.cache", "false"));
    dataCache = new NMEADataCache(isOriginalCache);
  }
    
  public static synchronized NMEAContext getInstance()
  {
    if (applicationContext == null)
      applicationContext = new NMEAContext();    
    return applicationContext;
  }
    
  public synchronized List<NMEAReaderListener> getReaderListeners()
  {
    return NMEAReaderListeners;
  }    

  public synchronized void addNMEAReaderListener(NMEAReaderListener l)
  {
    synchronized (NMEAReaderListeners)
    {
      if (!NMEAReaderListeners.contains(l))
      {
        NMEAReaderListeners.add(l);
      }
      if (System.getProperty("verbose", "false").equals("true")) System.out.println("We have " + NMEAReaderListeners.size() + " NMEAListener(s). Last one belongs to group [" + l.getGroupID() + "]");
    }
  }

  public synchronized void removeNMEAReaderListener(NMEAReaderListener l)
  {
    NMEAReaderListeners.remove(l);
  }

  public synchronized void removeNMEAListenerGroup(String groupID)
  {
    System.out.println("Removing NMEAListener from group [" + groupID + "]");
    System.out.println("Start with " + NMEAReaderListeners.size() + " NMEAListener(s).");
    List<NMEAReaderListener> toRemove = new ArrayList<NMEAReaderListener>(1);
    for (NMEAReaderListener listener : NMEAReaderListeners)
    {
      if (listener.getGroupID().equals(groupID))
        toRemove.add(listener);
    }
    for (NMEAReaderListener nl : toRemove)
      removeNMEAReaderListener(nl);
    System.out.println("End up with " + NMEAReaderListeners.size() + " NMEAListener(s):");
    for (NMEAReaderListener listener : NMEAReaderListeners)
    {
      System.out.println("Remaining: Listener belongs to group [" + listener.getGroupID() + "]");
    }
  }
  
  public synchronized List<NMEAListener> getNMEAListeners()
  {
    return NMEAListeners;
  }    

  public synchronized void addNMEAListener(NMEAListener l)
  {
    synchronized(NMEAListeners)
    {
      if (!NMEAListeners.contains(l))
      {
        NMEAListeners.add(l);
      }
    }
  }

  public synchronized void removeNMEAListener(NMEAListener l)
  {
    synchronized(NMEAListeners)
    {
      NMEAListeners.remove(l);
    }
  }

  public void fireBulkDataRead(NMEAEvent ne)
  {
    try
    {
      synchronized (NMEAListeners)
      {
        for (NMEAListener nl : NMEAListeners)
        {
          synchronized(nl)
          {
            nl.dataRead(ne);
          }
        }
      }
    }
    catch (Exception cme)
    {
      System.err.println("To be taken care of: " + cme.toString() + "...");
      cme.printStackTrace();
    }
  }
  
  public void fireNewSpotData(List<SpotLine> spotLines, GeoPos pos)
  {
    for (NMEAReaderListener l : NMEAReaderListeners)
    {
      synchronized (l)
      {
        l.newSpotData(spotLines, pos);
      }
    }
  }

  public void setReplayFile(final String replayFile)
  {
    this.replayFile = replayFile;
    // Count the number of records in the file
    Thread counter = new Thread("LoggedDataReplayer")
      {
        public void run()
        {
          try
          {
            BufferedReader br = new BufferedReader(new FileReader(replayFile));
            String l = "";
            long nbRec = 0;
            boolean b = true;
            while (b)
            {
              l = br.readLine();
              b = (l != null);
              if (b)
                nbRec++;
            }
            setReplayFileSize(nbRec);
            br.close();
          }
          catch (Exception ex)
          {
            System.err.println(ex.getLocalizedMessage());
          }
        }
      };
    counter.start();
  }

  public String getReplayFile()
  {
    return replayFile;
  }

  public void setReplayFileSize(long replayFilesize)
  {
    this.replayFileSize = replayFilesize;
  }

  public long getReplayFileSize()
  {
    return replayFileSize;
  }
  public void setReplayFileRecNum(long replayFileRecNum)
  {
    this.replayFileRecNum = replayFileRecNum;
  }

  public long getReplayFileRecNum()
  {
    return replayFileRecNum;
  }

  public DOMParser getParser()
  {
    return parser;
  }
  
  public Connection getDBConnection()
  {
    if (dbConnection == null)
    {
      String dbLoc = System.getProperty("db.location", ".." + File.separator + "all-db");
      try { dbConnection = SQLUtil.getConnection(dbLoc, "LOG", "log", "log"); }
      catch (Exception ex) { ex.printStackTrace(); }
    }
    return dbConnection;
  }
  
  public void closeDBConnection()
  {
    try { SQLUtil.shutdown(dbConnection); } catch (Exception ex) { ex.printStackTrace(); }
    dbConnection = null;
  }

  public void fireInternalFrameClosed()
  {
    for (int i=0; i<NMEAReaderListeners.size(); i++)
    {
      NMEAReaderListener l = NMEAReaderListeners.get(i);
      l.internalFrameClosed();
    }
  }
  
  public void fireInternalTxFrameClosed()
  {
    for (int i=0; i<NMEAReaderListeners.size(); i++)
    {
      NMEAReaderListener l = NMEAReaderListeners.get(i);
      l.internalTxFrameClosed();
    }
  }
  
  public void fireInternalAnalyzerFrameClosed()
  {
    for (int i=0; i<NMEAReaderListeners.size(); i++)
    {
      NMEAReaderListener l = NMEAReaderListeners.get(i);
      l.internalAnalyzerFrameClosed();
    }
  }
  
  public void firePositionManuallyChanged(GeoPos gp)
  {
    for (int i=0; i<NMEAReaderListeners.size(); i++)
    {
      NMEAReaderListener l = NMEAReaderListeners.get(i);
      l.positionManuallyUpdated(gp);
    }
  }
  
  public void fireSetSpotLineIndex(int idx)
  {
    for (int i=0; i<NMEAReaderListeners.size(); i++)
    {
      NMEAReaderListener l = NMEAReaderListeners.get(i);
      l.setSpotLineIndex(idx);
    }
  }
  
  public void fireLogChanged(boolean b)
  {
    for (int i=0; i<NMEAReaderListeners.size(); i++)
    {
      NMEAReaderListener l = NMEAReaderListeners.get(i);
      l.log(b);
    }
  }
  
  public void fireLogChanged(boolean log, boolean withDateTime)
  {
    for (int i=0; i<NMEAReaderListeners.size(); i++)
    {
      NMEAReaderListener l = NMEAReaderListeners.get(i);
      l.log(log, withDateTime);
    }
  }
  
  public void fireWindScale(float f)
  {
    setCurrentWindScale(f);
    for (int i=0; i<NMEAReaderListeners.size(); i++)
    {
      NMEAReaderListener l = NMEAReaderListeners.get(i);
      l.setWindScale(f);
    }
  }
  
  public void fireAutoScale(boolean b)
  {
    for (int i=0; i<NMEAReaderListeners.size(); i++)
    {
      NMEAReaderListener l = NMEAReaderListeners.get(i);
      l.setAutoScale(b);
    }
  }
  
  public void fireSetMessage(String s)
  {
    for (int i=0; i<NMEAReaderListeners.size(); i++)
    {
      NMEAReaderListener l = NMEAReaderListeners.get(i);
      l.setMessage(s);
    }
  }
  
  public void fireNMEAString(String s)
  {
    synchronized (this.NMEAReaderListeners)
    {
      for (NMEAReaderListener l : NMEAReaderListeners)
      {
        synchronized (l)
        {
          l.manageNMEAString(s);
        }
      }
    }
  }
  
  public void fireSaveUserConfig()
  {
    for (int i=0; i<NMEAReaderListeners.size(); i++)
    {
      NMEAReaderListener l = NMEAReaderListeners.get(i);
      l.saveUserConfig();
    }
  }
  
  public void fireDataChanged()
  {
    for (int i=0; i<NMEAReaderListeners.size(); i++)
    {
      NMEAReaderListener l = NMEAReaderListeners.get(i);
      l.dataUpdate();
    }
  }  

  public void fireDataBufferSizeChanged(int size)
  {
    for (int i=0; i<NMEAReaderListeners.size(); i++)
    {
      NMEAReaderListener l = NMEAReaderListeners.get(i);
      l.dataBufferSizeChanged(size);
    }
  }  

  public void fireJumpToOffset(long offset)
  {
    for (int i=0; i<NMEAReaderListeners.size(); i++)
    {
      NMEAReaderListener l = NMEAReaderListeners.get(i);
      l.jumpToOffset(offset);
    }
  }  

  public void fireDampingHasChanged(int val)
  {
    for (int i=0; i<NMEAReaderListeners.size(); i++)
    {
      NMEAReaderListener l = NMEAReaderListeners.get(i);
      l.dampingHasChanged(val);
    }
  }  

  public void fireLoadDataPointsForDeviation(List<double[]> data)
  {
    for (int i=0; i<NMEAReaderListeners.size(); i++)
    {
      NMEAReaderListener l = NMEAReaderListeners.get(i);
      l.loadDataPointsForDeviation(data);
    }
  }
  
  public void fireDeviationCurveChanged(Hashtable<Double, Double> devCurve)
  {
    for (int i=0; i<NMEAReaderListeners.size(); i++)
    {
      NMEAReaderListener l = NMEAReaderListeners.get(i);
      l.deviationCurveChanged(devCurve);
    }
  }
  
  public void fireReplaySpeedChanged(int slider)
  {
    for (int i=0; i<NMEAReaderListeners.size(); i++)
    {
      NMEAReaderListener l = NMEAReaderListeners.get(i);
      l.replaySpeedChanged(slider);
    }
  }
  
  public void fireStopReading() throws Exception
  {
    for (int i=0; i<NMEAReaderListeners.size(); i++)
    {
      NMEAReaderListener l = NMEAReaderListeners.get(i);
      l.stopReading();
    }
  }

  public void fireRefreshJournal()
  {
    for (int i=0; i<NMEAReaderListeners.size(); i++)
    {
      NMEAReaderListener l = NMEAReaderListeners.get(i);
      l.refreshLogJournal();
    }
  }
  
  public void fireShowRawData(boolean b)
  {
    for (int i=0; i<NMEAReaderListeners.size(); i++)
    {
      NMEAReaderListener l = NMEAReaderListeners.get(i);
      l.showRawData(b);
    }
  }
  
  public void fireEnableHTTPserver(boolean b)
  {    
    for (int i=0; i<NMEAReaderListeners.size(); i++)
    {
      NMEAReaderListener l = NMEAReaderListeners.get(i);
      l.enableHttpServer(b);
    }
  }
    
  public void fireFrameHasBeenClosed(String id)
  {      
    for (int i=0; i<NMEAReaderListeners.size(); i++)
    {
      NMEAReaderListener l = NMEAReaderListeners.get(i);
      l.loggedDataAnalyzerFrameClosed(id);
    }
  }
  
  public /* synchronized */ NMEADataCache getCache()
  {
//  System.out.println("NMEAContext getCache:" + Integer.toString(dataCache.size()) + " pos.");
    return dataCache;
  }
  
  public static NMEADataCache getCache_oneTrip() // Used for RMI
  {
    NMEADataCache cache = getInstance().getCache();
    return cache;
  }
  
  public synchronized Object getDataCache(String key)
  {
    return dataCache.get(key);
  }
  
  public synchronized void putDataCache(String key, Object value)
  {
    dataCache.put(key, value);
    fireDataChanged();
  }

  public synchronized void putDataCache(HashMap<String, Object> map)
  {
    dataCache.putAll(map);
    fireDataChanged();
  }

  public synchronized void setDeviation(List<double[]> deviation)
  {
    NMEAContext.deviation = deviation;
  }

  public /* synchronized */ List<double[]> getDeviation()
  {
    return deviation;
  }

  public void setFrozenDataCache(NMEADataCache frozenDataCache)
  {
    this.frozenDataCache = frozenDataCache;
  }

  public NMEADataCache getFrozenDataCache()
  {
    return frozenDataCache;
  }
  
  public synchronized NMEADataCache cloneCache()
  {
    NMEADataCache clone = null;
    synchronized (dataCache)
    {
      if (dataCache != null)
      {
        clone = new NMEADataCache();
        clone.putAll(dataCache);
      }
    }
    return clone;
  }

  public void setFromFile(boolean fromFile)
  {
    this.fromFile = fromFile;
  }

  public boolean isFromFile()
  {
    return fromFile;
  }

  public void setAutoScale(boolean autoScale)
  {
    this.autoScale = autoScale;
  }

  public boolean isAutoScale()
  {
    return autoScale;
  }

  public void setCurrentWindScale(float currentWindScale)
  {
    this.currentWindScale = currentWindScale;
  }

  public float getCurrentWindScale()
  {
    return currentWindScale;
  }
}
