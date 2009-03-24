//- Copyright ©2009 Micah Martin.  All Rights Reserved
//- MMHTTP and all included source files are distributed under terms of the GNU LGPL.

package mmhttp.protocol;

import java.util.*;
import java.text.*;

/**
 * The base class for building HTTP 1.1 compliant responses.
 */
public abstract class Response
{
  public static final String DEFAULT_CONTENT_TYPE = "text/html; charset=utf-8";

	protected static final String CRLF = "\r\n";

  /**
   * HTTP Protocol fluff.
   * @return an HTTP date format
   */
  public static SimpleDateFormat makeStandardHttpDateFormat()
  {
    //SimpleDateFormat is not thread safe, so we need to create each instance independently.
    SimpleDateFormat df = new SimpleDateFormat("EEE, dd MMM yyyy HH:mm:ss z");
    df.setTimeZone(TimeZone.getTimeZone("GMT"));
    return df;
  }

  private int status = 200;
	private HashMap headers = new HashMap(17);
  private String contentType = DEFAULT_CONTENT_TYPE;

  /**
   * Empty constructor.
   */
	public Response()
	{
	}

  /**
   * A constructor that sets the status of the response.
   * @param status
   */
	public Response(int status)
	{
		this.status = status;
	}

  /**
   * To be called when response has been prepared and is ready to go out the door.
   * @param sender
   * @throws Exception
   */
	public abstract void readyToSend(ResponseSender sender) throws Exception;
	protected abstract void addSpecificHeaders();

  /**
   * A method to get the size of the response content.  Abstract because, who know how the response is built?
   * @return number of bytes in content
   */
	public abstract int getContentSize();

  /**
   * @return the status
   */
	public int getStatus()
	{
		return status;
	}

  /**
   * Sets the status of the response
   * @param status
   */
	public void setStatus(int status)
	{
		this.status = status;
	}

  /**
   * Build the headers portion of the response.
   * @return headers string
   */
	public String makeHttpHeaders()
	{
		StringBuffer text = new StringBuffer();
		text.append("HTTP/1.1 ").append(status).append(" ").append(getReasonPhrase()).append(CRLF);
		makeHeaders(text);
		text.append(CRLF);
		return text.toString();
	}

  /**
   * @return the Content-Type header of the response
   */
	public String getContentType()
	{
		return contentType;
	}

  /**
   * Sets the Content-Type header.
   * @param type
   */
	public void setContentType(String type)
	{
		contentType = type;
	}

  /**
   * Configures this response to be a redirect to the specified URL.
   * @param location
   */
	public void redirect(String location)
	{
		status = 303;
		addHeader("Location", location);
	}

  /**
   * Configures the response for chaching by setting the Cache-Control header to 'max-age=&lt;age&gt;'.
   * This response should be considered fresh, by the client, for &lt;age&gt; seconds.
   * @param age
   */
	public void setMaxAge(int age)
	{
		addHeader("Cache-Control", "max-age=" + age);
	}

  /**
   * Tells the client when the requested resource was last modified.  Sets the Last-Modified header.
   * @param date
   */
	public void setLastModifiedHeader(String date)
	{
		addHeader("Last-Modified", date);
	}

  /**
   * Gives the response an expiration data.  Sets the Expires header.
   * @param date
   */
	public void setExpiresHeader(String date)
	{
		addHeader("Expires", date);
	}

  /**
   * Add an header to the response.
   * @param key
   * @param value
   */
	public void addHeader(String key, String value)
	{
		headers.put(key, value);
	}

  /**
   * Get the value of a header already set on this response.  Will return null if the specified header is not contained.
   * @param key
   * @return header value
   */
	public String getHeader(String key)
	{
		return (String) headers.get(key);
	}

  /**
   * @param value
   * @return a byte array representation of value encoded in UTF-8.
   * @throws Exception
   */
	public byte[] getEncodedBytes(String value) throws Exception
	{
		return value.getBytes("UTF-8");
	}

	private void makeHeaders(StringBuffer text)
	{
		for(Iterator iterator = headers.keySet().iterator(); iterator.hasNext();)
		{
			String key = (String) iterator.next();
			String value = (String) headers.get(key);
			text.append(key).append(": ").append(value).append(CRLF);
		}
	}

	protected void addStandardHeaders()
	{
		addHeader("Content-Type", getContentType());
		addSpecificHeaders();
	}

	protected String getReasonPhrase()
	{
		return getReasonPhrase(status);
	}

  /**
   * Provideds the HTTP description for each response status code.
   * @param status
   * @return response phrase
   */
	public static String getReasonPhrase(int status)
	{
		switch(status)
		{
		case 100:
			return "Continue";
		case 101:
			return "Switching Protocols";
		case 200:
			return "OK";
		case 201:
			return "Created";
		case 202:
			return "Accepted";
		case 203:
			return "Non-Authoritative Information";
		case 204:
			return "No Content";
		case 205:
			return "Reset Content";
		case 300:
			return "Multiple Choices";
		case 301:
			return "Moved Permanently";
		case 302:
			return "Found";
		case 303:
			return "See Other";
		case 304:
			return "Not Modified";
		case 305:
			return "Use Proxy";
		case 307:
			return "Temporary Redirect";
		case 400:
			return "Bad Request";
		case 401:
			return "Unauthorized";
		case 402:
			return "Payment Required";
		case 403:
			return "Forbidden";
		case 404:
			return "Not Found";
		case 405:
			return "Method Not Allowed";
		case 406:
			return "Not Acceptable";
		case 407:
			return "Proxy Authentication Required";
		case 408:
			return "Request Time-out";
		case 409:
			return "Conflict";
		case 410:
			return "Gone";
		case 411:
			return "Length Required";
		case 412:
			return "Precondition Failed";
		case 413:
			return "Request Entity Too Large";
		case 414:
			return "Request-URI Too Large";
		case 415:
			return "Unsupported Media Type";
		case 416:
			return "Requested range not satisfiable";
		case 417:
			return "Expectation Failed";
		case 500:
			return "Internal Server Error";
		case 501:
			return "Not Implemented";
		case 502:
			return "Bad Gateway";
		case 503:
			return "Service Unavailable";
		case 504:
			return "Gateway Time-out";
		case 505:
			return "HTTP Version not supported";
		default:
			return "Unknown Status";
		}
	}
}

