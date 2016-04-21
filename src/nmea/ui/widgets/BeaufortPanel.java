package nmea.ui.widgets;

import java.awt.BorderLayout;
import java.awt.Component;
import java.awt.Dimension;

import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTable;
import javax.swing.table.AbstractTableModel;
import javax.swing.table.TableCellRenderer;
import javax.swing.table.TableColumn;
import javax.swing.table.TableModel;

import nmea.local.LogisailResourceBundle;


public class BeaufortPanel
  extends JPanel
{
  private static final String FORCE = "Force";
  private static final String KNTS  = "kts";
  private static final String KMH   = "km/h";
  private static final String MS    = "m/s";
  private static final String MPH   = "mph";
  private static final String DESC  = "Description";

  private String names[] = new String[]
    { FORCE, KNTS, KMH, MPH, MS, DESC };
  private Object beaufortData[][] = new Object[][]
    {
      {  "0", "0",       "0",         "0",       "0 - 0.3",     LogisailResourceBundle.buildMessage("force.0") },
      {  "1", "1 - 4",   "1 - 7",     "1 - 4",   "0.3 - 1.6",   LogisailResourceBundle.buildMessage("force.1") },
      {  "2", "4 - 7",   "7 - 12",    "4 - 8",   "1.6 - 3.4",   LogisailResourceBundle.buildMessage("force.2") },
      {  "3", "7 - 11",  "12 - 20",   "8 - 13",  "3.4 - 5.5",   LogisailResourceBundle.buildMessage("force.3") },
      {  "4", "11 - 16", "20 - 30",   "13 - 19", "5.5 - 8.0",   LogisailResourceBundle.buildMessage("force.4") },
      {  "5", "16 - 22", "30 - 40",   "19 - 25", "8.0 - 10.8",  LogisailResourceBundle.buildMessage("force.5") },
      {  "6", "22 - 28", "40 - 51",   "25 - 32", "10.8 - 13.9", LogisailResourceBundle.buildMessage("force.6") },
      {  "7", "28 - 34", "51 - 63",   "32 - 39", "13.9 - 17.2", LogisailResourceBundle.buildMessage("force.7") },
      {  "8", "34 - 41", "63 - 76",   "39 - 47", "17.2 - 20.8", LogisailResourceBundle.buildMessage("force.8") },
      {  "9", "41 - 48", "76 - 88",   "47 - 55", "20.8 - 24.5", LogisailResourceBundle.buildMessage("force.9") },
      { "10", "48 - 56", "88 - 103",  "55 - 64", "24.5 - 28.5", LogisailResourceBundle.buildMessage("force.10") },
      { "11", "56 - 64", "103 - 118", "64 - 74", "28.5 - 32.7", LogisailResourceBundle.buildMessage("force.11") },
      { "12", "64 -",    "118 -",     "74 -",    "32.7 -",      LogisailResourceBundle.buildMessage("force.12") }
    };
  private TableModel beaufortDataModel;
  private JTable beaufortTable;

  private JScrollPane centerScrollPane = null;
  private JPanel centerPanel = new JPanel(new BorderLayout());

  public BeaufortPanel()
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
    this.setSize(new Dimension(415, 200));
    this.setPreferredSize(new Dimension(415, 200));
    this.add(centerPanel, BorderLayout.CENTER);
    initTable();
//  beaufortTable.setAutoResizeMode(JTable.AUTO_RESIZE_OFF);

    for (int i = 0; i < names.length; i++)
    {
      TableColumn col = beaufortTable.getColumnModel().getColumn(i);
      int align = JLabel.CENTER;
      if (names[i].equals(DESC))
        align = JLabel.LEADING;
      col.setCellRenderer(new AlignedCellRenderer(align));
    }
//  refreshJournal();
  }

  private void initTable()
  {
    // Data
    beaufortDataModel = new AbstractTableModel()
      {
        public int getColumnCount()
        {
          return names.length;
        }

        public int getRowCount()
        {
          return beaufortData.length;
        }

        public Object getValueAt(int row, int col)
        {
          return beaufortData[row][col];
        }

        public String getColumnName(int column)
        {
          return names[column];
        }

        public Class getColumnClass(int c)
        {
          Class cls = Object.class;
          try
          {
            cls = getValueAt(1, c).getClass();
          }
          catch (Exception ok)
          {
            cls = Object.class;
          }
          return cls;
        }

        public boolean isCellEditable(int row, int col)
        {
          return false;
        }

        public void setValueAt(Object aValue, int row, int column)
        {
          beaufortData[row][column] = aValue;
        }
      };
    beaufortTable = new JTable(beaufortDataModel);
    centerScrollPane = new JScrollPane(beaufortTable);
    centerPanel.add(centerScrollPane, BorderLayout.CENTER);
  }

  private class AlignedCellRenderer
    extends JLabel
    implements TableCellRenderer
  {
    int alignment = JLabel.CENTER;

    public AlignedCellRenderer(int align)
    {
      super();
      this.alignment = align;
    }

    public Component getTableCellRendererComponent(JTable table, Object value, boolean isSelected, boolean hasFocus, int row, int column)
    {
      this.setText(value.toString());
      this.setHorizontalAlignment(alignment);
      return this;
    }
  }
}
