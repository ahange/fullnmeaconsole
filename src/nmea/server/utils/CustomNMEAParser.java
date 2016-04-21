package nmea.server.utils;

public interface CustomNMEAParser
{
  public Object customParse(String sentence);
  public String getCacheID(String sentence);
}
