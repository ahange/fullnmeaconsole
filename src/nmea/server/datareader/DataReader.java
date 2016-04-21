package nmea.server.datareader;

public interface DataReader
{
  public void read();
  public void closeReader() throws Exception;
  public void manageError(Throwable t);
  public void setTimeout(long timeout);
}
