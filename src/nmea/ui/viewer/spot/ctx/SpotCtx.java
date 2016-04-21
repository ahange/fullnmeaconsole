package nmea.ui.viewer.spot.ctx;

import java.util.ArrayList;
import java.util.List;

public class SpotCtx
{
  private static SpotCtx context = null;
  private List<SpotEventListener> applicationListeners = null;
  
  private SpotCtx()
  {
    applicationListeners = new ArrayList<SpotEventListener>();
  }
  
  public static synchronized SpotCtx getInstance()
  {
    if (context == null)
      context = new SpotCtx();
    return context;
  }
  
  public void release()
  {
    context = null;
    System.gc();
  }

  public List<SpotEventListener> getListeners()
  {
    return applicationListeners;
  }
  
  public synchronized void addApplicationListener(SpotEventListener l)
  {
    if (!this.getListeners().contains(l))
    {      
      this.getListeners().add(l);
  //    System.out.println("Now having " + Integer.toString(this.getListeners().size()) + " listener(s)");
    }
  }

  public synchronized void removeApplicationListener(SpotEventListener l)
  {
    this.getListeners().remove(l);
  }
  
  public void fireInternalFrameClosed()
  {
    for (int i=0; i < this.getListeners().size(); i++)
    {
      SpotEventListener l = this.getListeners().get(i);
      l.internalFrameClosed();
    }    
  }    
}