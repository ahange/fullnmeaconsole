package nmea.ui.widgets;

import nmea.local.LogisailResourceBundle;

import java.awt.Dimension;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;

import java.awt.Insets;

import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JTextField;

public class ConfigNamePanel
  extends JPanel
{
  private GridBagLayout gridBagLayout1 = new GridBagLayout();
  private JLabel configNameLabel = new JLabel();
  private JTextField configNameTextField = new JTextField();

  public ConfigNamePanel()
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
    configNameLabel.setText(LogisailResourceBundle.buildMessage("name-your-config"));
    configNameTextField.setPreferredSize(new Dimension(150, 20));
    this.add(configNameLabel, new GridBagConstraints(0, 0, 1, 1, 0.0, 0.0, GridBagConstraints.WEST, GridBagConstraints.NONE,
          new Insets(0, 0, 0, 0), 0, 0));
    this.add(configNameTextField, new GridBagConstraints(0, 1, 1, 1, 0.0, 0.0, GridBagConstraints.WEST, GridBagConstraints.NONE,
          new Insets(0, 0, 0, 0), 0, 0));
  }
  
  public String getName()
  {
    return configNameTextField.getText();
  }
}
