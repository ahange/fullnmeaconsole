package nmea.server.datareader;

import java.io.File;

import nmea.server.NMEAEventManager;
import nmea.server.datareader.specific.CustomFileReader;
import nmea.server.datareader.specific.CustomRMIReader;
import nmea.server.datareader.specific.CustomSerialReader;
import nmea.server.datareader.specific.CustomTCPReader;
import nmea.server.datareader.specific.CustomUDPReader;

import ocss.nmea.api.NMEAClient;
import ocss.nmea.api.NMEAEvent;


/**
 * Used by the NMEAConsole
 */
public abstract class CustomNMEAClient extends NMEAClient
{
  private NMEAEventManager parent;
  private String port = "";
  private int br = 4800;
  private int dataPort = -1;
  private String host = "localhost";
  private File data = null;
  
  private final static long DEFAULT_TIMEOUT = -1L;
  
  private long timeout = DEFAULT_TIMEOUT;
  
  public final static int TCP_OPTION  = 0;
  public final static int UDP_OPTION  = 1;
  public final static int RMI_OPTION  = 2;
  
  private int option = TCP_OPTION;

  /**
   * Read Serial Port
   * @param parent
   * @param p
   * @param br
   */
  public CustomNMEAClient(NMEAEventManager parent, String p, int br)
  {
    this(parent, p, br, DEFAULT_TIMEOUT);
  }
  /**
   * Read Serial Port
   * @param parent
   * @param p    Serial port name, like COM10
   * @param br   Baud rate, like 4800
   * @param tout Timeout in milliseconds
   */
  public CustomNMEAClient(NMEAEventManager parent, String p, int br, long tout)
  {
    port = p;
    this.br = br;
    setDevicePrefix("*");
    this.parent = parent;
    this.timeout = tout;
    init();
  }

  /**
   * Read TCP/UDP/RMI Port
   * @param parent
   * @param port
   */
  public CustomNMEAClient(NMEAEventManager parent, int option, String host, int port)
  {
    this(parent, option, host, port, DEFAULT_TIMEOUT);
  }
  /**
   * Read TCP/UDP/RMI Port
   * @param parent
   * @param port TCP, RMI or UDP port number, like 8001
   * @param tout Timeout in milliseconds
   */
  public CustomNMEAClient(NMEAEventManager parent, int option, String host, int port, long tout)
  {
    this.option = option;
    this.host = host;
    dataPort = port;
    setDevicePrefix("*");
    this.parent = parent;
    this.timeout = tout;
    init();
  }

  /**
   * Read from log file
   * @param parent
   * @param f
   */
  public CustomNMEAClient(NMEAEventManager parent, File f)
  {
    data = f;
    setDevicePrefix("*");
    this.parent = parent;
    this.setEOS("\n"); // TASK Make sure it is OK. Use NMEAParser.STANDARD_NMEA_EOS otherwise.
    init();
  }
  private DataReader reader = null;
  
  private void init()
  {
    super.initClient();
    if (data != null)
      this.setReader(new CustomFileReader(getListeners(), data));            // File (Simulation, replay)
    else if (dataPort == -1)
    {
      this.setReader(new CustomSerialReader(getListeners(), port, br)        // Serial
        {
          @Override
          public void manageError(Throwable t)
          {
//          System.out.println("Aha!");
//          System.err.println(t.toString());
            manageNMEAError(t);
          }
        });     
      ((CustomSerialReader)this.getReader()).setTimeout(timeout);
    }
    else if (option == TCP_OPTION)
      this.setReader(new CustomTCPReader(getListeners(), host, dataPort)       // TCP
        {
          @Override
          public void manageError(Throwable t)
          {
            manageNMEAError(t);
          }
        });
    else if (option == UDP_OPTION)
    {
      this.setReader(new CustomUDPReader(getListeners(), host, dataPort)       // UDP 
        {
          @Override
          public void manageError(Throwable t)
          {
            manageNMEAError(t);
          }                       
        });
      ((CustomUDPReader)this.getReader()).setTimeout(timeout);
    }
    else if (option == RMI_OPTION)
      this.setReader(new CustomRMIReader(getListeners(), host, dataPort)       // RMI
        {
          @Override
          public void manageError(Throwable t)
          {
            manageNMEAError(t);
          }
        });
    reader = (DataReader)this.getReader();
    
    super.startWorking();
    
    if (System.getProperty("verbose", "false").equals("true")) 
      System.out.println(this.getClass().getName() + ":NMEA Client initialized, and started");
  }

  @Override
  public void dataDetectedEvent(NMEAEvent e)
  {
    String val = e.getContent();
//  if (parent.verbose())
//    System.out.println("Detected:" + val);
    parent.manageDataEvent(val);
  }
  
  public void stopReading() throws Exception
  {
    if (reader != null)
    {
      reader.closeReader();
    }
  }
  
  public abstract void manageNMEAError(Throwable t);
}
