package nmea.ui.widgets;

import utils.JTableFocusChangeListener;

import nmea.local.LogisailResourceBundle;

import java.awt.BorderLayout;

import java.awt.Component;
import java.awt.Dimension;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;

import java.awt.Insets;
import java.awt.KeyboardFocusManager;

import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

import javax.swing.JButton;
import javax.swing.JComponent;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTable;
import javax.swing.event.ListSelectionEvent;
import javax.swing.event.ListSelectionListener;
import javax.swing.table.AbstractTableModel;

import javax.swing.table.TableCellRenderer;
import javax.swing.table.TableModel;

public final class ConfigTablePanel 
           extends JPanel 
{
  BorderLayout borderLayout1 = new BorderLayout();
  JPanel topPanel = new JPanel();
  JPanel bottomPanel = new JPanel();
  JPanel centerPane = new JPanel();
  JButton deleteButton = new JButton(LogisailResourceBundle.buildMessage("delete"));

  final static String KEY = LogisailResourceBundle.buildMessage("name");
  final static String VALUE = LogisailResourceBundle.buildMessage("value");

  final static String[] names = {KEY, VALUE};
  
  TableModel dataModel;

  private Object[][] data = new Object[0][0];
  
  JTable table;
  JScrollPane scrollPane;
  BorderLayout borderLayout2 = new BorderLayout();
  GridBagLayout gridBagLayout1 = new GridBagLayout();
  JLabel titleLabel = new JLabel();
    
  public ConfigTablePanel()
  {
    try
    {
      jbInit();
    }
    catch(Exception e)
    {
      e.printStackTrace();
    }
  }

  public void setData(Object oaa)
  {
    data = (Object[][])oaa;
    ((AbstractTableModel)dataModel).fireTableDataChanged();
    table.repaint();
  }

  public Object[][] getData()
  {
    return data;
  }
  
  private void jbInit() throws Exception
  {
    this.setLayout(borderLayout1);
    Dimension dim = new Dimension(400, 150);
    this.setSize(dim);
    this.setPreferredSize(dim);
    bottomPanel.setLayout(gridBagLayout1);
    centerPane.setLayout(borderLayout2);
    deleteButton.setEnabled(false);
    deleteButton.addActionListener(new ActionListener()
      {
        public void actionPerformed(ActionEvent e)
        {
          deleteButton_actionPerformed(e);
        }
      });
    titleLabel.setText(LogisailResourceBundle.buildMessage("user-configs"));
    topPanel.add(titleLabel, null);
    this.add(topPanel, BorderLayout.NORTH);
    bottomPanel.add(deleteButton, new GridBagConstraints(2, 1, 1, 1, 0.0, 0.0, GridBagConstraints.CENTER, GridBagConstraints.NONE, new Insets(5, 5, 5, 5), 0, 0));
    this.add(bottomPanel, BorderLayout.SOUTH);
    this.add(centerPane, BorderLayout.CENTER);

    initTable();
    SelectionListener listener = new SelectionListener(table);
    table.getSelectionModel().addListSelectionListener(listener);
    table.getColumnModel().getSelectionModel().addListSelectionListener(listener);
  }

  private void initTable()
  {
    dataModel = new AbstractTableModel()
    {
      public int getColumnCount()
      { return names.length; }
      public int getRowCount()
      { return data.length; }
      public Object getValueAt(int row, int col)
      { return data[row][col]; }
      public String getColumnName(int column)
      { return names[column]; }
      public Class getColumnClass(int c)
      { return getValueAt(0, c).getClass(); }
      public boolean isCellEditable(int row, int col)
      { return true; } 
      public void setValueAt(Object aValue, int row, int column)
      { data[row][column] = aValue; }
    };
    table = new JTable(dataModel)
    {
      /* For the tooltip text */
      public Component prepareRenderer(TableCellRenderer renderer,
                                       int rowIndex, 
                                       int vColIndex) 
      {
        Component c = super.prepareRenderer(renderer, rowIndex, vColIndex);
        if (c instanceof JComponent) 
        {
          JComponent jc = (JComponent)c;
          try 
          { 
            Object o = getValueAt(rowIndex, vColIndex);
//          System.out.println("Object is " + (o==null?"null":("a " + o.getClass().getName())));
            if (o != null)
              jc.setToolTipText(o.toString()); 
          }
          catch (Exception ex)
          {
            System.err.println("From ConfigPanel:" + ex.getMessage());
            ex.printStackTrace();
          }
        }
        return c;
      }
    };
    scrollPane = new JScrollPane(table);  
    centerPane.add(scrollPane, BorderLayout.CENTER);
    KeyboardFocusManager.getCurrentKeyboardFocusManager().addPropertyChangeListener(new JTableFocusChangeListener(table));
  }

  /*
  private Object[][] addLineInTable(String k,
                                    String v)
  {
    return addLineInTable(k, v, data);
  }
  private Object[][] addLineInTable(String k,
                                    String v,
                                    Object[][] d)
  {
    int len = 0;
    if (d != null)
      len = d.length;
    Object[][] newData = new Object[len + 1][names.length];
    for (int i=0; i<len; i++)
    {
      for (int j=0; j<names.length; j++)
        newData[i][j] = d[i][j];
    }
    newData[len][0] = k;
    newData[len][1] = v;
//  System.out.println("Adding " + k + ":" + v);
    return newData;
  }
  */
  private void removeCurrentLine()
  {
    int selectedRow = table.getSelectedRow();
  //  System.out.println("Row " + selectedRow + " is selected");
    if (selectedRow < 0)
      JOptionPane.showMessageDialog(null,
                                    "Please choose a row to remove",
                                    "Removing an entry",
                                    JOptionPane.WARNING_MESSAGE);
    else
    {
      int l = data.length;
      Object[][] newData = new Object[l - 1][names.length];
      int oldInd, newInd;
      newInd = 0;
      for (oldInd=0; oldInd<l; oldInd++)
      {
        if (oldInd != selectedRow)
        {
          for (int j=0; j<names.length; j++)
            newData[newInd][j] = data[oldInd][j];
          newInd++;
        }
      }
      data = newData;
      ((AbstractTableModel)dataModel).fireTableDataChanged();
      table.repaint();
    }
  }

  private void deleteButton_actionPerformed(ActionEvent e)
  {
    removeCurrentLine();
  }
  public class SelectionListener
    implements ListSelectionListener
  {
    JTable table;

    SelectionListener(JTable table)
    {
      this.table = table;
    }

    public void valueChanged(ListSelectionEvent e)
    {
      int selectedRow = table.getSelectedRow();
      if (selectedRow >= 0)
      {
        // Activate delete/remove
        deleteButton.setEnabled(true);
      }
      else
      {
        // De-activate
        deleteButton.setEnabled(false);
      }
    }
  }
}
