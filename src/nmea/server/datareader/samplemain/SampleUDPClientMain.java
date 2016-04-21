package nmea.server.datareader.samplemain;

import nmea.server.NMEAEventManager;
import nmea.server.datareader.CustomNMEAClient;

public class SampleUDPClientMain
  implements NMEAEventManager
{
  public SampleUDPClientMain()
  {
    super();
  }

  public void manageDataEvent(String s)
  {
    System.out.println("Received:" + s);
  }

  public boolean verbose()
  {
    return false;
  }
  
  public static void main(String[] args) throws Exception
  {
    SampleUDPClientMain me = new SampleUDPClientMain();
    CustomNMEAClient cnc = new CustomNMEAClient(me, CustomNMEAClient.UDP_OPTION, "230.0.0.1", 8001) //, 10000L)
    {
      public void manageNMEAError(Throwable t)
      {
        System.err.println("Oops:" + t.toString());
      }
    };
//  cnc.startWorking();
  }
}
