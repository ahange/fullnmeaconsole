package nmea.server.datareader.specific;

import coreutilities.Utilities;

import nmea.server.ctx.NMEAContext;

import java.io.InputStream;

import gnu.io.CommPort;
import gnu.io.NoSuchPortException;
import gnu.io.UnsupportedCommOperationException;
import gnu.io.SerialPort;
import gnu.io.CommPortIdentifier;
import gnu.io.PortInUseException;

import java.util.List;

import javax.swing.JOptionPane;

import nmea.server.constants.Constants;

import nmea.server.datareader.DataReader;

import ocss.nmea.api.NMEAEvent;
import ocss.nmea.api.NMEAListener;
import ocss.nmea.api.NMEAReader;

public class CustomSerialReader
     extends NMEAReader
  implements DataReader
{
  private String comPort = "COM1";
  private int br = 4800;
  private long timeout = -1L; // Default value
  private String dataRead = null;

  private CommPort thePort = null;

  public CustomSerialReader(List<NMEAListener> al)
  {
    super(al);
    NMEAContext.getInstance().addNMEAReaderListener(new nmea.event.NMEAReaderListener(Constants.NMEA_SERVER_LISTENER_GROUP_ID)
      {
        public void stopReading()
          throws Exception
        {
          closeReader();
        }

        @Override
        public void fireError(Throwable t)
        {
          manageError(t);
        }
      });
  }

  public CustomSerialReader(List<NMEAListener> al, String com, int br)
  {
    super(al);
    comPort = com;
    this.br = br;
    NMEAContext.getInstance().addNMEAReaderListener(new nmea.event.NMEAReaderListener(Constants.NMEA_SERVER_LISTENER_GROUP_ID)
      {
        public void stopReading()
          throws Exception
        {
          closeReader();
        }

        @Override
        public void fireError(Throwable t)
        {
          manageError(t);
        }
      });
  }

  public void read()
  {
    if (System.getProperty("verbose", "false").equals("true")) 
      System.out.println("read() - From " + this.getClass().getName() + " Reading Serial Port " + comPort + ":" + Integer.toString(this.br));
    super.enableReading();
    CommPortIdentifier com = null;
    try
    {
      com = CommPortIdentifier.getPortIdentifier(comPort);
    }
    catch (NoSuchPortException nspe)
    {
//    nspe.printStackTrace();
//    System.err.println(this.getClass().getName() + ":read() - No Such Port");
//    JOptionPane.showMessageDialog(null, "No such port " + comPort + "!", "Error opening port", JOptionPane.ERROR_MESSAGE);
      manageError(nspe);
//    throw new RuntimeException(nspe);
      return;
    }
    catch (Exception ex)
    {
      manageError(ex);
    }
    try
    {
      thePort = com.open("PortOpener", 10);
    }
    catch (final PortInUseException piue)
    {
//      Thread errorThread = new Thread()
//        {
//          public void run()
//          {
//            JOptionPane.showMessageDialog(null, comPort + ":" + Integer.toString(br) + " in use.\n" + piue.getLocalizedMessage(), "Error opening port", JOptionPane.ERROR_MESSAGE);   
//          }
//        };
//      errorThread.start(); 
      System.out.println(comPort + ":" + Integer.toString(br) + " in use.\n" + piue.getLocalizedMessage());   
      manageError(piue);
//    throw new RuntimeException(piue);
      return;
    }
    catch (Exception ex)
    {
      manageError(ex);
    }
    int portType = com.getPortType();
    if (System.getProperty("verbose", "false").equals("true"))
    {
      if (portType == CommPortIdentifier.PORT_PARALLEL)
        System.out.println(this.getClass().getName() + ":read() - This is a parallel port");
      else if (portType == CommPortIdentifier.PORT_SERIAL)
        System.out.println(this.getClass().getName() + ":read() - This is a serial port");
      else
        System.out.println(this.getClass().getName() + ":read() - This is an unknown port:" + portType);
    }
    if (portType == CommPortIdentifier.PORT_SERIAL)
    {
      SerialPort sp = (SerialPort) thePort;
      try
      {
        sp.setSerialPortParams(br, SerialPort.DATABITS_8, SerialPort.STOPBITS_1, SerialPort.PARITY_NONE);
      }
      catch (UnsupportedCommOperationException ucoe)
      {
        System.err.println("read() - Unsupported Comm Operation");
        manageError(ucoe);
        return;
      }
      catch (Exception ex)
      {
        manageError(ex);
      }
    }
    try
    {
      // Read the port here
      InputStream theInput = thePort.getInputStream();
      boolean verbose = System.getProperty("verbose", "false").equals("true");
      if (verbose) System.out.println(this.getClass().getName() + ":read() - Reading serial port...");
      byte buffer[] = new byte[4096];

      while (canRead())
      {
        this.dataRead = null;
        if (this.timeout == -1) // No timeout
        {
          int bytesRead = theInput.read(buffer);
          if (bytesRead == -1)
            break;
//        System.out.println("# Read " + bytesRead + " characters");
//        System.out.println("# " + (new Date()).toString());
          int nn = bytesRead;
          for (int i = 0; i < Math.min(buffer.length, bytesRead); i++)
          {
            if (buffer[i] != 0)
              continue;
            nn = i;
            break;
          }
          byte toPrint[] = new byte[nn];
          for (int i = 0; i < nn; i++)
            toPrint[i] = buffer[i];

          setDataRead(new String(toPrint));
        }
        else
        {
          if (verbose) 
            System.out.println("Reading serial port (" + comPort + ":" + Integer.toString(br) + ") with timeout (" + timeout + " ms)");
          // Wait here.
          Thread waiter = Thread.currentThread();
          SerialReceiveThread drt = new SerialReceiveThread(theInput, waiter, this);
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
              if (verbose) 
              {
                System.out.println("- (Serial " + comPort + ":" + Integer.toString(br) + ") Done waiting (" + Long.toString(after - before) + " vs " + Long.toString(timeout) + ")");
                System.out.println("    -> data read " + (dataRead==null?"[null]":Integer.toString(dataRead.length()) + " byte(s) [" + dataRead + "]"));
              }
              if (drt.isAlive())
              {
  //            System.out.println("Interrupting the DatagramReceiveThread");
                drt.interrupt(); 
//              if (timeout != -1 && (after - before) >= timeout)
//                throw new RuntimeException("Serial reader on " + comPort + ":" + Integer.toString(br) + "took too long (" + Long.toString(after - before) + ").");
              }
              if (timeout != -1 && (after - before) >= timeout)
                throw new RuntimeException("Serial reader on " + comPort + ":" + Integer.toString(br) + " took too long (" + Long.toString(after - before) + ").");
            }
            catch (InterruptedException ie) 
            { 
              if (verbose) 
                System.out.println("Waiter Interrupted! (before end of wait, good)");              
            }
          }    
        }
//      System.out.println(s);
//      if (System.getProperty(this.getClass().getName() + ".verbose", "false").equals("true")) 
        if (Utilities.thisClassVerbose(this.getClass())) // nmea.server.datareader.CustomNMEAClient$1
          System.out.println("DataRead - " + this.getClass().getName() + ": Reading serial port [" + comPort + "]:[" + this.dataRead + "]");
        
        NMEAEvent n = new NMEAEvent(this, this.dataRead);
        super.fireDataRead(n);                              // TODO Untangle the 2 messages... First listener must not be registered correctly
        NMEAContext.getInstance().fireBulkDataRead(n);
      }
//    if (System.getProperty(this.getClass().getName() + ".verbose", "false").equals("true")) 
      if (Utilities.thisClassVerbose(this.getClass()))
        System.out.println("2 - " + this.getClass().getName() + ":Stop Reading serial port [" + comPort + "]");
    }
    catch (Exception e)
    {
//    e.printStackTrace();
      manageError(e);
    }    
  }

  public void closeReader()
    throws Exception
  {
//  if (System.getProperty("verbose", "false").equals("true")) 
    if (Utilities.thisClassVerbose(this.getClass()))
      System.out.println("1 - Stop reading Serial Port " + comPort);
    try
    {
      if (thePort != null)
      {
        this.goRead = false;
        thePort.close();
        thePort = null;
      }
    }
    catch (Exception ex)
    {
      throw ex;
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

  public void setDataRead(String s)
  {
    this.dataRead = s;
  }
  
  private class SerialReceiveThread extends Thread
  {
    private InputStream is = null;
    private DataReader parent = null;
    private Thread waiter;
    private String s;
    byte buffer[] = new byte[4096];
    
    public SerialReceiveThread(InputStream is, Thread from, DataReader dr)
    {
      super();
      this.is = is;
      this.parent = dr;
      this.waiter = from;
    }
    
    public void run()
    {
      try 
      { 
        int bytesRead = is.read(buffer);
        if (bytesRead == -1)
          return; // break;
        if ("true".equals(System.getProperty("verbose", "false")))
          System.out.println(this.getClass().getName() +  " # Read " + bytesRead + " characters");
//      System.out.println("# " + (new Date()).toString());
        int nn = bytesRead;
        for (int i = 0; i < Math.min(buffer.length, bytesRead); i++)
        {
          if (buffer[i] != 0)
            continue;
          nn = i;
          break;
        }
        byte toPrint[] = new byte[nn];
        for (int i = 0; i < nn; i++)
          toPrint[i] = buffer[i];

        s = new String(toPrint);
        setDataRead(s);

        synchronized (waiter) 
        { 
//        System.out.println("Notifying waiter (Done).");
          waiter.notify(); 
        }
      }
      catch (Exception ex)
      {
//      ex.printStackTrace();
        parent.manageError(ex);
      }
    }
  }
}
