package nmea.ui.viewer.elements;

import java.awt.AlphaComposite;

import nmea.server.ctx.NMEAContext;

import nmea.server.ctx.NMEADataCache;

import nmea.server.utils.Utils;

import nmea.event.NMEAReaderListener;

import java.awt.BasicStroke;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.Font;
import java.awt.GradientPaint;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.Paint;
import java.awt.Point;
import java.awt.RenderingHints;
import java.awt.Shape;
import java.awt.Stroke;

import java.awt.font.TextAttribute;

import java.text.AttributedString;
import java.text.DecimalFormat;

import java.util.ArrayList;

import java.util.List;

import javax.swing.JPanel;

import nmea.server.constants.Constants;

import ocss.nmea.parser.Angle180;
import ocss.nmea.parser.Angle180EW;
import ocss.nmea.parser.Angle180LR;
import ocss.nmea.parser.Angle360;
import ocss.nmea.parser.Speed;

import ocss.nmea.parser.Temperature;
import ocss.nmea.parser.TrueWindDirection;
import ocss.nmea.parser.TrueWindSpeed;

/**
 * All computing done in this class
 */
public class DrawingBoard
  extends JPanel
{
  private boolean debug = System.getProperty("debug", "off").equals("on");
  
  private final DecimalFormat DF3  = new DecimalFormat("###");
  private final DecimalFormat DF32 = new DecimalFormat("##0.00");
  private final DecimalFormat DF33 = new DecimalFormat("##0.000");
  private final DecimalFormat DF31 = new DecimalFormat("##0.0");  

  private int boatPosX = 0, boatPosY = 0;

  private double aws = 10f, awa = -30f;

  private double bsp = 8D;
  private double hdg = 53D;

  private double sog = 7D;
  private double cog = 94D;
  
  private double vmg = 0d;
  private double b2wp = 0d;
  private String wpName = "";
  
  private double perf = -1;

  private double bspCoeff  = 1D;
  private double hdgOffset = 0D;
  private double awsCoeff  = 1D;
  private double awaOffset = 0D;
  private double maxLeeway = 10d;
  
  private double leeway = 0d;

  private double tws = 0, twa = 0, twd = 0;
  private double wt  = -Double.MAX_VALUE;
  
  private double speedScale      = 3d; // 10 knots
  private boolean displayCurrent = true;
  private boolean freeze         = false;
  private boolean showTemperature = false;
  private boolean showPerimeterTicks = true;
  
  private boolean showVMG = true;
  private boolean vmgWithBSP_HDG = true;
  private boolean vmgWithSOG_COG = true;
  
  public DrawingBoard()
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
    NMEAContext.getInstance().addNMEAReaderListener(new NMEAReaderListener(Constants.NMEA_SERVER_LISTENER_GROUP_ID, "Drawing Board")
      {
        public void dataUpdate() 
        {
          NMEADataCache cache = NMEAContext.getInstance().getCache();
          if (cache != null)
          {
            try { setHDG(((Angle360)cache.get(NMEADataCache.HDG_TRUE)).getValue()); } catch (Exception ex) {}
            try { setBSP(((Speed)cache.get(NMEADataCache.BSP)).getValue()); } catch (Exception ex) {}
            try { setSOG(((Speed)cache.get(NMEADataCache.SOG)).getValue()); } catch (Exception ex) {}
            try { setCOG(((Angle360)cache.get(NMEADataCache.COG)).getValue()); } catch (Exception ex) {}
            try { setAWA(((Angle180)cache.get(NMEADataCache.AWA)).getValue()); } catch (Exception ex) {}
            try { setAWS(((Speed)cache.get(NMEADataCache.AWS)).getValue()); } catch (Exception ex) {}

            try { setBSPCoeff(((Double)cache.get(NMEADataCache.BSP_FACTOR)).doubleValue()); } catch (Exception ex) {}
            try { setHDGOffset(((Double)cache.get(NMEADataCache.HDG_OFFSET)).doubleValue()); } catch (Exception ex) {}
            try { setAWSCoeff(((Double)cache.get(NMEADataCache.AWS_FACTOR)).doubleValue()); } catch (Exception ex) {}
            try { setAWAOffset(((Double)cache.get(NMEADataCache.AWA_OFFSET)).doubleValue()); } catch (Exception ex) {}
            try { setMaxLeeway(((Double)cache.get(NMEADataCache.MAX_LEEWAY)).doubleValue()); } catch (Exception ex) {}
            try { setWaterTemperature(((Temperature)cache.get(NMEADataCache.WATER_TEMP)).getValue()); } catch (Exception ex) {}
            
            repaint();
          }
        }
        
        public void setWindScale(float f) 
        {
          try 
          { 
            setSpeedScale((double)f); 
          } catch (Exception ex) {}
        }
      });
    this.setLayout(null);
    this.setBackground(Color.white);
  }

  public void paintComponent(Graphics gr)
  {    
    ((Graphics2D)gr).setRenderingHint(RenderingHints.KEY_TEXT_ANTIALIASING,
                                      RenderingHints.VALUE_TEXT_ANTIALIAS_ON);      
    ((Graphics2D)gr).setRenderingHint(RenderingHints.KEY_ANTIALIASING,
                                      RenderingHints.VALUE_ANTIALIAS_ON);      
    // The boat position, in the middle
    boatPosX = this.getWidth() / 2;
    boatPosY = this.getHeight() / 2;

    GradientPaint gradient = new GradientPaint(0, 0, 
                                               Color.white, 
                                               this.getWidth(), this.getHeight(), 
                                               new Color(225, 225, 225) /*Color.lightGray*/);
    if (false)
    {
      ((Graphics2D)gr).setPaint(gradient);
  //  gr.setColor(Color.white);  // Aha!   
      gr.fillRect(0, 0, this.getWidth(), this.getHeight());
    }
    if (true)
    {
      drawGlossyRectangularDisplay((Graphics2D) gr, 
                                   new Point(0, 0),
                                   new Point(this.getWidth(), this.getHeight()), 
                                   Color.black,              // Color.white, 
                                   new Color(225, 225, 225), // Color.lightGray, 
                                   1f,
                                   DOWN);      
    }

    double bspLengthAt10 = 1d * this.getWidth() / speedScale; // 3d;
    
    // Draw ticks on the perimeter
    if (showPerimeterTicks)
    {
      for (int d=0; d<360; d+=1)
      {
        if (d % 10 == 0)
          gr.setColor(Color.black);
        else
          gr.setColor(Color.gray);
        
        Point[] tick = getPerimeterTickCoordinates(d, this.getWidth(), this.getHeight(), 10);
        gr.drawLine(tick[0].x, tick[0].y, tick[1].x, tick[1].y);
      }
    }
    // Draw speed circles
    Stroke thickStroke    = new BasicStroke(5f, BasicStroke.CAP_ROUND, BasicStroke.JOIN_MITER);
    Stroke originalStroke = ((Graphics2D)gr).getStroke();

    double currentWindScale = 0d;
    for (NMEAContext.WindScale ws : NMEAContext.WindScale.values())
    {
      if (ws.scale() == speedScale)
      {
        currentWindScale = ws.speed();
        break;
      }
    }
    
    for (int w=1; w<=60; w++)
    {
      if ( w == (int)currentWindScale)
        ((Graphics2D)gr).setStroke(thickStroke);
      else
        ((Graphics2D)gr).setStroke(originalStroke);
      double radius = (w * (bspLengthAt10 / 10D));
      if (w % 5 == 0)
        gr.setColor(Color.gray);
      else
        gr.setColor(Color.lightGray);
        
      gr.drawOval((int)(boatPosX - radius),
                  (int)(boatPosY - radius),
                  (int)(2 * radius),
                  (int)(2 * radius));
    }
    // Draw axis
    gr.setColor(Color.lightGray);
    gr.drawLine(0, boatPosY, this.getWidth(), boatPosY);    
    gr.drawLine(boatPosX, 0, boatPosX, this.getHeight());    
    
    // CMG - Route Vraie
    double rvX = boatPosX + ((bsp * bspCoeff) * (bspLengthAt10 / 10D) * Math.sin(Math.toRadians(hdg + hdgOffset)));
    double rvY = boatPosY - ((bsp * bspCoeff) * (bspLengthAt10 / 10D) * Math.cos(Math.toRadians(hdg + hdgOffset)));

    // Thickness
    Stroke stroke = new BasicStroke(5f, BasicStroke.CAP_ROUND, BasicStroke.JOIN_MITER);
    ((Graphics2D) gr).setStroke(stroke);

    /*
     * Boat Speed and Heading
     */    
    stroke = new BasicStroke(2f, BasicStroke.CAP_ROUND, BasicStroke.JOIN_MITER);
    ((Graphics2D) gr).setStroke(stroke);
    gr.setColor(Color.red);
    if (debug || bsp > 0)
      Utils.drawHollowArrow((Graphics2D) gr, new Point(boatPosX, boatPosY), new Point((int) rvX, (int) rvY), Color.red);
    stroke = new BasicStroke(5f, BasicStroke.CAP_ROUND, BasicStroke.JOIN_MITER);
    ((Graphics2D) gr).setStroke(stroke);
    if (bsp > 0)
      gr.drawString("BSP:" + DF32.format(bsp * bspCoeff) + " kts, HDG:" + DF3.format((hdg + hdgOffset)) + "\272", (int) rvX + 5, (int) rvY + 5);
    /*
     * Leeway
     */
    if (freeze)
      leeway = Utils.getLeeway(awa + awaOffset, maxLeeway);
    else
    {
      try 
      {
        synchronized(NMEAContext.getInstance().getCache())
        {
          leeway = ((Angle180LR) NMEAContext.getInstance().getCache().get(NMEADataCache.LEEWAY)).getValue();
        }
      }
      catch (NullPointerException npe)
      {
        System.err.println("Leeway:" + npe.getMessage());
      }
    }
    double rsX = boatPosX + ((bsp * bspCoeff) * (bspLengthAt10 / 10D) * Math.sin(Math.toRadians(hdg + hdgOffset + leeway)));
    double rsY = boatPosY - ((bsp * bspCoeff) * (bspLengthAt10 / 10D) * Math.cos(Math.toRadians(hdg + hdgOffset + leeway)));
    if (debug || (Math.abs(leeway) > 0.1 && bsp > 0))
    {
      gr.setColor(Color.cyan);
      Utils.drawArrow((Graphics2D) gr, new Point(boatPosX, boatPosY), new Point((int) rsX, (int) rsY), Color.pink);
      gr.setColor(Color.blue);
//    gr.drawString("LWY:" + df22.format(leeway) + "\272", (int) rsX + 5, (int) rsY + 5);
      gr.drawString("CMG:" + DF32.format(hdg + hdgOffset + leeway) + "\272", (int) rsX + 5, (int) rsY + 5);
    }
    /*
     * SOG, COG
     * Route fond
     */
    double rfX = boatPosX + (sog * (bspLengthAt10 / 10D) * Math.sin(Math.toRadians(cog)));
    double rfY = boatPosY - (sog * (bspLengthAt10 / 10D) * Math.cos(Math.toRadians(cog)));
    if (debug || sog > 0)
    {
      gr.setColor(Color.magenta);
      Utils.drawArrow((Graphics2D) gr, new Point(boatPosX, boatPosY), new Point((int) rfX, (int) rfY), Color.magenta);
      gr.drawString("SOG:" + DF32.format(sog) + " kts, COG:" + DF3.format(cog) + "\272", (int) rfX + 5, (int) rfY + 5);
    }
    /*
     * AWA, AWS
     */
    double awX = boatPosX + ((aws * awsCoeff) * (bspLengthAt10 / 10D) * Math.sin(Math.toRadians(hdg + hdgOffset + (awa + awaOffset))));
    double awY = boatPosY - ((aws * awsCoeff) * (bspLengthAt10 / 10D) * Math.cos(Math.toRadians(hdg + hdgOffset + (awa + awaOffset))));
    if (debug || (sog > 0 && bsp > 0))
    {
      gr.setColor(Color.blue);
      Utils.drawAnemometerArrow((Graphics2D) gr, new Point((int) awX, (int) awY), new Point(boatPosX, boatPosY), Color.blue);
      gr.drawString("AWS:" + DF32.format(aws * awsCoeff) + " kts, AWA:" + DF3.format((awa + awaOffset)) + "\272", (int) awX + 5, (int) awY + 5);
    }
    /*
     * TWA, TWS
     */
    double vvX = awX + ((sog) * (bspLengthAt10 / 10D) * Math.sin(Math.toRadians(180 + cog)));
    double vvY = awY - ((sog) * (bspLengthAt10 / 10D) * Math.cos(Math.toRadians(180 + cog)));
    // Vent vitesse
    if (false && (debug || (bsp > 0 && cog > 0)))
    {
      gr.setColor(Color.pink);
      Utils.drawAnemometerArrow((Graphics2D) gr, new Point((int) awX, (int) awY), new Point((int) vvX, (int) vvY), Color.pink);
    }
    // Calculate TW with GPS
    if (freeze)
    {
      double[] twData = Utils.calculateTWwithGPS(aws, awsCoeff, awa, awaOffset, hdg, hdgOffset, sog, cog);
      twa = twData[0];
      tws = twData[1];
      twd = twData[2];
    }
    else
    {
      synchronized (NMEAContext.getInstance().getCache())
      {
        twa = ((Angle180) NMEAContext.getInstance().getCache().get(NMEADataCache.TWA)).getValue();      
        tws = ((TrueWindSpeed) NMEAContext.getInstance().getCache().get(NMEADataCache.TWS)).getValue();      
        twd = ((TrueWindDirection) NMEAContext.getInstance().getCache().get(NMEADataCache.TWD)).getValue();      
      }
    }
    double twX = boatPosX + (tws * (bspLengthAt10 / 10D) * Math.sin(Math.toRadians(twd)));
    double twY = boatPosY - (tws * (bspLengthAt10 / 10D) * Math.cos(Math.toRadians(twd)));
    
    // Vent vitesse
    if (true && (debug || (bsp > 0 && cog > 0)))
    {
      gr.setColor(Color.pink);
      Utils.drawAnemometerArrow((Graphics2D) gr, new Point((int) awX, (int) awY), new Point((int) twX, (int) twY), Color.pink);
    }
    
    gr.setColor(Color.black);
    Utils.drawAnemometerArrow((Graphics2D) gr, new Point((int) twX, (int) twY), new Point(boatPosX, boatPosY), Color.black);
    gr.drawString("TWS:" + DF32.format(tws) + " kts, TWA:" + DF3.format(twa) + "\272", (int) twX + 5, (int) twY + 5);

    /*
     * Current
     */
      double a = 10 * (rsX - rfX) / bspLengthAt10;
      double b = 10 * (rfY - rsY) / bspLengthAt10;
      double csp = Math.sqrt((a * a) + (b * b));
      double cdr = Utils.getDir((float) a, (float) b);
    if (rsX != -Double.MAX_VALUE && !Double.isInfinite(rsX) && !Double.isNaN(rsX) &&
        rsY != -Double.MAX_VALUE && !Double.isInfinite(rsY) && !Double.isNaN(rsY))
    {
      if (debug || (displayCurrent && csp > 0.2))
      {
        gr.setColor(Color.green);
        stroke = new BasicStroke(2f, BasicStroke.CAP_ROUND, BasicStroke.JOIN_MITER);
        ((Graphics2D) gr).setStroke(stroke);
        Utils.drawCurrentArrow((Graphics2D) gr, new Point((int) rsX, (int) rsY), new Point((int) rfX, (int) rfY), Color.green);
        stroke = new BasicStroke(5f, BasicStroke.CAP_ROUND, BasicStroke.JOIN_MITER);
        ((Graphics2D) gr).setStroke(stroke);
      }
    }
    // VMG ?
    if (showVMG)
    {
      double arrowEndX = 0d, arrowEndY = 0d;
      double onTheWindX = 0d, onTheWindY = 0d;
      
      if (this.wpName.trim().length() > 0)
      {
        // Draw bearing to Waypoint
        gr.setColor(Color.black);
        stroke = new BasicStroke(1f, BasicStroke.CAP_ROUND, BasicStroke.JOIN_MITER);
        ((Graphics2D) gr).setStroke(stroke);
        double radius = Math.min(this.getWidth() / 2, this.getHeight() / 2);
        double wpX = boatPosX + (radius * Math.sin(Math.toRadians(b2wp)));
        double wpY = boatPosY - (radius * Math.cos(Math.toRadians(b2wp)));
        Point wp = new Point((int)wpX, (int)wpY); 

        Utils.drawArrow((Graphics2D) gr, new Point(boatPosX, boatPosY), wp, Color.black);
        gr.drawString(this.wpName, (int) wpX + 5, (int) wpY + 5);        
      }
      
      if (vmgWithBSP_HDG && bsp > 0)
      {
        arrowEndX = boatPosX + ((bsp * bspCoeff) * (bspLengthAt10 / 10D) * Math.sin(Math.toRadians(hdg + hdgOffset)));
        arrowEndY = boatPosY - ((bsp * bspCoeff) * (bspLengthAt10 / 10D) * Math.cos(Math.toRadians(hdg + hdgOffset)));
      }
      else if (vmgWithSOG_COG && sog > 0)
      {
        arrowEndX = boatPosX + (sog * (bspLengthAt10 / 10D) * Math.sin(Math.toRadians(cog)));
        arrowEndY = boatPosY - (sog * (bspLengthAt10 / 10D) * Math.cos(Math.toRadians(cog)));
      }  
      
      if (this.wpName.trim().length() > 0)
      {
        onTheWindX = boatPosX + (vmg * (bspLengthAt10 / 10D) * Math.sin(Math.toRadians(b2wp)));
        onTheWindY = boatPosY - (vmg * (bspLengthAt10 / 10D) * Math.cos(Math.toRadians(b2wp)));
      }
      else
      {
        onTheWindX = boatPosX + (vmg * (bspLengthAt10 / 10D) * Math.sin(Math.toRadians(twd)));
        onTheWindY = boatPosY - (vmg * (bspLengthAt10 / 10D) * Math.cos(Math.toRadians(twd)));
      }
      if (vmg != 0.0)
      {
        float dash[] = {10.0f};
        stroke = new BasicStroke(2f, BasicStroke.CAP_ROUND, BasicStroke.JOIN_MITER, 10f, dash, 0f);
        ((Graphics2D) gr).setStroke(stroke);
        gr.setColor(Color.orange);
        gr.drawLine((int)onTheWindX, (int)onTheWindY, (int)arrowEndX, (int)arrowEndY);

        ((Graphics2D) gr).setStroke(new CompositeStroke(new BasicStroke(5f), new BasicStroke(0.5f)));      
        gr.setColor(Color.black);
        gr.drawLine(boatPosX, boatPosY, (int)onTheWindX, (int)onTheWindY);
  
        gr.setColor(Color.orange);
        stroke = new BasicStroke(4f, BasicStroke.CAP_ROUND, BasicStroke.JOIN_MITER, 10f, dash, 0f);
        ((Graphics2D) gr).setStroke(stroke);
        Utils.drawArrow((Graphics2D) gr, new Point(boatPosX, boatPosY), new Point((int)onTheWindX, (int)onTheWindY), Color.orange);
        gr.setColor(Color.darkGray);
        gr.drawString("VMG", (int) onTheWindX + 5, (int) onTheWindY + 5);        
      }
    }

    /*
     * Boat itself
     * 
     * TODO An image ? (mono, cata, tri)
     */
    stroke = new BasicStroke(2f, BasicStroke.CAP_ROUND, BasicStroke.JOIN_ROUND);
    ((Graphics2D) gr).setStroke(stroke);
    Utils.drawBoat((Graphics2D) gr, 
                   Color.blue, 
                   Color.gray,
                   Color.black,
                   new Point(boatPosX, boatPosY), 
                   50, 
                   (int) (hdg + hdgOffset),
                   0.75f);

    // Display All Data Values:
    List<Object[]> dataTable = new ArrayList<Object[]>();
    
    if (bsp != -Double.MAX_VALUE)
    {
      dataTable.add(new Object[] {"BSP (corrected)", DF32.format(bsp * bspCoeff) + " kts", new Color(0, 142, 0)});
    }
    Angle180EW dec = (Angle180EW) NMEAContext.getInstance().getCache().get(NMEADataCache.DECLINATION);
    if (dec.getValue() == -Double.MAX_VALUE)
      dec = (Angle180EW) NMEAContext.getInstance().getCache().get(NMEADataCache.DEFAULT_DECLINATION);
    String hdgMess = "";
    if (Utils.isHdtPresent())
    {
      hdgMess = /*"HDG (corrected) :" + */ DF31.format(hdg + hdgOffset) + "\272 (Using HDT)";
      dataTable.add(new Object[] {"HDG (corrected)", hdgMess, new Color(0, 142, 0) });
    }
    else
    {
      double d = ((Angle180EW) NMEAContext.getInstance().getCache().get(NMEADataCache.DEVIATION)).getValue();
      hdgMess = /*"HDG (corrected) :" +*/ DF31.format(hdg + hdgOffset) + "\272 (Decl.= " + 
                  dec.toFormattedString() +
                  ", dev.= " + 
                  ((Angle180EW) NMEAContext.getInstance().getCache().get(NMEADataCache.DEVIATION)).toFormattedString() +
                  ", Var.= " + ((d == -Double.MAX_VALUE)?"- ":(dec.getValue() >=0 ? "E " : "W ") + DF31.format(Math.abs(dec.getValue() + ((Angle180EW) NMEAContext.getInstance().getCache().get(NMEADataCache.DEVIATION)).getValue())) + "\272") +
                  ")";
      dataTable.add(new Object[] {"HDG (corrected)",  hdgMess, new Color(0, 142, 0) });
    }
    if (aws != -Double.MAX_VALUE)
    {
      dataTable.add(new Object[] { "AWS (corrected)", DF32.format(aws * awsCoeff) + " kts", new Color(0, 142, 0) });
    }
    dataTable.add(new Object[] { "AWA (corrected)", DF3.format(awa + awaOffset) + "\272", new Color(0, 142, 0) });
    dataTable.add(new Object[] { "BSP Coeff", DF33.format(bspCoeff), Color.red });
    dataTable.add(new Object[] { "HDG Offset", DF3.format(hdgOffset) + "\272", Color.red });
    dataTable.add(new Object[] { "AWS Coeff", DF33.format(awsCoeff), Color.red });
    dataTable.add(new Object[] { "AWA Offset", DF3.format(awaOffset) + "\272", Color.red });
    if (tws != -Double.MAX_VALUE && !Double.isInfinite(tws) && !Double.isNaN(tws))
    {
      dataTable.add(new Object[] { "TWS", DF32.format(tws) + " kts", Color.blue });
    }
    dataTable.add(new Object[] { "TWA", DF3.format(twa) + "\272", Color.blue });
    dataTable.add(new Object[] { "TWD", DF3.format(twd) + "\272", Color.blue });
    if (cdr != -Double.MAX_VALUE && !Double.isInfinite(cdr) && !Double.isNaN(cdr))
    {
      dataTable.add(new Object[] { "CDR", DF3.format(cdr) + "\272", Color.blue });
    }
    if (csp != -Double.MAX_VALUE && !Double.isInfinite(csp) && !Double.isNaN(csp))
    {
      dataTable.add(new Object[] { "CSP", DF32.format(csp) + " kts", Color.blue });
    }
    dataTable.add(new Object[] { "leeway", DF31.format(leeway) + "\272 (on " + DF31.format(maxLeeway) + "\272)", Color.blue });
    dataTable.add(new Object[] { "CMG", DF31.format(hdg + hdgOffset + leeway) + "\272", Color.blue });
    // Now displaying data
    Font f = gr.getFont();
    gr.setFont(f.deriveFont(Font.BOLD));
    int y = drawDataTable(dataTable, gr);
    gr.setFont(f);
    
    // Performance
    if (this.perf != -1)
    {
      gr.setFont(f.deriveFont(Font.ITALIC, 24f));
      gr.setColor(Color.darkGray);
      gr.drawString("Perf: " + DF3.format(this.perf * 100d) + "%", 14, y + 24);
      gr.setFont(f);
    }
    
    // Leeway indicator
    if (Math.abs(leeway) > 0) // Indicator not shown if no leeway
    {
      int leewayFrameHeight = 100;  
      int leewayFrameWidth  =  50;  
      Dimension dim = this.getSize();
      // Draw recatngle
      gr.setColor(Color.blue);
      gr.drawRect(10, dim.height - leewayFrameHeight - 10, leewayFrameWidth, leewayFrameHeight);
      // Fill it
      Paint paint = ((Graphics2D)gr).getPaint();
      if (false)
      {
        Color startColor = Color.black;     // new Color(255, 255, 255);
        Color endColor   = Color.lightGray; // new Color(102, 102, 102);
        GradientPaint grad = new GradientPaint(0, this.getHeight(), startColor, 0, 0, endColor); // vertical, upside down
        ((Graphics2D)gr).setPaint(grad);
        gr.fillRect(10, dim.height - leewayFrameHeight - 10, leewayFrameWidth, leewayFrameHeight);
      }
      if (true)
      {
        Point topLeft     = new Point(10, dim.height - leewayFrameHeight - 10);
        Point bottomRight = new Point(topLeft.x + leewayFrameWidth, topLeft.y + leewayFrameHeight);
        drawGlossyRectangularDisplay((Graphics2D)gr, 
                                     topLeft, 
                                     bottomRight, 
                                     Color.lightGray, 
                                     Color.black, 
                                     1f); 
      }
      ((Graphics2D)gr).setPaint(paint); // reset
      // Value
      ((Graphics2D)gr).setComposite(AlphaComposite.getInstance(AlphaComposite.SRC_OVER, 0.70f));
      if (leeway > 0) // Starboard
        gr.setColor(Color.green);
      else            // Port
        gr.setColor(Color.red);
      gr.fillArc(10 - (leewayFrameWidth / 2), 
                 dim.height - leewayFrameHeight, 
                 Math.max(leewayFrameHeight, leewayFrameWidth), 
                 Math.max(leewayFrameHeight, leewayFrameWidth), 
                 90, 
                 -(int)Math.round(leeway));
      ((Graphics2D)gr).setComposite(AlphaComposite.getInstance(AlphaComposite.SRC_OVER, 1f));
      // Label
      gr.setColor(Color.cyan);
      String str = "Leeway";
      int l = gr.getFontMetrics(gr.getFont()).stringWidth(str);
      gr.drawString(str, (10 + (leewayFrameWidth / 2)) - (l / 2), dim.height - 30);
      str = DF31.format(leeway) + "\272";
      l = gr.getFontMetrics(gr.getFont()).stringWidth(str);
      gr.drawString(str, (10 + (leewayFrameWidth / 2)) - (l / 2), dim.height - 18);      
    }
    
    if (showTemperature)
    {
      gr.setColor(Color.black);
      int midThermometer = this.getWidth() - (6 * tubeWidth);
      if (wt != -Double.MAX_VALUE)
      {
        stroke = new BasicStroke(1f, BasicStroke.CAP_ROUND, BasicStroke.JOIN_MITER);
        ((Graphics2D) gr).setStroke(stroke);
        int fontSize = gr.getFont().getSize();
        // Graduation 
        for (int i=bottomTemperature; i<=topTemperature; i+=5)
        {
          int _y = getTemperatureYValue(i);
          gr.drawLine(midThermometer - tubeWidth, 
                     _y, 
                     midThermometer + tubeWidth, 
                     _y);
          gr.drawString(Integer.toString(i) + "\272C", (int)(midThermometer + (1.5 * tubeWidth)), _y + (fontSize / 2));
        }
        // Tube
        int tubeTop    = topTemperature + 2;
        int tubeBottom = bottomTemperature - 2;
        gr.drawLine(midThermometer - (tubeWidth / 2),
                   getTemperatureYValue(tubeTop),
                   midThermometer - (tubeWidth / 2),
                   getTemperatureYValue(tubeBottom));
        gr.drawLine(midThermometer + (tubeWidth / 2),
                   getTemperatureYValue(tubeTop),
                   midThermometer + (tubeWidth / 2),
                   getTemperatureYValue(tubeBottom));
        gr.drawArc(midThermometer - (tubeWidth / 2),
                  getTemperatureYValue(tubeTop) - (tubeWidth / 2),
                  tubeWidth, 
                  tubeWidth, 
                  180, 
                  -180);
        gr.drawOval(midThermometer - tubeWidth, 
                   getTemperatureYValue(tubeBottom),
                   tubeWidth * 2,
                   tubeWidth * 2);
        ((Graphics2D)gr).setComposite(AlphaComposite.getInstance(AlphaComposite.SRC_OVER, 0.75f));
        gradient = new GradientPaint(midThermometer - (tubeWidth / 2), 
                                     getTemperatureYValue(wt), 
                                     Color.red, 
                                     midThermometer - (tubeWidth / 2),
                                     getTemperatureYValue(tubeBottom - 1), 
                                     new Color(102, 0, 0)); // Vertical
        ((Graphics2D)gr).setPaint(gradient);
        // Value
        gr.fillRect(midThermometer - (tubeWidth / 2) + 2,
                   getTemperatureYValue(wt),
                   tubeWidth - 4,
                   getTemperatureYValue(tubeBottom - 1) - getTemperatureYValue(wt));
        // Ampoule en bas
        gr.fillOval(midThermometer - (tubeWidth - 2), 
                   getTemperatureYValue(tubeBottom) + 2,
                   (tubeWidth - 2) * 2,
                   (tubeWidth - 2) * 2);
        ((Graphics2D)gr).setComposite(AlphaComposite.getInstance(AlphaComposite.SRC_OVER, 1f));
        gr.setColor(Color.red);
        String value = DF31.format(wt) + "\272C";
     /* Font */ f = gr.getFont();
        gr.setFont(f.deriveFont(Font.BOLD));
        int l = gr.getFontMetrics(gr.getFont()).stringWidth(value);
        gr.drawString(value, midThermometer - (l / 2), this.getHeight() - 10);
        gr.setFont(f);
      }
    }
  }

  private Point[] getPerimeterTickCoordinates(double _angle, int _w, int _h, int borderThickness)
  {
    int w = _w;
    int h = _h;
    double angle = _angle % 90.0;
    double xOffset = Math.tan(Math.toRadians(angle)) * (h / 2);
    if (xOffset > w / 2)
      xOffset = w / 2; 
    if (xOffset < -w / 2)
      xOffset = -w / 2;
    if (Math.cos(Math.toRadians(_angle)) >= 0)
      xOffset = Math.abs(xOffset);
    else
      xOffset = -Math.abs(xOffset);
    
    double yOffset = (1d / Math.tan(Math.toRadians(angle))) * (w / 2);
    if (yOffset > h / 2)
      yOffset = h / 2;
    if (yOffset < -h / 2)
      yOffset = -h / 2;
    if (Math.sin(Math.toRadians(_angle)) >= 0)
      yOffset = Math.abs(yOffset);
    else
      yOffset = -Math.abs(yOffset);
    
    Point from = new Point((_w / 2) + (int)Math.round(xOffset), (_h / 2) - (int)Math.round(yOffset));
    
    w -= (2 * borderThickness);
    h -= (2 * borderThickness);
    xOffset = Math.tan(Math.toRadians(angle)) * (h / 2);
    if (xOffset > w / 2)
      xOffset = w / 2; 
    if (xOffset < -w / 2)
      xOffset = -w / 2;
    if (Math.cos(Math.toRadians(_angle)) >= 0)
      xOffset = Math.abs(xOffset);
    else
      xOffset = -Math.abs(xOffset);
    
    yOffset = (1d / Math.tan(Math.toRadians(angle))) * (w / 2);
    if (yOffset > h / 2)
      yOffset = h / 2;
    if (yOffset < -h / 2)
      yOffset = -h / 2;
    if (Math.sin(Math.toRadians(_angle)) >= 0)
      yOffset = Math.abs(yOffset);
    else
      yOffset = -Math.abs(yOffset);

    Point to = new Point((_w / 2) + (int)Math.round(xOffset), (_h / 2) - (int)Math.round(yOffset));
    
    return new Point[] { from, to };
  }

  private int drawDataTable(List<Object[]> data, Graphics gr)
  {
    int x = 14;
    // Determine biggest title width
    int maxLen = 0;
    for (Object[] sa : data)
    {
      String title = (String)sa[0];
      int len = gr.getFontMetrics(gr.getFont()).stringWidth(title);
      maxLen = Math.max(len, maxLen);
    }
    Font f = gr.getFont();
    int fontSize = f.getSize();
    int y = fontSize * 2;
    for (Object[] sa : data)
    {
      AttributedString asLeft  = new AttributedString((String)sa[0]);
      AttributedString asRight = new AttributedString((String)sa[1]);
      asLeft.addAttribute(TextAttribute.FONT, f.deriveFont(f.BOLD));
      asRight.addAttribute(TextAttribute.FONT, f.deriveFont(f.BOLD));
//    asLeft.addAttribute(TextAttribute.BACKGROUND, Color.WHITE);
//    asRight.addAttribute(TextAttribute.BACKGROUND, Color.WHITE);
      
      gr.setColor((Color)sa[2]);
      gr.drawString(asLeft.getIterator(), x, y);
      gr.drawString(asRight.getIterator(), maxLen + 5 + x, y);
      y += (fontSize + 2);
    }
    return y;
  }
  
  private final static int tubeWidth         = 10;
  private final static int bottomTemperature = -5;
  private final static int topTemperature    = 30;
  private final static int margin            = 60;

  private int getTemperatureYValue(double d)
  {
    int y = 0;
    int height = this.getHeight() - (2 * margin);
    int ampl = topTemperature - bottomTemperature;
    double absY = (double)(d - bottomTemperature) / (double)ampl;
    absY = absY * (double)height;
    y = margin + (height - (int)absY);
    
    return y;
  }

  private void setBSP(double bsp)
  {
    if (!freeze)
    {
      this.bsp = bsp;
//    this.repaint();
    }
  }

  private void setHDG(double hdg)
  {
    if (!freeze)
    {
      this.hdg = hdg;
//    this.repaint();
    }
  }

  private void setSOG(double sog)
  {
    if (!freeze)
    {
      this.sog = sog;
//    this.repaint();
    }
  }

  private void setCOG(double hdg)
  {
    if (!freeze)
    {
      this.cog = hdg;
//    this.repaint();
    }
  }

  private void setAWS(double aws)
  {
    if (!freeze)
    {
      this.aws = aws;
//    this.repaint();
    }
  }

  private void setAWA(double awa)
  {
    if (!freeze)
    {    
      this.awa = awa;
//    this.repaint();
    }
  }

  private void setBSPCoeff(double d)
  {
    this.bspCoeff = d;
//  this.repaint();
  }

  private void setHDGOffset(double d)
  {
    this.hdgOffset = d;
//  this.repaint();
  }

  private void setAWSCoeff(double d)
  {
    this.awsCoeff = d;
//  this.repaint();
  }

  private void setAWAOffset(double d)
  {
    this.awaOffset = d;
//  this.repaint();
  }

  private void setMaxLeeway(double d)
  {
    this.maxLeeway = d;
//  this.repaint();
  }

  private void setWaterTemperature(double t)
  {
    this.wt = t;
  }
  
  private void setSpeedScale(double windScale)
  {
    this.speedScale = windScale;
//  repaint();
  }

  public void setDisplayCurrent(boolean displayCurrent)
  {
    this.displayCurrent = displayCurrent;
//  repaint();
  }

  public final static int NO_VMG           = 0;
  public final static int VMG_WITH_BSP_HDG = 1;
  public final static int VMG_WITH_SOG_COG = 2;
  
  public void setVMGOption(int option)
  {
    switch (option)
    {
      case NO_VMG:
        showVMG = false;
        break;
      case VMG_WITH_BSP_HDG:
        vmgWithBSP_HDG = true;
        vmgWithSOG_COG = false;
        showVMG = true;
        break;
      case VMG_WITH_SOG_COG:
        vmgWithBSP_HDG = false;
        vmgWithSOG_COG = true;
        showVMG = true;
        break;
    }
  }
  
  public void setVMGValue(double d)
  {
    this.vmg = d;
    this.b2wp = 0d;
    this.wpName = "";
  }
  
  public void setPerformance(double d)
  {
    this.perf = d;
  }

  public void setVMGValue(double d, String wpName, double bearingToWaypoint)
  {
    this.vmg = d;
    this.b2wp = bearingToWaypoint;
    this.wpName = wpName;
  }
  
  public boolean isDisplayCurrent()
  {
    return displayCurrent;  
  }

  public void setFreeze(boolean freeze)
  {
    this.freeze = freeze;
  }

  public void setShowTemperature(boolean showTemperature)
  {
    this.showTemperature = showTemperature;
  }

  private final static int UP   = 0;
  private final static int DOWN = 1;
  private static void drawGlossyRectangularDisplay(Graphics2D g2d, 
                                                   Point topLeft, 
                                                   Point bottomRight, 
                                                   Color lightColor, 
                                                   Color darkColor, 
                                                   float transparency)
  {
    drawGlossyRectangularDisplay(g2d, topLeft, bottomRight, lightColor, darkColor, transparency, UP);
  }
  private static void drawGlossyRectangularDisplay(Graphics2D g2d, 
                                                   Point topLeft, 
                                                   Point bottomRight, 
                                                   Color lightColor, 
                                                   Color darkColor, 
                                                   float transparency,
                                                   int orientation)
  {
    g2d.setComposite(AlphaComposite.getInstance(AlphaComposite.SRC_OVER, transparency));
    g2d.setPaint(null);

    g2d.setColor(darkColor);

    int width  = bottomRight.x - topLeft.x;
    int height = bottomRight.y - topLeft.y;

    g2d.fillRoundRect(topLeft.x , topLeft.y, width, height, 10, 10);

    GradientPaint gradient = null;
    if (orientation == UP)
    {
      Point gradientOrigin = new Point(topLeft.x + (width) / 2,
                                       topLeft.y);
      gradient = new GradientPaint(gradientOrigin.x, 
                                   gradientOrigin.y, 
                                   lightColor, 
                                   gradientOrigin.x, 
                                   gradientOrigin.y + (height / 3), 
                                   darkColor); // vertical, light on top
    }
    else if (orientation == DOWN)
    {
      Point gradientOrigin = new Point(bottomRight.x + (width) / 2,
                                       bottomRight.y);
      gradient = new GradientPaint(gradientOrigin.x, 
                                   gradientOrigin.y, 
                                   lightColor, 
                                   gradientOrigin.x, 
                                   gradientOrigin.y - (height / 3), 
                                   darkColor); // vertical, light on top
    }
    else
      throw new RuntimeException("Illegal Orientation. Must be UP or DOWN.");
    
    g2d.setPaint(gradient);
    int offset = 3;
    int arcRadius = 5;
    g2d.fillRoundRect(topLeft.x + offset, topLeft.y + offset, (width - (2 * offset)), (height - (2 * offset)), 2 * arcRadius, 2 * arcRadius); 
  }

  public void setShowPerimeterTicks(boolean showPerimeterTicks)
  {
    this.showPerimeterTicks = showPerimeterTicks;
  }

  public class CompositeStroke
    implements Stroke
  {
    private Stroke stroke1, stroke2;

    public CompositeStroke(Stroke stroke1, Stroke stroke2)
    {
      this.stroke1 = stroke1;
      this.stroke2 = stroke2;
    }

    public Shape createStrokedShape(Shape shape)
    {
      return stroke2.createStrokedShape(stroke1.createStrokedShape(shape));
    }
  }
}
