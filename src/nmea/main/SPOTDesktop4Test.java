package nmea.main;


import java.awt.Dimension;

import java.awt.Rectangle;
import java.awt.Toolkit;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;

import javax.swing.JFrame;

import javax.swing.UIManager;

import nmea.ui.NMEAInternalFrame;
import nmea.ui.viewer.spot.SPOTInternalFrame;

public class SPOTDesktop4Test
     extends JFrame
{
  public SPOTDesktop4Test()
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
    this.getContentPane().setLayout( null );
    this.setSize(new Dimension(1400, 900));
    this.setTitle( "SPOT Desktop" );
    
    SPOTInternalFrame nmeaFrame = new SPOTInternalFrame();
    nmeaFrame.setIconifiable(true);
    nmeaFrame.setClosable(true);
    nmeaFrame.setMaximizable(true);
    nmeaFrame.setResizable(true);
    this.add(nmeaFrame);
    nmeaFrame.setVisible(true);
    nmeaFrame.setBounds(new Rectangle(70, 35, 1200, 800));
  }
  
  public static void main(String[] args)
  {
    String lnf = null;
    try { lnf = System.getProperty("swing.defaultlaf"); } catch (Exception ignore) { System.err.println(ignore.getLocalizedMessage()); }
    //  System.out.println("LnF:" + lnf);
    if (lnf == null) // Let the -Dswing.defaultlaf do the job.
    {
      try
      {
        if (System.getProperty("swing.defaultlaf") == null)
          UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName());
      }
      catch(Exception e)
      {
        e.printStackTrace();
      }
    }
    JFrame.setDefaultLookAndFeelDecorated(true);
    JFrame frame = new SPOTDesktop4Test();
    Dimension screenSize = Toolkit.getDefaultToolkit().getScreenSize();
    Dimension frameSize = frame.getSize();
    if (frameSize.height > screenSize.height)
    {
      frameSize.height = screenSize.height;
    }
    if (frameSize.width > screenSize.width)
    {
      frameSize.width = screenSize.width;
    }
    frame.setLocation( ( screenSize.width - frameSize.width ) / 2, ( screenSize.height - frameSize.height ) / 2 );

    frame.addWindowListener(new WindowAdapter()
    {
      public void windowClosing(WindowEvent e)
      {
        System.exit(0);
      }
    });    
    //  frame.setDefaultCloseOperation( JFrame.EXIT_ON_CLOSE );
    frame.setVisible(true);
    
  }
}
