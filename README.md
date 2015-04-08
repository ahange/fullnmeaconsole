![Console](./navigation.jpg "Navigation Console")
# Full Navigation Console 
This project is part of the Navigation Desktop project. Build it from [here](https://github.com/OlivierLD/oliv-soft-project-builder). This is a link to the build process, it manages all the required dependencies.
<br>
----
This Console renders the data emitted by the NMEA port in a graphical manner.
<br/>
The NMEA stream can be read from
  * a Serial port
  * a TCP Port
  * a UDP Port
  * XML over HTTP
  * a file (data replay)
  * RMI (Java to Java)
  * GPSd (in development)
  
NMEA Sentences can as well be _re-broadcasted_ on the channels mentioned in the list above (TCP and UDP being the most popular), so other applications can use them.
<br/>
There is also an integrated very light HTTP server, which can be used for other devices to access the data in real time, from a browser supporting HTML5 (for a rich client interface). Typically, an ad-hoc network setup in the boat will allow tablets to see the real-time NMEA data rendered in HTML5.
<br/>
This _re-broadcasting_ addresses the exclusive access required by Serial ports. For example, [OpenCPN](http://opencpn.org) and the NMEA Console can share the same data, read at the same time.

# NMEA Console
![Console](http://donpedro.lediouris.net/software/img/console.png)
Provides a rich user interface for NMEA Data.
<br>
Reads NMEA Data from the channels mentioned at the top of this page. Along with the re-broadcasting feature, that means that you can read the data from a serial port and forward them so they can be read from another application, on the same machine, or from another one connected on the same network (like Home Wireless Network).
<br>
Provides among others:
 * Bulk Data Display
 * Formatted Display
 * Graphical 2D display
 * Replay capabilities
 * Real time current evaluation (instant and dead reckoning on two other values - like 1 minute and 10 minutes)
 * Logging capabilities
 * NMEA Sentences _re-broadcasting_ (see channels above). This way several stations can "share" the same Serial Port...
 * Journal capabilities (with Hypersonic SQL)
 * Deviation curve elaboration and management

![Console](http://donpedro.lediouris.net/software/img/console.png)
<br>
Real time console.
<br>
![Data](http://donpedro.lediouris.net/software/img/data.png)
<br>
Data viewer, raw data, calculated data - with a smoothing factor.
<br>
![Dead Reckoning](http://donpedro.lediouris.net/software/img/dr.png)
<br>
Dead reckoning for the current. Instant (triangulation), 1 minute, 10 minutes.
<br>
![HTML Console](http://donpedro.lediouris.net/software/img/html5.png)
<br>
The HTML5 Console, displayed in an HTML5 browser (iPad, Android, any tablet...).
<br>
