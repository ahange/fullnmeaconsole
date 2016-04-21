package nmea.server.datareader.specific;

import nmea.server.ctx.NMEAContext;

import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;
import java.net.MulticastSocket;

import java.net.SocketException;

import java.util.List;

import nmea.server.constants.Constants;

import nmea.server.datareader.DataReader;

import ocss.nmea.api.NMEAEvent;
import ocss.nmea.api.NMEAListener;
import ocss.nmea.api.NMEAParser;
import ocss.nmea.api.NMEAReader;

public class CustomUDPReader extends NMEAReader implements DataReader
{
  private int udpport  = 8001;
  private long timeout = 5000L; // Default value
  private String host = "localhost";

  public CustomUDPReader(List<NMEAListener> al)
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

  public CustomUDPReader(List<NMEAListener> al, int udp)
  {
    super(al);
    udpport = udp;
    NMEAContext.getInstance().addNMEAReaderListener(new nmea.event.NMEAReaderListener(Constants.NMEA_SERVER_LISTENER_GROUP_ID)
      {
        public void stopReading()
          throws Exception
        {
          closeReader();
        }
      });
  }

  public CustomUDPReader(List<NMEAListener> al, String host, int udp)
  {
    super(al);
    udpport = udp;
    this.host = host;
    NMEAContext.getInstance().addNMEAReaderListener(new nmea.event.NMEAReaderListener(Constants.NMEA_SERVER_LISTENER_GROUP_ID)
      {
        public void stopReading()
          throws Exception
        {
          closeReader();
        }
      });
  }

//private DatagramSocket  skt   = null;
//private MulticastSocket skt   = null;
  private InetAddress     group  = null;
  private DatagramSocket dsocket = null;
  
  private boolean verbose = System.getProperty("verbose", "false").equals("true");

  public void read()
  {
    System.out.println("From " + getClass().getName() + " Reading UDP Port " + udpport);
    super.enableReading();
    try
    {
      InetAddress address = InetAddress.getByName(host);
      System.out.println("INFO:" + host + " (" + address.toString() + ")" + " is" + (address.isMulticastAddress() ? "" : " NOT") + " a multicast address");

//    skt = new MulticastSocket(udpport);
//    group = InetAddress.getByName(host); // ("230.0.0.1");
//    skt.joinGroup(group);

      if (address.isMulticastAddress())
      {
        dsocket = new MulticastSocket(udpport);
        ((MulticastSocket)dsocket).joinGroup(address);     
        group = address;
      }  
      else
        dsocket = new DatagramSocket(udpport, address);


      byte buffer[] = new byte[4096];
      String s;
      while (canRead())
      {
        DatagramPacket packet = new DatagramPacket(buffer, buffer.length);
        // Wait here.
        Thread waiter = Thread.currentThread();
        DatagramReceiveThread drt = new DatagramReceiveThread(dsocket, packet, waiter, this);
        drt.start();

        synchronized (waiter)
        {
          try 
          { 
            long before = System.currentTimeMillis();
            if (timeout > -1)
              waiter.wait(timeout);  
            else
              waiter.wait();
            long after = System.currentTimeMillis();
            if (verbose) System.out.println("- (UDP) Done waiting (" + Long.toString(after - before) + " vs " + Long.toString(timeout) + ")");
            if (drt.isAlive())
            {
//            System.out.println("Interrupting the DatagramReceiveThread");
              drt.interrupt(); 
              if (timeout != -1 && (after - before) >= timeout)
                throw new RuntimeException("UDP took too long.");
            }
          }
          catch (InterruptedException ie) 
          { 
            if (verbose) System.out.println("Waiter Interrupted! (before end of wait, good)");              
          }
        }    
        s = new String(buffer, 0, packet.getLength());
        // For simulation from file:
        if (System.getProperty("os.name").toUpperCase().contains("LINUX"))
        {
          if (s.endsWith(NMEAParser.STANDARD_NMEA_EOS))
            s = s.substring(0, s.length() - NMEAParser.STANDARD_NMEA_EOS.length());
        }
        if (!s.endsWith(NMEAParser.getEOS()))
          s += NMEAParser.getEOS();
        if (verbose) System.out.println("UDP:" + s);
          NMEAEvent n = new NMEAEvent(this, s);
          super.fireDataRead(n);
          NMEAContext.getInstance().fireBulkDataRead(n);
      }
    }
    catch(Exception e)
    {
//    e.printStackTrace();
//    JOptionPane.showMessageDialog(null, "No such UDP port " + udpport + "!", "Error opening port", JOptionPane.ERROR_MESSAGE);
      manageError(e);
    }
    finally
    {
      try
      {
        if (dsocket != null)
        {
          if (dsocket instanceof MulticastSocket)
          {
            if (verbose) System.out.println(">> From " + this.getClass().getName() + ": 1 - Leaving group " + group.toString());
            ((MulticastSocket)dsocket).leaveGroup(group);
          }
          if (verbose) System.out.println(">> From " + this.getClass().getName() + ": 2 - Closing Socket");
          dsocket.close();
        }
        else
          if (verbose) System.out.println("Socket already null (closed)...");
      }
      catch (Exception ex)
      {
        System.err.println(">> Error when Closing Socket...");
        ex.printStackTrace();
      }
//    closeReader();
    }
  }

  public void closeReader() throws Exception
  {
//  System.out.println("(" + this.getClass().getName() + ") Stop Reading UDP Port");
    try
    {
      if (dsocket != null)
      {
        this.goRead = false;
        if (dsocket instanceof MulticastSocket)
        {
          if (verbose) System.out.println(">> From " + this.getClass().getName() + ": 1 - Leaving group " + group.toString());
          if (group != null)
            ((MulticastSocket)dsocket).leaveGroup(group);
          else
            System.out.println(">> Multicast Socket: Group is null.");
        }
        if (verbose) System.out.println(">> From " + this.getClass().getName() + ": 2 - Closing Socket");
        dsocket.close();
        dsocket = null;
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
  {
    this.timeout = timeout;
  }

  public long getTimeout()
  {
    return timeout;
  }

  private class DatagramReceiveThread extends Thread
  {
    private DatagramSocket ds = null;
    private DataReader parent = null;
    private Thread waiter;
    private DatagramPacket packet;
    
    public DatagramReceiveThread(DatagramSocket ds, DatagramPacket packet, Thread from, DataReader dr)
    {
      super();
      this.ds = ds;
      this.parent = dr;
      this.waiter = from;
      this.packet = packet;
    }
    
    public void run()
    {
      try 
      { 
//      dsocket.receive(packet);
        ds.receive(packet);
        synchronized (waiter) 
        { 
//        System.out.println("Notifying waiter (Done).");
          waiter.notify(); 
        }
      }
      catch (SocketException se)
      {
        // Socket closed?
        if (! "socket closed".equals(se.getMessage()))
          System.out.println(">>>>> " + this.getClass().getName() + ":" + se.getMessage());
      }
      catch (Exception ex)
      {
//      ex.printStackTrace();
        parent.manageError(ex);
      }
    }
  }
}
