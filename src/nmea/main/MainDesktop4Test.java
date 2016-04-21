package nmea.main;


import java.awt.Dimension;

import java.awt.Rectangle;
import java.awt.Toolkit;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;

import javax.swing.JFrame;

import javax.swing.UIManager;

import nmea.ui.NMEAInternalFrame;

public class MainDesktop4Test
     extends JFrame
{
  @SuppressWarnings("compatibility:4368353132278818130")
  public final static long serialVersionUID = 1L;
  public MainDesktop4Test(boolean v, 
                          String serial, 
                          int br, 
                          String tcpPort, 
                          String udpPort,
                          String host,
                          String fName, // simulation file
                          String propertiesFile)
  {
    try
    {
      jbInit(v, 
             serial, 
             br, 
             tcpPort, 
             udpPort,
             host,
             fName, // simulation file
             propertiesFile);
    }
    catch (Exception e)
    {
      e.printStackTrace();
    }
  }

  private void jbInit(boolean v, 
                      String serial, 
                      int br, 
                      String tcpPort, 
                      String udpPort,
                      String host,
                      String fName, // simulation file
                      String propertiesFile)
    throws Exception
  {
    this.getContentPane().setLayout( null );
    this.setSize(new Dimension(1400, 900));
    this.setTitle( "Oliv Desktop" );
    
    NMEAInternalFrame nmeaFrame = new NMEAInternalFrame(v, 
                                                        serial, 
                                                        br, 
                                                        tcpPort, 
                                                        udpPort,
                                                        host,
                                                        fName, // simulation file
                                                        propertiesFile);
    nmeaFrame.setIconifiable(true);
    nmeaFrame.setClosable(true);
    nmeaFrame.setMaximizable(true);
    nmeaFrame.setResizable(true);
    this.add(nmeaFrame);
    nmeaFrame.setVisible(true);
    nmeaFrame.setBounds(new Rectangle(70, 35, 1200, 800));
  }
  
  public static void main(String[] args)
  {
    String fName = null;
    String pfile = null;
    boolean verb = false;
    String serial = "COM16";
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
    int baudrate = Integer.parseInt(br);


    String lnf = null;
    try { lnf = System.getProperty("swing.defaultlaf"); } catch (Exception ignore) { System.err.println(ignore.getLocalizedMessage()); }
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
    JFrame.setDefaultLookAndFeelDecorated(true);
    JFrame frame = new MainDesktop4Test(verb, 
                                        serial, 
                                        baudrate, 
                                        tcp, 
                                        udp,
                                        host,
                                        fName, // simulation file
                                        pfile);
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

    frame.addWindowListener(new WindowAdapter()
    {
      public void windowClosing(WindowEvent e)
      {
        System.exit(0);
      }
    });    
    //  frame.setDefaultCloseOperation( JFrame.EXIT_ON_CLOSE );
    frame.setVisible(true);
    
  }
}
