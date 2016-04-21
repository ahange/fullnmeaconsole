package rmi.nmea.rmiserver;

import java.net.InetAddress;
import java.net.URLEncoder;

import java.rmi.Naming;
//import java.rmi.RMISecurityManager;
import java.rmi.RemoteException;
//import java.rmi.server.RMIClientSocketFactory;
//import java.rmi.server.RMIServerSocketFactory;
import java.rmi.server.UnicastRemoteObject;

import java.util.ArrayList;

import java.util.List;

import rmi.nmea.client.Notifiable;

import nmea.server.ctx.NMEAContext;
import nmea.server.ctx.NMEADataCache;

import nmea.event.NMEAReaderListener;

public class NMEAServer
  extends UnicastRemoteObject
  implements RemoteNMEAInterface
{
  private transient List<Notifiable> listeners = new ArrayList<Notifiable>(1);
  private String name = null;
  
  public NMEAServer() throws RemoteException
  {
    super();
    NMEAContext.getInstance().addNMEAReaderListener(new NMEAReaderListener("RMI", "RMI Server (1)")
      {
        public void dataUpdate() 
        {
          notifyRemoteClients();
        }        
      });
  }
  
  public void startServer(int port, String serverPath) throws Exception
  {
    try
    {
      name = "rmi://" + InetAddress.getLocalHost().getHostName() /* "localhost" */ + ":" + 
             Integer.toString(port) + "/" + 
             URLEncoder.encode(serverPath, "UTF-8");
    }
    catch (Exception ex)
    {
      ex.printStackTrace();
    }
//  System.setSecurityManager(new RMISecurityManager());
    try
    {
      System.out.println("Registering as " + name + " ...");
      Naming.rebind(name, this);
      System.out.println("Registered as " + name + " !");
    }
    catch (Exception e)
    {
      throw e;
    }    
  }

  public void stopServer() throws Exception
  {
    try
    {
      System.out.println("Stoping RMI Server " + name);
      Naming.unbind(name);  
      System.out.println("Stopped.");
    }
    catch (Exception ex)
    {
      throw ex;
    }
  }
  
  public void notifyRemoteClients()
  {
    NMEADataCache cache = NMEAContext.getInstance().getCache();
    for (Notifiable client : listeners)    
    {
      try
      {         
    //  System.out.println("Notifying client:" + client.getAddress());
        client.notify(cache);
      }
      catch (RemoteException ex)
      {
//      ex.printStackTrace();
        try { System.err.println("notifyRemoteClient [" + client.getAddress() + "]:" + ex.getLocalizedMessage()); }
        catch (Exception ex2) {}
      }
    }
  }

  public NMEADataCache getNMEACache()
    throws RemoteException
  {
    System.out.println("getCache invoked on server");
    NMEADataCache cache = null;
    synchronized (NMEAContext.getInstance().getCache())
    {
  /*  NMEADataCache */ cache = NMEAContext.getCache_oneTrip(); // NMEAContext.getInstance().getCache();
    }
    System.out.println("Returning Cache. (" + Integer.toString(cache.size()) + " position(s))");
    return cache;
  }

  public void registerForNotification(Notifiable n)
    throws RemoteException
  {
    System.out.println("Server: registering client [" + n.getAddress() + "] for notification.");
    if (!listeners.contains(n))
      listeners.add(n);
  }

  public void unregisterForNotification(Notifiable n)
    throws RemoteException
  {
    System.out.println("Unregistering client [" + n.getAddress() + "]");
    listeners.remove(n);
  }

  public static void main(String[] args)
  {
    try
    {
      NMEAServer nmeaServer = new NMEAServer();
      nmeaServer.startServer(1234, "nmea");
      System.out.println("Server started...");
      // For tests
      try
      {
        Thread.sleep(1000L);
        System.out.println("Notifying (for tests)!");
        nmeaServer.notifyRemoteClients();        
      }
      catch (Exception ex)
      {
        ex.printStackTrace();
      }
    }
    catch (Exception ex)
    {
      ex.printStackTrace();
    }
  }
}
