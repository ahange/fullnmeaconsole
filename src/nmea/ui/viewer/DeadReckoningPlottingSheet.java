package nmea.ui.viewer;


import astro.calc.GeoPoint;
import astro.calc.GreatCircle;

import chart.components.ui.ChartPanelParentInterface;
import chart.components.ui.PlottingSheet;
import chart.components.util.MercatorUtil;

import java.awt.BasicStroke;
import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.Font;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Insets;
import java.awt.Point;
import java.awt.Stroke;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

import java.text.SimpleDateFormat;

import java.util.ArrayList;
import java.util.Date;
import java.util.EventObject;

import java.util.List;

import java.util.Map;

import javax.swing.BorderFactory;
import javax.swing.JButton;
import javax.swing.JCheckBox;
import javax.swing.JComboBox;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;

import javax.swing.border.BevelBorder;

import nmea.server.ctx.NMEAContext;
import nmea.server.ctx.NMEADataCache;
import nmea.server.utils.Utils;

import nmea.event.NMEAReaderListener;

import nmea.ui.viewer.elements.CurrentDisplay;

import ocss.nmea.parser.Angle360;
import ocss.nmea.parser.GeoPos;
import ocss.nmea.parser.Speed;
import ocss.nmea.parser.UTCDate;

import ocss.nmea.parser.UTCHolder;
import ocss.nmea.parser.UTCTime;

public class DeadReckoningPlottingSheet
  extends JPanel
  implements ChartPanelParentInterface
{
  private final static boolean DEBUG = false;
  
  private double minLatSpan = 0.001d;
  
  private PlottingSheet plottingSheet;
  private JPanel        topPanel;
  private JComboBox     timeComboBox;
  private JButton       resetButton;
  private JPanel centerPanel = new JPanel(new BorderLayout());
  private JPanel bottomPanel = new JPanel();
  private CurrentDisplay currentDisplay = new CurrentDisplay("Current", "00.00", "Current", 30);
  
  private GeoPoint[] groundData = null;
  private GeoPoint[] drData     = null;

  private int width  = 400;
  private int height = 400;
  private double centerLat = 0d;
  private double centerLng = 0d;
  private double latSpan   = 1d;
  
  private long bufferLength = 60000L;
  
  // Time, Position, CMG, BSP.
  private List<UTCHolder> timeBuffer    = new ArrayList<UTCHolder>();
  private List<GeoPos> positionBuffer   = new ArrayList<GeoPos>();
  private List<Angle360> cmgBuffer      = new ArrayList<Angle360>();
  private List<Angle360> hdgBuffer      = new ArrayList<Angle360>();
  private List<Speed> bspBuffer         = new ArrayList<Speed>();
  private JPanel boundariesPanel = new JPanel();
  private GridBagLayout gridBagLayout1 = new GridBagLayout();
  private GridBagLayout gridBagLayout2 = new GridBagLayout();
  private JLabel fromLabel = new JLabel();
  private JLabel toLabel   = new JLabel();
  
  private final static SimpleDateFormat SDF = new SimpleDateFormat("HH:mm:ss");
  private final static SimpleDateFormat SDF2 = new SimpleDateFormat("HH:mm:ss.SSS");
  private JLabel jLabel1 = new JLabel();
  private JLabel jLabel2 = new JLabel();
  
  private long timeStep = 0L;
  private JCheckBox smoothCheckBox = new JCheckBox();
  /**
   *
   * @param w Panel width
   * @param h Panel height
   * @param cL Center Latitude
   * @param cG Center Longitude
   * @param ls Latitude Span
   * @param defaultTimeStep Time step for calculation
   */
  public DeadReckoningPlottingSheet(int w, 
                                    int h, 
                                    double cL, 
                                    double cG, 
                                    double ls,
                                    long defaultTimeStep)
  {
    super();
    this.width = w;
    this.height = h;
    this.centerLat = cL;
    this.centerLng = cG;
    this.latSpan = ls;
    this.timeStep = defaultTimeStep;
    
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
    plottingSheet = new PlottingSheet(this, width, height, centerLat, centerLng, latSpan);    
    plottingSheet.setWithDistanceScale(true);

    centerPanel.setBorder(BorderFactory.createBevelBorder(BevelBorder.LOWERED));
    bottomPanel.setLayout(gridBagLayout1);
    topPanel = new JPanel();
    this.setLayout(new BorderLayout());
    this.setSize(new Dimension(400, 536));
    centerPanel.add(plottingSheet, BorderLayout.CENTER);
    this.add(centerPanel, BorderLayout.CENTER);
    timeComboBox = new JComboBox();
    timeComboBox.removeAllItems();
    timeComboBox.addItem(new TimeObject(  60000L,  "1 minute"));
    timeComboBox.addItem(new TimeObject( 120000L,  "2 minutes"));
    timeComboBox.addItem(new TimeObject( 300000L,  "5 minutes"));
    timeComboBox.addItem(new TimeObject( 600000L, "10 minutes"));
    timeComboBox.addItem(new TimeObject(1200000L, "20 minutes"));
    timeComboBox.addItem(new TimeObject(1800000L, "30 minutes"));
    timeComboBox.addItem(new TimeObject(3600000L,  "1 hour"));
    timeComboBox.addActionListener(new ActionListener()
      {
        public void actionPerformed(ActionEvent e)
        {
          setBufferLength(((TimeObject)timeComboBox.getSelectedItem()).getTime());
        }
      });    
    for (int i=0; i<timeComboBox.getItemCount(); i++) // Set default selection
    {
      if (((TimeObject)timeComboBox.getItemAt(i)).getTime() == this.timeStep)
      {
        timeComboBox.setSelectedIndex(i);
        setBufferLength(((TimeObject)timeComboBox.getItemAt(i)).getTime());
        break;
      }
    }
    resetButton = new JButton("Reset");
    resetButton.addActionListener(new ActionListener()
     {
        public void actionPerformed(ActionEvent e)
        {
          resetDataBuffers();
        }
      });
//  currentDisplay.setBorder(BorderFactory.createBevelBorder(BevelBorder.LOWERED));
    topPanel.add(timeComboBox, null);
    topPanel.add(resetButton, null);

    topPanel.add(smoothCheckBox, null);
    this.add(topPanel, BorderLayout.NORTH);

    currentDisplay.setDisplayColor(Color.cyan);
    boundariesPanel.setLayout(gridBagLayout2);
    fromLabel.setText("From...");
    toLabel.setText("To...");
    jLabel1.setText("From");
    jLabel2.setText("Until");
    smoothCheckBox.setText("smooth");
    bottomPanel.add(currentDisplay,
                    new GridBagConstraints(0, 0, 1, 1, 1.0, 1.0, GridBagConstraints.CENTER, GridBagConstraints.NONE,
                                           new Insets(0, 0, 0, 0), 0, 0));
    boundariesPanel.add(fromLabel,
                        new GridBagConstraints(1, 0, 1, 1, 0.0, 0.0, GridBagConstraints.WEST, GridBagConstraints.NONE,
                                               new Insets(0, 0, 0, 0), 0, 0));
    boundariesPanel.add(toLabel,
                        new GridBagConstraints(1, 1, 1, 1, 0.0, 0.0, GridBagConstraints.WEST, GridBagConstraints.NONE, new Insets(0,
                                                                                                                                  0,
                                                                                                                                  0,
                                                                                                                                  0),
                                               0, 0));
    boundariesPanel.add(jLabel1,
                        new GridBagConstraints(0, 0, 1, 1, 0.0, 0.0, GridBagConstraints.WEST, GridBagConstraints.NONE, new Insets(0,
                                                                                                                                  0,
                                                                                                                                  0,
                                                                                                                                  5),
                                               0, 0));
    boundariesPanel.add(jLabel2,
                        new GridBagConstraints(0, 1, 1, 1, 0.0, 0.0, GridBagConstraints.WEST, GridBagConstraints.NONE, new Insets(0,
                                                                                                                                  0,
                                                                                                                                  0,
                                                                                                                                  5),
                                               0, 0));
    bottomPanel.add(boundariesPanel,
                    new GridBagConstraints(1, 0, 1, 1, 1.0, 1.0, GridBagConstraints.CENTER, GridBagConstraints.BOTH,
                                           new Insets(0, 0, 0, 0), 0, 0));
    this.add(bottomPanel, BorderLayout.SOUTH);
    NMEAContext.getInstance().addNMEAReaderListener(new NMEAReaderListener("DR", "PlotSheet")
      {
        public void dataUpdate() 
        {
          NMEADataCache cache = NMEAContext.getInstance().getCache();
          if (cache != null)
          {
            try 
            { 
//            long time       = new Date().getTime();
              Object ot = (UTCDate)cache.get(NMEADataCache.GPS_DATE_TIME);
              if (ot == null)
              {
                ot = (UTCTime)cache.get(NMEADataCache.GPS_TIME);
                if (DEBUG) System.out.println("Time from NMEADataCache.GPS_TIME");
              }
              else if (DEBUG)
                System.out.println("Time from NMEADataCache.GPS_DATE_TIME");
                
              UTCHolder utcDate = null;
              if (ot instanceof UTCDate)
                utcDate = new UTCHolder((UTCDate)ot);
              else
                utcDate = new UTCHolder((UTCTime)ot);
              
              Angle360 cmg    = null;
              GeoPos position = null;
              Speed bsp       = null;
              Angle360 hdg    = null;
              synchronized (cache)
              {
                cmg    = (Angle360)cache.get(NMEADataCache.CMG); 
                position = (GeoPos)cache.get(NMEADataCache.POSITION);
                bsp       = (Speed)cache.get(NMEADataCache.BSP); 
                hdg    = (Angle360)cache.get(NMEADataCache.HDG_TRUE); 
              }
              // From a file: reset?
//            if (timeBuffer.size() > 1 && ((timeBuffer.get(timeBuffer.size() - 1).getValue().getTime() > utcDate.getValue().getTime())))
              if (timeBuffer != null &&
                  timeBuffer.size() > 1 && 
                  timeBuffer.get(timeBuffer.size() - 1) != null && 
                  !timeBuffer.get(timeBuffer.size() - 1).isNull() && 
                  utcDate != null && 
                  !utcDate.isNull() && 
                  utcDate.getValue() != null &&
                  ((timeBuffer.get(timeBuffer.size() - 1).getValue().getTime() - utcDate.getValue().getTime()) > 1000 ))
              {
                // Buffer Reset
                System.out.println("== Reseting data buffers: last date in buffer=[" + SDF2.format(timeBuffer.get(timeBuffer.size() - 1).getValue()) + "] > current Date=[" + SDF2.format(utcDate.getValue()) + "]");
                resetDataBuffers();
              }
              
              if (timeBuffer != null && 
                  utcDate != null && 
                  !utcDate.isNull() && 
                  utcDate.getValue() != null &&
                  (timeBuffer.size() == 0 || 
                   (timeBuffer.size() > 0 && 
                    timeBuffer.get(timeBuffer.size() - 1).getValue() != null && 
                    (timeBuffer.get(timeBuffer.size() - 1).getValue().getTime() < utcDate.getValue().getTime()))))
              {
                if (utcDate != null && cmg != null && position != null && bsp != null && hdg != null)
                {
                  if (timeBuffer.size() > 0)
                  {
                    UTCHolder oldest = timeBuffer.get(0);
                    boolean keepGoing = true;
                    
                    while (keepGoing && oldest.getValue().getTime() < (utcDate.getValue().getTime() - bufferLength))
                    {
                      timeBuffer.remove(0);
                      positionBuffer.remove(0);
                      cmgBuffer.remove(0);
                      bspBuffer.remove(0);
                      hdgBuffer.remove(0);

                      if (timeBuffer.size() > 0)
                        oldest = timeBuffer.get(0);
                      else
                        keepGoing = false;
                    }
                    if (timeBuffer.size() > 0)
                    {  
                      fromLabel.setText(SDF.format(oldest.getValue()));
                      toLabel.setText(SDF.format(utcDate.getValue()));
                    }
                  }
                  else
                  {
                    // When reseting a simulation file
//                  System.out.println("--> Timebuffer is empty");
//                  resetDataBuffers(); 
                    fromLabel.setText(" - ");
                    toLabel.setText(" - ");
                  }
                  
                  timeBuffer.add(utcDate);
                  positionBuffer.add(position);
//                System.out.println("Adding position:" + position.toString());
                  cmgBuffer.add(cmg);
                  bspBuffer.add(bsp);
                  hdgBuffer.add(hdg);
                  groundData = new GeoPoint[positionBuffer.size()];
                  int index = 0;
                  for (GeoPos gp : positionBuffer)
                  {
                    groundData[index++] = new GeoPoint(gp.lat, gp.lng);
                  }
                  index = 0;
                  drData     = new GeoPoint[positionBuffer.size()];
                  GeoPos drPos = positionBuffer.get(0);
                  int size = positionBuffer.size();
                  for (int i=0; i<size; i++)
                  {
                    if (i > 0)
                    {
                      long timeInterval = timeBuffer.get(i).getValue().getTime() - timeBuffer.get(i-1).getValue().getTime();
                      double bspeed = bspBuffer.get(i).getDoubleValue();
//                    System.out.println("-- TimeInterval:" + timeInterval + ", bsp:" + bspeed);
                      if (bspeed > 0)
                      {
                        double dist = bspeed * ((double)timeInterval / (double)3600000L); // in minutes (miles)
                        double rv   = cmgBuffer.get(i - 1).getValue();
//                      System.out.println("** In " + timeInterval + " ms, at " + bspeed + " kts, from " + drPos.toString() + " dist:" + dist + ", hdg:" + hdg + "... ");
                        if (dist > 0)
                        {
                          GeoPoint pt = MercatorUtil.deadReckoning(drPos.lat, drPos.lng, dist, rv);                      
//                        System.out.println("In " + timeInterval + " ms, from " + drPos.toString() + " dist:" + dist + ", hdg:" + hdg + ", ends up " + pt.toString());
                          drPos = new GeoPos(pt.getL(), pt.getG());
                        }
//                      else
//                        System.out.println("** dist : 0");
                      }
//                    else
//                      System.out.println("-- speed : 0");
                    } 
                    if (false)
                    {
                      if (i == 0)
                        System.out.println("-----------------------");
                      System.out.println("Adding to drData (" + i + ") :" + drPos.toString());
                    }
                    drData[i] = new GeoPoint(drPos.lat, drPos.lng);
                  }
                  setBoundaries();
                  repaint();
                }
              }
              else if (DEBUG)
              {
            //  if (!utcDate.isNull() && (timeBuffer.size() == 0 || (timeBuffer.size() > 0 && (timeBuffer.get(timeBuffer.size() - 1).getValue().getTime() < utcDate.getValue().getTime()))))
                System.out.println("utcDate is " + (utcDate == null || utcDate.isNull()?"":"not ") + "null");    
                System.out.println("timeBuffer.size() = " + timeBuffer.size());
                System.out.println("utcDate        :" + (utcDate.isNull()?"":new Date(utcDate.getValue().getTime()).toString()));
                System.out.println("last timeBuffer:" + (timeBuffer.size() > 0?new Date(timeBuffer.get(timeBuffer.size() - 1).getValue().getTime()).toString():"none"));
                System.out.println("-> " + ((timeBuffer.get(timeBuffer.size() - 1).getValue().getTime() < utcDate.getValue().getTime()) ? "true":"false"));
              }
            }
            catch (Exception ex) 
            {
              ex.printStackTrace();
            }
          }
        }        
      });
  }

  private final static Stroke STROKE_5 = new BasicStroke(5f, BasicStroke.CAP_ROUND, BasicStroke.JOIN_MITER);
  private final static Stroke STROKE_3 = new BasicStroke(3f, BasicStroke.CAP_ROUND, BasicStroke.JOIN_MITER);
  private final static Stroke STROKE_2 = new BasicStroke(1f, BasicStroke.CAP_ROUND, BasicStroke.JOIN_MITER);
  private final static Stroke STROKE_1 = new BasicStroke(1f, BasicStroke.CAP_ROUND, BasicStroke.JOIN_MITER);
  
  private transient List<TimeCurrent> timeCurrent = new ArrayList<TimeCurrent>();
  
  public void chartPanelPaintComponent(Graphics gr)
  {
    Stroke origStroke = ((Graphics2D)gr).getStroke();
    Color origColor = gr.getColor();

    plottingSheet.setW(centerPanel.getWidth() - 4);  // 4: for the bevel
    plottingSheet.setH(centerPanel.getHeight() - 4); // 4: for the bevel
    ((Graphics2D) gr).setStroke(STROKE_3);

    // Over Ground
    gr.setColor(Color.BLACK);
    Point ppt = null;
//  System.out.println("GroundData.length = " + groundData.length);
    // 1 - Build an array of panel points
    List<Point> gData = null;
    if (groundData != null)
    {
      gData = new ArrayList<Point>(groundData.length);
      for (int i=0; groundData != null && i<groundData.length; i++)
      {
        Point pt = null;
        try { pt = plottingSheet.getPanelPoint(groundData[i]); } catch (Exception nep) { System.err.println("Pt:" + nep.toString()); } 
        if (pt != null && i == 0) // First
        {
          String[] sa =  groundData[i].toString().split("/");
          String one = sa[0].trim();
          String two = sa[1].trim();
          int len = Math.max(one.length(), two.length());
          one = Utils.lpad(one, " ", len);
          two = Utils.lpad(two, " ", len);
          Font f = gr.getFont();
          gr.setFont(new Font("courier new", Font.PLAIN, 11));
          plottingSheet.postit(gr, one + "\n" + two, pt.x, pt.y, Color.yellow, Color.blue, 0.55f); // Postit. First point position
          gr.setFont(f);
        }
  
        if (pt != null && i == (groundData.length - 1)) // Last
        {
          String[] sa =  groundData[i].toString().split("/");
          String one = sa[0].trim();
          String two = sa[1].trim();
          int len = Math.max(one.length(), two.length());
          one = Utils.lpad(one, " ", len);
          two = Utils.lpad(two, " ", len);
          Font f = gr.getFont();
          gr.setFont(new Font("courier new", Font.PLAIN, 11));
          plottingSheet.postit(gr, one + "\n" + two, pt.x, pt.y, Color.yellow, Color.blue, 0.55f); // Postit. Last point position
          gr.setFont(f);
        }
        gData.add(pt);
      }
    }
    
    boolean smooth = smoothCheckBox.isSelected();
    if (smooth && gData != null)
    {
      // Draw Original data, thin line
      if (gData != null)
      {
        ((Graphics2D) gr).setStroke(STROKE_1);
        Color c = gr.getColor();
        gr.setColor(Color.BLUE);
        ppt = null;
        for (Point pt : gData)
        {
          if (ppt != null && pt != null)
          {        
            gr.drawLine(ppt.x, ppt.y, pt.x, pt.y);
            ppt = pt;
          }
          ppt = pt;      
        }
        gr.setColor(c);
      }
      // Expand
      gData = expandArray(gData, 75);
      // Smooth
//    System.out.println("New size : " + gData.size());
      int sw = gData.size() / 20;
      if (sw % 2 == 0)
        sw += 1;
      gData = smooth(sw, gData);
    }
    
    ppt = null;
    ((Graphics2D) gr).setStroke(STROKE_3);
    if (gData != null)
    {
      for (Point pt : gData)
      {
        if (ppt != null && pt != null)
        {        
          gr.drawLine(ppt.x, ppt.y, pt.x, pt.y);
          ppt = pt;
        }
        ppt = pt;      
      }
    }
    // Thru water
    gr.setColor(Color.RED);
    ppt = null;
    ((Graphics2D) gr).setStroke(STROKE_5);
//  System.out.println("DRData.length = " + drData.length);
    for (int i=0; drData != null && i<drData.length; i++)
    {
      Point pt = null;
      try
      {
        if (drData[i] != null)
          pt = plottingSheet.getPanelPoint(drData[i]);
      }
      catch (Exception ex)
      {
        System.err.println("drData[" + i + "]:");
        ex.printStackTrace();
      }
      if (ppt != null && pt != null)
      {
        gr.drawLine(ppt.x, ppt.y, pt.x, pt.y);
//      ppt = pt;
      }
      ppt = pt;
    }
    
    // Boat
    if (groundData != null && groundData.length > 0 && hdgBuffer.size() > 0)
    {
      Point pt = null;
      try { pt = plottingSheet.getPanelPoint(groundData[groundData.length - 1]); } catch (Exception nep) { System.err.println("Drawing boat:" + nep.toString()); } 
      if (pt != null)
      {
        ((Graphics2D) gr).setStroke(STROKE_2);
        Utils.drawBoat((Graphics2D) gr, 
                       Color.blue, 
                       Color.gray,
                       Color.black,
                       pt, 
                       50, 
                       (int) hdgBuffer.get(hdgBuffer.size() - 1).getDoubleValue(),
                       0.75f);
      }
    }
    // Current
    try
    {
      if (groundData != null && groundData.length > 0 && drData != null && drData.length > 0)
      {
        gr.setColor(Color.green);
        Point p1 = null;
        try { p1 = plottingSheet.getPanelPoint(groundData[groundData.length - 1]); } catch (Exception npe) { System.err.println("P1:" + npe.toString()); }
        Point p2 = null;
        try { p2 = plottingSheet.getPanelPoint(drData[drData.length - 1]); } catch (Exception npe) { System.err.println("P2:" + npe.toString()); }
        if (p1 != null && p2 != null)
        {
          GeoPoint geoFrom = new GeoPoint(Math.toRadians(drData[drData.length - 1].getL()),         
                                          Math.toRadians(drData[drData.length - 1].getG())); 
          GeoPoint geoTo   = new GeoPoint(Math.toRadians(groundData[groundData.length - 1].getL()), 
                                          Math.toRadians(groundData[groundData.length - 1].getG())); 
          double dist = GreatCircle.calculateRhumLineDistance(geoFrom, geoTo);
          double dir  = Math.toDegrees(GreatCircle.calculateRhumLineRoute(geoFrom, geoTo));
          double hourRatio = (double)(timeBuffer.get(timeBuffer.size() - 1).getValue().getTime() - timeBuffer.get(0).getValue().getTime()) / (double)3600000L;
          double speed = dist / hourRatio;
          timeCurrent.add(new TimeCurrent(timeBuffer.get(timeBuffer.size() - 1).getValue().getTime(), speed, dir));
          // trim current buffer
          long oldest = timeCurrent.get(0).getTime();
          boolean keepGoing = true;          
          while (keepGoing && oldest < (timeCurrent.get(timeCurrent.size() - 1).getTime() - bufferLength))
          {
            timeCurrent.remove(0);
            if (timeBuffer.size() > 0)
              oldest = timeCurrent.get(0).getTime();
            else
              keepGoing = false;
          }
//        System.out.println("Current Buffer [" + (bufferLength / 1000) + "] = " + timeCurrent.size() + " element(s)");
//        System.out.println("-- Speed:" + speed + " (" + dist + " nm in " + hourRatio + " hour(s).)");
  
          ((Graphics2D) gr).setStroke(STROKE_2);
          Utils.drawCurrentArrow((Graphics2D) gr, p2, p1, Color.green);
          if (speed > 0.25)
            Utils.drawHollowArrow((Graphics2D) gr, p2, p1, Color.green);
  //      gr.drawLine(p1.x, p1.y, p2.x, p2.y);
          // Display values
          gr.setColor(Color.BLUE);
          Font f = gr.getFont();
          gr.setFont(new Font("courier new", Font.PLAIN, 12));
          gr.drawString("Current dir  :" + NMEAContext.DF3.format(dir) + "\272", 10, 10);
          gr.drawString("Current speed:" + NMEAContext.DF22.format(speed) + " kts", 10, 25);
          gr.setFont(f);
          // Inject in the cache
       // System.out.println("Current display on " + ((TimeObject)timeComboBox.getSelectedItem()).getLabel() + ":" + NMEAContext.DF3.format(dir) + "\272 @" + NMEAContext.DF22.format(speed) + " kts");
          ((Map<Long, NMEADataCache.CurrentDefinition>)NMEAContext.getInstance().getCache().get(NMEADataCache.CALCULATED_CURRENT)).put(bufferLength, new NMEADataCache.CurrentDefinition(bufferLength, new Speed(speed), new Angle360(dir)));
          
          currentDisplay.setDirection(dir);
          currentDisplay.setSpeed(speed);
          currentDisplay.repaint();
        }
      }
    }
    catch (Exception ex)
    {
      System.err.println("Displaying current in DeadReckoningPlottingSheet:");
//    System.err.println(ex.toString());
      ex.printStackTrace();
    }
    // Point cloud
    try
    {
      gr.setColor(Color.BLUE);
      // Max Speed
      double maxSpeed = 0D;
      for (TimeCurrent tc : timeCurrent)
        maxSpeed = Math.max(maxSpeed, tc.getSpeed());
      double ratio = Math.min(plottingSheet.getWidth() / 2, plottingSheet.getHeight() / 2) / maxSpeed;
      ratio /= 1.5;
//    System.out.println("Ratio:" + ratio);
      int x = 0, y = 0, prevx = Integer.MIN_VALUE, prevy = Integer.MIN_VALUE;
      for (TimeCurrent tc : timeCurrent)
      {
        double dir = tc.getDir();
        double speed = tc.getSpeed();
        x = (int)((plottingSheet.getWidth() / 2) + (speed * Math.sin(Math.toRadians(dir)) * ratio));
        y = (int)((plottingSheet.getHeight() / 2) - (speed * Math.cos(Math.toRadians(dir)) * ratio));
        gr.drawOval(x - 2, y - 2, 4, 4);
        if (prevx != Integer.MIN_VALUE && prevy != Integer.MIN_VALUE)
          gr.drawLine(prevx, prevy, x, y);
        prevx = x;
        prevy = y;
      }
      gr.setColor(Color.GREEN);
      gr.drawLine(plottingSheet.getWidth() / 2, plottingSheet.getHeight() / 2, x, y);
      gr.setColor(Color.GRAY);
      String str = Integer.toString(timeCurrent.size()) + " point(s)";
      int strWidth  = gr.getFontMetrics(gr.getFont()).stringWidth(str);
      gr.drawString(str, plottingSheet.getWidth() - strWidth - 2, 12);
    }
    catch (Exception e)
    {
      e.printStackTrace();
    }
    ((Graphics2D) gr).setStroke(origStroke);
    gr.setColor(origColor);
  }

  private static List<Point> expandArray(List<Point> origData, int smoothFactor)
  {
    List<Point> expanded = new ArrayList<Point>(origData.size() * smoothFactor);
    for (int i=0; i<origData.size() - 1; i++)
    {
      try
      {
        int deltaX = origData.get(i + 1).x - origData.get(i).x;
        int deltaY = origData.get(i + 1).y - origData.get(i).y;
        
        for (int j=0; j<smoothFactor; j++)
        {
          int newX = (int)(origData.get(i).x + (deltaX * ((double)j / (double)smoothFactor)));
          int newY = (int)(origData.get(i).y + (deltaY * ((double)j / (double)smoothFactor)));
          expanded.add(new Point(newX, newY));
        }
      }
      catch (Exception ex)
      {
        continue;
      }
    }
    return expanded;
  }
  
  private static List<Point> smooth(int fork, List<Point>rawData)  
  {
    if ((fork % 2) != 1)
    {
      JOptionPane.showMessageDialog(null, "Fork width must be odd", "Smoothing", JOptionPane.ERROR_MESSAGE); // LOCALIZE     
      throw new RuntimeException("Fork must be odd.");
    }
    
    List<Point> smoothedData = new ArrayList<Point>(rawData.size());
      
    for (Point pt : rawData) // Clone the array
    {
      Point p = new Point(pt.x, pt.y);
      smoothedData.add(p);
    }
    int halfFork = ((fork-1) / 2);
    for (int i=0; i<rawData.size(); i++)    
    {
      double dx = 0D;      
      double dy = 0D;
      
      for (int j=(i-halfFork); j<=(i+halfFork); j++)
      {
        int _j = j;
        if (_j<0) 
          _j = 0;
        if (_j>=rawData.size()) 
          _j = rawData.size() - 1;
        dx += rawData.get(_j).x;
        dy += rawData.get(_j).y;
      }
      dx = dx / fork;
      dy = dy / fork;
      smoothedData.get(i).x = (int)Math.round(dx);      
      smoothedData.get(i).y = (int)Math.round(dy);      
    }
    return smoothedData;
  }

  private void resetDataBuffers()
  {
    timeBuffer     = new ArrayList<UTCHolder>();
    positionBuffer = new ArrayList<GeoPos>();
    cmgBuffer      = new ArrayList<Angle360>();
    hdgBuffer      = new ArrayList<Angle360>();
    bspBuffer      = new ArrayList<Speed>();
    timeCurrent    = new ArrayList<TimeCurrent>();
  }
  
  public boolean onEvent(EventObject eventobject, int i)
  {
    return false;
  }

  public String getMessForTooltip()
  {
    return null;
  }

  public boolean replaceMessForTooltip()
  {
    return false;
  }

  public void videoCompleted()
  {
  }

  public void videoFrameCompleted(Graphics g, Point p)
  {
  }

  public void zoomFactorHasChanged(double d)
  {
  }

  public void chartDDZ(double top, double bottom, double left, double right)
  {
  }

  private void setBoundaries()
  {
    // Calculate center
    double top = -90d, bottom = 90d, right = -180d, left = 180d;
    for (int i=0; groundData != null && i<groundData.length; i++)
    {
      GeoPoint gp = groundData[i];
      bottom = Math.min(bottom, gp.getL());
      top    = Math.max(top, gp.getL());
      right  = Math.max(right, gp.getG());
      left   = Math.min(left, gp.getG());
    }
    for (int i=0; drData != null && i<drData.length; i++)
    {
      GeoPoint gp = drData[i];
      bottom = Math.min(bottom, gp.getL());
      top    = Math.max(top, gp.getL());
      right  = Math.max(right, gp.getG());
      left   = Math.min(left, gp.getG());
    }
//  System.out.println("Width:" + (right - left) + ", Height:" + (top - bottom));
    double max = Math.max((top - bottom), (right - left));
//  if (max < 0.001d)
//    max = 0.001d;
    
    double latSpan = Math.max(max * 1.1, minLatSpan);
//  System.out.println("Lat Span:" + latSpan);
    plottingSheet.setChartLatitudeSpan(latSpan);
    plottingSheet.setCenterLat(bottom + ((top - bottom) / 2d));
    plottingSheet.setCenterLong(left + ((right - left) / 2d));
    
    this.repaint();
  }
  
  public void setGroundData(GeoPoint[] groundData)
  {
    this.groundData = groundData;
    setBoundaries();
  }

  public void setDrData(GeoPoint[] drData)
  {
    this.drData = drData;
    setBoundaries();
  }

  public void setCenterLat(double centerLat)
  {
    plottingSheet.setCenterLat(centerLat);
    this.centerLat = centerLat;
  }

  public void setCenterLng(double centerLng)
  {
    plottingSheet.setCenterLong(centerLng);
    this.centerLng = centerLng;
  }

  public void setLatSpan(double latSpan)
  {
    plottingSheet.setChartLatitudeSpan(latSpan);
    this.latSpan = latSpan;
  }
  
  public long getTimeWindow()
  {
    return ((TimeObject)timeComboBox.getSelectedItem()).getTime();
  }

  public void setBufferLength(long bufferLength)
  {
    this.bufferLength = bufferLength;
  }

  public PlottingSheet getPlottingSheet()
  {
    return plottingSheet;
  }

  public void setMinLatSpan(double minLatSpan)
  {
    this.minLatSpan = minLatSpan;
  }

  public static class TimeObject
  {
    private long time;
    private String label;
    public TimeObject(long time, String label)
    {
      this.time = time;
      this.label = label;
    }
    
    public String toString()
    {
      return label;
    }

    public void setTime(long time)
    {
      this.time = time;
    }

    public long getTime()
    {
      return time;
    }

    public void setLabel(String label)
    {
      this.label = label;
    }

    public String getLabel()
    {
      return label;
    }
  }
  
  public static class TimeCurrent
  {
    private final long time; 
    private final double speed; 
    private final double dir;
   
    public TimeCurrent(long time, double speed, double dir)
    {
      this.time = time;
      this.speed = speed;
      this.dir = dir;
    }

    public long getTime()
    {
      return time;
    }

    public double getSpeed()
    {
      return speed;
    }

    public double getDir()
    {
      return dir;
    }
  }
}
