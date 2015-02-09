package nmea.ui.viewer;

import coreutilities.gui.HeadingPanel;
import coreutilities.gui.JumboDisplay;
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

import javax.swing.ButtonGroup;
import javax.swing.ImageIcon;
import javax.swing.JButton;
import javax.swing.JCheckBox;
import javax.swing.JComboBox;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JRadioButton;
import javax.swing.JScrollPane;

import nmea.event.NMEAReaderListener;

import nmea.server.constants.Constants;
import nmea.server.ctx.NMEAContext;
import nmea.server.ctx.NMEADataCache;
import nmea.server.utils.Utils;

import nmea.ui.viewer.elements.AWDisplay;
import nmea.ui.viewer.elements.CurrentDisplay;
import nmea.ui.viewer.elements.DrawingBoard;
import nmea.ui.viewer.minimaxi.boatspeed.BoatSpeed;
import nmea.ui.viewer.minimaxi.wind.WindSpeed;

import ocss.nmea.parser.Angle180;
import ocss.nmea.parser.Angle180LR;
import ocss.nmea.parser.Angle360;
import ocss.nmea.parser.Speed;
import ocss.nmea.parser.TrueWindDirection;
import ocss.nmea.parser.TrueWindSpeed;
import ocss.nmea.utils.WindUtils;

public class CurrentSituationPanel
  extends JPanel
{
  private CurrentSituationPanel instance = this;
  
  private JPanel drawingPlusCompass = new JPanel();
  private DrawingBoard drawingBoard = new DrawingBoard();
  private JPanel compasPanel = new JPanel();
  private HeadingPanel hdgPanel = new HeadingPanel(true);
  private HeadingPanel cogPanel = new HeadingPanel(true);
  private JScrollPane rightScrollPane = null;
  private JPanel rightPanel = new JPanel();
  private JPanel displayPanel = new JPanel();
  private JPanel topDisplayPanel = new JPanel();
  private JumboDisplay bspDisplay = null;
  private JumboDisplay hdgDisplay = null;
  private JumboDisplay awaDisplay = null;
  private JumboDisplay awsDisplay = null;
  private JumboDisplay twaDisplay = null;
  private JumboDisplay twsDisplay = null;
  private JumboDisplay twdDisplay = null;
  private JumboDisplay lwyDisplay = null;
  private JumboDisplay cdrDisplay = null;
  private JumboDisplay cspDisplay = null;
  private JumboDisplay cogDisplay = null;
  private JumboDisplay sogDisplay = null;
  private JumboDisplay beaufortDisplay = null;
  
  private SpeedoPanel bspSpeedoPanel = null;
  private SpeedoPanel twsSpeedoPanel = null;
  
  private JumboDisplay vmgDisplay = null;
  private JRadioButton vmgWithHDG = null;
  private JRadioButton vmgWithCOG = null;
  private JPanel vmgRBPanel = null;

  private JPanel speedoPanel = null;
  private JPanel speedoPanelHolder = null;
  
  private int basicJumboSize = 24;
  
  private AWDisplay awDisplay           = null;
  private CurrentDisplay currentDisplay = null;
  
  private BoatSpeed bspMinMaxPanel = new BoatSpeed(basicJumboSize);
  private WindSpeed twsMinMaxPanel = new WindSpeed(basicJumboSize);
  
  private DecimalFormat df3 = new DecimalFormat("000");
  private DecimalFormat df2 = new DecimalFormat("#0");
  private DecimalFormat df22 = new DecimalFormat("00.00");
  private GridBagLayout gridBagLayout1 = new GridBagLayout();
  private JLabel hdgLabel = new JLabel();
  private JLabel cogLabel = new JLabel();
  private GridBagLayout gridBagLayout2 = new GridBagLayout();
  
  private JCheckBox showLeftPaneCheckBox = new JCheckBox();
  private JCheckBox miniMaxiCheckBox = new JCheckBox();
  private JCheckBox beaufortCheckBox = new JCheckBox();
  private JCheckBox vmgCheckBox = new JCheckBox();
  private JComboBox vmgZmgComboBox = new JComboBox();
  private JCheckBox showTemperatureCheckBox = new JCheckBox();
  private JCheckBox analogDisplayCheckBox = new JCheckBox();
  private JCheckBox perimeterTicksCheckBox = new JCheckBox();

  private JCheckBox displayCurrentCheckBox = new JCheckBox();
  
  private JPanel twsMethodPanel = new JPanel();
  private JLabel twLabel = new JLabel("True Wind:");
  private JRadioButton gpsMethod = new JRadioButton("With GPS Data");
  private JRadioButton bspMethod = new JRadioButton("With BSP & AW");
  private ButtonGroup twsMethodGroup = new ButtonGroup();

  private boolean frozen = false;
  private ImageIcon pause = new ImageIcon(this.getClass().getResource("elements/resources/pause.png"));
  private ImageIcon start = new ImageIcon(this.getClass().getResource("elements/resources/start.png"));
  private JButton freezeButton = new JButton();
  private JButton shiftLeftRightButton = new JButton();
  private ImageIcon left  = new ImageIcon(this.getClass().getResource("elements/resources/shuttleLeftAll.png"));
  private ImageIcon right = new ImageIcon(this.getClass().getResource("elements/resources/shuttleRightAll.png"));
  
  private JButton resetMinMaxButton = new JButton();
  private JCheckBox withMinMaxJCheckBox = new JCheckBox("With Min & Max");
  
  private boolean jumbosOnTheRight = true;
  private JCheckBox autoScaleCheckBox = new JCheckBox();
  private JCheckBox perfCheckBox = new JCheckBox();
  //private GridBagLayout gridBagLayout3 = new GridBagLayout();

  public CurrentSituationPanel()
  {
    try
    {
      jbInit();
    }
    catch (Exception ex)
    {
      ex.printStackTrace();
    }
  }

  private void jbInit()
    throws Exception
  {
    NMEAContext.getInstance().addNMEAReaderListener(new NMEAReaderListener(Constants.NMEA_SERVER_LISTENER_GROUP_ID, "Current Situation")
      {
        public void dataUpdate() 
        {
          NMEADataCache cache = NMEAContext.getInstance().getCache();
          if (frozen)
            cache = NMEAContext.getInstance().getFrozenDataCache();
          if (cache != null)
          {
            try 
            { 
              double hdg = ((Angle360)cache.get(NMEADataCache.HDG_TRUE)).getValue() + ((Double)cache.get(NMEADataCache.HDG_OFFSET)).doubleValue();              
              setHDG(hdg);               
              hdgPanel.setValue(hdg);
            } 
            catch (Exception ex) { System.err.println(ex.toString()); }
            try { setBSP(((Speed)cache.get(NMEADataCache.BSP)).getValue() * ((Double)cache.get(NMEADataCache.BSP_FACTOR)).doubleValue()); } catch (Exception ex) {}
            try { setSOG(((Speed)cache.get(NMEADataCache.SOG)).getValue()); } catch (Exception ex) {}
            try 
            { 
              double cog = ((Angle360)cache.get(NMEADataCache.COG)).getValue();
              setCOG(cog); 
              cogPanel.setValue(cog);
            } 
            catch (Exception ex) {}
            
            try { setAWA(((Angle180)cache.get(NMEADataCache.AWA)).getValue() + ((Double)cache.get(NMEADataCache.AWA_OFFSET)).doubleValue()); } catch (Exception ex) {}
            try { setAWS(((Speed)cache.get(NMEADataCache.AWS)).getValue() * ((Double)cache.get(NMEADataCache.AWS_FACTOR)).doubleValue()); } catch (Exception ex) {}
            
            if (frozen)
            {
              // Recalculated Data - if coeffs have been changed
              Utils.computeAndSendValuesToCache(cache);
            }
            
            try { setTWS(((TrueWindSpeed)cache.get(NMEADataCache.TWS)).getValue()); } catch (Exception ex) {}
            try { setTWA(((Angle180)cache.get(NMEADataCache.TWA)).getValue()); } catch (Exception ex) {}
            try { setTWD(((TrueWindDirection)cache.get(NMEADataCache.TWD)).getValue()); } catch (Exception ex) {}
            try { setLWY(((Angle180LR)cache.get(NMEADataCache.LEEWAY)).getValue()); } catch (Exception ex) {}
            try { setCDR(((Angle360)cache.get(NMEADataCache.CDR)).getValue()); } catch (Exception ex) {}
            try { setCSP(((Speed)cache.get(NMEADataCache.CSP)).getValue()); } catch (Exception ex) {}
            
            if (NMEAContext.getInstance().isAutoScale())
            {
              double tws = ((TrueWindSpeed)cache.get(NMEADataCache.TWS)).getValue();
              boolean found = false;
              for (NMEAContext.WindScale ws : NMEAContext.WindScale.values())
              {
                if (ws.speed() > tws)
                {
                  if (NMEAContext.getInstance().getCurrentWindScale() != ws.scale())
                    NMEAContext.getInstance().fireWindScale(ws.scale());
                  found = true;
                  break;
                }
              }
              if (!found)
                NMEAContext.getInstance().fireWindScale(NMEAContext.WindScale._50_60.scale());
            }
            // VMG
            try
            {
              double vmg = 0d;
              if (vmgZmgComboBox.getSelectedIndex() == 0) // On Wind
              {
                vmgDisplay.setName("VMG");
                vmgDisplay.setToolTipText("Velocity Made Good");
                if (vmgWithHDG.isSelected())
                {
                  double twa = ((Angle180)cache.get(NMEADataCache.TWA)).getValue();
                  double bsp = ((Speed)cache.get(NMEADataCache.BSP)).getValue();
                  if (bsp > 0)
                    vmg = bsp * Math.cos(Math.toRadians(twa));
                }
                else if (vmgWithCOG.isSelected())
                {
                  double sog = (((Speed)cache.get(NMEADataCache.SOG)).getValue());
                  double cog = ((Angle360)cache.get(NMEADataCache.COG)).getValue();
                  double twd = (((TrueWindDirection)cache.get(NMEADataCache.TWD)).getValue());
                  double twa = twd - cog;
                  if (sog > 0)
                    vmg = sog * Math.cos(Math.toRadians(twa));
                }
  //            System.out.println("VMG: " + vmg);
                setVMG(vmg);
                drawingBoard.setVMGValue(vmg);
              }
              else if (vmgZmgComboBox.getSelectedIndex() == 1) // On Waypoint
              {
//              System.out.println("Bearing to [" + cache.get(NMEADataCache.TO_WP) + "] :" + ((Angle360)cache.get(NMEADataCache.B2WP)).getValue() + " \272" );
                if (cache.get(NMEADataCache.TO_WP) != null && cache.get(NMEADataCache.TO_WP).toString().trim().length() > 0)
                {
                  vmgDisplay.setName(cache.get(NMEADataCache.TO_WP).toString().trim());
                  vmgDisplay.setToolTipText("Velocity Made Good to " + cache.get(NMEADataCache.TO_WP).toString().trim());
                  double b2wp = ((Angle360)cache.get(NMEADataCache.B2WP)).getValue();
                  if (vmgWithHDG.isSelected())
                  {
                    double angle = b2wp - ((Angle360)cache.get(NMEADataCache.HDG_TRUE)).getValue();
                    double bsp = ((Speed)cache.get(NMEADataCache.BSP)).getValue();
                    vmg = bsp * Math.cos(Math.toRadians(angle));
                  }
                  else if (vmgWithCOG.isSelected())
                  {
                    double sog = (((Speed)cache.get(NMEADataCache.SOG)).getValue());
                    double cog = ((Angle360)cache.get(NMEADataCache.COG)).getValue();
                    double angle = b2wp - cog;
                    vmg = sog * Math.cos(Math.toRadians(angle));
                  }
                  setVMG(vmg);
                  drawingBoard.setVMGValue(vmg, cache.get(NMEADataCache.TO_WP).toString().trim(), b2wp);
                }
              }
            }
            catch (Exception ex)
            {
              System.err.println(ex.toString());
            }
            // Performance
            Object obj = NMEAContext.getInstance().getCache().get(NMEADataCache.PERF);
            if (obj != null && perfCheckBox.isSelected())
            {
              double performance = ((Double)obj).doubleValue();
              drawingBoard.setPerformance(performance);
            }
            else
            {
              perfCheckBox.setEnabled(obj != null);
              drawingBoard.setPerformance(-1d);
            }
          }
          repaint();
        } 
      });

    this.setLayout(new BorderLayout());
    drawingPlusCompass.setLayout(new BorderLayout());
    compasPanel.setLayout(gridBagLayout1);
    hdgPanel.setSize(new Dimension(428, 40));
    hdgPanel.setPreferredSize(new Dimension(200, 30));
    hdgPanel.setMinimumSize(new Dimension(200, 30));
    hdgPanel.setRoseWidth(160);
    
    cogPanel.setPreferredSize(new Dimension(200, 30));
    cogPanel.setMinimumSize(new Dimension(200, 30));
    cogPanel.setWhiteOnBlack(false);
    cogPanel.setRoseWidth(160);

    drawingPlusCompass.add(drawingBoard, BorderLayout.CENTER);
    
    compasPanel.add(hdgPanel, new GridBagConstraints(1, 0, 1, 1, 1.0, 0.0, GridBagConstraints.CENTER, GridBagConstraints.HORIZONTAL,
          new Insets(1, 1, 1, 1), 0, 0));
    compasPanel.add(cogPanel, new GridBagConstraints(1, 1, 1, 1, 1.0, 0.0, GridBagConstraints.CENTER, GridBagConstraints.HORIZONTAL,
          new Insets(1, 1, 1, 1), 0, 0));
    compasPanel.add(hdgLabel, new GridBagConstraints(0, 0, 1, 1, 0.0, 0.0, GridBagConstraints.CENTER, GridBagConstraints.NONE,
          new Insets(0, 0, 0, 0), 0, 0));
    compasPanel.add(cogLabel, new GridBagConstraints(0, 1, 1, 1, 0.0, 0.0, GridBagConstraints.CENTER, GridBagConstraints.NONE,
          new Insets(0, 0, 0, 0), 0, 0));
    drawingPlusCompass.add(compasPanel, BorderLayout.SOUTH);
    this.setSize(new Dimension(800, 600));
    this.setPreferredSize(new Dimension(800, 600));
    this.add(drawingPlusCompass, BorderLayout.CENTER);
    rightPanel.setLayout(new BorderLayout());
    rightPanel.add(displayPanel, BorderLayout.NORTH);
    rightScrollPane = new JScrollPane(rightPanel);
//  rightScrollPane.setHorizontalScrollBarPolicy(JScrollPane.HORIZONTAL_SCROLLBAR_NEVER);
//  this.add(rightPanel, BorderLayout.EAST);    
    this.add(rightScrollPane, BorderLayout.EAST);    
    
    displayPanel.setLayout(gridBagLayout2);
    topDisplayPanel.setLayout(new GridBagLayout());
    displayPanel.add(topDisplayPanel, new GridBagConstraints(0, 0, 2, 1, 0.0, 0.0, GridBagConstraints.NORTH, GridBagConstraints.VERTICAL,
          new Insets(0, 0, 0, 0), 0, 0));
    
    displayPanel.add(showLeftPaneCheckBox, new GridBagConstraints(0, 1, 1, 1, 0.0, 0.0, GridBagConstraints.WEST, GridBagConstraints.NONE,
          new Insets(0, 3, 0, 0), 0, 0));
    displayPanel.add(miniMaxiCheckBox, new GridBagConstraints(0, 2, 1, 1, 0.0, 0.0, GridBagConstraints.WEST, GridBagConstraints.NONE,
          new Insets(0, 3, 0, 0), 0, 0));            
    displayPanel.add(analogDisplayCheckBox, new GridBagConstraints(1, 2, 1, 1, 0.0, 0.0, GridBagConstraints.WEST, GridBagConstraints.NONE,
          new Insets(0, 3, 0, 0), 0, 0));
    displayPanel.add(showTemperatureCheckBox,
                     new GridBagConstraints(0, 3, 1, 1, 0.0, 0.0, GridBagConstraints.WEST, GridBagConstraints.NONE,
          new Insets(0, 3, 0, 0), 0, 0));
    JPanel vmgZmgHolder = new JPanel(new BorderLayout());
    vmgZmgHolder.add(vmgCheckBox, BorderLayout.WEST);
    vmgZmgComboBox.removeAllItems();
    vmgZmgComboBox.addItem("VMG on Wind");
    vmgZmgComboBox.addItem("VMG on Waypoint");
    // Make sure there is an active waypoint (RMB) when the 2nd option is selected
    vmgZmgComboBox.addActionListener(new ActionListener()
      {
        public void actionPerformed(ActionEvent e)
        {
          if (vmgZmgComboBox.getSelectedIndex() == 1) // VMG on Waypoint
          {
            NMEADataCache cache = NMEAContext.getInstance().getCache();
            if (cache.get(NMEADataCache.TO_WP) == null || cache.get(NMEADataCache.TO_WP).toString().trim().length() == 0)
            {
              JOptionPane.showMessageDialog(vmgZmgComboBox, 
                                            "No waypoint currently set on your GPS (RMB).\nWill remain on VMG on Wind.", 
                                            "VMG on Waypoint (aka ZMG)", 
                                            JOptionPane.WARNING_MESSAGE);
              vmgZmgComboBox.setSelectedIndex(0);
            }
          }
        }
      });
    vmgZmgHolder.add(vmgZmgComboBox, BorderLayout.CENTER);
    displayPanel.add(vmgZmgHolder,
                     new GridBagConstraints(1, 3, 1, 1, 0.0, 0.0, GridBagConstraints.WEST, GridBagConstraints.NONE,
          new Insets(0, 3, 0, 0), 0, 0));
    displayPanel.add(displayCurrentCheckBox,
                     new GridBagConstraints(0, 4, 1, 1, 0.0, 0.0, GridBagConstraints.WEST, GridBagConstraints.NONE,
          new Insets(0, 3, 0, 0), 0, 0));
    displayPanel.add(perimeterTicksCheckBox,
                     new GridBagConstraints(1, 4, 1, 1, 0.0, 0.0, GridBagConstraints.WEST, GridBagConstraints.NONE,
          new Insets(0, 3, 0, 0), 0, 0));
    displayPanel.add(twsMethodPanel, new GridBagConstraints(0, 5, 2, 1, 0.0, 0.0, GridBagConstraints.WEST, GridBagConstraints.NONE,
          new Insets(0, 3, 0, 0), 0, 0));

    displayPanel.add(autoScaleCheckBox, new GridBagConstraints(1, 1, 1, 1, 0.0, 0.0, GridBagConstraints.WEST, GridBagConstraints.NONE,
          new Insets(0, 3, 0, 0), 0, 0));
    bspDisplay = new JumboDisplay("BSP", "00.00", "Boat Speed", basicJumboSize);
    hdgDisplay = new JumboDisplay("HDG", "000", "True Heading", basicJumboSize);
    awaDisplay = new JumboDisplay("AWA", "000", "Apparent Wind Angle", basicJumboSize);
    awsDisplay = new JumboDisplay("AWS", "00.00", "Apparent Wind Speed", basicJumboSize);
    sogDisplay = new JumboDisplay("SOG", "00.00", "Speed Over Ground", basicJumboSize);
    cogDisplay = new JumboDisplay("COG", "000", "Course Over Ground", basicJumboSize);

    twaDisplay = new JumboDisplay("TWA", "000", "True Wind Angle", basicJumboSize);
    twaDisplay.setDisplayColor(Color.cyan);
    twsDisplay = new JumboDisplay("TWS", "00.00", "True Wind Speed", basicJumboSize);
    twsDisplay.setDisplayColor(Color.cyan);
    twdDisplay = new JumboDisplay("WINDIR", "000", "True Wind Direction", basicJumboSize); // TWD
    twdDisplay.setDisplayColor(Color.cyan);

    vmgDisplay = new JumboDisplay("VMG", "+00.00", "Velocity Made Good", basicJumboSize);
    vmgDisplay.setDisplayColor(Color.cyan);
    vmgWithHDG = new JRadioButton("with HDG & BSP");
    vmgWithHDG.addActionListener(new ActionListener()
      {
        public void actionPerformed(ActionEvent e)
        {
          broadcastVMGOption();
        }
      });
    vmgWithCOG = new JRadioButton("with COG & SOG");
    vmgWithCOG.addActionListener(new ActionListener()
      {
        public void actionPerformed(ActionEvent e)
        {
          broadcastVMGOption();
        }
      });
    ButtonGroup vmgGroup = new ButtonGroup();
    vmgGroup.add(vmgWithHDG);
    vmgGroup.add(vmgWithCOG);
    vmgWithHDG.setSelected(true);
    vmgWithCOG.setSelected(false);
    vmgRBPanel = new JPanel(new GridBagLayout());
    vmgRBPanel.add(vmgWithHDG,
                    new GridBagConstraints(0, 0, 1, 1, 0.0, 0.0, GridBagConstraints.NORTHWEST, GridBagConstraints.NONE,
          new Insets(1, 1, 1, 1), 0, 0));
    vmgRBPanel.add(vmgWithCOG,
                    new GridBagConstraints(0, 1, 1, 1, 0.0, 0.0, GridBagConstraints.NORTHWEST, GridBagConstraints.NONE,
          new Insets(1, 1, 1, 1), 0, 0));
    
    JPanel buttonPanel = new JPanel(new GridBagLayout());
//  buttonPanel.setLayout(gridBagLayout3);
    buttonPanel.add(freezeButton,
                    new GridBagConstraints(0, 0, 1, 1, 0.0, 0.0, GridBagConstraints.NORTH, GridBagConstraints.NONE,
          new Insets(1, 1, 1, 1), 0, 0));
    buttonPanel.add(shiftLeftRightButton,
                    new GridBagConstraints(1, 0, 1, 1, 0.0, 0.0, GridBagConstraints.NORTH, GridBagConstraints.NONE,
          new Insets(1, 1, 1, 1), 0, 0));
    buttonPanel.add(vmgDisplay,
                    new GridBagConstraints(2, 0, 1, 2, 0.0, 0.0, GridBagConstraints.NORTH, GridBagConstraints.NONE,
          new Insets(1, 30, 1, 1), 0, 0));
    buttonPanel.add(vmgRBPanel,
                    new GridBagConstraints(3, 0, 1, 2, 0.0, 0.0, GridBagConstraints.NORTH, GridBagConstraints.NONE,
          new Insets(1, 1, 1, 1), 0, 0));

    buttonPanel.add(perfCheckBox,
                    new GridBagConstraints(0, 1, 2, 1, 0.0, 0.0, GridBagConstraints.CENTER, GridBagConstraints.NONE,
          new Insets(0, 0, 0, 0), 0, 0));
    displayPanel.add(buttonPanel,
                     new GridBagConstraints(0, 6, 2, 1, 0.0, 0.0,
                                            GridBagConstraints.WEST,
                                            GridBagConstraints.NONE,
                                            new Insets(0, 3, 0, 0), 0, 0));

    lwyDisplay =
        new JumboDisplay("LWY", "00.00", "Leeway", basicJumboSize);
    lwyDisplay.setDisplayColor(Color.red);

    cspDisplay =
        new JumboDisplay("CSP", "00.00", "Current Speed", basicJumboSize);
    cspDisplay.setDisplayColor(Color.cyan);
    cdrDisplay =
        new JumboDisplay("CDR", "000", "Current Direction", basicJumboSize);
    cdrDisplay.setDisplayColor(Color.cyan);

    awDisplay =
        new AWDisplay("Wind", "00.00", "Apparent Wind", basicJumboSize);
    currentDisplay =
        new CurrentDisplay("Current", "00.00", "Current", basicJumboSize);
    currentDisplay.setDisplayColor(Color.cyan);

    beaufortDisplay =
        new JumboDisplay("Beaufort", "F 0", "True Wind Speed",
                         basicJumboSize);
    beaufortDisplay.setDisplayColor(Color.cyan);

    hdgLabel.setText("HDG (t):");
    cogLabel.setText("COG:");

    showLeftPaneCheckBox.setText("Show 2D Pane");
    showLeftPaneCheckBox.setSelected(true);
    showLeftPaneCheckBox.addActionListener(new ActionListener()
      {
        public void actionPerformed(ActionEvent e)
        {
          showLeftPaneCheckBox_actionPerformed(e);
        }
      });

    miniMaxiCheckBox.setText("Show mini & maxi");
    miniMaxiCheckBox.setSelected(true);
    miniMaxiCheckBox.addActionListener(new ActionListener()
      {
        public void actionPerformed(ActionEvent e)
        {
          miniMaxiCheckBox_actionPerformed(e);
        }
      });

    beaufortCheckBox.setText("Show Beaufort Scale");
    beaufortCheckBox.setSelected(true);
    beaufortCheckBox.addActionListener(new ActionListener()
      {
        public void actionPerformed(ActionEvent e)
        {
          twsSpeedoPanel.withBeaufortScale(beaufortCheckBox.isSelected());
          twsSpeedoPanel.repaint();
        }
      });

    vmgCheckBox.setText("Show");
    vmgCheckBox.setSelected(true);
    vmgCheckBox.addActionListener(new ActionListener()
      {
        public void actionPerformed(ActionEvent e)
        {
          vmgDisplay.setVisible(vmgCheckBox.isSelected());
          vmgRBPanel.setVisible(vmgCheckBox.isSelected());
          broadcastVMGOption();
        }
      });

    analogDisplayCheckBox.setText("Show analog displays");
    analogDisplayCheckBox.setSelected(true);
    analogDisplayCheckBox.addActionListener(new ActionListener()
      {
        public void actionPerformed(ActionEvent e)
        {
          analogDisplayCheckBox_actionPerformed(e);
        }
      });


    perimeterTicksCheckBox.setText("Perimeter Ticks");
    perimeterTicksCheckBox.setSelected(true);
    perimeterTicksCheckBox.addActionListener(new ActionListener()
      {
        public void actionPerformed(ActionEvent e)
        {
          perimeterTicksCheckBox_actionPerformed(e);
        }
      });

    showTemperatureCheckBox.setText("Show Water Temp.");

    boolean dt =
      "true".equals(System.getProperty("display.temperature", "false"));
    showTemperatureCheckBox.setSelected(dt);
    drawingBoard.setShowTemperature(dt);
    showTemperatureCheckBox.addActionListener(new ActionListener()
      {
        public void actionPerformed(ActionEvent e)
        {
          drawingBoard.setShowTemperature(showTemperatureCheckBox.isSelected());
          System.setProperty("display.temperature",
                             showTemperatureCheckBox.isSelected()? "true":
                             "false");
        }
      });
    displayCurrentCheckBox.setText("Display Current");
    displayCurrentCheckBox.setSelected(true);
    displayCurrentCheckBox.addActionListener(new ActionListener()
      {
        public void actionPerformed(ActionEvent e)
        {
          displayCurrentCheckBox_actionPerformed(e);
        }
      });

    autoScaleCheckBox.addActionListener(new ActionListener()
      {
        public void actionPerformed(ActionEvent e)
        {
          NMEAContext.getInstance().setAutoScale(autoScaleCheckBox.isSelected());
          NMEAContext.getInstance().fireAutoScale(autoScaleCheckBox.isSelected());
        }
      });

    double maxBSP =
      Double.parseDouble(System.getProperty("max.analog.bsp", "10"));
    System.out.println("Setting MAX BSP to " + maxBSP);
    bspSpeedoPanel = new SpeedoPanel(maxBSP, 0.25, 1, false);
    bspSpeedoPanel.setPreferredSize(new Dimension(200, 120));
    bspSpeedoPanel.setLabel("STW");

    speedoPanel = new JPanel(new GridBagLayout());
    speedoPanelHolder = new JPanel(new BorderLayout());

    double maxTWS =
      Double.parseDouble(System.getProperty("max.analog.tws", "50"));
    System.out.println("Setting MAX BSP to " + maxTWS);
    twsSpeedoPanel = new SpeedoPanel(maxTWS, 1, false);
    twsSpeedoPanel.withBeaufortScale(true);

    twsSpeedoPanel.setPreferredSize(new Dimension(200, 120));
    twsSpeedoPanel.setLabel("TWS");

    twsMethodPanel.add(twLabel, null);
    twsMethodPanel.add(gpsMethod, null);
    twsMethodPanel.add(bspMethod, null);
    twsMethodGroup.add(gpsMethod);
    twsMethodGroup.add(bspMethod);
    gpsMethod.setSelected(true);
    bspMethod.setSelected(false);
    System.setProperty("use.gps.method", "true");
    gpsMethod.addActionListener(new ActionListener()
      {
        public void actionPerformed(ActionEvent e)
        {
          if (gpsMethod.isSelected())
            System.setProperty("use.gps.method", "true");
          else
            System.setProperty("use.gps.method", "false");
        }
      });
    bspMethod.addActionListener(new ActionListener()
      {
        public void actionPerformed(ActionEvent e)
        {
          if (bspMethod.isSelected())
            System.setProperty("use.gps.method", "false");
          else
            System.setProperty("use.gps.method", "true");
        }
      });

    freezeButton.setPreferredSize(new Dimension(24, 24));
    freezeButton.setBorderPainted(false);
    freezeButton.setIcon(pause);
    freezeButton.setToolTipText("<html>Freeze the display so<br>you can adjust coefficients</html>");
    freezeButton.addActionListener(new ActionListener()
      {
        public void actionPerformed(ActionEvent e)
        {
          freezeButton_actionPerformed(e);
        }
      });

    shiftLeftRightButton.setPreferredSize(new Dimension(24, 24));
    shiftLeftRightButton.setBorderPainted(false);
    shiftLeftRightButton.setIcon(left);
    shiftLeftRightButton.setToolTipText("Shift the all panel left");
    shiftLeftRightButton.addActionListener(new ActionListener()
      {
        public void actionPerformed(ActionEvent e)
        {
          if (jumbosOnTheRight) // Then shift left
          {
            instance.remove(rightScrollPane);
            instance.add(rightScrollPane, BorderLayout.WEST);
            shiftLeftRightButton.setIcon(right);
            shiftLeftRightButton.setToolTipText("Shift the all panel right");
            jumbosOnTheRight = false;
          }
          else // Then shift right
          {
            instance.remove(rightScrollPane);
            instance.add(rightScrollPane, BorderLayout.EAST);
            shiftLeftRightButton.setIcon(left);
            shiftLeftRightButton.setToolTipText("Shift the all panel left");
            jumbosOnTheRight = true;
          }
        }
      });

    resetMinMaxButton.setText("Reset Min & Max");
    resetMinMaxButton.setToolTipText("Reset Min & Max Speeds");
    resetMinMaxButton.addActionListener(new ActionListener()
      {
        public void actionPerformed(ActionEvent e)
        {
          bspSpeedoPanel.resetMinMax();
          twsSpeedoPanel.resetMinMax();
          bspMinMaxPanel.reset();
          twsMinMaxPanel.reset();
        }
      });
    withMinMaxJCheckBox.addActionListener(new ActionListener()
      {
        public void actionPerformed(ActionEvent e)
        {
          bspSpeedoPanel.setWithMinMax(withMinMaxJCheckBox.isSelected());
          twsSpeedoPanel.setWithMinMax(withMinMaxJCheckBox.isSelected());
          resetMinMaxButton.setEnabled(withMinMaxJCheckBox.isSelected());
        }
      });
    withMinMaxJCheckBox.setSelected(true);

    autoScaleCheckBox.setText("Wind Auto-Scale");
    perfCheckBox.setText("With Perf");
    perfCheckBox.setToolTipText("Show performance ratio in the 2D pane");
    topDisplayPanel.add(bspDisplay,
                        new GridBagConstraints(0, 0, 1, 1, 0.0, 0.0,
                                               GridBagConstraints.CENTER,
                                               GridBagConstraints.HORIZONTAL,
                                               new Insets(1, 1, 1, 1), 0,
                                               0));
    topDisplayPanel.add(hdgDisplay,
                        new GridBagConstraints(0, 1, 1, 1, 0.0, 0.0,
                                               GridBagConstraints.CENTER,
                                               GridBagConstraints.HORIZONTAL,
                                               new Insets(1, 1, 1, 1), 0,
                                               0));
    topDisplayPanel.add(awaDisplay,
                        new GridBagConstraints(0, 2, 1, 1, 0.0, 0.0,
                                               GridBagConstraints.CENTER,
                                               GridBagConstraints.HORIZONTAL,
                                               new Insets(1, 1, 1, 1), 0,
                                               0));
    topDisplayPanel.add(awsDisplay,
                        new GridBagConstraints(0, 3, 1, 1, 0.0, 0.0,
                                               GridBagConstraints.CENTER,
                                               GridBagConstraints.HORIZONTAL,
                                               new Insets(1, 1, 1, 1), 0,
                                               0));
    topDisplayPanel.add(cogDisplay,
                        new GridBagConstraints(0, 4, 1, 1, 0.0, 0.0,
                                               GridBagConstraints.CENTER,
                                               GridBagConstraints.HORIZONTAL,
                                               new Insets(1, 1, 1, 1), 0,
                                               0));
    topDisplayPanel.add(sogDisplay,
                        new GridBagConstraints(0, 5, 1, 1, 0.0, 0.0,
                                               GridBagConstraints.CENTER,
                                               GridBagConstraints.HORIZONTAL,
                                               new Insets(1, 1, 1, 1), 0,
                                               0));
    topDisplayPanel.add(awDisplay,
                        new GridBagConstraints(0, 6, 1, 1, 0.0, 0.0,
                                               GridBagConstraints.CENTER,
                                               GridBagConstraints.HORIZONTAL,
                                               new Insets(1, 1, 1, 1), 0,
                                               0));
    //    topDisplayPanel.add(vmgDisplay,
    //                        new GridBagConstraints(0, 7, 1, 1, 0.0, 0.0, GridBagConstraints.CENTER, GridBagConstraints.HORIZONTAL,
    //                                               new Insets(1, 1, 1, 1), 0, 0));

    topDisplayPanel.add(twdDisplay,
                        new GridBagConstraints(1, 0, 1, 1, 0.0, 0.0,
                                               GridBagConstraints.CENTER,
                                               GridBagConstraints.HORIZONTAL,
                                               new Insets(1, 1, 1, 1), 0,
                                               0));
    topDisplayPanel.add(lwyDisplay,
                        new GridBagConstraints(1, 1, 1, 1, 0.0, 0.0,
                                               GridBagConstraints.CENTER,
                                               GridBagConstraints.HORIZONTAL,
                                               new Insets(1, 1, 1, 1), 0,
                                               0));
    topDisplayPanel.add(twaDisplay,
                        new GridBagConstraints(1, 2, 1, 1, 0.0, 0.0,
                                               GridBagConstraints.CENTER,
                                               GridBagConstraints.HORIZONTAL,
                                               new Insets(1, 1, 1, 1), 0,
                                               0));
    topDisplayPanel.add(twsDisplay,
                        new GridBagConstraints(1, 3, 1, 1, 0.0, 0.0,
                                               GridBagConstraints.CENTER,
                                               GridBagConstraints.HORIZONTAL,
                                               new Insets(1, 1, 1, 1), 0,
                                               0));
    topDisplayPanel.add(cdrDisplay,
                        new GridBagConstraints(1, 4, 1, 1, 0.0, 0.0,
                                               GridBagConstraints.CENTER,
                                               GridBagConstraints.HORIZONTAL,
                                               new Insets(1, 1, 1, 1), 0,
                                               0));
    topDisplayPanel.add(cspDisplay,
                        new GridBagConstraints(1, 5, 1, 1, 0.0, 0.0,
                                               GridBagConstraints.CENTER,
                                               GridBagConstraints.HORIZONTAL,
                                               new Insets(1, 1, 1, 1), 0,
                                               0));
    topDisplayPanel.add(currentDisplay,
                        new GridBagConstraints(1, 6, 1, 1, 0.0, 0.0,
                                               GridBagConstraints.CENTER,
                                               GridBagConstraints.HORIZONTAL,
                                               new Insets(1, 1, 1, 1), 0,
                                               0));

    topDisplayPanel.add(bspMinMaxPanel,
                        new GridBagConstraints(2, 0, 1, 3, 0.0, 0.0,
                                               GridBagConstraints.CENTER,
                                               GridBagConstraints.HORIZONTAL,
                                               new Insets(1, 1, 1, 1), 0,
                                               0));
    topDisplayPanel.add(beaufortDisplay,
                        new GridBagConstraints(2, 3, 1, 1, 0.0, 0.0,
                                               GridBagConstraints.CENTER,
                                               GridBagConstraints.HORIZONTAL,
                                               new Insets(1, 1, 1, 1), 0,
                                               0));
    topDisplayPanel.add(twsMinMaxPanel,
                        new GridBagConstraints(2, 4, 1, 3, 0.0, 0.0,
                                               GridBagConstraints.CENTER,
                                               GridBagConstraints.VERTICAL,
                                               new Insets(1, 1, 1, 1), 0,
                                               0));
    // Gauges
    speedoPanelHolder.add(speedoPanel, BorderLayout.NORTH);
    speedoPanel.add(bspSpeedoPanel,
                    new GridBagConstraints(0, 0, 1, 1, 0.0, 0.0,
                                           GridBagConstraints.CENTER,
                                           GridBagConstraints.HORIZONTAL,
                                           new Insets(1, 1, 1, 1), 0, 0));
    speedoPanel.add(twsSpeedoPanel,
                    new GridBagConstraints(0, 1, 1, 1, 0.0, 0.0,
                                           GridBagConstraints.CENTER,
                                           GridBagConstraints.HORIZONTAL,
                                           new Insets(1, 1, 1, 1), 0, 0));
    speedoPanel.add(resetMinMaxButton,
                    new GridBagConstraints(0, 2, 1, 1, 0.0, 0.0,
                                           GridBagConstraints.CENTER,
                                           GridBagConstraints.NONE,
                                           new Insets(10, 1, 1, 1), 0, 0));
    speedoPanel.add(withMinMaxJCheckBox,
                    new GridBagConstraints(0, 3, 1, 1, 0.0, 0.0,
                                           GridBagConstraints.WEST,
                                           GridBagConstraints.NONE,
                                           new Insets(10, 3, 1, 1), 0, 0));
    speedoPanel.add(beaufortCheckBox,
                    new GridBagConstraints(0, 4, 1, 1, 0.0, 0.0,
                                           GridBagConstraints.WEST,
                                           GridBagConstraints.NONE,
                                           new Insets(10, 3, 1, 1), 0, 0));
    //    speedoPanel.add(vmgCheckBox, new GridBagConstraints(0, 5, 1, 1, 0.0, 0.0, GridBagConstraints.WEST, GridBagConstraints.NONE,
    //          new Insets(10, 3, 1, 1), 0, 0));
    //    speedoPanel.add(vmgDisplay, new GridBagConstraints(0, 6, 1, 1, 0.0, 0.0, GridBagConstraints.WEST, GridBagConstraints.NONE,
    //          new Insets(10, 3, 1, 1), 0, 0));

    topDisplayPanel.add(speedoPanelHolder,
                        new GridBagConstraints(3, 0, 1, 7, 0.0, 0.0,
                                               GridBagConstraints.NORTH,
                                               GridBagConstraints.HORIZONTAL,
                                               new Insets(1, 1, 1, 1), 0,
                                               0));

    // Init values
//  hdgPanel.setHdg(53);
//  cogPanel.setHdg(94);
  }

  private void broadcastVMGOption()
  {
    if (vmgCheckBox.isSelected())
    {
      if (vmgWithHDG.isSelected())
        drawingBoard.setVMGOption(DrawingBoard.VMG_WITH_BSP_HDG);
      else
        drawingBoard.setVMGOption(DrawingBoard.VMG_WITH_SOG_COG);
    }
    else
      drawingBoard.setVMGOption(DrawingBoard.NO_VMG);    
  }
  
  private void displayCurrentCheckBox_actionPerformed(ActionEvent e)
  {
    setDisplayCurrent(displayCurrentCheckBox.isSelected());
  }

  private void freezeButton_actionPerformed(ActionEvent e)
  {
    frozen = !frozen;
    if (frozen) // Take Snapshot
      NMEAContext.getInstance().setFrozenDataCache(NMEAContext.getInstance().cloneCache());
    else
      NMEAContext.getInstance().setFrozenDataCache(null);
    ImageIcon icon = (frozen?start:pause);
    freezeButton.setIcon(icon);
    freezeButton.setToolTipText(frozen?"Resume display":"<html>Freeze the display so<br>you can adjust coefficients</html>");

    freezeButton.repaint();
    setFreeze(frozen);
  }

  private void setBSP(double d)
  {
    if (d != -Double.MAX_VALUE)
    {
      if (bspDisplay.isVisible() || bspSpeedoPanel.isVisible())
      {
        bspDisplay.setValue(df22.format(d));
        bspSpeedoPanel.setSpeed(d);
      }
    }
  }

  private void setVMG(double d)
  {
    if (d != -Double.MAX_VALUE)
    {
      if (vmgDisplay.isVisible())
      {
        String txt = df22.format(d);
        if (!txt.startsWith("-"))
          txt = "+" + txt;
        vmgDisplay.setValue(txt);
      }
    }
  }

  private void setHDG(double d)
  {
    hdgDisplay.setValue(df3.format(d));
  }

  private void setAWA(double d)
  {
    String str = df3.format(d);
    if (d > 0)
      str += "-";
    awaDisplay.setValue(str);
    awDisplay.setAWA(d);
  }

  private void setAWS(double d)
  {
    if (d != -Double.MAX_VALUE)
    {
      awsDisplay.setValue(df22.format(d));
      awDisplay.setAWS(d);
    }
  }

  private void setTWA(double d)
  {
    String str = df3.format(d);
    if (d > 0)
      str += "-";
    twaDisplay.setValue(str);
  }

  private void setTWD(double d)
  {
    twdDisplay.setValue(df3.format(d));
  }

  private void setTWS(double d)
  {
    if (d != -Double.MAX_VALUE && !Double.isInfinite(d) && !Double.isNaN(d))
    {
      if (twsDisplay.isVisible() || twsSpeedoPanel.isVisible() || beaufortDisplay.isVisible())
      {
        twsDisplay.setValue(df22.format(d));
        beaufortDisplay.setValue("F " + df2.format(WindUtils.getBeaufort(d)));
        twsSpeedoPanel.setSpeed(d);
      }
    }
  }

  private void setCSP(double d)
  {
    if (d != -Double.MAX_VALUE && !Double.isInfinite(d) && !Double.isNaN(d))
    {
      cspDisplay.setValue(df22.format(d));
      currentDisplay.setSpeed(d);
    }
  }

  private void setCDR(double d)
  {
    if (d != -Double.MAX_VALUE && !Double.isInfinite(d) && !Double.isNaN(d))
    {
      cdrDisplay.setValue(df3.format(d));
      currentDisplay.setDirection(d);
    }
  }

  private void setLWY(double d)
  {
    lwyDisplay.setValue(df22.format(d));
  }

  private void setCOG(double d)
  {
    cogDisplay.setValue(df3.format(d));
  }

  private void setSOG(double d)
  {
    sogDisplay.setValue(df22.format(d));
  }
  
  public void setDisplayCurrent(boolean b)
  {
    try { drawingBoard.setDisplayCurrent(b); } catch (Exception ex) {}
  }

  public void setFreeze(boolean b)
  {
    try { drawingBoard.setFreeze(b); } catch (Exception ex) {}
    freezeButton.setToolTipText(b?"Resume":"Freeze");
  }

  private void miniMaxiCheckBox_actionPerformed(ActionEvent e)
  {
    bspMinMaxPanel.setVisible(miniMaxiCheckBox.isSelected());
    twsMinMaxPanel.setVisible(miniMaxiCheckBox.isSelected());
    beaufortDisplay.setVisible(miniMaxiCheckBox.isSelected());
  }
  
  private void analogDisplayCheckBox_actionPerformed(ActionEvent e)
  {
    bspSpeedoPanel.setVisible(analogDisplayCheckBox.isSelected());
    twsSpeedoPanel.setVisible(analogDisplayCheckBox.isSelected());
    resetMinMaxButton.setVisible(analogDisplayCheckBox.isSelected());    
    withMinMaxJCheckBox.setVisible(analogDisplayCheckBox.isSelected());
    beaufortCheckBox.setVisible(analogDisplayCheckBox.isSelected());
  }
    
  private void perimeterTicksCheckBox_actionPerformed(ActionEvent e)
  {
    drawingBoard.setShowPerimeterTicks(perimeterTicksCheckBox.isSelected());
    drawingBoard.repaint();
  }
  
  private void showLeftPaneCheckBox_actionPerformed(ActionEvent e)
  {
    drawingPlusCompass.setVisible(showLeftPaneCheckBox.isSelected());
  }

}
