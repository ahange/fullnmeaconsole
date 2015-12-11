/*
 * Spot GRIB Request result parser
 * By OlivSoft
 * olivier@lediouris.net
 */
 
var SpotParser =
{
  nmeaData : [],
  position : {},
  
  parse : function(spotContent, cb, cb2)
  {
    SpotParser.nmeaData  = [];
    var line = spotContent.split("\n");
//  console.info("We have " + line.length + " line(s)");
    
    var linkList = "";
    //                           07-03 00:00 1011.8  8.4 214  0.0 11.3
    var regExp     = new RegExp("(\\d{2}-\\d{2}\\s\\d{2}:\\d{2})\\s*(\\d*\\.\\d)\\s*(\\d*\\.\\d)\\s*(\\d*)\\s*(\\d*\\.\\d).*");  
    //                            ^                                  ^               ^               ^         ^          ^
    //                            date                               prmsl           tws             twd       rain       etc
    
    //                           request code: spot:37.5N,122.5W|5,3|PRMSL,WIND,RAIN,LFTX
    var posRegExpr = new RegExp("request\\scode:\\sspot:([^|]*).*");
    var j = 0;
    for (var i=0; i<line.length; i++)
    {
      var matches = regExp.exec(line[i]);
      if (matches !== null)
      {
        var date  = matches[1];
        var prmsl = matches[2];
        var tws   = matches[3];
        var twd   = matches[4];
        var rain  = matches[5];
        
//      console.info("Line:" + date + ":" + tws);
        SpotParser.nmeaData.push(new NMEAData(date, prmsl, tws, twd, rain));
        linkList += ("<a href='javascript:" + cb + "(" + j + ", \"" + tws + "\", \"" + twd + "\", \"" + prmsl + "\", \"" + rain + "\");' title='" + date + "'>" + (j+1).toString() + "</a>&nbsp;");
        j++;
      } 
      else
      {
        var posMatch = posRegExpr.exec(line[i]);
        if (posMatch !== null)
        {
          var posArray = posMatch[1].split(",");
          var lat = parseFloat(posArray[0].substring(0, posArray[0].length - 1));
          var lng = parseFloat(posArray[1].substring(0, posArray[1].length - 1));
          if (posArray[0].charAt(posArray[0].length - 1) === 'S')
            lat = -lat;
          if (posArray[1].charAt(posArray[1].length - 1) === 'W')
            lng = -lng;
          SpotParser.position["lat"] = lat;
          SpotParser.position["lng"] = lng;
        }
      }
    }    
    if (cb2)
      cb2();
      
    return linkList;
  }
};

var NMEAData = function(date, prmsl, tws, twd, rain)
{
  var nmeaDate = date;
  var nmeaPrmsl = prmsl;
  var nmeaTws = tws;
  var nmeaTwd = twd;
  var nmeaRain = rain;
  
  this.getNMEADate = function()
  { return nmeaDate; };
  
  this.getNMEAPrmsl = function()
  { return nmeaPrmsl; };
  
  this.getNMEATws = function()
  { return nmeaTws; };
  
  this.getNMEATwd = function()
  { return nmeaTwd; };
  
  this.getNMEARain = function()
  { return nmeaRain; };
};
