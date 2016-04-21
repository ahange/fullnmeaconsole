package nmea.ui.widgets;

import java.awt.BorderLayout;
import java.awt.Component;
import java.awt.Dimension;
import java.awt.KeyboardFocusManager;

import javax.swing.JComponent;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTable;
import javax.swing.table.AbstractTableModel;
import javax.swing.table.TableCellRenderer;
import javax.swing.table.TableColumn;
import javax.swing.table.TableModel;

import nmea.server.constants.Constants;

import utils.JTableFocusChangeListener;

import utils.log.LoggedDataSelectedInterface;

public final class LoggedDataTable
  extends JPanel
{
  private LoggedDataTable instance = this;
  private transient LoggedDataSelectedInterface parent;
  
  private BorderLayout borderLayout1 = new BorderLayout();
  private JPanel centerPane = new JPanel();

  private static final String NAME = "Data";
  private static final String SHOW = "Show";

  private static final String[] names =
  { NAME, SHOW };

  private transient TableModel dataModel;

  protected transient Object[][] data = new Object[0][0];

  private JTable table;
  private JScrollPane scrollPane;
  private BorderLayout borderLayout2 = new BorderLayout();

  public LoggedDataTable(LoggedDataSelectedInterface caller)
  {
    this.parent = caller;
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
    this.setSize(new Dimension(302, 250));
    centerPane.setLayout(borderLayout2);
    // Top Panel empty for now
    this.add(centerPane, BorderLayout.CENTER);
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
            return data == null? 0: data.length;
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
    //      System.out.println("Class requested column " + c + ", type:" + getValueAt(0, c).getClass());
            return getValueAt(0, c).getClass();
          }

          public boolean isCellEditable(int row, int col)
          {
            return col == 1;
          }

          public void setValueAt(Object aValue, final int row, final int column)
          {
            data[row][column] = aValue;
            if (column == 1) // Show/Hide
            {
              Thread thread = new Thread()
              {
                public void run()
                {
                  parent.setSelectedData((String)data[row][0], (Boolean)data[row][1]);
                }
              };
              thread.start();
            }
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
                if (vColIndex == 1)
                  jc.setToolTipText(getValueAt(rowIndex, vColIndex).toString());
                else
                  jc.setToolTipText(Constants.getInstance().getNMEAMap().get(getValueAt(rowIndex, vColIndex).toString()));
              }
              catch (Exception ex)
              {
                System.err.println("LoggedDataTable:" + ex.getMessage());
              }
            }
            return c;
          }
        };
    TableColumn firstColumn = table.getColumn(NAME);
//  firstColumn.setMaxWidth(150);
    firstColumn.setPreferredWidth(150);
    firstColumn.setMinWidth(120);
    
    scrollPane = new JScrollPane(table);
    centerPane.add(scrollPane, BorderLayout.CENTER);

    KeyboardFocusManager.getCurrentKeyboardFocusManager().addPropertyChangeListener(new JTableFocusChangeListener(table));
  }

  public Object[][] addLineInTable(String name)
  {
    return addLineInTable(name, false);
  }

  public Object[][] addLineInTable(String name, boolean b)
  {
    int len = 0;
    if (data != null)
      len = data.length;
    Object[][] newData = new Object[len + 1][names.length];
    for (int i = 0; i < len; i++)
    {
      for (int j = 0; j < names.length; j++)
        newData[i][j] = data[i][j];
    }
    newData[len][0] = name;
    newData[len][1] = b;
    data = newData;
    ((AbstractTableModel) dataModel).fireTableDataChanged();
    return newData;
  }

  public void clear()
  {
    setData(new Object[0][0]);
  }
  
  protected void refreshTable()
  {
    ((AbstractTableModel) dataModel).fireTableDataChanged();    
  }
  
  public void setData(Object[][] newData)
  {
    data = newData;
    ((AbstractTableModel) dataModel).fireTableDataChanged();
  }
  
  public void setSelectedRow(String id, boolean checked)
  {
    for (int i=0; i<data.length; i++)
    {
      if (id.equals((String)data[i][0]))
      {
        data[i][1] = checked;
        ((AbstractTableModel) dataModel).fireTableDataChanged();
        break;
      }
    }
  }
}
