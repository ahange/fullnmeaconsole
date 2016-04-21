package nmea.ui.viewer.elements;

import java.awt.Dimension;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;

import java.awt.Insets;

import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

import java.awt.event.MouseWheelEvent;

import java.awt.event.MouseWheelListener;

import javax.swing.JCheckBox;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JSpinner;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;

import nmea.server.ctx.NMEAContext;

public class BufferSizePanel
  extends JPanel
{
  private GridBagLayout gridBagLayout = new GridBagLayout();
  private JLabel bufferLabel = new JLabel();
  private JSpinner bufferSizeSpinner = new JSpinner();
  
  private JCheckBox showRawDataCheckBox = new JCheckBox();

  public BufferSizePanel()
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
    this.setLayout(gridBagLayout);
    bufferLabel.setText("Buffer size in points:");
    bufferSizeSpinner.setPreferredSize(new Dimension(100, 20));
    bufferSizeSpinner.setMinimumSize(new Dimension(100, 20));
    this.add(bufferLabel, new GridBagConstraints(0, 0, 1, 1, 0.0, 0.0, GridBagConstraints.CENTER, GridBagConstraints.NONE, new Insets(0, 0, 0, 0), 0, 0));
    this.add(bufferSizeSpinner, new GridBagConstraints(1, 0, 1, 1, 0.0, 0.0, GridBagConstraints.CENTER, GridBagConstraints.NONE, new Insets(0, 0, 0, 0), 0, 0));
    bufferSizeSpinner.setValue(new Integer(NMEAContext.DEFAULT_BUFFER_SIZE));
    bufferSizeSpinner.addChangeListener(new ChangeListener()
      {
        public void stateChanged(ChangeEvent evt)
        {
          JSpinner spinner = (JSpinner) evt.getSource();
          // Get the new value
          Object value = spinner.getValue();
//        System.out.println("Value is a " + value.getClass().getName());
          if (value instanceof Integer)
          {
            Integer d = (Integer) value;
            NMEAContext.getInstance().fireDataBufferSizeChanged(d.intValue());
          }
        }
      });
    bufferSizeSpinner.addMouseWheelListener(new MouseWheelListener()
      {
        public void mouseWheelMoved(MouseWheelEvent e)
        {
          int notches = e.getWheelRotation();
          Integer ds = (Integer)bufferSizeSpinner.getValue();
          bufferSizeSpinner.setValue(new Integer(ds.intValue() + (notches * -1)));
        }
      });
    showRawDataCheckBox.setText("Show raw (un-damped) data");
    this.add(showRawDataCheckBox,
             new GridBagConstraints(2, 0, 1, 1, 0.0, 0.0, GridBagConstraints.CENTER, GridBagConstraints.NONE,
          new Insets(0, 10, 0, 0), 0, 0));
    showRawDataCheckBox.setSelected(true);
    showRawDataCheckBox.addActionListener(new ActionListener()
      {
        public void actionPerformed(ActionEvent e)
        {
          NMEAContext.getInstance().fireShowRawData(showRawDataCheckBox.isSelected());
        }
      });
  }
}
