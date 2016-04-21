package nmea.ui.viewer.elements;

import java.awt.BorderLayout;

import javax.swing.JPanel;

import nmea.ui.viewer.CurrentSituationPanel;

public class ControlPanelHolder
  extends JPanel
{
  private CurrentSituationPanel parent = null;
  private BorderLayout borderLayout = new BorderLayout();
  private ControlPanelForAll cp4all = null;
  private ShowHidePanel shp = new ShowHidePanel(this);

  public ControlPanelHolder(CurrentSituationPanel cp)
  {
    this.parent = cp;
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
    cp4all = new ControlPanelForAll(this.parent);
    this.add(cp4all, BorderLayout.CENTER);
    this.add(shp,    BorderLayout.NORTH);
  }
  
  public void flip()
  {
    cp4all.setVisible(!cp4all.isVisible());
  }
}
