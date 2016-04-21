package nmea.event;

import java.util.ArrayList;
import java.util.EventListener;
import java.util.Hashtable;
import java.util.List;

import nmea.ui.viewer.spot.utils.SpotParser.SpotLine;

import ocss.nmea.parser.GeoPos;

/*
 * Specific listener for the NMEA Console.
 */
public abstract class NMEAReaderListener implements EventListener
{
  private String groupID = "";
  private String name = "";
  
  public NMEAReaderListener() 
  { this("default"); }
  public NMEAReaderListener(String gID)
  { this(gID, "default"); }
  public NMEAReaderListener(String gID, String listenerName)
  { 
    if ("true".equals(System.getProperty("verbose", "false")))
      System.out.println(" -- Creating " + this.getClass().getName() + ", group " + gID);
    this.groupID = gID; 
    this.name = listenerName;
  }
  public final String getGroupID() { return groupID; }
  public final String getListenerName() { return name; }
  
  public void log(boolean b) {}
  public void log(boolean log, boolean withDateTime) {}
  public void setMessage(String s) {}
  public void manageNMEAString(String str) {}
  public void saveUserConfig() {}
  
  public void internalFrameClosed() {}
  public void internalTxFrameClosed() {}
  public void internalAnalyzerFrameClosed() {}
  
  public void dataUpdate() {}
  public void loadDataPointsForDeviation(List<double[]> dp) {}
  public void deviationCurveChanged(Hashtable<Double, Double> devCurve) {}
  public void replaySpeedChanged(int slider) {}
  public void dataBufferSizeChanged(int size) {}
  
  public void stopReading() throws Exception {}
  public void jumpToOffset(long recordOffset) {}

  public void refreshLogJournal() {}
  public void showRawData(boolean b) {}
  
  public void fireError(Throwable t) {}
  
  public void setAutoScale(boolean b) {}
  public void setWindScale(float f) {}
  public void dampingHasChanged(int damping) {}
  
  public void positionManuallyUpdated(GeoPos gp) {}
  public void newSpotData(List<SpotLine> spotLines, GeoPos pos) {}
  public void setSpotLineIndex(int i) {}
  
  public void enableHttpServer(boolean b) {}
  public void loggedDataAnalyzerFrameClosed(String id) {}
}
