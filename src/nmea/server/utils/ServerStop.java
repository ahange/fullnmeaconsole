package nmea.server.utils;

import java.io.OutputStream;
import java.io.BufferedReader;
import java.io.InterruptedIOException;
import java.io.InputStreamReader;
import java.io.IOException;
import java.io.PrintWriter;
import java.io.InputStream;
import java.net.Socket;
import java.net.UnknownHostException;

/**
 * Stop the server from a client request
 */
public class ServerStop
{
  // The Socket parameters
  private int timeout;

  /** HTTP request-headers */
  private String httpAccept;
  private String httpUserAgent;

  /** Statistics data */
  private long totalRequestTime;
  private long totalRequests;
  private long timeouts;

  private String extraParameters = null;

  public ServerStop()
  {
    String timeoutStr = "0";
    try
    {
      timeout = Integer.parseInt(timeoutStr);
    }
    catch (NumberFormatException ex)
    {
      System.out.println("Invalid timeout interval");
      timeout = 0;
    }
    httpAccept = "text/plain, text/html, text/xml, text/*";

    totalRequestTime = 0;
    totalRequests = 0;
    timeouts = 0;
  }

  // NOTE: This method should be synchronized. But for now it will be called
  // only once and it's guaranteed that there are no race conditions
  public long getTotalRequestTime()
  {
    return totalRequestTime;
  }

  // NOTE: This method should be synchronized. But for now it will be called
  // only once and it's guaranteed that there are no race conditions
  public long getTotalRequests()
  {
    return totalRequests;
  }

  // NOTE: This method should be synchronized. But for now it will be called
  // only once and it's guaranteed that there are no race conditions
  public long getTimeouts()
  {
    return timeouts;
  }

  public String getResponse(String host,
                            String portStr,
                            String paramURL)
  {
    int port = 80;
    try
    {
      port = Integer.parseInt(portStr);
    }
    catch (NumberFormatException ex)
    {
      System.out.println("Invalid port number");
      port = 80;
    }

    StringBuffer buffer = new StringBuffer();
    // POST /mapviewer/omserver?xml_request=<xml/> HTTP/1.0
    buffer.append("POST ");
    buffer.append(paramURL);
    // native line endings are uncertain so add them manually
    buffer.append(" HTTP/1.0\r\n");
    String result = sendReceive(host, port, buffer);
    return result;
  }
  
  private String sendReceive(String host,
                             int port,
                             StringBuffer buffer)
  {
    // GET XXXX HTTP/1.0
    // Accept: xxxx
    // User-Agent: yyyy

    // Accept: text/plain, text/html, text/*
    buffer.append("Accept: ");
    buffer.append(httpAccept);
    buffer.append("\r\n");
    // User-Agent: Whatever
    buffer.append("User-Agent: ");
    buffer.append(httpUserAgent);
    buffer.append("\r\n");
    buffer.append("\r\n");
    String request = buffer.toString();

//  System.out.println("HTTP Request:[" + request + "]");

    long startTime = System.currentTimeMillis();
    String result = "";

    Socket socket = null;
    try
    {
      socket = new Socket(host, port);
    }
    catch (UnknownHostException ex)
    {
      System.out.println("Invalid hostname: " + host);
      return result;
    }
    catch (IOException ex)
    {
      System.out.println("Can't connect to hostname: " + host);
      return result;
    }

    try
    {
      socket.setSoTimeout(timeout);
    }
    catch (IOException ex)
    {
      System.out.println("Can't set the socket timeout interval");
    }

    OutputStream out = null;
    try
    {
      out = socket.getOutputStream();
    }
    catch (IOException ex)
    {
      System.out.println("Can't get the socket output stream");
      try
      {
        socket.close();
      }
      catch (IOException ex1) {}
      return result;
    }

    // Send the request to the server
    // no auto-flushing
    PrintWriter pw = new PrintWriter(out, false);
    pw.print(request);
    pw.flush();

    // Receive the server response
    InputStream in = null;
    try
    {
      in = socket.getInputStream();
    }
    catch (IOException ex)
    {
      System.out.println("Can't get the socket input stream");
      // close the socket before exiting
      try
      {
        pw.close();
        out.close();
        socket.close();
      }
      catch (IOException ex1) {}
      return result;
    }
    InputStreamReader isr = new InputStreamReader(in);
    BufferedReader br = new BufferedReader(isr);
    StringBuffer buff = new StringBuffer();
    int c = -1;
    try
    {
      while ((c = br.read()) != -1)
      {
        buff.append((char) c);
      }
    }
    catch (InterruptedIOException ex)
    {
      System.out.println("Connection time-out");
      System.out.println("Error during receiving the server response");
      timeouts++;
    }
    catch (IOException ignore)
    {
      // Will happen when the connection has been cut, 
      // which is the goal of this program.
    }
    result = buff.toString();

    // Close everything in a reverse order
    try
    {
      br.close();
      isr.close();
      in.close();
      pw.close();
      out.close();
      socket.close();
    }
    catch (IOException ex)
    {
      System.out.println("Error closing the connection");
    }

    long endTime = System.currentTimeMillis();

    // Update the statistics data
    long requestTime = endTime - startTime;
//  System.out.println("The request time for request no: " + totalRequests + " is: " + requestTime);

    totalRequestTime += requestTime;
    totalRequests++;

    return result;
  }

  public static void main(String[] args)
  {
    System.out.println("Usage:");
    System.out.println("java -Dhttp.port=6666 -Dhttp.host=localhost " + new ServerStop().getClass().getName());
    try
    {
      ServerStop client = new ServerStop();
      String port = System.getProperty("http.port", "6666");
      String host = System.getProperty("http.host", "localhost");
      String resp = client.getResponse(host, port, "/exit");
    }
    catch (Exception e)
    {
      e.printStackTrace();
    }
  }
}
