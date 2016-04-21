package nmea.ui.widgets;


import astro.calc.GeoPoint;

import java.awt.Dimension;
import java.awt.Font;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Insets;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.FocusAdapter;
import java.awt.event.FocusEvent;

import java.text.DecimalFormat;

import javax.swing.BorderFactory;
import javax.swing.JButton;
import javax.swing.JComboBox;
import javax.swing.JFormattedTextField;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JTextField;
import javax.swing.text.NumberFormatter;

import nmea.server.ctx.NMEAContext;

import ocss.nmea.parser.GeoPos;

public class PositionPanel extends JPanel 
{
  @SuppressWarnings("compatibility:3582636225952168128")
  public final static long serialVersionUID = 1L;
  
  private GridBagLayout gridBagLayout1 = new GridBagLayout();
  private JLabel jLabel2 = new JLabel();
  private JLabel jLabel3 = new JLabel();
  private JFormattedTextField LDeg = new JFormattedTextField(new DecimalFormat("00"));
  private JLabel jLabel4 = new JLabel();
  private JFormattedTextField LMin = new JFormattedTextField(new DecimalFormat("00.00"));
  private JComboBox LSign = new JComboBox();
  private JComboBox GSign = new JComboBox();
  private JFormattedTextField GMin = new JFormattedTextField(new DecimalFormat("00.00"));
  private JFormattedTextField GDeg = new JFormattedTextField(new DecimalFormat("000"));
  private JLabel jLabel5 = new JLabel();
  private JButton updatePositionButton = new JButton();
  
  private Font font = new Font("Tahoma", 0, 9);

  private boolean showButton = true;

  public PositionPanel()
  {
    this(true);
  }
  public PositionPanel(Font f)
  {
    this(true, f);
  }
  public PositionPanel(boolean b)
  {
    this(b, null);
  }
  public PositionPanel(boolean b, Font f)
  {
    this.showButton = b;
    if (f != null)
      this.font = f;
    
    try
    {
      jbInit();
    }
    catch(Exception e)
    {
      e.printStackTrace();
    }
  }
  
  public void showButton(boolean b)
  {
    this.showButton = b;
    updatePositionButton.setVisible(b);
    this.repaint();
  }

  private void jbInit() throws Exception
  {
    this.setLayout(gridBagLayout1);
    this.setSize(new Dimension(160, 100));
    this.setPreferredSize(new Dimension(160, 100));
    this.setMinimumSize(new Dimension(160, 100));
    this.setBorder(BorderFactory.createTitledBorder("Position"));
    this.setFont(font);
    jLabel2.setText("L:");
    jLabel2.setFont(font);
    jLabel3.setText("G:");
    jLabel3.setFont(font);
    LDeg.setText("00");
    LDeg.setHorizontalAlignment(JTextField.RIGHT);
    LDeg.setPreferredSize(new Dimension(40, 20));
    LDeg.setMinimumSize(new Dimension(40, 20));
    LDeg.setFont(font);
    LDeg.addFocusListener(new FocusAdapter()
      {
        public void focusGained(FocusEvent e)
        {
          LDeg_focusGained(e);
        }
      });
    jLabel4.setText("°");
    jLabel4.setFont(font);
    LMin.setText(((NumberFormatter)LMin.getFormatter()).getFormat().format(0.0));
    LMin.setHorizontalAlignment(JTextField.RIGHT);
    LMin.setPreferredSize(new Dimension(50, 20));
    LMin.setMinimumSize(new Dimension(50, 20));
    LMin.setFont(font);
    LMin.addFocusListener(new FocusAdapter()
      {
        public void focusGained(FocusEvent e)
        {
          LMin_focusGained(e);
        }
      });
    LSign.setPreferredSize(new Dimension(35, 20));
    LSign.setMinimumSize(new Dimension(35, 20));
    LSign.setFont(font);
    LSign.addItem("N");
    LSign.addItem("S");
    GSign.setPreferredSize(new Dimension(35, 20));
    GSign.setMinimumSize(new Dimension(35, 20));
    GSign.setFont(font);
    GSign.addItem("E");
    GSign.addItem("W");
    GMin.setText(((NumberFormatter)GMin.getFormatter()).getFormat().format(0.0));
    GMin.setHorizontalAlignment(JTextField.RIGHT);
    GMin.setPreferredSize(new Dimension(50, 20));
    GMin.setMinimumSize(new Dimension(50, 20));
    GMin.setFont(font);
    GMin.addFocusListener(new FocusAdapter()
      {
        public void focusGained(FocusEvent e)
        {
          GMin_focusGained(e);
        }
      });
    GDeg.setText("000");
    GDeg.setHorizontalAlignment(JTextField.RIGHT);
    GDeg.setPreferredSize(new Dimension(40, 20));
    GDeg.setMinimumSize(new Dimension(40, 20));
    GDeg.setFont(font);
    GDeg.addFocusListener(new FocusAdapter()
      {
        public void focusGained(FocusEvent e)
        {
          GDeg_focusGained(e);
        }
      });
    jLabel5.setText("°");
    jLabel5.setFont(font);
    updatePositionButton.setVisible(showButton);
    updatePositionButton.setFont(font);
    updatePositionButton.setText("Set");
    updatePositionButton.addActionListener(new ActionListener()
      {
        public void actionPerformed(ActionEvent e)
        {
          updatePositionButton_actionPerformed(e);
        }
      });
    this.add(jLabel2, new GridBagConstraints(0, 0, 1, 1, 0.0, 0.0, GridBagConstraints.CENTER, GridBagConstraints.NONE, new Insets(0, 0, 0, 0), 0, 0));
    this.add(jLabel3, new GridBagConstraints(0, 1, 1, 1, 0.0, 0.0, GridBagConstraints.CENTER, GridBagConstraints.NONE, new Insets(0, 0, 0, 0), 0, 0));
    this.add(LDeg, new GridBagConstraints(1, 0, 1, 1, 0.0, 0.0, GridBagConstraints.CENTER, GridBagConstraints.HORIZONTAL, new Insets(0, 0, 0, 0), 0, 0));
    this.add(jLabel4, new GridBagConstraints(2, 0, 1, 1, 0.0, 0.0, GridBagConstraints.CENTER, GridBagConstraints.NONE, new Insets(0, 0, 0, 0), 0, 0));
    this.add(LMin, new GridBagConstraints(3, 0, 1, 1, 0.0, 0.0, GridBagConstraints.CENTER, GridBagConstraints.HORIZONTAL, new Insets(0, 0, 0, 0), 0, 0));
    this.add(LSign, new GridBagConstraints(4, 0, 1, 1, 0.0, 0.0, GridBagConstraints.CENTER, GridBagConstraints.NONE, new Insets(0, 0, 0, 0), 0, 0));
    this.add(GSign, new GridBagConstraints(4, 1, 1, 1, 0.0, 0.0, GridBagConstraints.CENTER, GridBagConstraints.NONE, new Insets(0, 0, 0, 0), 0, 0));
    this.add(GMin, new GridBagConstraints(3, 1, 1, 1, 0.0, 0.0, GridBagConstraints.CENTER, GridBagConstraints.HORIZONTAL, new Insets(0, 0, 0, 0), 0, 0));
    this.add(GDeg, new GridBagConstraints(1, 1, 1, 1, 0.0, 0.0, GridBagConstraints.CENTER, GridBagConstraints.HORIZONTAL, new Insets(0, 0, 0, 0), 0, 0));
    this.add(jLabel5, new GridBagConstraints(2, 1, 1, 1, 0.0, 0.0, GridBagConstraints.CENTER, GridBagConstraints.NONE, new Insets(0, 0, 0, 0), 0, 0));
    if (showButton)
      this.add(updatePositionButton, new GridBagConstraints(1, 2, 5, 1, 0.0, 0.0, GridBagConstraints.CENTER, GridBagConstraints.NONE, new Insets(3, 0, 0, 0), 0, 0));
  }

  public double getL()
  {
    double l = 0.0;
//  String d = LDeg.getText();
//  String m = LMin.getText();
//  l = GeomUtil.sexToDec(d, m);

    try
    {
      double deg = ((DecimalFormat)((NumberFormatter)LDeg.getFormatter()).getFormat()).parse(LDeg.getText()).doubleValue(); // Double.parseDouble(degreeFormattedTextField.getText());
      double min = ((DecimalFormat)((NumberFormatter)LMin.getFormatter()).getFormat()).parse(LMin.getText()).doubleValue(); // Double.parseDouble(minuteFormattedTextField.getText());
      l = deg + (min / 60d);
    }
    catch (Exception ex)
    {
      ex.printStackTrace();
    }

    String ns = (String)LSign.getSelectedItem();
    if (ns.toUpperCase().equals("S"))
      l *= -1;
    return l;
  }
  
  public double getG()
  {
    double g = 0.0;
//  String d = GDeg.getText();
//  String m = GMin.getText();
//  g = GeomUtil.sexToDec(d, m);

    try
    {
      double deg = ((DecimalFormat)((NumberFormatter)GDeg.getFormatter()).getFormat()).parse(GDeg.getText()).doubleValue(); // Double.parseDouble(degreeFormattedTextField.getText());
      double min = ((DecimalFormat)((NumberFormatter)GMin.getFormatter()).getFormat()).parse(GMin.getText()).doubleValue(); // Double.parseDouble(minuteFormattedTextField.getText());
      g = deg + (min / 60d);
    }
    catch (Exception ex)
    {
      ex.printStackTrace();
    }

    String ew = (String)GSign.getSelectedItem();
    if (ew.toUpperCase().equals("W"))
      g *= -1;
    return g;
  }
  
  public GeoPoint getPosition()
  {
    return new GeoPoint(getL(), getG());
  }
  
  public void setPosition(double l, double g)
  {
    double absVal = Math.abs(l);
    double intValue = Math.floor(absVal);
    double dec = absVal - intValue;
    int i = (int)intValue;
    dec *= 60D;
    DecimalFormat df = new DecimalFormat("00.00");
    LDeg.setText(Integer.toString(i));
    LMin.setText(df.format(dec));
    LSign.setSelectedItem(l<0?"S":"N");
    
    absVal = Math.abs(g);
    intValue = Math.floor(absVal);
    dec = absVal - intValue;
    i = (int)intValue;
    dec *= 60D;
    GDeg.setText(Integer.toString(i));
    GMin.setText(df.format(dec));
    GSign.setSelectedItem(g<0?"W":"E");    
  }

  public void setLDeg(String str)
  {
    LDeg.setText(str);
  }
  public void setLMin(String str)
  {
    LMin.setText(str);
  }
  public void setGDeg(String str)
  {
    GDeg.setText(str);
  }
  public void setGMin(String str)
  {
    GMin.setText(str);
  }
  public void setLSign(String str)
  {
    LSign.setSelectedItem(str);
  }
  public void setGSign(String str)
  {
    GSign.setSelectedItem(str);
  }

  public void setEnabled(boolean b)
  {
    jLabel2.setEnabled(b);
    jLabel3.setEnabled(b);
    LDeg.setEnabled(b);
    jLabel4.setEnabled(b);
    LMin.setEnabled(b);
    LSign.setEnabled(b);
    GSign.setEnabled(b);
    GMin.setEnabled(b);
    GDeg.setEnabled(b);
    jLabel5.setEnabled(b);
    updatePositionButton.setEnabled(b);    
  }

  private void updatePositionButton_actionPerformed(ActionEvent e)
  {
    GeoPos gp = new GeoPos(getPosition().getL(), getPosition().getG());
    NMEAContext.getInstance().firePositionManuallyChanged(gp);
  }

  private void LDeg_focusGained(FocusEvent e)
  {
    selectField((JTextField)e.getSource());
  }

  private void GDeg_focusGained(FocusEvent e)
  {
    selectField((JTextField)e.getSource());
  }

  private void LMin_focusGained(FocusEvent e)
  {
    selectField((JTextField)e.getSource());
  }

  private void GMin_focusGained(FocusEvent e)
  {
    selectField((JTextField)e.getSource());
  }

  public void setCustomFont(Font font)
  {
    this.font = font;
//  System.out.println("Font:" + font.getName() + " " + font.getSize());
    validate();
    repaint();
  }

  public Font getFont()
  {
    return font;
  }

  public static void selectField(JTextField jtf)
  {
    jtf.setSelectionStart(0);
    jtf.setSelectionEnd(jtf.getText().length());  
  }
}
