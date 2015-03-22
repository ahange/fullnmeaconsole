# Full Navigation Console #
This project is part of the Navigation Desktop project. Build it from [there](http://code.google.com/p/oliv-soft-project-builder/). This is a link to the build process, it manages all the required dependencies.
<br>
If you are not interested in the build process from the Source Control Repository, you can download the executable directly from the <a href='http://code.google.com/p/navigation-desktop/downloads/list'>navigation desktop download page</a>.<br>
<hr />
This Console renders the data emitted by the NMEA port in a graphical manner.<br>
<br />
The NMEA stream can be read from<br>
<ul><li>a Serial port<br>
</li><li>a TCP Port<br>
</li><li>a UDP Port<br>
</li><li>XML over HTTP<br>
</li><li>a file (data replay)<br>
</li><li>RMI (Java to Java)<br>
</li><li>GPSd (in development)<br>
NMEA Sentences can as well be <i>re-broadcasted</i> on the channels mentioned in the list above (TCP and UDP being the most popular), so other applications can use them.<br>
<br />
There is also an integrated very light HTTP server, which can be used for other devices to access the data in real time, from a browser supporting HTML5 (for a rich client interface). Typically, an ad-hoc network setup in the boat will allow tablets to see the real-time NMEA data rendered in HTML5.<br>
<br />
This <i>re-broadcasting</i> addresses the exclusive access required by Serial ports. For example, <a href='http://opencpn.org'>OpenCPN</a> and the NMEA Console can share the same data, read at the same time.<br>
<h2>NMEA Console</h2>
<img src='http://donpedro.lediouris.net/software/img/console.png' />
Provides a rich user interface for NMEA Data.<br>
<br>
Reads NMEA Data from the channels mentioned at the top of this page. Along with the re-broadcasting feature, that means that you can read the data from a serial port and forward them so they can be read from another application, on the same machine, or from another one connected on the same network (like Home Wireless Network).<br>
<br>
Provides among others:<br>
</li></ul><ul><li>Bulk Data Display<br>
</li><li>Formatted Display<br>
</li><li>Graphical 2D display<br>
</li><li>Replay capabilities<br>
</li><li>Real time current evaluation (instant and dead reckoning on two other values - like 1 minute and 10 minutes)<br>
</li><li>Logging capabilities<br>
</li><li>NMEA Sentences <i>re-broadcasting</i> (see channels above). This way several stations can "share" the same Serial Port...<br>
</li><li>Journal capabilities (with Hypersonic SQL)<br>
</li><li>Deviation curve elaboration and management</li></ul>

<img src='http://donpedro.lediouris.net/software/img/console.png' />
<br>
Real time console.<br>
<br>
<img src='http://donpedro.lediouris.net/software/img/data.png' />
<br>
Data viewer, raw data, calculated data - with a smoothing factor.<br>
<br>
<img src='http://donpedro.lediouris.net/software/img/dr.png' />
<br>
Dead reckoning for the current. Instant (triangulation), 1 minute, 10 minutes.<br>
<br>
<img src='http://donpedro.lediouris.net/software/img/html5.png' />
<br>
The HTML5 Console, displayed in an HTML5 browser (iPad, Android, any tablet...).<br>
<br>