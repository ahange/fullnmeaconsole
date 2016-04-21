package nmea.ui.journal;

import java.awt.BorderLayout;
import java.awt.Component;
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
import java.text.SimpleDateFormat;

import java.util.ArrayList;
import java.util.Date;

import java.util.List;

import javax.swing.JButton;
import javax.swing.JCheckBox;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTable;
import javax.swing.table.AbstractTableModel;
import javax.swing.table.TableCellRenderer;
import javax.swing.table.TableColumn;
import javax.swing.table.TableModel;

import nmea.server.ctx.NMEAContext;

import nmea.server.utils.Utils;

import nmea.event.NMEAReaderListener;

import nmea.server.constants.Constants;

import ocss.nmea.utils.WindUtils;

import user.util.GeomUtil;


public class JournalDataPanel
  extends JPanel
{
  private static final String GET_DATA_TYPE_STMT = "select * from datatype order by 1";
  private static final String GET_JOURNAL_HEADER = "select log.logtime, " + 
                                                   "       log.comment " + 
                                                   "from datalog log " + 
                                                   "order by log.logtime desc";
  private static final String GET_JOURNAL_HEADER_2 = "select log.logtime, " + 
                                                     "       log.comment " + 
                                                     "from datalog log " + 
                                                     "order by log.logtime asc";
  private static final String GET_JOURNAL_DETAIL = "select data.data " + 
                                                   "from datacell data " + 
                                                   "where data.datatime = ? " +
                                                   "  and data.dataid = ? ";
  private static final String GET_JOURNAL_DETAIL_2 = "select data.dataid, " + 
                                                     "       data.data " +
                                                     "from datacell data " + 
                                                     "where data.datatime = ? ";
  
  private static final String DELETE_JOURNAL = "delete from datalog"; // Cascade constraint in DB
  
  private static final DecimalFormat SPEED_DIST_FMT = new DecimalFormat("##0.00");
  private static final DecimalFormat DIRECTION_FMT  = new DecimalFormat("000'\272'");
  private static final DecimalFormat DEPTH_FMT      = new DecimalFormat("##0.0 'm'");
  private static final DecimalFormat TEMP_FMT       = new DecimalFormat("##0.0'\272C'");
  private static final SimpleDateFormat DATE_FMT    = new SimpleDateFormat("dd MMM yyyy HH:mm:ss");
  
  private static final SimpleDateFormat DATE_ONLY_FMT = new SimpleDateFormat("EEEE, dd MMM yyyy");
  private static final SimpleDateFormat TIME_ONLY_FMT = new SimpleDateFormat("HH:mm:ss, z");
  private static final SimpleDateFormat TO_XSD_DURATION = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss'.000'");

  private static final String DATE_TIME = "Date";
  private static final String COMMENT   = "Comment";

  private String journalNames[] = new String[] { DATE_TIME, COMMENT };
  private transient Object journalData[][] = new Object[0][journalNames.length];
  private transient TableModel journalDataModel;
  private JTable journalTable;

  private JScrollPane centerScrollPane = null;
  private JPanel centerPanel = new JPanel(new BorderLayout());
  private JPanel bottomPanel = new JPanel(new GridBagLayout());
  private JButton resetButton   = new JButton("Reset");
  private JButton refreshButton = new JButton("Refresh");
  private JButton publishButton = new JButton("Publish");
  private JCheckBox collapseCheckBox = new JCheckBox("Collapse empty columns");

  public JournalDataPanel()
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
    this.add(centerPanel, BorderLayout.CENTER);
    this.add(bottomPanel, BorderLayout.SOUTH);
    bottomPanel.add(resetButton, new GridBagConstraints(0, 0, 1, 1, 0.0, 0.0, GridBagConstraints.CENTER, GridBagConstraints.NONE, new Insets(3, 0, 0, 0), 0, 0));
    resetButton.addActionListener(new ActionListener()
      {
        public void actionPerformed(ActionEvent e)
        {
          resetJournal();
        }
      });
    bottomPanel.add(refreshButton, new GridBagConstraints(1, 0, 1, 1, 0.0, 0.0, GridBagConstraints.CENTER, GridBagConstraints.NONE, new Insets(3, 2, 0, 0), 0, 0));
    refreshButton.addActionListener(new ActionListener()
      {
        public void actionPerformed(ActionEvent e)
        {
          refreshJournal();
        }
      });    
    bottomPanel.add(publishButton, new GridBagConstraints(2, 0, 1, 1, 0.0, 0.0, GridBagConstraints.CENTER, GridBagConstraints.NONE, new Insets(3, 2, 0, 0), 0, 0));
    publishButton.addActionListener(new ActionListener()
      {
        public void actionPerformed(ActionEvent e)
        {
          publishJournal();
        }
      });
    
    bottomPanel.add(collapseCheckBox, new GridBagConstraints(3, 0, 1, 1, 0.0, 0.0, GridBagConstraints.CENTER, GridBagConstraints.NONE, new Insets(3, 2, 0, 0), 0, 0));
    collapseCheckBox.addActionListener(new ActionListener()
      {
        public void actionPerformed(ActionEvent e)
        {
          if (collapseCheckBox.isSelected())
          {
            for (int i=0; i<journalTable.getColumnCount(); i++)
            {
              boolean empty = true;
              for (int j=0; j<journalData.length; j++)
              {
                if (journalData[j][i].toString().trim().length() > 0)
                {
                  empty = false;
                  break;
                }
              }
              if (empty)
              {
//              System.out.println("Collapsing colum#" + i);
                TableColumn col = journalTable.getColumnModel().getColumn(i);
//              System.out.println("Was " + col.getPreferredWidth());
                col.setMinWidth(0);
                col.setPreferredWidth(0);
              }
            }
          }
          else
          {
//          System.out.println("Expand");
            for (int i=0; i<journalTable.getColumnCount(); i++)
            {
              TableColumn col = journalTable.getColumnModel().getColumn(i);
              col.setPreferredWidth(75);
            }
          }
        }
      });
    initTable();
    journalTable.setAutoResizeMode(JTable.AUTO_RESIZE_OFF);
    
    for (int i=0; i<journalNames.length; i++)
    {
      TableColumn col = journalTable.getColumnModel().getColumn(i);
      int align = JLabel.CENTER;
      if (journalNames[i].equals("BSP") ||
          journalNames[i].equals("TWS") ||
          journalNames[i].equals("AWS") ||
          journalNames[i].equals("SOG") ||
          journalNames[i].equals("CSP") ||
          journalNames[i].equals("LOG"))
        align = JLabel.RIGHT;
      else if (journalNames[i].equals(COMMENT))
        align = JLabel.LEADING;
      col.setCellRenderer(new AlignedCellRenderer(align));      
    }    
    refreshJournal();

    NMEAContext.getInstance().addNMEAReaderListener(new NMEAReaderListener(Constants.NMEA_SERVER_LISTENER_GROUP_ID, "Journal")
      {
        public void refreshLogJournal() 
        {
          refreshJournal();
        }
      });
  }

  private void initTable()
  {
    // Nb Columns
    try
    {
      Connection conn = NMEAContext.getInstance().getDBConnection();
      Statement stmt = conn.createStatement();
      ResultSet rs = stmt.executeQuery(GET_DATA_TYPE_STMT);
      List<String> result = new ArrayList<String>();
      while (rs.next())
      {
        String s1 = rs.getString(1);
        result.add(s1);
      }
      rs.close();
      int i = 2;
      journalNames = new String[result.size() + i];
      journalNames[0] = DATE_TIME;
      journalNames[1] = COMMENT;
      for (String s : result)
      {
        journalNames[i++] = s;
      }
    }
    catch (Exception ex)
    {
      ex.printStackTrace();
    }
    
    // Data
    journalDataModel = new AbstractTableModel()
      {
        public int getColumnCount()
        {
          return journalNames.length;
        }

        public int getRowCount()
        {
          return journalData.length;
        }

        public Object getValueAt(int row, int col)
        {
          return journalData[row][col];
        }

        public String getColumnName(int column)
        {
          return journalNames[column];
        }

        public Class getColumnClass(int c)
        {
          Class cls = Object.class;
          try { cls = getValueAt(1, c).getClass(); } catch (Exception ok) {}
          return cls;
        }

        public boolean isCellEditable(int row, int col)
        {
          return false;
        }

        public void setValueAt(Object aValue, int row, int column)
        {
          journalData[row][column] = aValue;
        }
      };
    journalTable = new JTable(journalDataModel);
    centerScrollPane = new JScrollPane(journalTable);
    centerPanel.add(centerScrollPane, BorderLayout.CENTER);
  }

  private void readJournalData()
  {
    try
    {
      Connection conn = NMEAContext.getInstance().getDBConnection();
      Statement stmt = conn.createStatement();
      ResultSet rs = stmt.executeQuery(GET_JOURNAL_HEADER);
      List<ArrayList<String>> result = new ArrayList<ArrayList<String>>();
      while (rs.next())
      {
        ArrayList<String> line = new ArrayList<String>();
        Timestamp ts   = rs.getTimestamp(1);
        String comment = rs.getString(2);
        line.add(DATE_FMT.format(new java.util.Date(ts.getTime())));
        line.add(comment);
        PreparedStatement pStmt = conn.prepareStatement(GET_JOURNAL_DETAIL);
        pStmt.setTimestamp(1, ts);
        for (int i=2; i<journalNames.length; i++)
        {
          pStmt.setString(2, journalNames[i]);
          ResultSet details = pStmt.executeQuery();
          String data = "";
          while (details.next())
          {
            double d = 0;
            try { d = details.getDouble(1); }
            catch (Exception ex)
            {  System.err.println("Managed:" + ex.getLocalizedMessage()); }
            if (journalNames[i].equals("BSP") ||
                journalNames[i].equals("SOG") ||
                journalNames[i].equals("TWS") ||
                journalNames[i].equals("AWS") ||
                journalNames[i].equals("CSP"))
            {              
              data = SPEED_DIST_FMT.format(d) + " kts";
              if (journalNames[i].equals("TWS"))
              {
                int beaufort = WindUtils.getBeaufort(d);
                data += (" - F " + Integer.toString(beaufort));
              }
              if (journalNames[i].equals("BSP") || journalNames[i].equals("AWS") || journalNames[i].equals("TWS") || journalNames[i].equals("CSP"))
              {
                if (d == -Double.MAX_VALUE || Double.isInfinite(d) || Double.isInfinite(d))
                  data = " - ";
              }
            }
            else if (journalNames[i].equals("LOG"))
            {              
              data = SPEED_DIST_FMT.format(d) + " nm";
            }
            else if (journalNames[i].equals("HDG") ||
                     journalNames[i].equals("COG") ||
                     journalNames[i].equals("AWA") ||
                     journalNames[i].equals("TWA") ||
                     journalNames[i].equals("TWD") ||
                     journalNames[i].equals("CDR"))   
            {
              data = DIRECTION_FMT.format(d);
              if (journalNames[i].equals("TWD"))
                data += (" - " + WindUtils.getRoseDir(d));
              if (journalNames[i].equals("TWA") || journalNames[i].equals("CDR"))
              {
                if (d == -Double.MAX_VALUE || Double.isInfinite(d) || Double.isInfinite(d))
                  data = " - ";
              }
            }
            else if (journalNames[i].equals("LAT"))
              data = GeomUtil.decToSex(d, GeomUtil.SWING, GeomUtil.NS, GeomUtil.LEADING_SIGN);
            else if (journalNames[i].equals("LNG"))
              data = GeomUtil.decToSex(d, GeomUtil.SWING, GeomUtil.EW, GeomUtil.LEADING_SIGN);
            else if (journalNames[i].equals("DBT")) 
              data = DEPTH_FMT.format(d);
            else if (journalNames[i].equals("MWT"))
              data = TEMP_FMT.format(d);
            else
              data = Double.toString(d);
          }
          line.add(data);
          details.close();
        }
        pStmt.close();
        result.add(line);        
      }
      rs.close();
      stmt.close();
      journalData = new Object[result.size()][journalNames.length];
      int i = 0;
      for (ArrayList<String> as: result)
      {
        int j = 0;
        for (String s : as)
        {
          journalData[i][j] = s;
          j++;
        }
        i++;
      }
    }
    catch (Exception ex)
    {
      ex.printStackTrace();
    }
  }

  private void resetJournal()
  {
    try
    {
      Connection conn = NMEAContext.getInstance().getDBConnection();
      Statement stmt = conn.createStatement();
      int nb = stmt.executeUpdate(DELETE_JOURNAL);
      conn.commit();
      System.out.println("Deleted " + nb + " record(s)");
    }
    catch (Exception ex)
    {
      ex.printStackTrace();
    }
    refreshJournal();
  }

  private void refreshJournal()
  {
    readJournalData();
    ((AbstractTableModel) journalDataModel).fireTableDataChanged();
  }
  
  private void publishJournal()
  {
    System.out.println("Publishing the journal.");
    try
    {
      Connection conn = NMEAContext.getInstance().getDBConnection();
      Statement stmt = conn.createStatement();
      ResultSet rs = stmt.executeQuery(GET_JOURNAL_HEADER_2);
      String dateStr = "";
      StringBuffer sb = new StringBuffer();
      sb.append("<journal>\n");
      while (rs.next())
      {
        Timestamp ts = rs.getTimestamp(1);
        String comment = rs.getString(2);
        Date logDate = new Date(ts.getTime());
        String refStrDate = DATE_ONLY_FMT.format(logDate);
//      System.out.println("That makes [" + refStrDate + "]");
        if (!refStrDate.equals(dateStr))
        {
          // New day
          if (dateStr.trim().length() > 0)
            sb.append("  </day>\n");
          sb.append("  <day date='" + refStrDate + "'>\n");
          dateStr = refStrDate;
        }
        sb.append("    <time value='" + TO_XSD_DURATION.format(logDate) + "'>\n");
        sb.append("      <comment><![CDATA[" + comment + "]]></comment>\n");
        PreparedStatement pStmt = conn.prepareStatement(GET_JOURNAL_DETAIL_2);
        pStmt.setTimestamp(1, ts);
        ResultSet details = pStmt.executeQuery();
        while (details.next())
        {
          String dataId = details.getString(1);
          double data   = 0;
          try { data = details.getDouble(2); } catch (SQLException sqle) { System.err.println("Managed (2):" + sqle.getLocalizedMessage()); }
          if (data == -Double.MAX_VALUE)
            data = 0d;
          sb.append("      <data id='" + dataId + "'>" + Double.toString(data) + "</data>\n");
        }
        details.close();
        pStmt.close();
        sb.append("    </time>\n");
      }
      rs.close();
      stmt.close();
      if (dateStr.trim().length() > 0)
        sb.append("  </day>\n");
      sb.append("</journal>\n");
      // Generate XML data
      File pubDir = new File("pub");
      if (!pubDir.exists())
        pubDir.mkdirs();
//    System.out.println("Result:\n" + sb.toString());
      BufferedWriter bw = new BufferedWriter(new FileWriter("pub" + File.separator + "journal.xml"));
      bw.write(sb.toString());
      bw.close();
      
      // Generate pdf
      try
      {
        String cmd = "cmd /k start /min pub" + File.separator + "publish-journal.bat \"journal.xml\" \"journal.pdf\"";
        if (System.getProperty("os.name").indexOf("Linux") > -1)
          cmd = "." + File.separator + "pub" + File.separator + "publish-journal journal.xml journal.pdf";
        System.out.println("Command:" + cmd);
        Runtime.getRuntime().exec(cmd);
      }
      catch (Exception e)
      {
        e.printStackTrace();
      }
    }
    catch (Exception ex)
    {
      ex.printStackTrace();
    }
  }
  
  private class AlignedCellRenderer extends JLabel implements TableCellRenderer
  {
    int alignment = JLabel.CENTER;
    
    public AlignedCellRenderer(int align)
    {
      super();  
      this.alignment = align;
    }
    
    public Component getTableCellRendererComponent(JTable table, 
                                                   Object value, 
                                                   boolean isSelected, 
                                                   boolean hasFocus, 
                                                   int row, 
                                                   int column)
    {
      this.setText(value.toString());
      this.setHorizontalAlignment(alignment);
      return this;
    }
  }  
}
