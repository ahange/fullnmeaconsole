package nmea.ui;

import java.awt.BorderLayout;
import java.awt.Dimension;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;

import java.io.File;
import java.io.FileOutputStream;
import java.io.FileWriter;

import java.text.DecimalFormat;
import java.text.SimpleDateFormat;

import java.util.Date;
import java.util.Properties;

import java.util.TimeZone;

import javax.swing.JLabel;
import javax.swing.JMenuItem;
import javax.swing.JPanel;
import javax.swing.JPopupMenu;
import javax.swing.JTabbedPane;
import javax.swing.event.PopupMenuEvent;
import javax.swing.event.PopupMenuListener;

import nmea.event.NMEAReaderListener;

import nmea.server.ctx.NMEAContext;
import nmea.server.utils.Utils;

import nmea.local.LogisailResourceBundle;

import nmea.server.NMEAEventManager;
import nmea.server.constants.Constants;
import nmea.server.ctx.NMEADataCache;
import nmea.server.datareader.CustomNMEAClient;

import nmea.ui.calc.CalculatedDataTablePane;
import nmea.ui.deviation.ControlPanel;
import nmea.ui.deviation.DeviationPanelHolder;
import nmea.ui.journal.JournalPanel;
import nmea.ui.viewer.BulkPanel;
import nmea.ui.viewer.Full2DPanel;
import nmea.ui.viewer.gsv.GPSSatellitesPanel;
import nmea.ui.viewer.ViewerTablePane;

import nmea.ui.viewer.gsv.GSVPanelHolder;

import nmea.ui.viewer.spot.SpotPanelHolder;

import ocss.nmea.api.NMEAEvent;
import ocss.nmea.api.NMEAListener;
import ocss.nmea.parser.SolarDate;
import ocss.nmea.parser.UTCDate;
import ocss.nmea.utils.NMEAUtils;


public class NMEAMasterPanel
     extends JPanel
  implements NMEAFrameInterface, 
             NMEAEventManager
{
  @SuppressWarnings("compatibility:-469133995607650775")
  public final static long serialVersionUID = 1L;

  private boolean verbose = false;
  private boolean goLog = false;
  private boolean logWithDate = true;
  private long nbRec = 0L;
  private transient FileWriter fw = null;
  private long nbFSFile = 0L;
  private final static DecimalFormat NF = new DecimalFormat("000000");
  private final static SimpleDateFormat SDF = new SimpleDateFormat("dd-MMM-yyyy HH:mm:ss:");
  private final static SimpleDateFormat UTC_DATE_FORMAT = new SimpleDateFormat("EEE dd MMM yyyy HH:mm:ss 'UTC'");
  private final static SimpleDateFormat SOLAR_DATE_FORMAT = new SimpleDateFormat("'Solar:' dd MMM yyyy HH:mm:ss");
  static
  {
    UTC_DATE_FORMAT.setTimeZone(TimeZone.getTimeZone("Etc/UTC"));
    SOLAR_DATE_FORMAT.setTimeZone(TimeZone.getTimeZone("Etc/UTC"));
  }
  private static long nbRecToCommit = Long.MAX_VALUE; // 100L;

  private String pfile = "";

  private BorderLayout borderLayout  = new BorderLayout();
  private JPanel bottomPanel         = new JPanel();
  private BulkPanel               bp = new BulkPanel();                   // As they are, as a stream
  private ViewerTablePane         vp = new ViewerTablePane(this);         // As they are, in a table
  private CalculatedDataTablePane cp = new CalculatedDataTablePane(this); // Parsed data, in a table
  private JTabbedPane nmeaTabbedPane = new JTabbedPane();

  private Full2DPanel            f2d = new Full2DPanel();                 // 2D Graphical Display
  
  private JPanel                  ep = new JPanel(new BorderLayout());
  private JPanel                 gps = new GSVPanelHolder();  
//private JPanel                spot = new SpotPanelHolder();  
  private int gpsTabIndex = -1;
  
  private JTabbedPane tabbedPane = new JTabbedPane();
  private JLabel status = new JLabel(LogisailResourceBundle.buildMessage("ready"));

  private String[] currentSentencesToLog = null;
  
  private boolean openSerialPort = true;

  private String serial = null;
  private int br = 0;
  private String tcp = "";
  private String udp = "";
  private String rmi = "";
  private String host = "localhost";
  private String data = null;
  
  private transient CustomNMEAClient nmeaClient = null; 
  // Add NMEAListener, override dataRead
  private transient NMEAListener nmeaDataListener = new NMEAListener()
    {
      // ocss.nmea.api
      @Override
      public void dataRead(NMEAEvent e) // nmea.ui.NMEAMasterPanel$1
      {
        String chunk = e.getContent();
        // Translate (for CR NL) here
        // Tell the GUI...
        bp.setDataStream(NMEAUtils.translateEscape(chunk, bp.getDataStreamType())); // CR_NL, ALL_IN_HEXA
      }
    };
  
  public NMEAMasterPanel(boolean v,
                         String serial,
                         int br,
                         String port,
                         int option,
                         String host,
                         String fName, // simulation file
                         String propertiesFile,
                         boolean openSerial)
  {
    this.verbose = v;
    this.serial = serial;
    this.br = br;
    if (option == CustomNMEAClient.TCP_OPTION)
      this.tcp = port;
    if (option == CustomNMEAClient.UDP_OPTION)
      this.udp = port;
    if (option == CustomNMEAClient.RMI_OPTION)
      this.rmi = port;
    this.host = host;
    this.data = fName;
    this.pfile = propertiesFile;
    if (data != null && data.trim().length() > 0)
      setLogMessage("Simulation:" + data);
    if (NMEAContext.getInstance().isFromFile())
      NMEAContext.getInstance().setReplayFile(fName);
    
    this.openSerialPort = openSerial;
    
    try
    {
      jbInit();
    }
    catch (Exception e)
    {
      e.printStackTrace();
    }
  }
  
  private transient NMEAReaderListener nl = new NMEAReaderListener(Constants.NMEA_SERVER_LISTENER_GROUP_ID, "MasterPanel")
    {
      public void manageNMEAString(String str)
      {
        dispatchData(str);
        try
        {
          if (NMEAContext.getInstance().getCache().get(NMEADataCache.SAT_IN_VIEW) != null)
            nmeaTabbedPane.setEnabledAt(gpsTabIndex, true);
        }
        catch (Exception ex)
        {
          // No cache yet
        }
      }

      public void log(boolean b)
      {
        log(b, true);
      }
      
      public void log(boolean b, boolean dateTime)
      {
        System.out.println("Logging:" + b);
        goLog = b;
        logWithDate = dateTime;
        if (!b && fw != null)
        {
          System.out.println("Stopping the logging");
          try
          {
            fw.flush();
            fw.close();
          }
          catch (Exception ex)
          {
            ex.printStackTrace();
          }
          fw = null;
          System.out.println("Committed " + Long.toString(nbRec) + " records");
        }
      }
      
      public void setMessage(String s) 
      {
        setLogMessage(s);
      }
    };

  public NMEAReaderListener getNMEAListener()
  {
    return nl;
  }
  
  private void jbInit()
    throws Exception
  {
    // Init
    NMEAContext.getInstance().addNMEAReaderListener(nl);
    this.setLayout(borderLayout);
    setSize(new Dimension(767, 427));
//  setTitle(LogisailResourceBundle.buildMessage("frame-title"));
    bottomPanel.setLayout(new BorderLayout());
    bottomPanel.add(status, BorderLayout.WEST);
    status.addMouseListener(new MouseListener()
      {
        public void mouseClicked(MouseEvent e)
        {
          // Right click: popup -> Reset
          int mask = e.getModifiers();
          // Right-click only (Actually: no left-click)
          if ((mask & MouseEvent.BUTTON2_MASK) != 0 || (mask & MouseEvent.BUTTON3_MASK) != 0)
          {
            String statusText = status.getText();
            if (NMEAContext.getInstance().isFromFile())
            {
              String fileName = NMEAContext.getInstance().getReplayFile();
              File f = new File(fileName);
              if (f.exists())
              {
                LoggingDetailsPopup popup = new LoggingDetailsPopup(fileName);
                popup.show(status, e.getX(), e.getY());
              }
              else
                System.out.println(statusText + " does not exist.");
            }
          }
        }

        public void mousePressed(MouseEvent e)
        {
        }

        public void mouseReleased(MouseEvent e)
        {
        }

        public void mouseEntered(MouseEvent e)
        {
        }

        public void mouseExited(MouseEvent e)
        {
        }
      });
    this.add(bottomPanel, BorderLayout.SOUTH);

    nmeaTabbedPane.setTabPlacement(JTabbedPane.BOTTOM);
    nmeaTabbedPane.add(LogisailResourceBundle.buildMessage("bulk-data"), bp);
    nmeaTabbedPane.add(LogisailResourceBundle.buildMessage("viewer"), vp);
    nmeaTabbedPane.add(LogisailResourceBundle.buildMessage("data-viewer"), cp);
    nmeaTabbedPane.add("GPS", gps);
    gpsTabIndex = nmeaTabbedPane.getTabCount() - 1;
//  nmeaTabbedPane.add("SPOT", spot);
    nmeaTabbedPane.setEnabledAt(gpsTabIndex, false);
      
    tabbedPane.add(LogisailResourceBundle.buildMessage("nmea-data"), nmeaTabbedPane);
    tabbedPane.add(LogisailResourceBundle.buildMessage("2d-visualization"), f2d);

    String withJournal = System.getProperty("with.journal", "false");
    if ("true".equals(withJournal))
    {
      JournalPanel jp = new JournalPanel();
      tabbedPane.add("Journal", jp); // LOCALIZE
    }
//  tabbedPane.add("Evolution", dataP);
    
    JPanel shifLeftPanel = new JPanel(new BorderLayout());
    shifLeftPanel.add(new ControlPanel(), BorderLayout.NORTH); // BorderLayout.WEST);
    ep.add(shifLeftPanel, BorderLayout.NORTH);
    ep.add(new DeviationPanelHolder(), BorderLayout.CENTER);
    tabbedPane.add(LogisailResourceBundle.buildMessage("deviation-curve"), ep);
    
    
    this.add(tabbedPane, BorderLayout.CENTER);

    vp.setCurrentSentences(currentSentencesToLog);

    System.out.println(this.getClass().toString() + ":Port is " + (openSerialPort?"":"NOT ") + "to be opened.");
    if (openSerialPort || (data != null && data.trim().length() > 0)) // If Serial Port is to be opened
    {
      try
      {
        if (tcp != null && tcp.trim().length() > 0)
        {
          int tcpport = Integer.parseInt(tcp);
          read(tcpport, host, CustomNMEAClient.TCP_OPTION);
        } 
        else if (udp != null && udp.trim().length() > 0)
        {
          int udpport = Integer.parseInt(udp);
          read(udpport, host, CustomNMEAClient.UDP_OPTION);
        } 
        else if (rmi != null && rmi.trim().length() > 0)
        {
          int rmiport = Integer.parseInt(rmi);
          read(rmiport, host, CustomNMEAClient.RMI_OPTION);
        } 
        else if (data != null && data.trim().length() > 0)
        {
          read(new File(data));
        }
        else if (serial != null & serial.trim().length() > 0)
        {
          read(serial, br);
        }
        else
        {
          System.out.println("Nothing to read, exiting.");
          System.exit(1);
        }        
      }
      catch(Exception e)
      {
        e.printStackTrace();
      }
    }
    NMEAContext.getInstance().addNMEAListener(nmeaDataListener);
    // TODO Multiplexing (other NMEA Sources, like the RasPI)
  }

  private void read()
  {
    String port = null;
    read(port);
  }

  private void read(String port)
  {
    read(port, 4800);
  }

  /**
   * Read serial port
   * @param port
   * @param br
   */
  private void read(String port, int br)
  {
    System.out.println(this.getClass().getName()+ ":Reading...");
    setLogMessage(LogisailResourceBundle.buildMessage("reading", new String[] { port, Integer.toString(br)}));
//  CustomNMEAClient nmeaClient = null;
    nmeaClient = new CustomNMEAClient(this, port, br)
      {
        public void manageNMEAError(Throwable t)
        {
          throw new RuntimeException(t);
        }
      };
  }

  /**
   * Read with TCP, RMI or UDP
   * @param port
   */
  private void read(int port, String host, int option)
  {
    String optionStr = "";
    if (option == CustomNMEAClient.TCP_OPTION)
      optionStr = "TCP";
    else if (option == CustomNMEAClient.UDP_OPTION)
      optionStr = "UDP";
    else if (option == CustomNMEAClient.RMI_OPTION)
      optionStr = "RMI";
    System.out.println("Reading " + optionStr + "...");
    setLogMessage(LogisailResourceBundle.buildMessage("reading", new String[] { optionStr, host + ":" + Integer.toString(port)}));
//  CustomNMEAClient nmeaClient = null;
    nmeaClient = new CustomNMEAClient(this, option, host, port)
      {
        public void manageNMEAError(Throwable t)
        {
          throw new RuntimeException(t);
        }
      };
  }

  /**
   * Read from log file
   * @param f
   */
  private void read(File f)
  {
    System.out.println("Reading Data File...");
//  CustomNMEAClient nmeaClient = null;
    nmeaClient = new CustomNMEAClient(this, f)
      {
        public void manageNMEAError(Throwable t)
        {
          throw new RuntimeException(t);
        }
      };
  }
  
  public void stopReadingSimulationFile()
  {
    try
    {
      nmeaClient.stopReading(); // TODO Remove listeners?
      NMEAContext.getInstance().removeNMEAListener(nmeaDataListener);
    }
    catch (Exception ex)
    {
      ex.printStackTrace();
    }
  }
  
  public boolean verbose()
  {
    return verbose;
  }

  public void manageDataEvent(String payload)
  {
    if (verbose || "true".equals(System.getProperty("verbose", "false")))
    {
      String displ = payload;
        while ((displ.endsWith("\r") || displ.endsWith("\n")) && displ.length() >= 0)
        displ = displ.substring(0, displ.length() - 1);
      System.out.println(this.getClass().getName() + ": Read from NMEA :[" + displ + "]");
    }
    
    NMEAContext.getInstance().fireNMEAString(payload);
  }

  private void dispatchData(String payload)
  {
    if (payload.trim().length() < 6 || !payload.trim().substring(1, 6).equals(payload.trim().substring(1, 6).toUpperCase()))
      return;
//  System.out.println("Substring:[" + payload.substring(1, 6) +"]");

    String key = "";

    try
    {
      key = payload.trim().substring(1);
      if (key.length() > 5)
        key = key.substring(0, 5);
      if (key.length() == 5)
        vp.setValue(key, payload, null); // Feed the viewer here. And the cache (last parameter)
    }
    catch (Exception ex)
    {
      System.err.println("Dispatch Data:");
      ex.printStackTrace();
    }
    // $xxGLL or $GPGLL
    int match = longOrShort(payload);
    if (match == LONG_MATCH)
      key = payload.substring(1, 6);
    else if (match == SHORT_MATCH)
      key = payload.substring(3, 6);

    // If key in list, display in logger pane, and log if necessary
    if (Utils.isInArray(key, currentSentencesToLog))
    {
      if (goLog) // Logging?
      {
        nbRec++;
        if (fw == null)
        {
          try
          {
            String pattern = "dd-MMM-yyyy";
            String _logDir = (new SimpleDateFormat(pattern)).format(new Date());
            File logDir = new File(_logDir);
            if (logDir.exists() && logDir.isDirectory())
              System.out.println(_logDir + " directory exists");
            else if (logDir.mkdir())
            {
              System.out.println(_logDir + " directory created");
            }
            else
            {
              System.err.println("Cannot create directory " + _logDir);
              System.exit(1);
            }            
            String fName = _logDir + File.separator + "data_" + NF.format(nbFSFile++) + ".nmea";
            File f = null;
            boolean ok = false;
            while (!ok)
            {
              f = new File(fName);
              if (!f.exists())
                ok = true;
              else
                fName = _logDir + File.separator + "data_" + NF.format(nbFSFile++) + ".nmea";
            }
            fw = new FileWriter(f);
          }
          catch (Exception e)
          {
            e.printStackTrace();
          }
        }
        // Actual data logging is here
        try
        {
          setLogMessage(LogisailResourceBundle.buildMessage("record", new String[] { Long.toString(nbRec - 1L) }));
          if (false)
          {
            if (nbRecToCommit < Long.MAX_VALUE) // Old version
              fw.write( (logWithDate?(SDF.format(new Date()) + "\t"):"") + payload + "\n");
            else
            {                                   // New version (no date)
              fw.write(payload + "\n");
              if (nbRecToCommit % 500 == 0)
                fw.flush();
            }
          }
          else
          {
            fw.write( (logWithDate?(SDF.format(new Date()) + "\t"):"") + payload + "\n");
            if (nbRecToCommit % 500 == 0)
              fw.flush();
          }
        }
        catch (Exception ex)
        {
          ex.printStackTrace();
        }
        if (nbRec % nbRecToCommit == 0L)
        {
          try
          {
            fw.flush();
            fw.close();
          }
          catch (Exception ex)
          {
            ex.printStackTrace();
          }
          fw = null;
          System.out.println("Committed " + Long.toString(nbRec) + " records");
        }
      }
    }
    if (NMEAContext.getInstance().isFromFile())
    {
      UTCDate utcDate = (UTCDate)NMEAContext.getInstance().getCache().get(NMEADataCache.GPS_DATE_TIME);      
      if (utcDate != null && utcDate.getValue() != null)
      {
        SolarDate solarDate = (SolarDate)NMEAContext.getInstance().getCache().get(NMEADataCache.GPS_SOLAR_TIME);
        String message = "Simulation:" + NMEAContext.getInstance().getReplayFile() + 
                      // ", rec #" + Long.toString(NMEAContext.getInstance().getReplayFileRecNum()) + (fileSize > 0L?"/" + Long.toString(NMEAContext.getInstance().getReplayFileSize()):"") +  
                         ", " + UTC_DATE_FORMAT.format(utcDate.getValue());
        if (solarDate != null)
          message += (" (" + SOLAR_DATE_FORMAT.format(solarDate.getValue()) + ")");
        setLogMessage(message);
      }
    }
  }

  public void setLogMessage(String s)
  {
    status.setText(s);
  }
  private static final int LONG_MATCH  = 0;
  private static final int SHORT_MATCH = 1;

  private final int longOrShort(String payload)
  {
    int match = -1;
    for (int i = 0; currentSentencesToLog != null && i < currentSentencesToLog.length; i++)
    {
      try
      {
        if (currentSentencesToLog[i].length() == 3 && currentSentencesToLog[i].equals(payload.substring(3, 6)))
        {
          match = SHORT_MATCH;
          break;
        }
        else if (currentSentencesToLog[i].length() == 5 && currentSentencesToLog[i].equals(payload.substring(1, 6)))
        {
          match = LONG_MATCH;
          break;
        }
      }
      catch (Exception ex)
      {
        System.out.println(ex.toString() + " for [" + payload + "]");
      }
    }
    return match;
  }

  public void writeSentencesToLog(String[] sa)
  {
    String sentences = "";
    for (int i = 0; i < sa.length; i++)
      sentences += ((sentences.trim().length() == 0? "": ",") + sa[i]);
    Properties props = new Properties();
    props.setProperty("nmea.sentences", sentences);
    try
    {
      FileOutputStream fos = new FileOutputStream(new File(Utils.PROPERTIES_FILE));
      props.store(fos, "Sentences to log");
      fos.close();
    }
    catch (Exception ex)
    {
      ex.printStackTrace();
    }
  }

  private void showHelp()
  {
    try
    {
      String docFileName = System.getProperty("user.dir") + File.separator + "doc" + File.separator + "index.html";
      Runtime.getRuntime().exec("cmd /k start " + docFileName);
    }
    catch (Exception e)
    {
      e.printStackTrace();
    }
  }

  public String getPfile()
  {
    return pfile;
  }

  public String[] getCurrentSentencesToLog()
  {
    return currentSentencesToLog;
  }

  public void setSentencesToLog(String[] sa)
  {
    currentSentencesToLog = sa;
  }
  
  public ViewerTablePane getVp()
  {
    return vp;
  }

  static class LoggingDetailsPopup extends JPopupMenu
                                implements ActionListener,
                                           PopupMenuListener
  {
    @SuppressWarnings("compatibility:1443743705366716038")
    public final static long serialVersionUID = 1L;

    private JMenuItem details;
    private String fileName;

    private final static String DETAILS     = "Recording Details...";

    public LoggingDetailsPopup(String fName)
    {
      super();
      fileName = fName;
      details = new JMenuItem(DETAILS);
      this.add(details);
      details.addActionListener(this);
    }

    public void actionPerformed(ActionEvent event)
    {
      if (event.getActionCommand().equals(DETAILS))
      {
     // System.out.println("Details on " + fileName);
//      JOptionPane.showMessageDialog(null, "Details on " + fileName, "Recording Details", JOptionPane.PLAIN_MESSAGE);
        Thread detailThread = new Thread()
          {
            public void run()
            {
              Utils.displayNMEADetails(fileName);
            }
          };
        detailThread.start();
      }
    }

    public void popupMenuWillBecomeVisible(PopupMenuEvent e)
    {
    }

    public void popupMenuWillBecomeInvisible(PopupMenuEvent e)
    {
    }

    public void popupMenuCanceled(PopupMenuEvent e)
    {
    }
  }
}
