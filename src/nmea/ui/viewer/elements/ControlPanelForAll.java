package nmea.ui.viewer.elements;

import chart.components.ui.ChartPanel;

import java.awt.Cursor;
import java.awt.Dimension;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Image;
import java.awt.Insets;
import java.awt.Point;
import java.awt.Toolkit;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

import java.awt.event.FocusAdapter;

import java.awt.event.FocusEvent;

import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.awt.event.MouseMotionAdapter;
import java.awt.event.MouseMotionListener;

import java.awt.event.MouseWheelEvent;
import java.awt.event.MouseWheelListener;

import java.io.File;

import java.text.DecimalFormat;

import javax.swing.JButton;
import javax.swing.JComboBox;
import javax.swing.JFileChooser;
import javax.swing.JFormattedTextField;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JProgressBar;
import javax.swing.JSlider;
import javax.swing.JSpinner;
import javax.swing.JTextField;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;

import nmea.event.NMEAReaderListener;

import nmea.server.constants.Constants;
import nmea.server.ctx.NMEAContext;
import nmea.server.ctx.NMEADataCache;

import nmea.ui.viewer.CurrentSituationPanel;

import ocss.nmea.parser.Angle180EW;

import polarmaker.polars.smooth.gui.components.ToolFileFilter;

import utils.PolarHelper;


public class ControlPanelForAll
  extends JPanel
{
  Cursor closedHandCursor = null;
  Cursor openHandCursor = null;
  Cursor previousCursor = null;
  private boolean onBorder = false;
  
  private GridBagLayout gridBagLayout1 = new GridBagLayout();
  private final static DecimalFormat DF22 = new DecimalFormat("00.00");
  private final static DecimalFormat DF24 = new DecimalFormat("00.0000");
  private final static DecimalFormat DF3  = new DecimalFormat("000");

  private CurrentSituationPanel parent = null;

  private JLabel bspCoeffLabel = new JLabel();
  private JFormattedTextField bspCoeffTextField = new JFormattedTextField(DF24);
  private JLabel hdgOffsetLabel = new JLabel();
  private JFormattedTextField hdgOffsetTextField = new JFormattedTextField(DF3);
  private JLabel awsCoeffLabel = new JLabel();
  private JFormattedTextField awsCoeffTextField = new JFormattedTextField(DF24);
  private JLabel awaOffsetLabel = new JLabel();
  private JFormattedTextField awaOffsetTextField = new JFormattedTextField(DF3);
  private JLabel maxLeewayLabel = new JLabel();
  private JFormattedTextField maxLeewayTextField = new JFormattedTextField();
  private JLabel windScaleLabel = new JLabel();
  private JComboBox scaleComboBox = new JComboBox();
  
  private JLabel polarFileLabel = new JLabel();
  private JTextField polarFileTextField = new JTextField();
  private JButton findPolarFilelButton = new JButton();
  private JLabel polarFactorLabel = new JLabel();
  private JFormattedTextField polarFactorTextField = new JFormattedTextField(DF24);  
  
//private boolean frozen = false;
  
  private JLabel defaultDeclinationLabel = new JLabel();
  private JFormattedTextField defaultDeclinationFormattedTextField = new JFormattedTextField(DF22);
  private JLabel dampingLabel = new JLabel();
  private JSpinner dampingSpinner = new JSpinner();
  private JSlider replaySpeedSlider = new JSlider();
  private JProgressBar fileProgress = new JProgressBar();

  public ControlPanelForAll(CurrentSituationPanel cp)
  {
    try
    {
      this.parent = cp;
      jbInit();
    }
    catch (Exception e)
    {
      e.printStackTrace();
    }
  }

  private void jbInit()
    throws Exception
  {
    String imageFileName = "resources/closed.hand.png";
    Image image = Toolkit.getDefaultToolkit().getImage(this.getClass().getResource(imageFileName));
    closedHandCursor = Toolkit.getDefaultToolkit().createCustomCursor(image , new Point(15,15), imageFileName);
    imageFileName = "resources/open.hand.png";
    image = Toolkit.getDefaultToolkit().getImage(this.getClass().getResource(imageFileName));
    openHandCursor = Toolkit.getDefaultToolkit().createCustomCursor(image , new Point(15,15), imageFileName);

    this.setLayout(gridBagLayout1);

    bspCoeffLabel.setText("BSP Coeff:");
    bspCoeffTextField.setPreferredSize(new Dimension(40, 20));
    bspCoeffTextField.setHorizontalAlignment(JTextField.TRAILING);
    double bspCoeff = 1d;
    try { bspCoeff = ((Double) NMEAContext.getInstance().getDataCache(NMEADataCache.BSP_FACTOR)).doubleValue(); } catch (Exception ex) {}
    bspCoeffTextField.setText(bspCoeffTextField.getFormatter().valueToString(bspCoeff));
    bspCoeffTextField.setMinimumSize(new Dimension(35, 20));
    bspCoeffTextField.setSize(new Dimension(40, 20));
    bspCoeffTextField.addActionListener(new ActionListener()
      {
        public void actionPerformed(ActionEvent e)
        {
          bspCoeffTextField_actionPerformed(e);
        }
      });
    bspCoeffTextField.addFocusListener(new FocusAdapter()
      {
        public void focusLost(FocusEvent e)
        {
          bspCoeffTextField_focusLost(e);
        }
      });
    hdgOffsetLabel.setText("HDG Offset:");
    hdgOffsetTextField.setPreferredSize(new Dimension(40, 20));
    hdgOffsetTextField.setHorizontalAlignment(JTextField.TRAILING);
    hdgOffsetTextField.setToolTipText("in degrees");
    double hdgOffset = 0d;
    try { hdgOffset = ((Double) NMEAContext.getInstance().getDataCache(NMEADataCache.HDG_OFFSET)).doubleValue(); } catch (Exception ex) {}
    hdgOffsetTextField.setText(DF3.format(hdgOffset));
    hdgOffsetTextField.setMinimumSize(new Dimension(35, 20));
    hdgOffsetTextField.setSize(new Dimension(40, 20));
    hdgOffsetTextField.addActionListener(new ActionListener()
      {
        public void actionPerformed(ActionEvent e)
        {
          hdgOffsetTextField_actionPerformed(e);
        }
      });
    hdgOffsetTextField.addFocusListener(new FocusAdapter()
      {
        public void focusLost(FocusEvent e)
        {
          hdgOffsetTextField_focusLost(e);
        }
      });
    awsCoeffLabel.setText("AWS Coeff:");
    awsCoeffTextField.setPreferredSize(new Dimension(40, 20));
    double awsCoeff = 1d;
    try { awsCoeff = ((Double) NMEAContext.getInstance().getDataCache(NMEADataCache.AWS_FACTOR)).doubleValue(); } catch (Exception ex) {}
    awsCoeffTextField.setText(awsCoeffTextField.getFormatter().valueToString(awsCoeff));
    awsCoeffTextField.setHorizontalAlignment(JTextField.RIGHT);
    awsCoeffTextField.setMinimumSize(new Dimension(35, 20));
    awsCoeffTextField.setSize(new Dimension(40, 20));
    awsCoeffTextField.addActionListener(new ActionListener()
      {
        public void actionPerformed(ActionEvent e)
        {
          awsCoeffTextField_actionPerformed(e);
        }
      });
    awsCoeffTextField.addFocusListener(new FocusAdapter()
      {
        public void focusLost(FocusEvent e)
        {
          awsCoeffTextField_focusLost(e);
        }
      });
    awaOffsetLabel.setText("AWA Offset:");
    awaOffsetTextField.setPreferredSize(new Dimension(40, 20));
    double awaOffset = 0d;
    try { awaOffset = ((Double) NMEAContext.getInstance().getDataCache(NMEADataCache.AWA_OFFSET)).doubleValue(); } catch (Exception ex) {}
    awaOffsetTextField.setText(DF3.format(awaOffset));
    awaOffsetTextField.setHorizontalAlignment(JTextField.RIGHT);
    awaOffsetTextField.setToolTipText("in degrees");
    awaOffsetTextField.setMinimumSize(new Dimension(35, 20));
    awaOffsetTextField.setSize(new Dimension(40, 20));
    awaOffsetTextField.addActionListener(new ActionListener()
      {
        public void actionPerformed(ActionEvent e)
        {
          awaOffsetTextField_actionPerformed(e);
        }
      });
    awaOffsetTextField.addFocusListener(new FocusAdapter()
      {
        public void focusLost(FocusEvent e)
        {
          awaOffsetTextField_focusLost(e);
        }
      });
    maxLeewayLabel.setText("Max Leeway:");
    maxLeewayTextField.setPreferredSize(new Dimension(40, 20));
    double mlw = 0d;
    try { mlw = ((Double) NMEAContext.getInstance().getDataCache(NMEADataCache.MAX_LEEWAY)).doubleValue(); } catch (Exception ex) {}
    maxLeewayTextField.setText(DF22.format(mlw));
    maxLeewayTextField.setHorizontalAlignment(JTextField.RIGHT);
    maxLeewayTextField.setToolTipText("in degrees");
    maxLeewayTextField.setMinimumSize(new Dimension(35, 20));
    maxLeewayTextField.setSize(new Dimension(40, 20));
    maxLeewayTextField.addActionListener(new ActionListener()
      {
        public void actionPerformed(ActionEvent e)
        {
          maxLeewayTextField_actionPerformed(e);
        }
      });
    maxLeewayTextField.addFocusListener(new FocusAdapter()
      {
        public void focusLost(FocusEvent e)
        {
          maxLeewayTextField_focusLost(e);
        }
      });
    windScaleLabel.setText("Speed Scale:");
    scaleComboBox.removeAllItems();
    for (NMEAContext.WindScale ws : NMEAContext.WindScale.values())
      scaleComboBox.addItem(new ScaleForWind( ws.scale(), ws.label()));
    
    /*
    scaleComboBox.addItem(new ScaleForWind( 1.5f, "05 knots"));
    scaleComboBox.addItem(new ScaleForWind( 3,    "10 knots"));
    scaleComboBox.addItem(new ScaleForWind( 4.5f, "15 knots"));
    scaleComboBox.addItem(new ScaleForWind( 6,    "20 knots"));
    scaleComboBox.addItem(new ScaleForWind( 7.5f, "25 knots"));
    scaleComboBox.addItem(new ScaleForWind( 9,    "30 knots"));
    scaleComboBox.addItem(new ScaleForWind(12,    "40 knots"));
    scaleComboBox.addItem(new ScaleForWind(15,    "50 knots"));
    scaleComboBox.addItem(new ScaleForWind(18,    "60 knots"));
    */

    float f = Float.parseFloat(System.getProperty("wind.scale", "-1"));
    for (int i=0; i<scaleComboBox.getItemCount(); i++)
    {
      ScaleForWind sfw = (ScaleForWind)scaleComboBox.getItemAt(i);
      if (sfw.getScale() == f)
      {
        scaleComboBox.setSelectedIndex(i);
        NMEAContext.getInstance().fireWindScale(f);
        System.setProperty("wind.scale", Float.toString(f));
        break;
      }
    }
    scaleComboBox.addActionListener(new ActionListener()
      {
        public void actionPerformed(ActionEvent e)
        {
          if (!NMEAContext.getInstance().isAutoScale())
            scaleComboBox_actionPerformed(e);
        }
      });

    defaultDeclinationLabel.setText("Default Declination:");
    defaultDeclinationFormattedTextField.setHorizontalAlignment(JTextField.RIGHT);
    defaultDeclinationFormattedTextField.addActionListener(new ActionListener()
      {
        public void actionPerformed(ActionEvent e)
        {
          defaultDeclinationFormattedTextField.repaint();
          defaultDeclination_Changed(e);
        }
      });
    double dd = 0d;
    try { dd = ((Angle180EW) NMEAContext.getInstance().getDataCache(NMEADataCache.DEFAULT_DECLINATION)).getValue(); } catch (Exception ex) {}    
    defaultDeclinationFormattedTextField.setText(DF22.format(dd));

    defaultDeclinationFormattedTextField.setMinimumSize(new Dimension(35, 20));
    defaultDeclinationFormattedTextField.setPreferredSize(new Dimension(40, 20));
    defaultDeclinationFormattedTextField.setSize(new Dimension(40, 20));
    defaultDeclinationFormattedTextField.addFocusListener(new FocusAdapter()
      {
        public void focusLost(FocusEvent e)
        {
          defaultDeclinationFormattedTextField_focusLost(e);
        }
      });
    dampingLabel.setText("Damping:");    
    int dv = 1;
    try { dv = ((Integer) NMEAContext.getInstance().getDataCache(NMEADataCache.DAMPING)).intValue(); } catch (Exception ex) {}    
    dampingSpinner.setValue(new Integer(dv));
    dampingSpinner.setMinimumSize(new Dimension(35, 20));
    dampingSpinner.setPreferredSize(new Dimension(40, 20));
    dampingSpinner.setSize(new Dimension(40, 20));
    dampingSpinner.setToolTipText("<html>In number of points<br>(use the mouse wheel to change value)<html>");
    NMEAContext.getInstance().getCache().setDampingSize(dv);
    dampingSpinner.addChangeListener(new ChangeListener()
    {
      public void stateChanged(ChangeEvent evt) 
      {
        JSpinner spinner = (JSpinner)evt.getSource();
        // Get the new value
        Object value = spinner.getValue();
        System.out.println("DampingValue changed: Value is a " + value.getClass().getName());
        if (value instanceof Integer)
        {
          Integer i = (Integer)value;
          int val = i.intValue();
          if (val < 1)
            val = 1;
          NMEAContext.getInstance().getCache().setDampingSize(val);
          NMEAContext.getInstance().getCache().put(NMEADataCache.DAMPING, val);
          NMEAContext.getInstance().fireDampingHasChanged(val);
          // TODO Resmooth whatever is displayed          
        }
      }
    });
    dampingSpinner.addMouseWheelListener(new MouseWheelListener()
      {
        public void mouseWheelMoved(MouseWheelEvent e)
        {
          int notches = e.getWheelRotation();
          Integer ds = (Integer)dampingSpinner.getValue();
          dampingSpinner.setValue(new Integer(ds.intValue() + (notches * -1)));
        }
      });

    polarFactorLabel.setText("Polar factor:");
    polarFactorTextField.setPreferredSize(new Dimension(40, 20));
    polarFactorTextField.setHorizontalAlignment(JTextField.TRAILING);
    double polarCoeff = 1d;
    try { polarCoeff = ((Double) NMEAContext.getInstance().getDataCache(NMEADataCache.POLAR_FACTOR)).doubleValue(); } catch (Exception ex) {}
    polarFactorTextField.setText(polarFactorTextField.getFormatter().valueToString(polarCoeff));
    polarFactorTextField.setMinimumSize(new Dimension(35, 20));
    polarFactorTextField.setSize(new Dimension(40, 20));
    polarFactorTextField.addActionListener(new ActionListener()
      {
        public void actionPerformed(ActionEvent e)
        {
          polarFactorTextField_actionPerformed(e);
        }
      });
    polarFactorTextField.addFocusListener(new FocusAdapter()
      {
        public void focusLost(FocusEvent e)
        {
          polarFactorTextField_focusLost(e);
        }
      });

    polarFileLabel.setText("Polar file:");
    polarFileTextField.setPreferredSize(new Dimension(100, 20));
    polarFileTextField.setHorizontalAlignment(JTextField.TRAILING);
    String polarFile = "";
    try { polarFile = NMEAContext.getInstance().getDataCache(NMEADataCache.POLAR_FILE_NAME).toString(); } catch (Exception ex) {}
    polarFileTextField.setText(polarFile);
    polarFileTextField.setMinimumSize(new Dimension(35, 20));
    polarFileTextField.setSize(new Dimension(40, 20));
    polarFileTextField.addActionListener(new ActionListener()
      {
        public void actionPerformed(ActionEvent e)
        {
          polarFileTextField_actionPerformed(e);
        }
      });
    polarFileTextField.addFocusListener(new FocusAdapter()
      {
        public void focusLost(FocusEvent e)
        {
          polarFileTextField_focusLost(e);
        }
      });
    findPolarFilelButton.setText("...");
    findPolarFilelButton.setPreferredSize(new Dimension(30, 20));
    findPolarFilelButton.addActionListener(new ActionListener()
      {
        public void actionPerformed(ActionEvent e)
        {
          String fName = chooseFile(JFileChooser.FILES_AND_DIRECTORIES, "polar-coeff", "Polars", "Choose the Polar file", "Choose");
          if (fName.trim().length() > 0)
          {
            polarFileTextField.setText(fName);
            polarFileChanged();
          }
        }
      });
    
    this.add(bspCoeffLabel, new GridBagConstraints(0, 0, 1, 1, 0.0, 0.0, GridBagConstraints.EAST, GridBagConstraints.NONE, new Insets(0, 0, 0, 0), 0, 0));
    this.add(bspCoeffTextField, new GridBagConstraints(1, 0, 1, 1, 1.0, 0.0, GridBagConstraints.WEST, GridBagConstraints.HORIZONTAL, new Insets(0, 0, 0, 0), 0, 0));
    this.add(hdgOffsetLabel, new GridBagConstraints(2, 0, 1, 1, 0.0, 0.0, GridBagConstraints.EAST, GridBagConstraints.NONE, new Insets(0, 5, 0, 0), 0, 0));
    this.add(hdgOffsetTextField, new GridBagConstraints(3, 0, 1, 1, 1.0, 0.0, GridBagConstraints.WEST, GridBagConstraints.HORIZONTAL, new Insets(0, 0, 0, 0), 0, 0));
    this.add(awsCoeffLabel, new GridBagConstraints(4, 0, 1, 1, 0.0, 0.0, GridBagConstraints.EAST, GridBagConstraints.NONE, new Insets(0, 0, 0, 0), 0, 0));
    this.add(awsCoeffTextField, new GridBagConstraints(5, 0, 1, 1, 1.0, 0.0, GridBagConstraints.WEST, GridBagConstraints.HORIZONTAL, new Insets(0, 0, 0, 0), 0, 0));
    this.add(awaOffsetLabel, new GridBagConstraints(6, 0, 1, 1, 0.0, 0.0, GridBagConstraints.EAST, GridBagConstraints.NONE, new Insets(0, 5, 0, 0), 0, 0));
    this.add(awaOffsetTextField, new GridBagConstraints(7, 0, 1, 1, 1.0, 0.0, GridBagConstraints.WEST, GridBagConstraints.HORIZONTAL, new Insets(0, 0, 0, 0), 0, 0));
    this.add(polarFactorLabel, new GridBagConstraints(8, 0, 1, 1, 0.0, 0.0, GridBagConstraints.EAST, GridBagConstraints.NONE, new Insets(0, 5, 0, 0), 0, 0));
    this.add(polarFactorTextField,
             new GridBagConstraints(9, 0, 2, 1, 1.0, 0.0, GridBagConstraints.WEST, GridBagConstraints.HORIZONTAL,
          new Insets(0, 0, 0, 0), 0, 0));
        
    this.add(maxLeewayLabel, new GridBagConstraints(0, 1, 1, 1, 0.0, 0.0, GridBagConstraints.EAST, GridBagConstraints.NONE, new Insets(5, 5, 0, 0), 0, 0));
    this.add(maxLeewayTextField, new GridBagConstraints(1, 1, 1, 1, 1.0, 0.0, GridBagConstraints.WEST, GridBagConstraints.HORIZONTAL, new Insets(5, 0, 0, 0), 0, 0));
    this.add(windScaleLabel, new GridBagConstraints(2, 1, 1, 1, 0.0, 0.0, GridBagConstraints.EAST, GridBagConstraints.NONE, new Insets(5, 0, 0, 0), 0, 0));
    this.add(scaleComboBox, new GridBagConstraints(3, 1, 1, 1, 0.0, 0.0, GridBagConstraints.CENTER, GridBagConstraints.HORIZONTAL, new Insets(5, 0, 0, 0), 0, 0));

    this.add(defaultDeclinationLabel, new GridBagConstraints(4, 1, 1, 1, 0.0, 0.0, GridBagConstraints.EAST, GridBagConstraints.NONE, new Insets(5, 3, 0, 0), 0, 0));
    this.add(defaultDeclinationFormattedTextField, new GridBagConstraints(5, 1, 1, 1, 0.0, 0.0, GridBagConstraints.CENTER, GridBagConstraints.HORIZONTAL, new Insets(3, 0, 0, 0), 0, 0));
    this.add(dampingLabel, new GridBagConstraints(6, 1, 1, 1, 0.0, 0.0, GridBagConstraints.EAST, GridBagConstraints.NONE, new Insets(5, 0, 0, 0), 0, 0));
    this.add(dampingSpinner, new GridBagConstraints(7, 1, 1, 1, 0.0, 0.0, GridBagConstraints.CENTER, GridBagConstraints.HORIZONTAL, new Insets(5, 0, 0, 0), 0, 0));
    this.add(polarFileLabel, new GridBagConstraints(8, 1, 1, 1, 0.0, 0.0, GridBagConstraints.EAST, GridBagConstraints.NONE, new Insets(5, 0, 0, 0), 0, 0));
    this.add(polarFileTextField,
             new GridBagConstraints(9, 1, 1, 1, 1.0, 0.0, GridBagConstraints.CENTER, GridBagConstraints.HORIZONTAL,
          new Insets(5, 0, 0, 0), 0, 0));
    this.add(findPolarFilelButton,
             new GridBagConstraints(10, 1, 1, 1, 0.0, 0.0, GridBagConstraints.EAST, GridBagConstraints.NONE,
          new Insets(5, 0, 0, 0), 0, 0));

    this.add(replaySpeedSlider, new GridBagConstraints(0, 3, 11, 1, 0.0, 0.0, GridBagConstraints.CENTER, GridBagConstraints.HORIZONTAL, new Insets(5, 0, 0, 0), 0, 0));
    replaySpeedSlider.addChangeListener(new ChangeListener()
      {
        public void stateChanged(ChangeEvent evt)
        {
          JSlider slider = (JSlider) evt.getSource();

          //    if (!slider.getValueIsAdjusting())
          {
            // Get new value
            int value = slider.getValue();
            NMEAContext.getInstance().fireReplaySpeedChanged(value);
          }
        }
      });
    replaySpeedSlider.setToolTipText("Replay Speed");
    replaySpeedSlider.setVisible(NMEAContext.getInstance().isFromFile());    
    
    this.add(fileProgress, new GridBagConstraints(0, 4, 11, 1, 0.0, 0.0, GridBagConstraints.CENTER, GridBagConstraints.HORIZONTAL,
          new Insets(5, 0, 0, 0), 0, 0));
    fileProgress.addMouseListener(new MouseAdapter()
      {
        private int draggedFrom = -1;
        
        @Override
        public void mousePressed(MouseEvent e)
        {
//        super.mousePressed(e);
          if (onBorder)
          {
            fileProgress.setCursor(closedHandCursor);
            draggedFrom = e.getX();
          }
        }

        @Override
        public void mouseReleased(MouseEvent e)
        {
//        super.mouseReleased(e);
          fileProgress.setCursor(previousCursor);
          if (draggedFrom != -1)
          {
            mouseOperationOnProgressBar(e);
          }
          draggedFrom = -1;
        }

        @Override
        public void mouseEntered(MouseEvent e)
        {
//        super.mouseEntered(e);
          // Store Original Cursor
          previousCursor = fileProgress.getCursor();
        }

        @Override
        public void mouseExited(MouseEvent e)
        {
          super.mouseExited(e);
          // Return Original Cursor
      //  fileProgress.setCursor(previousCursor);
        }
      });
    fileProgress.addMouseMotionListener(new MouseMotionAdapter()
      {

        @Override
        public void mouseDragged(MouseEvent e)
        {
//        System.out.println("Dragged");
          mouseOperationOnProgressBar(e);
        }

        @Override
        public void mouseMoved(MouseEvent e)
        {
//        super.mouseMoved(e);
          int x = e.getX();
//        System.out.println("X:" + x);
          int width = fileProgress.getSize().width;
          int value = fileProgress.getValue();
          int offset = (int)Math.round((double)width * ((double)value / 1000D));
          onBorder = (Math.abs(offset - x) < 3); // At 3 pixels or less
          if (onBorder)
            fileProgress.setCursor(openHandCursor);
          else
            fileProgress.setCursor(previousCursor);
        }
      });
    
    fileProgress.setVisible(NMEAContext.getInstance().isFromFile());
    if (NMEAContext.getInstance().isFromFile())
    {
      fileProgress.setMaximum(1000);
      fileProgress.setMinimum(0);
      fileProgress.setStringPainted(true);
      fileProgress.setString("0%");
      fileProgress.setValue(0);
    }
    
    NMEAContext.getInstance().addNMEAReaderListener(new NMEAReaderListener(Constants.NMEA_SERVER_LISTENER_GROUP_ID, "Control Panel")
      {
        @Override
        public void setWindScale(float f) 
        {
          for (int i=0; NMEAContext.getInstance().isAutoScale() && i<scaleComboBox.getItemCount(); i++)
          {
            ScaleForWind sfw = (ScaleForWind)scaleComboBox.getItemAt(i);
            if (sfw.getScale() == f)
            {
              scaleComboBox.setSelectedIndex(i);
              System.setProperty("wind.scale", Float.toString(f));
              break;
            }
          }
        }
        
        @Override
        public void setAutoScale(boolean b)
        {
          scaleComboBox.setEnabled(!b);
          windScaleLabel.setEnabled(!b);
        }
        
        @Override
        public void dataUpdate() 
        {
          if (NMEAContext.getInstance().isFromFile())
          {
            long fileSize = NMEAContext.getInstance().getReplayFileSize();
            if (fileSize > 0)
            {
              long filePos = NMEAContext.getInstance().getReplayFileRecNum();
              double pos = 1000D * (double)filePos / (double)fileSize;
              fileProgress.setValue((int)Math.round(pos));
              fileProgress.setString(Long.toString(filePos) + "/" + Long.toString(fileSize));
            }
          }
        }

      });
  }

  private void mouseOperationOnProgressBar(MouseEvent e)
  {
    int width = fileProgress.getSize().width;
    long fileSize = NMEAContext.getInstance().getReplayFileSize();
    if (fileSize > 0)
    {
      int recOffset = (int)Math.round(fileSize * (double)(e.getX()) / (double)width); 
      long filePos = recOffset; // NMEAContext.getInstance().getReplayFileRecNum();
//    System.out.println("Record:" + recOffset);
      NMEAContext.getInstance().fireJumpToOffset(filePos);
      double pos = 1000D * (double)filePos / (double)fileSize;
      fileProgress.setValue((int)Math.round(pos));
      fileProgress.setString(Long.toString(filePos) + "/" + Long.toString(fileSize));
    }
  }
  
  private void bspCoeffTextField_actionPerformed(ActionEvent e)
  {
    bspCoefChanged();
  }  
  private void bspCoeffTextField_focusLost(FocusEvent e)
  {
    bspCoefChanged();
  }
  private void bspCoefChanged()
  {
    System.out.println("BSP Coeff changed?");
    double d = Double.parseDouble(bspCoeffTextField.getText());
    NMEAContext.getInstance().putDataCache(NMEADataCache.BSP_FACTOR, d);
    if (NMEAContext.getInstance().getFrozenDataCache() != null)
      NMEAContext.getInstance().getFrozenDataCache().put(NMEADataCache.BSP_FACTOR, d);
    NMEAContext.getInstance().fireDataChanged();
    //  repaint();
  }

  private void hdgOffsetTextField_actionPerformed(ActionEvent e)
  {
    hdgOffsetChanged();
  }
  private void hdgOffsetTextField_focusLost(FocusEvent e)
  {
    hdgOffsetChanged();
  }
  private void hdgOffsetChanged()
  {
    System.out.println("HDG Offset changed?");
    double d = Double.parseDouble(hdgOffsetTextField.getText());
    NMEAContext.getInstance().putDataCache(NMEADataCache.HDG_OFFSET, d);
    if (NMEAContext.getInstance().getFrozenDataCache() != null)
      NMEAContext.getInstance().getFrozenDataCache().put(NMEADataCache.HDG_OFFSET, d);
    NMEAContext.getInstance().fireDataChanged();
    //  repaint();
  }

  private void awsCoeffTextField_actionPerformed(ActionEvent e)
  {
    awsCoeffChanged();
  }
  private void awsCoeffTextField_focusLost(FocusEvent e)
  {
    awsCoeffChanged();
  }
  private void awsCoeffChanged()
  {
    System.out.println("AWS Coeff changed?");
    double d = Double.parseDouble(awsCoeffTextField.getText());
    NMEAContext.getInstance().putDataCache(NMEADataCache.AWS_FACTOR, d);
    if (NMEAContext.getInstance().getFrozenDataCache() != null)
      NMEAContext.getInstance().getFrozenDataCache().put(NMEADataCache.AWS_FACTOR, d);
    NMEAContext.getInstance().fireDataChanged();
    //  repaint();
  }

  private void awaOffsetTextField_actionPerformed(ActionEvent e)
  {
    awaOffsetChanged();
  }
  private void awaOffsetTextField_focusLost(FocusEvent e)
  {
    awaOffsetChanged();
  }
  private void awaOffsetChanged()
  {
    System.out.println("AWA Offset changed?");
    double d = Double.parseDouble(awaOffsetTextField.getText());
    NMEAContext.getInstance().putDataCache(NMEADataCache.AWA_OFFSET, d);
    if (NMEAContext.getInstance().getFrozenDataCache() != null)
      NMEAContext.getInstance().getFrozenDataCache().put(NMEADataCache.AWA_OFFSET, d);
    NMEAContext.getInstance().fireDataChanged();
    //  repaint();
  }

  private void maxLeewayTextField_actionPerformed(ActionEvent e)
  {
    maxLeewayChanged();
  }
  private void maxLeewayTextField_focusLost(FocusEvent e)
  {
    maxLeewayChanged();
  }
  private void maxLeewayChanged()
  {
    System.out.println("Max Leeway changed");
    double d = Double.parseDouble(maxLeewayTextField.getText());
    NMEAContext.getInstance().putDataCache(NMEADataCache.MAX_LEEWAY, d);
    if (NMEAContext.getInstance().getFrozenDataCache() != null)
      NMEAContext.getInstance().getFrozenDataCache().put(NMEADataCache.MAX_LEEWAY, d);
    NMEAContext.getInstance().fireDataChanged();
    //  repaint();
  }

  private void defaultDeclination_Changed(ActionEvent e)
  {
    ddChanged();
  }
  private void defaultDeclinationFormattedTextField_focusLost(FocusEvent e)
  {
    ddChanged();
  }
  private void ddChanged()
  {
    System.out.println("Default Declination changed");
    double d = Double.parseDouble(defaultDeclinationFormattedTextField.getText());
    NMEAContext.getInstance().putDataCache(NMEADataCache.DEFAULT_DECLINATION, new Angle180EW(d));
    if (NMEAContext.getInstance().getFrozenDataCache() != null)
      NMEAContext.getInstance().getFrozenDataCache().put(NMEADataCache.DEFAULT_DECLINATION, new Angle180EW(d));
    NMEAContext.getInstance().fireDataChanged();
//  repaint();
  }

  private void scaleComboBox_actionPerformed(ActionEvent e)
  {
    if (scaleComboBox.isEnabled()) // ie not in auto-scale mode
    {
      float f = ((ScaleForWind)scaleComboBox.getSelectedItem()).getScale();
  //  parent.setWindScale(f);
      NMEAContext.getInstance().fireWindScale(f);
      System.setProperty("wind.scale", Float.toString(f));
    }
  }

  private void polarFactorTextField_actionPerformed(ActionEvent e)
  {
    polarFactorChanged();
  }
  private void polarFactorTextField_focusLost(FocusEvent e)
  {
    polarFactorChanged();
  }
  private void polarFactorChanged()
  {
    System.out.println("Polar factor changed?");
    double d = Double.parseDouble(polarFactorTextField.getText());
    NMEAContext.getInstance().getCache().put(NMEADataCache.POLAR_FACTOR, new Double(d));      
    PolarHelper.setPolarCoeff(d);
  }

  private void polarFileTextField_actionPerformed(ActionEvent e)
  {
    polarFileChanged();
  }
  private void polarFileTextField_focusLost(FocusEvent e)
  {
    polarFileChanged();
  }
  
  private void polarFileChanged()
  {
    System.out.println("Polar file changed?");
    String fn = polarFileTextField.getText();
    NMEAContext.getInstance().getCache().put(NMEADataCache.POLAR_FILE_NAME, fn);
    PolarHelper.setFileName(fn);
    PolarHelper.refreshCoeffs();
  }

  public static String chooseFile(int mode,
                                  String flt,
                                  String desc,
                                  String title,
                                  String buttonLabel)
  {
    String fileName = "";
    JFileChooser chooser = new JFileChooser();
    if (title != null)
      chooser.setDialogTitle(title);
    if (buttonLabel != null)
      chooser.setApproveButtonText(buttonLabel);    

    ToolFileFilter filter = new ToolFileFilter(flt,
                                               desc);                                               
    chooser.addChoosableFileFilter(filter);
    chooser.setFileFilter(filter);

    chooser.setFileSelectionMode(mode);
    // Set current directory
    File f = new File(".");
    String currPath = f.getAbsolutePath();
    f = new File(currPath.substring(0, currPath.lastIndexOf(File.separator)));
    chooser.setCurrentDirectory(f);

    int retval = chooser.showOpenDialog(null);
    switch (retval)
    {
      case JFileChooser.APPROVE_OPTION:
        fileName = chooser.getSelectedFile().toString();
        break;
      case JFileChooser.CANCEL_OPTION:
        break;
      case JFileChooser.ERROR_OPTION:
        break;
    }
    return fileName;
  }  

  class ScaleForWind
  {
    private float scale;
    private String label;
    public ScaleForWind(float scale, String label)
    {
      this.scale = scale;
      this.label = label;
    }
    
    public float getScale() { return scale; }
    public String toString() { return label; }
  }
}
