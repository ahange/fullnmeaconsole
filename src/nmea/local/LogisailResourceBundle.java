package nmea.local;
import java.util.MissingResourceException;
import java.util.ResourceBundle;

public class LogisailResourceBundle 
{
  private static String baseName = "nmea.local.logisail";
  private static ResourceBundle resourceBundle;
  
  private LogisailResourceBundle()
  {
  }
  
  public static synchronized ResourceBundle getLogisailResourceBundle()
  {
    if (resourceBundle == null)
    {
      try
      {
        resourceBundle = ResourceBundle.getBundle(baseName);
//      System.out.println("ResourceBundle created");
      }
      catch (MissingResourceException mre)
      {
        System.err.println("Missing Resource:" + mre.getMessage());
      }
    }
//  else
//    System.out.println("ResourceBundle reused");
    return resourceBundle;
  }
  
  /**
   * Builds a regular message based on the id in the resource bundle.
   */
  public static String buildMessage(String id)
  {
    return LogisailResourceBundle.getLogisailResourceBundle().getString(id); 
  }

  /**
   * Builds a patched message.
   * The id is the id of the message in the resource bundle, which must look like
   * "akeu coucou {$1} larigou {$2}"
   * In that case, data must be 2 entries long, the first entry will patch {$1},
   * the second one {$2}.
   * 
   * Note:
   * If there is a string like "xx {$1} and {$1}, {$2}",
   * data needs to be only 2 entries big.
   */
  public static String buildMessage(String id, String[] data)
  {
    String mess = LogisailResourceBundle.getLogisailResourceBundle().getString(id); 
    for (int i=0; i<data.length; i++)
    {
      String toReplace = "{$" + Integer.toString(i+1) + "}";
      mess = replaceString(mess, toReplace, data[i]);
    }
    return mess;
  }
  
  public static String replaceString(String orig, String oldStr, String newStr)
  {
    String ret = orig;
    int indx = 0;
    for (boolean go = true; go;)
    {
      indx = ret.indexOf(oldStr, indx);
      if (indx < 0)
      {
        go = false;
      } 
      else
      {
        ret = ret.substring(0, indx) + newStr + ret.substring(indx + oldStr.length());
        indx += 1 + oldStr.length();
      }
    }
    return ret;
  }  
}