package nmea.server.datareader.specific;


import java.io.BufferedReader;

import nmea.server.ctx.NMEAContext;

import java.io.File;
import java.io.FileInputStream;

import nmea.server.datareader.DataReader;

import ocss.nmea.api.NMEAReader;
import ocss.nmea.api.NMEAEvent;
import java.util.ArrayList;

import java.util.List;

import nmea.server.constants.Constants;

import ocss.nmea.api.NMEAListener;

/**
 * A Simulator, taking its inputs from a file
 */
public class CustomFileReader extends NMEAReader implements DataReader
{
  private File dataFile = null;
  private long recNum = 0;
  
  private long jumpToOffset = -1;
  
  public CustomFileReader(List<NMEAListener> al, File f)
  {
    super(al);
    dataFile = f;
    NMEAContext.getInstance().addNMEAReaderListener(new nmea.event.NMEAReaderListener(Constants.NMEA_SERVER_LISTENER_GROUP_ID)
      {
        public void replaySpeedChanged(int slider) 
        {
          // 50 :  1000
          // 100:     1
          // 0  : 10000
          int x = 100 - slider;
          double d = x / 50d;
          sleepTime = (int)(1000d * (Math.pow(d, 4)));
//        System.out.println("Sleeptime:" + sleepTime);
        }
        public void stopReading()
          throws Exception
        {
          closeReader();
        }
        public void jumpToOffset(long recordOffset) 
        {
          jumpToOffset = recordOffset;
        }
      });
  }
  
  private FileInputStream fis = null;
  private long sleepTime = 1000L;
  
  public void read()
  {
    // Simulation
    super.enableReading();
    try
    {
      fis = new FileInputStream(dataFile);
//    InputStream fis = this.getClass().getResourceAsStream(fileName);
//    assert fis != null;
      while (canRead())
      {
        if (jumpToOffset != -1)
        {
          fis.close();
          fis = new FileInputStream(dataFile);
          recNum = 0;
          byte[] ba = new byte[250];
          while (recNum < jumpToOffset)
          {
            int l = fis.read(ba);
            if (l != -1)
            {
              String nmeaContent = new String(ba);
              recNum += nbNMEASentences(nmeaContent);
            }
          }
          NMEAContext.getInstance().setReplayFileRecNum(recNum);
          jumpToOffset = -1;
        }
        double size = Math.random();
        int dim = (int)(750 * size);
        if (dim > 0)
        {
          byte[] ba = new byte[dim];
          
          int l = fis.read(ba);
  //      System.out.println("Read " + l);
          if (l != -1)
          {
            String nmeaContent = new String(ba);
            recNum += nbNMEASentences(nmeaContent);
            NMEAContext.getInstance().setReplayFileRecNum(recNum);
            NMEAEvent n = new NMEAEvent(this, nmeaContent);
            super.fireDataRead(n);
            NMEAContext.getInstance().fireBulkDataRead(n);
            try { Thread.sleep(sleepTime); } catch (Exception ignore) {}
          }
          else
          {
            System.out.println("===== Reseting Reader =====");
            fis.close();
            fis = new FileInputStream(dataFile);
            recNum = 0;
  //        fis = this.getClass().getResourceAsStream(fileName);
          }
        }
//      else
//        System.out.println("======>>> dim = 0, continuing.");
      }
    }
    catch (Exception e)
    {
     e.printStackTrace();
    }
  }

  private static int nbNMEASentences(String chunk)
  {
    int nbs = 0;
    int idx = chunk.indexOf("$");
    while (idx > -1)
    {
      nbs++;
      idx = chunk.indexOf("$", idx + 1);
    }    
    // For AIS
    idx = chunk.indexOf("!AIVDM");
    while (idx > -1)
    {
      nbs++;
      idx = chunk.indexOf("!AIVDM", idx + "!AIVDM".length());
    }    
    return nbs;
  }
  
  public void closeReader() throws Exception
  {
    System.out.println("Stop reading Data File.");
    try
    {
      if (fis != null)
      {
        this.goRead = false;
        fis.close();
        fis = null;
      }
    }
    catch (Exception ex)
    {
      ex.printStackTrace();
    }
  }

  public void manageError(Throwable t)
  {
    throw new RuntimeException(t);
  }
  
  public void setTimeout(long timeout)
  { /* Not used for File Reader */  }
  
  public static void main4tests(String[] args)
  {
    String s = "26.5,C*39$IIMWV,238,R,17.4,N,A*18!AIVDM,1,1,,A,13cie15001Qj26pRJjjSIVtr0@BS,0*02$IIMWV,225,T,21.4,N,A*17$IIRMC,220712,A,0906.452,S,14012.516,W,06.6,227,211110,10,E,A*0B$IIVHW,,,220,M,06.1,N,,*63$IIVLW,03013,N,012.2,N*53$IIVWR,122,L,17.4,N,,,,*7C$IIDPT,000.9,+0.7,*49$IIGLL,0906.455,S,14012.519,W,220714,A,A*5D$IIHDG,221,,,10,E*12$IIMTW,+26.5,C*39$IIMWV,232,R,16.2,N,A*15$IIMWV,224,T,21.2,N,A*10$IIRMB,A,3.00,R,,RANGI   ,,,,,561.80,230,06.5,V,A*0F$IIRMC,220714,A,0906.455,S,14012.519,W,06.6,227,211110,10,E,A*05$IIVHW,,,221,M,06.0,N,,*63$IIVLW,03013,N,012.2,N*53$IIVWR,130,L,15.5,N,,,,*7C$IIDPT,000.9,+0.7,*49$IIGLL,0906.455,S,14012.519,W,220714,A,A*5D$IIHDG,220,,,10,E*13$IIMTW,+26.5,C*39$IIMWV,230,R,15.5,N,A*";
    int nb = nbNMEASentences(s);
    System.out.println("Found " + nb + " sentence(s).");
  }
}
