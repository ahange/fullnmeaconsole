package nmea.ui.viewer.elements;

import java.awt.BasicStroke;
import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.Font;
import java.awt.FontFormatException;
import java.awt.GradientPaint;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.Point;
import java.awt.RenderingHints;
import java.awt.Stroke;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;
import java.awt.event.MouseMotionListener;

import java.io.IOException;
import java.io.InputStream;

import java.text.DecimalFormat;

import java.text.SimpleDateFormat;

import java.util.ArrayList;
import java.util.Date;

import java.util.List;

import javax.swing.JPanel;

import javax.swing.ToolTipManager;

import nmea.server.ctx.NMEAContext;
import nmea.server.utils.Utils;

import nmea.event.NMEAReaderListener;

import nmea.server.constants.Constants;
import nmea.server.ctx.NMEADataCache;

import ocss.nmea.parser.Speed;


public class SpeedEvolutionDisplay
     extends JPanel  
  implements MouseMotionListener, MouseListener
{
  private Font digiFont = null;
  private Color displayColor = Color.green;

  private String toolTipText = null;

  private String name = "BSP";
  private transient List<DatedData> aldd   = null; // ArrayList of Dated Data (Damped)
  private transient List<DatedData> alnddd = null; // ArrayList of Not Damped Dated Data
  private transient DatedData mini = null, maxi = null; // Extrema
  
  private long maxDataLength = NMEAContext.DEFAULT_BUFFER_SIZE;
  private final static long MINIMUM_DELAY = 100L;
  
  private transient  double min = 0d, max = 0d; // Boundaries
  private double step = 1d;
  private String unit = "kts";
  private DecimalFormat df21 = new DecimalFormat("##0.0");
  private SimpleDateFormat justTime = new SimpleDateFormat("HH:mm:ss");
  
  private DataPanel dataPanel   = new DataPanel();
  private RangePanel rangePanel = new RangePanel();
  
  private boolean showRawData = true;

  public SpeedEvolutionDisplay(String name)
  {
    this.name = name;
    aldd   = new ArrayList<DatedData>();
    alnddd = new ArrayList<DatedData>();
  }

  public SpeedEvolutionDisplay(String name, String ttText)
  {
    this(name, ttText, 36);
  }

  public SpeedEvolutionDisplay(String name, String ttText, int basicSize)
  {
    this.name = name;
    toolTipText = ttText;
    this.jumboFontSize = basicSize;
    aldd   = new ArrayList<DatedData>();
    alnddd = new ArrayList<DatedData>();
    try
    {
      jbInit();
    }
    catch (Exception e)
    {
      e.printStackTrace();
    }
  }

  private int jumboFontSize = 36;

  private void jbInit()
    throws Exception
  {
    try
    {
      digiFont = tryToLoadFont("ds-digi.ttf", this);
    }
    catch (Exception ex)
    {
      System.err.println(ex.getMessage());
    }
    if (digiFont == null)
      digiFont = new Font("Courier New", Font.BOLD, jumboFontSize);
    else
      digiFont = digiFont.deriveFont(Font.BOLD, jumboFontSize);
    digiFont = loadDigiFont();
    this.setLayout(new BorderLayout());
    this.setBackground(Color.black);

//  resize(jumboFontSize);

    addMouseMotionListener(this);
    addMouseListener(this);

    this.setSize(new Dimension(600, 100));
    this.setPreferredSize(new Dimension(600, 100));
    if (toolTipText != null)
      this.setToolTipText(toolTipText);
    
    this.add(dataPanel, BorderLayout.CENTER);
    this.add(rangePanel, BorderLayout.EAST);

    NMEAContext.getInstance().addNMEAReaderListener(new NMEAReaderListener(Constants.NMEA_SERVER_LISTENER_GROUP_ID, "Speed Evolution")
    {
      public void dataBufferSizeChanged(int size) 
      {
        maxDataLength = size;
      }
      
      public void dampingHasChanged(int damping) 
      {
        System.out.println("Damping value is now " + damping + ", buffer has " + alnddd.size() + " point(s)");
      }
      
      public void showRawData(boolean b) 
      {
        showRawData = b;
      }
    });
    
    ToolTipManager.sharedInstance().setInitialDelay(0);
    ToolTipManager.sharedInstance().setDismissDelay(Integer.MAX_VALUE);
  }

  public void resize(int bigFontSize)
  {
    jumboFontSize = bigFontSize;
//    int width = (int) (120d * (double) jumboFontSize / 36d);
//    int height = (int) (65d * (double) jumboFontSize / 36d);
//    this.setSize(new Dimension(width, height));
//    this.setPreferredSize(new Dimension(width, height));
  }

  private Font loadDigiFont()
  {
    Font f = null;
    try
    {
      f = tryToLoadFont("ds-digi.ttf", this);
    }
    catch (Exception ex)
    {
      System.err.println(ex.getMessage());
    }
    if (f == null)
      f = new Font("Courier New", Font.BOLD, jumboFontSize);
    else
      f = f.deriveFont(Font.BOLD, jumboFontSize);
    return f;
  }

  public static Font tryToLoadFont(String fontName, Object parent)
  {
    final String RESOURCE_PATH = "resources" + "/"; // A slash! Not File.Separator, it is a URL.
    try
    {
      String fontRes = RESOURCE_PATH + fontName;
      InputStream fontDef = parent.getClass().getResourceAsStream(fontRes);
      if (fontDef == null)
      {
        throw new NullPointerException("Could not find font resource \"" + fontName + "\"\n\t\tin \"" + fontRes + "\"\n\t\tfor \"" + parent.getClass().getName() + "\"\n\t\ttry: " + parent.getClass().getResource(fontRes));
      }
      else
        return Font.createFont(Font.TRUETYPE_FONT, fontDef);
    }
    catch (FontFormatException e)
    {
      System.err.println("getting font " + fontName);
      e.printStackTrace();
    }
    catch (IOException e)
    {
      System.err.println("getting font " + fontName);
      e.printStackTrace();
    }
    return null;
  }

  public void setName(String s)
  {
    name = s;
  }
  
//private SimpleDateFormat sdf = new SimpleDateFormat("HH:mm:ss.SSS");

  public void addValue(Date date, double value)
  {
    synchronized (aldd)
    {
      if (!Double.isInfinite(value) && value != -Double.MAX_VALUE && (aldd.size() == 0 || (date.getTime() - aldd.get(aldd.size() - 1).getDate().getTime() > MINIMUM_DELAY)))
      {
//      if ("PERF".equals(this.name))
//        System.out.println("Adding value to PERF:" + date.toString() + " " + value);
        aldd.add(new DatedData(date, value));
      }
      while (aldd.size() > maxDataLength)
        aldd.remove(0);
    }
  }

  public void addNDValue(Date date, double value)
  {
    synchronized (alnddd)
    {
      if (!Double.isInfinite(value) && value != -Double.MAX_VALUE && (alnddd.size() == 0 || (date.getTime() - alnddd.get(alnddd.size() - 1).getDate().getTime() > MINIMUM_DELAY)))
      {
  //      if ("BSP".equals(this.name))
  //        System.out.println("Adding value to BSP:" + sdf.format(date));
        alnddd.add(new DatedData(date, value));
      }
      while (alnddd.size() > maxDataLength)
        alnddd.remove(0);
    }
  }

  public void setDisplayColor(Color c)
  {
    displayColor = c;
  }

  public void paintComponent(Graphics g) 
  {
    // super.paintComponent(g);
  }

  public void setMaxDataLength(long maxDataLength)
  {
    this.maxDataLength = maxDataLength;
  }

  public long getMaxDataLength()
  {
    return maxDataLength;
  }

  public void setMin(double min)
  {
    this.min = min;
  }

  public double getMin()
  {
    return min;
  }

  public void setMax(double max)
  {
    this.max = max;
  }

  public void setStep(double step)
  {
    this.step = step;
  }

  public void setUnit(String unit)
  {
    this.unit = unit;
  }

  public void setEditMask(DecimalFormat df21)
  {
    this.df21 = df21;
  }

  @Override
  public void mouseClicked(MouseEvent mouseEvent)
  {
    // TODO Implement this method
  }

  @Override
  public void mousePressed(MouseEvent mouseEvent)
  {
    // TODO Implement this method
  }

  @Override
  public void mouseReleased(MouseEvent mouseEvent)
  {
    // TODO Implement this method
  }

  @Override
  public void mouseEntered(MouseEvent mouseEvent)
  {
    // TODO Implement this method
  }

  @Override
  public void mouseExited(MouseEvent mouseEvent)
  {
    plotDot = null;
  }

  @Override
  public void mouseDragged(MouseEvent e)
  {
  }

  private PlotDot plotDot = null; 

  @Override
  public void mouseMoved(MouseEvent e)
  {
    Point mouse = e.getPoint();
    int width = dataPanel.getWidth();
    int arraySize = aldd.size();
    if (mouse.x <= width && arraySize > 0)
    {
      int index = (int)(((double)mouse.x / (double)width) * (double)arraySize);
      index -= 1;
      while (index >= aldd.size())
        index--;
      if (index == -1)
        return;
      
      Date date = aldd.get(index).getDate();
      boolean onMini = false, onMaxi = false;
      if (date.equals(mini.getDate()))
        onMini = true;
      if (date.equals(maxi.getDate()))
        onMaxi = true;
      
      String str = "<html>";
      if (!onMini && !onMaxi)
      {  
        str += (aldd.size() + " positions<br>");
        long range = aldd.get(aldd.size() - 1).getDate().getTime() - aldd.get(0).getDate().getTime();
        str += ("Range:" + Utils.setRange(range) + "<br>");
        str += ("From " + justTime.format(aldd.get(0).getDate()) + "<br>");
        str += ("To " + justTime.format(aldd.get(aldd.size() - 1).getDate()) + "<br>");
        str += ("<hr>");
      }
      if (mini != null && maxi != null)
      {
        if (!onMini && !onMaxi)
          str += ("Mini:<b><font color='red'>" + df21.format(mini.getValue()) + " " + unit + "</font></b> at " + justTime.format(mini.getDate()) + "<br>");
        else if (onMini)
          str += ("<b>Mini<b>:<b><font color='red'>" + df21.format(mini.getValue()) + " " + unit + "</font></b><br>at " + justTime.format(mini.getDate()));
        if (!onMini && !onMaxi)
          str += ("Maxi:<b><font color='red'>" + df21.format(maxi.getValue()) + " " + unit + "</font></b> at " + justTime.format(maxi.getDate()) + "<br>");
        else if (onMaxi)
          str += ("<b>Maxi</b>:<b><font color='red'>" + df21.format(maxi.getValue()) + " " + unit + "</font></b><br>at " + justTime.format(maxi.getDate()));
        if (!onMini && !onMaxi)
          str += "<hr>";
      }
      if (!onMini && !onMaxi)
      {
        str += ("index " + index + ":<b>" + justTime.format(date) + "</b>, Value:<font color='red'><b>" + df21.format(aldd.get(index).getValue()) + unit + "</b></font>");
        plotDot = new PlotDot();
        plotDot.time  = aldd.get(index).getDate();
        plotDot.value = aldd.get(index).getValue();        
      }
      str += "</html>";
      this.setToolTipText(str);
//    System.out.println("Array Size:" + aldd.size() + ", Pos:" + index);
    }
  }

  private class DatedData
  {
    private Date date;
    private double value;
    
    public DatedData(Date date, double d)
    {
      this.date = date;
      this.value = d;
    }

    public Date getDate()
    {
      return date;
    }

    public double getValue()
    {
      return value;
    }
  }

  private class DataPanel extends JPanel
  {
    public DataPanel()
    {
      super();
  //  this.setMinimumSize(new Dimension(400, 100));
      this.setPreferredSize(new Dimension(500, 100));
    }
    
    public void paintComponent(Graphics gr)
    {
      ((Graphics2D)gr).setRenderingHint(RenderingHints.KEY_TEXT_ANTIALIASING,
                                        RenderingHints.VALUE_TEXT_ANTIALIAS_ON);      
      ((Graphics2D)gr).setRenderingHint(RenderingHints.KEY_ANTIALIASING,
                                        RenderingHints.VALUE_ANTIALIAS_ON);      
      Color startColor = Color.black; // new Color(255, 255, 255);
      Color endColor = Color.gray; // new Color(102, 102, 102);
      GradientPaint gradient = new GradientPaint(0, this.getHeight(), startColor, 0, 0, endColor); // vertical, upside down
      ((Graphics2D) gr).setPaint(gradient);
      gr.fillRect(0, 0, this.getWidth(), this.getHeight());
      
      gr.setColor(displayColor);
      int small = (int) (20d * jumboFontSize / 36d);
      gr.setFont(digiFont.deriveFont(Font.BOLD, small));
      gr.drawString(name, 10, small + 5);
      // Draw Data Curve
      synchronized (aldd)
      {
        // 1 - Length
        long begin = 0L;
        long end   = 0L;
        if (aldd != null && aldd.size() > 1)
        {
          begin = aldd.get(0).getDate().getTime();
          end   = aldd.get(aldd.size() - 1).getDate().getTime();
        }
        // Min and max, for scale
        for (DatedData dd : alnddd) // Undamped ones
        {
          if (mini == null) mini = dd;
          if (maxi == null) maxi = dd;
          if (dd.getValue() < mini.getValue()) mini = dd;
          if (dd.getValue() > maxi.getValue()) maxi = dd;
        }
        if (maxi != null) 
          max = (1 + (int)(maxi.getValue() / 10)) * 10; // Setting the scale there
        // 2 - Grid and data
        long length = (end - begin);
        if (length > 0)
        {
          int w = this.getWidth();
          int h = this.getHeight();
          double stepH = ((double)w / (double)length);
          double stepV = ((double)h / (max - min));
          gr.setColor(new Color(0, 128, 64));
          // Grid
          for (double d=min; d<=max; d+=step)
          {
            int y = h - (int)((d - min) * stepV);
            gr.drawLine(0, y, w, y);
          }
          // Not Damped Data
          gr.setColor(Color.yellow);
          Point previous = null;
          
          if (showRawData)
          {
            for (DatedData dd : alnddd)
            {
              int x = (int)((dd.getDate().getTime() - begin) * stepH);
              int y = h - (int)((dd.getValue() - min) * stepV);
              Point p = new Point(x, y);
              if (previous != null)
                gr.drawLine(previous.x, previous.y, p.x, p.y);
              previous = p;
            }
          }          
          // Data
          gr.setColor(Color.red);
          Stroke origStroke = ((Graphics2D)gr).getStroke();
          Stroke stroke =  new BasicStroke(2, 
                                           BasicStroke.CAP_BUTT,
                                           BasicStroke.JOIN_BEVEL);
          ((Graphics2D)gr).setStroke(stroke);  
       /* Point */ previous = null;
          for (DatedData dd : aldd)
          {
//          if (mini == null) mini = dd;
//          if (maxi == null) maxi = dd;
//          if (dd.getValue() < mini.getValue()) mini = dd;
//          if (dd.getValue() > maxi.getValue()) maxi = dd;
            int x = (int)((dd.getDate().getTime() - begin) * stepH);
            int y = h - (int)((dd.getValue() - min) * stepV);
            Point p = new Point(x, y);
            if (previous != null)
              gr.drawLine(previous.x, previous.y, p.x, p.y);
            previous = p;
//          System.out.println(name + ":" + dd.getValue() + " " + unit);
          }
          ((Graphics2D)gr).setStroke(origStroke);  
          // Plot mini/maxi
          int x = (int)((mini.getDate().getTime() - begin) * stepH);
          gr.drawLine(x, 0, x, this.getHeight());
          x = (int)((maxi.getDate().getTime() - begin) * stepH);
          gr.drawLine(x, 0, x, this.getHeight());
          // Last value
          String str = df21.format(aldd.get(aldd.size() - 1).getValue()) + " " + unit;
          int strWidth  = gr.getFontMetrics(gr.getFont()).stringWidth(str);
          gr.setColor(Color.cyan);
          gr.drawString(str, this.getWidth() - strWidth - 2,  jumboFontSize + 2);
          if (plotDot != null)
          {
            int w_ = dataPanel.getWidth();
            double stepH_ = ((double)w_ / (double)length);
            int x_ = (int)((plotDot.time.getTime() - begin) * stepH_);
            int y_ = h - (int)((plotDot.value - min) * stepV);
            gr.fillOval(x_-3, y_-3, 6, 6);
            Stroke dotted = new BasicStroke(1f, BasicStroke.CAP_ROUND, BasicStroke.JOIN_ROUND, 1f, new float[] {3f}, 0f);
            ((Graphics2D)gr).setStroke(dotted);  
            gr.drawLine(x_, 0, x_, this.getHeight());
            ((Graphics2D)gr).setStroke(origStroke);  
          }
        }
        else
          gr.drawString("No Data...", 10, this.getHeight() - 20);
      }
    }
  }
  
  private class RangePanel extends JPanel
  {
    public RangePanel()
    {
      super();
      this.setPreferredSize(new Dimension(75, 200));
      this.setOpaque(false);
    }
    
    public void paintComponent(Graphics gr)
    {
      ((Graphics2D)gr).setRenderingHint(RenderingHints.KEY_TEXT_ANTIALIASING,
                                        RenderingHints.VALUE_TEXT_ANTIALIAS_ON);      
      ((Graphics2D)gr).setRenderingHint(RenderingHints.KEY_ANTIALIASING,
                                        RenderingHints.VALUE_ANTIALIAS_ON);      
      int fontSize = 10;
      int w = this.getWidth();
      int h = this.getHeight();
      double stepV = ((double)h / (max - min));
      Color c = gr.getColor();
      gr.setColor(this.getBackground());
      gr.fillRect(0, 0, w, h);
      gr.setColor(c);
//    gr.setColor(new Color(0, 128, 64));
      gr.setFont(gr.getFont().deriveFont(Font.PLAIN, fontSize));
      // Grid
      for (double d=min; d<=max; d+=step)
      {
        int y = h - (int)((d - min) * stepV);
        gr.drawLine(0, y, w/2, y);
        gr.drawString(df21.format(d) + " " +  unit, (w/2) + 2, y + (fontSize / 2));
      }
    }
  }
  
  private static class PlotDot
  {
    Date time = null;
    double value = 0D;
  }
}
