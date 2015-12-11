/*
 * @author Olivier Le Diouris
 */
var initAjax = function() 
{
  var interval = setInterval(function() { pingNMEAConsole(); }, 1000);
};

/**
 * Sample data:
<?xml version='1.0' encoding='UTF-8'?>
<?xml-stylesheet href="nmea-xml-html.xsl" type="text/xsl"?>
<!DOCTYPE data [
 <!ENTITY deg     "&#176;">
]>
<data>
  <wtemp>26.50</wtemp>
  <gps-time>1290377286000</gps-time>
  <gps-time-fmt>14:08:06 UTC</gps-time-fmt>
  <d2wp>561.7</d2wp>
  <cog>223</cog>
  <leeway>0</leeway>
  <csp>0.79</csp>
  <bsp>6.83</bsp>
  <lat>-9.10875</lat>
  <lng>-140.20975</lng>
  <pos>S  09&deg;06.53' / W 140&deg;12.59'</pos>
  <b2wp>230</b2wp>
  <xte>3.0</xte>
  <gps-date-time>1290377286000</gps-date-time>
  <gps-date-time-fmt>21 Nov 2010 14:08:06 UTC</gps-date-time-fmt>
  <D>10</D>
  <aws>14.60</aws>
  <cdr>140</cdr>
  <to-wp>RANGI   </to-wp>
  <tws>18.96</tws>
  <dbt>1.60</dbt>
  <log>3013.0</log>
  <awa>-126</awa>
  <hdg>229</hdg>
  <cmg>227</cmg>
  <twd>85</twd>
  <prmsl>0.0</prmsl>
  <d>-1</d>
  <atemp>0.00</atemp>
  <twa>-143</twa>
  <day-log>12.3</day-log>
  <sog>6.91</sog>
  <gps-solar-date>1290343635660</gps-solar-date>
  <vmg-wind>-5.11</vmg-wind>
  <vmg-wp>6.85</vmg-wp>
  <perf>1.03</perf>
  <bsp-factor>1.0</bsp-factor>
  <aws-factor>1.0</aws-factor>
  <awa-offset>0.0</awa-offset>
  <hdg-offset>0.0</hdg-offset>
  <max-leeway>15.0</max-leeway>
  <dev-file>D:\OlivSoft\all-scripts\dp_2011_04_15.csv</dev-file>
  <default-decl>15.0</default-decl>
  <damping>30</damping>
  <polar-file>D:\OlivSoft\all-scripts\polars\CheoyLee42.polar-coeff</polar-file>
  <polar-speed-factor>0.8</polar-speed-factor>
</data>
*/
var pingNMEAConsole = function()
{
  try
  {
    var xhr = new XMLHttpRequest();
    xhr.open("GET", "/", false);
    xhr.send();
    doc = xhr.responseXML; 
    var errMess = "";
    
    var showWT = true, showAT = false, showGDT = false, showPRMSL = false, showHUM = false, showVOLT = false;
    try { showWT = ("true" === doc.getElementsByTagName("Display_Web_Water_Temp")[0].childNodes[0].nodeValue); } catch (err) {}
    try { showAT = ("true" === doc.getElementsByTagName("Display_Web_Air_Temp")[0].childNodes[0].nodeValue); } catch (err) {}
    try { showGDT = ("true" === doc.getElementsByTagName("Display_Web_GPSDateTime")[0].childNodes[0].nodeValue); } catch (err) {}
    try { showPRMSL = ("true" === doc.getElementsByTagName("Display_Web_PRMSL")[0].childNodes[0].nodeValue); } catch (err) {}
    try { showHUM = ("true" === doc.getElementsByTagName("Display_Web_HUM")[0].childNodes[0].nodeValue); } catch (err) {}
    try { showVOLT = ("true" === doc.getElementsByTagName("Display_Web_Volt")[0].childNodes[0].nodeValue); } catch (err) {}

    events.publish('show-hide-wt',    showWT);
    events.publish('show-hide-at',    showAT);
    events.publish('show-hide-gdt',   showGDT);
    events.publish('show-hide-prmsl', showPRMSL);
    events.publish('show-hide-hum',   showHUM);
    events.publish('show-hide-volt',  showVOLT);
    
    try
    {
      var latitude  = parseFloat(doc.getElementsByTagName("lat")[0].childNodes[0].nodeValue);
//    console.log("latitude:" + latitude)
      var longitude = parseFloat(doc.getElementsByTagName("lng")[0].childNodes[0].nodeValue);
//    console.log("Pt:" + latitude + ", " + longitude);
      events.publish('pos', { 'lat': latitude,
                              'lng': longitude });
    }
    catch (err)
    {
      errMess += ((errMess.length > 0?"\n":"") + "Problem with position...");
    }
    // Displays
    try
    {
      var bsp = parseFloat(doc.getElementsByTagName("bsp")[0].childNodes[0].nodeValue);
      events.publish('bsp', bsp);
    }
    catch (err)
    {
      errMess += ((errMess.length > 0?"\n":"") + "Problem with boat speed...");
    }
    try
    {
      var log = parseFloat(doc.getElementsByTagName("log")[0].childNodes[0].nodeValue);
      events.publish('log', log);
    }
    catch (err)
    {
      errMess += ((errMess.length > 0?"\n":"") + "Problem with log...:" + err);
    }
    try
    {
      var gpsDate = parseFloat(doc.getElementsByTagName("gps-date-time")[0].childNodes[0].nodeValue);
      events.publish('gps-time', gpsDate);
    }
    catch (err)
    {
      errMess += ((errMess.length > 0?"\n":"") + "Problem with GPS Date...:" + err);
    }    

    try
    {
      var hdg = parseFloat(doc.getElementsByTagName("hdg")[0].childNodes[0].nodeValue) % 360;
      events.publish('hdg', hdg);
    }
    catch (err)
    {
      errMess += ((errMess.length > 0?"\n":"") + "Problem with heading...");
    }
    try
    {
      var twd = parseFloat(doc.getElementsByTagName("twd")[0].childNodes[0].nodeValue) % 360;
      events.publish('twd', twd);
    }
    catch (err)
    {
      errMess += ((errMess.length > 0?"\n":"") + "Problem with TWD...");
    }
    try
    {
      var twa = parseFloat(doc.getElementsByTagName("twa")[0].childNodes[0].nodeValue);
      events.publish('twa', twa);
    }
    catch (err)
    {
      errMess += ((errMess.length > 0?"\n":"") + "Problem with TWA...");
    }
    try
    {
      var tws = parseFloat(doc.getElementsByTagName("tws")[0].childNodes[0].nodeValue);
      events.publish('tws', tws);
    }
    catch (err)
    {
      errMess += ((errMess.length > 0?"\n":"") + "Problem with TWS...");
    }

    if (showWT)
    {
      try
      {
        var waterTemp = parseFloat(doc.getElementsByTagName("wtemp")[0].childNodes[0].nodeValue);
        events.publish('wt', waterTemp);
      }
      catch (err)
      {
        errMess += ((errMess.length > 0?"\n":"") + "Problem with water temperature...");
      }
    }
    try
    {
      var airTemp = parseFloat(doc.getElementsByTagName("atemp")[0].childNodes[0].nodeValue);
      events.publish('at', airTemp);
    }
    catch (err)
    {
      errMess += ((errMess.length > 0?"\n":"") + "Problem with air temperature...");
    }
    // Battery_Voltage, Relative_Humidity, Barometric_Pressure
    try
    {
      var voltage = parseFloat(doc.getElementsByTagName("Battery_Voltage")[0].childNodes[0].nodeValue);
      if (voltage > 0) {
        events.publish('volt', voltage);
      }
    }
    catch (err)
    {
      errMess += ((errMess.length > 0?"\n":"") + "Problem with air Battery_Voltage...");
    }
    try
    {
      var baro = parseFloat(doc.getElementsByTagName("prmsl")[0].childNodes[0].nodeValue);
      if (baro != 0) {
        events.publish('prmsl', baro);
      }
    }
    catch (err)
    {
      errMess += ((errMess.length > 0?"\n":"") + "Problem with air PRMSL...");
    }
    try
    {
      var hum = parseFloat(doc.getElementsByTagName("Relative_Humidity")[0].childNodes[0].nodeValue);
      if (hum > 0) {
        events.publish('hum', hum);
      }
    }
    catch (err)
    {
      errMess += ((errMess.length > 0?"\n":"") + "Problem with air Relative_Humidity...");
    }
    try
    {
      var aws = parseFloat(doc.getElementsByTagName("aws")[0].childNodes[0].nodeValue);
      events.publish('aws', aws);
    }
    catch (err)
    {
      errMess += ((errMess.length > 0?"\n":"") + "Problem with AWS...");
    }    
    try
    {
      var awa = parseFloat(doc.getElementsByTagName("awa")[0].childNodes[0].nodeValue);
      events.publish('awa', awa);
    }
    catch (err)
    {
      errMess += ((errMess.length > 0?"\n":"") + "Problem with AWA...");
    }    
    try
    {
      var cdr = parseFloat(doc.getElementsByTagName("cdr")[0].childNodes[0].nodeValue);
      events.publish('cdr', cdr);
    }
    catch (err)
    {
      errMess += ((errMess.length > 0?"\n":"") + "Problem with CDR...");
    }
      
    try
    {
      var cog = parseFloat(doc.getElementsByTagName("cog")[0].childNodes[0].nodeValue);
      events.publish('cog', cog);
    }
    catch (err)
    {
      errMess += ((errMess.length > 0?"\n":"") + "Problem with COG...");
    }
    try
    {
      var cmg = parseFloat(doc.getElementsByTagName("cmg")[0].childNodes[0].nodeValue);
      events.publish('cmg', cmg);
    }
    catch (err)
    {
      errMess += ((errMess.length > 0?"\n":"") + "Problem with CMG...");
    }      
    try
    {
      var leeway = parseFloat(doc.getElementsByTagName("leeway")[0].childNodes[0].nodeValue);
      events.publish('leeway', leeway);
    }
    catch (err)
    {
      errMess += ((errMess.length > 0?"\n":"") + "Problem with Leeway...");
    }      
    try
    {
      var csp = parseFloat(doc.getElementsByTagName("csp")[0].childNodes[0].nodeValue);
      events.publish('csp', csp);
    }
    catch (err)
    {
      errMess += ((errMess.length > 0?"\n":"") + "Problem with CSP...");
    }    
    try
    {
      var sog = parseFloat(doc.getElementsByTagName("sog")[0].childNodes[0].nodeValue);
      events.publish('sog', sog);
    }
    catch (err)
    {
      errMess += ((errMess.length > 0?"\n":"") + "Problem with SOG...");
    }
    // to-wp, vmg-wind, vmg-wp, b2wp
    try
    {
      var to_wp = doc.getElementsByTagName("to-wp")[0].childNodes[0].nodeValue;
      var b2wp = parseFloat(doc.getElementsByTagName("b2wp")[0].childNodes[0].nodeValue);
      events.publish('wp', { 'to_wp': to_wp,
                             'b2wp': b2wp });
    }
    catch (err)
    {
    }
    
    try
    {
      events.publish('vmg', { 'onwind': parseFloat(doc.getElementsByTagName("vmg-wind")[0].childNodes[0].nodeValue),
                              'onwp':   parseFloat(doc.getElementsByTagName("vmg-wp")[0].childNodes[0].nodeValue) });

    }
    catch (err)
    {
      errMess += ((errMess.length > 0?"\n":"") + "Problem with VMG...");
    }
    
    // perf
    try
    {
      var perf = parseFloat(doc.getElementsByTagName("perf")[0].childNodes[0].nodeValue);
      perf *= 100;
      events.publish('perf', perf);
    }
    catch (err)
    {
      errMess += ((errMess.length > 0?"\n":"") + "Problem with Perf...");
    }
    
    if (errMess !== undefined)
      displayErr(errMess);
  }
  catch (err)
  {
    displayErr(err);
  }
};
