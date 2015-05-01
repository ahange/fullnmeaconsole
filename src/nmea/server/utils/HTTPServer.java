package nmea.server.utils;

import coreutilities.log.Logger;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.io.PrintWriter;

import java.net.ServerSocket;
import java.net.Socket;
import java.net.URLDecoder;
import java.net.URLEncoder;

import java.text.DecimalFormat;
import java.text.SimpleDateFormat;

import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Properties;
import java.util.Set;

import nmea.event.NMEAReaderListener;

import nmea.server.ctx.NMEAContext;
import nmea.server.ctx.NMEADataCache;

import ocss.nmea.api.NMEAListener;
import ocss.nmea.parser.Angle180;
import ocss.nmea.parser.Angle180EW;
import ocss.nmea.parser.Angle180LR;
import ocss.nmea.parser.Angle360;
import ocss.nmea.parser.ApparentWind;
import ocss.nmea.parser.Depth;
import ocss.nmea.parser.Distance;
import ocss.nmea.parser.GeoPos;
import ocss.nmea.parser.Pressure;
import ocss.nmea.parser.RMC;
import ocss.nmea.parser.SVData;
import ocss.nmea.parser.SolarDate;
import ocss.nmea.parser.Speed;
import ocss.nmea.parser.StringParsers;
import ocss.nmea.parser.Temperature;
import ocss.nmea.parser.TrueWindDirection;
import ocss.nmea.parser.TrueWindSpeed;
import ocss.nmea.parser.UTC;
import ocss.nmea.parser.UTCDate;
import ocss.nmea.parser.UTCTime;
import ocss.nmea.parser.Wind;

import user.util.GeomUtil;
import user.util.TimeUtil;


/**
 * Dedicated HTTP Server.
 * This is NOT J2EE Compliant, not even CGI.
 *
 * Runs the communication between an HTTP client and the
 * features of the Data server to be displayed remotely.
 */
public class HTTPServer 
{
  private final static SimpleDateFormat DATE_FORMAT = new SimpleDateFormat("dd MMM yyyy HH:mm:ss 'UTC'");
  private final static SimpleDateFormat TIME_FORMAT = new SimpleDateFormat("HH:mm:ss 'UTC'");
  
  private boolean enabled = true;

  public HTTPServer()
  {
  }

  private final static int XML_OUTPUT  = 0;
  private final static int TEXT_OUTPUT = 1;
  private final static int JSON_OUTPUT = 2;

//private DecimalFormat decimalFmt = new DecimalFormat("#0.00");
//private DecimalFormat integerFmt = new DecimalFormat("##0");

  private int output      = XML_OUTPUT;
  private boolean verbose = "true".equals(System.getProperty("verbose", "false"));
  private String propFileName = "";
  private Object[][] data;
  private int _port = 0;
  
  public HTTPServer(String[] prms, String pfn, Object[][] tableData) throws Exception
  {
    propFileName = pfn;
    data = tableData;
    // Bind the server
    String machineName = "localhost";
    String port        = "6666";

    machineName = System.getProperty("http.host", machineName);
    port        = System.getProperty("http.port", port);
    
    System.out.println("HTTP Host:" + machineName);
    System.out.println("HTTP Port:" + port);
    
    if (prms != null && prms.length > 0)
    {
      // verbose
      // fmt
      for (int i=0; i<prms.length; i++)
      {
        if (prms[i].startsWith("-verbose="))
        {
          verbose = prms[i].substring("-verbose=".length()).equals("y");
        }
        else if (prms[i].startsWith("-fmt="))
        {
          String fmt = prms[i].substring("-fmt=".length());
          if (fmt.equals("xml"))
            output = XML_OUTPUT;
          else if (fmt.equals("txt"))
            output = TEXT_OUTPUT;
          else if (fmt.equals("json"))
            output = JSON_OUTPUT;
        }
      }
    }
    
    NMEAContext.getInstance().addNMEAReaderListener(new NMEAReaderListener("HTTP", "HTTPServer")
    {
      public void enableHttpServer(boolean b) 
      {
//      System.out.println("HTTP Server enabled:" + b);
        enabled = b;
      }
    });
    
    try
    {
      _port = Integer.parseInt(port);
    }
    catch (NumberFormatException nfe)
    {
      throw nfe;
    }

    // Infinite loop, waiting for requests
    Thread httpListenerThread = new Thread("HTTPListener") 
    {
      public void run()        
      {
        boolean go = true;
        try
        {
          Map<String, String> header = new HashMap<String, String>();
          ServerSocket ss = new ServerSocket(_port);
          System.err.println("Port " + _port + " opened successfully.");
          boolean help = false;
          boolean latitude = false, longitude = false,
                  latitudefmt = false, longitudefmt = false;
          boolean hdg = false, hdm = false, bsp = false, sog = false, 
                  cog = false, awa = false, aws = false, twa = false, 
                  tws = false, localtime = false, lnginhours = false,
                  prmUpdate = false;
          while (go)
          {
            Socket client = ss.accept();
            BufferedReader in = new BufferedReader(new InputStreamReader(client.getInputStream()));
            PrintWriter   out = new PrintWriter(new OutputStreamWriter(client.getOutputStream()));
            
            help = latitude = longitude = latitudefmt = longitudefmt = localtime = lnginhours =
            hdg = hdm = bsp = sog = cog = awa = aws = twa = tws = false;
            String line;
            String fileToFetch = "";
            while ((line = in.readLine()) != null && enabled)
            {
              if (verbose) System.out.println("HTTP Request:" + line);
              if (line.length() == 0)
                break;
              else if (line.startsWith("POST /exit") || line.startsWith("GET /exit"))
              {
//              System.out.println("Received an exit signal");
                go = false;
                Utils.play(this.getClass().getResource("lazer.wav"));
              }
              else if (line.startsWith("POST /help") || line.startsWith("GET /help"))
              {
//              System.out.println("Received an help request");
                help = true;
              }
              else if (line.startsWith("POST /latitude-fmt") || line.startsWith("GET /latitude-fmt"))
              {
//              System.out.println("Received a latitude-fmt request");
                latitudefmt = true;
              }
              else if (line.startsWith("POST /longitude-fmt") || line.startsWith("GET /longitude-fmt"))
              {
//              System.out.println("Received a longitude-fmt request");
                longitudefmt = true;
              }
              else if (line.startsWith("POST /latitude") || line.startsWith("GET /latitude"))
              {
//              System.out.println("Received a latitude request");
                latitude = true;
              }
              else if (line.startsWith("POST /longitude") || line.startsWith("GET /longitude"))
              {
//              System.out.println("Received a longitude request");
                longitude = true;
              }
              else if (line.startsWith("POST /bsp") || line.startsWith("GET /bsp"))
              {
//              System.out.println("Received a bsp request");
                bsp = true;
              }
              else if (line.startsWith("POST /cog") || line.startsWith("GET /cog"))
              {
//              System.out.println("Received a cog request");
                cog = true;
              }
              else if (line.startsWith("POST /sog") || line.startsWith("GET /sog"))
              {
//              System.out.println("Received a sog request");
                sog = true;
              }
              else if (line.startsWith("POST /hdg") || line.startsWith("GET /hdg"))
              {
//              System.out.println("Received a hdg request");
                hdg = true;
              }
              else if (line.startsWith("POST /hdm") || line.startsWith("GET /hdm"))
              {
//              System.out.println("Received a hdm request");
                hdm = true;
              }
              else if (line.startsWith("POST /awa") || line.startsWith("GET /awa"))
              {
//              System.out.println("Received a awa request");
                awa = true;
              }
              else if (line.startsWith("POST /aws") || line.startsWith("GET /aws"))
              {
//              System.out.println("Received a aws request");
                aws = true;
              }
              else if (line.startsWith("POST /twa") || line.startsWith("GET /twa"))
              {
//              System.out.println("Received a twa request");
                twa = true;
              }
              else if (line.startsWith("POST /tws") || line.startsWith("GET /tws"))
              {
//              System.out.println("Received a tws request");
                tws = true;
              }
              else if (line.startsWith("POST /localtime") || line.startsWith("GET /localtime"))
              {
//              System.out.println("Received a localtime request");
                localtime = true;
              }
              else if (line.startsWith("POST /lnginhours") || line.startsWith("GET /lnginhours"))
              {
//              System.out.println("Received a localtime request");
                lnginhours = true;
              }
              else if (line.startsWith("GET /update-prms"))
              {
                prmUpdate = true;
                // Update the cache and whatever needs to be updated
                updateCalParams(line);
//              System.out.println(line);
//              System.out.println("---- Update Prms request ---");
              }
              else if (line.startsWith("POST / ") || line.startsWith("GET / "))
              {
                // All data in XML Format                
              }
              else if (line.startsWith("GET /")) // display a file
              {
                fileToFetch = line.substring("GET /".length());
                fileToFetch = fileToFetch.substring(0, fileToFetch.indexOf(" "));
//              System.out.println("********** File to fetch:[" + fileToFetch + "] *************");
              }
//            System.out.println("Read:[" + line + "]");
              if (line.indexOf(":") > -1) // Header?
              {
                String headerKey = line.substring(0, line.indexOf(":"));
                String headerValue = line.substring(line.indexOf(":") + 1);
                header.put(headerKey, headerValue);
              }
            }
            String contentType = "text/plain";
            if (!help && !latitude && !longitude && !latitudefmt && !longitudefmt && !localtime && !lnginhours && !hdg && 
                !hdm && !bsp && !sog && !cog && !awa && !aws && !twa && !tws && !prmUpdate)
            {
              if (output == XML_OUTPUT)
                contentType = "text/xml";
              if (output == JSON_OUTPUT)
                contentType = "application/json";
            }

            String content = "";
            if (help)          
            {
              content = (generateHelpContent());
              contentType = "text/html";
            }
            else if (latitude)
              content = (generateLatitude());
            else if (longitude)
              content = (generateLongitude());
            else if (latitudefmt)
              content = (generateLatitudeFmt());
            else if (longitudefmt)
              content = (generateLongitudeFmt());
            else if (bsp)
//            content = (df22.format(Math.random() * 20.0));
              content = (generateBSP());
            else if (cog)
              content = (generateCOG());
            else if (sog)
              content = (generateSOG());
            else if (hdg)
//            content = (Integer.toString((int)(Math.random() * 360.0)));
              content = (generateHDG());
            else if (hdm)
//            content = (Integer.toString((int)(Math.random() * 360.0)));
              content = (generateHDM());
            else if (awa)
//            content = (Integer.toString((int)(Math.random() * 180.0)));
              content = (generateAWA());
            else if (aws)
//            content = (df22.format(Math.random() * 60.0));
              content = (generateAWS());
            else if (twa)
//            content = (Integer.toString((int)(Math.random() * 180.0)));
              content = (generateTWA());
            else if (tws)
//            content = (df22.format(Math.random() * 60.0));
              content = (generateTWS());
            else if (localtime)
              content = (generateLocalSolarTime());
            else if (lnginhours)
              content = (generateLngInHours());
            else if (prmUpdate)
            {
              content = "";
              prmUpdate = false;
            }
            else if (fileToFetch.trim().length() > 0)
            {
              File f = new File(fileToFetch);
              if (!f.exists())
                out.println(fileToFetch + " not found from " + System.getProperty("user.dir"));
              else
              {
                if (fileToFetch.toUpperCase().endsWith(".HTML") ||
                    fileToFetch.toUpperCase().endsWith(".XHTML"))
                  contentType = "text/html";
                else if (fileToFetch.toUpperCase().endsWith(".XML") ||
                         fileToFetch.toUpperCase().endsWith(".XSD"))
                  contentType = "text/xml";
                else if (fileToFetch.toUpperCase().endsWith(".XSL"))
                  contentType = "text/xsl";
                else if (fileToFetch.toUpperCase().endsWith(".TXT"))
                  contentType = "text/plain";
                else if (fileToFetch.toUpperCase().endsWith(".JS"))
                  contentType = "text/javascript";
                else if (fileToFetch.toUpperCase().endsWith(".CSS"))
                  contentType = "text/css";
                else
                  System.out.println("File extension not managed for " + fileToFetch); // We don't read binaries. See below.
//              System.out.println("............... Reading " + f.getAbsolutePath());
                BufferedReader br = new BufferedReader(new FileReader(f));
                String data = "";

                out.print("HTTP/1.1 200 \r\n"); 
                out.print("Content-Type: " + contentType + "\r\n");
//              out.print("Content-Length: " + content.length() + "\r\n");
//              out.print("Access-Control-Allow-Origin: *\r\n"); 
                out.print("\r\n"); // End Of Header
                //
                boolean ok = true;
                while (ok)
                {
                  data = br.readLine();
                  if (data == null)
                    ok = false;
                  else
                    out.println(data);
                }
                br.close();
              }
            }
            else
              content = (generateContent());
            
            if (content.length() > 0 && enabled)
            {
              // Headers?
              out.print("HTTP/1.1 200 \r\n"); 
              out.print("Content-Type: " + contentType + "\r\n");
              out.print("Content-Length: " + content.length() + "\r\n");
              out.print("Access-Control-Allow-Origin: *\r\n"); 
              out.print("\r\n"); // End Of Header
              //
              out.println(content);
            }
            out.flush();
            out.close();
            in.close();
            client.close();
          }
          ss.close();
        }
        catch (Exception e)
        {
          System.err.println(">>> Port " + _port + ", " + e.toString() + " >>>");
          Logger.log(e.getLocalizedMessage(), Logger.INFO);      
          e.printStackTrace();
          System.err.println("<<< Port " + _port + " <<<");
        }
      }
    };
    httpListenerThread.start();
    try 
    {
      Utils.playSound(this.getClass().getResource("saberup.wav")); 
    } 
    catch (Exception ex) { ex.printStackTrace(); }
  }

  public void setEnabled(boolean enabled)
  {
    this.enabled = enabled;
  }

  public boolean isEnabled()
  {
    return enabled;
  }

  private enum CacheToQSMatch
  {
    BSP_FACTOR("bsp", NMEADataCache.BSP_FACTOR),
    AWS_FACTOR("aws", NMEADataCache.AWS_FACTOR),
    AWA_OFFSET("awa", NMEADataCache.AWA_OFFSET),
    HDG_OFFSET("hdg", NMEADataCache.HDG_OFFSET),
    MAX_LEEWAY("lwy", NMEADataCache.MAX_LEEWAY),
    DEVIATION_FILE("dev", NMEADataCache.DEVIATION_FILE),
    DEFAULT_DECLINATION("dec", NMEADataCache.DEFAULT_DECLINATION),
    DAMPING("dpg", NMEADataCache.DAMPING),
    POLAR_FILE("pol", NMEADataCache.POLAR_FILE_NAME),
    POLAR_FACTOR("fac", NMEADataCache.POLAR_FACTOR);
    
    private final String name;
    private final String key;

    CacheToQSMatch(String name, String key)
    {
      this.name = name;
      this.key = key;
    }
    
    public String prmname() { return this.name; }
    public String key() { return this.key; }
  }
  
  private void updateCalParams(String line)
  {
    try
    {
      int from = line.indexOf(" ") + 1;
      String request = line.substring(from, line.indexOf(" ", from + 1));
//    System.out.println("[" + request + "]");
      String qString = request.substring(request.indexOf("?") + 1);
      String[] prms = qString.split("&");
      for (String nvPair : prms)
      {
        String[] nv = nvPair.split("=");
        for (CacheToQSMatch match : CacheToQSMatch.values())
        {
          if (match.prmname().equals(nv[0]))
          {
            Object cachedData = NMEAContext.getInstance().getCache().get(match.key());
            if (cachedData instanceof String)
            {
              String str = URLDecoder.decode(nv[1], "ISO-8859-1");
              NMEAContext.getInstance().getCache().put(match.key, str);              
            }
            else if (cachedData instanceof Double)
            {
              String str = URLDecoder.decode(nv[1], "ISO-8859-1");
              NMEAContext.getInstance().getCache().put(match.key, new Double(str));
            }
            else if (cachedData instanceof Integer)
            {
              String str = URLDecoder.decode(nv[1], "ISO-8859-1");
              NMEAContext.getInstance().getCache().put(match.key, new Integer(str));
            }
            else if (cachedData instanceof Angle180EW)
            {
              String str = URLDecoder.decode(nv[1], "ISO-8859-1");
              NMEAContext.getInstance().getCache().put(match.key, new Angle180EW(Double.parseDouble(str)));
            }
            else
              System.out.println("Un-managed type:" + cachedData.getClass().getName());
            break;
          }
        }
      }      
      NMEAContext.getInstance().fireDampingHasChanged(((Integer)NMEAContext.getInstance().getCache().get(NMEADataCache.DAMPING)).intValue());
      NMEAContext.getInstance().fireDeviationCurveChanged(Utils.loadDeviationHashtable((String)NMEAContext.getInstance().getCache().get(NMEADataCache.DEVIATION_FILE)));
      
      // Write properties file
      Utils.writeNMEAParameters();

      System.out.println("Prms Updated.");
    }
    catch (Exception ex)
    {
      ex.printStackTrace();
    }
  }
  
  private List<String> getSentenceList()
  {
    List<String> al = new ArrayList<String>(data.length);
    for (int i=0; i<data.length; i++)
      al.add((String)data[i][0]);
    return al;
  }
  
  private String getSentence(String sentenceID)
  {
    String sentence = "";
    for (int i=0; i<data.length; i++)
    {
      if (((String)data[i][0]).substring(2).equals(sentenceID) || ((String)data[i][0]).equals(sentenceID))
        sentence = (String)data[i][1];
    }
    return sentence;
  }
  
  private String generateHelpContent()
  {
    Properties properties = new Properties();
    String currentPrefix = "";
    String currentSentences = "";
    try
    {
      properties.load(new FileInputStream(propFileName));
//    currentPrefix = properties.getProperty("device.prefix");
      currentSentences = properties.getProperty("nmea.sentences");
    }
    catch (Exception ex)
    {
      ex.printStackTrace();
    }
    String str = ""; // "Content-Type: text/html\r\n\r\n";
    str += "<html><head><title>You've requested help</title></head><body><pre>\n";
    str += ("Date is:" + new Date().toString() + "\n\n");
    str += "Content of the " + propFileName + " file:\n";
    str += "------------------------------------\n";
//  str += ("Device prefix:" + currentPrefix + "\n");
    str += ("NMEA Sentences:" + currentSentences + "\n");
    str += "------------------------------------\n";
    str += "Based on the sentences you've subscribed to, you might request:\n";
    str += "/latitude\n";
    str += "/longitude\n";
    str += "/latitude-fmt\n";
    str += "/longitude-fmt\n";
    str += "/bsp\n";
    str += "/sog\n";
    str += "/hdg\n";
    str += "/hdm\n";
    str += "/cog\n";
    str += "/awa\n";
    str += "/aws\n";
    str += "/twa\n";
    str += "/tws\n";
    str += "/localtime\n";
    str += "/lnginhours\n";
    str += "</pre></body></html>\n";    
    return str;
  }
  
//  private DecimalFormat df3  = new DecimalFormat("000");
//  private DecimalFormat df31 = new DecimalFormat("000.0");
//  private DecimalFormat df22 = new DecimalFormat("00.00");

  private DecimalFormat df3  = new DecimalFormat("##0");
  private DecimalFormat df41 = new DecimalFormat("###0.0");
  private DecimalFormat df22 = new DecimalFormat("#0.00");

  private String generateLatitude()
  {
    if (false)
    {
    if (data == null)
      return "0.0";
    else
    {
      try
      {
        RMC rmc = StringParsers.parseRMC(getSentence("RMC"));
        double lat = rmc.getGp().lat;
        return Double.toString(lat);
      }
      catch (Exception e)
      {
        return "0.0";
      }
    }
    }
    else
    {
      double lat = 0d; 
      try { lat = ((GeoPos) NMEAContext.getInstance().getCache().get(NMEADataCache.POSITION)).lat; } catch (Exception ignore) {}
      return Double.toString(lat);
    }
  }
  
  private String generateLongitude()
  {
    if (false)
    {
      if (data == null)
        return "0.0";
      else
      {
        try
        {
          RMC rmc = StringParsers.parseRMC(getSentence("RMC"));
          double lng = rmc.getGp().lng;
          return Double.toString(lng);
        }
        catch (Exception e)
        {
          return "0.0";
        }
      }
    }
    else
    {
      double lng = 0d; 
      try { lng = ((GeoPos) NMEAContext.getInstance().getCache().get(NMEADataCache.POSITION)).lng; } catch (Exception ignore) {}
      return Double.toString(lng);
    }
  }
  
  private String generateLatitudeFmt()
  {
    if (data == null)
      return "0.0";
    else
    {
      try
      {
        RMC rmc = StringParsers.parseRMC(getSentence("RMC"));
        String lat = rmc.getGp().getLatInDegMinDec();
        return lat;
      }
      catch (Exception e)
      {
        return "0.0";
      }
    }
  }
  
  private String generateLongitudeFmt()
  {
    if (data == null)
      return "0.0";
    else
    {
      try
      {
        RMC rmc = StringParsers.parseRMC(getSentence("RMC"));
        String lng = rmc.getGp().getLngInDegMinDec();
        return lng;
      }
      catch (Exception e)
      {
        return "0.0";
      }
    }
  }
  
  private String generateLngInHours()
  {
    if (data == null)
      return "0.000";
    else
    {
      try
      {
        RMC rmc = StringParsers.parseRMC(getSentence("RMC"));
        double lng = rmc.getGp().lng;
        double lngInHours = GeomUtil.degrees2hours(lng);
        return Double.toString(lngInHours);
      }
      catch (Exception e)
      {
        return "0.000";
      }
    }
  }
  
  private String generateLocalSolarTime()
  {
    if (data == null)
      return "00:00:00";
    else
    {
      try
      {
        RMC rmc = StringParsers.parseRMC(getSentence("RMC"));
        double lng = rmc.getGp().lng;
        double lngInHours = GeomUtil.degrees2hours(lng);
        Date ut = TimeUtil.getGMT();
        SimpleDateFormat sdf = new SimpleDateFormat("EEE, d MMM yyyy HH:mm:ss");
        String str = sdf.format(ut) + " UT";
        long dategmt = ut.getTime();
        long localtime = dategmt + (long)(lngInHours * 3600L * 1000L);
        Date localsolar = new Date(localtime);
        str = sdf.format(localsolar);
        return str;
      }
      catch (Exception e)
      {
        return "00:00:00";
      }
    }
  }
  
  private String generateCOG()
  {
    if (false)
    {
      if (data == null)
        return "0.0";
      else
      {
        try
        {
          RMC rmc = StringParsers.parseRMC(getSentence("RMC"));
          double cog = rmc.getCog();
  //      System.out.println("Returning COG:" + df31.format(cog));
          return df41.format(cog);
        }
        catch (Exception e)
        {
          return "0.0";
        }
      }
    }
    else
    {
      double cog = 0d; 
      try { cog = ((Angle360) NMEAContext.getInstance().getCache().get(NMEADataCache.COG)).getValue(); } catch (Exception ignore) {}
      return df41.format(cog);
    }
  }
  
  private String generateSOG()
  {
    if (false)
    {
      if (data == null)
        return "0.0";
      else
      {
        try
        {
          RMC rmc = StringParsers.parseRMC(getSentence("RMC"));
          double sog = rmc.getSog();
  //      System.out.println("Returning SOG:" + df22.format(sog));
          return df22.format(sog);
        }
        catch (Exception e)
        {
          return "0.0";
        }
      }
    }
    else
    {
      double sog = 0d; 
      try { sog = ((Speed) NMEAContext.getInstance().getCache().get(NMEADataCache.SOG)).getValue(); } catch (Exception ignore) {}
      return df22.format(sog);
    }
  }
  
  private String generateBSP()
  {
    double speed = 0d; // StringParsers.parseVHW(getSentence("VHW"))[StringParsers.BSP_in_VHW];
    try { speed = ((Speed) NMEAContext.getInstance().getCache().get(NMEADataCache.BSP)).getValue(); } catch (Exception ignore) {}
    return df22.format(speed);
  }
  
  private String generateHDG()
  {
    int heading = 0; // (int)StringParsers.parseHDG(getSentence("HDG"))[StringParsers.HDG_in_HDG];
    try { heading = (int)Math.round(((Angle360) NMEAContext.getInstance().getCache().get(NMEADataCache.HDG_TRUE)).getValue()); } catch (Exception ignore) {}
    return Integer.toString(heading);
  }
  
  private String generateHDM()
  {    
    int heading = StringParsers.parseHDM(getSentence("HDM"));
    return Integer.toString(heading);
  }

  private String generateAWA()
  {
    if (false)
    {
      Wind wind = StringParsers.parseMWV(getSentence("MWV"));
      if (wind != null && wind instanceof ApparentWind)
        return Integer.toString(wind.angle);
      else
        return "000";
    }
    else
    {
      double awa = 0d; 
      try { awa = ((Angle180) NMEAContext.getInstance().getCache().get(NMEADataCache.AWA)).getValue(); } catch (Exception ignore) {}
      return df41.format(awa);
    }
  }

  private String generateTWA()
  {
    if (false)
      return "000";
    else
    {
      double twa = 0d; 
      try { twa = ((Angle180) NMEAContext.getInstance().getCache().get(NMEADataCache.TWA)).getValue(); } catch (Exception ignore) {}
      return df41.format(twa);
    }
  }
  
  private String generateAWS()
  {
    if (false)
    {
      Wind wind = StringParsers.parseMWV(getSentence("MWV"));
      if (wind != null && wind instanceof ApparentWind)
        return df22.format(wind.speed);
      else
        return "00.00";
    }
    else
    {
      double aws = 0d; 
      try { aws = ((Speed) NMEAContext.getInstance().getCache().get(NMEADataCache.AWS)).getValue(); } catch (Exception ignore) {}
      return df22.format(aws);
    }
  }
  
  private String generateTWS()
  {
    if (false)
      return "00.00";
    else
    {
      double tws = 0d; 
      try { tws = ((TrueWindSpeed) NMEAContext.getInstance().getCache().get(NMEADataCache.TWS)).getValue(); } catch (Exception ignore) {}
      return df22.format(tws);
    }
  }
  
  private String generateContent() // From the cache
  {
    String str = "";
    if (output == XML_OUTPUT)
    {
      str += ("<?xml version='1.0' encoding='UTF-8'?>\n");
      str += ("<?xml-stylesheet href=\"nmea-xml-html.xsl\" type=\"text/xsl\"?>\n");
      str += "<!DOCTYPE data [\n" + 
      " <!ENTITY deg     \"&#176;\">\n" + 
      "]>\n";
      str += ("<data>\n");
    }  
    else if (output == JSON_OUTPUT)
    {
      str += "{\n";
    }
    
    NMEADataCache cache = NMEAContext.getInstance().getCache();
    synchronized (cache)
    {
      Set<String> keys = cache.keySet();
      boolean first = true;
      for (String k : keys)
      {
        Object cached = cache.get(k);
        if (cached instanceof Speed)
        {
          if (k.equals(NMEADataCache.BSP))
          {
  //        str += ("  <bsp>" + df22.format(((Speed)cached).getValue()) + "</bsp>\n");
            str += (((!first && output == JSON_OUTPUT)?",\n":"") + "  " + dataFormat(df22.format(((Speed)cached).getValue()), "bsp", output, NUMERIC_OPTION) + ((output != JSON_OUTPUT)?"\n":""));
            first = false;
          }
          else if (k.equals(NMEADataCache.SOG))
          {
  //        str += ("  <sog>" + df22.format(((Speed)cached).getValue()) + "</sog>\n");
            str += (((!first && output == JSON_OUTPUT)?",\n":"") + "  " + dataFormat(df22.format(((Speed)cached).getValue()), "sog", output, NUMERIC_OPTION) + ((output != JSON_OUTPUT)?"\n":""));
            first = false;
          }
          else if (k.equals(NMEADataCache.AWS))
          {
  //        str += ("  <aws>" + df22.format(((Speed)cached).getValue()) + "</aws>\n");
            str += (((!first && output == JSON_OUTPUT)?",\n":"") + "  " + dataFormat(df22.format(((Speed)cached).getValue()), "aws", output, NUMERIC_OPTION) + ((output != JSON_OUTPUT)?"\n":""));
            first = false;
          }
          else if (k.equals(NMEADataCache.TWS))
          {
  //        str += ("  <tws>" + df22.format(((Speed)cached).getValue()) + "</tws>\n");
            str += (((!first && output == JSON_OUTPUT)?",\n":"") + "  " + dataFormat(df22.format(((Speed)cached).getValue()), "tws", output, NUMERIC_OPTION) + ((output != JSON_OUTPUT)?"\n":""));
            first = false;
          }
          else if (k.equals(NMEADataCache.CSP))
          {
  //        str += ("  <csp>" + df22.format(((Speed)cached).getValue()) + "</csp>\n");
            str += (((!first && output == JSON_OUTPUT)?",\n":"") + "  " + dataFormat(df22.format(((Speed)cached).getValue()), "csp", output, NUMERIC_OPTION) + ((output != JSON_OUTPUT)?"\n":""));
            first = false;
          }
        }
        else if (cached instanceof Angle360)
        {
          if (k.equals(NMEADataCache.COG))
          {
  //        str += ("  <cog>" + df3.format(((Angle360)cached).getValue()) + "</cog>\n");
            str += (((!first && output == JSON_OUTPUT)?",\n":"") + "  " + dataFormat(df3.format(((Angle360)cached).getValue()), "cog", output, NUMERIC_OPTION) + ((output != JSON_OUTPUT)?"\n":""));
            first = false;
          }
          else if (k.equals(NMEADataCache.HDG_TRUE))
          {
  //        str += ("  <hdg>" + df3.format(((Angle360)cached).getValue()) + "</hdg>\n");        
            str += (((!first && output == JSON_OUTPUT)?",\n":"") + "  " + dataFormat(df3.format(((Angle360)cached).getValue()), "hdg", output, NUMERIC_OPTION) + ((output != JSON_OUTPUT)?"\n":""));
            first = false;
          }
          else if (k.equals(NMEADataCache.TWD))
          {
  //        str += ("  <twd>" + df3.format(((Angle360)cached).getValue()) + "</twd>\n");        
            str += (((!first && output == JSON_OUTPUT)?",\n":"") + "  " + dataFormat(df3.format(((Angle360)cached).getValue()), "twd", output, NUMERIC_OPTION) + ((output != JSON_OUTPUT)?"\n":""));
            first = false;
          }
          else if (k.equals(NMEADataCache.CMG))
          {
  //        str += ("  <cmg>" + df3.format(((Angle360)cached).getValue()) + "</cmg>\n");        
            str += (((!first && output == JSON_OUTPUT)?",\n":"") + "  " + dataFormat(df3.format(((Angle360)cached).getValue()), "cmg", output, NUMERIC_OPTION) + ((output != JSON_OUTPUT)?"\n":""));
            first = false;
          }
          else if (k.equals(NMEADataCache.CDR))
          {
  //        str += ("  <cdr>" + df3.format(((Angle360)cached).getValue()) + "</cdr>\n");        
            str += (((!first && output == JSON_OUTPUT)?",\n":"") + "  " + dataFormat(df3.format(((Angle360)cached).getValue()), "cdr", output, NUMERIC_OPTION) + ((output != JSON_OUTPUT)?"\n":""));
            first = false;
          }
          else if (k.equals(NMEADataCache.B2WP))
          {
  //        str += ("  <cdr>" + df3.format(((Angle360)cached).getValue()) + "</cdr>\n");
            str += (((!first && output == JSON_OUTPUT)?",\n":"") + "  " + dataFormat(df3.format(((Angle360)cached).getValue()), "b2wp", output, NUMERIC_OPTION) + ((output != JSON_OUTPUT)?"\n":""));
            first = false;
          }
        }
        else if (cached instanceof Angle180)
        {
          if (k.equals(NMEADataCache.AWA))
          {
  //        str += ("  <awa>" + df3.format(((Angle180)cached).getValue()) + "</awa>\n");
            str += (((!first && output == JSON_OUTPUT)?",\n":"") + "  " + dataFormat(df3.format(((Angle180)cached).getValue()), "awa", output, NUMERIC_OPTION) + ((output != JSON_OUTPUT)?"\n":""));
            first = false;
          }
          else if (k.equals(NMEADataCache.TWA))
          {
  //        str += ("  <twa>" + df3.format(((Angle180)cached).getValue()) + "</twa>\n");
            str += (((!first && output == JSON_OUTPUT)?",\n":"") + "  " + dataFormat(df3.format(((Angle180)cached).getValue()), "twa", output, NUMERIC_OPTION) + ((output != JSON_OUTPUT)?"\n":""));
            first = false;
          }
        }
        else if (cached instanceof Angle180LR)
        {
          if (k.equals(NMEADataCache.LEEWAY))
          {
  //        str += ("  <leeway>" + df3.format(((Angle180LR)cached).getValue()) + "</leeway>\n");
            str += (((!first && output == JSON_OUTPUT)?",\n":"") + "  " + dataFormat(df3.format(((Angle180LR)cached).getValue()), "leeway", output, NUMERIC_OPTION) + ((output != JSON_OUTPUT)?"\n":""));
            first = false;
          }
        }
        else if (cached instanceof Angle180EW)
        {
          if (k.equals(NMEADataCache.DECLINATION))
          {
            double d = ((Angle180EW)cached).getValue();
            if (d != -Double.MAX_VALUE)
            {
  //          str += ("  <D>" + df3.format(d) + "</D>\n");
              str += (((!first && output == JSON_OUTPUT)?",\n":"") + "  " + dataFormat(df3.format(d), "D", output, NUMERIC_OPTION) + ((output != JSON_OUTPUT)?"\n":""));
              first = false;
            }
          }
          if (k.equals(NMEADataCache.DEVIATION))
          {
  //        str += ("  <d>" + df3.format(((Angle180EW)cached).getValue()) + "</d>\n");
            str += (((!first && output == JSON_OUTPUT)?",\n":"") + "  " + dataFormat(df3.format(((Angle180EW)cached).getValue()), "d", output, NUMERIC_OPTION) + ((output != JSON_OUTPUT)?"\n":""));
            first = false;
          }
        }
        else if (cached instanceof GeoPos)
        {
          if (k.equals(NMEADataCache.POSITION))
          {
  //        str += ("  <lat>" + Double.toString(((GeoPos)cached).lat) + "</lat>\n");
            str += (((!first && output == JSON_OUTPUT)?",\n":"") + "  " + dataFormat(Double.toString(((GeoPos)cached).lat), "lat", output, NUMERIC_OPTION) + ((output != JSON_OUTPUT)?"\n":""));
            first = false;
  //        str += ("  <lng>" + Double.toString(((GeoPos)cached).lng) + "</lng>\n");
            str += (((!first && output == JSON_OUTPUT)?",\n":"") + "  " + dataFormat(Double.toString(((GeoPos)cached).lng), "lng", output, NUMERIC_OPTION) + ((output != JSON_OUTPUT)?"\n":""));
  //        try { str += ("  <pos>" + URLEncoder.encode(((GeoPos)cached).toString(), "UTF-8") + "</pos>\n"); }
            try 
            { 
  //          str += ("  <pos>" + ((GeoPos)cached).toString().replaceAll("°","&deg;") + "</pos>\n"); 
              str += (((!first && output == JSON_OUTPUT)?",\n":"") + "  " + dataFormat(((GeoPos)cached).toString().replaceAll("°","&deg;"), "pos", output, CHARACTER_OPTION) + ((output != JSON_OUTPUT)?"\n":""));
            }
            catch (Exception ex) { ex.printStackTrace(); }
          }        
        }
        else if (cached instanceof Depth)
        {
          if (k.equals(NMEADataCache.DBT))
          {
  //        str += ("  <dbt>" + df22.format(((Depth)cached).getValue()) + "</dbt>\n");
            str += (((!first && output == JSON_OUTPUT)?",\n":"") + "  " + dataFormat(df22.format(((Depth)cached).getValue()), "dbt", output, NUMERIC_OPTION) + ((output != JSON_OUTPUT)?"\n":""));
            first = false;
          }
        }
        else if (cached instanceof Temperature)
        {
          if (k.equals(NMEADataCache.WATER_TEMP))
          {
  //        str += ("  <wtemp>" + df22.format(((Temperature)cached).getValue()) + "</wtemp>\n");
            str += (((!first && output == JSON_OUTPUT)?",\n":"") + "  " + dataFormat(df22.format(((Temperature)cached).getValue()), "wtemp", output, NUMERIC_OPTION) + ((output != JSON_OUTPUT)?"\n":""));
            first = false;
          }
          else if (k.equals(NMEADataCache.AIR_TEMP))
          {
            str += (((!first && output == JSON_OUTPUT)?",\n":"") + "  " + dataFormat(df22.format(((Temperature)cached).getValue()), "atemp", output, NUMERIC_OPTION) + ((output != JSON_OUTPUT)?"\n":""));
            first = false;
          }
        }
        else if (cached instanceof Distance)
        {
          if (k.equals(NMEADataCache.LOG))
          {
            str += (((!first && output == JSON_OUTPUT)?",\n":"") + "  " + dataFormat(df41.format(((Distance)cached).getValue()), "log", output, NUMERIC_OPTION) + ((output != JSON_OUTPUT)?"\n":""));
            first = false;
          }          
          else if (k.equals(NMEADataCache.DAILY_LOG))
          {
            str += (((!first && output == JSON_OUTPUT)?",\n":"") + "  " + dataFormat(df41.format(((Distance)cached).getValue()), "day-log", output, NUMERIC_OPTION) + ((output != JSON_OUTPUT)?"\n":""));
            first = false;
          }          
          else if (k.equals(NMEADataCache.XTE))
          {
            str += (((!first && output == JSON_OUTPUT)?",\n":"") + "  " + dataFormat(df41.format(((Distance)cached).getValue()), "xte", output, NUMERIC_OPTION) + ((output != JSON_OUTPUT)?"\n":""));
            first = false;
          }          
          else if (k.equals(NMEADataCache.D2WP))
          {
            str += (((!first && output == JSON_OUTPUT)?",\n":"") + "  " + dataFormat(df41.format(((Distance)cached).getValue()), "d2wp", output, NUMERIC_OPTION) + ((output != JSON_OUTPUT)?"\n":""));
            first = false;
          }          
        }
        else if (cached instanceof Pressure)
        {
          if (k.equals(NMEADataCache.BARO_PRESS))
          {
            str += (((!first && output == JSON_OUTPUT)?",\n":"") + "  " + dataFormat(df41.format(((Pressure)cached).getValue()), "prmsl", output, NUMERIC_OPTION) + ((output != JSON_OUTPUT)?"\n":""));
            first = false;
          }          
        }
        else if (cached instanceof UTCTime)
        {
          if (k.equals(NMEADataCache.GPS_TIME))
          {
  //        str += ("  <gps-time>" + Long.toString(((UTCTime)cached).getValue().getTime()) + "</gps-time>\n");
            str += (((!first && output == JSON_OUTPUT)?",\n":"") + "  " + dataFormat(Long.toString(((UTCTime)cached).getValue().getTime()), "gps-time", output, NUMERIC_OPTION) + ((output != JSON_OUTPUT)?"\n":""));
            first = false;
  //        str += ("  <gps-time-fmt>" + TIME_FORMAT.format(((UTCTime)cached).getValue()) + "</gps-time-fmt>\n");
            str += (((!first && output == JSON_OUTPUT)?",\n":"") + "  " + dataFormat(TIME_FORMAT.format(((UTCTime)cached).getValue()), "gps-time-fmt", output, CHARACTER_OPTION) + ((output != JSON_OUTPUT)?"\n":""));
          }
        }
        else if (cached instanceof UTCDate)
        {
          if (k.equals(NMEADataCache.GPS_DATE_TIME))
          {
  //        str += ("  <gps-date-time>" + Long.toString(((UTCDate)cached).getValue().getTime()) + "</gps-date-time>\n");
            str += (((!first && output == JSON_OUTPUT)?",\n":"") + "  " + dataFormat(Long.toString(((UTCDate)cached).getValue().getTime()), "gps-date-time", output, NUMERIC_OPTION) + ((output != JSON_OUTPUT)?"\n":""));
            first = false;
  //        str += ("  <gps-date-time-fmt>" + DATE_FORMAT.format(((UTCDate)cached).getValue()) + "</gps-date-time-fmt>\n");
            str += (((!first && output == JSON_OUTPUT)?",\n":"") + "  " + dataFormat(DATE_FORMAT.format(((UTCDate)cached).getValue()), "gps-date-time-fmt", output, CHARACTER_OPTION) + ((output != JSON_OUTPUT)?"\n":""));
          }
        }
        else if (cached instanceof SolarDate)
        {
          if (k.equals(NMEADataCache.GPS_SOLAR_TIME))
          {
  //        str += ("  <gps-date-time>" + Long.toString(((UTCDate)cached).getValue().getTime()) + "</gps-date-time>\n");
            str += (((!first && output == JSON_OUTPUT)?",\n":"") + "  " + dataFormat(Long.toString(((SolarDate)cached).getValue().getTime()), "gps-solar-date", output, NUMERIC_OPTION) + ((output != JSON_OUTPUT)?"\n":""));
            first = false;
          }
        }
        else if (cached instanceof String)
        {
          if (k.equals(NMEADataCache.TO_WP))
          {
  //        str += ("  <gps-date-time>" + Long.toString(((UTCDate)cached).getValue().getTime()) + "</gps-date-time>\n");
            str += (((!first && output == JSON_OUTPUT)?",\n":"") + "  " + dataFormat((String)cached, "to-wp", output, CHARACTER_OPTION) + ((output != JSON_OUTPUT)?"\n":""));
            first = false;
          }
        }
        else if (true) // Extra data
        {
          try 
          { 
  //        str += ("  <obj name='" + URLEncoder.encode(k, "UTF-8") + "'><![CDATA[" + URLEncoder.encode(cached.toString(), "UTF-8") + "]]></obj>\n"); 
  //        str += (((!first && output == JSON_OUTPUT)?",\n":"") + "  " + dataFormat(URLEncoder.encode(cached.toString(), "UTF-8"), URLEncoder.encode(k, "UTF-8"), output, CHARACTER_OPTION) + ((output != JSON_OUTPUT)?"\n":""));
            str += (((!first && output == JSON_OUTPUT)?",\n":"") + "  " + dataFormat(cached.toString(), k.replace(' ', '_'), output, CHARACTER_OPTION) + ((output != JSON_OUTPUT)?"\n":""));
            System.out.println(">>> EXTRA >>> " + k + " is a " + cached.getClass().getName() + ":" + cached);
            first = false;
          } 
          catch (Exception ex) { ex.printStackTrace(); }
        }
      }
      // VMG & Perf
      double vmg = 0d;
      try
      {
        double sog = (((Speed)cache.get(NMEADataCache.SOG)).getValue());
        double cog = ((Angle360)cache.get(NMEADataCache.COG)).getValue();
        double twd = (((TrueWindDirection)cache.get(NMEADataCache.TWD)).getValue());
        double twa = twd - cog;
        if (sog > 0) // Try with GPS Data first
          vmg = sog * Math.cos(Math.toRadians(twa));
        else
        {
          twa = ((Angle180)cache.get(NMEADataCache.TWA)).getValue();
          double bsp = ((Speed)cache.get(NMEADataCache.BSP)).getValue();
          if (bsp > 0)
            vmg = bsp * Math.cos(Math.toRadians(twa));
        }
        str += (((!first && output == JSON_OUTPUT)?",\n":"") + "  " + dataFormat(df22.format(vmg), "vmg-wind", output, NUMERIC_OPTION) + ((output != JSON_OUTPUT)?"\n":""));
        first = false;

        if (cache.get(NMEADataCache.TO_WP) != null && cache.get(NMEADataCache.TO_WP).toString().trim().length() > 0)
        {
          double b2wp = ((Angle360)cache.get(NMEADataCache.B2WP)).getValue();
          sog = (((Speed)cache.get(NMEADataCache.SOG)).getValue());
          cog = ((Angle360)cache.get(NMEADataCache.COG)).getValue();
          if (sog > 0)
          {
            double angle = b2wp - cog;
            vmg = sog * Math.cos(Math.toRadians(angle));
          }
          else
          {
            double angle = b2wp - ((Angle360)cache.get(NMEADataCache.HDG_TRUE)).getValue();
            double bsp = ((Speed)cache.get(NMEADataCache.BSP)).getValue();
            vmg = bsp * Math.cos(Math.toRadians(angle));
          }
          str += (((!first && output == JSON_OUTPUT)?",\n":"") + "  " + dataFormat(df22.format(vmg), "vmg-wp", output, NUMERIC_OPTION) + ((output != JSON_OUTPUT)?"\n":""));
          first = false;
        }
      }
      catch (Exception ex)
      {
        if (verbose)
          System.err.println("Perf & VMG:" + ex.getMessage());
      }
      if (cache.get(NMEADataCache.PERF) != null && ((Double)cache.get(NMEADataCache.PERF)).doubleValue() > -1d)
      {
        double perf = ((Double)cache.get(NMEADataCache.PERF)).doubleValue();
        str += (((!first && output == JSON_OUTPUT)?",\n":"") + "  " + dataFormat(df22.format(perf), "perf", output, NUMERIC_OPTION) + ((output != JSON_OUTPUT)?"\n":""));
        first = false;
      }
      
      try
      {
        // Calibration Parameters
        double bspFactor   = ((Double)cache.get(NMEADataCache.BSP_FACTOR)).doubleValue();
        double awsFactor   = ((Double)cache.get(NMEADataCache.AWS_FACTOR)).doubleValue();
        double awaOffset   = ((Double)cache.get(NMEADataCache.AWA_OFFSET)).doubleValue();
        double hdgOffset   = ((Double)cache.get(NMEADataCache.HDG_OFFSET)).doubleValue();
        double maxLeeway   = ((Double)cache.get(NMEADataCache.MAX_LEEWAY)).doubleValue();
  
        String devFile     = ((String)cache.get(NMEADataCache.DEVIATION_FILE)).toString();
        double defaultDecl = ((Angle180EW)cache.get(NMEADataCache.DEFAULT_DECLINATION)).getDoubleValue();
        int damping        = ((Integer)cache.get(NMEADataCache.DAMPING)).intValue();
        
        String polarFile   = ((String)cache.get(NMEADataCache.POLAR_FILE_NAME)).toString();
        double polarFactor = ((Double)cache.get(NMEADataCache.POLAR_FACTOR)).doubleValue();

        str += (((!first && output == JSON_OUTPUT)?",\n":"") + "  " + dataFormat(Double.toString(bspFactor), "bsp-factor", output, NUMERIC_OPTION) + ((output != JSON_OUTPUT)?"\n":""));
        first = false;
        str += (((!first && output == JSON_OUTPUT)?",\n":"") + "  " + dataFormat(Double.toString(awsFactor), "aws-factor", output, NUMERIC_OPTION) + ((output != JSON_OUTPUT)?"\n":""));
        str += (((!first && output == JSON_OUTPUT)?",\n":"") + "  " + dataFormat(Double.toString(awaOffset), "awa-offset", output, NUMERIC_OPTION) + ((output != JSON_OUTPUT)?"\n":""));
        str += (((!first && output == JSON_OUTPUT)?",\n":"") + "  " + dataFormat(Double.toString(hdgOffset), "hdg-offset", output, NUMERIC_OPTION) + ((output != JSON_OUTPUT)?"\n":""));
        str += (((!first && output == JSON_OUTPUT)?",\n":"") + "  " + dataFormat(Double.toString(maxLeeway), "max-leeway", output, NUMERIC_OPTION) + ((output != JSON_OUTPUT)?"\n":""));
        str += (((!first && output == JSON_OUTPUT)?",\n":"") + "  " + dataFormat(devFile, "dev-file", output, CHARACTER_OPTION) + ((output != JSON_OUTPUT)?"\n":""));
        str += (((!first && output == JSON_OUTPUT)?",\n":"") + "  " + dataFormat(Double.toString(defaultDecl), "default-decl", output, NUMERIC_OPTION) + ((output != JSON_OUTPUT)?"\n":""));
        str += (((!first && output == JSON_OUTPUT)?",\n":"") + "  " + dataFormat(Integer.toString(damping), "damping", output, NUMERIC_OPTION) + ((output != JSON_OUTPUT)?"\n":""));
        str += (((!first && output == JSON_OUTPUT)?",\n":"") + "  " + dataFormat(polarFile, "polar-file", output, CHARACTER_OPTION) + ((output != JSON_OUTPUT)?"\n":""));
        str += (((!first && output == JSON_OUTPUT)?",\n":"") + "  " + dataFormat(Double.toString(polarFactor), "polar-speed-factor", output, NUMERIC_OPTION) + ((output != JSON_OUTPUT)?"\n":""));
      }
      catch (Exception ex)
      {
        ex.printStackTrace();
      }
      try
      {
        // Running since...
        Object obj = cache.get(NMEADataCache.TIME_RUNNING);
        if (obj != null)
          str += (((!first && output == JSON_OUTPUT)?",\n":"") + "  " + dataFormat((((Long)cache.get(NMEADataCache.TIME_RUNNING)).toString()), "running", output, NUMERIC_OPTION) + ((output != JSON_OUTPUT)?"\n":""));
      }
      catch (Exception ex) 
      {
        System.err.println("NMEADataCache.TIME_RUNNING:");
        ex.printStackTrace();
      }
  
      if (output == XML_OUTPUT)
      {
        str += ("</data>\n");
      }  
      else if (output == JSON_OUTPUT)
      {
        str += "\n}\n";
      }
    }
    if (verbose)
      System.out.println(str);
    return str;
  }

  private final static int NUMERIC_OPTION = 0;
  private final static int CHARACTER_OPTION = 1;
  
  private static String dataFormat(String data, String dataName, int opt, int dataOption)
  {
    String str = "";
    if (opt == XML_OUTPUT)
      str = "<" + dataName + ">" + data + "</" + dataName + ">";
    else if (opt == JSON_OUTPUT)
    {
      if (dataOption == NUMERIC_OPTION) // Remove leading '0'
      {
        
      }
      str = "\"" + dataName + "\":" + (dataOption == CHARACTER_OPTION?"\"":"") + data + (dataOption == CHARACTER_OPTION?"\"":"");
    }
    
    return str;
  }
  
  /**
   * @deprecated
   */
  private String generateOldContent()
  {
    String str = ""; // "Content-Type: text/xml\r\n\r\n";
    if (output == XML_OUTPUT)
    {
      str += ("<?xml version='1.0' encoding='UTF-8'?>\n");
      str += ("<data>\n");
    }  
    Iterator keys = null;
    if (data != null)
      keys = getSentenceList().iterator();
    /* "HDM", "GLL", "XTE", "MWV", "VHW" */    
    while (keys != null && keys.hasNext())
    {
      String key = (String)keys.next();
      String nmea = getSentence(key);
      
   // System.out.println("HTTP: key=[" + key + "], value=[" + nmea + "]");
      
      if (key.substring(2).equals("GLL") || key.equals("GLL"))
      {
        GeoPos gll = (GeoPos)StringParsers.parseGLL(nmea)[StringParsers.GP_in_GLL];
        if (gll != null)
        {
          if (output == XML_OUTPUT)
          {
            str += ("<lat>" + Double.toString(gll.lat) + "</lat>\n");
            str += ("<lng>" + Double.toString(gll.lng) + "</lng>\n");
          }
          else
          {
            str += ("LAT=" + Double.toString(gll.lat) + "\n");
            str += ("LNG=" + Double.toString(gll.lng) + "\n");
          }
        }
      }
      else if (key.substring(2).equals("HDM") || key.equals("HDM"))
      {
        int heading = StringParsers.parseHDM(nmea);
        if (output == XML_OUTPUT)
          str += ("<hdm>" + Integer.toString(heading) + "</hdm>\n");
        else
          str += ("HDM=" + Integer.toString(heading) + "\n");
      }
      else if (key.substring(2).equals("RMC") || key.equals("RMC"))
      {
        RMC rmc = null;
        boolean ok = true;
        try { rmc = StringParsers.parseRMC(nmea); }
        catch (RuntimeException rte)
        { ok = false; }
        if (ok && rmc != null)
        {
          if (output == XML_OUTPUT)
          {
            str += ("<lat>" + Double.toString(rmc.getGp().lat) + "</lat>\n");
            str += ("<lng>" + Double.toString(rmc.getGp().lng) + "</lng>\n");
            str += ("<cog>" + Double.toString(rmc.getCog()) + "</cog>\n");
            str += ("<sog>" + Double.toString(rmc.getSog()) + "</sog>\n");
          }
          else
          {
            str += ("LAT=" + Double.toString(rmc.getGp().lat) + "\n");
            str += ("LNG=" + Double.toString(rmc.getGp().lng) + "\n");
            str += ("COG=" + Double.toString(rmc.getCog()) + "\n");
            str += ("SOG=" + Double.toString(rmc.getSog()) + "\n");
          }
        }
      }
      else if (key.substring(2).equals("HDG") || key.equals("HDG"))
      {
        int heading = (int)StringParsers.parseHDG(nmea)[StringParsers.HDG_in_HDG];
        if (output == XML_OUTPUT)
          str += ("<hdg>" + Integer.toString(heading) + "</hdg>\n");
        else
          str += ("HDG=" + Integer.toString(heading) + "\n");
      }
      else if (key.substring(2).equals("MWV") || key.equals("MWV"))
      {
        Wind wind = StringParsers.parseMWV(nmea);
        if (wind != null && wind instanceof ApparentWind)
        {
          if (output == XML_OUTPUT)
          {
            str += ("<aws>" + Double.toString(wind.speed) + "</aws>\n");
            str += ("<awa>" + Integer.toString(wind.angle) + "</awa>\n");
          }
          else
          {
            str += ("AWS=" + Double.toString(wind.speed) + "\n");
            str += ("AWA=" + Integer.toString(wind.angle) + "\n");
          }
        }
      }
      else if (key.substring(2).equals("VHW") || key.equals("VHW"))
      {
        double speed = StringParsers.parseVHW(nmea)[StringParsers.BSP_in_VHW];
        if (output == XML_OUTPUT)
          str += ("<bsp>" + Double.toString(speed) + "</bsp>\n");
        else
          str += ("BSP=" + Double.toString(speed) + "\n");
      }
      else if (key.substring(2).equals("DBT") || key.equals("DBT"))
      {
        String s = StringParsers.parseDBTinMetersToString(nmea);
        if (output == XML_OUTPUT)
          str += ("<dbt unit='m'>" + s + "</dbt>\n");
        else
          str += ("DBT=" + s + "\n");
      }
      else if (key.substring(2).equals("VTG") || key.equals("VTG"))
      {
        String s = StringParsers.parseVTGtoString(nmea);
        if (output == XML_OUTPUT)
          str += ("<vtg>" + s + "</vtg>\n");
        else
          str += ("VTG=" + s + "\n");
      }
      else if (key.substring(2).equals("VWR") || key.equals("VWR"))
      {
        String s = StringParsers.parseVWRtoString(nmea);
        if (output == XML_OUTPUT)
          str += ("<vwr>" + s + "</vwr>\n");
        else
          str += ("VWR=" + s + "\n");
      }
      else if (key.substring(2).equals("MWV") || key.equals("MWV"))
      {
        String s = StringParsers.parseMWVtoString(nmea);
        if (output == XML_OUTPUT)
          str += ("<mwv>" + s + "</mwv>\n");
        else
          str += ("MWV=" + s + "\n");
      }
      else if (key.substring(2).equals("GGA") || key.equals("GGA"))
      {
        try
        {
          List<Object> al = StringParsers.parseGGA(nmea);
          UTC utc = (UTC)al.get(0);
          GeoPos pos = (GeoPos)al.get(1);
          Integer nbs = (Integer)al.get(2);
          if (output == XML_OUTPUT)
          {
            str += ("<lat>" + Double.toString(pos.lat) + "</lat>\n");
            str += ("<lng>" + Double.toString(pos.lng) + "</lng>\n");
            str += ("<utc h='" + utc.getH() + "' m='" + utc.getM() + "' s='" + utc.getS() + "'/>\n");
          }
          else
          {
            str += ("UTC:" + utc.toString() + "\n");
            str += ("Pos:" + pos.toString() + "\n");
            str += (nbs.intValue() + " Satellite(s) in use\n");
          }
        }
        catch (Exception ex) {}
      }
      else if (key.substring(2).equals("GSV") || key.equals("GSV"))
      {
        Map<Integer, SVData> map = null;
        try 
        { 
          map = StringParsers.parseGSV(nmea);        
          if (output == XML_OUTPUT)
          {
            str += "<satellites nb='" + Integer.toString(map.size()) + "'>\n";
            for (Integer sn : map.keySet())
            {
              SVData svd = map.get(sn);
              str += ("  <sv id='" + svd.getSvID() + "' elev='" + svd.getElevation() + "' z='" + svd.getAzimuth() + "' snr='" + svd.getSnr() + "'/>\n");
            }
            str += "</satellites>\n";          
          }
          else
          {
            str += (map.size() + " Satellites in view.\n");
            for (Integer sn : map.keySet())
            {
              SVData svd = map.get(sn);
              str += ("Satellite #" + svd.getSvID() + " Elev:" + svd.getElevation() + ", Z:" + svd.getAzimuth() + ", SNR:" + svd.getSnr() + "db\n");
            }
          }
        }
        catch (Exception ex) { }
      }
      else
      {
        if (output == XML_OUTPUT)
          str += ("<unmanaged>" + nmea + "</unmanaged>\n");
        else
          str += ("UnManaged:" + nmea + "\n");
      }
    }    
    if (output == XML_OUTPUT)
    {
      str += ("</data>\n");
    }  
    return str;
  }
  
  //  For dev tests
  public static void main(String[] args) throws Exception
  {
  //System.setProperty("http.port", "9999");
    new HTTPServer(new String[] { "-verbose=y", "-fmt=xml" }, null, null);
    Thread t = new Thread("HTTPWaiter")
      {
        public void run()
        {
          synchronized(this)
          {
            try {  this.wait(); } catch (Exception ex) {}
          }
        }
      };
    t.start();
  }
}