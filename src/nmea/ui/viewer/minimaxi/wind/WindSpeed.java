package nmea.ui.viewer.minimaxi.wind;

import coreutilities.gui.JumboDisplay;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.Graphics;
import java.awt.Graphics2D;

import java.text.DecimalFormat;

import java.util.Date;

import javax.swing.JPanel;

import nmea.event.NMEAReaderListener;

import nmea.server.constants.Constants;
import nmea.server.ctx.NMEAContext;
import nmea.server.ctx.NMEADataCache;

import nmea.ui.viewer.minimaxi.GaugePanel;
import nmea.ui.viewer.minimaxi.MinMaxPanelInterface;

import ocss.nmea.parser.TrueWindSpeed;
import ocss.nmea.utils.WindUtils;


public class WindSpeed
  extends JPanel
implements MinMaxPanelInterface
{
  private BorderLayout borderLayout1 = new BorderLayout();
  private GaugePanel wgp = null;
  private JumboDisplay   tws = null;

  private int jumboFontSize = 36;
  
  private double minimum =  Double.MAX_VALUE;
  private double maximum = -Double.MAX_VALUE;
  
  private Date minDate = null;
  private Date maxDate = null;

  public WindSpeed(int refSize)
  {
    jumboFontSize = refSize;
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
    wgp = new GaugePanel(this, 0d, 70d, new DecimalFormat("00.00 'kts'"), new Color(128, 0, 0), Color.white, Color.black)
      {
//      public void paintComponent(Graphics gr)
        public void paintBackGround(Graphics gr)
        {
//        super.paintComponent(gr);
          super.paintBackGround(gr);
          int fontSize = gr.getFont().getSize();
          
          Graphics2D g2d = (Graphics2D)gr;
          // Print beaufort scale
          gr.setColor(Color.red);
          int h = this.getHeight() - fontSize;
          for (int b=0; b < WindUtils.BEAUFORT_SCALE.length; b++)
          {
            int y = (int)(h + (fontSize / 2) - ((WindUtils.BEAUFORT_SCALE[b] / (maxValue - minValue)) * h));
            String str = /* "F " + */ Integer.toString(b);
            int strWidth  = gr.getFontMetrics(gr.getFont()).stringWidth(str);
            gr.drawString(str, this.getWidth() - strWidth, y + (gr.getFont().getSize() / 2));
//          y = (int)(h + (fontSize / 2) - ((beaufortScale[b] / (maxValue - minValue)) * h));
//          g2d.drawLine(3 * this.getWidth() / 4, y, this.getWidth() - strWidth - strOffset, y);
            g2d.drawLine(this.getWidth() / 2, y, this.getWidth() - strWidth - strOffset, y);
          }
          // Print speed in knots
          for (int s=0; s<=70; s += 10)
          {
            int y = (int)(h + (fontSize / 2) - ((s / (maxValue - minValue)) * h));
            String str = Integer.toString(s) + " kts";
            int strWidth  = gr.getFontMetrics(gr.getFont()).stringWidth(str);
            gr.drawString(str, 2, y + (gr.getFont().getSize() / 2));
//          y = (int)(h + (fontSize / 2) - ((beaufortScale[b] / (maxValue - minValue)) * h));
            g2d.drawLine(strWidth + 4, y, this.getWidth() / 2, y);
          }
          
          // Find current beaufort
          int currentBeaufort = WindUtils.BEAUFORT_SCALE.length;
          for (int b=0; b < WindUtils.BEAUFORT_SCALE.length; b++)
          {
            if (value < WindUtils.BEAUFORT_SCALE[b])
            {
              currentBeaufort = b;
              break;
            }
          }
          try
          {
            setToolTipText("<html>" + 
                           valueFormat.format(value) + " = " + Integer.toString(currentBeaufort - 1) + " Beaufort" + "<br>" +
                           "Min at " + minDate.toString() + "<br>" +
                           "Max at " + maxDate.toString() + 
                           "</html>");
          }
          catch (Exception ex)
          {
            System.err.println("Managed:" + ex.toString());            
          }
        }        
      }; 
    this.setLayout(borderLayout1);
    resize(jumboFontSize);
    this.add(wgp, BorderLayout.CENTER);
    tws = new JumboDisplay("TWS min-max", "00.00", "True Wind Speed", jumboFontSize);
    this.add(tws, BorderLayout.SOUTH);
    tws.setDisplayColor(Color.orange);

    NMEAContext.getInstance().addNMEAReaderListener(new NMEAReaderListener(Constants.NMEA_SERVER_LISTENER_GROUP_ID, "Wind Speed")
      {
        @Override
        public void dataUpdate()
        {
          try
          {
            double trueWS = ((TrueWindSpeed) NMEAContext.getInstance().getCache().get(NMEADataCache.TWS)).getValue(); 
            if (trueWS != -Double.MAX_VALUE && !Double.isInfinite(trueWS))
            {
    //        wgp.setValue(trueWS);          
              tws.setValue(NMEAContext.DF22.format(trueWS));
              if (trueWS > maximum) 
              {
                maximum = trueWS;
                maxDate = new Date();
              }
              if (trueWS < minimum) 
              {
                minimum = trueWS;
                minDate = new Date();
              }
              wgp.setValues(trueWS, maximum, minimum);
            }
          }
          catch (NullPointerException npe)
          {
            // Not in cache (yet)              
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
    wgp.paintBackGround(gr);
  }
  
  public void reset()
  {
    minimum = Double.MAX_VALUE;
    maximum = -Double.MAX_VALUE;
  }
}
