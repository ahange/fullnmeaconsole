package rmi.nmea.client.gui;

import java.awt.Dimension;
import java.awt.Toolkit;

import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;

import java.net.InetAddress;
import java.net.URLEncoder;

import java.rmi.Naming;
import java.rmi.NotBoundException;

import java.rmi.RMISecurityManager;

import javax.swing.JFrame;
import javax.swing.UIManager;

import rmi.nmea.client.NMEAClient;

import rmi.nmea.client.NMEAClientImplementation;

import nmea.server.ctx.NMEADataCache;

import rmi.nmea.rmiserver.RemoteNMEAInterface;

import ocss.nmea.parser.Speed;

public class MainClientApplication
{
  private NMEAClient client = null;
  
  public MainClientApplication()
  {
    JFrame frame = new NMEAClientFrame(this);
    Dimension screenSize = Toolkit.getDefaultToolkit().getScreenSize();
    Dimension frameSize = frame.getSize();
    if (frameSize.height > screenSize.height)
    {
      frameSize.height = screenSize.height;
    }
    if (frameSize.width > screenSize.width)
    {
      frameSize.width = screenSize.width;
    }
    frame.setLocation( ( screenSize.width - frameSize.width ) / 2, ( screenSize.height - frameSize.height ) / 2 );
//  frame.setDefaultCloseOperation( JFrame.EXIT_ON_CLOSE );
    frame.addWindowListener(new WindowAdapter() 
      {
        public void windowClosing(WindowEvent e)
        {
          System.out.println("Bye now...");
          if (client != null)
            client.unregister();
          System.exit(0);
        }
      });
    
    frame.setVisible(true);
  }

  public void pingServer()
  {
    try 
    { 
      NMEADataCache cache = client.getNmeaServer().getNMEACache(); 
      System.out.println("Got the Cache. (" + Integer.toString(cache.size()) + " position(s))");
      try
      {
        double speed = ((Speed)cache.get(NMEADataCache.BSP)).getValue();
        System.out.println("BSP:" + speed);
      }
      catch (Exception ex)
      {
        System.out.println(ex.toString());
      }
    }
    catch (Exception ex) 
    {
      ex.printStackTrace();
    }
  }
  
  public void connect(String server, int port) throws Exception
  {
    try
    {
//    System.setSecurityManager(new RMISecurityManager());
      String serverName = "//" + server + ":" + Integer.toString(port) + "/" + URLEncoder.encode("nmea", "UTF-8");
      client = new NMEAClientImplementation();
      
      try
      {
        int counter = 0;
        boolean ok = false;
        while (!ok && counter < 5) // Try at most 5 times
        {
          try 
          { 
            client.setNmeaServer((RemoteNMEAInterface) Naming.lookup(serverName));  // Lookup 
            ok = true;
          }
          catch (NotBoundException nbe)
          {
            counter++;
            System.out.println("Retrying...");
            Thread.sleep(1000L); 
          }
        }
        if (counter == 5)
        {
          System.out.println("Check the server...");
          throw new RuntimeException("Connection failed");
        }
        client.getNmeaServer().registerForNotification(client);  // Register client
        System.out.println("Registering [" + InetAddress.getLocalHost().getHostName() + "] with Server");
      }
      catch (Exception e)
      {
        e.printStackTrace();
      }
    }
    catch (Exception ex)
    {
      ex.printStackTrace();
    }
  }
  
  public void disconnect() throws Exception
  {
    client.unregister();    
  }
  
  public static void main(String[] args)
  {
    try
    {
      UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName());
    }
    catch (Exception e)
    {
      e.printStackTrace();
    }
    new MainClientApplication();
  }
}
