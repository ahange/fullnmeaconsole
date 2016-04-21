package nmea.ui.remote;

import coreutilities.HTTPClient;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Insets;
import java.awt.Toolkit;

import javax.swing.JApplet;
import javax.swing.JFrame;
import javax.swing.JPanel;
import javax.swing.UIManager;

import coreutilities.gui.JumboDisplay;

public class NMEAApplet
  extends JApplet
{
  private GridBagLayout gridBagLayout1 = new GridBagLayout();
  private int basicJumboSize = 24;
  private JumboDisplay bspJumbo = new JumboDisplay("BSP", "0.0", "Boat Speed", basicJumboSize);
  
  private boolean loop = true;

  public NMEAApplet()
  {
  }

  private void jbInit()
    throws Exception
  {
    this.getContentPane().setLayout(gridBagLayout1);
    this.getContentPane().add(bspJumbo, new GridBagConstraints(0, 0, 1, 1, 0.0, 0.0, GridBagConstraints.CENTER, GridBagConstraints.NONE,
          new Insets(0, 0, 0, 0), 0, 0));
    
    
  }

  public void init()
  {
    try
    {
      String title = "BSP";
      try { title = this.getParameter("title"); } catch (Exception ex) {} 
      String value = "0.0";
      try { value = this.getParameter("value"); } catch (Exception ex) {}
      String tooltip = "Boat Speed";
      try { tooltip = this.getParameter("tooltip"); } catch (Exception ex) {}
      jbInit();

      bspJumbo.setName(title);
      bspJumbo.setDisplayColor(Color.green);
      bspJumbo.setValue(value);
      bspJumbo.setToolTipText(tooltip);
    }
    catch (Exception e)
    {
      e.printStackTrace();
    }
  }

  public static void main(String[] args)
  {
    NMEAApplet applet = new NMEAApplet();
    JFrame frame = new JFrame();
    frame.setDefaultCloseOperation( JFrame.EXIT_ON_CLOSE );
    frame.getContentPane().add(applet, BorderLayout.CENTER);
    frame.setTitle( "NMEA Applet Frame" );
    applet.init();
    applet.start();
    frame.setSize(300, 300);
    Dimension d = Toolkit.getDefaultToolkit().getScreenSize();
    Dimension frameSize = frame.getSize();
    frame.setLocation((d.width-frameSize.width)/2, (d.height-frameSize.height)/2);
    frame.setVisible(true);
  }
  static
  {
    try
    {
    }
    catch (Exception e)
    {
    }
  }

  @Override
  public void destroy()
  {
    super.destroy();
    System.out.println("NMEA Applet: destroy");
    loop = false;
  }
}
