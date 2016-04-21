package nmea.ui.viewer.minimaxi.boatspeed;

import nmea.server.ctx.NMEAContext;
import nmea.server.ctx.NMEADataCache;

import nmea.event.NMEAReaderListener;

import coreutilities.gui.JumboDisplay;
import nmea.ui.viewer.minimaxi.GaugePanel;
import nmea.ui.viewer.minimaxi.MinMaxPanelInterface;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.Graphics;

import java.text.DecimalFormat;

import java.util.Date;

import javax.swing.JPanel;

import nmea.server.constants.Constants;

import ocss.nmea.parser.Speed;

public class BoatSpeed
  extends JPanel
  implements MinMaxPanelInterface
{
  private BorderLayout borderLayout1 = new BorderLayout();
  private GaugePanel bgp = null;
  private JumboDisplay bsp = null;

  private double minimum = Double.MAX_VALUE;
  private double maximum = -Double.MAX_VALUE;

  private Date minDate = null;
  private Date maxDate = null;
  
  private int jumboFontSize = 36;

  public BoatSpeed(int refSize)
  {
    this.jumboFontSize = refSize;
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
    
    bgp = new GaugePanel(this, 0d, 20d, new DecimalFormat("00.00 'kts'"), new Color(0, 0, 128), Color.black, Color.white);
    this.setLayout(borderLayout1);
    
    resize(jumboFontSize);
    
    this.add(bgp, BorderLayout.CENTER);
    bsp = new JumboDisplay("BSP min-max", "00.00", "Boat Speed", jumboFontSize);
    this.add(bsp, BorderLayout.SOUTH);
    
    bsp.setDisplayColor(Color.orange);

    NMEAContext.getInstance().addNMEAReaderListener(new NMEAReaderListener(Constants.NMEA_SERVER_LISTENER_GROUP_ID, "Boat Speed")
      {
        @Override
        public void dataUpdate()
        {
          double boatSpeed = ((Speed) NMEAContext.getInstance().getCache().get(NMEADataCache.BSP)).getValue();
          if (boatSpeed != -Double.MAX_VALUE)
          {
  //        bgp.setValue(boatSpeed);
            bsp.setValue(NMEAContext.DF22.format(boatSpeed));
            if (boatSpeed > maximum)
            {
              maximum = boatSpeed;
              maxDate = new Date();
            }
            if (boatSpeed < minimum)
            {
              minimum = boatSpeed;
              minDate = new Date();
            }
            bgp.setValues(boatSpeed, maximum, minimum);
          }
        }
      });
  }

  public void resize(int bigFontSize)
  {
    jumboFontSize  = bigFontSize;
    int width = (int)(120d * (double)jumboFontSize / 36d);
    int height = (int)(200d * (double)jumboFontSize / 36d);
    this.setSize(new Dimension(width, height));
    this.setPreferredSize(new Dimension(width, height));
  }
  
  public void paintBackGround(Graphics gr)
  {
    bgp.paintBackGround(gr);
  }

  public void reset()
  {
    minimum = Double.MAX_VALUE;
    maximum = -Double.MAX_VALUE;
  }
}
