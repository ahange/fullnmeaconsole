package nmea.server.utils;

import java.io.BufferedReader;
import java.io.DataOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileReader;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.io.PrintWriter;

import java.net.ServerSocket;
import java.net.Socket;
import java.net.URLEncoder;

import java.text.DecimalFormat;
import java.text.SimpleDateFormat;

import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Properties;
import java.util.Set;

import nmea.server.ctx.NMEAContext;
import nmea.server.ctx.NMEADataCache;

import ocss.nmea.parser.Angle180;
import ocss.nmea.parser.Angle180EW;
import ocss.nmea.parser.Angle180LR;
import ocss.nmea.parser.Angle360;
import ocss.nmea.parser.ApparentWind;
import ocss.nmea.parser.Depth;
import ocss.nmea.parser.GeoPos;
import ocss.nmea.parser.RMC;
import ocss.nmea.parser.SVData;
import ocss.nmea.parser.Speed;
import ocss.nmea.parser.StringParsers;
import ocss.nmea.parser.Temperature;
import ocss.nmea.parser.TrueWindSpeed;
import ocss.nmea.parser.UTC;
import ocss.nmea.parser.UTCDate;
import ocss.nmea.parser.UTCTime;
import ocss.nmea.parser.Wind;

import user.util.GeomUtil;
import user.util.TimeUtil;

/**
 * Proto for GPSD...
 */
public class TCPServer 
{
  private final static SimpleDateFormat DATE_FORMAT = new SimpleDateFormat("dd MMM yyyy HH:mm:ss 'UTC'");
  private final static SimpleDateFormat TIME_FORMAT = new SimpleDateFormat("HH:mm:ss 'UTC'");
  
  public TCPServer()
  {
  }

  private boolean verbose = true;
  private int port = 0;
  
  public TCPServer(String strPort)
  {
    try
    {
      port = Integer.parseInt(strPort);
    }
    catch (NumberFormatException nfe)
    {
      throw nfe;
    }

    // Infinite loop, waiting for requests
    Thread tcpListenerThread = new Thread("TCPServer")
    {
      public void run()
      {
        boolean go = true;
        try
        {
          ServerSocket ss = new ServerSocket(port);

          while (go)
          {
            Socket client = ss.accept();
            BufferedReader    in = new BufferedReader(new InputStreamReader(client.getInputStream()));
            DataOutputStream out = new DataOutputStream(client.getOutputStream());
            String clientRequest = in.readLine();
            System.out.println("Received: " + clientRequest);
            // TODO Manage here the client reqsuest
            
            String requestResponse = clientRequest.toUpperCase() + '\n';
            out.writeBytes(requestResponse);
            if ("EXIT".equalsIgnoreCase(clientRequest))
            {
              go = false;
            }
            client.close();            
          }
          ss.close();
        }
        catch (Exception e)
        {
          System.err.println("TCP Server:" + e.toString());
          e.printStackTrace();
        }
      }
    };
    tcpListenerThread.start();
  }
  
  public static void main(String[] args)
  {
    TCPServer server = new TCPServer("2947");
  }
}