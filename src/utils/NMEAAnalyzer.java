package utils;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.FileReader;

import java.io.FileWriter;

import java.text.SimpleDateFormat;

import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import java.util.Set;
import java.util.TimeZone;
import java.util.TreeMap;

import nmea.server.constants.Constants;

import ocss.nmea.parser.GeoPos;
import ocss.nmea.parser.OverGround;
import ocss.nmea.parser.RMC;
import ocss.nmea.parser.StringGenerator;
import ocss.nmea.parser.StringParsers;
import ocss.nmea.parser.Wind;

import utils.log.LogAnalysis;
import utils.log.LoggedDataSelectedInterface;

public class NMEAAnalyzer
{
  private final static SimpleDateFormat SDF_UT = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss Z");
  static { SDF_UT.setTimeZone(TimeZone.getTimeZone("etc/UTC")); }
  
  private boolean verbose = "true".equals(System.getProperty("analyzer.verbose", "false"));
  
  private LoggedDataSelectedInterface parent = null;
  /*
   * VWR: Wind
   * MWV: Wind
   * RMC: RMC
   * RMB: RMB
   * BAT: float
   * HDG: double[] [HDG_in_HDG, DEV_in_HDG, VAR_in_HDG]
   * VLW: double[] [LOG_in_VLW, DAILYLOG_in_VLW]
   * VHW: double
   * MTW: double
   * MTA: double
   * MMB: double
   * DPT: float
   * GLL: Object[] [GeoPos, Date]
   * VTG: OverGround
   */
  
  public static class ScalarValue
  {
    private double value;
    public ScalarValue(double d) { this.value = d; }
    public double getValue() { return this.value; };
  }
  public static class Hdg extends ScalarValue            { public Hdg(double d) { super(d); } }
  public static class Bsp extends ScalarValue            { public Bsp(double d) { super(d); } }
  public static class WaterTemp extends ScalarValue      { public WaterTemp(double d) { super(d); } }
  public static class AirTemp extends ScalarValue        { public AirTemp(double d) { super(d); } }
  public static class BatteryVoltage extends ScalarValue { public BatteryVoltage(double d) { super(d); } }
  public static class AtmPressure extends ScalarValue    { public AtmPressure(double d) { super(d); } }
  public static class Depth extends ScalarValue          { public Depth(double d) { super(d); } }  
  public static class Humidity extends ScalarValue       { public Humidity(double d) { super(d); } }
  
  public Map<String, Integer> getGenericDataMap(String fileName) throws Exception
  {
    Map<String, Integer> map = new HashMap<String, Integer>();
    String file = fileName;
    BufferedReader br = new BufferedReader(new FileReader(file));
    String line = "";
    long nbl = 0;
    while (line != null)
    {
      line = br.readLine();
      if (line != null)
      {
        nbl++;
        if (line.startsWith("$") && line.length() > 6 && StringParsers.validCheckSum(line))
        {
          String prefix = line.substring(3, 6);
          Integer nb = map.get(prefix);
          if (nb == null)
            nb = new Integer(1);
          else
            nb = new Integer(nb.intValue() + 1);
          map.put(prefix, nb);
        }
      }
    }
    br.close();
    return map;
  }
  
  public Map<String, Map<Date, Object>> getDataMap(String fileName) throws Exception
  {
    Map<String, Map<Date, Object>> dataMap = new HashMap<String, Map<Date, Object>>();
    String file = fileName;
    BufferedReader br = new BufferedReader(new FileReader(file));
    String line = "";
    Date date = null, prevDate = null;
    Date minDate = null, maxDate = null;
    String minDateStr = "", maxDateStr = "";
    Date maxGapFrom = null, maxGapTo = null;
    long min = Long.MAX_VALUE, max = Long.MIN_VALUE;
    int nbl = 0;
    while (line != null)
    {
      line = br.readLine();
      if (line != null)
      {
        nbl++;
        if (nbl % 1000 == 0)
          parent.setNbRecordProcessed(nbl);
        
        if (line.startsWith("$") && line.length() > 6 && StringParsers.validCheckSum(line))
        {
          String prefix = line.substring(3, 6);          
          if ("RMC".equals(prefix))
          {
            RMC rmc = StringParsers.parseRMC(line);
            if (rmc != null)
            {
              date = rmc.getRmcDate();
              if (date != null)
              {
                if (minDate == null || (minDate != null && date.before(minDate)))
                {
                  minDate = date;
                  minDateStr = line;
                }
                if (maxDate == null || (maxDate != null && date.after(maxDate)))
                {
                  maxDate = date;
                  maxDateStr = line;
                }
                if (prevDate != null && date != null)
                {
                  long diff = date.getTime() - prevDate.getTime();
                  if (Math.abs(diff) > (DAY - (5 * SEC)) && Math.abs(diff) < DAY + (5 * SEC)) // Day change, probably
                  {
                    if (Math.abs(diff) < DAY)
                      diff += DAY;
                    if (Math.abs(diff) > DAY)
                      diff -= DAY;
                  }
                  if (diff < 0)
                  {
                    if (verbose)
                      System.out.println("** Line " + nbl + ", Backward jump in time: found " + SDF_UT.format(date) + " after " + SDF_UT.format(prevDate) + "...");
                    date = prevDate; // Reset
                    continue; // Tossion...
                  }
                  if (diff > (10 * MIN)) // More than 10 minutes
                  {
                    System.out.println("** Line " + nbl + ", More than 10 minutes between dates: " + SDF_UT.format(date) + ", " + SDF_UT.format(prevDate));
                //                continue;
                  }
                  min = Math.min(min, diff);
                  if (diff > max)
                  {
                    maxGapFrom = prevDate;
                    maxGapTo = date;
                  }
                  max = Math.max(max, diff);
                }
                prevDate = date;
                //
                Map<Date, Object> map = dataMap.get("RMC");
                if (map == null)
                  map = new TreeMap<Date, Object>();
                map.put(date, rmc);
                dataMap.put("RMC", map);
              }
            }
          }
          if ("VWR".equals(prefix))
          {
            Wind wind = StringParsers.parseVWR(line);
            if (date != null)
            {
              Map<Date, Object> map = dataMap.get("VWR");
              if (map == null)
                map = new TreeMap<Date, Object>();
              map.put(date, wind);
              dataMap.put("VWR", map);
            }
          }
          else if ("BAT".equals(prefix))
          {
            // This is not standard "$XXBAT,14.21,V*29"
            String[] sa = line.split(",");
            float voltage = Float.parseFloat(sa[1]);
  //        System.out.println("Voltage:" + voltage);
            if (date != null)
            {              
              Map<Date, Object> map = dataMap.get("BAT");
              if (map == null)
                map = new TreeMap<Date, Object>();
              map.put(date, new BatteryVoltage(voltage));
              dataMap.put("BAT", map);
            }
          }
          else if ("VHW".equals(prefix))
          {
            double[] da = StringParsers.parseVHW(line);
            double bsp = da[StringParsers.BSP_in_VHW];
            if (date != null)
            {
              Map<Date, Object> map = dataMap.get("VHW");
              if (map == null)
                map = new TreeMap<Date, Object>();
              map.put(date, new Bsp(bsp));
              dataMap.put("VHW", map);
            }
          }
          else if ("VTG".equals(prefix))
          {
            OverGround og = StringParsers.parseVTG(line);
            if (date != null)
            {
              Map<Date, Object> map = dataMap.get("VTG");
              if (map == null)
                map = new TreeMap<Date, Object>();
              map.put(date, og);
              dataMap.put("VTG", map);              
            }
          }
          else if ("HDG".equals(prefix))
          {
            double[] hdg = StringParsers.parseHDG(line);
            if (date != null)
            {
              Map<Date, Object> map = dataMap.get("HDG");
              if (map == null)
                map = new TreeMap<Date, Object>();
              map.put(date, new Hdg(hdg[StringParsers.HDG_in_HDG]));
              dataMap.put("HDG", map);              
            }
          }
          else if ("MTW".equals(prefix))
          {
            double temp = StringParsers.parseMTW(line);
            if (date != null)              
            {
              Map<Date, Object> map = dataMap.get("MTW");
              if (map == null)
                map = new TreeMap<Date, Object>();
              map.put(date, new WaterTemp(temp));
              dataMap.put("MTW", map);              
            }
          }
          else if ("MMB".equals(prefix))
          {
            double atm = StringParsers.parseMMB(line);
        //  if (atm == 0)
        //    System.out.println("Np pressure in " + line);
            if (date != null && atm > 0)              
            {
              Map<Date, Object> map = dataMap.get("MMB");
              if (map == null)
                map = new TreeMap<Date, Object>();
              map.put(date, new AtmPressure(atm));
              dataMap.put("MMB", map);              
            }              
          }
          else if ("MTA".equals(prefix))
          {
            double temp = StringParsers.parseMTA(line);
            if (date != null)              
            {
              Map<Date, Object> map = dataMap.get("MTA");
              if (map == null)
                map = new TreeMap<Date, Object>();
              map.put(date, new AirTemp(temp));            
              dataMap.put("MTA", map);              
            }              
          }
          else if ("XDR".equals(prefix))
          {
            if (date != null)              
            {
              List<StringGenerator.XDRElement> xdrData = StringParsers.parseXDR(line);
              if (xdrData != null)              
              {
                Map<Date, Object> map = dataMap.get("XDR");
                if (map == null)
                  map = new TreeMap<Date, Object>();
                double hum = -1;
                for (StringGenerator.XDRElement xdr : xdrData)
                {
                  if (xdr.getTypeNunit().equals(StringGenerator.XDRTypes.HUMIDITY))
                  {
                    hum = xdr.getValue();
                //  if (hum == 0)
                //    System.out.println("No humidity in " + line);
                //  System.out.println("XDR:" + hum + "%");
                    break;
                  }
                }
                if (hum != -1 && hum > 0)
                  map.put(date, new Humidity(hum));            
                dataMap.put("XDR", map);    // TODO Narrow this one, depending on the data.     
              }
            }              
          }
          else if ("DPT".equals(prefix))
          {
            double dpt = StringParsers.parseDPT(line, StringParsers.DEPTH_IN_METERS);
            if (date != null)
            {
              Map<Date, Object> map = dataMap.get("DPT");
              if (map == null)
                map = new TreeMap<Date, Object>();
              map.put(date, new Depth(dpt));            
              dataMap.put("DPT", map);              
            }              
          }
        }
      }
    }
    br.close();
    System.out.println("+---------------------------------------------------------");
    System.out.println("| ... Processed " + Integer.toString(nbl) + " line(s).");
    System.out.println("| Date span:" + minDate.toString() + ", " + maxDate.toString());
    System.out.println("| Min: " + minDateStr);
    System.out.println("| Max: " + maxDateStr);
    System.out.println("+---------------------------------------------------------");
    parent.setNbRecordProcessed(nbl);
    return dataMap;
  }
  
  private final static long SEC  = 1000L;
  private final static long MIN  = 60 * SEC;
  private final static long HOUR = 60 * MIN;
  private final static long DAY  = 24 * HOUR;
  
  private Map<String, Integer> dataMap = null;
  private Map<String, Map<Date, Object>> fullData = null;

  public Map<String, Integer> getDataMap()
  {
    return this.dataMap;
  }
  
  public Map<String, Map<Date, Object>> getFullData()
  {
    return this.fullData;
  }
  
  public NMEAAnalyzer(String fileName, LoggedDataSelectedInterface parent) throws Exception
  {
    this.parent = parent;
    this.dataMap  = this.getGenericDataMap(fileName);
    this.fullData = this.getDataMap(fileName);
  }
  
  public static void main(String[] args) throws Exception
  {
    String fileName = "D:\\OlivSoft\\all-scripts\\logged-data\\2014-09-01.headless.labor.day.week.end.nmea";
    NMEAAnalyzer na = new NMEAAnalyzer(fileName, null);

    Map<String, Integer> map = na.getDataMap();
    // Dump
    for (String s : map.keySet())
      System.out.println(s + ":" + map.get(s).intValue() + ", " + (Constants.getInstance().getNMEAMap().get(s) == null ? "[Non standard]" : Constants.getInstance().getNMEAMap().get(s)));
    System.out.println("================================");
    
    Map<String, Map<Date, Object>> fullData = na.getFullData();
    System.out.println("Full Data: " + fullData.size() + " entry(ies).");

    // Example: BAT
    Map<Date, ScalarValue> batData = new TreeMap<Date, ScalarValue>();
    Map<Date, Object> datamap = fullData.get("BAT");
    Set<Date> dates = datamap.keySet();
    for (Date d : dates)
    {
      NMEAAnalyzer.BatteryVoltage bv = (NMEAAnalyzer.BatteryVoltage)datamap.get(d);
      batData.put(d, bv);
    }
    new LogAnalysis("BAT", batData, "Battery Voltage", "V");
    
    // Example: DPT
    Map<Date, ScalarValue> dptData = new TreeMap<Date, ScalarValue>();
    datamap = fullData.get("DPT");
    dates = datamap.keySet();
    for (Date d : dates)
    {
      NMEAAnalyzer.Depth depth = (NMEAAnalyzer.Depth)datamap.get(d);
      dptData.put(d, depth);
    }
    new LogAnalysis("DPT", dptData, "Depth", "m");
  }  
}
