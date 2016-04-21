package rmi.nmea.rmiserver;

import java.rmi.Remote;
import java.rmi.RemoteException;

import rmi.nmea.client.Notifiable;

import nmea.server.ctx.NMEADataCache;

public interface RemoteNMEAInterface extends Remote
{
  public NMEADataCache getNMEACache() throws RemoteException;
  // called by clients to register for server callbacks
  public void registerForNotification(Notifiable n) throws RemoteException;
  public void unregisterForNotification(Notifiable n) throws RemoteException;
}
