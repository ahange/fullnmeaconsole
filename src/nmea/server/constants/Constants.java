package nmea.server.constants;

import java.io.File;
import java.io.FileReader;

import java.util.Hashtable;
import java.util.Map;
import java.util.Properties;
import java.util.Set;

public class Constants
{
  public final static String NMEA_SERVER_LISTENER_GROUP_ID = "NMEAServer";
  
  private final static String nmeaSentences[][] = new String[][]
  {
    { "AAM", "Waypoint Arrival Alarm" },
    { "ALM", "GPS Almanac Data" },
    { "APB", "Autopilot Sentence \"B\"" },
    { "ASD", "Autopilot System Data" },
    { "BEC", "Bearing & Distance to Waypoint, Dead Reckoning" },
    { "BOD", "Bearing, Origin to Destination" },
    { "BWC", "Bearing & Distance to Waypoint, Great Circle" },
    { "BWR", "Bearing & Distance to Waypoint, Rhumb Line" },
    { "BWW", "Bearing, Waypoint to Waypoint" },
    { "DBT", "Depth Below Transducer" },
    { "DCN", "Decca Position" },
    { "DPT", "Depth" },
    { "FSI", "Frequency Set Information" },
    { "GGA", "Global Positioning System Fix Data" },
    { "GLC", "Geographic Position, Loran-C" },
    { "GLL", "Geographic Position, Latitude/Longitude" },
    { "GSA", "GPS DOP and Active Satellites" },
    { "GSV", "GPS Satellites in View" },
    { "GXA", "TRANSIT Position" },
    { "HDG", "Heading, Deviation & Variation" },
    { "HDM", "Heading, Magnetic" },
    { "HDT", "Heading, True" },
    { "HSC", "Heading Steering Command" },
    { "LCD", "Loran-C Signal Data" },
    { "MDA", "Meteorological Composite" },
    { "MTA", "Air Temperature" },
    { "MMB", "Barometric Pressure" },
    { "MTW", "Water Temperature" },
    { "MWV", "Wind Speed and Angle" },
    { "MWD", "Wind Speed and Direction" },
    { "OLN", "Omega Lane Numbers" },
    { "OSD", "Own Ship Data" },
    { "RMA", "Recommend Minimum Specific Loran-C Data" },
    { "RMB", "Recommend Minimum Navigation Information" },
    { "RMC", "Recommend Minimum Specific GPS/TRANSIT Data" },
    { "ROT", "Rate of Turn" },
    { "RPM", "Revolutions" },
    { "RSA", "Rudder Sensor Angle" },
    { "RSD", "RADAR System Data" },
    { "RTE", "Routes" },
    { "SFI", "Scanning Frequency Information" },
    { "STN", "Multiple Data ID" },
    { "TRF", "TRANSIT Fix Data" },
    { "TTM", "Tracked Target Message" },
    { "VBW", "Dual Ground/Water Speed" },
    { "VDR", "Set and Drift" },
    { "VHW", "Water Speed and Heading" },
    { "VLW", "Distance Traveled through the Water" },
    { "VPW", "Speed, Measured Parallel to Wind" },
    { "VTG", "Track Made Good and Ground Speed" },
    { "VWR", "Relative wind direction and speed" },
    { "VWT", "True Wind Speed and Angle (deprecated, use MWV)" },
    { "WCV", "Waypoint Closure Velocity" },
    { "WNC", "Distance, Waypoint to Waypoint" },
    { "WPL", "Waypoint Location" },
    { "XDR", "Transducer Measurements" },
    { "XTE", "Cross-Track Error, Measured" },
    { "XTR", "Cross-Track Error, Dead Reckoning" },
    { "ZDA", "Time & Date" },
    { "ZFO", "UTC & Time from Origin Waypoint" },
    { "ZTG", "UTC & Time to Destination Waypoint" }
  };
  
  private static Hashtable<String, String> nmeaMap = new Hashtable<String, String>(nmeaSentences.length);
  
  private static Constants instance = null;
  
  private Constants()
  {
    for (int i=0; i<nmeaSentences.length; i++)
      nmeaMap.put(nmeaSentences[i][0], nmeaSentences[i][1]);   
    // Extra sentences (non standard), properties file
    Properties extraSentences = new Properties();
    try
    {
      extraSentences.load(new FileReader("config" + File.separator + "extra.nmea.properties"));
      Set<Map.Entry<Object, Object>> keys = extraSentences.entrySet();
      for (Map.Entry<Object, Object> k : keys)
      {
//      System.out.println(k.toString());
        String key = k.getKey().toString();
        String value = k.getValue().toString();
//      System.out.println("KEY:" + key + ", VALUE:" + value);
        nmeaMap.put(key, value);   
      }
    }
    catch (Exception ex)
    {
      ex.printStackTrace();
    }
  }
  
  public synchronized static Constants getInstance()
  {
    if (instance == null)
      instance = new Constants();
    return instance;
  }
  
  public Hashtable<String, String> getNMEAMap()
  {
    return nmeaMap;
  }
}
