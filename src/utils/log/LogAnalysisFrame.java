package utils.log;

import java.awt.BorderLayout;
import java.awt.Dimension;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;

import java.text.DecimalFormat;

import java.text.NumberFormat;

import java.util.Calendar;
import java.util.Date;
import java.util.Map;

import javax.swing.JButton;
import javax.swing.JCheckBox;
import javax.swing.JFormattedTextField;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JScrollPane;

import javax.swing.JTextField;

import utils.NMEAAnalyzer.ScalarValue;

public class LogAnalysisFrame
  extends JFrame
{
  private LogAnalysisPanel displayPanel = null;
  private transient LogAnalysis caller;
  private JScrollPane jScrollPane = null;
  private String title = "";
  private JCheckBox narrowCheckBox = new JCheckBox();
  
  private static NumberFormat nf = new DecimalFormat("##########0"); // NumberFormat.getIntegerInstance();
  static { nf.setMaximumFractionDigits(0); }
  
  private JFormattedTextField smoothWidthTextField = new JFormattedTextField(nf);
  private JLabel smoothWidthLabel = new JLabel();
  private JButton applySmoothWidthButton = new JButton();
  private String titleRoot = "";

  public LogAnalysisFrame(LogAnalysis parent, String title, String unit)
  {
    this.caller = parent;
    this.title = title;
    this.titleRoot = title;
    if (unit.equals("hPa"))
      displayPanel = new LogAnalysisPanel(unit, 985, 1035, 5, 1);
    else if (unit.equals("V"))
      displayPanel = new LogAnalysisPanel(unit, 5d, 16d);
    else if (unit.equals("%"))
      displayPanel = new LogAnalysisPanel(unit, 0, 100, 10, 10);
    else if (unit.equals("kt"))
      displayPanel = new LogAnalysisPanel(unit, 5, 1);
    else
      displayPanel = new LogAnalysisPanel(unit);
    try
    {
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
    this.getContentPane().setLayout(new BorderLayout());
    this.setSize(new Dimension(1000, 275));
    this.setTitle(this.title);
    
    displayPanel.setPreferredSize(new Dimension(1400, 275));
    jScrollPane = new JScrollPane(displayPanel);
//  this.getContentPane().add(displayPanel, BorderLayout.CENTER);
    this.getContentPane().add(jScrollPane, BorderLayout.CENTER);
    JPanel bottomPanel = new JPanel();
    this.getContentPane().add(bottomPanel, BorderLayout.SOUTH);
    JButton widerButton = new JButton("< >");
    widerButton.setToolTipText("Make the graph wider");
    bottomPanel.add(widerButton, null);
    JButton narrowerButton = new JButton("> <");
    narrowerButton.setToolTipText("Make the graph narrower");
    bottomPanel.add(narrowerButton, null);
    widerButton.addActionListener(new ActionListener()
    {
      @Override
      public void actionPerformed(ActionEvent actionEvent)
      {
        adjustSize(WIDER);
      }
    });
    narrowerButton.addActionListener(new ActionListener()
    {
      @Override
      public void actionPerformed(ActionEvent actionEvent)
      {
        adjustSize(NARROWER);
      }
    });
    
    final JCheckBox withRawData    = new JCheckBox("Raw Data");
    final JCheckBox withSmoothData = new JCheckBox("Smooth Data");
    bottomPanel.add(withRawData, null);
    bottomPanel.add(withSmoothData, null);
    bottomPanel.add(narrowCheckBox, null);
    bottomPanel.add(smoothWidthLabel, null);
    bottomPanel.add(smoothWidthTextField, null);
    bottomPanel.add(applySmoothWidthButton, null);
    applySmoothWidthButton.addActionListener(new ActionListener()
    {
      @Override
      public void actionPerformed(ActionEvent actionEvent)
      {
        try
        {
          displayPanel.setSmoothWidth(Integer.parseInt(smoothWidthTextField.getText()));
          displayPanel.reSmooth();
          displayPanel.repaint();
        }
        catch (Exception ex)
        {
          JOptionPane.showMessageDialog(null, ex.getLocalizedMessage(), "Smooth Width Value", JOptionPane.ERROR_MESSAGE);
        }
      }
    }); 
    withRawData.setSelected(true);
    withSmoothData.setSelected(false);
    withRawData.addActionListener(new ActionListener()
    {
      @Override
      public void actionPerformed(ActionEvent actionEvent)
      {
        displayPanel.setWithRawData(withRawData.isSelected());
        displayPanel.repaint();
      }
    });
    withSmoothData.addActionListener(new ActionListener()
    {
      @Override
      public void actionPerformed(ActionEvent actionEvent)
      {
        displayPanel.setWithSmoothData(withSmoothData.isSelected());
        displayPanel.repaint();
      }
    });
    applySmoothWidthButton.setText("Apply");
    applySmoothWidthButton.setToolTipText("Apply Smoothing Width Value");
    smoothWidthLabel.setText("Smooth. width:");
    smoothWidthTextField.setPreferredSize(new Dimension(70, 19));
    smoothWidthTextField.setText("1000");
    smoothWidthTextField.setHorizontalAlignment(JTextField.RIGHT);
    narrowCheckBox.setText("Narrow");
    narrowCheckBox.addActionListener(new ActionListener()
    {
      @Override
      public void actionPerformed(ActionEvent actionEvent)
      {
        displayPanel.setNarrow(narrowCheckBox.isSelected());
        displayPanel.repaint();
      }
    });
    this.setDefaultCloseOperation(JFrame.DO_NOTHING_ON_CLOSE);
    this.addWindowListener(new WindowAdapter()
                           {
                             @Override
                             public void windowClosing(WindowEvent e) 
                             {
                               System.out.println(">>> Closing " + getTitle());
                           //  Broadcast this to the table.. (check box)
                               caller.frameHasBeenClosed();
                               setVisible(false);
                             }
                           });
  }
  
  private final static int WIDER    = 0;
  private final static int NARROWER = 1;
  
  private void adjustSize(int mode)
  {
    Dimension dim = displayPanel.getSize();
    Dimension newDim = new Dimension(mode == WIDER ? (int)(dim.width * 1.1) : (int)(dim.width / 1.1), dim.height);
    displayPanel.setPreferredSize(newDim);
    displayPanel.setSize(newDim);
    displayPanel.repaint();
  }

  public void setSmoothWidthValue(int v)
  {
    this.smoothWidthTextField.setText(Integer.toString(v)); 
  }
  
  public void setLogData(Map<Date, ScalarValue> logdata, Map<Long, Calendar[]> riseAndSet, String tz)
  {
    this.setTitle(this.titleRoot + " - " + Integer.toString(logdata.size()) + " entries");
    int sw = logdata.size() / 200;
    if (sw % 2 == 1) // Must be even
      sw += 1;
    setSmoothWidthValue(sw);
    displayPanel.setSmoothWidth(sw);
    displayPanel.setLogData(logdata, riseAndSet, tz);
  }
  
  public void setTimeZone(String tz)
  {
    displayPanel.setTimeZone(tz);
  }
}
