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
   * @param reference The day you want the SUN rise and set of.
   * @return Calendar 4-pos array: [SunRise time, SunSet time, SunRise Z, SunSet Z]
   */
  public static Object[] calculateRiseSet(GeoPos pos, String tz, Calendar reference)
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
    
    return new Object[] {sunRise, sunSet, rsSun[AstroComputer.RISE_Z_IDX], rsSun[AstroComputer.SET_Z_IDX]};
  }

  /**
   *
   * @param pos Position.
   * @param tz Time Zone as String (like "America/Los_Angeles").
   * @param reference The day you want the MOON rise and set of.
   * @return Calendar 2-pos array: [MoonRise, MoonSet]
   */
  public static Object[] calculateMoonRiseSet(GeoPos pos, String tz, Calendar reference)
  {
    double[] rsMoon  = null;
    rsMoon  = AstroComputer.moonRiseAndSet(pos.lat, pos.lng);
    Calendar moonRise = new GregorianCalendar();
    moonRise.setTimeZone(TimeZone.getTimeZone(tz));
    moonRise.set(Calendar.YEAR, reference.get(Calendar.YEAR));
    moonRise.set(Calendar.MONTH, reference.get(Calendar.MONTH));
    moonRise.set(Calendar.DAY_OF_MONTH, reference.get(Calendar.DAY_OF_MONTH));
    moonRise.set(Calendar.SECOND, 0);

    double r = rsMoon[AstroComputer.UTC_RISE_IDX] /*+ Utils.daylightOffset(sunRise)*/ + AstroComputer.getTimeZoneOffsetInHours(TimeZone.getTimeZone(tz), moonRise.getTime());
    int min = (int)((r - ((int)r)) * 60);
    moonRise.set(Calendar.MINUTE, min);
    moonRise.set(Calendar.HOUR_OF_DAY, (int)r);

    Calendar moonSet = new GregorianCalendar();
    moonSet.setTimeZone(TimeZone.getTimeZone(tz));
    moonSet.set(Calendar.YEAR, reference.get(Calendar.YEAR));
    moonSet.set(Calendar.MONTH, reference.get(Calendar.MONTH));
    moonSet.set(Calendar.DAY_OF_MONTH, reference.get(Calendar.DAY_OF_MONTH));
    moonSet.set(Calendar.SECOND, 0);

//  System.out.println("Set : TZ offset at " + moonSet.getTime() + " is " + AstroComputer.getTimeZoneOffsetInHours(TimeZone.getTimeZone(timeZone2Use /*ts.getTimeZone()*/), moonSet.getTime()));
    r = rsMoon[AstroComputer.UTC_SET_IDX] /*+ Utils.daylightOffset(moonSet)*/ + AstroComputer.getTimeZoneOffsetInHours(TimeZone.getTimeZone(tz), moonSet.getTime());
    min = (int)((r - ((int)r)) * 60);
    moonSet.set(Calendar.MINUTE, min);
    moonSet.set(Calendar.HOUR_OF_DAY, (int)r);

    moonRise.setTimeZone(TimeZone.getTimeZone(tz));
    moonSet.setTimeZone(TimeZone.getTimeZone(tz));

//   return new Object[] {moonRise, moonSet, rsMoon[AstroComputer.RISE_Z_IDX], rsMoon[AstroComputer.SET_Z_IDX]};
    return new Object[] {moonRise, moonSet};
  }
}
