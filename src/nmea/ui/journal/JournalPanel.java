package nmea.ui.journal;

import java.awt.BorderLayout;
import java.awt.Dimension;

import javax.swing.JPanel;

public class JournalPanel
  extends JPanel
{
  JournalSetupPanel jsp = new JournalSetupPanel();
  JournalDataPanel  jdp = new JournalDataPanel();
  
  public JournalPanel()
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
    this.setLayout( new BorderLayout() );
    jsp.setPreferredSize(new Dimension(400, 150));
    jdp.setPreferredSize(new Dimension(400, 200));
//  this.add(jsp, new GridBagConstraints(0, 0, 1, 1, 0.0, 0.0, GridBagConstraints.CENTER, GridBagConstraints.BOTH, new Insets(0, 0, 0, 0), 0, 0));
//  this.add(jdp, new GridBagConstraints(0, 1, 1, 1, 0.0, 0.0, GridBagConstraints.CENTER, GridBagConstraints.BOTH, new Insets(3, 0, 0, 0), 0, 0));
    this.add(jsp, BorderLayout.NORTH);
    this.add(jdp, BorderLayout.CENTER);
  }
}
