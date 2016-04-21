package nmea.ui;

import java.awt.BorderLayout;
import java.awt.Dimension;

import javax.swing.ImageIcon;
import javax.swing.JInternalFrame;
import javax.swing.event.InternalFrameAdapter;
import javax.swing.event.InternalFrameEvent;

import nmea.server.ctx.NMEAContext;

public class NMEAAnalyzerInternalFrame
  extends JInternalFrame
{
  private BorderLayout borderLayout1 = new BorderLayout();
  private NMEAAnalyzerLandingPanel panel = new NMEAAnalyzerLandingPanel();

  public NMEAAnalyzerInternalFrame()
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
    getContentPane().setLayout(borderLayout1);
    setSize(new Dimension(450, 450));
    setTitle("NMEA Analyzer");
    try { this.setFrameIcon(new ImageIcon(this.getClass().getResource("controller.png"))); } catch (Exception ignore) {}
    this.addInternalFrameListener(new InternalFrameAdapter()
      {
        public void internalFrameClosed(InternalFrameEvent e)
        {
          NMEAContext.getInstance().fireInternalAnalyzerFrameClosed();
        }
      });
    getContentPane().add(panel, BorderLayout.CENTER);
  }
}
