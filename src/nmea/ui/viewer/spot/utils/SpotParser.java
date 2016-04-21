package nmea.ui.viewer.spot.utils;

import java.text.SimpleDateFormat;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.TimeZone;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import ocss.nmea.parser.GeoPos;

public class SpotParser
{
  private final static String SPOT_DATE  = ".*(\\d{6}-\\d{2}z\\.grb)\\sdated\\s((\\d{4})/(\\d{2})/(\\d{2})\\s(\\d{2}:\\d{2}:\\d{2}))";
  private final static String SIX_COLUMN = "(\\d{2}-\\d{2}\\s\\d{2}:\\d{2})\\s*(\\d*\\.\\d)\\s*(\\d*\\.\\d)\\s*(\\d*)\\s*(\\d*\\.\\d)(\\s*(\\d*\\.\\d))*";
  private final static String PLOT_POS = "spot:(\\d*(\\.(\\d*)))([N|S]),(\\d*(\\.(\\d*)))([E|W])";
  
  private final static SimpleDateFormat SDF = new SimpleDateFormat("yyyy-MM-dd HH:mm");
  static
  {
    SDF.setTimeZone(TimeZone.getTimeZone("Etc/UTC"));
  }
  
  private static GeoPos position = null;
  
  public static List<SpotLine> parse(String spotData) throws Exception
  {
    List<SpotLine> result = new ArrayList<SpotLine>();
    
    String[] lines = spotData.split("\n");
    
    Pattern spot     = Pattern.compile(SIX_COLUMN);
    Pattern spotDate = Pattern.compile(SPOT_DATE);
    Pattern spotPosition = Pattern.compile(PLOT_POS);
    
    int year = -1;
    
    for (String line : lines)
    {
      if (line != null)
      {
        if (year == -1)
        {
          Matcher dateMatcher = spotDate.matcher(line);
          if (dateMatcher.find())
          {
            String strYear = dateMatcher.group(3);
            try { year = Integer.parseInt(strYear); } 
            catch (NumberFormatException nfe)
            {
              nfe.printStackTrace();
            }
          }
        }
        Matcher matcher = spot.matcher(line);
        if (matcher.find())
        {
          String date  = matcher.group(1);
          String prmsl = matcher.group(2);
          String tws   = matcher.group(3);
          String twd   = matcher.group(4);
          String rain  = matcher.group(5);
//        String lftx  = matcher.group(6);
          
          Date utc = SDF.parse(Integer.toString(year) + "-" + date);
          result.add(new SpotLine(utc, 
                                  Double.parseDouble(prmsl),
                                  Double.parseDouble(tws),
                                  Integer.parseInt(twd),
                                  Double.parseDouble(rain)));
        } 
        else
        {
          Matcher posMatcher = spotPosition.matcher(line);
          if (posMatcher.find())
          {
            String latitude  = posMatcher.group(1);
            String latSgn    = posMatcher.group(4);
            String longitude = posMatcher.group(5);
            String lngSgn    = posMatcher.group(8);
            double lat = Double.parseDouble(latitude);
            if ("S".equals(latSgn))
              lat *= -1;
            double lng = Double.parseDouble(longitude);
            if ("W".equals(lngSgn))
              lng *= -1;
            position = new GeoPos(lat, lng);
          }
        }
      }
    }
    return result;
  }
  
  public static GeoPos getSpotPos()
  {
    return position;
  }
  
  public static class SpotLine
  {
    private Date date;
    private double prmsl;
    private double tws;
    private int twd;
    private double rain;

    public Date getDate()
    {
      return date;
    }

    public double getPrmsl()
    {
      return prmsl;
    }

    public double getTws()
    {
      return tws;
    }

    public int getTwd()
    {
      return twd;
    }

    public double getRain()
    {
      return rain;
    }
    
    public SpotLine(Date date, double prmsl, double tws, int twd, double rain)
    {
      this.date = date;
      this.prmsl = prmsl;
      this.tws = tws;
      this.twd = twd;
      this.rain = rain;
    }
  }
  
  /*
   * For tests
   */
  public static void main(String[] args) throws Exception
  {
    String spot = "Data extracted from file gfs130702-12z.grb dated 2013/07/02 16:42:32\n" + 
    "request code: spot:37.5N,122.5W|5,3|PRMSL,WIND,RAIN,LFTX\n" + 
    "\n" + 
    "Forecast for 37°30N 122°30W (see notes below)\n" + 
    "Date  Time  PRESS  WIND DIR RAIN LFTX\n" + 
    "        utc    hPa  kts deg mm/h  °C\n" + 
    "----------- ------ ----- --- ---- ----\n" + 
    "07-03 00:00 1011.8  8.4 214  0.0 11.3\n" + 
    "07-03 03:00 1011.8  4.5 187  0.0 12.3\n" + 
    "07-03 06:00 1012.1  4.4 199  0.0 12.5\n" + 
    "07-03 09:00 1011.6  1.6 210  0.0 11.0\n" + 
    "07-03 12:00 1010.9  2.3 267  0.0 10.2\n" + 
    "07-03 15:00 1010.9  1.0 167  0.0 10.3\n" + 
    "07-03 18:00 1011.3  4.5 236  0.0  9.1\n" + 
    "07-03 21:00 1009.7  7.9 253  0.0  9.2\n" + 
    "\n" + 
    "07-04 00:00 1008.7  7.3 264  0.0  9.8\n" + 
    "07-04 03:00 1007.8  5.2 254  0.0 10.1\n" + 
    "07-04 06:00 1007.5  3.5 246  0.0 10.4\n" + 
    "07-04 09:00 1006.6  3.9 246  0.0 10.9\n" + 
    "07-04 12:00 1005.5  1.6 218  0.0 10.5\n" + 
    "07-04 15:00 1006.2  1.6 234  0.0  9.0\n" + 
    "07-04 18:00 1006.2  4.6 272  0.0  6.5\n" + 
    "07-04 21:00 1005.7  10.4 274  0.0  6.8\n" + 
    "\n" + 
    "07-05 00:00 1004.3  9.1 282  0.0  8.7\n" + 
    "07-05 03:00 1004.2  10.0 291  0.0 11.1\n" + 
    "07-05 06:00 1005.1  11.0 297  0.0 12.6\n" + 
    "07-05 09:00 1004.6  10.9 299  0.0 12.8\n" + 
    "07-05 12:00 1005.0  10.7 299  0.0 12.9\n" + 
    "07-05 15:00 1005.9  8.7 295  0.0 13.0\n" + 
    "07-05 18:00 1006.9  7.7 272  0.0 12.5\n" + 
    "07-05 21:00 1006.1  9.2 260  0.0 12.0\n" + 
    "\n" + 
    "07-06 00:00 1004.9  9.0 256  0.0 12.7\n" + 
    "07-06 03:00 1005.5  8.2 242  0.0 13.8\n" + 
    "07-06 06:00 1006.7  6.7 220  0.0 14.6\n" + 
    "07-06 09:00 1006.6  7.8 186  0.0 15.3\n" + 
    "07-06 12:00 1006.7  7.3 181  0.0 15.5\n" + 
    "07-06 15:00 1007.7  7.8 172  0.0 15.2\n" + 
    "07-06 18:00 1008.6  8.8 191  0.0 14.3\n" + 
    "07-06 21:00 1007.6  9.9 200  0.0 13.7\n" + 
    "\n" + 
    "07-07 00:00 1007.2  8.4 203  0.0 13.8\n" + 
    "07-07 03:00 1007.6  5.1 196  0.0 14.5\n" + 
    "07-07 06:00 1008.7  4.4 189  0.0 14.9\n" + 
    "07-07 09:00 1008.3  4.8 185  0.0 14.8\n" + 
    "07-07 12:00 1008.7  5.3 191  0.0 14.1\n" + 
    "07-07 15:00 1009.9  4.2 199  0.0 13.7\n" + 
    "07-07 18:00 1010.5  5.9 227  0.0 13.1\n" + 
    "07-07 21:00 1010.3  7.6 228  0.0 12.8\n" + 
    "\n" + 
    "Refer to notice & warnings sent 2013/07/02 18:36:36, for another copy send a (blank) email to: SpotWarning@saildocs.com\n" + 
    "\n" + 
    "=====";
    List<SpotLine> spotData = parse(spot);
    for (SpotLine sl : spotData)
      System.out.println("Date:" + sl.getDate().toString() + " (system zone) PRMSL:" + sl.getPrmsl() + " hPa, Wind " + sl.getTws() + " kts, " + sl.getTwd() + "\272, rain " + sl.getRain() + " mm/h");
  }
}
