package nmea.ui;

public interface NMEAFrameInterface
{
  public String getPfile();
  public void setSentencesToLog(String[] sa);
  public void writeSentencesToLog(String[] sa);
}
