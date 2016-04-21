package nmea.ui.viewer.spot;

import java.awt.BorderLayout;
import java.awt.Dimension;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

import java.text.SimpleDateFormat;

import java.util.Date;
import java.util.HashMap;

import javax.swing.ImageIcon;
import javax.swing.JInternalFrame;
import javax.swing.JMenu;
import javax.swing.JMenuBar;
import javax.swing.JMenuItem;
import javax.swing.JOptionPane;
import javax.swing.JSeparator;
import javax.swing.event.InternalFrameAdapter;
import javax.swing.event.InternalFrameEvent;

import nmea.local.LogisailResourceBundle;

import nmea.server.constants.Constants;
import nmea.server.ctx.NMEAContext;
import nmea.server.ctx.NMEADataCache;
import nmea.server.datareader.CustomNMEAClient;
import nmea.server.utils.Utils;

import nmea.ui.NMEAInternalFrame;
import nmea.ui.NMEAMasterPanel;
import nmea.ui.viewer.spot.ctx.SpotCtx;
import nmea.ui.widgets.BeaufortPanel;
import nmea.ui.widgets.ConfigTablePanel;

import ocss.nmea.parser.TrueWindDirection;
import ocss.nmea.parser.TrueWindSpeed;
import ocss.nmea.utils.WindUtils;

public class SPOTInternalFrame
    extends JInternalFrame
{
  private SpotPanelHolder spotPanel = new SpotPanelHolder();
  private BorderLayout borderLayout1 = new BorderLayout();

  public SPOTInternalFrame()
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
    setSize(new Dimension(1200, 650));
    setTitle("SPOT Bulletins");
    try { this.setFrameIcon(new ImageIcon(this.getClass().getResource("anchor.png"))); } catch (Exception ignore) {}
    this.addInternalFrameListener(new InternalFrameAdapter()
      {
        public void internalFrameClosed(InternalFrameEvent e)
        {
          SpotCtx.getInstance().fireInternalFrameClosed();
//        NMEAContext.getInstance().removeNMEAListenerGroup(Constants.NMEA_SERVER_LISTENER_GROUP_ID);
        }
      });
    getContentPane().add(spotPanel, BorderLayout.CENTER);
  }

}
