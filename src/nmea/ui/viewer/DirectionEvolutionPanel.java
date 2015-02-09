package nmea.ui.viewer;

import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.Graphics;

import java.awt.Graphics2D;
import java.awt.RenderingHints;

import java.util.Date;

import javax.swing.JPanel;

import nmea.server.ctx.NMEAContext;
import nmea.server.ctx.NMEADataCache;

import nmea.event.NMEAReaderListener;

import nmea.server.constants.Constants;

import nmea.ui.viewer.elements.DirectionEvolutionDisplay;

import coreutilities.gui.HeadingPanel;

import java.awt.GridBagConstraints;

import java.awt.Insets;

import ocss.nmea.parser.Angle180;
import ocss.nmea.parser.Angle360;
import ocss.nmea.parser.TrueWindDirection;

public class DirectionEvolutionPanel
     extends JPanel  
{

  private DirectionEvolutionDisplay hdgLoggingDisplay = new DirectionEvolutionDisplay("HDG", "Heading", 36);
  private DirectionEvolutionDisplay cogLoggingDisplay = new DirectionEvolutionDisplay("COG", "Course Over Ground", 36);
  private DirectionEvolutionDisplay awaLoggingDisplay = new DirectionEvolutionDisplay("AWA", "Apparent Wind Angle", 36, HeadingPanel.MINUS_180_TO_PLUS_180);
  private DirectionEvolutionDisplay twdLoggingDisplay = new DirectionEvolutionDisplay("WINDIR", "True Wind Direction", 36); // "TWD"
  private DirectionEvolutionDisplay cdrLoggingDisplay = new DirectionEvolutionDisplay("CDR", "Current Direction", 36);
  
  private FlowLayout flowLayout1 = new FlowLayout();

  public DirectionEvolutionPanel()
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
    this.setLayout(flowLayout1);
    this.setSize(new Dimension(910, 430));

//    this.add(hdgLoggingDisplay, null); // new GridBagConstraints(0, 0, 1, 1, 0.0, 0.0, GridBagConstraints.WEST, GridBagConstraints.HORIZONTAL, new Insets(0, 0, 0, 0), 0, 0));
//    this.add(cogLoggingDisplay, null); // new GridBagConstraints(0, 1, 1, 1, 0.0, 0.0, GridBagConstraints.WEST, GridBagConstraints.HORIZONTAL, new Insets(2, 0, 0, 0), 0, 0));
//    this.add(awaLoggingDisplay, null); // new GridBagConstraints(0, 2, 1, 1, 0.0, 0.0, GridBagConstraints.WEST, GridBagConstraints.HORIZONTAL, new Insets(2, 0, 0, 0), 0, 0));
//    this.add(twdLoggingDisplay, null); // new GridBagConstraints(0, 2, 1, 1, 0.0, 0.0, GridBagConstraints.WEST, GridBagConstraints.HORIZONTAL, new Insets(2, 0, 0, 0), 0, 0));
//    this.add(cdrLoggingDisplay, null); // new GridBagConstraints(0, 3, 1, 1, 0.0, 0.0, GridBagConstraints.WEST, GridBagConstraints.HORIZONTAL, new Insets(2, 0, 0, 0), 0, 0));

    this.add(hdgLoggingDisplay, new GridBagConstraints(0, 0, 1, 1, 0.0, 0.0, GridBagConstraints.WEST, GridBagConstraints.HORIZONTAL, new Insets(0, 0, 0, 0), 0, 0));
    this.add(cogLoggingDisplay, new GridBagConstraints(0, 1, 1, 1, 0.0, 0.0, GridBagConstraints.WEST, GridBagConstraints.HORIZONTAL, new Insets(2, 0, 0, 0), 0, 0));
    this.add(awaLoggingDisplay, new GridBagConstraints(0, 2, 1, 1, 0.0, 0.0, GridBagConstraints.WEST, GridBagConstraints.HORIZONTAL, new Insets(2, 0, 0, 0), 0, 0));
    this.add(twdLoggingDisplay, new GridBagConstraints(0, 3, 1, 1, 0.0, 0.0, GridBagConstraints.WEST, GridBagConstraints.HORIZONTAL, new Insets(2, 0, 0, 0), 0, 0));
    this.add(cdrLoggingDisplay, new GridBagConstraints(0, 4, 1, 1, 0.0, 0.0, GridBagConstraints.WEST, GridBagConstraints.HORIZONTAL, new Insets(2, 0, 0, 0), 0, 0));

    /*
    Dimension dim = new Dimension(DirectionEvolutionDisplay.DEFAULT_WIDTH, this.getHeight());
    hdgLoggingDisplay.setPreferredSize(dim);
    cogLoggingDisplay.setPreferredSize(dim);
    awaLoggingDisplay.setPreferredSize(dim);
    twdLoggingDisplay.setPreferredSize(dim);
    cdrLoggingDisplay.setPreferredSize(dim);
    */
    NMEAContext.getInstance().addNMEAReaderListener(new NMEAReaderListener(Constants.NMEA_SERVER_LISTENER_GROUP_ID, "Direction")
      {
        public void dataUpdate()
        {
          NMEADataCache cache = NMEAContext.getInstance().getCache();
          if (cache != null)
          {
            Date d = new Date();
            try
            {
              synchronized (hdgLoggingDisplay)
              {
                try 
                {
                  hdgLoggingDisplay.addValue(d, ((Angle360) cache.get(NMEADataCache.HDG_TRUE)).getValue());
                  hdgLoggingDisplay.addNDValue(d, ((Angle360) cache.get(NMEADataCache.HDG_TRUE, false)).getValue());
                  hdgLoggingDisplay.repaint();
                }
                catch (NullPointerException npe)
                {
                  // Not in cache
                }
              }
            }
            catch (Exception ex)
            {
              ex.printStackTrace();
            }
            try
            {
              synchronized (cogLoggingDisplay)
              {
                try 
                {
                  cogLoggingDisplay.addValue(d, ((Angle360) cache.get(NMEADataCache.COG)).getValue());
                  cogLoggingDisplay.addNDValue(d, ((Angle360) cache.get(NMEADataCache.COG, false)).getValue());
                  cogLoggingDisplay.repaint();
                }
                catch (NullPointerException npe)
                {
                  // Not in cache
                }
              }
            }
            catch (Exception ex)
            {
              ex.printStackTrace();
            }
            try
            {
              synchronized (awaLoggingDisplay)
              {
                try 
                {
                  awaLoggingDisplay.addValue(d, ((Angle180) cache.get(NMEADataCache.AWA)).getValue());
                  awaLoggingDisplay.addNDValue(d, ((Angle180) cache.get(NMEADataCache.AWA, false)).getValue());
                  awaLoggingDisplay.repaint();
                }
                catch (NullPointerException npe)
                {
                  // Not in cache
                }
              }
            }
            catch (Exception ex)
            {
              ex.printStackTrace();
            }
            try
            {
              synchronized (twdLoggingDisplay)
              {
                try 
                {
                  twdLoggingDisplay.addValue(d, ((TrueWindDirection) cache.get(NMEADataCache.TWD)).getValue());
                  twdLoggingDisplay.addNDValue(d, ((TrueWindDirection) cache.get(NMEADataCache.TWD, false)).getValue());
                  twdLoggingDisplay.repaint();
                }
                catch (NullPointerException npe)
                {
                  // Not in cache
                }
              }
            }
            catch (Exception ex)
            {
              ex.printStackTrace();
            }
            try
            {
              synchronized (cdrLoggingDisplay)
              {
                try 
                {
                  cdrLoggingDisplay.addValue(d, ((Angle360) cache.get(NMEADataCache.CDR)).getValue());
                  cdrLoggingDisplay.addNDValue(d, ((Angle360) cache.get(NMEADataCache.CDR, false)).getValue());
                  cdrLoggingDisplay.repaint();
                }
                catch (NullPointerException npe)
                {
                  // Not in cache
                }
              }
            }
            catch (Exception ex)
            {
              ex.printStackTrace();
            }
          }
        }
      });
    
    this.repaint();
  }
  
  public void paintComponent(Graphics gr)
  {
    ((Graphics2D)gr).setRenderingHint(RenderingHints.KEY_TEXT_ANTIALIASING,
                                      RenderingHints.VALUE_TEXT_ANTIALIAS_ON);      
    ((Graphics2D)gr).setRenderingHint(RenderingHints.KEY_ANTIALIASING,
                                      RenderingHints.VALUE_ANTIALIAS_ON);   
    
    Dimension dim = new Dimension(DirectionEvolutionDisplay.DEFAULT_WIDTH, this.getHeight());
    hdgLoggingDisplay.setPreferredSize(dim);
    cogLoggingDisplay.setPreferredSize(dim);
    awaLoggingDisplay.setPreferredSize(dim);
    twdLoggingDisplay.setPreferredSize(dim);
    cdrLoggingDisplay.setPreferredSize(dim);
     
/*  hdgLoggingDisplay.repaint();
    cogLoggingDisplay.repaint();
    twdLoggingDisplay.repaint();
    cdrLoggingDisplay.repaint(); */
  }
}
