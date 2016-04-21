package nmea.server.datareader.samplemain;

import nmea.server.NMEAEventManager;
import nmea.server.datareader.CustomNMEAClient;

public class SampleTCPClientMain
  implements NMEAEventManager
{
  public SampleTCPClientMain()
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
    SampleTCPClientMain me = new SampleTCPClientMain();
//  CustomNMEAClient cnc = new CustomNMEAClient(me, CustomNMEAClient.TCP_OPTION, "127.0.0.1", 7001) //, 10000L)
    CustomNMEAClient cnc = new CustomNMEAClient(me, CustomNMEAClient.TCP_OPTION, "localhost", 7001) //, 10000L)
    {
      public void manageNMEAError(Throwable t)
      {
        System.err.println("Oops:" + t.toString());
      }
    };
//  cnc.startWorking();
  }
}
