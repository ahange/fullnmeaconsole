package rmi.nmea.client;

import java.awt.Color;

import java.net.InetAddress;

import java.rmi.RemoteException;
import java.rmi.server.UnicastRemoteObject;

import java.util.Iterator;
import java.util.Set;

import rmi.nmea.client.gui.jumbo.TransparentJumbo;

import nmea.server.ctx.NMEADataCache;
import rmi.nmea.rmiserver.RemoteNMEAInterface;

import ocss.nmea.parser.Speed;

/*
 * On Windows, your address must be the first of the list for the servr to issue the right callback...
 * 
 * Control Panel\Network and Internet\Network Connections
 * 
 */
public class NMEAClientImplementation
extends NMEAClient
{
  private TransparentJumbo twsJumbo = new TransparentJumbo();
  private TransparentJumbo bspJumbo = new TransparentJumbo(Color.green, 30, 30);

  public NMEAClientImplementation()
  {
    super();
    twsJumbo.setName("TWS");
    bspJumbo.setName("STW");
  }

  public void notify(NMEADataCache cache)
  {
    System.out.println("Client: notified by Server.");
    if (cache != null)
    {
      System.out.println("Got the Cache:" + cache.getClass().getName() + " (" + Integer.toString(cache.size()) + " position(s))");
      System.out.println("================================================================");
      String lastData = (String)cache.get(NMEADataCache.LAST_NMEA_SENTENCE);
      System.out.println("Last Sentence:" + lastData);
      try
      {
        // Example: BSP
//      System.out.println("BSP:" + cache.get(NMEADataCache.BSP).toString());
        System.out.println("BSP:" + cache.get(NMEADataCache.BSP));
        bspJumbo.setValue(((Speed)cache.get(NMEADataCache.BSP)).getValue());
        System.out.println("SOG:" + cache.get(NMEADataCache.SOG).toString());
        System.out.println("TWS:" + cache.get(NMEADataCache.TWS).toString());
        twsJumbo.setValue(((Speed)cache.get(NMEADataCache.TWS)).getValue());
        System.out.println("Pos:" + cache.get(NMEADataCache.POSITION).toString());
        System.out.println("================================================================");
      }
      catch (Exception ex)
      {
        System.out.println("Exception :NMEAClient notify...");
        ex.printStackTrace();
        Set<String> ks = cache.keySet();
        Iterator<String> is = ks.iterator();
        while (is.hasNext())
          System.out.println("Key:" + is.next());
      }
    }
    else
      System.out.println("Cache is null");
  }
}
