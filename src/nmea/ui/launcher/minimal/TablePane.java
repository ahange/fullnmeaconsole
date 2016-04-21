package nmea.ui.launcher.minimal;

import nmea.server.utils.HTTPServer;

import nmea.server.utils.ServerStop;

import java.awt.BorderLayout;

import java.awt.Dimension;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.ItemEvent;

import java.text.SimpleDateFormat;

import java.util.Date;

import java.util.Hashtable;

import javax.swing.JCheckBox;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTable;
import javax.swing.JTextField;
import javax.swing.table.AbstractTableModel;
import javax.swing.table.TableModel;
import ocss.nmea.parser.StringParsers;

public class TablePane extends JPanel
{
  private SimpleDateFormat dateFormat = new SimpleDateFormat("HH:mm:ss");
  private JCheckBox httpCheckBox = new JCheckBox();
  private JTextField httpPortTextField = new JTextField();

  private transient HTTPServer nmeaHttpServer = null;
  
  void init()
  {
    data = new Object[0][names.length];
    borderLayout1 = new BorderLayout();
    centerPanel = new JPanel();
    bottomPanel = new JPanel();
    borderLayout2 = new BorderLayout();
    centerScrollPane = null;
    topPanel = new JPanel();
    logCheckbox = new JCheckBox();
    messageField = new JLabel();
    borderLayout3 = new BorderLayout();
  }

  public TablePane(NMEAFrame f)
  {
    init();
    parent = f;
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
    setLayout(borderLayout1);
    centerPanel.setLayout(borderLayout2);
    bottomPanel.setLayout(borderLayout3);
    logCheckbox.setText("Log Data");
    logCheckbox.addActionListener(new ActionListener()
        {
          public void actionPerformed(ActionEvent e)
          {
            logCheckbox_actionPerformed(e);
          }
        });
    messageField.setText("Message");
    httpPortTextField.setText(System.getProperty("http.port", "6666"));
    httpPortTextField.setSize(new Dimension(40, 20));
    httpPortTextField.setPreferredSize(new Dimension(40, 20));
    httpPortTextField.setHorizontalAlignment(JTextField.CENTER);
    httpCheckBox.setText("Re-broadcast on HTTP port ");
    httpCheckBox.addActionListener(new ActionListener()
        {
          public void actionPerformed(ActionEvent e)
          {
            httpCheckBox_actionPerformed(e);
          }
        });
    add(centerPanel, BorderLayout.CENTER);
    bottomPanel.add(messageField, BorderLayout.CENTER);
    add(bottomPanel, BorderLayout.SOUTH);
    topPanel.add(logCheckbox, null);
    topPanel.add(httpCheckBox, null);
    topPanel.add(httpPortTextField, null);
    add(topPanel, BorderLayout.NORTH);
    initTable();
  }

  private void initTable()
  {
    dataModel = new AbstractTableModel() 
    {
      public int getColumnCount()
      {
        return names.length;
      }

      public int getRowCount()
      {
        return data.length;
      }

      public Object getValueAt(int row, int col)
      {
        return data[row][col];
      }

      public String getColumnName(int column)
      {
        return names[column];
      }

      public Class getColumnClass(int c)
      {
        return getValueAt(0, c).getClass();
      }

      public boolean isCellEditable(int row, int col)
      {
        return col == 0;
      }

      public void setValueAt(Object aValue, int row, int column)
      {
        data[row][column] = aValue;
      }
    };
    table = new JTable(dataModel);
    centerScrollPane = new JScrollPane(table);
    centerPanel.add(centerScrollPane, BorderLayout.CENTER);
  }

  public void addLineInTable(String s)
  {
    int len = data.length;
    Object newData[][] = new Object[len + 1][names.length];
    for(int i = 0; i < len; i++)
    {
      for(int j = 0; j < names.length; j++)
        newData[i][j] = data[i][j];
    }

    newData[len][0] = s;
    newData[len][1] = "";
    newData[len][2] = Boolean.valueOf(false);
    newData[len][3] = "";
    newData[len][4] = "";
    data = newData;
    ((AbstractTableModel)dataModel).fireTableDataChanged();
    table.repaint();
  }

  public void removeCurrentLine()
  {
    int selectedRow = table.getSelectedRow();
    if(selectedRow < 0)
    {
      JOptionPane.showMessageDialog(null, "Please choose a row to remove", "Removing an entry", 2);
    } else
    {
      int l = data.length;
      Object newData[][] = new Object[l - 1][names.length];
      int newInd = 0;
      for(int oldInd = 0; oldInd < l; oldInd++)
        if(oldInd != selectedRow)
        {
          for(int j = 0; j < names.length; j++)
            newData[newInd][j] = data[oldInd][j];

          newInd++;
        }

      data = newData;
      ((AbstractTableModel)dataModel).fireTableDataChanged();
      table.repaint();
    }
  }

  public String[] getKeys()
  {
    String sa[] = new String[data.length];
    for(int i = 0; i < data.length; i++)
      sa[i] = new String((String)data[i][0]);
    return sa;
  }

  Hashtable<String, Long> timemap = new Hashtable<String, Long>();
  
  public void setValue(String key, String val)
  {
    for(int i = 0; i < data.length; i++)
    {
      if(!((String)data[i][0]).toUpperCase().equals(key))
        continue;
      data[i][1] = val;
      
      // Validation
      data[i][2] = Boolean.valueOf(StringParsers.validCheckSum(val));
      
      long previous = 0L;
      try
      {
        previous = ((Long)timemap.get(key)).longValue();
      }
      catch(Exception ignore) { }
      long current = System.currentTimeMillis();
//    System.out.println("-- Key [" + key + "] Val:[" + val + "]");
      if (!key.substring(2).equals("GSV") || (key.substring(2).equals("GSV") && StringParsers.getMessNum(val)[StringParsers.MESS_NUM] == 1))
      {
        timemap.put(key, new Long(current));
        data[i][3] = dateFormat.format(new Date(current));
        if(previous != 0L)
          data[i][4] = Long.toString(current - previous);
      }
      break;
    }
    ((AbstractTableModel)dataModel).fireTableDataChanged();
    table.repaint();
  }

  private void logCheckbox_itemStateChanged(ItemEvent e)
  {
    parent.setGoLog(logCheckbox.isSelected());
  }

  public void setMessage(String mess)
  {
    messageField.setText(mess);
  }

//  static void logCheckbox_itemStateChanged(TablePane tablepane, ItemEvent itemevent)
//  {
//    tablepane.logCheckbox_itemStateChanged(itemevent);
//  }

  static final String SENTENCE_ID = "Sentence ID";
  static final String VALUE = "Value";
  static final String VALID = "Valid";
  static final String DATE  = "Read at";
  static final String DELTA = "delta (ms)";
  
  final String names[] = new String[] {
      "Sentence ID", "Value", "Valid", "Read at", "delta (ms)"
    };
  private transient Object data[][] = new Object[0][names.length];
  private transient TableModel dataModel;
  private JTable table;
  private BorderLayout borderLayout1 = new BorderLayout();
  private JPanel centerPanel = new JPanel();
  private JPanel bottomPanel = new JPanel();
  private BorderLayout borderLayout2 = new BorderLayout();
  private JScrollPane centerScrollPane = null;
  private JPanel topPanel = new JPanel();
  private JCheckBox logCheckbox = new JCheckBox();
  private NMEAFrame parent;
  private JLabel messageField = new JLabel();
  private BorderLayout borderLayout3 = new BorderLayout();

  private void httpCheckBox_actionPerformed(ActionEvent e)
  {
    httpPortTextField.setEnabled(!httpCheckBox.isSelected());
    System.setProperty("http.port", httpPortTextField.getText());
    if (httpCheckBox.isSelected())
    {
      // Create listener
      String[] prms = new String[] 
        {
          "-verbose=n",
          "-fmt=xml"
        };
      try { nmeaHttpServer = new HTTPServer(prms, "nmea-config.properties", data); }
      catch (Exception ex)
      {
        ex.printStackTrace();
      }
    }
    else
    {
      // remove listener
       try
       {
         ServerStop client = new ServerStop();
         String port = System.getProperty("http.port", "6666");
         String host = System.getProperty("http.host", "localhost");
         String resp = client.getResponse(host, port, "/exit");
       }
       catch (Exception ex)
       {
         ex.printStackTrace();
       }
      nmeaHttpServer = null;
    }
  }

  private void logCheckbox_actionPerformed(ActionEvent e)
  {
    parent.parent.setGoLog(logCheckbox.isSelected());
  }
}
