package nmea.ui.viewer;

import java.awt.BorderLayout;

import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTabbedPane;

import nmea.ui.viewer.elements.BufferSizePanel;
import nmea.ui.viewer.elements.DepthEvolutionDisplay;

public class EvolutionPanel
  extends JPanel
{
  private BorderLayout borderLayout = new BorderLayout();
  private JTabbedPane jTabbedPane   = new JTabbedPane();
  
  private SpeedEvolutionPanel     sep = new SpeedEvolutionPanel();
  private DirectionEvolutionPanel dep = new DirectionEvolutionPanel();  
  private DepthEvolutionDisplay   ded = new DepthEvolutionDisplay("DBT", "Depth", 36); // TODO Add temperature?
  
  private BufferSizePanel         bsp = new BufferSizePanel();

  public EvolutionPanel()
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
    jTabbedPane.setTabPlacement(JTabbedPane.RIGHT);
    this.add(jTabbedPane, BorderLayout.CENTER);
    JPanel bottomPlaceHolder = new JPanel(new BorderLayout());
    bottomPlaceHolder.add(bsp, BorderLayout.WEST);
    this.add(bottomPlaceHolder, BorderLayout.SOUTH);
    
    jTabbedPane.add("Speeds",     new JScrollPane(sep));
    jTabbedPane.add("Directions", dep); // new JScrollPane(dep));
    jTabbedPane.add("Depth",      ded);
  }
}
