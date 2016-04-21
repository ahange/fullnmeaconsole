package nmea.ui.viewer.elements;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.Font;
import java.awt.GradientPaint;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.RenderingHints;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;

import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.SwingConstants;


public class ShowHidePanel
     extends JPanel
{
  private BorderLayout borderLayout1 = new BorderLayout();
  private JLabel showHideLabel = new JLabel();
  private ControlPanelHolder parent = null;

  public ShowHidePanel(ControlPanelHolder cph)
  {
    parent = cph;
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
    this.setLayout(borderLayout1);
    this.setSize(new Dimension(400, 20));
    Font f = showHideLabel.getFont().deriveFont(9f);
    showHideLabel.setFont(f);
    showHideLabel.setText("Show/Hide Control Panel");
    showHideLabel.setHorizontalAlignment(SwingConstants.CENTER);
    this.add(showHideLabel, BorderLayout.CENTER);
    this.addMouseListener(new MouseAdapter()
                          {
                            public void mouseClicked(MouseEvent e)
                            {
                              control_mouseClicked(e);
                            }
                          });
  }

  public void paintComponent(Graphics g)
  {
    ((Graphics2D)g).setRenderingHint(RenderingHints.KEY_TEXT_ANTIALIASING,
                                     RenderingHints.VALUE_TEXT_ANTIALIAS_ON);      
    ((Graphics2D)g).setRenderingHint(RenderingHints.KEY_ANTIALIASING,
                                     RenderingHints.VALUE_ANTIALIAS_ON);      
    Color startColor = Color.white;
    Color endColor   = Color.gray;
    GradientPaint gradient = new GradientPaint(0, this.getHeight(), startColor, 0, 0, endColor); // vertical, upside down
    ((Graphics2D)g).setPaint(gradient);
    g.fillRect(0, 0, this.getWidth(), this.getHeight());        
  }
  
  private void control_mouseClicked(MouseEvent e)
  {
    parent.flip();
  }
}
