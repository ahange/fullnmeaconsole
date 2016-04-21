package utils.astro;

import calculation.AstroComputer;

import java.util.Calendar;
import java.util.GregorianCalendar;
import java.util.TimeZone;

import ocss.nmea.parser.GeoPos;

public class AstroUtil
{
  /**
   * 
   * @param pos Position.
   * @param tz Time Zone as String (like "America/Los_Angeles").
   * @param reference The day you want the sun rise and set of.
   * @return Calendar 2-pos array: [SunRise, SunSet]
   */
  public static Calendar[] calculateRiseSet(GeoPos pos, String tz, Calendar reference)
  {
    double[] rsSun  = null;
    rsSun  = AstroComputer.sunRiseAndSet(pos.lat, pos.lng);
    Calendar sunRise = new GregorianCalendar();
    sunRise.setTimeZone(TimeZone.getTimeZone(tz));
    sunRise.set(Calendar.YEAR, reference.get(Calendar.YEAR));
    sunRise.set(Calendar.MONTH, reference.get(Calendar.MONTH));
    sunRise.set(Calendar.DAY_OF_MONTH, reference.get(Calendar.DAY_OF_MONTH));
    sunRise.set(Calendar.SECOND, 0);

    double r = rsSun[AstroComputer.UTC_RISE_IDX] /*+ Utils.daylightOffset(sunRise)*/ + AstroComputer.getTimeZoneOffsetInHours(TimeZone.getTimeZone(tz), sunRise.getTime());
    int min = (int)((r - ((int)r)) * 60);
    sunRise.set(Calendar.MINUTE, min);
    sunRise.set(Calendar.HOUR_OF_DAY, (int)r);
    
    Calendar sunSet = new GregorianCalendar();
    sunSet.setTimeZone(TimeZone.getTimeZone(tz));
    sunSet.set(Calendar.YEAR, reference.get(Calendar.YEAR));
    sunSet.set(Calendar.MONTH, reference.get(Calendar.MONTH));
    sunSet.set(Calendar.DAY_OF_MONTH, reference.get(Calendar.DAY_OF_MONTH));
    sunSet.set(Calendar.SECOND, 0);

//  System.out.println("Set : TZ offset at " + sunSet.getTime() + " is " + AstroComputer.getTimeZoneOffsetInHours(TimeZone.getTimeZone(timeZone2Use /*ts.getTimeZone()*/), sunSet.getTime()));
    r = rsSun[AstroComputer.UTC_SET_IDX] /*+ Utils.daylightOffset(sunSet)*/ + AstroComputer.getTimeZoneOffsetInHours(TimeZone.getTimeZone(tz), sunSet.getTime());
    min = (int)((r - ((int)r)) * 60);
    sunSet.set(Calendar.MINUTE, min);
    sunSet.set(Calendar.HOUR_OF_DAY, (int)r);
    
    sunRise.setTimeZone(TimeZone.getTimeZone(tz));
    sunSet.setTimeZone(TimeZone.getTimeZone(tz));    
    
    return new Calendar[] {sunRise, sunSet};
  }
}
