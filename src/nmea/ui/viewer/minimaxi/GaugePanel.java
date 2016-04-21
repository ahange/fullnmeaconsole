package nmea.ui.viewer.minimaxi;


import java.awt.AlphaComposite;
import java.awt.Color;
import java.awt.Font;
import java.awt.GradientPaint;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.RenderingHints;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;

import java.text.DecimalFormat;
import java.text.Format;

import javax.swing.JMenuItem;
import javax.swing.JPanel;
import javax.swing.JPopupMenu;
import javax.swing.event.PopupMenuEvent;
import javax.swing.event.PopupMenuListener;


public class GaugePanel
     extends JPanel
{
  GaugePanel instance = this;
  
  protected double value = 0d;
  private Color bottom = Color.white;
  private Color top    = new Color(128, 0, 0);
  private Color valueColor = Color.gray;
  
  protected Format valueFormat = new DecimalFormat("00.00 'kts'");
  protected double minValue = 70d;
  protected double maxValue =  0d;
  
  private double topValue    = 0d;
  private double bottomValue = 0d;
  
  protected int strOffset = 3;
  
  private MinMaxPanelInterface parent;
  
  public GaugePanel(MinMaxPanelInterface from,
                    double min,
                    double max,
                    Format fmt,
                    Color topColor, 
                    Color bottomColor,
                    Color valueColor)
  {
    this.parent = from;
    this.minValue = min;
    this.maxValue = max;
    this.valueFormat = fmt;
    this.top = topColor;
    this.bottom = bottomColor;
    this.valueColor = valueColor;
    
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
    this.setLayout( null );
    this.addMouseListener(new MouseAdapter()
                          {
                            public void mouseClicked(MouseEvent e)
                            {
                              // Right click: popup -> Reset
                              int mask = e.getModifiers();
                              // Right-click only (Actually: no left-click)
                              if ((mask & MouseEvent.BUTTON2_MASK) != 0 || (mask & MouseEvent.BUTTON3_MASK) != 0)
                              {
                                ResetPopup popup = new ResetPopup();
                                popup.show(instance, e.getX(), e.getY());
                              }
                            }
                          });
  }
  
  public void paintComponent(Graphics gr)
  {
    ((Graphics2D)gr).setRenderingHint(RenderingHints.KEY_TEXT_ANTIALIASING,
                                      RenderingHints.VALUE_TEXT_ANTIALIAS_ON);      
    ((Graphics2D)gr).setRenderingHint(RenderingHints.KEY_ANTIALIASING,
                                      RenderingHints.VALUE_ANTIALIAS_ON);      
    Graphics2D g2d = (Graphics2D)gr;
    int fontSize = gr.getFont().getSize();
    // Gauge background 
    paintBackGround(gr);
    // Data
    g2d.setColor(this.valueColor);
    
    drawMinMaxRange(g2d, bottomValue, topValue);
    
    plotValue(gr, topValue);
    plotValue(gr, bottomValue);

    double amplitude = (maxValue - minValue);
    int h = this.getHeight() - fontSize;    
    int y = (int)(h + (fontSize / 2) - ((value / (amplitude)) * h));
//  String str = this.valueFormat.format(value);
//  gr.drawString(str, strOffset, y + (gr.getFont().getSize() / 2));
    int strWidth  = 0; // gr.getFontMetrics(gr.getFont()).stringWidth(str);
    g2d.drawLine(strOffset + strWidth, y, this.getWidth(), y);    
  }

  public void paintBackGround(Graphics gr)
  {
    Graphics2D g2d = (Graphics2D)gr;
    // Gauge background 
    int h = this.getHeight();
    GradientPaint gradient = new GradientPaint(0, h, bottom, 0, 0, top);
    g2d.setPaint(gradient);
    g2d.fillRect(0, 
                 0, 
                 this.getWidth(), 
                 h);
  }

  private void plotValue(Graphics gr, double d)
  {
    int fontSize = gr.getFont().getSize();
    int h = this.getHeight() - fontSize;
    int y = (int)(h + (fontSize / 2) - ((d / (maxValue - minValue)) * h));
    gr.setColor(valueColor);
    Font f = gr.getFont();
    Font f2 = f.deriveFont(Font.BOLD);
    gr.setFont(f2);
    gr.drawString(valueFormat.format(d), 3, y + (gr.getFont().getSize() / 2));
    gr.setFont(f);
  }
  
  private void drawMinMaxRange(Graphics2D g2d, double min,double max)
  {
    // Transparency
    g2d.setComposite(AlphaComposite.getInstance(AlphaComposite.SRC_OVER, .45f));
    g2d.setColor(Color.gray);
       
    int fontSize = g2d.getFont().getSize();
    int h = this.getHeight() - fontSize;
    int y1 = (int)(h + (fontSize / 2) - ((min / (maxValue - minValue)) * h));
    int y2 = (int)(h + (fontSize / 2) - ((max / (maxValue - minValue)) * h));
//  g2d.drawRect(0, y2, this.getWidth(), (y1 - y2));
    g2d.fillRect(0, y2, this.getWidth(), (y1 - y2));    
    g2d.setComposite(AlphaComposite.getInstance(AlphaComposite.SRC_OVER, 1f));
  }
  
  public void setValue(double tws)
  {
    this.value = tws;
    this.repaint();
  }
  
  public void setValues(double tws, double top, double bottom)
  {
    this.value = tws;
    this.topValue = top;
    this.bottomValue = bottom;
    this.repaint();
  }
  
  public double getValue()
  {
    return value;
  }

  public void setBottom(Color bottom)
  {
    this.bottom = bottom;
  }

  public void setTop(Color top)
  {
    this.top = top;
  }

  public void setValueFormat(Format valueFormat)
  {
    this.valueFormat = valueFormat;
  }

  public Format getValueFormat()
  {
    return valueFormat;
  }

  public void setMinValue(double minValue)
  {
    this.minValue = minValue;
  }

  public double getMinValue()
  {
    return minValue;
  }

  public void setMaxValue(double maxValue)
  {
    this.maxValue = maxValue;
  }

  public double getMaxValue()
  {
    return maxValue;
  }

  class ResetPopup extends JPopupMenu
                implements ActionListener,
                           PopupMenuListener
  {
    JMenuItem reset;

    private final static String RESET = "Reset Min & Max";

    public ResetPopup()
    {
      super();
      this.add(reset = new JMenuItem(RESET));
      reset.addActionListener(this);
    }

    public void actionPerformed(ActionEvent event)
    {
      if (event.getActionCommand().equals(RESET))
      {
        parent.reset(); 
      }
    }

    public void popupMenuWillBecomeVisible(PopupMenuEvent e)
    {
    }

    public void popupMenuWillBecomeInvisible(PopupMenuEvent e)
    {
    }

    public void popupMenuCanceled(PopupMenuEvent e)
    {
    }
  }

}
