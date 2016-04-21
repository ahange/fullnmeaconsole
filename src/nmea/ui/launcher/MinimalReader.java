package nmea.ui.launcher;

import java.io.FileInputStream;
import java.io.IOException;

import java.text.DecimalFormat;
import java.text.SimpleDateFormat;

import java.util.Properties;

import nmea.server.NMEAEventManager;

import nmea.server.datareader.CustomClient;

import java.io.File;

@Deprecated
public class MinimalReader
  implements NMEAEventManager
{
  private DecimalFormat nf = new DecimalFormat("000000");
  private SimpleDateFormat sdf = new SimpleDateFormat("dd-MMM-yyyy HH:mm:ss:");
  private boolean verbose = false;
  String simulator = "";
  SimpleDateFormat dateFormat = new SimpleDateFormat("d-MMM-yyyy hh:mm:ss:");

  public MinimalReader(boolean verb, String serial, int br, String tcp, String data)
  {
    verbose = verb;
    try
    {
      if (tcp.trim().length() > 0)
      {
        int tcpport = Integer.parseInt(tcp);
        read(tcpport);
      }
      else if (data != null && data.trim().length() > 0)
      {
        read(new File(data));
      }
      else
      {
        read(serial, br);
      }
    }
    catch (Exception e)
    {
      e.printStackTrace();
    }
  }

  private void read()
  {
    read((String) null);
  }

  private void read(String port)
  {
    read(port, 4800);
  }

  private void read(String port, int br)
  {
    System.out.println("Reading...");
    CustomClient nmeaClient = null;
    String prefix = "";
    String array[] = null;
    Properties properties = new Properties();
    try
    {
      properties.load(new FileInputStream("nmea-config.properties"));
      prefix = properties.getProperty("device.prefix");
      String sentences = properties.getProperty("nmea.sentences");
      String patternStr = ",";
      array = sentences.split(patternStr);
    }
    catch (IOException e)
    {
      e.printStackTrace();
      System.exit(1);
    }
    nmeaClient = new CustomClient(this, port, br, prefix, array);
  }

  private void read(int port)
  {
    System.out.println("Reading TCP...");
    CustomClient nmeaClient = null;
    String prefix = "";
    String array[] = null;
    Properties properties = new Properties();
    try
    {
      properties.load(new FileInputStream("nmea-config.properties"));
      prefix = properties.getProperty("device.prefix");
      String sentences = properties.getProperty("nmea.sentences");
      String patternStr = ",";
      array = sentences.split(patternStr);
    }
    catch (IOException e)
    {
      e.printStackTrace();
      System.exit(1);
    }
    nmeaClient = new CustomClient(this, port, prefix, array);
  }

  private void read(File f)
  {
    System.out.println("Reading Data File...");
    CustomClient nmeaClient = null;
    String prefix = "";
    String array[] = null;
    Properties properties = new Properties();
    try
    {
      properties.load(new FileInputStream("nmea-config.properties"));
      prefix = properties.getProperty("device.prefix");
      String sentences = properties.getProperty("nmea.sentences");
      String patternStr = ",";
      array = sentences.split(patternStr);
    }
    catch (IOException e)
    {
      e.printStackTrace();
      System.exit(1);
    }
    nmeaClient = new CustomClient(this, f, prefix, array);
  }
  
  public void manageDataEvent(String payload)
  {
    if (verbose)
      System.out.println("Read :[" + payload + "]");
  }

  public static void main(String[] args)
  {
    System.out.println("Usage:");
    System.out.println(" java ui.launcher.MinimalReader [-config [configfile]] [-verb [y|n]] [[-simul [filename]] | [[-serial [COM1] -br [4800]] | [-tcp [80]] ");
    boolean verb = false;
    String serial = "COM1";
    String br = "4800";
    String tcp = "";
    String fName = null;
    for (int i = 0; i < args.length; i++)
    {
      if (args[i].equals("-verb"))
        verb = args[i + 1].toUpperCase().equals("Y");
      else if (args[i].equals("-serial"))
        serial = args[i + 1];
      else if (args[i].equals("-br"))
        br = args[i + 1];
      else if (args[i].equals("-tcp"))
        tcp = args[i + 1];
      else if(args[i].equals("-simul"))
        fName = args[i + 1];
    }
    int baudrate = 4800;
    try
    {
      baudrate = Integer.parseInt(br);
    }
    catch (Exception e)
    {
      e.printStackTrace();
    }
    new MinimalReader(verb, 
                      System.getProperty("nmea.serial.port", serial), 
                      Integer.parseInt(System.getProperty("nmea.baud.rate", Integer.toString(baudrate))), 
                      tcp, 
                      fName);
  }

  public boolean verbose()
  {
    return verbose;
  }
}
