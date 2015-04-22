package nmea.ui.viewer.spot;

import java.awt.BorderLayout;
import java.awt.Font;
import java.awt.Graphics;

import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;

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
    final Format MESS_NUM_FMT = new DecimalFormat("#0000");
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
    int messnum = 0;
    File inboxDir = new File(airmailDir, "Inbox"); 
    if (!inboxDir.exists())
    {
      System.out.println(inboxDir.toString() + " does not exist. Exiting");
      spotBulletinEditorPane.setText(inboxDir.toString() + " does not exist. Found no inbox.");
//    parseContent(str);
      return;
    }
    Pattern pattern = Pattern.compile("([0-9]*)_" + airmailId.toUpperCase() + ".msg");
    
    File[] messages = inboxDir.listFiles();
    for (File mess : messages)
    {
      if (mess.isFile())
      {
        String messName = mess.getName();
        Matcher matcher = pattern.matcher(messName);
        while (matcher.find())
        {  
          String match = matcher.group(1).trim();
          messnum = Math.max(messnum, Integer.parseInt(match));
        }        
      }
    }
    messnum += 1;
    String messageName = MESS_NUM_FMT.format(messnum) + "_" + airmailId;
    
    String strReq = "";
    String messageContent = 
      "X-Priority: 4\r\n" + 
      "X-MID: " + messageName + "\r\n" +
      "X-Status: Posted\r\n" + 
      "To: query@saildocs.com\r\n" + 
      "X-Type: Email; Outmail\r\n" + 
      "Subject: Saildocs Request\r\n" + 
      "X-Via: Sailmail\r\n" + 
      "X-Date: " + MESS_DATE_FMT.format(new Date()) + "\r\n" + 
      "\r\n" + 
      strReq;
    // Read the outbox
    System.out.println("Message:\n" + messageContent);
    try
    {
      BufferedWriter br = new BufferedWriter(new FileWriter(new File(inboxDir, messageName + ".msg")));
      br.write(messageContent);
      br.close();
      // TODO Put the content in the right place
      spotBulletinEditorPane.setText("Et toc!");
    }
    catch (Exception ex)
    {
      ex.printStackTrace();
    }
  }  
}
