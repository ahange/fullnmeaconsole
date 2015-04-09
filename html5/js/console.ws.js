/*
 * @author Olivier Le Diouris
 */
var displayBSP, displayLog, displayPRF, displayTWD, displayTWS, thermometer, athermometer, displayHDG, rose, displayOverview,
    jumboBSP, jumboHDG, jumboTWD, jumboLWY, jumboAWA, jumboTWA, jumboAWS, jumboTWS, jumboCOG, jumboCDR, jumboSOG, jumboCSP, jumboVMG,
    displayAW, displayCurrent,
    twdEvolution, twsEvolution;
    
var jumboList = [];

var editing = false;

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

  var connection;

  // if user is running mozilla then use it's built-in WebSocket
  //  window.WebSocket = window.WebSocket || window.MozWebSocket;  // TODO otherwise, fall back
  var ws = window.WebSocket || window.MozWebSocket;  // TODO otherwise, fall back

  // if browser doesn't support WebSocket, just show some notification and exit
  //  if (!window.WebSocket) 

  if (!ws) 
  {
    alert('Sorry, but your browser does not support WebSockets.'); // TODO Fallback
    return;
  }

  // open connection
  var rootUri = "ws://" + (document.location.hostname === "" ? "localhost" : document.location.hostname) + ":" +
                          (document.location.port === "" ? "9876" : document.location.port);
  console.log(rootUri);
  connection = new WebSocket(rootUri); // 'ws://localhost:9876');

  connection.onopen = function () 
  {
    console.log('Connected.')
  };

  connection.onerror = function (error) 
  {
    // just in there were some problems with connection...
    alert('Sorry, but there is some problem with your connection or the server is down.');
  };

  connection.onmessage = function (message) 
  {
//  console.log('onmessage:' + JSON.stringify(message.data));
    var data = JSON.parse(message.data);
    setValues(data);
  };
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

var setValues = function(doc)
{
  try
  {
    var errMess = "";
    
    var json = doc;
    try
    {
      var latitude  = parseFloat(json.lat);
//    console.log("latitude:" + latitude)
      var longitude = parseFloat(json.lng);
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
      var bsp = parseFloat(json.bsp.toFixed(2));
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
      var log = parseFloat(json.log);
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
      var hdg = parseFloat(json.hdg.toFixed(0)) % 360;
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
      var twd = parseFloat(json.twd.toFixed(0)) % 360;
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
      var twa = parseFloat(json.twa.toFixed(0));
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
      errMess += ((errMess.length > 0?"\n":"") + "Problem with TWA...");
//    displayTWD.animate(0.0);
      displayTWD.setValue(0.0);
    }
    try
    {
      var tws = parseFloat(json.tws.toFixed(2));
//    displayTWS.animate(tws);
      displayTWS.setSecondaryValue("F" + getBeaufort(tws));
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
      var waterTemp = parseFloat(json.wtemp.toFixed(0));
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
      var airTemp = parseFloat(json.atemp.toFixed(0));
//    athermometer.animate(airTemp);
      athermometer.setValue(airTemp);
    }
    catch (err)
    {
      errMess += ((errMess.length > 0?"\n":"") + "Problem with air temperature...");
//    athermometer.animate(0.0);
      athermometer.setValue(0.0);
    }
    try
    {
      var aws = parseFloat(json.aws.toFixed(2));
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
      var awa = parseFloat(json.awa.toFixed(0));
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
      var cdr = parseFloat(json.cdr.toFixed(0));
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
      var cog = parseFloat(json.cog.toFixed(0));
      displayOverview.setCOG(cog);
      jumboCOG.setValue(lpad(Math.round(cog).toString(), '0', 3));
    }
    catch (err)
    {
      errMess += ((errMess.length > 0?"\n":"") + "Problem with COG...");
    }
    try
    {
      var cmg = parseFloat(json.cmg.toFixed(0));
      displayOverview.setCMG(cmg);
//    jumboCMG.setValue(lpad(Math.round(cmg).toString(), '0', 3));
    }
    catch (err)
    {
      errMess += ((errMess.length > 0?"\n":"") + "Problem with CMG...");
    }      
    try
    {
      var leeway = parseFloat(json.leeway.toFixed(2));
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
      errMess += ((errMess.length > 0?"\n":"") + "Problem with Leway...");
    }      
    try
    {
      var csp = parseFloat(json.csp.toFixed(2));
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
      var sog = parseFloat(json.sog.toFixed(2));
      displayOverview.setSOG(sog);
      jumboSOG.setValue(sog.toFixed(2));
    }
    catch (err)
    {
      errMess += ((errMess.length > 0?"\n":"") + "Problem with SOG...");
    }
    // towp, vmgwind, vmgwp, b2wp
    try
    {
      var to_wp = json.towp;
      var b2wp = parseFloat(json.b2wp.toFixed(0));
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
        vmg = parseFloat(json.vmgwind.toFixed(2));
      else
        vmg = parseFloat(json.vmgwp.toFixed(2));
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
      var perf = parseFloat(json.perf.toFixed(2));
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
          populatePrmForEditing(json);
        editing = true;
      }
      else
      {
        populatePrmForDisplaying(json);
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

var populatePrmForDisplaying = function(json)
{
  document.getElementById("bsp-factor").innerHTML         = json.bspfactor;
  document.getElementById("aws-factor").innerHTML         = json.awsfactor;
  document.getElementById("awa-offset").innerHTML         = json.awaoffset;
  document.getElementById("hdg-offset").innerHTML         = json.hdgoffset;
  document.getElementById("max-leeway").innerHTML         = json.maxleeway;
  document.getElementById("dev-file").innerHTML           = json.devfile;
  document.getElementById("def-decl").innerHTML           = json.defaultdecl;
  document.getElementById("damping").innerHTML            = json.damping;
  document.getElementById("polar-file").innerHTML         = json.polarfile;
  document.getElementById("polar-speed-factor").innerHTML = json.polarspeedfactor;
};

var populatePrmForEditing = function(json)
{
  document.getElementById("bsp-factor").innerHTML         = "<input id='new-bsp' type='text' value='" + json.bspfactor + "'>";
  document.getElementById("aws-factor").innerHTML         = "<input id='new-aws' type='text' value='" + json.awsfactor + "'>";
  document.getElementById("awa-offset").innerHTML         = "<input id='new-awa' type='text' value='" + json.awaoffset + "'>";
  document.getElementById("hdg-offset").innerHTML         = "<input id='new-hdg' type='text' value='" + json.hdgoffset + "'>";
  document.getElementById("max-leeway").innerHTML         = "<input id='new-lwy' type='text' value='" + json.maxleeway + "'>";
  document.getElementById("dev-file").innerHTML           = "<input id='new-dev' type='text' value='" + json.devfile + "'>";
  document.getElementById("def-decl").innerHTML           = "<input id='new-dec' type='text' value='" + json.defaultdecl + "'>";
  document.getElementById("damping").innerHTML            = "<input id='new-dpg' type='text' value='" + json.damping + "'>";
  document.getElementById("polar-file").innerHTML         = "<input id='new-pol' type='text' value='" + json.polarfile + "'>";
  document.getElementById("polar-speed-factor").innerHTML = "<input id='new-fac' type='text' value='" + json.polarspeedfactor + "'>";
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

var getBeaufort = function(ws)
{
  var BEAUFORT_SCALE = [ 0, 1, 4, 7, 11, 16, 22, 28, 34, 41, 48, 56, 64 ];
  var b = 0;
  while (ws > BEAUFORT_SCALE[b]) 
  {
    b++;
  }
  return (b>0 ? b - 1 : b);
};