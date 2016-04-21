package nmea.ui;

import nmea.server.ctx.NMEAContext;

import nmea.server.utils.Utils;

import nmea.local.LogisailResourceBundle;

import nmea.ui.widgets.ConfigTablePanel;

import java.awt.BorderLayout;
import java.awt.Dimension;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

import java.util.HashMap;

import javax.swing.ImageIcon;
import javax.swing.JFrame;
import javax.swing.JMenu;
import javax.swing.JMenuBar;
import javax.swing.JMenuItem;
import javax.swing.JOptionPane;
import javax.swing.JSeparator;

import nmea.server.datareader.CustomNMEAClient;

import nmea.ui.widgets.BeaufortPanel;

/**
 * @deprecated Use NMEAInternalFrame instead
 */
public class NMEAFrame 
     extends JFrame
{
  private NMEAFrame instance = this;
  
  private JMenuBar menuBar               = new JMenuBar();
  private JMenu menuFile                 = new JMenu(LogisailResourceBundle.buildMessage("file")); 
  private JMenuItem menuFileExit         = new JMenuItem(LogisailResourceBundle.buildMessage("exit"));
  private JMenuItem menuFileSave         = new JMenuItem(LogisailResourceBundle.buildMessage("save"));
  private JMenuItem menuFileManageConfig = new JMenuItem(LogisailResourceBundle.buildMessage("manage"));
  private JMenuItem menuFileSavePrms = new JMenuItem("Save Calibration Parameters"); // LOCALIZE

  private JMenu menuHelp = new JMenu("Help");                           // LOCALIZE
  private JMenuItem menuHelpAbout = new JMenuItem("About");             // LOCALIZE
  private JMenuItem menuHelpBeaufort = new JMenuItem("Beaufort Scale"); // LOCALIZE

  private NMEAMasterPanel nmeaTP = null;
  
  private String pfile = "";

  private BorderLayout borderLayout1 = new BorderLayout();

  private String[] currentSentencesToLog = null;

  private boolean verbose = false;
  private String serial = null;
  private int br = 0;
  private int option = -1;
  private String tcp = "";
  private String udp = "";
  private String rmi = "";
  private String host = "localhost";
  private String data = null;

  public NMEAFrame(boolean v,
                   String serial,
                   int br,
                   int option,
                   String port,
                   String host,
                   String fName, // simulation file
                   String propertiesFile)
  {
    this.verbose = v;
    this.serial = serial;
    this.br = br;
    this.option = option;
    if (option == CustomNMEAClient.TCP_OPTION)
      this.tcp = port;
    if (option == CustomNMEAClient.UDP_OPTION)
      this.udp = port;
    if (option == CustomNMEAClient.RMI_OPTION)
      this.rmi = port;
    this.host = host;
    this.data = fName;
    pfile = propertiesFile;
    NMEAContext.getInstance().setFromFile(data != null && data.trim().length() > 0);
    try
    {
      jbInit();
    }
    catch(Exception e)
    {
      e.printStackTrace();
    }
  }

  private void jbInit()
    throws Exception
  {
    String _port = "";
    if (option == CustomNMEAClient.TCP_OPTION)
      _port = tcp;
    if (option == CustomNMEAClient.UDP_OPTION)
      _port = udp;
    if (option == CustomNMEAClient.RMI_OPTION)
      _port = rmi;
    nmeaTP = new NMEAMasterPanel(verbose, 
                                 serial, 
                                 br, 
                                 _port, 
                                 option,
                                 host,
                                 data, 
                                 pfile,
                                 true); 
    this.setJMenuBar(menuBar);
    menuBar.add(menuFile); 
    menuFile.add(menuFileSave);
    menuFile.add(menuFileManageConfig);
    menuFile.add(menuFileSavePrms);
    menuFile.add(new JSeparator());
    menuFile.add(menuFileExit);
    menuFileExit.addActionListener(new ActionListener() 
    {
      public void actionPerformed(ActionEvent ae)
      {
        NMEAContext.getInstance().fireLogChanged(false);
        try 
        { NMEAContext.getInstance().fireStopReading(); } 
        catch (Exception ex) {}
        NMEAContext.getInstance().getCache().resetDampingBuffers();
        System.exit(0);
      }
    });
    menuFileSave.addActionListener(new ActionListener() 
    {
      public void actionPerformed(ActionEvent ae)
      {
          NMEAContext.getInstance().fireSaveUserConfig();
      }
    });
    menuFileManageConfig.addActionListener(new ActionListener() 
    {
      public void actionPerformed(ActionEvent ae)
      {
        HashMap<String, String> userConfig = nmeaTP.getVp().getUserConfig();
        Object[][] data = new Object[userConfig.size()][2];
        int i=0;
        for (String s : userConfig.keySet())
        {
          data[i][0] = s;
          data[i][1] = userConfig.get(s);
          i++;
        }
        ConfigTablePanel ctp = new ConfigTablePanel();
        ctp.setData(data);
        int resp = JOptionPane.showConfirmDialog(instance, 
                                                 ctp, LogisailResourceBundle.buildMessage("manage"), 
                                                 JOptionPane.OK_CANCEL_OPTION, 
                                                 JOptionPane.PLAIN_MESSAGE);
        if (resp == JOptionPane.OK_OPTION)
        {
          // Save Configs
          data = ctp.getData();
          userConfig = new HashMap<String, String>(data.length);
          for (i=0; i<data.length; i++)
            userConfig.put((String)data[i][0], (String)data[i][1]);
          nmeaTP.getVp().setUserConfig(userConfig);
          nmeaTP.getVp().saveUserConfigs();
        }
      }
    });
    menuFileSavePrms.addActionListener(new ActionListener()
      {
        public void actionPerformed(ActionEvent ae)
        {
          Utils.writeNMEAParameters();
          JOptionPane.showMessageDialog(instance, "Configuration saved.", "NMEA Config", JOptionPane.INFORMATION_MESSAGE);
        }
      });
    menuBar.add(menuHelp); 
    menuHelp.add(menuHelpAbout);
    menuHelpAbout.addActionListener(new ActionListener()
      {
        public void actionPerformed(ActionEvent ae)
        {
          Utils.displayHelpAbout(instance);
        }
      });
    menuHelp.add(menuHelpBeaufort);
    menuHelpBeaufort.addActionListener(new ActionListener()
      {
        public void actionPerformed(ActionEvent ae)
        {
          BeaufortPanel bp = new BeaufortPanel();
          JOptionPane.showMessageDialog(instance, bp, "Beaufort Scale", JOptionPane.PLAIN_MESSAGE);
        }
      });
    
    getContentPane().setLayout(borderLayout1);
    setSize(new Dimension(770, 650));
    setTitle(LogisailResourceBundle.buildMessage("frame-title"));
    try { this.setIconImage(new ImageIcon(this.getClass().getResource("controller.png")).getImage()); } catch (Exception ignore) {}
    getContentPane().add(nmeaTP, BorderLayout.CENTER);
    
    nmeaTP.getVp().setCurrentSentences(currentSentencesToLog);
  }

  public void setStatus(String str)
  {
    nmeaTP.setLogMessage(str);
  }
}
