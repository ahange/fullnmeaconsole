package nmea.server.datareader;

import nmea.server.NMEAEventManager;

import java.io.File;

import nmea.server.datareader.specific.CustomFileReader;

import nmea.server.datareader.specific.CustomSerialReader;

import nmea.server.datareader.specific.CustomTCPReader;

import ocss.nmea.api.NMEAClient;
import ocss.nmea.api.NMEAEvent;
/**
 * Used by the MinimalReader
 * @deprecated
 * @see CustomNMEAClient
 */
@Deprecated
public class CustomClient extends NMEAClient
{
  private NMEAEventManager parent;
  private String port = "";
  private int br = 4800;
  private int tcpport = -1;
  private File data = null;

  public CustomClient(NMEAEventManager parent, String p, int br, String prefix, String array[])
  {
    port = p;
    this.br = br;
    setDevicePrefix(prefix);
    setSentenceArray(array);
    this.parent = parent;
    init();
  }

  // TODO Add option for UDP
  public CustomClient(NMEAEventManager parent, int tcp, String prefix, String array[])
  {
    tcpport = tcp;
    setDevicePrefix(prefix);
    setSentenceArray(array);
    this.parent = parent;
    init();
  }

  public CustomClient(NMEAEventManager parent, File f, String prefix, String array[])
  {
    data = f;
    setDevicePrefix(prefix);
    setSentenceArray(array);
    this.parent = parent;
    this.setEOS("\n");
    init();
  }

  private void init()
  {
    super.initClient();
    if (data != null)
      this.setReader(new CustomFileReader(getListeners(), data));
    else if (tcpport == -1)
      this.setReader(new CustomSerialReader(getListeners(), port, br));
    else
      this.setReader(new CustomTCPReader(getListeners(), tcpport));
    super.startWorking();
    System.out.println(this.getClass().getName() + ":NMEA Client initialized and started");
  }

  public void dataDetectedEvent(NMEAEvent e)
  {
    String val = e.getContent();
//  if (parent.verbose())
//    System.out.println("Detected:" + val);
    parent.manageDataEvent(val);
  }

}
