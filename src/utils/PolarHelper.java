package utils;

import java.io.File;

import java.io.FileNotFoundException;

import java.util.List;

import nmea.server.ctx.NMEAContext;

import oracle.xml.parser.v2.DOMParser;
import oracle.xml.parser.v2.XMLDocument;
import oracle.xml.parser.v2.XMLParseException;
import oracle.xml.parser.v2.XMLParser;

import polarmaker.polars.smooth.gui.components.PolarUtilities;
import polarmaker.polars.smooth.gui.components.polars.CoeffForPolars;

public class PolarHelper
{
  private static List<CoeffForPolars> coeffList = null;
  private static String fileName = ""; // "D:\\OlivSoft\\all-scripts\\polars\\CheoyLee42.polar-coeff"
  private static double polarCoeff = 1d;
  
  public static boolean arePolarsAvailable()
  {
    return coeffList != null;  
  }
  
  public static void refreshCoeffs()
  {
    // Polar V2.
    String fName = fileName;
    coeffList = null;
    DOMParser parser = NMEAContext.getInstance().getParser();
    try
    {
      synchronized (parser)
      {
        parser.setValidationMode(XMLParser.NONVALIDATING);
        parser.parse(new File(fName).toURI().toURL());
        XMLDocument doc = parser.getDocument();
        
        coeffList = PolarUtilities.buildCoeffList(doc);
      }
    }
    catch (FileNotFoundException fnfe)
    {
      // That's OK.
    }
    catch (XMLParseException xpe)
    {
      // All right too
    }
    catch (Exception ex)
    {
      ex.printStackTrace();
    }
  }
  
  public static double getSpeed(double tws, double twa)
  {
    return getSpeed(tws, twa, 1D);
  }
  
  public static double getSpeed(double tws, double twa, double speedCoeff)
  {
    if (coeffList == null)
      refreshCoeffs();
      
    double speed = 0.0D;
    
    if (arePolarsAvailable())
    {
      double angle = twa;
      if (angle > 180D)
        angle = 360D - angle;
      speed = PolarUtilities.getBSP(coeffList, tws, angle);
      speed *= speedCoeff;
  //  System.out.println("Speed for TWS " + tws + " and TWA " + twa + " -> " + speed);
    }
    return speed;
  }
  
  public static void main(String args[])
  {
//  new ParamPanel();
    refreshCoeffs();
    
    System.out.println("Speed for TWS:6 , TWA=52 = " + getSpeed(6D, 52D));
    System.out.println("Speed for TWS:10, TWA=52 = " + getSpeed(10D, 52D));
    System.out.println("Speed for TWS:20, TWA=52 = " + getSpeed(20D, 52D));
    System.out.println("Speed for TWS:6 , TWA=90 = " + getSpeed(6D, 90D));
    System.out.println("Speed for TWS:10, TWA=90 = " + getSpeed(10D, 90D));
    System.out.println("Speed for TWS:20, TWA=90 = " + getSpeed(20D, 90D));
    System.out.println("Speed for TWS:6 , TWA=150 = " + getSpeed(6D, 150D));
    System.out.println("Speed for TWS:10, TWA=150 = " + getSpeed(10D, 150D));
    System.out.println("Speed for TWS:20, TWA=150 = " + getSpeed(20D, 150D));
    System.out.println("Speed for TWS:20, TWA=250 = " + getSpeed(20D, 250D));
  }

  public static void setFileName(String fileName)
  {
    PolarHelper.fileName = fileName;
  }

  public static String getFileName()
  {
    return fileName;
  }

  public static void setPolarCoeff(double polarCoeff)
  {
    PolarHelper.polarCoeff = polarCoeff;
  }

  public static double getPolarCoeff()
  {
    return polarCoeff;
  }
}
