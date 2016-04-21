package nmea.ui.widgets;

import java.awt.Dimension;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;

import java.awt.Insets;

import java.text.DecimalFormat;

import javax.swing.JComboBox;
import javax.swing.JFormattedTextField;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JTextField;
import javax.swing.text.NumberFormatter;

public class DeclinationPanel
  extends JPanel
{
  private GridBagLayout gridBagLayout1 = new GridBagLayout();
  private JFormattedTextField declinationTextField = new JFormattedTextField(new DecimalFormat("00.00"));
  private JLabel degreeLabel = new JLabel();
  private JComboBox ewComboBox = new JComboBox();

  public DeclinationPanel()
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

  private void jbInit()
    throws Exception
  {
    this.setLayout(gridBagLayout1);
    declinationTextField.setPreferredSize(new Dimension(60, 20));
    declinationTextField.setText(((NumberFormatter)declinationTextField.getFormatter()).getFormat().format(0.0));
    declinationTextField.setHorizontalAlignment(JTextField.CENTER);
    degreeLabel.setText("\272");
    ewComboBox.setPreferredSize(new Dimension(40, 20));
    ewComboBox.removeAllItems();
    ewComboBox.addItem("E");
    ewComboBox.addItem("W");
    this.add(declinationTextField, new GridBagConstraints(0, 0, 1, 1, 0.0, 0.0, GridBagConstraints.CENTER, GridBagConstraints.NONE, new Insets(0, 0, 0, 0), 0, 0));
    this.add(degreeLabel, new GridBagConstraints(1, 0, 1, 1, 0.0, 0.0, GridBagConstraints.CENTER, GridBagConstraints.NONE, new Insets(0, 2, 0, 2), 0, 0));
    this.add(ewComboBox, new GridBagConstraints(2, 0, 1, 1, 0.0, 0.0, GridBagConstraints.CENTER, GridBagConstraints.NONE, new Insets(0, 0, 0, 0), 0, 0));
  }
  
  public void setDeclinationValue(double d)
  {    
    declinationTextField.setText(((NumberFormatter)declinationTextField.getFormatter()).getFormat().format(Math.abs(d)));
    if (d < 0)
      ewComboBox.setSelectedItem("W");
  }
  
  public double getDeclinationValue()
  {
    double d = 0;
    try { d = ((DecimalFormat)((NumberFormatter)declinationTextField.getFormatter()).getFormat()).parse(declinationTextField.getText()).doubleValue(); } catch (Exception ex) {}
    return d;
  }
}
