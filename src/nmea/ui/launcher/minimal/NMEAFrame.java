package nmea.ui.launcher.minimal;

import java.awt.BorderLayout;
import java.awt.Dimension;

import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

import java.io.File;

import javax.swing.JButton;
import javax.swing.JPanel;
import javax.swing.JTextField;
import javax.swing.BorderFactory;
import javax.swing.ImageIcon;
import javax.swing.JFrame;
import javax.swing.JLabel;

public class NMEAFrame extends JFrame
{
  private JButton helpButton = new JButton();
  private ImageIcon imageHelp = 
    new ImageIcon(NMEAFrame.class.getResource("help.png"));

  public NMEAFrame(DataLogger caller)
  {
    parent = caller;
    try
    {
      jbInit();
    }
    catch(Exception e)
    {
      e.printStackTrace();
    }
  }

  private void jbInit()
    throws Exception
  {
    getContentPane().setLayout(borderLayout1);
    setSize(new Dimension(400, 300));
    setTitle("NMEA Reader and Logger");
    topPanel.setBorder(BorderFactory.createEtchedBorder(1));
    jLabel1.setText("NMEA Device Prefix");
    String prefix = "";
    String array[] = null;
    prefix = System.getProperty("device.prefix");
    String sentences = System.getProperty("nmea.sentences");
    String patternStr = ",";
    array = sentences.split(patternStr);
    for (int i=0; i<array.length; i++)
      System.out.println("NMEA Sentence:" + array[i]);
      
    prefixFld.setText(prefix);
    prefixFld.setPreferredSize(new Dimension(20, 20));
    prefixFld.setHorizontalAlignment(0);
    prefixFld.setEditable(false);
    helpButton.setToolTipText("Help!");
    helpButton.setIcon(imageHelp);
    helpButton.setActionCommand("help");
    helpButton.setMinimumSize(new Dimension(24, 24));
    helpButton.setPreferredSize(new Dimension(24, 24));
    helpButton.addActionListener(new ActionListener()
        {
          public void actionPerformed(ActionEvent e)
          {
            helpButton_actionPerformed(e);
          }
        });
    getContentPane().add(bottomPanel, BorderLayout.SOUTH);
    getContentPane().add(tp, BorderLayout.CENTER);
    topPanel.add(jLabel1, null);
    topPanel.add(prefixFld, null);
    topPanel.add(helpButton, null);
    getContentPane().add(topPanel, BorderLayout.NORTH);
    for (int i=0; i<array.length; i++)
      tp.addLineInTable(array[i]);

  }

  public void setValue(String key, String val)
  {
    tp.setValue(key, val);
  }

  public void setGoLog(boolean b)
  {
    parent.setGoLog(b);
  }

  public void setMessage(String mess)
  {
    tp.setMessage(mess);
  }

  transient DataLogger parent;

  private BorderLayout borderLayout1 = new BorderLayout();
  private JPanel bottomPanel = new JPanel();
  private TablePane tp = new TablePane(this);
  boolean reading = false;
  private JPanel topPanel = new JPanel();
  private JLabel jLabel1 = new JLabel();
  private JTextField prefixFld = new JTextField();

  private void helpButton_actionPerformed(ActionEvent e)
  {
    showHelp();
  }
  
  private void showHelp()
  {
    try
    {
      String docFileName = System.getProperty("user.dir") + File.separator + "doc" + File.separator + "index.html";
      Runtime.getRuntime().exec("cmd /k start " + docFileName);
    }
    catch (Exception e)
    {
      e.printStackTrace();
    }
  }  
}
