function Graph(cName,       // Canvas Name
               cWidth,      // Canvas width
               cHeight,     // Canvas height
               graphData,   // x,y tuple array
               xLabels,     // array of labels, for abscisses
               yLabels)     // array of labels, for ordinates
{
  var instance = this;
  var xScale, yScale;
  var minx, miny, maxx, maxy;
  var context;
  
  var canvas = document.getElementById(cName);
  canvas.addEventListener('mousemove', function(evt)
  {
    if (document.getElementById("tooltip").checked)
    {
      var x = evt.pageX - canvas.offsetLeft;
      var y = evt.pageY - canvas.offsetTop;
      
      var coords = relativeMouseCoords(evt, canvas);
      x = coords.x;
      y = coords.y;
//    console.log("Mouse: x=" + x + ", y=" + y);
      
      var idx = Math.round(x / xScale);
      if (idx < SpotParser.nmeaData.length)
      {
        var str1; // = 'X : ' + x + ', ' + 'Y :' + y;
        var str2;
        try 
        { 
          str1 = SpotParser.nmeaData[idx].getNMEATws() + "kt @ " + SpotParser.nmeaData[idx].getNMEATwd() + "\272";
          if (document.getElementById("utc-display").checked)
            str2 = SpotParser.nmeaData[idx].getNMEADate() + " UT";
          else
            str2 = reformatDate(SpotParser.nmeaData[idx].getNMEADate(), "d-M H:i");
  //      console.log("Bubble:" + str);
        }
        catch (err) { console.log(JSON.stringify(err)); }
        
  //    context.fillStyle = '#000';
  //    context.fillRect(0, 0, w, h);
        instance.drawGraph(cName, graphData);
        instance.drawWind(SpotParser.nmeaData);
        context.fillStyle = "rgba(250, 250, 210, .7)"; 
//      context.fillStyle = 'yellow';
        context.fillRect(x + 10, y + 10, 70, 30); // Background
        context.fillStyle = 'black';
        context.font = 'bold 12px verdana';
        context.fillText(str1, x + 15, y + 25, 60); 
        context.fillText(str2, x + 15, y + 37, 60); 
      }
    }
  }, 0);
  
  var relativeMouseCoords = function (event, element)
  {
    var totalOffsetX = 0;
    var totalOffsetY = 0;
    var canvasX = 0;
    var canvasY = 0;
    var currentElement = element;

    do
    {
      totalOffsetX += currentElement.offsetLeft - currentElement.scrollLeft;
      totalOffsetY += currentElement.offsetTop - currentElement.scrollTop;
    }
    while (currentElement = currentElement.offsetParent)

    canvasX = event.pageX - totalOffsetX;
    canvasY = event.pageY - totalOffsetY;

    return {x:canvasX, y:canvasY};
  };
  
  this.minX = function(data)
  {
    var min = Number.MAX_VALUE;
    for (var i=0; i<data.length; i++)
      min = Math.min(min, data[i].getX());
    return min;
  };
  
  this.minY = function(data)
  {
    var min = Number.MAX_VALUE;
    for (var i=0; i<data.length; i++)
      min = Math.min(min, data[i].getY());
    return min;
  };
  
  this.maxX = function(data)
  {
    var max = Number.MIN_VALUE;
    for (var i=0; i<data.length; i++)
      max = Math.max(max, data[i].getX());
    return max;
  };
  
  this.maxY = function(data)
  {
    var max = Number.MIN_VALUE;
    for (var i=0; i<data.length; i++)
      max = Math.max(max, data[i].getY());
    return max;
  };
  
  this.drawGraph = function(displayCanvasName, data, idx)
  {
    context = canvas.getContext('2d');
    
    var _idxX;
    if (idx !== undefined)
      _idxX = idx * xScale;
    
    var mini = Math.floor(this.minY(data));
    var maxi = Math.ceil(this.maxY(data));
    var gridXStep = Math.round((maxi - mini) / 3);
    var gridYStep = Math.round(SpotParser.nmeaData.length / 10);
    
    // Sort the tuples (on X)
    data.sort(sortTupleX);
    
    var smoothData = [];
    // 1 - More data (10 times more)
    for (var i=0; i<data.length - 1; i++)
    {
      for (var j=0; j<10; j++)
      {
        var _x = data[i].getX() + (j * (data[i + 1].getX() - data[i].getX()) / 10);
        var _y = data[i].getY() + (j * (data[i + 1].getY() - data[i].getY()) / 10);
        smoothData.push(new Tuple(_x, _y));
      }
    }
    // 2 - Smooth
    var _smoothData = [];
    var smoothWidth = 20;
    for (var i=0; i<smoothData.length; i++)
    {
      var yAccu = 0;
      for (var acc=i-(smoothWidth / 2); acc<i+(smoothWidth/2); acc++)
      {
        var y;
        if (acc < 0)
          y = smoothData[0].getY();
        else if (acc > (smoothData.length - 1))
          y = smoothData[smoothData.length - 1].getY();
        else
          y = smoothData[acc].getY();
        yAccu += y;
      }
      yAccu = yAccu / smoothWidth;
      _smoothData.push(new Tuple(smoothData[i].getX(), yAccu));
//    console.log("I:" + smoothData[i].getX() + " y from " + smoothData[i].getY() + " becomes " + yAccu);
    }
    smoothData = _smoothData;
    
    context.fillStyle = "LightGray";
    context.fillRect(0, 0, canvas.width, canvas.height);    

    // Horizontal grid (TWS)
    for (var i=Math.round(mini); i<maxi; i+=gridXStep)
    {
      context.beginPath();
      context.lineWidth = 1;
      context.strokeStyle = 'gray';
      context.moveTo(0, cHeight - (i - mini) * yScale);
      context.lineTo(cWidth, cHeight - (i - mini) * yScale);
      context.stroke();

      context.save();
      context.font = "bold 10px Arial"; 
      context.fillStyle = 'black';
      str = i.toString() + " kt";
      len = context.measureText(str).width;
      context.fillText(str, cWidth - (len + 2), cHeight - ((i - mini) * yScale) - 2);
      context.restore();            
      context.closePath();
    }
    
    // Vertical grid (Time)
    for (var i=gridYStep; i<data.length; i+=gridYStep)
    {
      context.beginPath();
      context.lineWidth = 1;
      context.strokeStyle = 'gray';
      context.moveTo(i * xScale, 0);
      context.lineTo(i * xScale, cHeight);
      context.stroke();

      // Rotate the whole context, and then write on it (that's why we need the translate)
      context.save(); 
      context.translate(i * xScale, canvas.height);
      context.rotate(-Math.PI / 2);
      context.font = "bold 10px Arial"; 
      context.fillStyle = 'black';
      if (document.getElementById("utc-display").checked)
        str = SpotParser.nmeaData[i].getNMEADate() + " UT";
      else
        str = reformatDate(SpotParser.nmeaData[i].getNMEADate(), "D d-M H:i");
      len = context.measureText(str).width;
      context.fillText(str, 2, -1); //i * xScale, cHeight - (len));
      context.restore();            
      context.closePath();
    }

    if (document.getElementById("raw-data").checked) // Raw data
    {
      context.beginPath();
      context.lineWidth = 1;
      context.strokeStyle = 'green';
  
      var previousPoint = data[0];
      for (var i=1; i<data.length; i++)
      {
        context.moveTo((previousPoint.getX() - minx) * xScale, cHeight - (previousPoint.getY() - miny) * yScale);
        context.lineTo((data[i].getX() - minx) * xScale, cHeight - (data[i].getY() - miny) * yScale);
        context.stroke();
        previousPoint = data[i];
      }
      context.closePath();
    }
    
    if (document.getElementById("smooth-data").checked) // Smoothed data
    {
      data = smoothData;
      
      context.beginPath();
      context.lineWidth = 3;
      context.strokeStyle = 'red';
  
      previousPoint = data[0];
      for (var i=1; i<data.length; i++)
      {
        context.moveTo((previousPoint.getX() - minx) * xScale, cHeight - (previousPoint.getY() - miny) * yScale);
        context.lineTo((data[i].getX() - minx) * xScale, cHeight - (data[i].getY() - miny) * yScale);
        context.stroke();
        previousPoint = data[i];
      }
      context.closePath();
    }
    
    if (idx !== undefined)
    {
      context.beginPath();
      context.lineWidth = 1;
      context.strokeStyle = 'green';
      context.moveTo(_idxX, 0);
      context.lineTo(_idxX, cHeight);
      context.stroke();
      context.closePath();
    }
  };
  
  var ARROW_LEN = 20;
  
  this.drawWind = function(nmea)
  {    
    for (var i=0; i<nmea.length; i++)
    {
      var wd = parseFloat(nmea[i].getNMEATwd()) + 180; // Direction the wind is blowing TO
      while (wd > 360)
        wd -= 360;
      var twd = toRadians(wd); 
      context.beginPath();
      var x = i * (cWidth / nmea.length);
      var y = cHeight / 2;
      var dX = ARROW_LEN * Math.sin(twd);
      var dY = - ARROW_LEN * Math.cos(twd);
      // create a new line object
      var line = new Line(x, y, x + dX, y + dY);
      // draw the line
      line.drawWithArrowhead(context);
      context.closePath();
    }
  };

  (function()
   { 
     minx = instance.minX(graphData);
     miny = instance.minY(graphData);
     maxx = instance.maxX(graphData);
     maxy = instance.maxY(graphData);
     
//   console.log("MinX:" + minx + ", MaxX:" + maxx + ", MinY:" + miny + ", MaxY:" + maxy);
     
     xScale = cWidth / (maxx - minx);
     yScale = cHeight / (maxy - miny);
     
//   console.log("xScale:" + xScale + ", yScale:" + yScale);
     
     instance.drawGraph(cName, graphData);
   })(); // Invoked automatically when new is invoked.  
};

function Tuple(_x, _y)
{
  var x = _x;
  var y = _y;
  
  this.getX = function() { return x; };
  this.getY = function() { return y; };
};

function sortTupleX(t1, t2)
{
  if (t1.getX() < t2.getX())
    return -1;
  if (t1.getX() > t2.getX())
    return 1;
  return 0;  
};
