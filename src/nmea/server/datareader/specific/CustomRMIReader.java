package nmea.server.datareader.specific;

import java.net.InetAddress;
import java.net.URLEncoder;

import java.rmi.Naming;
import java.rmi.NotBoundException;

import java.util.List;

import rmi.nmea.client.NMEAClient;

import rmi.nmea.rmiserver.RemoteNMEAInterface;

import nmea.server.constants.Constants;
import nmea.server.ctx.NMEAContext;
import nmea.server.ctx.NMEADataCache;
import nmea.server.datareader.DataReader;

import ocss.nmea.api.NMEAEvent;
import ocss.nmea.api.NMEAListener;
import ocss.nmea.api.NMEAParser;
import ocss.nmea.api.NMEAReader;


public class CustomRMIReader extends NMEAReader implements DataReader
{
  private int rmiport     = 1099;
  private String hostName = "localhost";
  
  private NMEAClient nmeaClient = null;

  public CustomRMIReader(List<NMEAListener> al)
  {
    super(al);
    NMEAContext.getInstance().addNMEAReaderListener(new nmea.event.NMEAReaderListener(Constants.NMEA_SERVER_LISTENER_GROUP_ID)
      {
        public void stopReading()
          throws Exception
        {
          closeReader();
        }
      });
  }

  public CustomRMIReader(List<NMEAListener> al, int rmi)
  {
    super(al);
    rmiport = rmi;
    NMEAContext.getInstance().addNMEAReaderListener(new nmea.event.NMEAReaderListener(Constants.NMEA_SERVER_LISTENER_GROUP_ID)
      {
        public void stopReading()
          throws Exception
        {
          closeReader();
        }
      });
  }

  public CustomRMIReader(List<NMEAListener> al, String host, int rmi)
  {
    super(al);
    hostName = host;
    rmiport = rmi;
    NMEAContext.getInstance().addNMEAReaderListener(new nmea.event.NMEAReaderListener(Constants.NMEA_SERVER_LISTENER_GROUP_ID)
      {
        public void stopReading()
          throws Exception
        {
          closeReader();
        }
      });
  }

  private String lastSentenceInCache = "";
  
  public void read()
  {
    System.out.println("From " + getClass().getName() + " Reading RMI Port " + rmiport + " on " + hostName);
    super.enableReading();
    try
    {
//    InetAddress address = InetAddress.getByName(hostName);
      nmeaClient = new NMEAClient()
        {
          public void notify(NMEADataCache nmeaDataCache)
          {
            String lastSentence = (String)nmeaDataCache.get(NMEADataCache.LAST_NMEA_SENTENCE);
            if (!lastSentenceInCache.equals(lastSentence))
            {
              NMEAEvent n = new NMEAEvent(this, lastSentence + NMEAParser.getEOS());
              fireDataRead(n);
              NMEAContext.getInstance().fireBulkDataRead(n);
            }
          }
        };

      String serverName = "//" + hostName + ":" + Integer.toString(rmiport) + "/" + URLEncoder.encode("nmea", "UTF-8");      
      try
      {
        int counter = 0;
        boolean ok = false;
        while (!ok && counter < 5) // Try at most 5 times
        {
          try 
          { 
            nmeaClient.setNmeaServer((RemoteNMEAInterface) Naming.lookup(serverName));  // Lookup 
            ok = true;
          }
          catch (NotBoundException nbe)
          {
            counter++;
            System.out.println("Retrying...");
            Thread.sleep(1000L); 
          }
        }
        if (counter == 5)
        {
          System.out.println("Check the server...");
          throw new RuntimeException("Connection failed (5 times)");
        }
        nmeaClient.getNmeaServer().registerForNotification(nmeaClient);  // Register client
        System.out.println("Registering [" + InetAddress.getLocalHost().getHostName() + "] with Server");
      }
      catch (Exception e)
      {
        e.printStackTrace();
      }


//      while (canRead())
//      {
//        super.fireDataRead(new NMEAEvent(this, s));
//      }
//      System.out.println("Stop Reading RMI port.");
    }
    catch(Exception e)
    {
      manageError(e);
    }
  }

  public void closeReader() throws Exception
  {
    System.out.println("(" + this.getClass().getName() + ") Stop Reading RMI Port");
    nmeaClient.unregister();    
  }

  public void manageError(Throwable t)
  {
    throw new RuntimeException(t);
  }

  public void setTimeout(long timeout)
  { /* Not used for RMI */  }
}
