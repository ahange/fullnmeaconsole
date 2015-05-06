/*
 * @author Olivier Le Diouris
 */
var editing = false;
var errMess = "";

// TODO from Config file/request
var displayOptions = {
  bspFactor: 1.0,
  awsFactor: 1.0,
  awaOffset: 0,
  hdgOffset: 0,
  maxLeeway: 10,
  devFile: 'path/to/dev/file.cvs',
  defaultDecl: 15,
  damping: 30,
  polarFile: 'path/to/polar/file.coeff',
  polarSpeedFactor: 0.8,
  displayWT: true,
  displayAT: true,
  displayGDT: true,
  displayPRMSL: true,
  displayHUM: true,
  displayVOLT: true
};


var init = function() 
{
  displayOptions = getPrms();
  editDisplay();
};

var getPrms = function() {
  var prms = {};
  try
  {
    var xhr = new XMLHttpRequest();
    xhr.open("GET", "/fetch-prms", false);
    xhr.send();
    doc = xhr.response; 
//  console.log(">>> DEBUG >>> Prm:" + doc);
    errMess = "";
    prms = JSON.parse(doc);
  }
  catch (err) {
    console.log(err);
  }
  return prms;
};

var editDisplay = function() {
  try
  {
    document.getElementById("update.button").disabled = !(document.getElementById("edit.prms").checked)
    if (document.getElementById("edit.prms").checked)  
    {        
      if (!editing)
        populatePrmForEditing();
      editing = true;
    }
    else
    {
      populatePrmForDisplaying();
      editing = false;
    }
  }
  catch (err)
  {
    errMess += ((errMess.length > 0?"\n":"") + "Problem with Cal Prms...:" + err);
  }
  
  if (errMess !== undefined)
    document.getElementById("err-mess").innerHTML = errMess;
};

var populatePrmForDisplaying = function()
{
  document.getElementById("bsp-factor").innerHTML         = displayOptions.bspFactor;
  document.getElementById("aws-factor").innerHTML         = displayOptions.awsFactor;
  document.getElementById("awa-offset").innerHTML         = displayOptions.awaOffset;
  document.getElementById("hdg-offset").innerHTML         = displayOptions.hdgOffset;
  document.getElementById("max-leeway").innerHTML         = displayOptions.maxLeeway;
  document.getElementById("dev-file").innerHTML           = displayOptions.devFile;
  document.getElementById("def-decl").innerHTML           = displayOptions.defaultDecl;
  document.getElementById("damping").innerHTML            = displayOptions.damping;
  document.getElementById("polar-file").innerHTML         = displayOptions.polarFile;
  document.getElementById("polar-speed-factor").innerHTML = displayOptions.polarSpeedFactor;

  document.getElementById("display-wt").innerHTML    = "&nbsp;&nbsp;" + (displayOptions.displayWT    === true ? "Y" : "N");
  document.getElementById("display-at").innerHTML    = "&nbsp;&nbsp;" + (displayOptions.displayAT    === true ? "Y" : "N");
  document.getElementById("display-gdt").innerHTML   = "&nbsp;&nbsp;" + (displayOptions.displayGDT   === true ? "Y" : "N");
  document.getElementById("display-prmsl").innerHTML = "&nbsp;&nbsp;" + (displayOptions.displayPRMSL === true ? "Y" : "N");
  document.getElementById("display-hum").innerHTML   = "&nbsp;&nbsp;" + (displayOptions.displayHUM   === true ? "Y" : "N");
  document.getElementById("display-volt").innerHTML  = "&nbsp;&nbsp;" + (displayOptions.displayVOLT  === true ? "Y" : "N");
};

var populatePrmForEditing = function()
{
  document.getElementById("bsp-factor").innerHTML         = "<input id='new-bsp' type='text' value='" + displayOptions.bspFactor + "'>";
  document.getElementById("aws-factor").innerHTML         = "<input id='new-aws' type='text' value='" + displayOptions.awsFactor + "'>";
  document.getElementById("awa-offset").innerHTML         = "<input id='new-awa' type='text' value='" + displayOptions.awaOffset + "'>";
  document.getElementById("hdg-offset").innerHTML         = "<input id='new-hdg' type='text' value='" + displayOptions.hdgOffset + "'>";
  document.getElementById("max-leeway").innerHTML         = "<input id='new-lwy' type='text' value='" + displayOptions.maxLeeway + "'>";
  document.getElementById("dev-file").innerHTML           = "<input id='new-dev' type='text' value='" + displayOptions.devFile + "'>";
  document.getElementById("def-decl").innerHTML           = "<input id='new-dec' type='text' value='" + displayOptions.defaultDecl + "'>";
  document.getElementById("damping").innerHTML            = "<input id='new-dpg' type='text' value='" + displayOptions.damping + "'>";
  document.getElementById("polar-file").innerHTML         = "<input id='new-pol' type='text' value='" + displayOptions.polarFile + "'>";
  document.getElementById("polar-speed-factor").innerHTML = "<input id='new-fac' type='text' value='" + displayOptions.polarSpeedFactor + "'>";

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

    var dWT    = document.getElementById('display-wt-cb').checked;
    var dAT    = document.getElementById('display-at-cb').checked;
    var dGDT   = document.getElementById('display-gdt-cb').checked;
    var dPRMSL = document.getElementById('display-prmsl-cb').checked;
    var dHUM   = document.getElementById('display-hum-cb').checked;
    var dVOLT  = document.getElementById('display-volt-cb').checked;


    displayOptions.bspFactor = bsp;
    displayOptions.awsFactor = aws;
    displayOptions.awaOffset = awa;
    displayOptions.hdgOffset = hdg;
    displayOptions.maxLeeway = lwy;
    displayOptions.devFile   = dev;
    displayOptions.defaultDecl = dec;
    displayOptions.damping = dpg;
    displayOptions.polarFile = pol;
    displayOptions.polarSpeedFactor = fac;

    displayOptions.displayWT = dWT;
    displayOptions.displayAT = dAT;
    displayOptions.displayGDT = dGDT;
    displayOptions.displayPRMSL = dPRMSL;
    displayOptions.displayHUM = dHUM;
    displayOptions.displayVOLT = dVOLT;
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
                    "&fac=" + encodeURIComponent(fac) +
                    "&dwt=" + encodeURIComponent(dWT) +
                    "&dat=" + encodeURIComponent(dAT) +
                    "&dgdt=" + encodeURIComponent(dGDT) +
                    "&dprmsl=" + encodeURIComponent(dPRMSL) +
                    "&dhum=" + encodeURIComponent(dHUM) +
                    "&dvolt=" + encodeURIComponent(dVOLT);
      console.log(">>> DEBUG >>> qString:" + qString);
      updateXHR.open("GET", "/update-prms" + qString, true);
//    xhr.setRequestHeader("Content-type","application/x-www-form-urlencoded");
      updateXHR.send();
      var resp = updateXHR.responseText; 
      console.log("Update completed");
      //
      document.getElementById("edit.prms").checked = false;
      editDisplay();
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
