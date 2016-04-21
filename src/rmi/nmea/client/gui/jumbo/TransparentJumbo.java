package rmi.nmea.client.gui.jumbo;

import coreutilities.gui.TransparentJWindow;

import java.awt.Color;
import java.awt.Dimension;
import java.awt.Font;
import java.awt.Graphics;

import java.awt.Point;

import java.text.DecimalFormat;

import javax.swing.JLabel;
import javax.swing.JOptionPane;

public class TransparentJumbo
{
  private String name = "BSP";
  private JLabel valueLabel = null;
  private double value = 0d;
  private DecimalFormat df22 = new DecimalFormat("#0.00");
  private String unit = " kts";
  
  private final static int JUMBO_WIDTH  = 200;
  private final static int JUMBO_HEIGHT = 100;
  
  private final TransparentJWindow transpWin = new TransparentJWindow()
    {
      protected void transparentWindowPaintComponent(Graphics g)
      {
        Point p = this.getCurrentPosition();
//      System.out.println("Painting Jumbo:" + p.x + "," + p.y);
        g.setColor(new Color(5, 5, 5, 50));
//      g.setColor(Color.lightGray);        
        this.setSize(new Dimension(JUMBO_WIDTH, JUMBO_HEIGHT));
        g.fillRoundRect(0, 0, this.getSize().width, this.getSize().height, 25, 25);
//      g.fillRect(0, 0, this.getSize().width, this.getSize().height);
        g.setColor(Color.blue);
        g.setFont(g.getFont().deriveFont(30, Font.BOLD));
        g.drawString("BSP", 5, 30);
      }
      
      protected void onClick()
      {
        manageClick();
      }
    };

  public TransparentJumbo()
  {
    this(Color.red, 10, 10);
  }
  
  public TransparentJumbo(Color fontColor, int x, int y)
  {
    try
    {
      valueLabel = new JLabel("Value");
      valueLabel.setFont(new Font("Courier new", Font.BOLD, 24));
      valueLabel.setForeground(fontColor);
      transpWin.getContentPane().add(valueLabel);

      transpWin.setBounds(x, // transpWin.getCurrentPosition().x, 
                          y, // transpWin.getCurrentPosition().y, 
                          JUMBO_WIDTH,   // transpWin.getWinDim().width, 
                          JUMBO_HEIGHT); // transpWin.getWinDim().height);
      transpWin.setVisible(true);      
    }
    catch (Exception ex)
    {
      ex.printStackTrace();
    }
  }
  
  private void manageClick()
  {
    int resp = JOptionPane.showConfirmDialog(transpWin, "Exit?", "Exit", JOptionPane.YES_NO_OPTION, JOptionPane.QUESTION_MESSAGE);
    if (resp == JOptionPane.YES_OPTION)
      System.exit(0);
    else
      System.out.println("Still alive!");
  }
  
  public void setValue(String str)
  {
    valueLabel.setText(str);
  }

  public void setValue(double value)
  {
    this.value = value;
    valueLabel.setText(name + ":" + df22.format(value) + unit);
    transpWin.repaint();
  }

  public double getValue()
  {
    return value;
  }

  public void setFormat(DecimalFormat df22)
  {
    this.df22 = df22;
  }

  public DecimalFormat getFormat()
  {
    return df22;
  }

  public void setUnit(String unit)
  {
    this.unit = unit;
  }

  public String getUnit()
  {
    return unit;
  }

  public void setName(String name)
  {
    this.name = name;
  }

  public String getName()
  {
    return name;
  }
}
