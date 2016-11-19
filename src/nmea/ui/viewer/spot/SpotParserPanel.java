package nmea.ui.viewer.spot;

import coreutilities.gui.CircularDisplay;
import coreutilities.gui.PartCircularDisplay;
import coreutilities.gui.SpeedoPanel;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Insets;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

import java.text.DecimalFormat;

import java.util.List;
import java.util.TimeZone;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import javax.swing.JCheckBox;
import javax.swing.JComboBox;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JTextField;
import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;

import nmea.event.NMEAReaderListener;

import nmea.server.ctx.NMEAContext;

import nmea.ui.viewer.elements.CurrentDisplay;
import nmea.ui.viewer.spot.utils.SpotParser.SpotLine;

import ocss.nmea.parser.GeoPos;

public class SpotParserPanel
     extends JPanel
{
  private final static DecimalFormat DF2 = new DecimalFormat("00");
  private BorderLayout borderLayout1 = new BorderLayout();
  private JPanel parsedDataPanel = new JPanel();
  private SpeedoPanel twsSpeedoPanel = null;
  private CircularDisplay twdDisplay = null;
  private WindGaugePanel windGauge = null;
  private GridBagLayout gridBagLayout1 = new GridBagLayout();
  private SpotCanvas spotCanvas = new SpotCanvas();
  private transient List<SpotLine> spotLines = null;
  private JPanel controlPanel = new JPanel();
  private JCheckBox rawCheckBox = new JCheckBox();
  private JCheckBox smoothCheckBox = new JCheckBox();
  private JComboBox timeZoneComboBox = new JComboBox();
  private JTextField narrowTextField = new JTextField();
  private JCheckBox dateCheckBox = new JCheckBox();
  private JLabel twsLabel = new JLabel();
  private JLabel twdLabel = new JLabel();
  private JLabel rainLabel = new JLabel();
  private PartCircularDisplay prmslDisplay = null;
  private JLabel prmslLabel = new JLabel();
  private JCheckBox rainCheckBox = new JCheckBox();

  public SpotParserPanel()
  {
    jbInit();
  }

  private void jbInit()
  {
    this.setLayout(borderLayout1);
    this.setBackground(Color.black);
    this.setSize(new Dimension(801, 478));
    parsedDataPanel.setBackground(Color.black);
//  spotScrollPane.setMaximumSize(new Dimension(32767, 37));
    parsedDataPanel.setLayout(gridBagLayout1);
    this.add(parsedDataPanel, BorderLayout.CENTER);
    
    windGauge = new WindGaugePanel();
    windGauge.setPreferredSize(new Dimension(50, 100));

    rawCheckBox.setText("Raw Data");
    rawCheckBox.setSelected(true);
    rawCheckBox.addActionListener(new ActionListener()
    {
      public void actionPerformed(ActionEvent e)
      {
        rawCheckBox_actionPerformed(e);
      }
    });
    smoothCheckBox.setText("Smooth Data");
    smoothCheckBox.setSelected(true);
    smoothCheckBox.addActionListener(new ActionListener()
    {
      public void actionPerformed(ActionEvent e)
      {
        smoothCheckBox_actionPerformed(e);
      }
    });
    timeZoneComboBox.removeAllItems();
    String[] tzIDs = TimeZone.getAvailableIDs();
    for (String tz : tzIDs)
      timeZoneComboBox.addItem(tz);
    timeZoneComboBox.setSelectedItem("Etc/UTC");
    timeZoneComboBox.setPreferredSize(new Dimension(200, 20));
    timeZoneComboBox.addActionListener(new ActionListener()
    {
      public void actionPerformed(ActionEvent e)
      {
        timeZoneComboBox_actionPerformed(e);
      }
    });
    parsedDataPanel.add(windGauge,
                        new GridBagConstraints(3, 1, 1, 1, 1.0, 1.0, GridBagConstraints.CENTER, GridBagConstraints.NONE,
                                               new Insets(0, 0, 0, 0), 0, 0));
    
    double maxTWS = Double.parseDouble(System.getProperty("max.analog.tws", "50"));
    System.out.println("Setting MAX TWS to " + maxTWS);
    twsSpeedoPanel = new SpeedoPanel(maxTWS, false);
    twsSpeedoPanel.withBeaufortScale(true);

    twsSpeedoPanel.setPreferredSize(new Dimension(200, 120));
    twsSpeedoPanel.setLabel("TWS");
    parsedDataPanel.add(twsSpeedoPanel,
                        new GridBagConstraints(0, 1, 1, 1, 1.0, 1.0, GridBagConstraints.CENTER, GridBagConstraints.NONE,
                                               new Insets(0, 0, 0, 0), 0, 0));
    twdDisplay = new CircularDisplay("TWD", "000", "True Wind");
    twdDisplay.setPreferredSize(new Dimension(150, 150));
    twdDisplay.setDisplayColor(Color.green);
    twdDisplay.setBackground(Color.black);
    parsedDataPanel.add(twdDisplay,
                        new GridBagConstraints(1, 1, 1, 1, 1.0, 1.0, GridBagConstraints.CENTER, GridBagConstraints.NONE,
                                               new Insets(0, 0, 0, 0), 0, 0));

    parsedDataPanel.add(spotCanvas,
                        new GridBagConstraints(0, 3, 34, 1, 0.0, 0.0, GridBagConstraints.CENTER,
                                               GridBagConstraints.BOTH, new Insets(10, 5, 10, 5), 0, 0));
    controlPanel.add(dateCheckBox, null);
    controlPanel.add(rawCheckBox, null);
    controlPanel.add(smoothCheckBox, null);
    controlPanel.add(rainCheckBox, null);
    controlPanel.add(timeZoneComboBox, null);
    controlPanel.add(narrowTextField, null);
    narrowTextField.setToolTipText("Restriction on the Time Zone (filter, regex)");
    narrowTextField.setPreferredSize(new Dimension(100, 20));
    dateCheckBox.setText("Date");
    dateCheckBox.setSelected(true);
    dateCheckBox.addActionListener(new ActionListener()
    {
      public void actionPerformed(ActionEvent e)
      {
        dateCheckBox_actionPerformed(e);
      }
    });
    twsLabel.setText("TWS");
    twsLabel.setForeground(new Color(247, 247, 247));
    twdLabel.setText("TWD");
    twdLabel.setForeground(new Color(247, 247, 247));
    rainLabel.setText("RAIN");
    rainLabel.setForeground(new Color(247, 247, 247));
    prmslLabel.setText("PRMSL");
    prmslLabel.setForeground(new Color(247, 247, 247));
    rainCheckBox.setText("Rain");
    rainCheckBox.addActionListener(new ActionListener()
    {
      public void actionPerformed(ActionEvent e)
      {
        rainCheckBox_actionPerformed(e);
      }
    });
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
    parsedDataPanel.add(controlPanel,
                        new GridBagConstraints(0, 2, 4, 1, 0.0, 0.0, GridBagConstraints.CENTER,
                                               GridBagConstraints.HORIZONTAL, new Insets(0, 5, 0, 5), 0, 0));
    parsedDataPanel.add(twsLabel,
                        new GridBagConstraints(0, 0, 1, 1, 0.0, 0.0, GridBagConstraints.SOUTH, GridBagConstraints.NONE,
                                               new Insets(5, 0, 0, 0), 0, 0));
    parsedDataPanel.add(twdLabel,
                        new GridBagConstraints(1, 0, 1, 1, 0.0, 0.0, GridBagConstraints.SOUTH, GridBagConstraints.NONE,
                                               new Insets(5, 0, 0, 0), 0, 0));
    parsedDataPanel.add(rainLabel,
                        new GridBagConstraints(3, 0, 1, 1, 0.0, 0.0, GridBagConstraints.SOUTH, GridBagConstraints.NONE,
                                               new Insets(5, 0, 0, 0), 0, 0));
    prmslDisplay = new PartCircularDisplay(1060, 20, 5, false, 40, 960);
    prmslDisplay.setPreferredSize(new Dimension(175, 175));
    parsedDataPanel.add(prmslDisplay,
                        new GridBagConstraints(2, 1, 1, 1, 0.0, 0.0, GridBagConstraints.CENTER, GridBagConstraints.NONE,
                                               new Insets(0, 0, 0, 0), 0, 0));
    parsedDataPanel.add(prmslLabel,
                        new GridBagConstraints(2, 0, 1, 1, 0.0, 0.0, GridBagConstraints.CENTER, GridBagConstraints.NONE,
                                               new Insets(0, 0, 0, 0), 0, 0));
    NMEAContext.getInstance().addNMEAReaderListener(new NMEAReaderListener("SPOT", "Parser Panel")
      {
        public void newSpotData(List<SpotLine> spotLines, GeoPos pos)
        {
          setSpotLines(spotLines);
          if (spotLines != null && spotLines.size() > 0)
          {
            // Display data
            if (spotLines.size() > 0)
            {
              windGauge.setTws(10 * (float)spotLines.get(0).getRain());
              windGauge.setToolTipText("Rain:" + DF2.format(spotLines.get(0).getRain()) + " mm/h");
              twsSpeedoPanel.setSpeed(spotLines.get(0).getTws());
              twdDisplay.setDirection(spotLines.get(0).getTwd());
              twdDisplay.setAngleValue(spotLines.get(0).getTwd());
              prmslDisplay.setSpeed(spotLines.get(0).getPrmsl());
              // Set min max TWS
              double maxWind = 0D, maxPress = 0D;
              double minWind = Double.MAX_VALUE, minPress = Double.MAX_VALUE;
              float maxRain = 0f;
              for (SpotLine sp : spotLines)
              {
                double tws = sp.getTws();
                maxWind = Math.max(maxWind, tws);
                minWind = Math.min(minWind, tws);
                double prmsl = sp.getPrmsl();
                maxPress = Math.max(maxPress, prmsl);
                minPress = Math.min(minPress, prmsl);
                double rain = sp.getRain();
                maxRain = (float)Math.max((double)maxRain, rain);
              }
              twsSpeedoPanel.setMinMax(minWind, maxWind);
              prmslDisplay.setMinMax(minPress, maxPress);
              windGauge.setMax(10 * maxRain);
            }
            repaint();
          }
          else
            twsSpeedoPanel.resetMinMax();
        }

        public void setSpotLineIndex(int i) 
        {
          if (spotLines != null)
          {
            // Display data
            if (spotLines.size() > 0)
            {
              windGauge.setTws(10 * (float)spotLines.get(i).getRain());
              windGauge.setToolTipText("Rain:" + DF2.format(spotLines.get(i).getRain()) + " mm/h");
              twsSpeedoPanel.setSpeed(spotLines.get(i).getTws());
              twdDisplay.setDirection(spotLines.get(i).getTwd());
              twdDisplay.setAngleValue(spotLines.get(i).getTwd());
              prmslDisplay.setSpeed(spotLines.get(i).getPrmsl());
            }
            repaint();
          }
        }

      });
  }

  private void narrowTZList(String filter)
  {
    adjustingTZ = true;
    Pattern pattern = Pattern.compile(".*" + filter + ".*", Pattern.CASE_INSENSITIVE);
    timeZoneComboBox.removeAllItems();
    for (String tz : TimeZone.getAvailableIDs())
    {
      Matcher tzMatcher = pattern.matcher(tz);
      if (tzMatcher.find())
        timeZoneComboBox.addItem(tz);
    }
    String tz = (String)timeZoneComboBox.getSelectedItem();
    if (tz != null)
      spotCanvas.setTimeZone(tz);
//    timeZoneComboBox.addActionListener(new ActionListener()
//    {
//      public void actionPerformed(ActionEvent e)
//      {
//        timeZoneComboBox_actionPerformed(e);
//      }
//    });
    adjustingTZ = false;
  }

  public void setSpotLines(List<SpotLine> spotLines)
  {
    this.spotLines = spotLines;
    spotCanvas.setSpotLines(spotLines);
  }
  
  private void rawCheckBox_actionPerformed(ActionEvent e)
  {
    spotCanvas.setWithRawData(rawCheckBox.isSelected());
  }

  private void smoothCheckBox_actionPerformed(ActionEvent e)
  {
    spotCanvas.setWithSmoothData(smoothCheckBox.isSelected());
  }

  private boolean adjustingTZ = false;
  private void timeZoneComboBox_actionPerformed(ActionEvent e)
  {
    String tz = (String)timeZoneComboBox.getSelectedItem();
//  System.out.println("TimeZone:" + tz);
    if (tz != null && TimeZone.getTimeZone(tz) != null && !adjustingTZ)
      spotCanvas.setTimeZone(tz);
  }

  private void dateCheckBox_actionPerformed(ActionEvent e)
  {
    spotCanvas.setWithDate(dateCheckBox.isSelected());
    timeZoneComboBox.setEnabled(dateCheckBox.isSelected());
    narrowTextField.setEnabled(dateCheckBox.isSelected());
  }

  private void rainCheckBox_actionPerformed(ActionEvent e)
  {
    spotCanvas.setWithRain(rainCheckBox.isSelected());
  }
}
