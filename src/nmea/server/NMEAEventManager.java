package nmea.server;

public interface NMEAEventManager
{
  public abstract void manageDataEvent(String s);
  public abstract boolean verbose();
}
