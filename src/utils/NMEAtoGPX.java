package utils;


import coreutilities.Utilities;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileReader;

import java.io.FileWriter;

import java.text.SimpleDateFormat;

import java.util.HashMap;
import java.util.Map;

import java.util.TimeZone;

import ocss.nmea.parser.RMC;
import ocss.nmea.parser.StringParsers;

public class NMEAtoGPX
{
  private static Map<String, Integer> map = new HashMap<String, Integer>();
  private final static SimpleDateFormat UTC_MASK = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss'Z'");
  static { UTC_MASK.setTimeZone(TimeZone.getTimeZone("etc/UTC")); }
  
  public static void transform(String fileInName, 
                               String fileOutName) throws Exception
  {
    String file  = fileInName;
    String track = fileOutName;
    BufferedReader br = new BufferedReader(new FileReader(file));
    String line = "";

    BufferedWriter bw = new BufferedWriter(new FileWriter(new File(track)));
    bw.write("<?xml version=\"1.0\"?>\n" + 
    "<gpx version=\"1.1\" creator=\"OpenCPN\" xmlns:xsi=\"http://www.w3.org/2001/XMLSchema-instance\" xmlns=\"http://www.topografix.com/GPX/1/1\" xmlns:gpxx=\"http://www.garmin.com/xmlschemas/GpxExtensions/v3\" xsi:schemaLocation=\"http://www.topografix.com/GPX/1/1 http://www.topografix.com/GPX/1/1/gpx.xsd\" xmlns:opencpn=\"http://www.opencpn.org\">\n" + 
    "  <trk>\n" + 
    "    <extensions>\n" + 
    "      <opencpn:guid>21180000-44ac-4218-a090-ed331f980000</opencpn:guid>\n" + 
    "      <opencpn:viz>1</opencpn:viz>\n" + 
    "    </extensions>\n" + 
    "    <trkseg>\n");
    
    while (line != null)
    {
      line = br.readLine();
      if (line != null)
      {
        if (line.startsWith("$") && line.length() > 6)
        {
          String prefix = line.substring(3, 6);
          Integer nb = map.get(prefix);
          if (nb == null)
            nb = new Integer(1);
          else
            nb = new Integer(nb.intValue() + 1);
          map.put(prefix, nb);
          // Specific
          if ("RMC".equals(prefix))
          {
            if (StringParsers.validCheckSum(line))
            {
              RMC rmc = StringParsers.parseRMC(line);
              if (rmc != null && rmc.getRmcTime() != null)
              {
                bw.write("      <trkpt lat=\"" + rmc.getGp().lat + "\" lon=\"" + rmc.getGp().lng + "\">\n" + 
                         "        <time>" + UTC_MASK.format(rmc.getRmcTime()) + "</time>\n" +                                           
                         "      </trkpt>\n");
              }
            }
          }
        }
      }
    }
    br.close();
    bw.write("    </trkseg>\n" + 
    "  </trk>\n" + 
    "</gpx>");
    bw.close();
    String fileDir = fileOutName.substring(0, fileOutName.lastIndexOf(File.separator));
    System.out.println("Showing " + fileDir);
    Utilities.showFileSystem(fileDir);
  }
}