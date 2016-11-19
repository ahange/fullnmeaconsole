package nmea.ui.viewer;

import utils.JTableFocusChangeListener;
import nmea.server.ctx.NMEAContext;
import nmea.server.utils.Utils;

import nmea.event.NMEAReaderListener;

import nmea.local.LogisailResourceBundle;

import nmea.server.constants.Constants;

import nmea.ui.NMEAFrameInterface;
import nmea.ui.widgets.ConfigNamePanel;

import java.awt.BorderLayout;
import java.awt.Component;
import java.awt.Dimension;
import java.awt.KeyboardFocusManager;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

import java.io.File;
import java.io.FileOutputStream;

import java.text.SimpleDateFormat;

import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.Hashtable;

import java.util.List;

import javax.swing.JButton;
import javax.swing.JCheckBox;
import javax.swing.JComboBox;
import javax.swing.JComponent;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JSeparator;
import javax.swing.JTable;
import javax.swing.SwingConstants;
import javax.swing.table.AbstractTableModel;
import javax.swing.table.TableCellRenderer;
import javax.swing.table.TableModel;

import nmea.server.ctx.NMEADataCache;

import ocss.nmea.parser.StringParsers;

import oracle.xml.parser.v2.DOMParser;
import oracle.xml.parser.v2.XMLDocument;
import oracle.xml.parser.v2.XMLElement;

import org.w3c.dom.NodeList;


public class ViewerTablePane
     extends JPanel
{
  private ViewerTablePane instance = this;
  private HashMap<String, String> userConfig = new HashMap<String, String>();
  private ConfigNamePanel cnp = null;
  
  private final static String config[][] = new String[][]
  {
    new String[] { "GPS Only",              "GLL,RMC,RMB" },
    new String[] { "Nav Station",           "HDG,HDM,MWV,VHW" },
    new String[] { "GPS + NKE Nav Station", "GLL,HDG,HDM,VHW,VTG" },
    new String[] { "GPS + B&G Nav Station", "GLL,HDG,HDM,MWV,RMC,VHW" }
// ,"Everything"
  };
  
  private static final String SELECTED = LogisailResourceBundle.buildMessage("select");  
  private static final String SENTENCE_ID = LogisailResourceBundle.buildMessage("sentence-id");  
  private static final String DESCRIPTION = LogisailResourceBundle.buildMessage("description");  
  private static final String VALUE = LogisailResourceBundle.buildMessage("value");
  private static final String VALID = LogisailResourceBundle.buildMessage("valid");
  private static final String DATE = LogisailResourceBundle.buildMessage("read-at");
  private static final String DELTA = LogisailResourceBundle.buildMessage("delta");

  private final String names[] = new String[]
    { SELECTED, SENTENCE_ID, DESCRIPTION, VALUE, VALID, DATE, DELTA };
  
  private final static int SELECTED_POS    = 0;
  private final static int SENTENCE_ID_POS = 1;
  private final static int DESCRIPTION_POS = 2;
  private final static int VALUE_POS       = 3;
  private final static int VALID_POS       = 4;
  private final static int DATE_POS        = 5;
  private final static int DELTA_POS       = 6;
  
  private String[] currentSentences = null;
  
  private transient Object data[][] = new Object[0][names.length];
  private transient TableModel dataModel;
  private JTable table;
  private BorderLayout borderLayout1 = new BorderLayout();
  private JPanel centerPanel = new JPanel();
  private JPanel bottomPanel = new JPanel();
  private JComboBox predefList = new JComboBox();
  private JButton applyButton = new JButton(LogisailResourceBundle.buildMessage("apply"));
  private JButton revertButton = new JButton(LogisailResourceBundle.buildMessage("reset"));
  private JCheckBox logCheckBox = new JCheckBox("Log checked sentences");
  private JCheckBox addDateCheckBox = new JCheckBox("Time in Log");
  private BorderLayout borderLayout2 = new BorderLayout();
  private JScrollPane centerScrollPane = null;
  private JPanel topPanel = new JPanel();
  private transient NMEAFrameInterface parent;
  private BorderLayout borderLayout3 = new BorderLayout();
  private SimpleDateFormat dateFormat = new SimpleDateFormat("HH:mm:ss");
  private JSeparator jSeparator1 = new JSeparator();

  private void init()
  {   
    data = new Object[0][names.length];
    borderLayout1 = new BorderLayout();
    centerPanel = new JPanel();
    borderLayout2 = new BorderLayout();
    centerScrollPane = null;
    topPanel = new JPanel();
    borderLayout3 = new BorderLayout();
  }

  public ViewerTablePane(NMEAFrameInterface f)
  {
    init();
    parent = f;
    try
    {
      jbInit();
    }
    catch (Exception e)
    {
      e.printStackTrace();
    }
  }

  public void setCurrentSentences(String[] sa)
  {
    currentSentences = sa;
    setSelectedSentences();
  }
  
  private void setSelectedSentences()
  {
    for (int i=0; i<data.length; i++)
    {
      if (Utils.isInArray(((String)data[i][SENTENCE_ID_POS]), currentSentences))
        data[i][SELECTED_POS] = Boolean.valueOf(true);
      else
        data[i][SELECTED_POS] = Boolean.valueOf(false);
    }
  }
  
  private void jbInit()
    throws Exception
  {
    // Setup HashMap for user's config
    try
    {
      DOMParser parser = NMEAContext.getInstance().getParser();
      parser.parse(new File(Utils.USER_CONFIG).toURI().toURL());
      XMLDocument doc = parser.getDocument();
      NodeList nl = doc.selectNodes("//lgsl:config", Utils.LgSlResolver);
      for (int i=0; i<nl.getLength(); i++)
      {
        XMLElement elmt = (XMLElement)nl.item(i);
        String key      = elmt.getAttribute("name");
        String value    = elmt.getAttribute("data");
        userConfig.put(key, value);
      }
    }
    catch (Exception ignore) 
    {
      System.err.println("ViewerTableListener:" + ignore.getLocalizedMessage());
//    ignore.printStackTrace();
    }

    NMEAContext.getInstance().addNMEAReaderListener(new NMEAReaderListener(Constants.NMEA_SERVER_LISTENER_GROUP_ID, "Viewer")
     {
       public void saveUserConfig() 
       {
         System.out.println("Saving user config.");
         String values = "";
         for (int i=0; i<data.length; i++)
         {
           if (((Boolean) data[i][SELECTED_POS]).booleanValue())
             values += ((values.length()==0?"":",") + ((String)data[i][SENTENCE_ID_POS]));
//         System.out.println("Values [" + values + "]");
         }
         // Look for such an exiting config
         if (userConfig.containsValue(values))
         {
           String configName = "";
           for (String s : userConfig.keySet())
           {
             String val = userConfig.get(s);
             if (val.equals(values))
             {
               configName = s;
               break;
             }
           } 
           JOptionPane.showMessageDialog(instance, LogisailResourceBundle.buildMessage("already-exists") + configName, LogisailResourceBundle.buildMessage("user-config"), 
                                         JOptionPane.ERROR_MESSAGE);
         }
         else
         {
           if (cnp == null)
             cnp = new ConfigNamePanel();
           int resp = JOptionPane.showConfirmDialog(instance, 
                                                    cnp, LogisailResourceBundle.buildMessage("user-config"), 
                                                    JOptionPane.OK_CANCEL_OPTION, 
                                                    JOptionPane.PLAIN_MESSAGE);
           if (resp == JOptionPane.OK_OPTION)
           {
             String configName = cnp.getName();
             if (userConfig.containsKey(configName)) // FIXME all the configs.
               JOptionPane.showMessageDialog(instance, LogisailResourceBundle.buildMessage("name-exists"), LogisailResourceBundle.buildMessage("user-config"), 
                                             JOptionPane.WARNING_MESSAGE);
             // Actual save
             userConfig.put(configName, values);
             saveUserConfigs();
             // Update current list
             setupConfigList();
           }
         }
       }       
     });
    
    setLayout(borderLayout1);
    centerPanel.setLayout(borderLayout2);
    applyButton.addActionListener(new ActionListener()
      {
        public void actionPerformed(ActionEvent e)
        {
          applyButton_actionPerformed(e);
        }
      });
    revertButton.addActionListener(new ActionListener()
      {
        public void actionPerformed(ActionEvent e)
        {
          setSelectedSentences();
        }
      });
    logCheckBox.addActionListener(new ActionListener()
      {
        public void actionPerformed(ActionEvent e)
        {
          logCheckBox_actionPerformed(e);
        }
      });
    jSeparator1.setOrientation(SwingConstants.VERTICAL);
    jSeparator1.setMinimumSize(new Dimension(1, 20));
    jSeparator1.setPreferredSize(new Dimension(2, 20));
    add(centerPanel, BorderLayout.CENTER);
    add(topPanel, BorderLayout.NORTH);
    String withLogger = System.getProperty("with.logger", "false");
    if ("true".equals(withLogger))
      add(bottomPanel, BorderLayout.SOUTH);
    
    setupConfigList();    

    bottomPanel.add(new JLabel("Config:"), null);
    bottomPanel.add(predefList, null);
    bottomPanel.add(applyButton, null);
    bottomPanel.add(revertButton, null);
    bottomPanel.add(jSeparator1, null);
    bottomPanel.add(logCheckBox, null);
    bottomPanel.add(addDateCheckBox, null);

    initTable();
  }
  
  public void saveUserConfigs()
  {
    XMLDocument newList = new XMLDocument();
    XMLElement root = (XMLElement)newList.createElementNS(Utils.LOGISAIL_NS, "lgsl:config-list");
    newList.appendChild(root);
    for (String k : userConfig.keySet())
    {
      String val = userConfig.get(k);
      XMLElement config = (XMLElement)newList.createElementNS(Utils.LOGISAIL_NS, "lgsl:config");
      root.appendChild(config);
      config.setAttribute("name", k);
      config.setAttribute("data", val);
    }    
    try
    {
      FileOutputStream fos = new FileOutputStream(new File(Utils.USER_CONFIG));
      newList.print(fos);
      fos.close();
    }
    catch (Exception ex)
    {
      ex.printStackTrace();
    }
  }
  
  private void setupConfigList()
  {
    predefList.removeAllItems();
    for (int i=0; i<config.length; i++)
    {
      NameNMEAString ns = new NameNMEAString(config[i][0], config[i][1]);
      predefList.addItem(ns);
    }

    if (userConfig != null)
    {
      for (String s : userConfig.keySet())
      {
        NameNMEAString ns = new NameNMEAString(s, userConfig.get(s));
        predefList.addItem(ns);
      }
    }
    // Everything
    predefList.addItem(new NameNMEAString("Everything", "*"));
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
            return col == 0; // Selected
          }

          public void setValueAt(Object aValue, int row, int column)
          {
//            if (column == 0)
//              System.out.println("Selected:"+ aValue.toString());
            data[row][column] = aValue;
          }
        };
    table = new JTable(dataModel)        
      {
          /* For the tooltip text */
          public Component prepareRenderer(TableCellRenderer renderer, int rowIndex, int vColIndex)
          {
            Component c = super.prepareRenderer(renderer, rowIndex, vColIndex);
            if (c instanceof JComponent)
            {
              JComponent jc = (JComponent) c;
              try
              {
                jc.setToolTipText(getValueAt(rowIndex, vColIndex).toString());
              }
              catch (Exception ex)
              {
                System.err.println("ViewerTablePane:" + ex.getMessage());
              }
            }
            return c;
          }
        };
//  table.setAutoResizeMode(JTable.AUTO_RESIZE_OFF);
//  table.setAutoResizeMode(JTable.AUTO_RESIZE_LAST_COLUMN);
//  table.setAutoResizeMode(JTable.AUTO_RESIZE_SUBSEQUENT_COLUMNS);
//  table.getColumnModel().getColumn(0).setPreferredWidth(30); // Select
    table.getColumn(SELECTED).setPreferredWidth(40); // Select
    table.getColumn(SELECTED).setMaxWidth(60);
    table.getColumn(SENTENCE_ID).setPreferredWidth(60); // Sentence ID
    table.getColumn(SENTENCE_ID).setMaxWidth(70);
    table.getColumn(VALID).setPreferredWidth(40); // Valid
    table.getColumn(VALID).setMaxWidth(60); // Valid
    table.getColumn(DATE).setPreferredWidth(60); // Read at
    table.getColumn(DATE).setMaxWidth(60); // Read at
    table.getColumn(DELTA).setPreferredWidth(60); // delta
    table.getColumn(DELTA).setMaxWidth(60); // delta

    centerScrollPane = new JScrollPane(table);
    centerPanel.add(centerScrollPane, BorderLayout.CENTER);
    KeyboardFocusManager.getCurrentKeyboardFocusManager().addPropertyChangeListener(new JTableFocusChangeListener(table));
  }

  public void addLineInTable(String s)
  {
    int len = data.length;
    Object newData[][] = new Object[len + 1][names.length];
    for (int i = 0; i < len; i++)
    {
      for (int j = 0; j < names.length; j++)
        newData[i][j] = data[i][j];
    }
    
    newData[len][SELECTED_POS]    = Boolean.valueOf(false);
    newData[len][SENTENCE_ID_POS] = s;
    String desc = "";
    try { desc = Constants.getInstance().getNMEAMap().get(s.substring(2)); } 
    catch (Exception ignore) 
    {
      System.out.println("For [" + s + "]");
    }
    newData[len][DESCRIPTION_POS] = (desc!=null?desc:"[Non NMEA Standard]");
    newData[len][VALUE_POS]       = "";
    newData[len][VALID_POS]       = Boolean.valueOf(false);
    newData[len][DATE_POS]        = "";
    newData[len][DELTA_POS]       = "";
    data = newData;
    ((AbstractTableModel)dataModel).fireTableDataChanged();
    
    setSelectedSentences();
    
    table.repaint();
  }

  public void removeCurrentLine()
  {
    int selectedRow = table.getSelectedRow();
    if (selectedRow < 0)
    {
      JOptionPane.showMessageDialog(null, "Please choose a row to remove", "Removing an entry", 2);
    }
    else
    {
      int l = data.length;
      Object newData[][] = new Object[l - 1][names.length];
      int newInd = 0;
      for (int oldInd = 0; oldInd < l; oldInd++)
      {
        if (oldInd != selectedRow)
        {
          for (int j = 0; j < names.length; j++)
            newData[newInd][j] = data[oldInd][j];
          newInd++;
        }
      }
      data = newData;
      ((AbstractTableModel) dataModel).fireTableDataChanged();
      table.repaint();
    }
  }
  
  public String[] getKeys() 
  {
    String sa[] = new String[data.length];
    for (int i = 0; i < data.length; i++)
      sa[i] = (String)data[i][SENTENCE_ID_POS];
    return sa;
  }

  private Hashtable<String, Long> timemap = new Hashtable<String, Long>();

  public void setValue(String key, String val, NMEADataCache ndc)
  {
//  System.out.println("VP, setValue, key[" + key + "], val [" + val + "]");
    
    // Isolate and Calculate values
    Utils.parseAndCalculate(key, val, ndc);
    
    boolean found = false;
    for (int i = 0; i < data.length; i++)
    {
      if (!((String) data[i][SENTENCE_ID_POS]).toUpperCase().equals(key))
        continue;
      found = true;
      data[i][VALUE_POS] = val;

      // Validation
      boolean ok = StringParsers.validCheckSum(val);
//    if (!ok)
//      System.out.println("Bad sentence:\n" + val);
      data[i][VALID_POS] = Boolean.valueOf(ok);

      long previous = 0L;
      try
      {
        if (timemap != null)
        {
          Long v = timemap.get(key);
          if (v != null)
            previous = (v).longValue();
        }
      }
      catch (Exception ignore)
      {
        System.err.println("Oops!");
        ignore.printStackTrace();
      }
      if (!key.substring(2).equals("GSV") || (key.substring(2).equals("GSV") && StringParsers.getMessNum(val)[StringParsers.MESS_NUM] == 1))
      {      
        long current = System.currentTimeMillis();
        timemap.put(key, new Long(current));
        data[i][DATE_POS] = dateFormat.format(new Date(current));
        if (previous != 0L)
          data[i][DELTA_POS] = Long.toString(current - previous);
      }
      break;
    }
    if (!found) // && StringParsers.validCheckSum(val)) // Added. Add to table only if checksum ok
    {
      addLineInTable(key);
      data[getKeys().length - 1][VALUE_POS] = val;

      // Validation
      data[getKeys().length - 1][VALID_POS] = Boolean.valueOf(StringParsers.validCheckSum(val));
      if (!key.substring(2).equals("GSV") || (key.substring(2).equals("GSV") && StringParsers.getMessNum(val)[StringParsers.MESS_NUM] == 1))
      {      
        long current = System.currentTimeMillis();
        timemap.put(key, new Long(current));
        data[getKeys().length - 1][DATE_POS] = dateFormat.format(new Date(current));
      }
    }
    ((AbstractTableModel) dataModel).fireTableDataChanged();
    table.repaint();
  }

  private void applyButton_actionPerformed(ActionEvent e)
  {
    int selectedIndex = predefList.getSelectedIndex();
    
    if (selectedIndex == predefList.getItemCount() - 1 ) // Select All, Everything
    {
      for (int j=0; j<data.length; j++)
        data[j][SELECTED_POS] = Boolean.valueOf(true);
      return;
    }
    
    String[] nmeaSentences = ((NameNMEAString)predefList.getSelectedItem()).getStrings().split(",");
    // Reset
    for (int j=0; j<data.length; j++)
      data[j][SELECTED_POS] = Boolean.valueOf(false);
    
    List<String> missingSentences = new ArrayList<String>();
    
    for (int i=0; i<nmeaSentences.length; i++)
    {
//    System.out.println(nmeaSentences[i]);
      boolean found = false;
      for (int j=0; j<data.length; j++)
      {
        String nmeaString = nmeaSentences[i];
        if ((nmeaString.length() == 3 && ((String)data[j][SENTENCE_ID_POS]).substring(2).equals(nmeaString)) ||
            (nmeaString.length() == 5 && ((String)data[j][SENTENCE_ID_POS]).equals(nmeaString)))
        {
          data[j][SELECTED_POS] = Boolean.valueOf(true);
          found = true;
          break;
        }
      }
      if (!found)
        missingSentences.add(nmeaSentences[i]);
    }
    if (missingSentences.size() > 0)
    {
      System.out.println("Missing Sentences!");
      String mess = "Missing Sentences:\n";
      for (String s : missingSentences)
      {
        mess += (s + "\n");
      }
      JOptionPane.showMessageDialog(this, mess, "Missing Sentences", JOptionPane.WARNING_MESSAGE);
    }
  }

  private void logCheckBox_actionPerformed(ActionEvent e)
  {
    boolean log = logCheckBox.isSelected();
    if (log)
    {
      List<String> toLog = new ArrayList<String>();
      for (int i=0; i<data.length; i++)
      {
        if (((Boolean) data[i][SELECTED_POS]).booleanValue())
          toLog.add((String)data[i][SENTENCE_ID_POS]);
      }
      String[] sa = new String[toLog.size()];
      sa = toLog.toArray(sa);
      parent.setSentencesToLog(sa);
      parent.writeSentencesToLog(sa);      
    }
    NMEAContext.getInstance().fireLogChanged(log, addDateCheckBox.isSelected());
    addDateCheckBox.setEnabled(!logCheckBox.isSelected());
  }
  
  public void setUserConfig(HashMap<String, String> userConfig)
  {
    this.userConfig = userConfig;
    setupConfigList();
  }

  public HashMap<String, String> getUserConfig()
  {
    return userConfig;
  }

  private class NameNMEAString
  {
    private String name;
    private String strings;
    public NameNMEAString()
    { }
    public NameNMEAString(String k, String v)
    {
      this.name = k;
      this.strings = v;
    }

    public void setName(String name)
    {
      this.name = name;
    }

    public String getName()
    {
      return name;
    }

    public void setStrings(String strings)
    {
      this.strings = strings;
    }

    public String getStrings()
    {
      return strings;
    }
    
    public String toString()
    {
      return name;
    }
    
    public String getValue()
    {
      return strings;
    }
  }
}
