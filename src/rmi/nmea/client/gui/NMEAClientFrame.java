package rmi.nmea.client.gui;

import java.awt.Button;
import java.awt.Dimension;

import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Insets;
import java.awt.Rectangle;

import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

import java.text.DecimalFormat;

import javax.swing.JButton;
import javax.swing.JFormattedTextField;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JTextField;

/**
 * An example.
 */
public class NMEAClientFrame
  extends JFrame
{
  private transient MainClientApplication parent;
  private JButton pingButton = new JButton();
  private GridBagLayout gridBagLayout1 = new GridBagLayout();
  private JLabel serverLabel = new JLabel();
  private JTextField serverTextField = new JTextField();
  private JLabel portLabel = new JLabel();
  private JFormattedTextField portFormattedTextField = new JFormattedTextField(new DecimalFormat("#0000"));
  private JButton connectButton = new JButton();

  public NMEAClientFrame(MainClientApplication parent)
  {
    this.parent = parent;
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
    this.getContentPane().setLayout(gridBagLayout1);
    this.setSize(new Dimension(400, 143));
    this.setTitle( "NMEA RMI Client" );
    pingButton.setText("Ping Server");
    pingButton.setEnabled(false);
    pingButton.addActionListener(new ActionListener()
      {
        public void actionPerformed(ActionEvent e)
        {
          jButton1_actionPerformed(e);
        }
      });
    serverLabel.setText("Server:");
    serverTextField.setPreferredSize(new Dimension(150, 20));
    serverTextField.setText("localhost");
    portLabel.setText("Port:");
    portFormattedTextField.setText("1099");
    portFormattedTextField.setPreferredSize(new Dimension(50, 20));
    portFormattedTextField.setHorizontalAlignment(JTextField.CENTER);
    connectButton.setText("Connect");
    connectButton.addActionListener(new ActionListener()
      {
        public void actionPerformed(ActionEvent e)
        {
          connectButton_actionPerformed(e);
        }
      });
    this.getContentPane().add(pingButton, new GridBagConstraints(0, 2, 4, 1, 0.0, 0.0, GridBagConstraints.CENTER, GridBagConstraints.NONE, new Insets(3, 0, 0, 0), 13, -2));
    this.getContentPane().add(serverLabel, new GridBagConstraints(0, 0, 1, 1, 0.0, 0.0, GridBagConstraints.EAST, GridBagConstraints.NONE, new Insets(0, 0, 0, 0), 0, 0));
    this.getContentPane().add(serverTextField, new GridBagConstraints(1, 0, 1, 1, 0.0, 0.0, GridBagConstraints.CENTER, GridBagConstraints.NONE, new Insets(0, 0, 0, 0), 0, 0));
    this.getContentPane().add(portLabel, new GridBagConstraints(2, 0, 1, 1, 0.0, 0.0, GridBagConstraints.CENTER, GridBagConstraints.NONE, new Insets(0, 5, 0, 0), 0, 0));
    this.getContentPane().add(portFormattedTextField, new GridBagConstraints(3, 0, 1, 1, 0.0, 0.0, GridBagConstraints.CENTER, GridBagConstraints.NONE, new Insets(0, 0, 0, 0), 0, 0));
    this.getContentPane().add(connectButton, new GridBagConstraints(0, 1, 4, 1, 0.0, 0.0, GridBagConstraints.CENTER, GridBagConstraints.NONE, new Insets(3, 0, 0, 0), 0, 0));
  }

  private void jButton1_actionPerformed(ActionEvent e)
  {
    parent.pingServer();
  }

  private boolean connected = false;
  private void connectButton_actionPerformed(ActionEvent e)
  {
    try
    {
      if (!connected)
      {
        parent.connect(serverTextField.getText(), Integer.parseInt(portFormattedTextField.getText()));
        connected = true;
      }
      else
      {
        parent.disconnect();
        connected = false;
      }
      pingButton.setEnabled(connected);
      connectButton.setText(connected?"Disconnect":"Connect");
      serverTextField.setEnabled(!connected);
      portFormattedTextField.setEnabled(!connected);
      serverLabel.setEnabled(!connected);
      portLabel.setEnabled(!connected);
      
    }
    catch (Exception ex)
    {
      ex.printStackTrace();
    }
  }
}
