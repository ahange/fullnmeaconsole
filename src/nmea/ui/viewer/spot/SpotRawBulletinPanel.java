package nmea.ui.viewer.spot;

import java.awt.BorderLayout;
import java.awt.Font;
import java.awt.Graphics;

import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;

import java.io.FilenameFilter;

import java.text.DecimalFormat;
import java.text.Format;
import java.text.SimpleDateFormat;

import java.util.Date;
import java.util.List;

import java.util.TimeZone;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import javax.swing.BorderFactory;
import javax.swing.JButton;
import javax.swing.JEditorPane;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;

import nmea.server.ctx.NMEAContext;

import nmea.ui.viewer.spot.utils.SpotParser;
import nmea.ui.viewer.spot.utils.SpotParser.SpotLine;

import ocss.nmea.parser.GeoPos;

public class SpotRawBulletinPanel
     extends JPanel
{
  private BorderLayout borderLayout1 = new BorderLayout();
  private JEditorPane spotBulletinEditorPane = new JEditorPane();
  private JScrollPane spotScrollPane = null;
  private JPanel topPanel = new JPanel();
  private JButton scanAirmailInbox = null;

  public SpotRawBulletinPanel()
  {
    jbInit();
  }

  private void jbInit()
  {
    topPanel.setLayout(new BorderLayout());
    scanAirmailInbox = new JButton("Scan Airmail Inbox");
    scanAirmailInbox.addActionListener(new ActionListener()
    {
      public void actionPerformed(ActionEvent e)
      {
        scanAirmailInbox_actionPerformed(e);
      }
    });
    this.setLayout(borderLayout1);
//  this.setBackground(Color.white);
    this.setToolTipText("Paste the SPOT Bulletin here, and tab out of the field.");
    spotBulletinEditorPane.setBorder(BorderFactory.createTitledBorder("SPOT Bulletin"));
    spotBulletinEditorPane.setFont(new Font("Source Code Pro", 0, 11));
    spotScrollPane = new JScrollPane();
    spotScrollPane.getViewport().add(spotBulletinEditorPane, null);

    spotBulletinEditorPane.getDocument().addDocumentListener(new DocumentListener()
      {
          public void insertUpdate(DocumentEvent e)
          {
            parseContent(spotBulletinEditorPane.getText());
          }

          public void removeUpdate(DocumentEvent e)
          {
            parseContent(spotBulletinEditorPane.getText());
          }

          public void changedUpdate(DocumentEvent e)
          {
            parseContent(spotBulletinEditorPane.getText());
          }
        });    
    topPanel.add(scanAirmailInbox, BorderLayout.WEST);
    this.add(topPanel, BorderLayout.NORTH);
    this.add(spotScrollPane, BorderLayout.CENTER);
  }
  
  public void paintComponent(Graphics gr)
  {    
  }
  
  private void parseContent(String str)
  {
    try
    {
      List<SpotLine> spotLines = SpotParser.parse(str);
      GeoPos spotPos = SpotParser.getSpotPos();
      System.out.println("SPOT Data parsed, " + spotLines.size() + " line(s).");
      // Broadcast parsed data
      NMEAContext.getInstance().fireNewSpotData(spotLines, spotPos);
    }
    catch (Exception ex)
    {
      System.out.println("Cannot parse the SPOT data...");
      ex.printStackTrace();
      NMEAContext.getInstance().fireNewSpotData(null, null);
    }
  }

  private void scanAirmailInbox_actionPerformed(ActionEvent e)
  {
    final SimpleDateFormat MESS_DATE_FMT = new SimpleDateFormat("yyyy/MM/dd HH:mm:ss");
    MESS_DATE_FMT.setTimeZone(TimeZone.getTimeZone("Etc/UTC"));
    
    String airmailLocation = System.getProperty("airmail.location");
    String airmailId       = System.getProperty("airmail.id");
    
    if (airmailLocation == null) 
    {
      throw new RuntimeException("Property airmail.location not set. Please see your preferences (SailMail)");
    }
    if (airmailId == null) 
    {
      throw new RuntimeException("Property airmail.id not set. Please see your preferences (SailMail)");
    }
    File airmailDir = new File(airmailLocation);
    if (!airmailDir.exists() || !airmailDir.isDirectory())
    {
      throw new RuntimeException(airmailLocation + " does not exist, or is not a directory. Please see your preferences (SailMail)");
    }
    File inboxDir = new File(airmailDir, "Inbox"); 
    if (!inboxDir.exists())
    {
      System.out.println(inboxDir.toString() + " does not exist. Exiting");
      spotBulletinEditorPane.setText(inboxDir.toString() + " does not exist. Found no inbox.");
      return;
    }
    
    final String XDATE_HEADER    = "X-Date:";
    final String XSTATUS_HEADER  = "X-Status:";
    final String SUBJECT_HEADER  = "Subject:";
    
    File[] messages = inboxDir.listFiles(new FilenameFilter()
    {
      @Override
      public boolean accept(File file, String string)
      {
        return string.endsWith(".msg");
      }
    });
    String messContent = "";
    Date mostRecent = null;
    for (File mess : messages)
    {
      if (mess.isFile())
      {
        try
        {
          BufferedReader br = new BufferedReader(new FileReader(mess));     
          String xDate   = "";
          String xStatus = "";
          String subject = "";
          String content = "";
          boolean inContent   = false;
          boolean keepReading = true;
          while (keepReading)
          {
            String line = br.readLine();
            if (line == null)
              keepReading = false;
            else
            {
              if (inContent)
                content += (line + "\n");
              else
              {
                if (line.trim().length() == 0)
                {
                  if (xStatus.trim().equals("New"))
                    inContent = true;
                  else
                    keepReading = false;
                }
                else if (line.startsWith(XDATE_HEADER))
                  xDate = line.substring(XDATE_HEADER.length()).trim();
                else if (line.startsWith(XSTATUS_HEADER))
                  xStatus = line.substring(XSTATUS_HEADER.length()).trim();
                else if (line.startsWith(SUBJECT_HEADER))
                  subject = line.substring(SUBJECT_HEADER.length()).trim();
              }
            }
          }
          br.close();
          Date messDate = MESS_DATE_FMT.parse(xDate);
          if (mostRecent == null || messDate.after(mostRecent)) // TODO Filter on the subject
          {
            System.out.println("Message Date:" + xDate);
            mostRecent = messDate;
            messContent = content;
          }
        }
        catch (Exception ex)
        {
          ex.printStackTrace();
        }
      }
    }
    System.out.println("Message:\n" + messContent);
    // Put the content in the right place
    spotBulletinEditorPane.setText(messContent);
  }  
}
