<?xml version="1.0" encoding="windows-1252" ?>
<!DOCTYPE xsl:stylesheet [
 <!ENTITY copy    "&#169;">
 <!ENTITY Delta   "&#916;">
 <!ENTITY delta   "&#948;">
 <!ENTITY epsilon "&#949;">
 <!ENTITY psi     "&#968;">
 <!ENTITY micro   "&#181;">
 <!ENTITY pi      "&#960;">
 <!ENTITY Pi      "&#928;">
 <!ENTITY frac12  "&#189;">
 <!ENTITY Oslash  "&#216;">
 <!ENTITY deg     "&#176;">
]>
<xsl:stylesheet version="2.0" xmlns:xsl="http://www.w3.org/1999/XSL/Transform">
  <xsl:output method="html" indent="yes"/>
  <xsl:template match="/">
    <html>
      <head>
        <meta http-equiv="Refresh" content="1; URL=."/>
        <link rel="stylesheet" href="print.css" type="text/css"/>
        <title>NMEA Data over HTTP</title>
      </head>
      <body>
        <h2>NMEA in HTML</h2>
        <table border="1">
          <xsl:for-each select="data/*">
          <tr>
            <xsl:choose>
              <xsl:when test="name(.) = 'bsp'">
                <td>Boat Speed</td><td><xsl:value-of select="."/> kts</td>                
              </xsl:when>
              <xsl:when test="name(.) = 'aws'">
                <td>Apparent Wind Speed</td><td><xsl:value-of select="."/> kts</td>                
              </xsl:when>
              <xsl:when test="name(.) = 'awa'">
                <td>Apparent Wind Angle</td><td><xsl:value-of select="."/>&deg;</td>                
              </xsl:when>
              <xsl:when test="name(.) = 'tws'">
                <td>True Wind Speed</td><td><xsl:value-of select="."/> kts</td>                
              </xsl:when>
              <xsl:when test="name(.) = 'twa'">
                <td>True Wind Angle</td><td><xsl:value-of select="."/>&deg;</td>                
              </xsl:when>
              <xsl:when test="name(.) = 'wtemp'">
                <td>Water Temperature</td><td><xsl:value-of select="."/>&deg;C</td>                
              </xsl:when>
              <xsl:when test="name(.) = 'atemp'">
                <td>Air Temperature</td><td><xsl:value-of select="."/>&deg;C</td>                
              </xsl:when>
              <xsl:when test="name(.) = 'twd'">
                <td>True Wind Direction</td><td><xsl:value-of select="."/>&deg;</td>                
              </xsl:when>
              <xsl:when test="name(.) = 'cog'">
                <td>Course Over Ground</td><td><xsl:value-of select="."/>&deg;</td>                
              </xsl:when>
              <xsl:when test="name(.) = 'sog'">
                <td>Speed Over Ground</td><td><xsl:value-of select="."/> kts</td>                
              </xsl:when>
              <xsl:when test="name(.) = 'dbt'">
                <td>Depth Below Transducer</td><td><xsl:value-of select="."/> m</td>                
              </xsl:when>
              <xsl:when test="name(.) = 'cmg'">
                <td>Course Made Good</td><td><xsl:value-of select="."/>&deg;</td>                
              </xsl:when>
              <xsl:when test="name(.) = 'hdg'">
                <td>True Heading</td><td><xsl:value-of select="."/>&deg;</td>                
              </xsl:when>
              <xsl:when test="name(.) = 'cdr'">
                <td>Current Direction</td><td><xsl:value-of select="."/>&deg;</td>                
              </xsl:when>
              <xsl:when test="name(.) = 'csp'">
                <td>Current Speed</td><td><xsl:value-of select="."/> kts</td>                
              </xsl:when>
              <xsl:when test="name(.) = 'd'">
                <td>Magnetic Deviation</td><td><xsl:value-of select="."/>&deg;</td>                
              </xsl:when>
              <xsl:when test="name(.) = 'D'">
                <td>Magnetic Declination</td><td><xsl:value-of select="."/>&deg;</td>                
              </xsl:when>
              <xsl:when test="name(.) = 'leeway'">
                <td>Leeway</td><td><xsl:value-of select="."/>&deg;</td>                
              </xsl:when>
              <xsl:when test="name(.) = 'pos'">
                <td>Boat Position</td><td><xsl:value-of select="." disable-output-escaping="yes"/></td>                
              </xsl:when>
              <xsl:when test="name(.) = 'gps-time-fmt'">
                <td>GPS Time</td><td><xsl:value-of select="." disable-output-escaping="yes"/></td>                
              </xsl:when>
              <xsl:when test="name(.) = 'gps-date-time-fmt'">
                <td>GPS Date &amp; Time</td><td><xsl:value-of select="." disable-output-escaping="yes"/></td>                
              </xsl:when>
              <xsl:when test="name(.) = 'Battery_Voltage'">
                <td>Voltage</td><td><xsl:value-of select="."/> V</td>                
              </xsl:when>
              <xsl:when test="name(.) = 'Relative_Humidity'">
                <td>Rel. Humidity</td><td><xsl:value-of select="."/> %</td>                
              </xsl:when>
              <xsl:when test="name(.) = 'prmsl'">
                <td>Baro</td><td><xsl:value-of select="."/> hPa</td>                
              </xsl:when>
              <!--xsl:otherwise>
                <td><xsl:value-of select="name(.)"/></td><td><xsl:value-of select="."/></td>                
              </xsl:otherwise-->
            </xsl:choose>
          </tr>
          </xsl:for-each>
        </table>
        <hr/>
        <small>The XSL Stylsesheet is nmea-xml-html.xsl</small>
      </body>
    </html>
  </xsl:template>
</xsl:stylesheet>
