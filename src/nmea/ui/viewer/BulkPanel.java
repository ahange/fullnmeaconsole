package nmea.ui.viewer;

import coreutilities.Utilities;

import java.lang.reflect.InvocationTargetException;

import javax.swing.JCheckBox;

import nmea.server.ctx.NMEAContext;

import nmea.event.NMEAReaderListener;

import java.awt.BorderLayout;

import java.awt.Color;
import java.awt.Dimension;
import java.awt.Font;
import java.awt.Graphics;

//import java.awt.Graphics2D;
//import java.awt.RenderingHints;

import java.util.ArrayList;

import java.util.List;

import javax.swing.JEditorPane;
import javax.swing.JPanel;

import javax.swing.SwingUtilities;

import nmea.server.constants.Constants;

import ocss.nmea.utils.NMEAUtils;

public class BulkPanel
  extends JPanel
{
  @SuppressWarnings("compatibility:3299581623637298497")
  private final static long serialVersionUID = 1L;
  
  private final static boolean SCROLL_STREAM = true;
  
  private BorderLayout borderLayout1 = new BorderLayout();
  private JEditorPane bulkEditorPane = new JEditorPane()
    {
      @SuppressWarnings("compatibility:-6555361911476203029")
      private final static long serialVersionUID = 2L;
      
      public void paintComponent(Graphics gr)
      {
//        ((Graphics2D)gr).setRenderingHint(RenderingHints.KEY_TEXT_ANTIALIASING,
//                                          RenderingHints.VALUE_TEXT_ANTIALIAS_ON);      
//        ((Graphics2D)gr).setRenderingHint(RenderingHints.KEY_ANTIALIASING,
//                                          RenderingHints.VALUE_ANTIALIAS_ON);      
        super.paintComponent(gr);
        // Calculate nb lines
        int fontSize = this.getFont().getSize();
        int height   = this.getHeight();
        nbLines  = height / fontSize;
//      System.out.println("nblines:" + nbLines);
      }
    };

  int nbLines = 0;
  List<String> sentences = new ArrayList<String>(10);
  private JPanel dataStreamPanel = new JPanel();
  private String currentStream = "";
  private BorderLayout borderLayout2 = new BorderLayout();
  private JPanel centerDataPanel = new JPanel()
    {
      @SuppressWarnings("compatibility:-7001108031039269352")
      private final static long serialVersionUID = 1L;
      
      @Override
      public void paintComponent(Graphics gr)
      {
        if (Utilities.thisClassVerbose(this.getClass())) // nmea.ui.viewer.BulkPanel$2
          System.out.println("... Repainting centerDataPanel (bulk NMEA stream)");
        gr.setColor(Color.white);
        gr.fillRect(0, 0, this.getWidth(), this.getHeight());
        gr.setColor(Color.red);
        if (!SCROLL_STREAM)
        {
          // Calculate nb characters to display
      //  int fontSize = this.getFont().getSize();
          int strWidth  = gr.getFontMetrics(gr.getFont()).stringWidth(currentStream);
          if (strWidth > this.getWidth())
          {
            while (strWidth > this.getWidth())
            {
              currentStream = currentStream.substring(1);
              strWidth  = gr.getFontMetrics(gr.getFont()).stringWidth(currentStream);
            }
          }
        }
        if (currentStream.length() == 0)
          gr.drawString("- No Data -", 2, this.getHeight() - 2);
        else
          gr.drawString(currentStream, 2, this.getHeight() - 2);
      }
    };
  private JCheckBox hexaCheckBox = new JCheckBox();

  public BulkPanel()
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
    this.setLayout(borderLayout1);
    centerDataPanel.setBackground(Color.white);
    centerDataPanel.setFont(new Font("Courier New", Font.PLAIN, 14));
    this.add(bulkEditorPane, BorderLayout.CENTER);
    dataStreamPanel.setLayout(borderLayout2);
    dataStreamPanel.add(centerDataPanel, BorderLayout.CENTER);
    dataStreamPanel.add(hexaCheckBox, BorderLayout.WEST);
    this.add(dataStreamPanel, BorderLayout.SOUTH);
    bulkEditorPane.setBackground(Color.black);
    bulkEditorPane.setForeground(Color.green);
    bulkEditorPane.setFont(new Font("courier new", Font.PLAIN, 14));
//  centerDataPanel.setPreferredSize(new Dimension(200, 16));
    dataStreamPanel.setPreferredSize(new Dimension(200, 16));

    hexaCheckBox.setText("Hexa");
    hexaCheckBox.setToolTipText("Display stream in hexadecimal or text");
    NMEAContext.getInstance().addNMEAReaderListener(new NMEAReaderListener(Constants.NMEA_SERVER_LISTENER_GROUP_ID, "Bulk")
     {
       public void manageNMEAString(String str) 
       {
//       System.out.println("Displaying NMEA Sentences");
         // Add the new line to the panel.
         synchronized (sentences)
         {
           sentences.add(str.trim());
           while (sentences.size() > nbLines)
             sentences.remove(0);
  
           StringBuffer content = new StringBuffer();
           for (String s : sentences)
             content.append(s + "\n");
           bulkEditorPane.setText(content.toString());
         }
       }
     });
  }  
  
  public void setDataStream(String s)
  {
    if (Utilities.thisClassVerbose(this.getClass())) // nmea.ui.viewer.BulkPanel
      System.out.println("... Repainting centerDataPanel (bulk NMEA stream) [" + s + "]");
    currentStream  += s;
    if (centerDataPanel.isVisible())
    {
      if (!SCROLL_STREAM)
        centerDataPanel.repaint();
      else
      {
        // vusial scroll
        int strWidth  = centerDataPanel.getFontMetrics(centerDataPanel.getFont()).stringWidth(currentStream);
        if (strWidth > this.getWidth())
        {
          while (strWidth > this.getWidth())
          {
            currentStream = currentStream.substring(1);
            strWidth  = centerDataPanel.getFontMetrics(centerDataPanel.getFont()).stringWidth(currentStream);
            try
            {
              SwingUtilities.invokeAndWait(new Runnable()
                {
                  public void run()
                  {
                    centerDataPanel.repaint();
                  }
                });
            }
            catch (InvocationTargetException ite)
            {
              ite.printStackTrace();
            }
            catch (InterruptedException ie)
            {
              ie.printStackTrace();
            }
          }
        }
      }
    }
  }
  
  public int getDataStreamType()
  {
    return (hexaCheckBox.isSelected()?NMEAUtils.ALL_IN_HEXA:NMEAUtils.CR_NL);    
  }
}
