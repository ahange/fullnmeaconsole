package nmea.server.datareader.specific;

import nmea.server.ctx.NMEAContext;

import java.io.InputStream;

import java.net.BindException;
import java.net.ConnectException;
import java.net.InetAddress;
import java.net.Socket;

import java.net.SocketException;

import java.util.ArrayList;
import java.util.Date;

import java.util.List;

import nmea.server.constants.Constants;

import nmea.server.datareader.DataReader;

import nmea.server.utils.DumpUtil;
import ocss.nmea.api.NMEAEvent;
import ocss.nmea.api.NMEAListener;
import ocss.nmea.api.NMEAParser;
import ocss.nmea.api.NMEAReader;
/**
 * Works with SailMail rebroadcast
 */
public class CustomTCPReader extends NMEAReader implements DataReader
{
  private int tcpport     = 80;
  private String hostName = "localhost";

  public CustomTCPReader(List<NMEAListener> al)
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

  public CustomTCPReader(List<NMEAListener> al, int tcp)
  {
    super(al);
    tcpport = tcp;
    NMEAContext.getInstance().addNMEAReaderListener(new nmea.event.NMEAReaderListener(Constants.NMEA_SERVER_LISTENER_GROUP_ID)
      {
        public void stopReading()
          throws Exception
        {
          closeReader();
        }
      });
  }

  public CustomTCPReader(List<NMEAListener> al, String host, int tcp)
  {
    super(al);
    hostName = host;
    tcpport = tcp;
    NMEAContext.getInstance().addNMEAReaderListener(new nmea.event.NMEAReaderListener(Constants.NMEA_SERVER_LISTENER_GROUP_ID)
      {
        public void stopReading()
          throws Exception
        {
          closeReader();
        }
      });
  }

  private Socket skt = null;
  
  public void read()
  {
    boolean verbose = "true".equals((System.getProperty("verbose", "false")));
    System.out.println("From " + getClass().getName() + " Reading TCP Port " + tcpport + " on " + hostName);
    super.enableReading();
    try
    {
      InetAddress address = InetAddress.getByName(hostName);
//    System.out.println("INFO:" + hostName + " (" + address.toString() + ")" + " is" + (address.isMulticastAddress() ? "" : " NOT") + " a multicast address");
      skt = new Socket(address, tcpport);
      
      InputStream theInput = skt.getInputStream();
      byte buffer[] = new byte[4096];
      String s;
      int nbReadTest = 0;
      while (canRead())
      {
        int bytesRead = theInput.read(buffer);
        if (bytesRead == -1)
        {
          System.out.println("Nothing to read...");
          if (nbReadTest++ > 10)
            break;
        }
        else
        {
          if (verbose)
          {
            System.out.println("# Read " + bytesRead + " characters");
            System.out.println("# " + (new Date()).toString());
          }
          int nn = bytesRead;
          for(int i = 0; i < Math.min(buffer.length, bytesRead); i++)
          {
            if(buffer[i] != 0)
              continue;
            nn = i;
            break;
          }
  
          byte toPrint[] = new byte[nn];
          for(int i = 0; i < nn; i++)
            toPrint[i] = buffer[i];
  
          s = new String(toPrint).trim() + NMEAParser.getEOS();
  //      System.out.println("TCP:" + s);
          DumpUtil.displayDualDump(s);
          NMEAEvent n = new NMEAEvent(this, s);
          super.fireDataRead(n);
          NMEAContext.getInstance().fireBulkDataRead(n);
        }
      }

      System.out.println("Stop Reading TCP port.");
      theInput.close();
    }
    catch (BindException be)
    {
      System.err.println("From " + this.getClass().getName() + ", " + hostName + ":" + tcpport);
      be.printStackTrace();   
      manageError(be);
    }
    catch (final SocketException se)
    {
//    se.printStackTrace();
      if (se.getMessage().indexOf("Connection refused") > -1)
        System.out.println("Refused (1)");
      else if (se.getMessage().indexOf("Connection reset") > -1)
        System.out.println("Reset (2)");
      else
      {
        boolean tryAgain = false;
        if (se instanceof ConnectException && "Connection timed out: connect".equals(se.getMessage()))
          tryAgain = true;
        else if (se instanceof ConnectException && "Network is unreachable: connect".equals(se.getMessage())) 
          tryAgain = true;
        else if (se instanceof ConnectException) // Et hop!
        {
          tryAgain = false;
          System.err.println("TCP :" + se.getMessage());
        }
        else 
        {
          tryAgain = false;
          System.err.println("TCP Socket:" + se.getMessage());
        }
        
        if (tryAgain)
        {
          // Wait and try again
          try
          {
//            Thread userThread = new Thread("TCPReader")
//            {
//              public void run()
//              {
//                System.out.println("TCP Thread...");
//                try 
//                { 
//                  System.out.println(se.getMessage() + ", Timeout on TCP.\nWill re-try to connect again...");
////                JOptionPane.showMessageDialog(null, se.getMessage() + ", Timeout on TCP.\nWill re-try to connect again...", "TCP Connection", JOptionPane.WARNING_MESSAGE);  
//                }
//                catch (Exception ex)
//                {
//                  System.out.println("Timeout on TCP.\nWill re-try to connect again...");
//                }
//              }
//            };
//            userThread.start();
            System.out.println("Timeout on TCP. Will Re-try to connect in 1s");
            closeReader();
            Thread.sleep(1000L);
            System.out.println("Re-trying now. (from " + this.getName() + ")");
            read();
          }
          catch (Exception ex)
          {
            ex.printStackTrace();
          }
        }
        else
          manageError(se);
      }
    }
    catch(Exception e)
    {
//    e.printStackTrace();
      manageError(e);
    }
  }

  public void closeReader() throws Exception
  {
//  System.out.println("(" + this.getClass().getName() + ") Stop Reading TCP Port");
    try
    {
      if (skt != null)
      {
        this.goRead = false;
        skt.close();
        skt = null;
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
  { /* Not used for TCP */  }
  
  public static void main(String[] args)
  {
    String host = "192.168.1.136";
    int port = 7001; // 2947
    try
    {
      List<NMEAListener> ll = new ArrayList<NMEAListener>();
      NMEAListener nl = new NMEAListener()
      {
        @Override
        public void dataRead(NMEAEvent nmeaEvent)
        {
          DumpUtil.displayDualDump(nmeaEvent.getContent());
//        System.out.println(nmeaEvent.getContent().trim()); // TODO Send to the GUI?
        }
      };
      ll.add(nl);
      
      boolean keepTrying = true;
      while (keepTrying)
      {
        CustomTCPReader ctcpr = new CustomTCPReader(ll, host, port);
        System.out.println(new Date().toString() + ": New " + ctcpr.getClass().getName() + " created.");

        try { ctcpr.read(); }
        catch (Exception ex)
        {
          System.err.println("TCP Reader:" + ex.getMessage());
          ctcpr.closeReader();
          long howMuch = 1000L;
          System.out.println("Will try to reconnect in " + Long.toString(howMuch) + "ms.");
          try { Thread.sleep(howMuch); } catch (InterruptedException ie) {}
        }
      }
    }
    catch (Exception e)
    {
      e.printStackTrace();
    }
  }
}
