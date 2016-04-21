package nmea.main;

import nmea.server.ctx.NMEAContext;
import nmea.server.utils.Utils;

import nmea.local.LogisailResourceBundle;

import nmea.ui.NMEAFrame;

import java.awt.Dimension;
import java.awt.Toolkit;
import java.awt.event.ActionEvent;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;

import java.text.SimpleDateFormat;

import java.util.Date;

import javax.swing.UIManager;

import nmea.server.datareader.CustomNMEAClient;


public class NMEAConsole
{
  private boolean verbose = false;
  private boolean goLog   = false;
  private NMEAFrame frame = null;
  
//private final static SimpleDateFormat dateFormat = new SimpleDateFormat("d-MMM-yyyy hh:mm:ss:");
  private final static SimpleDateFormat DATE_FMT = new SimpleDateFormat("HH:mm:ss");
  private static String pfile = Utils.PROPERTIES_FILE;
  
  public NMEAConsole(boolean verb, 
                     String serial, 
                     int br, 
                     int option,
                     String tcp, 
                     String host,
                     String data, 
                     String prmfile)
  {
    String lnf = System.getProperty("swing.defaultlaf");
    //  System.out.println("LnF:" + lnf);
    if (lnf == null) // Let the -Dswing.defaultlaf do the job.
    {
      try
      {
        if (System.getProperty("swing.defaultlaf") == null)
          UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName());
      }
      catch(Exception e)
      {
        e.printStackTrace();
      }
    }

    verbose = verb;
    pfile = prmfile;
    if (verbose) System.out.println("Displaying minimal UI");
//  NMEAContext.getInstance().setFromFile(data != null && data.trim().length() > 0);
    frame = new NMEAFrame(verb, 
                          serial, 
                          br, 
                          option,
                          tcp, 
                          host,
                          data, 
                          pfile);

    Dimension screenSize = Toolkit.getDefaultToolkit().getScreenSize();
    Dimension frameSize = frame.getSize();
    if(frameSize.height > screenSize.height)
      frameSize.height = screenSize.height;
    if(frameSize.width > screenSize.width)
      frameSize.width = screenSize.width;
    frame.setLocation((screenSize.width - frameSize.width) / 2, (screenSize.height - frameSize.height) / 2);
    frame.addWindowListener(new WindowAdapter() 
    {
      public void windowClosing(WindowEvent e)
      {
        if (goLog) // Stop Logging
        {
          System.out.println("Stopping Logging...");
            NMEAContext.getInstance().fireLogChanged(false);
        }
        stop();
      }
    });

    frame.setVisible(true);
    Thread timeThread = new Thread("ConsoleTimer")
      {
        public void run()
        {
          while (true)
          {
            Date now = new Date();
            frame.setTitle(LogisailResourceBundle.buildMessage("frame-title") + " " + DATE_FMT.format(now));
            try { Thread.sleep(1000L); } catch (Exception ex) {}
          }
        }
      };
    if (verb)
      System.out.println("Starting time thread");
    timeThread.start();
    if (data != null && data.trim().length() > 0)
      frame.setStatus("Simulation:" + data);
  }

  private void stop()
  {
    System.exit(0);
  }

  void fileExit_ActionPerformed(ActionEvent e)
  {
    System.exit(0);
  }

  public static void main(String[] args)
  {
    System.out.println("Usage:");
    System.out.println(" java nmea.main.NMEAConsole [-config [configfile]] [-verb [y|n]] [[-simul [filename]] | [-serial [COM1] -br [4800]] | [-tcp [80]] | [-udp [8000]] | [-host [localhost]] [-http.port [80]]");
    String fName = null;
    boolean verb = false;
    String serial = "COM1";
    String br     = "4800";
    String tcp = "";
    String udp = "";
    String httpport = "";
    String host = "localhost";
    for (int i = 0; i < args.length; i++)
    {
      if(args[i].equals("-verb"))
        verb = args[i + 1].toUpperCase().equals("Y");
      else if(args[i].equals("-serial"))
        serial = args[i + 1];
      else if(args[i].equals("-br"))
        br = args[i + 1];
      else if(args[i].equals("-tcp"))
        tcp = args[i + 1];
      else if(args[i].equals("-udp"))
        udp = args[i + 1];
      else if(args[i].equals("-http.port"))
        httpport = args[i + 1];
      else if(args[i].equals("-host"))
        host = args[i + 1];
      else if(args[i].equals("-config"))
        pfile = args[i + 1];
      else if(args[i].equals("-simul"))
        fName = args[i + 1];
    }
    int baudrate = 4800;
    try
    {
      baudrate = Integer.parseInt(br);
    }
    catch(Exception e)
    {
      e.printStackTrace();
    }
    int option = -1;
    if (tcp.trim().length() > 0)
      option = CustomNMEAClient.TCP_OPTION;
    if (udp.trim().length() > 0)
      option = CustomNMEAClient.UDP_OPTION;
    new NMEAConsole(verb, 
                    System.getProperty("nmea.serial.port", serial), 
                    Integer.parseInt(System.getProperty("nmea.baud.rate", Integer.toString(baudrate))), 
                    option,
                    (option == CustomNMEAClient.TCP_OPTION)?System.getProperty("nmea.tcp.port", tcp):
                                                            System.getProperty("nmea.udp.port", udp), // TCP/UDP Port
                    host,
                    fName, 
                    System.getProperty("nmea.config.file", pfile));
  }
}
