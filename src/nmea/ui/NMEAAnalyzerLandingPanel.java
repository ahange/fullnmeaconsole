package nmea.ui;

import astro.calc.GeoPoint;

import coreutilities.Utilities;

import java.awt.BorderLayout;
import java.awt.Dimension;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Insets;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

import java.util.Date;
import java.util.Map;
import java.util.Set;
import java.util.TimeZone;
import java.util.TreeMap;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import javax.swing.ButtonGroup;
import javax.swing.JButton;
import javax.swing.JComboBox;
import javax.swing.JFileChooser;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JProgressBar;
import javax.swing.JRadioButton;
import javax.swing.JTextField;
import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;

import nmea.event.NMEAReaderListener;

import nmea.server.constants.Constants;
import nmea.server.ctx.NMEAContext;
import nmea.server.ctx.NMEADataCache;

import nmea.ui.widgets.LoggedDataTable;
import nmea.ui.widgets.PositionPanel;

import ocss.nmea.parser.GeoPos;
import ocss.nmea.parser.OverGround;
import ocss.nmea.parser.RMC;
import ocss.nmea.parser.StringParsers;
import ocss.nmea.parser.Wind;

import utils.NMEAAnalyzer;

import utils.NMEAAnalyzer.ScalarValue;

import utils.log.LogAnalysis;
import utils.log.LoggedDataSelectedInterface;

public class NMEAAnalyzerLandingPanel 
     extends JPanel
  implements LoggedDataSelectedInterface
{
  @SuppressWarnings("compatibility:-4182475730283721677")
  public final static long serialVersionUID = 1L;
  
  private NMEAAnalyzerLandingPanel instance = this;

  private GridBagLayout layout  = new GridBagLayout();
  private JLabel fileInLabel = new JLabel();
  private JLabel fileOutLabel = new JLabel();
  private JTextField fileInTextField = new JTextField();
  private JButton browseFileInButton = new JButton();
  private JButton parseButton = new JButton();
  private LoggedDataTable loggedDataTable = null;
  private transient NMEAAnalyzer na = null;
  private transient LogAnalysis[] laa = null;
  private PositionPanel positionPanel = new PositionPanel();
  private ButtonGroup group = new ButtonGroup();
  private JRadioButton gpsRadioButton = new JRadioButton();
  private JRadioButton manualRadioButton = new JRadioButton();
  private JRadioButton fromLogRadioButton = new JRadioButton();
  private JComboBox timeZoneComboBox = new JComboBox();
  private JTextField narrowTextField = new JTextField();
  private JPanel tzPanel = new JPanel();
  
  private JPanel mainPanel = new JPanel();
  private JPanel bottomPanel = new JPanel();
  private JLabel status = new JLabel("");
  private JProgressBar progressBar = new JProgressBar();

  public NMEAAnalyzerLandingPanel()
  {
    try
    {
      jbInit();
    }
    catch (Exception e)
    {
      e.printStackTrace();
    }
  }

  @Override
  public void setNbRecordProcessed(int nb)
  {
    status.setText(Integer.toString(nb) + " record(s) processed.");
  }
  
  @Override
  public void setSelectedData(String data, boolean b)
  {
    System.out.println("Selected " + data + ", " + b);
    Map<String, Map<Date, Object>> fullData = na.getFullData();
    Set<String> keys = fullData.keySet();
    int idx = -1, i = 0;
    for (String k : keys)
    {
      if (data.equals(k))
      {
        idx = i;
        break;
      }
      else
        i++;
    }
    if (idx == -1) // Not found, weird
    {
      JOptionPane.showMessageDialog(this, "Requested data [" + data + "] not found in the log");
      return;
    }
    else
    {
      Map<Date, ScalarValue> newData = null;
      String newTitle = "Data Title";
      String newUnit = "?";
      if (b && laa[idx] == null)
      {
       /* VWR: Wind
        * MWV: Wind
        * RMC: RMC
        * RMB: RMB
        * BAT: float
        * HDG: double[] [HDG_in_HDG, DEV_in_HDG, VAR_in_HDG]
        * VLW: double[] [LOG_in_VLW, DAILYLOG_in_VLW]
        * VHW: double
        * MTW: double
        * MTA: double
        * MMB: double
        * DPT: float
        * GLL: Object[] [GeoPos, Date]
        * VTG: OverGround
        */
        if ("BAT".equals(data))
        {
          newTitle = "Battery Voltage";
          newUnit  = "V";
        }
        else if ("DPT".equals(data))
        {
          newTitle = "Depth";
          newUnit  = "m";
        }
        else if ("VHW".equals(data))
        {
          newTitle = "Speed Through Water";
          newUnit  = "kt";
        }
        else if ("VWR".equals(data) || "MWV".equals(data))
        {
          newTitle = "Apparent Wind Speed";
          newUnit  = "kt";
        }
        else if ("MTW".equals(data))
        {
          newTitle = "Water Temperature";
          newUnit  = "\272C";
        }
        else if ("MTA".equals(data))
        {
          newTitle = "Air Temperature";
          newUnit  = "\272C";
        }
        else if ("MMB".equals(data))
        {
          newTitle = "Atmospheric Pressure";
          newUnit  = "hPa";
        }
        else if ("RMC".equals(data) || "VTG".equals(data))
        {
          newTitle = "Speed over Ground";
          newUnit  = "kt";
        }
        else if ("XDR".equals(data))
        {
          newTitle = "Relative Humidity";
          newUnit  = "%";
        }
        newData = new TreeMap<Date, ScalarValue>();
        Map<Date, Object> datamap = fullData.get(data);
        Set<Date> dates = datamap.keySet();        
        System.out.println(data + ":selected " + Integer.toString(dates.size()) + " record(s).");
        int nb = 0;
        for (Date d : dates)
        {
          nb++;
//          if (nb % 1000 == 0)
//            System.out.println("... Processed " + Integer.toString(nb) + " records.");
          if ("BAT".equals(data))
          {            
            NMEAAnalyzer.BatteryVoltage bv = (NMEAAnalyzer.BatteryVoltage)datamap.get(d);
            newData.put(d, bv);
          }
          else if ("DPT".equals(data))
          {
            NMEAAnalyzer.Depth depth = (NMEAAnalyzer.Depth)datamap.get(d);
            newData.put(d, depth);
          }
          else if ("RMC".equals(data))
          {
            RMC rmc = (RMC)datamap.get(d);
            newData.put(d, new ScalarValue(rmc.getSog()));
          }
          else if ("VWR".equals(data))
          {
            Wind wind = (Wind)datamap.get(d);
            newData.put(d, new ScalarValue(wind.speed));
          }
          else if ("VHW".equals(data))
          {
            NMEAAnalyzer.Bsp bsp = (NMEAAnalyzer.Bsp)datamap.get(d);
            newData.put(d, bsp);
          }
          else if ("VTG".equals(data))
          {
            OverGround og = (OverGround)datamap.get(d);
            newData.put(d, new ScalarValue(og.getSpeed()));
          }
          else if ("MTW".equals(data))
          {
            NMEAAnalyzer.WaterTemp wt = (NMEAAnalyzer.WaterTemp)datamap.get(d);
            newData.put(d, wt);
          }
          else if ("MTA".equals(data))
          {
            NMEAAnalyzer.AirTemp at = (NMEAAnalyzer.AirTemp)datamap.get(d);
            newData.put(d, at);
          }
          else if ("MMB".equals(data))
          {
            NMEAAnalyzer.AtmPressure ap = (NMEAAnalyzer.AtmPressure)datamap.get(d);
            newData.put(d, ap);
          }
          else if ("XDR".equals(data))
          {
            NMEAAnalyzer.Humidity h = (NMEAAnalyzer.Humidity)datamap.get(d);
            newData.put(d, h);
          }
        }  
        System.out.println(data + ":Step one completed.");        
      }
      if (laa[idx] == null)
      {
        try
        {
          GeoPoint gp = positionPanel.getPosition();
          laa[idx] = new LogAnalysis(data, newData, newTitle, newUnit, new GeoPos(gp.getL(), gp.getG()), (String)timeZoneComboBox.getSelectedItem());
        }
        catch (Exception ex)
        {
          ex.printStackTrace();
        }
      }
      if (laa[idx] != null)
      {
        if (b)
        {
          laa[idx].setTimeZone((String)timeZoneComboBox.getSelectedItem());
          laa[idx].show();
        }
        else
          laa[idx].hide();
      }
      else
        System.out.println("Ooops...");
    }
  }

  private void jbInit()
    throws Exception
  {
    this.setLayout(new BorderLayout());
    loggedDataTable = new LoggedDataTable(this);
    mainPanel.setLayout(layout);
    setSize(new Dimension(450, 440));
    fileInLabel.setText("Log file Name:");
    fileInTextField.setPreferredSize(new Dimension(200, 19));
    browseFileInButton.setText("...");
    browseFileInButton.addActionListener(new ActionListener()
    {
      public void actionPerformed(ActionEvent e)
      {
        browseFileInButton_actionPerformed(e);
      }
    });
    parseButton.setText("Parse");
    parseButton.addActionListener(new ActionListener()
    {
      public void actionPerformed(ActionEvent e)
      {
        parseButton_actionPerformed(e);
      }
    });
    mainPanel.add(fileInLabel,
                  new GridBagConstraints(0, 3, 1, 1, 0.0, 0.0, GridBagConstraints.EAST, GridBagConstraints.NONE,
                                         new Insets(0, 0, 0, 0), 0, 0));
    mainPanel.add(fileOutLabel,
                  new GridBagConstraints(0, 4, 1, 1, 0.0, 0.0, GridBagConstraints.EAST, GridBagConstraints.NONE,
                                         new Insets(0, 0, 0, 0), 0, 0));
    mainPanel.add(fileInTextField,
                  new GridBagConstraints(1, 3, 1, 1, 0.0, 0.0, GridBagConstraints.CENTER, GridBagConstraints.NONE,
                                         new Insets(0, 5, 0, 5), 0, 0));
    mainPanel.add(browseFileInButton,
                  new GridBagConstraints(2, 3, 1, 1, 0.0, 0.0, GridBagConstraints.CENTER, GridBagConstraints.NONE,
                                         new Insets(0, 0, 0, 0), 0, 0));
    mainPanel.add(parseButton,
                  new GridBagConstraints(0, 4, 3, 1, 0.0, 0.0, GridBagConstraints.CENTER, GridBagConstraints.NONE,
                                         new Insets(5, 0, 5, 0), 0, 0));
    mainPanel.add(loggedDataTable,
                  new GridBagConstraints(0, 6, 3, 1, 0.0, 0.0, GridBagConstraints.CENTER, GridBagConstraints.HORIZONTAL,
                                         new Insets(5, 0, 0, 0), 0, 0));
    mainPanel.add(positionPanel,
                  new GridBagConstraints(1, 0, 2, 3, 0.0, 0.0, GridBagConstraints.NORTH, GridBagConstraints.NONE,
                                         new Insets(5, 0, 5, 0), 0, 0));
    positionPanel.setToolTipText("Used to calculate daylight on the graphs");
    mainPanel.add(tzPanel,
                  new GridBagConstraints(0, 5, 3, 1, 0.0, 0.0, GridBagConstraints.CENTER, GridBagConstraints.HORIZONTAL,
                                         new Insets(0, 0, 0, 0), 0, 0));
    mainPanel.add(gpsRadioButton,
                  new GridBagConstraints(0, 0, 1, 1, 0.0, 0.0, GridBagConstraints.NORTHWEST, GridBagConstraints.NONE,
                                         new Insets(0, 0, 0, 0), 0, 0));
    mainPanel.add(manualRadioButton,
                  new GridBagConstraints(0, 2, 1, 1, 0.0, 0.0, GridBagConstraints.NORTHWEST, GridBagConstraints.NONE,
                                         new Insets(0, 0, 0, 0), 0, 0));
    mainPanel.add(fromLogRadioButton,
                  new GridBagConstraints(0, 1, 1, 1, 0.0, 0.0, GridBagConstraints.CENTER, GridBagConstraints.NONE,
                                         new Insets(0, 0, 0, 0), 0, 0));
    timeZoneComboBox.removeAllItems();
    String[] tzIDs = TimeZone.getAvailableIDs();
    for (String tz: tzIDs)
      timeZoneComboBox.addItem(tz);
    timeZoneComboBox.setSelectedItem("Etc/UTC");
    timeZoneComboBox.setPreferredSize(new Dimension(200, 20));
    tzPanel.add(timeZoneComboBox, null);
    tzPanel.add(narrowTextField, null);
    narrowTextField.setToolTipText("Restriction on the Time Zone (filter, regex)");
    narrowTextField.setPreferredSize(new Dimension(100, 20));
    narrowTextField.getDocument().addDocumentListener(new DocumentListener()
    {
      public void insertUpdate(DocumentEvent e)
      {
        narrowTZList(narrowTextField.getText());
      }

      public void removeUpdate(DocumentEvent e)
      {
        narrowTZList(narrowTextField.getText());
      }

      public void changedUpdate(DocumentEvent e)
      {
        narrowTZList(narrowTextField.getText());
      }
    });

    loggedDataTable.setPreferredSize(new Dimension(300, 150));
    group.add(gpsRadioButton);
    group.add(manualRadioButton);
    group.add(fromLogRadioButton);
    gpsRadioButton.setSelected(false);
    manualRadioButton.setSelected(true);
    gpsRadioButton.setText("From GPS");
    manualRadioButton.setText("Manual Entry");
    fromLogRadioButton.setText("From the logged data");
    fromLogRadioButton.setToolTipText("<html>Position will be read from RMC sentence<br><b>during the parsing</b> of the data file.</html>");
    gpsRadioButton.addActionListener(new ActionListener()
    {
      public void actionPerformed(ActionEvent e)
      {
        gpsRadioButton_actionPerformed(e);
      }
    });
    manualRadioButton.addActionListener(new ActionListener()
    {
      public void actionPerformed(ActionEvent e)
      {
        manualPosRadioButton_actionPerformed(e);
      }
    });
    fromLogRadioButton.addActionListener(new ActionListener()
    {
      public void actionPerformed(ActionEvent e)
      {
        fromLogRadioButton_actionPerformed(e);
      }
    });
    
    NMEAContext.getInstance().addNMEAReaderListener(new NMEAReaderListener("Analyzer", "NMEAAnalyzer")
    {
      public void manageNMEAString(String str)
      {
        //  System.out.println("NMEA:" + str);
        if (str.trim().length() > 6 && str.startsWith("$") && gpsRadioButton.isSelected())
        {
          if (StringParsers.validCheckSum(str) &&
              (str.substring(3, 6).equals("RMC") || str.substring(3, 6).equals("GLL")))
          {
            try
            {
              if (NMEAContext.getInstance().getCache().get(NMEADataCache.POSITION) != null)
              {
                GeoPos position = (GeoPos) NMEAContext.getInstance().getCache().get(NMEADataCache.POSITION);
                positionPanel.setPosition(position.lat, position.lng);
              }
            }
            catch (Exception ex)
            {
              // No cache yet
            }
          }
        }
      }

      public void loggedDataAnalyzerFrameClosed(String id)
      {
        // Update the JTable
        loggedDataTable.setSelectedRow(id, false);
      }
    });
    this.add(mainPanel, BorderLayout.CENTER);
    bottomPanel.setLayout(new BorderLayout());
    this.add(bottomPanel, BorderLayout.SOUTH);
    bottomPanel.add(status, BorderLayout.CENTER);
    bottomPanel.add(progressBar, BorderLayout.EAST);
  }

  private void narrowTZList(String filter)
  {
    Pattern pattern = Pattern.compile(".*" + filter + ".*", Pattern.CASE_INSENSITIVE);
    timeZoneComboBox.removeAllItems();
    for (String tz : TimeZone.getAvailableIDs())
    {
      Matcher tzMatcher = pattern.matcher(tz);
      if (tzMatcher.find())
        timeZoneComboBox.addItem(tz);
    }
//  String tz = (String)timeZoneComboBox.getSelectedItem();
  }

  private void browseFileInButton_actionPerformed(ActionEvent e)
  {
    String fileIn = Utilities.chooseFile(JFileChooser.FILES_ONLY, "nmea", "NMEA Log-Files", "Choose the logged data file", "Select");
    if (fileIn.trim().length() > 0)
      fileInTextField.setText(fileIn);
  }

  private void manualPosRadioButton_actionPerformed(ActionEvent e)
  {
    positionPanel.setEnabled(manualRadioButton.isSelected());
    positionPanel.showButton(manualRadioButton.isSelected());
  }

  private void gpsRadioButton_actionPerformed(ActionEvent e)
  {
    positionPanel.setEnabled(manualRadioButton.isSelected());
    positionPanel.showButton(manualRadioButton.isSelected());
  }

  private void fromLogRadioButton_actionPerformed(ActionEvent e)
  {
    positionPanel.setEnabled(manualRadioButton.isSelected());
    positionPanel.showButton(manualRadioButton.isSelected());
  }
  
  private void parseButton_actionPerformed(ActionEvent e)
  {
    // Clean the table, remove all opened frames.
    loggedDataTable.clear();
    loggedDataTable.repaint();
    if (laa != null)
    {
      for (int i=0; i<laa.length; i++)
      {
        if (laa[i] != null)
        {
          laa[i].hide();
          laa[i].close();
        }
      }
    }
    laa = null;
    
    Thread parser = new Thread()
    {
      public void run()
      {
        System.out.println(">>> Parser Thread started");
        try
        {
          parseButton.setEnabled(false);
          String fIn = fileInTextField.getText();
          na = new NMEAAnalyzer(fIn, instance);
          Map<String, Integer> map = na.getDataMap();
          if (map.containsKey("RMC") && fromLogRadioButton.isSelected())
          {
            Map<Date, Object> rmcMap = na.getFullData().get("RMC");
            RMC rmc = (RMC)rmcMap.get(rmcMap.keySet().iterator().next());
            if (rmc != null && rmc.getGp() != null)
            {
              GeoPos gp = rmc.getGp();
              positionPanel.setPosition(gp.lat, gp.lng);
              positionPanel.repaint();
            }
          }
          // Dump
          for (String s : map.keySet())
            System.out.println(s + ":" + map.get(s).intValue() + ", " + (Constants.getInstance().getNMEAMap().get(s) == null ? "[Non standard]" : Constants.getInstance().getNMEAMap().get(s)));
          System.out.println("================================");
          
          Map<String, Map<Date, Object>> fullData = na.getFullData();
          System.out.println("Full Data: " + fullData.size() + " entry(ies).");
          
          Set<String> keys = fullData.keySet();
          for (String k : keys)
            loggedDataTable.addLineInTable(k);
          
          laa = new LogAnalysis[keys.size()];
          for (int i=0; i<laa.length; i++)
            laa[i] = null;
        }
        catch (Exception ex)
        {
          ex.printStackTrace();
        }
        finally
        {
          System.out.println(">>> Parser Thread done working.");
          loggedDataTable.repaint();
          parseButton.setEnabled(true);
//        status.setText("Parsing completed"); // Taken care of by the interface
          progressBar.setIndeterminate(false);
          progressBar.setEnabled(false);
          progressBar.repaint();
        }
      }
    };
    parser.start();
    
    Thread progress = new Thread()
    {
      public void run()
      {
        System.out.println(">>> Progress Thread started");
        status.setText("Parsing...");
        progressBar.setIndeterminate(true);   
        progressBar.setEnabled(true);
        progressBar.repaint();
      }
    };
    progress.start();
  }
}
