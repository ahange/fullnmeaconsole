package nmea.ui.deviation.deviationcurve;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;

import java.util.ArrayList;
import java.util.Hashtable;
import java.util.List;
import java.util.Set;

import nmea.ui.deviation.deviationcurve.util.SquareMatrix;
import nmea.ui.deviation.deviationcurve.util.SystemUtil;

/**
 * Resolving
 *
 * d = d(R)
 * d = A + BsinR + CcosR + Dsin2R + Ecos2R
 * Using the least squares method
 *
 * Based on Andres Ruis Gonzalez (navigationalalgorithms@gmail.com) docs.
 */
public class DeviationCurve
{
  /**
   * Just a sample.
   * 
   * @param args the CSV file containing the deviation data,
   * like C:\_mywork\dev-corner\olivsoft\all-scripts\config\deviation.donpedro.csv
   */
  public static void main(String[] args) throws Exception
  {
    double[] f = calculateCurve(args[0]);
    for (int i=0; i<5; i++)
      System.out.println("Coeff deg " + Integer.toString(i) + "=" + Double.toString(f[i]));
    
    System.out.println("Calculated:");
    for (int i=0; i<360; i+=5)
      System.out.println("For " + Integer.toString(i) + " d=" + Double.toString(devFunc(f, i)));
    System.out.println("Done");
  }
  
  public static double[] calculateCurve(String fName) throws Exception
  {
    List<double[]> al = new ArrayList<double[]>();
    File devFile = new File(fName);
    if (!devFile.exists())
    {
      System.err.println("Need the dev file name as a parameter. Exiting.");
      System.exit(1);
    }
    BufferedReader br = new BufferedReader(new FileReader(devFile));
    String line = "";
    boolean keepReading = true;
    
    while (keepReading)
    {
      line = br.readLine();
      if (line == null)
        keepReading = false;
      else
      {
        String[] value = line.split(",");
        double r = Double.parseDouble(value[0]);
        double d = Double.parseDouble(value[1]);
        al.add(new double[] { r, d });
      }
    }
    br.close();    
    return calculateCurve(al);
  }
  
  public static double[] calculateCurve(Hashtable<Double, Double> ht) throws Exception
  {
    List<double[]> ald = new ArrayList<double[]>();
    for (Double d : ht.keySet())
    {
      double val = ht.get(d).doubleValue();
      ald.add(new double[] { d.doubleValue(), val });
    }                                              
    return calculateCurve(ald);
  }
  
  /**
   * For Calculated dev curve
   * @param ald
   * @return
   * @throws Exception
   */
  public static double[] calculateCurve(List<double[]> ald) throws Exception
  {
    SquareMatrix matrix = new SquareMatrix(5, true);
    double[] c = new double[] { 0d, 0d, 0d, 0d, 0d };
    
    for (double[] val : ald)
    {
      double r = val[0];
      double d = val[1];
      // Do the sommations here
      matrix.setElementAt(0, 0, matrix.getElementAt(0, 0) + 1); // n
      matrix.setElementAt(0, 1, matrix.getElementAt(0, 1) + Math.sin(Math.toRadians(r))); // sinR
      matrix.setElementAt(0, 2, matrix.getElementAt(0, 2) + Math.cos(Math.toRadians(r))); // cosR
      matrix.setElementAt(0, 3, matrix.getElementAt(0, 3) + Math.sin(2 * Math.toRadians(r))); // sin2R
      matrix.setElementAt(0, 4, matrix.getElementAt(0, 4) + Math.cos(2 * Math.toRadians(r))); // cos2R

      matrix.setElementAt(1, 0, matrix.getElementAt(1, 0) + Math.sin(Math.toRadians(r))); // sinR
      matrix.setElementAt(1, 1, matrix.getElementAt(1, 1) + Math.pow(Math.sin(Math.toRadians(r)), 2d)); // sinR^2
      matrix.setElementAt(1, 2, matrix.getElementAt(1, 2) + (Math.cos(Math.toRadians(r)) * Math.sin(Math.toRadians(r)))); // sinR cosR
      matrix.setElementAt(1, 3, matrix.getElementAt(1, 3) + (Math.sin(2 * Math.toRadians(r)) * Math.sin(Math.toRadians(r)))); // sin2R sinR
      matrix.setElementAt(1, 4, matrix.getElementAt(1, 4) + (Math.cos(2 * Math.toRadians(r)) * Math.sin(Math.toRadians(r)))); // cos2R sinR

      matrix.setElementAt(2, 0, matrix.getElementAt(2, 0) + Math.sin(Math.toRadians(r))); // cosR
      matrix.setElementAt(2, 1, matrix.getElementAt(2, 1) + (Math.cos(Math.toRadians(r)) * Math.sin(Math.toRadians(r)))); // cosR sinR
      matrix.setElementAt(2, 2, matrix.getElementAt(2, 2) + Math.pow(Math.cos(Math.toRadians(r)), 2d)); // cosR^2
      matrix.setElementAt(2, 3, matrix.getElementAt(2, 3) + (Math.sin(2 * Math.toRadians(r)) * Math.cos(Math.toRadians(r)))); // sin2R cosR
      matrix.setElementAt(2, 4, matrix.getElementAt(2, 4) + (Math.cos(2 * Math.toRadians(r)) * Math.cos(Math.toRadians(r)))); // cos2R cosR

      matrix.setElementAt(3, 0, matrix.getElementAt(3, 0) + Math.sin(2 * Math.toRadians(r))); // sin2R
      matrix.setElementAt(3, 1, matrix.getElementAt(3, 1) + (Math.sin(2 * Math.toRadians(r)) * Math.sin(Math.toRadians(r)))); // sin2R sinR
      matrix.setElementAt(3, 2, matrix.getElementAt(3, 2) + (Math.sin(2 * Math.toRadians(r)) * Math.cos(Math.toRadians(r)))); // sin2R cosR
      matrix.setElementAt(3, 3, matrix.getElementAt(3, 3) + Math.pow(Math.sin(2d * Math.toRadians(r)), 2d)); // sin2R^2
      matrix.setElementAt(3, 4, matrix.getElementAt(3, 4) + (Math.cos(2 * Math.toRadians(r)) * Math.sin(2 * Math.toRadians(r)))); // cos2R sin2R      

      matrix.setElementAt(4, 0, matrix.getElementAt(4, 0) + Math.cos(2 * Math.toRadians(r))); // cos2R
      matrix.setElementAt(4, 1, matrix.getElementAt(4, 1) + (Math.cos(2 * Math.toRadians(r)) * Math.sin(Math.toRadians(r)))); // cos2R sinR
      matrix.setElementAt(4, 2, matrix.getElementAt(4, 2) + (Math.cos(2 * Math.toRadians(r)) * Math.cos(Math.toRadians(r)))); // cos2R cosR
      matrix.setElementAt(4, 3, matrix.getElementAt(4, 3) + (Math.cos(2 * Math.toRadians(r)) * Math.sin(2 * Math.toRadians(r)))); // cos2R sin2R      
      matrix.setElementAt(4, 4, matrix.getElementAt(4, 4) + Math.pow(Math.cos(2d * Math.toRadians(r)), 2d)); // cos2R^2
      
      c[0] += d;
      c[1] += (d * Math.sin(Math.toRadians(r)));
      c[2] += (d * Math.cos(Math.toRadians(r)));
      c[3] += (d * Math.sin(2 * Math.toRadians(r)));
      c[4] += (d * Math.cos(2 * Math.toRadians(r)));
    }
    double[] f = SystemUtil.solveSystem(matrix, c);
    return f;
  }
  
  public static double devFunc(double[] f, double r)
  {
    double d = 0d;
    d = f[0] + 
       (f[1] * Math.sin(Math.toRadians(r))) + 
       (f[2] * Math.cos(Math.toRadians(r))) + 
       (f[3] * Math.sin(2 * Math.toRadians(r))) + 
       (f[4] * Math.cos(2 * Math.toRadians(r)));    
    return d;
  }

  public static double devFuncComponent(int component, double[] f, double r)
  {
    double d = 0d;
    switch (component)
    {
      case 0:
        d = f[0];
        break;
      case 1:
        d = (f[1] * Math.sin(Math.toRadians(r)));
        break;
      case 2:
        d = (f[2] * Math.cos(Math.toRadians(r)));
        break;
      case 3:
        d = (f[3] * Math.sin(2 * Math.toRadians(r)));
        break;
      case 4:
        d = (f[4] * Math.cos(2 * Math.toRadians(r)));
        break;
      default:
        break;        
    }
    return d;
  }
}
