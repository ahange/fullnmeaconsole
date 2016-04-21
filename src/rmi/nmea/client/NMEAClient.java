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
public abstract class NMEAClient
implements Notifiable
{
  private RemoteNMEAInterface nmeaServer;

  public NMEAClient()
  {
    super();
    try
    {
      UnicastRemoteObject.exportObject(this); // For call back
      System.out.println("UnicastRemoteObject.exportObject");
    }
    catch (RemoteException re)
    {
      System.err.println("UnicastRemoteObject.exportObject");
      re.printStackTrace();
    }
  }

  public abstract void notify(NMEADataCache cache);

  public String getAddress()
  {
    String address = " - ";
    try
    {
      address = InetAddress.getLocalHost().getHostName();
    }
    catch (Exception ex)
    {
      ex.printStackTrace();
    }
    return address;
  }

  public void unregister()
  {
    try
    {
      nmeaServer.unregisterForNotification(this);
    }
    catch (Exception ex)
    {
      ex.printStackTrace();
    }
  }

  public void setNmeaServer(RemoteNMEAInterface nmeaServer)
  {
    this.nmeaServer = nmeaServer;
  }

  public RemoteNMEAInterface getNmeaServer()
  {
    return nmeaServer;
  }
}
