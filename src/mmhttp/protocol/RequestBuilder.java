//- Copyright ©2009 Micah Martin.  All Rights Reserved
//- MMHTTP and all included source files are distributed under terms of the GNU LGPL.

package mmhttp.protocol;

import mmsocketserver.StreamReader;

import java.util.*;
import java.net.URLEncoder;
import java.io.*;

/**
 * While Request is used for parsing requests, this clas is used to build request.
 */
public class RequestBuilder
{
	private static final byte[] ENDL = "\r\n".getBytes();

	private String resource;
	private String method = "GET";
	private List<InputStream> bodyParts = new LinkedList<InputStream>();
	private HashMap<String, String> headers = new HashMap<String, String>();
	private HashMap<String, Object> inputs = new HashMap<String, Object>();
	private String host;
	private int port;
	private String boundary;
	private boolean isMultipart = false;
	private int bodyLength = 0;

  /**
   * Contructs a new RequestBuilder with the specified resource.  This is sufficient for a minimal compliant HTTP
   * request.
   *
   * @param resource
   */
	public RequestBuilder(String resource)
	{
		this.resource = resource;
	}

  /**
   * Sets the HTTP method... GET, POST, etc...
   * @param method
   */
	public void setMethod(String method)
	{
		this.method = method;
	}

  /**
   * Adds a header to the request.
   * @param key
   * @param value
   */
	public void addHeader(String key, String value)
	{
		headers.put(key, value);
	}

  /**
   * @return an HTTP 1.1 compliant string representation of the request.
   * @throws Exception
   */
	public String getText() throws Exception
	{
		ByteArrayOutputStream output = new ByteArrayOutputStream();
		send(output);
		return output.toString();
	}

	private String buildRequestLine() throws Exception
	{
		StringBuffer text = new StringBuffer();
		text.append(method).append(" ").append(resource);
		if(isGet())
		{
			String inputString = queryString();
			if(inputString.length() > 0)
				text.append("?").append(inputString);
		}
		text.append(" HTTP/1.1");
		return text.toString();
	}

	private boolean isGet()
	{
		return method.equals("GET");
	}

  /**
   * Writes an HTTP 1.1 compiant representation of the request to the provided OutputStream.
   *
   * @param output
   * @throws Exception
   */
	public void send(OutputStream output) throws Exception
	{
		output.write(buildRequestLine().getBytes("UTF-8"));
		output.write(ENDL);
		buildBody();
		sendHeaders(output);
		output.write(ENDL);
		sendBody(output);
    output.flush();
	}

	private void sendHeaders(OutputStream output) throws Exception
	{
		addHostHeader();
    for(Object o : headers.keySet())
    {
      String key = (String) o;
      output.write((key + ": " + headers.get(key)).getBytes("UTF-8"));
      output.write(ENDL);
    }
	}

	private void buildBody() throws Exception
	{
		if(!isMultipart)
		{
			byte[] bytes = queryString().getBytes("UTF-8");
			bodyParts.add(new ByteArrayInputStream(bytes));
			bodyLength += bytes.length;
		}
		else
		{
      for(String name : inputs.keySet())
      {
        Object value = inputs.get(name);
        StringBuffer partBuffer = new StringBuffer();
        partBuffer.append("--").append(getBoundary()).append("\r\n");
        partBuffer.append("Content-Disposition: form-data; name=\"").append(name).append("\"").append("\r\n");
        if(value instanceof InputStreamPart)
        {
          InputStreamPart part = (InputStreamPart) value;
          partBuffer.append("Content-Type: ").append(part.contentType).append("\r\n");
          partBuffer.append("\r\n");
          addBodyPart(partBuffer.toString());
          bodyParts.add(part.input);
          bodyLength += part.size;
          addBodyPart("\r\n");
        }
        else
        {
          partBuffer.append("Content-Type: text/plain").append("\r\n");
          partBuffer.append("\r\n");
          partBuffer.append(value);
          partBuffer.append("\r\n");
          addBodyPart(partBuffer.toString());
        }
      }
			StringBuffer tail = new StringBuffer();
			tail.append("--").append(getBoundary()).append("--").append("\r\n");
			addBodyPart(tail.toString());
		}
		addHeader("Content-Length", bodyLength + "");
	}

	private void addBodyPart(String input) throws Exception
	{
		byte[] bytes = input.getBytes("UTF-8");
		bodyParts.add(new ByteArrayInputStream(bytes));
		bodyLength += bytes.length;
	}

	private void sendBody(OutputStream output) throws Exception
	{
    for(InputStream input : bodyParts)
    {
      StreamReader reader = new StreamReader(input);
      while(!reader.isEof())
      {
        byte[] bytes = reader.readBytes(1000);
        output.write(bytes);
      }
    }
	}

	private void addHostHeader()
	{
		if(host != null)
			addHeader("Host", host + ":" + port);
		else
			addHeader("Host", "");
	}

  /**
   * Add inputs that will be go in the query string.
   *
   * @param key
   * @param value
   * @throws Exception
   */
	public void addInput(String key, Object value) throws Exception
	{
		inputs.put(key, value);
	}

  /**
   * Builds the query string for this request.
   * @return query string
   * @throws Exception
   */
	public String queryString() throws Exception
	{
		StringBuffer buffer = new StringBuffer();
		boolean first = true;
    for(String key : inputs.keySet())
    {
      String value = (String) inputs.get(key);
      if(!first)
        buffer.append("&");
      buffer.append(key).append("=").append(URLEncoder.encode(value, "UTF-8"));
      first = false;
    }
		return buffer.toString();
	}

  /**
   * Adds digest authentication credentials to the request.
   *
   * @param username
   * @param password
   * @throws Exception
   */
	public void addCredentials(String username, String password) throws Exception
	{
		String rawUserpass = username + ":" + password;
		String userpass = Base64.encode(rawUserpass);
		addHeader("Authorization", "Basic " + userpass);
	}

  /**
   * Use to build the Host header.
   *
   * @param host
   * @param port
   */
	public void setHostAndPort(String host, int port)
	{
		this.host = host;
		this.port = port;
	}

  /**
   * Build a unique boundary for sparating multi-part content.
   *
   * @return a unique boundary
   */
	public String getBoundary()
	{
		if(boundary == null)
			boundary = "----------" + new Random().nextInt() + "BoUnDaRy";
		return boundary;
	}

  /**
   * Add the input as multi-part content to the request.  Once called the request becomes a multi-part request
   * and all inputs will be included in the body of the request, ie. no query string.
   *
   * @param name
   * @param content
   * @throws Exception
   */
	public void addInputAsPart(String name, Object content) throws Exception
	{
		multipart();
    addInput(name, content);
	}

  /**
   * Same as addInputAsPart(String name, Object content) except that this version should be used for large datasets
   * that wouldn't want to load into memory all at once.
   * 
   * @param name
   * @param input
   * @param size - number of bytes in the input stream
   * @param contentType - for the Content-Type header
   * @throws Exception
   */
	public void addInputAsPart(String name, InputStream input, int size, String contentType) throws Exception
	{
    addInputAsPart(name, new InputStreamPart(input, size, contentType));
	}

	private void multipart()
	{
		if(!isMultipart)
		{
			isMultipart = true;
			setMethod("POST");
			addHeader("Content-Type", "multipart/form-data; boundary=" + getBoundary());
		}
	}

  private static class InputStreamPart
	{
		public InputStream input;
		public int size;
		public String contentType;

		public InputStreamPart(InputStream input, int size, String contentType)
		{
			this.input = input;
			this.size = size;
			this.contentType = contentType;
		}
	}
}
