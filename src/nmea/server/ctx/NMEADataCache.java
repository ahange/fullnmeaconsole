package nmea.server.ctx;

import java.io.Serializable;

import java.util.ArrayList;
import java.util.ConcurrentModificationException;
import java.util.HashMap;

import java.util.List;
import java.util.Set;

import nmea.event.NMEAReaderListener;

import nmea.ui.calc.CalculatedDataTablePane;

import ocss.nmea.api.NMEAEvent;
import ocss.nmea.api.NMEAListener;
import ocss.nmea.parser.Angle;
import ocss.nmea.parser.Angle360;
import ocss.nmea.parser.NMEADoubleValueHolder;
import ocss.nmea.parser.Speed;
import ocss.nmea.parser.StringParsers;

public class NMEADataCache extends HashMap<String, Object> implements Serializable
{
  public static final String LAST_NMEA_SENTENCE = "NMEA";

  public static final String SOG         = "SOG";
  public static final String POSITION    = "Boat Position";
  public static final String GPS_DATE_TIME = "GPS Date & Time";
  public static final String GPS_TIME    = "GPS Time";
  public static final String GPS_SOLAR_TIME = "Solar Time";
  public static final String COG         = "COG";
  public static final String DECLINATION = "D";
  public static final String BSP         = "BSP";
  public static final String LOG         = "Log";
  public static final String DAILY_LOG   = "Daily";
  public static final String WATER_TEMP  = "Water Temperature";
  public static final String AIR_TEMP    = "Air Temperature";
  public static final String BARO_PRESS  = "Barometric Pressure";
  public static final String RELATIVE_HUMIDITY = "Relative Humidity";
  public static final String AWA         = "AWA";
  public static final String AWS         = "AWS";
  public static final String HDG_COMPASS = "HDG c.";
  public static final String HDG_MAG     = "HDG mag.";
  public static final String HDG_TRUE    = "HDG true";
  public static final String DEVIATION   = "d";
  public static final String VARIATION   = "W";
  public static final String TWA         = "TWA";
  public static final String TWS         = "TWS";
  public static final String TWD         = "TWD";
  public static final String CSP         = "CSP";
  public static final String CDR         = "CDR";
  public static final String XTE         = "XTE";
  public static final String FROM_WP     = "From Waypoint";
  public static final String TO_WP       = "To Waypoint";
  public static final String WP_POS      = "WayPoint pos";
  public static final String DBT         = "Depth";
  public static final String D2WP        = "Distance to WP";
  public static final String B2WP        = "Bearing to WP";
  public static final String S2WP        = "Speed to WP";
  public static final String S2STEER     = "Steer";  
  public static final String LEEWAY      = "Leeway";
  public static final String CMG         = "CMG";
  public static final String PERF        = "Performance";
  public static final String SAT_IN_VIEW = "Satellites in view";
  
  public static final String BATTERY     = "Battery Voltage"; 
  public static final String CALCULATED_CURRENT = "Current calculated with damping";
  public static final String VDR_CURRENT = "Set and Drift";
  
  public static final String BSP_FACTOR  = "BSP Factor";
  public static final String AWS_FACTOR  = "AWS Factor";
  public static final String AWA_OFFSET  = "AWA Offset";
  public static final String HDG_OFFSET  = "HDG Offset";
  public static final String MAX_LEEWAY  = "Max Leeway";

  public static final String DEVIATION_FILE      = "Deviation file name";
  public static final String DEFAULT_DECLINATION = "Default Declination";
  public static final String DAMPING             = "Damping";
  
  public static final String POLAR_FILE_NAME = "Polar File name";
  public static final String POLAR_FACTOR    = "Polar Factor";

  public static final String TIME_RUNNING    = "Time Running";
  
  public static final String DISPLAY_WEB_WT    = "Display Web Water Temp";
  public static final String DISPLAY_WEB_AT    = "Display Web Air Temp";
  public static final String DISPLAY_WEB_GDT   = "Display Web GPSDateTime";
  public static final String DISPLAY_WEB_PRMSL = "Display Web PRMSL";
  public static final String DISPLAY_WEB_HUM   = "Display Web HUM";
  public static final String DISPLAY_WEB_VOLT  = "Display Web Volt";

  private final static boolean DEBUG = false;

  public static final HashMap<String, String> TOOLTIP_MAP = new HashMap<String, String>();

  // Damping ArrayList's
  private int dampingSize = 1;  
  
  private transient HashMap<String, List<Object>> dampingMap = new HashMap<String, List<Object>>();
  
  private long started = 0L;
  private boolean originalCache = false;
  
  private NMEADataCache instance = this;
  
  public NMEADataCache()
  {
    this(false);
  }
  
  public NMEADataCache(boolean originalCache)
  {
    super();
    this.originalCache = originalCache;
    started = System.currentTimeMillis();
    if (System.getProperty("verbose", "false").equals("true"))
    {
      System.out.println("+=================================+");
      System.out.println("| Instantiating an NMEADataCache. |");
      System.out.println("+=================================+");
    }
    TOOLTIP_MAP.put(LAST_NMEA_SENTENCE, "<html>Last read NMEA Sentence</html>");
    
    TOOLTIP_MAP.put(SOG,         "<html>Speed Over Ground<br>From the GPS</html>");
    TOOLTIP_MAP.put(POSITION,    "<html>Boat Position<br>From the GPS</html>");
    TOOLTIP_MAP.put(GPS_TIME,    "<html>GPS Time<br>From the GPS (GLL, RMC)</html>");
    TOOLTIP_MAP.put(GPS_DATE_TIME, "<html>GPS Time<br>From the GPS (RMC)</html>");
    TOOLTIP_MAP.put(GPS_SOLAR_TIME, "<html>Solar Time<br>From the GPS Time and Position</html>");
    TOOLTIP_MAP.put(COG,         "<html>Course Over Ground<br>From the GPS</html>");
    TOOLTIP_MAP.put(DECLINATION, "<html>Magnetic Declination<br>From the GPS</html>");
    TOOLTIP_MAP.put(BSP,         "<html>Boat Speed<br>From the NMEA Station</html>");
    TOOLTIP_MAP.put(LOG,         "<html>Log<br>From the NMEA Station</html>");
    TOOLTIP_MAP.put(DAILY_LOG,   "<html>Daily Log<br>From the NMEA Station</html>");
    TOOLTIP_MAP.put(WATER_TEMP,  "<html>Water Temperature<br>From the NMEA Station</html>");
    TOOLTIP_MAP.put(AIR_TEMP,    "<html>Air Temperature<br>From the NMEA Station or an extra sensor</html>");
    TOOLTIP_MAP.put(BARO_PRESS,  "<html>Barometric Pressure<br>From the NMEA Station or an extra sensor</html>");
    TOOLTIP_MAP.put(RELATIVE_HUMIDITY, "<html>Relatve Humidity<br>From the NMEA Station or an extra sensor</html>");
    TOOLTIP_MAP.put(AWA,         "<html>Apparent Wind Angle<br>From the NMEA Station</html>");
    TOOLTIP_MAP.put(AWS,         "<html>Apparent Wind Speed<br>From the NMEA Station</html>");
    TOOLTIP_MAP.put(HDG_COMPASS, "<html>Heading, Compass<br>From the NMEA Station</html>");
    TOOLTIP_MAP.put(HDG_MAG,     "<html>Heading, Magnetic<br>Calculated with the deviation</html>");
    TOOLTIP_MAP.put(HDG_TRUE,    "<html>Heading, True<br>Calculated with the Variation (W = D + d)</html>");
    TOOLTIP_MAP.put(DEVIATION,   "<html>deviation<br>Estimated, see deviation curve</html>");
    TOOLTIP_MAP.put(VARIATION,   "<html>Variation<br>Calculated, W = D + d</html>");
    TOOLTIP_MAP.put(TWA,         "<html>True Wind Angle<br>Calculated</html>");
    TOOLTIP_MAP.put(TWS,         "<html>True Wind Speed<br>Calculated</html>");
    TOOLTIP_MAP.put(TWD,         "<html>True Wind Direction<br>Calculated</html>");
    TOOLTIP_MAP.put(CSP,         "<html>Current Speed<br>Calculated</html>");
    TOOLTIP_MAP.put(CDR,         "<html>Current Direction<br>Calculated</html>");
    TOOLTIP_MAP.put(XTE,         "<html>Cross Track Error<br>From the GPS</html>");
    TOOLTIP_MAP.put(FROM_WP,     "<html>From Waypoint<br>From the GPS</html>");
    TOOLTIP_MAP.put(TO_WP,       "<html>To Waypoint<br>From the GPS</html>");
    TOOLTIP_MAP.put(WP_POS,      "<html>WayPoint position<br>From the GPS</html>");
    TOOLTIP_MAP.put(DBT,         "<html>Depth<br>From the NMEA Station</html>");
    TOOLTIP_MAP.put(D2WP,        "<html>Distance to WayPoint<br>From the GPS</html>");
    TOOLTIP_MAP.put(B2WP,        "<html>Bearing to WayPoint<br>From the GPS</html>");
    TOOLTIP_MAP.put(S2WP,        "<html>Speed to WayPoint<br>From the GPS</html>");
    TOOLTIP_MAP.put(S2STEER,     "<html>Steer left or right<br>From the GPS</html>");  
    TOOLTIP_MAP.put(LEEWAY,      "<html>Leeway<br>Estimated</html>");
    TOOLTIP_MAP.put(CMG,         "<html>Course Made Good<br>Calculated</html>");
    TOOLTIP_MAP.put(SAT_IN_VIEW, "<html>Satellites in view<br>From the GPS</html>");

    TOOLTIP_MAP.put(BATTERY,     "<html>Battery Voltage<br>From Raspberry PI</html>");
    TOOLTIP_MAP.put(CALCULATED_CURRENT, "<html>Current calculated with damping (0, 1 minute, 10 minutes...)</html>");
    TOOLTIP_MAP.put(VDR_CURRENT, "<html>Set and Drift</html>");
    
    TOOLTIP_MAP.put(BSP_FACTOR, "<html>Coefficient to apply to Boat Speed<br>(1.0 = 100%)</html>");
    TOOLTIP_MAP.put(AWS_FACTOR, "<html>Coefficient to apply to Appaprent Wind Speed<br>(1.0 = 100%)</html>");
    TOOLTIP_MAP.put(AWA_OFFSET, "<html>Apparent Wind Angle Offset<br>In degrees, positive to the right, negative to the left</html>");
    TOOLTIP_MAP.put(HDG_OFFSET, "<html>Heading Offset<br>In degrees, positive to the right, negative to the left</html>");
    TOOLTIP_MAP.put(MAX_LEEWAY, "<html>Max Leeway, in degrees<br>Estimated</html>");
    
    TOOLTIP_MAP.put(DEVIATION_FILE,      "Name of the CSV file containing the deviation curve points");
    TOOLTIP_MAP.put(DEFAULT_DECLINATION, "Declination to use, in case there is none in the NMEA Data");
    TOOLTIP_MAP.put(DAMPING,             "Damping for Data smoothing");
    
    TOOLTIP_MAP.put(POLAR_FILE_NAME, "Polar file name, with 'polar-coeff' extension");
    TOOLTIP_MAP.put(POLAR_FACTOR,    "Coefficient to apply to the target speed.");
    TOOLTIP_MAP.put(PERF,            "Performance, calculated with the polars");
    TOOLTIP_MAP.put(TIME_RUNNING,    "NMEA Server (cache) has been running for 'X' ms");
    
    TOOLTIP_MAP.put(DISPLAY_WEB_WT,    "Display Web Water Temp");
    TOOLTIP_MAP.put(DISPLAY_WEB_AT,    "Display Web Air Temp");
    TOOLTIP_MAP.put(DISPLAY_WEB_GDT,   "Display Web GPSDateTime");
    TOOLTIP_MAP.put(DISPLAY_WEB_PRMSL, "Display Web PRMSL");
    TOOLTIP_MAP.put(DISPLAY_WEB_HUM,   "Display Web Humidity");
    TOOLTIP_MAP.put(DISPLAY_WEB_VOLT,  "Display Web Volt");


    dampingMap.put(BSP,      new ArrayList<Object>());
    dampingMap.put(HDG_TRUE, new ArrayList<Object>());
    dampingMap.put(AWA,      new ArrayList<Object>());
    dampingMap.put(AWS,      new ArrayList<Object>());
    dampingMap.put(TWA,      new ArrayList<Object>());
    dampingMap.put(TWS,      new ArrayList<Object>());
    dampingMap.put(TWD,      new ArrayList<Object>());
    dampingMap.put(CSP,      new ArrayList<Object>());
    dampingMap.put(CDR,      new ArrayList<Object>());
    dampingMap.put(COG,      new ArrayList<Object>());
    dampingMap.put(SOG,      new ArrayList<Object>());
    dampingMap.put(LEEWAY,   new ArrayList<Object>());
    
    // Initialization
    this.put(CALCULATED_CURRENT, new HashMap<Long, CurrentDefinition>());
    
    if (this.originalCache)
      startBroadcastingCacheAge();
  }

  @Override
  public /*synchronized*/ Object put(String key, Object value)
  {
    Object o = null;
    synchronized (this) { o = super.put(key, value); }
    if (dampingSize > 1 && dampingMap.containsKey(key))
    {
      List<Object> ald = dampingMap.get(key);
      ald.add(value);      
      while (ald.size() > dampingSize)
        ald.remove(0);
    }
    return o;
  }

  // For debug
  double prevTWD = 0d;
  
  /**
   * @param key
   * @return Damped Data, by default
   */
  @Override
  public /*synchronized*/ Object get(Object key)
  {
    return get(key, true);
  }

  public /*synchronized*/ Object get(Object key, boolean useDamping)
  {
    Object ret = null;
    try
    {
  //  System.out.println("Damping = " + dampingSize);
      if (useDamping && dampingSize > 1 && dampingMap != null && dampingMap.containsKey(key))
      {
        Class cl = null;
        List<?> ald = dampingMap.get(key);
        double sum = 0d;
        double sumCos = 0d,
               sumSin = 0d;
        
        for (Object v : ald)
        {
          if (cl == null)
            cl = v.getClass();
          if (v instanceof Double)
            sum += ((Double)v).doubleValue();
          else if (v instanceof NMEADoubleValueHolder)
          {
            // Debug
            if (false && key.equals(TWD))
              System.out.print(((NMEADoubleValueHolder)v).getDoubleValue() + ";");
  
            if (v instanceof Angle) // Angle360 || v instanceof Angle180 || v instanceof Angle180EW || v instanceof Angle180LR)
            {
              double val = ((NMEADoubleValueHolder)v).getDoubleValue();
              sumCos += (Math.cos(Math.toRadians(val)));
              sumSin += (Math.sin(Math.toRadians(val)));
            }
            else
              sum += ((NMEADoubleValueHolder)v).getDoubleValue();
          }
          else
            System.out.println("What'zat:" + v.getClass().getName());
        }
        try
        {
          if (ald.size() != 0) // Average here
          {
            sum    /= ald.size();
            sumCos /= ald.size();
            sumSin /= ald.size();
          }
          if (cl != null)
          {          
            if (cl.equals(Double.class))
            {
              ret = new Double(sum);
            }
            else
            {
              ret = Class.forName(cl.getName()).newInstance();
              if (ret instanceof Angle) // Angle360 || ret instanceof Angle180 || ret instanceof Angle180EW || ret instanceof Angle180LR)
              {
                double a = Math.toDegrees(Math.acos(sumCos));
                if (sumSin < 0)
                  a = 360d - a;
                sum = a;
                if (DEBUG && key.equals(TWD))
                {
                  System.out.println(" Average:" + sum);
                  if (ald.size() == dampingSize && Math.abs(sum - prevTWD) > 2)
                    System.out.println("Honk!!");
                  prevTWD = sum;
                }
              }
              ((NMEADoubleValueHolder)ret).setDoubleValue(sum);
              
              if (DEBUG) 
              {
                double orig = 0;
                if (cl.equals(Double.class))
                  orig = ((Double)super.get(key)).doubleValue();
                else
                  orig = ((NMEADoubleValueHolder)super.get(key)).getDoubleValue();
                System.out.println("Damping on " + dampingSize + " value(s):" + sum + " instead of " + orig + " for " + key);
              }
            }
          }
          else
            ret = super.get(key);
        }
        catch (Exception ex)
        {
          System.err.println("For key:" + key);
          ex.printStackTrace();
        }
      }
      else
      {
        if (!TIME_RUNNING.equals(key) || (!originalCache && TIME_RUNNING.equals(key)))
        {
          Object o = null;
          synchronized(this) { o = super.get(key); } 
          return o;
        }
        else
        {
          long age = System.currentTimeMillis() - started;
          ret = new Long(age);
        }
      }
    }
    catch (ConcurrentModificationException cme)
    {
      System.err.println("Conflict for key [" + key + "] -> " + cme.toString());      
    }
    return ret;
  }

  public void setDampingSize(int dampingSize)
  {
    System.out.println("Setting Damping to " + dampingSize);
    this.dampingSize = dampingSize;
    NMEAContext.getInstance().fireDataChanged();
  }

  public int getDampingSize()
  {
    return dampingSize;
  }
  
  public void resetDampingBuffers()
  {
    Set<String> keys = dampingMap.keySet();
    for (String k : keys)
      dampingMap.get(k).clear();
  }
  
  public static class CurrentDefinition
  {
    private long bufferLength; // in ms
    private Speed speed;

    public long getBufferLength()
    {
      return bufferLength;
    }

    public Speed getSpeed()
    {
      return speed;
    }

    public Angle360 getDirection()
    {
      return direction;
    }
    private Angle360 direction;
    
    public CurrentDefinition(long bl, Speed sp, Angle360 dir)
    {
      this.bufferLength = bl;
      this.speed = sp;
      this.direction = dir;
    }
  }
  
  private static String generateCacheAge(String devicePrefix, long age)
  {
    String std = devicePrefix + "STD,";
    std += Long.toString(age);
    // Checksum
    int cs = StringParsers.calculateCheckSum(std);
    std += ("*" + lpad(Integer.toString(cs, 16).toUpperCase(), "0", 2));    
    return "$" + std;
  }
  
  private static String lpad(String s, String with, int len)
  {
    String str = s;
    while (str.length() < len)
      str = with + str;
    return str;
  }
  
  private void broadcastNMEASentence(String nmea)
  {
    try
    {
      synchronized (NMEAContext.getInstance().getNMEAListeners())
      {
        for (NMEAListener l : NMEAContext.getInstance().getNMEAListeners())
        {
          synchronized (l)
          {
            try { l.dataDetected(new NMEAEvent(this, nmea)); }
            catch (Exception err)
            {
              err.printStackTrace();
            }
          }
        }
      }
    }
    catch (ConcurrentModificationException cme)
    {
      System.err.println("Managed (1)................");
      cme.printStackTrace();
      System.err.println("..........................");
    }
    
    try
    {
      synchronized (NMEAContext.getInstance().getReaderListeners())
      {
        for (NMEAReaderListener l : NMEAContext.getInstance().getReaderListeners())
        {
          synchronized (l)
          {
            try { l.manageNMEAString(nmea); } 
            catch (Exception err)
            {
              err.printStackTrace();
            }
          }
        }
      }
    }
    catch (ConcurrentModificationException cme)
    {
      System.err.println("Managed (2)................");
      cme.printStackTrace();
      System.err.println("..........................");
    }
  }
  
  private void startBroadcastingCacheAge()
  {
    long sleepTime = 1000L;
    try
    {
      sleepTime = Long.parseLong(System.getProperty("cache.age.sleep.time", "1000"));
    }
    catch (Exception ex)
    {
      System.err.println("Cache Age Sleep Time:" + ex.getLocalizedMessage());
    }
    final long _sleepTime = sleepTime; // Faut pas m'faire chier.
    Thread ageBroadcaster = new Thread("CacheAgeBroadcaster")
    {
      public void run()
      {
        while (true)
        {
          try
          {
            long age = ((Long)get(TIME_RUNNING)).longValue();
            String nmeaAge = generateCacheAge("RP", age);
            // ConcurrentModification Exception
     /*     synchronized (instance) */ { broadcastNMEASentence(nmeaAge); }
//          System.out.println(">>> DEBUG >>> Broadcasted: " + nmeaAge);
          }
          catch (Exception ex)
          {
            // Do not stop!
            ex.printStackTrace();
          }
          try { Thread.sleep(_sleepTime); } catch (Exception ex) {}
        }
      }
    };
    ageBroadcaster.start();
  }
}
