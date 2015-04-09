/*
 * @author Olivier Le Diouris
 */
function AnalogDisplay(cName,                     // Canvas Name
                       dSize,                     // Display radius
                       maxValue,                  // default 10
                       majorTicks,                // default 1
                       minorTicks,                // default 0
                       withDigits,                // default true, boolean
                       overlapOver180InDegree,    // default 0, beyond horizontal, in degrees, before 0, after 180
                       startValue)                // default 0, In case it is not 0
{
  if (maxValue === undefined)
    maxValue = 10;
  if (majorTicks === undefined)
    majorTicks = 1;
  if (minorTicks === undefined)
    minorTicks = 0;
  if (withDigits === undefined)
    withDigits = true;
  if (overlapOver180InDegree === undefined)
    overlapOver180InDegree = 0;
  if (startValue === undefined)
    startValue = 0;
    
  var scale = dSize / 100;
  var secondaryValue;

  var canvasName = cName;
  var displaySize = dSize;

  var running = false;
  var previousValue = startValue;
  var intervalID;
  var valueToDisplay = 0;
  var incr = 1;
  var nbDec = 2;
  
  var instance = this;
  
//try { console.log('in the AnalogDisplay constructor for ' + cName + " (" + dSize + ")"); } catch (e) {}
  
  (function(){ drawDisplay(canvasName, displaySize, previousValue); })(); // Invoked automatically
  
  this.setNbDec = function(nb) 
  {
    nbDec = nb;
  };

  this.setDisplaySize = function(ds)
  {
    scale = ds / 100;
    displaySize = ds;
    drawDisplay(canvasName, displaySize, previousValue);
  };
  
  this.startStop = function (buttonName) 
  {
//  console.log('StartStop requested on ' + buttonName);
    var button = document.getElementById(buttonName);
    running = !running;
    button.value = (running ? "Stop" : "Start");
    if (running)
      this.animate();
    else 
    {
      window.clearInterval(intervalID);
      previousValue = valueToDisplay;
    }
  };

  this.animate = function()
  {    
    var value;
    if (arguments.length === 1)
      value = arguments[0];
    else
    {
//    console.log("Generating random value");
      value = maxValue * Math.random();
    }
    value = Math.max(value, startValue);
    value = Math.min(value, maxValue);
    
  //console.log("Reaching Value :" + value + " from " + previousValue);
    diff = value - previousValue;
    valueToDisplay = previousValue;
    
//  console.log(canvasName + " going from " + previousValue + " to " + value);
    
//    if (diff > 0)
//      incr = 0.01 * maxValue;
//    else 
//      incr = -0.01 * maxValue;
    incr = diff / 10;
    if (intervalID)
      window.clearInterval(intervalID);      
    intervalID = window.setInterval(function () { displayAndIncrement(value); }, 10);
  };

  var displayAndIncrement = function(finalValue) 
  {
    //console.log('Tic ' + inc + ', ' + finalValue);
    drawDisplay(canvasName, displaySize, valueToDisplay);
    valueToDisplay += incr;
    if ((incr > 0 && valueToDisplay > finalValue) || (incr < 0 && valueToDisplay < finalValue))
    {
//    console.log('Stop, ' + finalValue + ' reached, steps were ' + incr);
      window.clearInterval(intervalID);
      previousValue = finalValue;
      if (running)
        instance.animate();
      else
        drawDisplay(canvasName, displaySize, finalValue); // Final display
    }
  };

  function setSecondaryValue(val)
  {
    secondaryValue = val;  
  }
  
  function drawDisplay(displayCanvasName, displayRadius, displayValue)
  {
    var digitColor = 'LightBlue';
    
    var canvas = document.getElementById(displayCanvasName);
    var context = canvas.getContext('2d');

    var radius = displayRadius;
  
    // Cleanup
  //context.fillStyle = "#ffffff";
  //context.fillStyle = "LightBlue";
    context.fillStyle = "transparent";
    context.fillRect(0, 0, canvas.width, canvas.height);    
  //context.fillStyle = 'rgba(255, 255, 255, 0.0)';
  //context.fillRect(0, 0, canvas.width, canvas.height);    
  
    context.beginPath();
  //context.arc(x, y, radius, startAngle, startAngle + Math.PI, antiClockwise);      
//  context.arc(canvas.width / 2, radius + 10, radius, Math.PI - toRadians(overlapOver180InDegree), (2 * Math.PI) + toRadians(overlapOver180InDegree), false);
    context.arc(canvas.width / 2, radius + 10, radius, Math.PI - toRadians(overlapOver180InDegree > 0?90:0), (2 * Math.PI) + toRadians(overlapOver180InDegree > 0?90:0), false);
    context.lineWidth = 5;
  
    var grd = context.createLinearGradient(0, 5, 0, radius);
    grd.addColorStop(0, 'black');// 0  Beginning
    grd.addColorStop(1, 'LightGrey');// 1  End
    context.fillStyle = grd;
    
    context.shadowBlur  = 20;
    context.shadowColor = 'black';
  
    context.lineJoin    = "round";
    context.fill();
    context.strokeStyle = 'DarkGrey';
    context.stroke();
    context.closePath();
    
    var totalAngle = (Math.PI + (2 * (toRadians(overlapOver180InDegree))));
    // Major Ticks
    context.beginPath();
    for (i = 0;i <= (maxValue - startValue) ;i+=majorTicks)
    {
      var currentAngle = (totalAngle * (i / (maxValue - startValue))) - toRadians(overlapOver180InDegree);
      xFrom = (canvas.width / 2) - ((radius * 0.95) * Math.cos(currentAngle));
      yFrom = (radius + 10) - ((radius * 0.95) * Math.sin(currentAngle));
      xTo = (canvas.width / 2) - ((radius * 0.85) * Math.cos(currentAngle));
      yTo = (radius + 10) - ((radius * 0.85) * Math.sin(currentAngle));
      context.moveTo(xFrom, yFrom);
      context.lineTo(xTo, yTo);
    }
    context.lineWidth = 3;
    context.strokeStyle = 'LightGreen';
    context.stroke();
    context.closePath();
  
    // Minor Ticks
    if (minorTicks > 0)
    {
      context.beginPath();
      for (i = 0;i <= (maxValue - startValue) ;i+=minorTicks)
      {
        var _currentAngle = (totalAngle * (i / (maxValue - startValue))) - toRadians(overlapOver180InDegree);

        xFrom = (canvas.width / 2) - ((radius * 0.95) * Math.cos(_currentAngle));
        yFrom = (radius + 10) - ((radius * 0.95) * Math.sin(_currentAngle));
        xTo = (canvas.width / 2) - ((radius * 0.90) * Math.cos(_currentAngle));
        yTo = (radius + 10) - ((radius * 0.90) * Math.sin(_currentAngle));
        context.moveTo(xFrom, yFrom);
        context.lineTo(xTo, yTo);
      }
      context.lineWidth = 1;
      context.strokeStyle = 'LightGreen';
      context.stroke();
      context.closePath();
    }
    
    // Numbers
    if (withDigits)
    {
      context.beginPath();
      for (i = 0;i <= (maxValue - startValue) ;i+=majorTicks)
      {
        context.save();
        context.translate(canvas.width/2, (radius + 10)); // canvas.height);
        var __currentAngle = (totalAngle * (i / (maxValue - startValue))) - toRadians(overlapOver180InDegree);
//      context.rotate((Math.PI * (i / maxValue)) - (Math.PI / 2));
        context.rotate(__currentAngle - (Math.PI / 2));
        context.font = "bold " + Math.round(scale * 15) + "px Arial"; // Like "bold 15px Arial"
        context.fillStyle = digitColor;
        str = (i + startValue).toString();
        len = context.measureText(str).width;
        context.fillText(str, - len / 2, (-(radius * .8) + 10));
        context.restore();
      }
      context.closePath();
    }
    // Value
    text = displayValue.toFixed(nbDec);
    len = 0;
    context.font = "bold " + Math.round(scale * 40) + "px Arial"; // "bold 40px Arial"
    var metrics = context.measureText(text);
    len = metrics.width;
  
    context.beginPath();
    context.fillStyle = 'LightGreen';
    context.fillText(text, (canvas.width / 2) - (len / 2), ((radius * .75) + 10));
    context.lineWidth = 1;
    context.strokeStyle = 'black';
    context.strokeText(text, (canvas.width / 2) - (len / 2), ((radius * .75) + 10)); // Outlined  
    context.closePath();
    
    if (secondaryValue !== undefined) 
    {
      text = secondaryValue;
      len = 0;
      context.font = "bold " + Math.round(scale * 32) + "px Arial"; // "bold 32px Arial"
      metrics = context.measureText(text);
      len = metrics.width;
    
      context.beginPath();
      context.fillStyle = 'LightGreen';
      context.fillText(text, (canvas.width / 2) - (len / 2), ((radius * .75) + 20));
      context.lineWidth = 1;
      context.strokeStyle = 'black';
      context.strokeText(text, (canvas.width / 2) - (len / 2), ((radius * .75) + 20)); // Outlined  
      context.closePath();      
    }
  
    // Hand
    context.beginPath();
    // Center
    context.moveTo(canvas.width / 2, radius + 10);
    
    var ___currentAngle = (totalAngle * ((displayValue - startValue) / (maxValue - startValue))) - toRadians(overlapOver180InDegree);
    // Left
    x = (canvas.width / 2) - ((radius * 0.05) * Math.cos((___currentAngle - (Math.PI / 2))));
    y = (radius + 10) - ((radius * 0.05) * Math.sin((___currentAngle - (Math.PI / 2))));
    context.lineTo(x, y);
    // Tip
    x = (canvas.width / 2) - ((radius * 0.90) * Math.cos(___currentAngle));
    y = (radius + 10) - ((radius * 0.90) * Math.sin(___currentAngle));
    context.lineTo(x, y);
    // Right
    x = (canvas.width / 2) - ((radius * 0.05) * Math.cos((___currentAngle + (Math.PI / 2))));
    y = (radius + 10) - ((radius * 0.05) * Math.sin((___currentAngle + (Math.PI / 2))));
    context.lineTo(x, y);
  
    context.closePath();
    context.fillStyle = 'rgba(0, 0, 100, 0.25)';
    context.fill();
    context.lineWidth = 1;
    context.strokeStyle = 'blue';
    context.stroke();
    // Knob
    context.beginPath();
    context.arc((canvas.width / 2), (radius + 10), 7, 0, 2 * Math.PI, false);
    context.closePath();
    context.fillStyle = '#8ED6FF';
    context.fill();
    context.strokeStyle = 'blue';
    context.stroke();
  };
  
  this.setValue = function(val)
  {
    drawDisplay(canvasName, displaySize, val);  
  };
  
  function toDegrees(rad)
  {
    return rad * (180 / Math.PI);
  }
  
  function toRadians(deg)
  {
    return deg * (Math.PI / 180);
  }
}