package rmi.nmea.client;

import java.rmi.Remote;
import java.rmi.RemoteException;

import nmea.server.ctx.NMEADataCache;

public interface Notifiable  extends Remote
{
  public void notify(NMEADataCache cache) throws RemoteException;
  public String getAddress() throws RemoteException;
}
