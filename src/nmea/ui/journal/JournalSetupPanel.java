package nmea.ui.journal;

import java.awt.BorderLayout;
import java.awt.Dimension;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Insets;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.sql.Timestamp;

import java.text.DecimalFormat;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import javax.swing.JButton;
import javax.swing.JCheckBox;
import javax.swing.JComboBox;
import javax.swing.JFormattedTextField;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTable;
import javax.swing.JTextField;
import javax.swing.table.AbstractTableModel;
import javax.swing.table.TableModel;

import nmea.server.ctx.NMEAContext;
import nmea.server.ctx.NMEADataCache;

import ocss.nmea.parser.Angle180;
import ocss.nmea.parser.Angle360;
import ocss.nmea.parser.Depth;
import ocss.nmea.parser.Distance;
import ocss.nmea.parser.GeoPos;
import ocss.nmea.parser.Pressure;
import ocss.nmea.parser.Speed;
import ocss.nmea.parser.Temperature;
import ocss.nmea.parser.TrueWindDirection;
import ocss.nmea.parser.TrueWindSpeed;

import oracle.xml.parser.v2.DOMParser;
import oracle.xml.parser.v2.XMLDocument;

import org.w3c.dom.NodeList;

public class JournalSetupPanel
  extends JPanel
{
  private final static String LOG_CONFIG_FILE_NAME = "log-config.xml";
  
  private final static String GET_DATA_TYPE_STMT   = "select * from datatype";
  private final static String INSERT_LOG_HEADER    = "insert into datalog values (?, ?)";
  private final static String INSERT_LOG_DETAIL    = "insert into datacell values (?, ?, ?)";
  
  private transient LoggingThread lt = null;
  
  private static final String KEY   = "Data";
  private static final String VALUE = "Write";

  private final String configNames[] = new String[] { KEY, VALUE };
  private transient Object configData[][] = new Object[0][configNames.length];
  private transient TableModel configDataModel;
  private JTable configTable;
  
  private JScrollPane centerScrollPane = null;
  private JPanel centerPanel = new JPanel(new BorderLayout());
  private JPanel bottomPanel = new JPanel(new GridBagLayout());
  private JCheckBox selectAllCheckBox = new JCheckBox("Select All");
  private JButton applyButton = new JButton("Apply & Log");
  private JLabel logFrequencyLabel = new JLabel("Log frequency:");
  private JLabel commentLabel = new JLabel("Comment:");
  private final JFormattedTextField freqTextField = new JFormattedTextField(new DecimalFormat("##0"));
  private JTextField commentTextField = new JTextField();  
  private JComboBox unitComboBox = new JComboBox();
  
  private boolean logging = false;
  private List<String> data2log = null;

  public JournalSetupPanel()
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
    this.setLayout(new BorderLayout());
    this.setSize(new Dimension(683, 300));
    this.add(centerPanel, BorderLayout.CENTER);
    this.add(bottomPanel, BorderLayout.SOUTH);
    bottomPanel.add(selectAllCheckBox, new GridBagConstraints(0, 0, 1, 1, 0.0, 0.0, GridBagConstraints.CENTER, GridBagConstraints.NONE, new Insets(3, 5, 0, 0), 0, 0));
    selectAllCheckBox.addActionListener(new ActionListener()
      {
        public void actionPerformed(ActionEvent e)
        {
          if (selectAllCheckBox.isSelected())
          {
            // Check All
            for (int i=0; i<configData.length; i++)
              configData[i][1] = Boolean.TRUE;
          }
          else
          {
            // Uncheck All
            for (int i=0; i<configData.length; i++)
              configData[i][1] = Boolean.FALSE;
          }
          ((AbstractTableModel)configDataModel).fireTableDataChanged();
        }
      });
    bottomPanel.add(applyButton, new GridBagConstraints(1, 0, 1, 1, 0.0, 0.0, GridBagConstraints.CENTER, GridBagConstraints.NONE, new Insets(3, 0, 0, 0), 0, 0));
    applyButton.addActionListener(new ActionListener()
      {
        public void actionPerformed(ActionEvent e)
        {
          applyConfig();
          storeConfig();
        }
      });
    freqTextField.setMinimumSize(new Dimension(40, 20));
    freqTextField.setPreferredSize(new Dimension(40, 20));
    commentTextField.setSize(new Dimension(100, 20));
    commentTextField.setPreferredSize(new Dimension(200, 20));
    
    unitComboBox.removeAllItems();
    unitComboBox.addItem(new FrequencyUnit(1L,       "Millisecond(s)"));
    unitComboBox.addItem(new FrequencyUnit(1000L,    "Second(s)"));
    unitComboBox.addItem(new FrequencyUnit(60000L,   "Minute(s)"));
    unitComboBox.addItem(new FrequencyUnit(3600000L, "Hour(s)"));
    
    bottomPanel.add(logFrequencyLabel, new GridBagConstraints(2, 0, 1, 1, 0.0, 0.0, GridBagConstraints.CENTER, GridBagConstraints.NONE, new Insets(3, 5, 0, 0), 0, 0));
    bottomPanel.add(freqTextField,     new GridBagConstraints(3, 0, 1, 1, 0.0, 0.0, GridBagConstraints.CENTER, GridBagConstraints.NONE, new Insets(3, 0, 0, 0), 0, 0));
    bottomPanel.add(unitComboBox,      new GridBagConstraints(4, 0, 1, 1, 0.0, 0.0, GridBagConstraints.CENTER, GridBagConstraints.NONE, new Insets(3, 0, 0, 0), 0, 0));
    bottomPanel.add(commentLabel,      new GridBagConstraints(5, 0, 1, 1, 0.0, 0.0, GridBagConstraints.CENTER, GridBagConstraints.NONE, new Insets(3, 5, 0, 0), 0, 0));
    bottomPanel.add(commentTextField,  new GridBagConstraints(6, 0, 1, 1, 0.0, 0.0, GridBagConstraints.CENTER, GridBagConstraints.HORIZONTAL, new Insets(3, 0, 0, 0), 0, 0));
    freqTextField.setText("15");
    unitComboBox.setSelectedIndex(2); // 2: Minutes

    freqTextField.setHorizontalAlignment(JTextField.RIGHT);
    initTable();
    readData();
    ((AbstractTableModel)configDataModel).fireTableDataChanged();
  }

  private void initTable()
  {
    configDataModel = new AbstractTableModel()
      {
        public int getColumnCount()
        {
          return configNames.length;
        }

        public int getRowCount()
        {
          return configData.length;
        }

        public Object getValueAt(int row, int col)
        {
          return configData[row][col];
        }

        public String getColumnName(int column)
        {
          return configNames[column];
        }

        public Class getColumnClass(int c)
        {
          return getValueAt(1, c).getClass();
        }

        public boolean isCellEditable(int row, int col)
        {
          return (col == 1);
        }

        public void setValueAt(Object aValue, int row, int column)
        {
          configData[row][column] = aValue;
        }
      };
    configTable = new JTable(configDataModel);
    centerScrollPane = new JScrollPane(configTable);
    centerPanel.add(centerScrollPane, BorderLayout.CENTER);
  }
  
  private void storeConfig()
  {
    String configContent = "<log-config>\n";
    for (int i=0; i<configData.length; i++)
    {
      if (((Boolean)configData[i][1]).booleanValue())
        configContent += ("<" + ((DataType)configData[i][0]).getName() + "/>\n");
    }
    configContent += "</log-config>\n";
    try
    {
      BufferedWriter bw = new BufferedWriter(new FileWriter(LOG_CONFIG_FILE_NAME));
      bw.write(configContent);
      bw.close();
    }
    catch (Exception ex)
    {
      ex.printStackTrace();
    }
  }
  
  private void readData()
  {
    try
    {
      Connection conn = NMEAContext.getInstance().getDBConnection();
      Statement stmt = conn.createStatement();
      ResultSet rs = stmt.executeQuery(GET_DATA_TYPE_STMT);
      List<DataType> result = new ArrayList<DataType>();
      while (rs.next())
      {
        String s1 = rs.getString(1);
        String s2 = rs.getString(2);
        result.add(new DataType(s1, s2));
      }
      rs.close();
      configData = new Object[result.size()][2];
      int i = 0;
      for (DataType dt : result)
      {
        configData[i][0] = dt;
        configData[i][1] = Boolean.valueOf(false);
        i++;
      }
      // Read stored config
      try
      {
        DOMParser parser = NMEAContext.getInstance().getParser();
        XMLDocument doc = null;
        synchronized (parser)
        {
          parser.parse(new File(LOG_CONFIG_FILE_NAME).toURI().toURL());
          doc = parser.getDocument();
        }
        if (doc != null)
        {
          for (int x=0; x<configData.length; x++)
          {
            String dataName = ((DataType)configData[x][0]).getName();
            String xPath = "/log-config/" + dataName;
            NodeList nl = doc.selectNodes(xPath);
            if (nl.getLength() > 0)
              configData[x][1] = Boolean.TRUE;
          }
        }
      }
      catch (Exception ex)
      {
        System.out.println(ex.toString());
      }
    }
    catch (Exception ex)
    {
      ex.printStackTrace();
    }
  }
  
  private void applyConfig()
  {
    if (!logging)
    {
      List<String> config = new ArrayList<String>();
      for (int i=0; i<configData.length; i++)
      {
        if (((Boolean)configData[i][1]).booleanValue()) // Selected
          config.add(((DataType)configData[i][0]).getName());
      }
      // Broadcast ArrayList ?
      for (String s : config)
        System.out.println("-> " + s);
      
      data2log = config;
      logging = true;
      applyButton.setText("Stop Logging");
      lt = new LoggingThread();
      lt.start();
    }
    else
    {
      applyButton.setText("Apply & Log");
      logging = false;
      if (lt != null)
        lt.stopLogging();
      lt = null;
    }
  }
  
  private void log()
  {
    try
    {
//    System.out.println("Log.");
      Date now = new Date();
      Timestamp ts = new Timestamp(now.getTime());
      Connection c = NMEAContext.getInstance().getDBConnection();
      PreparedStatement pHeaderStmt = c.prepareStatement(INSERT_LOG_HEADER);
      PreparedStatement pDetailStmt = c.prepareStatement(INSERT_LOG_DETAIL);
      
      pHeaderStmt.setTimestamp(1, ts);
      pHeaderStmt.setString(2, commentTextField.getText());
      
      int nbLines = pHeaderStmt.executeUpdate();
      
      NMEADataCache cache = NMEAContext.getInstance().getCache();
      
      for (String key : data2log)
      {
//      System.out.println("== Log ==> Key [" + key + "]");
        double value2log = 0D;
        if (key.equals("BSP"))   
          value2log = ((Speed)cache.get(NMEADataCache.BSP)).getValue();
        else if (key.equals("SOG"))   
          value2log = ((Speed)cache.get(NMEADataCache.SOG)).getValue();
        else if (key.equals("TWS"))   
          value2log = ((TrueWindSpeed)cache.get(NMEADataCache.TWS)).getValue();
        else if (key.equals("TWD"))
          value2log = ((TrueWindDirection)cache.get(NMEADataCache.TWD)).getValue();
        else if (key.equals("LAT"))   
          value2log = ((GeoPos)cache.get(NMEADataCache.POSITION)).lat;
        else if (key.equals("LNG"))   
          value2log = ((GeoPos)cache.get(NMEADataCache.POSITION)).lng;
        else if (key.equals("HDG"))   
          value2log = ((Angle360)cache.get(NMEADataCache.HDG_TRUE)).getValue();
        else if (key.equals("COG"))   
          value2log = ((Angle360)cache.get(NMEADataCache.COG)).getValue();
        else if (key.equals("LOG"))   
          value2log = ((Distance)cache.get(NMEADataCache.LOG)).getValue();
        else if (key.equals("AWS"))   
          value2log = ((Speed)cache.get(NMEADataCache.AWS)).getValue();
        else if (key.equals("AWA"))   
          value2log = ((Angle180)cache.get(NMEADataCache.AWA)).getValue();
        else if (key.equals("CSP"))   
          value2log = ((Speed)cache.get(NMEADataCache.CSP)).getValue();
        else if (key.equals("CDR"))   
          value2log = ((Angle360)cache.get(NMEADataCache.CDR)).getValue();
        else if (key.equals("MWT"))   
          value2log = ((Temperature)cache.get(NMEADataCache.WATER_TEMP)).getValue();
        else if (key.equals("MTA"))   
          value2log = ((Temperature)cache.get(NMEADataCache.AIR_TEMP)).getValue();
        else if (key.equals("MMB"))   
          value2log = ((Pressure)cache.get(NMEADataCache.BARO_PRESS)).getValue();
        else if (key.equals("DBT"))   
          value2log = ((Depth)cache.get(NMEADataCache.DBT)).getValue();
        else 
          value2log = -1D;
        
        pDetailStmt.setTimestamp(1, ts);
        pDetailStmt.setString(2, key);
        boolean log = true;
        try { pDetailStmt.setDouble(3, value2log); }
        catch (SQLException nfe)
        {
          System.out.println(" - Exception for " + key + ":" + value2log);
          log = false;
        }
        if (log)
          nbLines = pDetailStmt.executeUpdate();
      }
      pHeaderStmt.close();
      pDetailStmt.close();
      c.commit();
    }
    catch (Exception ex)
    {
      ex.printStackTrace();
    }
    // Refresh view
    NMEAContext.getInstance().fireRefreshJournal();
  }
  
  private final class LoggingThread extends Thread
  {
    private boolean keepLogging = false;
    
    public LoggingThread()
    {
      super();
    }
    
    public void run()
    {
      keepLogging = true;
      while (keepLogging)
      {
        log();
        long sleep = 60000L; // One minute default
        try 
        { 
          sleep = Long.parseLong(freqTextField.getText()); 
          sleep *= ((FrequencyUnit)unitComboBox.getSelectedItem()).getValue();
        } 
        catch (NumberFormatException nfe) { nfe.printStackTrace(); }
        try { Thread.sleep(sleep); } catch (InterruptedException ie) {}
      }
      System.out.println("Logging stopped");
    }
    
    public void stopLogging()
    {
      keepLogging = false;
    }
  }
  
  public final class DataType
  {
    private String name = "";
    private String desc = "";
    
    public DataType(String name, String desc)
    {
      this.name = name;
      this.desc = desc;
    }
    
    public String toString()
    {
      return name + " - " + desc;
    }
    public String getName()
    {
      return this.name;
    }
    public String getDescription()
    {
      return this.desc;
    }
  }
  
  private final class FrequencyUnit
  {
    private long value = 1L;
    private String description = "Millisecond(s)";
    
    public FrequencyUnit(long val, String str)
    {
      this.value = val;
      this.description = str;
    }
    public String toString()
    {
      return this.description;
    }
    public long getValue()
    {
      return this.value;
    }
  }
}
