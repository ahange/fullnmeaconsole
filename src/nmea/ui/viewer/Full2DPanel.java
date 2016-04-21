package nmea.ui.viewer;

import java.awt.BorderLayout;

import java.awt.Dimension;

import javax.swing.JPanel;
import javax.swing.JTabbedPane;

import nmea.ui.viewer.elements.ControlPanelHolder;

public class Full2DPanel
  extends JPanel
{
  private BorderLayout   borderLayout = new BorderLayout();
  private JTabbedPane   jtabbedPane2D = new JTabbedPane();
  private CurrentSituationPanel    fp = new CurrentSituationPanel();
  private EvolutionPanel           ep = new EvolutionPanel();
  private ControlPanelHolder       cp = new ControlPanelHolder(fp);
  private TwinDRPanel              dr = new TwinDRPanel(); 

  public Full2DPanel()
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
    this.setLayout(borderLayout);
    this.setSize(new Dimension(598, 431));
    jtabbedPane2D.setTabPlacement(JTabbedPane.BOTTOM);
    this.add(jtabbedPane2D, BorderLayout.CENTER);
    
    // LOCALIZE
    jtabbedPane2D.add("Live Data", fp);
    jtabbedPane2D.add("Dead Reckoning", dr);
    jtabbedPane2D.add("Evolution", ep);
    
    this.add(cp, BorderLayout.SOUTH);
  }
  
  public CurrentSituationPanel getFullSituationPanel()
  {
    return fp;
  }
}
