package nmea.ui.viewer.spot;

import astro.calc.GeoPoint;

import coreutilities.Utilities;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Desktop;
import java.awt.Dimension;
import java.awt.Font;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Insets;
import java.awt.Point;
import java.awt.RenderingHints;
import java.awt.Toolkit;
import java.awt.datatransfer.Clipboard;
import java.awt.datatransfer.StringSelection;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

import java.io.BufferedWriter;
import java.io.File;

import java.io.FileWriter;

import java.net.URI;

import java.net.URLEncoder;

import java.text.DecimalFormat;

import java.text.Format;
import java.text.SimpleDateFormat;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Map;

import java.util.TimeZone;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

import javax.swing.ButtonGroup;
import javax.swing.JButton;
import javax.swing.JCheckBox;
import javax.swing.JComboBox;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JRadioButton;

import nmea.event.NMEAReaderListener;

import nmea.server.ctx.NMEAContext;
import nmea.server.ctx.NMEADataCache;

import nmea.ui.widgets.PositionPanel;

import ocss.nmea.parser.GeoPos;
import ocss.nmea.parser.SVData;
import ocss.nmea.parser.StringParsers;

import user.util.GeomUtil;

public class SpotComposerPanel
     extends JPanel
{
  private JPanel topPanel = new JPanel();
  private final static DecimalFormat DF2 = new DecimalFormat("00");
  private final static DecimalFormat DF34 = new DecimalFormat("#00.0000");
  private GridBagLayout gridBagLayout1 = new GridBagLayout();
  private JRadioButton gpsRadioButton = new JRadioButton();
  private JRadioButton manualPosRadioButton = new JRadioButton();
  private ButtonGroup group = new ButtonGroup();
  private JLabel positionOriginLabel = new JLabel();
  private PositionPanel positionPanel = new PositionPanel();
  private JPanel durationPanel = new JPanel();
  private JLabel numberOfDaysLabel = new JLabel();
  private JComboBox daysComboBox = new JComboBox();
  private JLabel intervalInHoursLabel = new JLabel();
  private JComboBox hoursComboBox = new JComboBox();
  private JPanel dataPanel = new JPanel();
  private JCheckBox windCheckBox = new JCheckBox();
  private JCheckBox prmslCheckBox = new JCheckBox();
  private JCheckBox rainCheckBox = new JCheckBox();
  private JLabel generatedRequestLabel = new JLabel();
  private JButton clipBoardButton = new JButton();
  private JButton mailButton = new JButton();
  private JButton airmailButton = new JButton();
  private JPanel buttonPanel = new JPanel();

  public SpotComposerPanel()
  {
    jbInit();
    NMEAContext.getInstance().addNMEAReaderListener(new NMEAReaderListener("SPOT", "Spot Composer Panel")
    {
      public void manageNMEAString(String str)
      {
    //  System.out.println("NMEA:" + str);
        if (str.trim().length() > 6 && str.startsWith("$") && gpsRadioButton.isSelected())
        {
          if (StringParsers.validCheckSum(str) && (str.substring(3, 6).equals("RMC") ||
                                                   str.substring(3, 6).equals("GLL")))
          {
            try
            {
              if (NMEAContext.getInstance().getCache().get(NMEADataCache.POSITION) != null)
              {
                GeoPos position = (GeoPos)NMEAContext.getInstance().getCache().get(NMEADataCache.POSITION);
                positionPanel.setPosition(position.lat, position.lng);
                updateData();
              }
            }
            catch (Exception ex)
            {
              // No cache yet
            }
          }
        }
      }
    });
  }

  private void jbInit()
  {
    topPanel.setLayout(gridBagLayout1);
    this.setLayout(new BorderLayout());
    this.add(topPanel, BorderLayout.NORTH);
    this.add(new JPanel(), BorderLayout.CENTER);
    this.setPreferredSize(new Dimension(200, 600));
    gpsRadioButton.setText("From GPS");
    gpsRadioButton.setSelected(true);
    gpsRadioButton.addActionListener(new ActionListener()
    {
      public void actionPerformed(ActionEvent e)
      {
        gpsRadioButton_actionPerformed(e);
      }
    });
    manualPosRadioButton.setText("Manual Entry");
    manualPosRadioButton.addActionListener(new ActionListener()
    {
      public void actionPerformed(ActionEvent e)
      {
        manualPosRadioButton_actionPerformed(e);
      }
    });
    group.add(gpsRadioButton);
    group.add(manualPosRadioButton);
    positionOriginLabel.setText("Position Origin");
    numberOfDaysLabel.setText("Number of Days");
    intervalInHoursLabel.setText("Interval in Hours");
    hoursComboBox.addActionListener(new ActionListener()
    {
      public void actionPerformed(ActionEvent e)
      {
        hoursComboBox_actionPerformed(e);
      }
    });
    hoursComboBox.removeAllItems();
    hoursComboBox.addItem("3");
    hoursComboBox.addItem("6");
    hoursComboBox.addItem("9");
    hoursComboBox.addItem("12");
    hoursComboBox.addItem("24");
    hoursComboBox.setSelectedIndex(0);

    windCheckBox.setText("WIND");
    windCheckBox.setSelected(true);
    windCheckBox.addActionListener(new ActionListener()
    {
      public void actionPerformed(ActionEvent e)
      {
        windCheckBox_actionPerformed(e);
      }
    });
    prmslCheckBox.setText("PRMSL");
    prmslCheckBox.setSelected(true);
    prmslCheckBox.addActionListener(new ActionListener()
    {
      public void actionPerformed(ActionEvent e)
      {
        prmslCheckBox_actionPerformed(e);
      }
    });
    rainCheckBox.setText("RAIN");
    rainCheckBox.setSelected(true);
    rainCheckBox.addActionListener(new ActionListener()
    {
      public void actionPerformed(ActionEvent e)
      {
        rainCheckBox_actionPerformed(e);
      }
    });
    generatedRequestLabel.setText("Generated Request");
    clipBoardButton.setText("Copy to Clipboard");
    clipBoardButton.addActionListener(new ActionListener()
    {
      public void actionPerformed(ActionEvent e)
      {
        clipBoardButton_actionPerformed(e);
      }
    });
    mailButton.setText("email client");
    mailButton.addActionListener(new ActionListener()
    {
      public void actionPerformed(ActionEvent e)
      {
        mailButton_actionPerformed(e);
      }
    });
    airmailButton.setText("Generate Airmail Request");
    airmailButton.addActionListener(new ActionListener()
    {
      public void actionPerformed(ActionEvent e)
      {
        airmailButton_actionPerformed(e);
      }
    });
    topPanel.add(gpsRadioButton,
                 new GridBagConstraints(2, 1, 1, 1, 0.0, 0.0, GridBagConstraints.WEST, GridBagConstraints.NONE,
                                        new Insets(0, 0, 0, 0), 0, 0));
    topPanel.add(manualPosRadioButton,
                 new GridBagConstraints(3, 1, 1, 1, 0.0, 0.0, GridBagConstraints.WEST, GridBagConstraints.NONE,
                                        new Insets(0, 0, 0, 0), 0, 0));
    topPanel.add(positionOriginLabel,
                 new GridBagConstraints(2, 0, 2, 1, 0.0, 0.0, GridBagConstraints.NORTHWEST, GridBagConstraints.NONE,
                                        new Insets(0, 0, 0, 0), 0, 0));
    topPanel.add(positionPanel,
                 new GridBagConstraints(2, 2, 2, 1, 0.0, 0.0, GridBagConstraints.NORTHWEST, GridBagConstraints.BOTH,
                                        new Insets(0, 0, 0, 0), 0, 0));
    positionPanel.setEnabled(false);
    positionPanel.showButton(false);
    durationPanel.add(numberOfDaysLabel, null);
    daysComboBox.removeAllItems();
    for (int i=1; i<=7; i++)
      daysComboBox.addItem(Integer.toString(i));
    daysComboBox.setSelectedIndex(4); // "5"

    daysComboBox.addActionListener(new ActionListener()
    {
      public void actionPerformed(ActionEvent e)
      {
        daysComboBox_actionPerformed(e);
      }
    });
    durationPanel.add(daysComboBox, null);
    durationPanel.add(intervalInHoursLabel, null);
    durationPanel.add(hoursComboBox, null);
    topPanel.add(durationPanel,
                 new GridBagConstraints(2, 3, 2, 1, 0.0, 0.0, GridBagConstraints.NORTHWEST, GridBagConstraints.BOTH,
                                        new Insets(5, 0, 0, 0), 0, 0));
    dataPanel.add(windCheckBox, null);
    dataPanel.add(prmslCheckBox, null);
    dataPanel.add(rainCheckBox, null);
    topPanel.add(dataPanel,
                 new GridBagConstraints(2, 4, 2, 1, 0.0, 0.0, GridBagConstraints.NORTHWEST, GridBagConstraints.BOTH,
                                        new Insets(0, 0, 0, 0), 0, 0));
    topPanel.add(generatedRequestLabel,
                 new GridBagConstraints(2, 5, 2, 1, 0.0, 0.0, GridBagConstraints.CENTER, GridBagConstraints.BOTH,
                                        new Insets(0, 0, 0, 0), 0, 0));

    buttonPanel.add(clipBoardButton, null);
    buttonPanel.add(mailButton, null);
    buttonPanel.add(airmailButton, null);
    mailButton.setToolTipText("<html>Will launch your default email client<br>so you can send your request to query@saildocs.com<br>... in case you're connected on the Internet.</html>");
    airmailButton.setToolTipText("<html>Will compose an email and place it in your Airmail outbox. Appropriate preferences need to be correctly set.</html>");
    topPanel.add(buttonPanel,
                 new GridBagConstraints(2, 6, 2, 1, 0.0, 0.0, GridBagConstraints.CENTER, GridBagConstraints.NONE,
                                        new Insets(0, 0, 0, 0), 0, 0));
    generatedRequestLabel.setText(composeRequest());
    NMEAContext.getInstance().addNMEAReaderListener(new NMEAReaderListener("SPOT", "Spot Composer Panel (2)")
      {
        @Override
        public void positionManuallyUpdated(GeoPos gp) 
        {
          updateData();
        }
      });
  }
  
  public void paintComponent(Graphics gr)
  {    
    ((Graphics2D)gr).setRenderingHint(RenderingHints.KEY_TEXT_ANTIALIASING,
                                      RenderingHints.VALUE_TEXT_ANTIALIAS_ON);      
    ((Graphics2D)gr).setRenderingHint(RenderingHints.KEY_ANTIALIASING,
                                      RenderingHints.VALUE_ANTIALIAS_ON);  
//  gr.setColor(Color.white);
    gr.fillRect(0, 0, this.getWidth(), this.getHeight());
    try
    {
      if (NMEAContext.getInstance().getCache().get(NMEADataCache.SAT_IN_VIEW) != null)
      {
        Map<Integer, SVData> hm = ( Map<Integer, SVData>)NMEAContext.getInstance().getCache().get(NMEADataCache.SAT_IN_VIEW);
        
        gr.setColor(Color.red);
        Font f = gr.getFont();
        gr.setFont(f.deriveFont(Font.BOLD));
        gr.drawString(hm.size() + " Satellites in view", 5, 20);
        int inUse = 0;
        // Display All Data Values:
        List<Object[]> dataTable = new ArrayList<Object[]>();
        dataTable.add(new Object[] {"Sat.#", "El.(\272)", "Z(\272)", "SNR(db)"}); // Titles
        for (Integer sn : hm.keySet())
        {
          SVData svd = hm.get(sn);
          int snr = svd.getSnr();
          if (snr > 0)
            inUse++;
          dataTable.add(new Object[] { DF2.format(svd.getSvID()), 
                                       Integer.toString(svd.getElevation()), 
                                       Integer.toString(svd.getAzimuth()), 
                                       Integer.toString(svd.getSnr()) });
        }
        gr.setColor(Color.red);
        gr.drawString(inUse + " Satellites in use", 5, 34);
        // Position and GRID
        try
        {
          GeoPos pos = (GeoPos)NMEAContext.getInstance().getCache().get(NMEADataCache.POSITION);
          if (pos != null)
          {
            int x = this.getWidth() / 2;
            gr.drawString("Position:" + pos.toString(), x, 20);
            gr.drawString("Square GRID:" + GeomUtil.gridSquare(pos.lat, pos.lng), x, 34);
          }
        }
        catch (Exception ex)
        {
          // No Cache
        }
        //
        String[][] data = new String[dataTable.size()][4];
        int i = 0;
        for (Object[] line : dataTable)
        {
          for (int j=0; j<4; j++)
            data[i][j] = (String)line[j];
          i++;
        }
        int x = 5;
        int y = 40;
        gr.setColor(Color.white);        
        Utilities.drawPanelTable(data, 
                                 gr, 
                                 new Point(x + 10, y + 20 + gr.getFont().getSize() + 2), 
                                 10, 
                                 2, 
                                 new int[] { Utilities.CENTER_ALIGNED, Utilities.RIGHT_ALIGNED, Utilities.RIGHT_ALIGNED, Utilities.RIGHT_ALIGNED }, 
                                 true, 
                                 Color.cyan,
                                 Color.blue,
                                 0.35f,
                                 0.9f);        

        gr.setFont(f);
      }
    }
    catch (Exception ex)
    {
      // No cache yet
    }
  }

  private void clipBoardButton_actionPerformed(ActionEvent e)
  {
    String str = generatedRequestLabel.getText();
    Clipboard clipboard = Toolkit.getDefaultToolkit().getSystemClipboard();
    StringSelection stringSelection = new StringSelection(str);
    clipboard.setContents(stringSelection, null);
    JOptionPane.showMessageDialog(this, "Request is in the clipboard.\nEmail it in plain text to query@saildocs.com.", "SPOT Request", JOptionPane.PLAIN_MESSAGE);
  }
  
  private void airmailButton_actionPerformed(ActionEvent e)
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
    File outboxDir = new File(airmailDir, "Outbox"); 
    if (!outboxDir.exists())
      outboxDir.mkdirs();
    
    Pattern pattern = Pattern.compile("([0-9]*)_" + airmailId.toUpperCase() + ".msg");
    
    File[] messages = outboxDir.listFiles();
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
    
    String strReq = generatedRequestLabel.getText();
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
    // Write in the outbox
    System.out.println("Message:\n" + messageContent);
    try
    {
      BufferedWriter br = new BufferedWriter(new FileWriter(new File(outboxDir, messageName + ".msg")));
      br.write(messageContent);
      br.close();
      JOptionPane.showMessageDialog(this, 
                                    "Email has been generated", 
                                    "AirMail SPOT request Generation", 
                                    JOptionPane.INFORMATION_MESSAGE);
    }
    catch (Exception ex)
    {
      ex.printStackTrace();
    }
  }
  
  private String composeRequest()
  {
    String request = "send spot:"; // 37.5N,122.5W|5,3|PRMSL,WIND,RAIN";
    GeoPoint gp = positionPanel.getPosition();
    request += (DF34.format(Math.abs(gp.getL())) + (gp.getL()>0?"N":"S"));
    request += ",";
    request += (DF34.format(Math.abs(gp.getG())) + (gp.getG()>0?"E":"W"));
    request += "|";
    request += (String)daysComboBox.getSelectedItem();
    request += ",";
    request += (String)hoursComboBox.getSelectedItem();
    request += "|";
    boolean first = true;
    if (prmslCheckBox.isSelected())
    {
      request += ((first?"":",") + "PRMSL");
      first = false;
    }
    if (windCheckBox.isSelected())
    {
      request += ((first?"":",") + "WIND");
      first = false;
    }
    if (rainCheckBox.isSelected())
    {
      request += ((first?"":",") + "RAIN");
      first = false;
    }
    return request;
  }
  
  private void updateData()
  {
    generatedRequestLabel.setText(composeRequest());
  }

  private void daysComboBox_actionPerformed(ActionEvent e)
  {
    updateData();
  }

  private void hoursComboBox_actionPerformed(ActionEvent e)
  {
    updateData();
  }

  private void windCheckBox_actionPerformed(ActionEvent e)
  {
    updateData();
  }

  private void prmslCheckBox_actionPerformed(ActionEvent e)
  {
    updateData();
  }

  private void rainCheckBox_actionPerformed(ActionEvent e)
  {
    updateData();
  }

  private void manualPosRadioButton_actionPerformed(ActionEvent e)
  {
    positionPanel.setEnabled(manualPosRadioButton.isSelected());
    positionPanel.showButton(manualPosRadioButton.isSelected());
  }

  private void gpsRadioButton_actionPerformed(ActionEvent e)
  {
    positionPanel.setEnabled(manualPosRadioButton.isSelected());
    positionPanel.showButton(manualPosRadioButton.isSelected());
  }

  private void mailButton_actionPerformed(ActionEvent e)
  {
    try
    {
      String href = null;
      System.out.println("Emailing [" + href + "]");
      
      if (Desktop.isDesktopSupported() && (Desktop.getDesktop()).isSupported(Desktop.Action.MAIL)) 
      {
        href = "mailto:query@saildocs.com?subject=SPOT%20Request&body=" + URLEncoder.encode(generatedRequestLabel.getText(), "UTF-8").replace("+", "%20");
      } 
      else 
      {
        JOptionPane.showMessageDialog(this, "Desktop doesn't support mailto.\nPlease send the request manually.", "SPOT Request", JOptionPane.ERROR_MESSAGE);
        throw new RuntimeException("Desktop doesn't support mailto.");
      }
      JOptionPane.showMessageDialog(this, "Make sure to send your email in PLAIN text.", "SPOT Request", JOptionPane.WARNING_MESSAGE);
      Desktop.getDesktop().mail(new URI(href));      
    }
    catch (UnsupportedOperationException uoe)
    {
      JOptionPane.showMessageDialog(this, "Operation not supported here!", "SPOT Request", JOptionPane.ERROR_MESSAGE);
    }
    catch (Exception ex)
    {
      ex.printStackTrace();
    }
  }
  
  public static void main_(String[] args)
  {

    Pattern pattern = Pattern.compile("([0-9]*)_WDC7278.msg");
    
    String[] messages = new String[] { "1234_WDC7278.msg", "1235_WDC7278.msg", "1236_WDC7278.msg" };
    for (String mess : messages)
    {
        String messName = mess;
        Matcher matcher = pattern.matcher(messName);
        while (matcher.find())
        {  
          String match = matcher.group(1).trim();
          System.out.println(match);
        }        
    }
  }
}
