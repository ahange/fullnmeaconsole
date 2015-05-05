/*
 * @author Olivier Le Diouris
 */
var displayBSP, displayLog, displayPRF, displayTWD, displayTWS, thermometer, athermometer, displayHDG, rose, 
    displayBaro, displayHum, displayVolt, displayDate, displayTime, displayOverview,
    jumboBSP, jumboHDG, jumboTWD, jumboLWY, jumboAWA, jumboTWA, jumboAWS, jumboTWS, jumboCOG, jumboCDR, jumboSOG, jumboCSP, jumboVMG,
    displayAW, displayCurrent, 
    twdEvolution, twsEvolution;
    
var jumboList = [];

var editing = false;

// TODO from Config file/request
var displayOptions = {
  displayWT: true,
  displayAT: true,
  displayGDT: true,
  displayPRMSL: true,
  displayHUM: true,
  displayVOLT: true
};

var init = function() 
{
  displayBSP      = new AnalogDisplay('bspCanvas', 100,   15,  5,  1);
  displayLog      = new NumericDisplay('logCanvas', 60, 5);
  displayPRF      = new AnalogDisplay('prfCanvas', 100,   200,  25,  5, false);
  displayPRF.setNbDec(1);
  displayHDG      = new Direction('hdgCanvas', 100, 45, 5, true);
  displayTWD      = new Direction('twdCanvas', 100, 45, 5, true);
//displayTWD      = new Direction('twdCanvas', 100, 1060,  10,  1, false, 60, 960);
  displayTWS      = new AnalogDisplay('twsCanvas', 100,   50,  10,  1, true, 40);
  thermometer     = new Thermometer('tmpCanvas', 200);
  athermometer    = new Thermometer('atmpCanvas', 200);
  rose            = new CompassRose('roseCanvas', 400, 50);
  displayDate     = new DateDisplay('dateCanvas', 60);
  displayTime     = new TimeDisplay('timeCanvas', 60);
  displayBaro     = new AnalogDisplay('baroCanvas', 100, 1040,  10,  1, true, 40, 980);
  displayHum      = new AnalogDisplay('humCanvas',  100,  100,  10,  1, true, 40);
  displayVolt     = new AnalogDisplay('voltCanvas', 100,   16,   1,  0.25, true, 40);
  
  displayOverview = new BoatOverview('overviewCanvas');
  
  jumboBSP        = new JumboDisplay('jumboBSPCanvas', 'BSP', 120, 60, "0.00");
  jumboHDG        = new JumboDisplay('jumboHDGCanvas', 'HDG', 120, 60, "000");
  jumboTWD        = new JumboDisplay('jumboTWDCanvas', 'TWD', 120, 60, "000", 'cyan');
  jumboLWY        = new JumboDisplay('jumboLWYCanvas', 'LWY', 120, 60, "000", 'red');
  jumboAWA        = new JumboDisplay('jumboAWACanvas', 'AWA', 120, 60, "000");
  jumboTWA        = new JumboDisplay('jumboTWACanvas', 'TWA', 120, 60, "000", 'cyan');
  jumboAWS        = new JumboDisplay('jumboAWSCanvas', 'AWS', 120, 60, "00.0");
  jumboTWS        = new JumboDisplay('jumboTWSCanvas', 'TWS', 120, 60, "00.0", 'cyan');
  jumboCOG        = new JumboDisplay('jumboCOGCanvas', 'COG', 120, 60, "000");
  jumboCDR        = new JumboDisplay('jumboCDRCanvas', 'CDR', 120, 60, "000", 'cyan');
  jumboSOG        = new JumboDisplay('jumboSOGCanvas', 'SOG', 120, 60, "0.00");
  jumboCSP        = new JumboDisplay('jumboCSPCanvas', 'CSP', 120, 60, "00.0", 'cyan');
  jumboVMG        = new JumboDisplay('jumboVMGCanvas', 'VMG', 120, 60, "0.00", 'yellow');
  
  jumboList = [jumboBSP, jumboHDG, jumboTWD, jumboLWY, jumboAWA, jumboTWA, jumboAWS, jumboTWS, jumboCOG, jumboCDR, jumboSOG, jumboCSP, jumboVMG];
  
  displayAW       = new AWDisplay('awDisplayCanvas', 80, 45, 5);
  displayCurrent  = new CurrentDisplay('currentDisplayCanvas', 80, 45, 5);
  twdEvolution    = new TWDEvolution('twdEvolutionCanvas');
  twsEvolution    = new TWSEvolution('twsEvolutionCanvas');
  
  var interval = setInterval(function() { pingNMEAConsole(); }, 1000);
};

var changeBorder = function(b) 
{
  displayBSP.setBorder(b);
  displayPRF.setBorder(b);
  displayHDG.setBorder(b);
  displayTWD.setBorder(b);
  displayTWS.setBorder(b);
  displayBaro.setBorder(b);
  displayHum.setBorder(b);
  displayVolt.setBorder(b);
  displayAW.setBorder(b);
  displayCurrent.setBorder(b);
};

var TOTAL_WIDTH = 1200;

var resizeDisplays = function(width)
{
  if (displayBSP !== undefined && displayTWS !== undefined) // TODO Other displays
  {
    displayBSP.setDisplaySize(100 * (Math.min(width, TOTAL_WIDTH) / TOTAL_WIDTH)); 
    displayPRF.setDisplaySize(100 * (Math.min(width, TOTAL_WIDTH) / TOTAL_WIDTH)); 
    displayTWS.setDisplaySize(100 * (Math.min(width, TOTAL_WIDTH) / TOTAL_WIDTH)); 
    displayHDG.setDisplaySize(100 * (Math.min(width, TOTAL_WIDTH) / TOTAL_WIDTH)); 
    displayTWD.setDisplaySize(100 * (Math.min(width, TOTAL_WIDTH) / TOTAL_WIDTH)); 
    thermometer.setDisplaySize(200 * (Math.min(width, TOTAL_WIDTH) / TOTAL_WIDTH)); 
    athermometer.setDisplaySize(200 * (Math.min(width, TOTAL_WIDTH) / TOTAL_WIDTH)); 
    rose.setDisplaySize(400 * (Math.min(width, TOTAL_WIDTH) / TOTAL_WIDTH)); 
    displayBaro.setDisplaySize(100 * (Math.min(width, TOTAL_WIDTH) / TOTAL_WIDTH)); 
    displayHum.setDisplaySize(100 * (Math.min(width, TOTAL_WIDTH) / TOTAL_WIDTH)); 
    displayVolt.setDisplaySize(100 * (Math.min(width, TOTAL_WIDTH) / TOTAL_WIDTH)); 
    displayOverview.drawGraph();
    twdEvolution.drawGraph();
    twsEvolution.drawGraph();

    var jumboFactor = width / TOTAL_WIDTH;
    for (var i=0; i<jumboList.length; i++)
    {
      if (jumboList[i] !== undefined)
        jumboList[i].setDisplaySize(120 * jumboFactor, 60 * jumboFactor);
    }
  }
};
  
var reloadMap = function() 
{
  var start = new Date().getTime();
  var iframe = document.getElementById("map-frame");
  iframe.onload = function() 
  {
    var end = new Date().getTime();
    var time = end - start;
    document.getElementById("elapsed").innerHTML = "Reload (iframe & map) took <b>" + time + "</b> ms.";
  };
  iframe.contentWindow.location.reload();
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
    
    try
    {
      var latitude  = parseFloat(doc.getElementsByTagName("lat")[0].childNodes[0].nodeValue);
//    console.log("latitude:" + latitude)
      var longitude = parseFloat(doc.getElementsByTagName("lng")[0].childNodes[0].nodeValue);
//    console.log("Pt:" + latitude + ", " + longitude);
      var label = "Your position";
      // Plot the station on the map
      var mapFrame = document.getElementById("map-frame");
      var canvas = "mapCanvas";
      mapFrame.contentWindow.plotPosToCanvas(canvas, latitude, longitude, label);
    }
    catch (err)
    {
      errMess += ((errMess.length > 0?"\n":"") + "Problem with position...");
    }
    // Displays
    try
    {
      var bsp = parseFloat(doc.getElementsByTagName("bsp")[0].childNodes[0].nodeValue);
//    displayBSP.animate(bsp);
      displayBSP.setValue(bsp);
      displayOverview.setBSP(bsp);
      jumboBSP.setValue(bsp.toFixed(2));
    }
    catch (err)
    {
      errMess += ((errMess.length > 0?"\n":"") + "Problem with boat speed...");
//    displayBSP.animate(0.0);
      displayBSP.setValue(0.0);
    }
    try
    {
      var log = parseFloat(doc.getElementsByTagName("log")[0].childNodes[0].nodeValue);
      displayLog.setValue(log);
   // document.getElementById("log").innerText = log.toFixed(0);
    }
    catch (err)
    {
      console.log("Log problem...")
      errMess += ((errMess.length > 0?"\n":"") + "Problem with log...:" + err);
    }
    try
    {
      var gpsDate = parseFloat(doc.getElementsByTagName("gps-date-time")[0].childNodes[0].nodeValue);
      displayDate.setValue(gpsDate);
      displayTime.setValue(gpsDate);
   // document.getElementById("log").innerText = log.toFixed(0);
    }
    catch (err)
    {
      console.log("GPS Date problem...")
      errMess += ((errMess.length > 0?"\n":"") + "Problem with GPS Date...:" + err);
    }    

    try
    {
      var hdg = parseFloat(doc.getElementsByTagName("hdg")[0].childNodes[0].nodeValue) % 360;
//    displayHDG.animate(hdg);
      displayHDG.setValue(hdg);
      displayOverview.setHDG(hdg);
      jumboHDG.setValue(lpad(Math.round(hdg).toString(), '0', 3));
      rose.setValue(Math.round(hdg));
    }
    catch (err)
    {
      errMess += ((errMess.length > 0?"\n":"") + "Problem with heading...");
//    displayHDG.animate(0.0);
      displayHDG.setValue(0.0);
    }
    try
    {
      var twd = parseFloat(doc.getElementsByTagName("twd")[0].childNodes[0].nodeValue) % 360;
//    displayTWD.animate(twd);
      displayTWD.setValue(twd);
      displayOverview.setTWD(twd);
      jumboTWD.setValue(lpad(Math.round(twd).toString(), '0', 3));
      twdEvolution.addTWD({ "angle": twd, "time": (new Date()).getTime() });
    }
    catch (err)
    {
      errMess += ((errMess.length > 0?"\n":"") + "Problem with TWD...");
//    displayTWD.animate(0.0);
      displayTWD.setValue(0.0);
    }
    try
    {
      var twa = parseFloat(doc.getElementsByTagName("twa")[0].childNodes[0].nodeValue);
      displayOverview.setTWA(twa);
      var twaStr = lpad(Math.round(Math.abs(twa)).toString(), '0', 3);
      if (twa < 0)
        twaStr = '-' + twaStr;
      else
        twaStr += '-';
      jumboTWA.setValue(twaStr);
    }
    catch (err)
    {
      errMess += ((errMess.length > 0?"\n":"") + "Problem with TWD...");
//    displayTWD.animate(0.0);
      displayTWD.setValue(0.0);
    }
    try
    {
      var tws = parseFloat(doc.getElementsByTagName("tws")[0].childNodes[0].nodeValue);
//    displayTWS.animate(tws);
      displayTWS.setValue(tws);
      displayOverview.setTWS(tws);
      jumboTWS.setValue(tws.toFixed(1));
      twsEvolution.addTWS({ "speed": tws, "time": (new Date()).getTime() });
      
      var from = twsEvolution.getFromBoundary();
      var to   = twsEvolution.getToBoundary();
      
      var dateFmt = (to - from > (3600000 * 24)) ? "d-M H:i:s" : "H:i:s"; // "d-M-Y H:i:s._ Z";
      document.getElementById("life-span").innerHTML = twsEvolution.getBufferLength() + " pts<br>" + 
                                                       "From " + (new Date(from)).format(dateFmt) + "<br>" + 
                                                       "To " + (new Date(to)).format(dateFmt) + "<br>" + 
                                                    // "(" + twsEvolution.getLifeSpan() + ")" + "<br>" + 
                                                       twsEvolution.getLifeSpanFormatted();
    }
    catch (err)
    {
      errMess += ((errMess.length > 0?"\n":"") + "Problem with TWS...");
//    displayTWS.animate(0.0);
      displayTWS.setValue(0.0);
    }
    try
    {
      var waterTemp = parseFloat(doc.getElementsByTagName("wtemp")[0].childNodes[0].nodeValue);
//    thermometer.animate(waterTemp);
      thermometer.setValue(waterTemp);
    }
    catch (err)
    {
      errMess += ((errMess.length > 0?"\n":"") + "Problem with water temperature...");
//    thermometer.animate(0.0);
      thermometer.setValue(0.0);
    }
    try
    {
      var airTemp = parseFloat(doc.getElementsByTagName("atemp")[0].childNodes[0].nodeValue);
//    athermometer.animate(airTemp);
      athermometer.setValue(airTemp);
    }
    catch (err)
    {
      errMess += ((errMess.length > 0?"\n":"") + "Problem with air temperature...");
//    athermometer.animate(0.0);
      athermometer.setValue(0.0);
    }
    // Battery_Voltage, Relative_Humidity, Barometric_Pressure
    try
    {
      var voltage = parseFloat(doc.getElementsByTagName("Battery_Voltage")[0].childNodes[0].nodeValue);
//    displayVolt.animate(airTemp);
      displayVolt.setValue(voltage);
    }
    catch (err)
    {
      errMess += ((errMess.length > 0?"\n":"") + "Problem with air Battery_Voltage...");
//    displayVolt.animate(0.0);
//    displayVolt.setValue(0.0);
    }
    try
    {
      var baro = parseFloat(doc.getElementsByTagName("prmsl")[0].childNodes[0].nodeValue);
//    displayBaro.animate(airTemp);
      displayBaro.setValue(baro);
    }
    catch (err)
    {
//    errMess += ((errMess.length > 0?"\n":"") + "Problem with air Barometric_Pressure...");
//    displayBaro.animate(0.0);
//    displayBaro.setValue(1013.0);
    }
    try
    {
      var hum = parseFloat(doc.getElementsByTagName("Relative_Humidity")[0].childNodes[0].nodeValue);
//    displayHum.animate(airTemp);
      displayHum.setValue(hum);
    }
    catch (err)
    {
      errMess += ((errMess.length > 0?"\n":"") + "Problem with air Relative_Humidity...");
//    displayHum.animate(0.0);
      displayHum.setValue(0.0);
    }
    try
    {
      var aws = parseFloat(doc.getElementsByTagName("aws")[0].childNodes[0].nodeValue);
      displayAW.setAWS(aws);
      displayOverview.setAWS(aws);
      jumboAWS.setValue(aws.toFixed(1));
    }
    catch (err)
    {
      errMess += ((errMess.length > 0?"\n":"") + "Problem with AWS...");
    }    
    try
    {
      var awa = parseFloat(doc.getElementsByTagName("awa")[0].childNodes[0].nodeValue);
//    displayAW.animate(awa);
      displayAW.setValue(awa);
      displayOverview.setAWA(awa);
      var awaStr = lpad(Math.round(Math.abs(awa)).toString(), '0', 3);
      if (awa < 0)
        awaStr = '-' + awaStr;
      else
        awaStr += '-';
      jumboAWA.setValue(awaStr);
    }
    catch (err)
    {
      errMess += ((errMess.length > 0?"\n":"") + "Problem with AWA...");
    }    
    try
    {
      var cdr = parseFloat(doc.getElementsByTagName("cdr")[0].childNodes[0].nodeValue);
      displayOverview.setCDR(cdr);
      jumboCDR.setValue(lpad(Math.round(cdr).toString(), '0', 3));
      displayCurrent.setValue(cdr);
    }
    catch (err)
    {
      errMess += ((errMess.length > 0?"\n":"") + "Problem with CDR...");
    }
      
    try
    {
      var cog = parseFloat(doc.getElementsByTagName("cog")[0].childNodes[0].nodeValue);
      displayOverview.setCOG(cog);
      jumboCOG.setValue(lpad(Math.round(cog).toString(), '0', 3));
    }
    catch (err)
    {
      errMess += ((errMess.length > 0?"\n":"") + "Problem with COG...");
    }
    try
    {
      var cmg = parseFloat(doc.getElementsByTagName("cmg")[0].childNodes[0].nodeValue);
      displayOverview.setCMG(cmg);
//    jumboCMG.setValue(lpad(Math.round(cmg).toString(), '0', 3));
    }
    catch (err)
    {
      errMess += ((errMess.length > 0?"\n":"") + "Problem with CMG...");
    }      
    try
    {
      var leeway = parseFloat(doc.getElementsByTagName("leeway")[0].childNodes[0].nodeValue);
      displayOverview.setLeeway(leeway);
      var lwyStr = lpad(Math.round(Math.abs(leeway)).toString(), '0', 2);
      if (leeway < 0)
        lwyStr = '-' + lwyStr;
      else
        lwyStr += '-';
      jumboLWY.setValue(lwyStr);
    }
    catch (err)
    {
      errMess += ((errMess.length > 0?"\n":"") + "Problem with Leeway...");
    }      
    try
    {
      var csp = parseFloat(doc.getElementsByTagName("csp")[0].childNodes[0].nodeValue);
      displayOverview.setCSP(csp);
      jumboCSP.setValue(csp.toFixed(2));
      displayCurrent.setCurrentSpeed(csp);
    }
    catch (err)
    {
      errMess += ((errMess.length > 0?"\n":"") + "Problem with CSP...");
    }    
    try
    {
      var sog = parseFloat(doc.getElementsByTagName("sog")[0].childNodes[0].nodeValue);
      displayOverview.setSOG(sog);
      jumboSOG.setValue(sog.toFixed(2));
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
      displayOverview.setB2WP(b2wp);
      document.getElementById("display.vmg.waypoint").disabled = false;
      document.getElementById("display.vmg.waypoint").value = to_wp;
      document.getElementById("display.vmg.waypoint").nextSibling.textContent = "VMG to " + to_wp;
    }
    catch (err)
    {
      document.getElementById("display.vmg.waypoint").disabled = true;
      document.getElementById("display.vmg.wind").checked = true;
    }
    
    try
    {
      var vmg = 0;
      if (document.getElementById("display.vmg.wind").checked)
        vmg = parseFloat(doc.getElementsByTagName("vmg-wind")[0].childNodes[0].nodeValue);
      else
        vmg = parseFloat(doc.getElementsByTagName("vmg-wp")[0].childNodes[0].nodeValue);
      displayOverview.setVMG(vmg);
      jumboVMG.setValue(vmg.toFixed(2));
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
      displayPRF.setValue(perf);
      displayOverview.setPerf(perf);
    }
    catch (err)
    {
   // errMess += ((errMess.length > 0?"\n":"") + "Problem with Perf...");
      displayPRF.setValue(100);
    }
    
    // Calibration Prms
    try
    {
      document.getElementById("update.button").disabled = !(document.getElementById("edit.prms").checked)
      if (document.getElementById("edit.prms").checked)  
      {        
        if (!editing)
          populatePrmForEditing(doc);
        editing = true;
      }
      else
      {
        populatePrmForDisplaying(doc);
        editing = false;
      }
    }
    catch (err)
    {
      errMess += ((errMess.length > 0?"\n":"") + "Problem with Cal Prms...");
    }
    
    if (errMess !== undefined)
      document.getElementById("err-mess").innerHTML = errMess;
  }
  catch (err)
  {
    document.getElementById("err-mess").innerHTML = err;
  }
};

var populatePrmForDisplaying = function(doc)
{
  document.getElementById("bsp-factor").innerHTML         = doc.getElementsByTagName("bsp-factor")[0].childNodes[0].nodeValue;
  document.getElementById("aws-factor").innerHTML         = doc.getElementsByTagName("aws-factor")[0].childNodes[0].nodeValue;
  document.getElementById("awa-offset").innerHTML         = doc.getElementsByTagName("awa-offset")[0].childNodes[0].nodeValue;
  document.getElementById("hdg-offset").innerHTML         = doc.getElementsByTagName("hdg-offset")[0].childNodes[0].nodeValue;
  document.getElementById("max-leeway").innerHTML         = doc.getElementsByTagName("max-leeway")[0].childNodes[0].nodeValue;
  document.getElementById("dev-file").innerHTML           = doc.getElementsByTagName("dev-file")[0].childNodes[0].nodeValue;
  document.getElementById("def-decl").innerHTML           = doc.getElementsByTagName("default-decl")[0].childNodes[0].nodeValue;
  document.getElementById("damping").innerHTML            = doc.getElementsByTagName("damping")[0].childNodes[0].nodeValue;
  document.getElementById("polar-file").innerHTML         = doc.getElementsByTagName("polar-file")[0].childNodes[0].nodeValue;
  document.getElementById("polar-speed-factor").innerHTML = doc.getElementsByTagName("polar-speed-factor")[0].childNodes[0].nodeValue;

  // TODO For real
  document.getElementById("display-wt").innerHTML    = "&nbsp;&nbsp;" + (displayOptions.displayWT    === true ? "Y" : "N");
  document.getElementById("display-at").innerHTML    = "&nbsp;&nbsp;" + (displayOptions.displayAT    === true ? "Y" : "N");
  document.getElementById("display-gdt").innerHTML   = "&nbsp;&nbsp;" + (displayOptions.displayGDT   === true ? "Y" : "N");
  document.getElementById("display-prmsl").innerHTML = "&nbsp;&nbsp;" + (displayOptions.displayPRMSL === true ? "Y" : "N");
  document.getElementById("display-hum").innerHTML   = "&nbsp;&nbsp;" + (displayOptions.displayHUM   === true ? "Y" : "N");
  document.getElementById("display-volt").innerHTML  = "&nbsp;&nbsp;" + (displayOptions.displayVOLT  === true ? "Y" : "N");
};

var populatePrmForEditing = function(doc)
{
  document.getElementById("bsp-factor").innerHTML         = "<input id='new-bsp' type='text' value='" + doc.getElementsByTagName("bsp-factor")[0].childNodes[0].nodeValue + "'>";
  document.getElementById("aws-factor").innerHTML         = "<input id='new-aws' type='text' value='" + doc.getElementsByTagName("aws-factor")[0].childNodes[0].nodeValue + "'>";
  document.getElementById("awa-offset").innerHTML         = "<input id='new-awa' type='text' value='" + doc.getElementsByTagName("awa-offset")[0].childNodes[0].nodeValue + "'>";
  document.getElementById("hdg-offset").innerHTML         = "<input id='new-hdg' type='text' value='" + doc.getElementsByTagName("hdg-offset")[0].childNodes[0].nodeValue + "'>";
  document.getElementById("max-leeway").innerHTML         = "<input id='new-lwy' type='text' value='" + doc.getElementsByTagName("max-leeway")[0].childNodes[0].nodeValue + "'>";
  document.getElementById("dev-file").innerHTML           = "<input id='new-dev' type='text' value='" + doc.getElementsByTagName("dev-file")[0].childNodes[0].nodeValue + "'>";
  document.getElementById("def-decl").innerHTML           = "<input id='new-dec' type='text' value='" + doc.getElementsByTagName("default-decl")[0].childNodes[0].nodeValue + "'>";
  document.getElementById("damping").innerHTML            = "<input id='new-dpg' type='text' value='" + doc.getElementsByTagName("damping")[0].childNodes[0].nodeValue + "'>";
  document.getElementById("polar-file").innerHTML         = "<input id='new-pol' type='text' value='" + doc.getElementsByTagName("polar-file")[0].childNodes[0].nodeValue + "'>";
  document.getElementById("polar-speed-factor").innerHTML = "<input id='new-fac' type='text' value='" + doc.getElementsByTagName("polar-speed-factor")[0].childNodes[0].nodeValue + "'>";

  // TODO For real
  document.getElementById("display-wt").innerHTML    = "<input type='checkbox' id='display-wt-cb'" + (displayOptions.displayWT === true ? "checked" : "") + ">";
  document.getElementById("display-at").innerHTML    = "<input type='checkbox' id='display-at-cb'" + (displayOptions.displayAT === true ? "checked" : "") + ">";
  document.getElementById("display-gdt").innerHTML   = "<input type='checkbox' id='display-gdt-cb'" + (displayOptions.displayGDT === true ? "checked" : "") + ">";
  document.getElementById("display-prmsl").innerHTML = "<input type='checkbox' id='display-prmsl-cb'" + (displayOptions.displayPRMSL === true ? "checked" : "") + ">";
  document.getElementById("display-hum").innerHTML   = "<input type='checkbox' id='display-hum-cb'" + (displayOptions.displayHUM === true ? "checked" : "") + ">";
  document.getElementById("display-volt").innerHTML  = "<input type='checkbox' id='display-volt-cb'" + (displayOptions.displayVOLT === true ? "checked" : "") + ">";
};

var updatePrms = function()
{
  try
  {
    var bsp = parseFloat(document.getElementById('new-bsp').value);
    var aws = parseFloat(document.getElementById('new-aws').value);
    var awa = parseFloat(document.getElementById('new-awa').value);
    var hdg = parseFloat(document.getElementById('new-hdg').value);
    var lwy = parseFloat(document.getElementById('new-lwy').value);
    var dev = document.getElementById('new-dev').value;
    var dec = parseFloat(document.getElementById('new-dec').value);
    var dpg = parseInt(document.getElementById('new-dpg').value);
    var pol = document.getElementById('new-pol').value;
    var fac = parseFloat(document.getElementById('new-fac').value);
    // Send values to server
    try
    {
      var updateXHR = new XMLHttpRequest();
      var qString = "?bsp=" + encodeURIComponent(bsp) + 
                    "&aws=" + encodeURIComponent(aws) + 
                    "&awa=" + encodeURIComponent(awa) + 
                    "&hdg=" + encodeURIComponent(hdg) + 
                    "&lwy=" + encodeURIComponent(lwy) + 
                    "&dev=" + encodeURIComponent(dev) + 
                    "&dec=" + encodeURIComponent(dec) + 
                    "&dpg=" + encodeURIComponent(dpg) + 
                    "&pol=" + encodeURIComponent(pol) + 
                    "&fac=" + encodeURIComponent(fac);
      updateXHR.open("GET", "/update-prms" + qString, true);
//    xhr.setRequestHeader("Content-type","application/x-www-form-urlencoded");
      updateXHR.send();
      var resp = updateXHR.responseText; 
      console.log("Update completed");
    }
    catch (err)
    {
      console.log(err);
    }
  }
  catch (err)
  {
    console.log(err);
  }
};

var lpad = function(str, pad, len)
{
  while (str.length < len)
    str = pad + str;
  return str;
};
