//- Copyright ©2009 Micah Martin.  All Rights Reserved
//- MMHTTP and all included source files are distributed under terms of the GNU LGPL.

package mmhttp.protocol;

import mmsocketserver.StreamReader;

import java.io.*;
import java.util.*;
import java.util.regex.*;
import java.net.Socket;

/**
 * Used to parse HTTP 1.1 responses.
 */
public class ResponseParser
{
	private int status;
	private String body;
	private HashMap<String, String> headers = new HashMap<String, String>();
	private StreamReader input;

	private static final Pattern statusLinePattern = Pattern.compile("HTTP/\\d.\\d (\\d\\d\\d) ");
	private static final Pattern headerPattern = Pattern.compile("([^:]*): (.*)");

  /**
   * The Response will be read out of the provided InputStream.
   * @param input
   * @throws Exception
   */
	public ResponseParser(InputStream input) throws Exception
	{
		this.input = new StreamReader(input);
		parseStatusLine();
		parseHeaders();
		if(isChuncked())
		{
			parseChunks();
			parseHeaders();
		}
		else
			parseBody();
	}

	private boolean isChuncked()
	{
		String encoding = getHeader("Transfer-Encoding");
		return encoding != null && "chunked".equals(encoding.toLowerCase());
	}

	private void parseStatusLine() throws Exception
	{
		String statusLine = input.readLine();
		Matcher match = statusLinePattern.matcher(statusLine);
		if(match.find())
		{
			String status = match.group(1);
			this.status = Integer.parseInt(status);
		}
		else
			throw new Exception("Could not parse Response");
	}

	private void parseHeaders() throws Exception
	{
		String line = input.readLine();
		while(!"".equals(line))
		{
			Matcher match = headerPattern.matcher(line);
			if(match.find())
			{
				String key = match.group(1);
				String value = match.group(2);
				headers.put(key, value);
			}
			line = input.readLine();
		}
	}

	private void parseBody() throws Exception
	{
		String lengthHeader = "Content-Length";
		if(hasHeader(lengthHeader))
		{
			int bytesToRead = Integer.parseInt(getHeader(lengthHeader));
			body = input.read(bytesToRead);
		}
	}

	private void parseChunks() throws Exception
	{
		StringBuffer bodyBuffer = new StringBuffer();
		int chunkSize = readChunkSize();
		while(chunkSize != 0)
		{
			bodyBuffer.append(input.read(chunkSize));
			readCRLF();
			chunkSize = readChunkSize();
		}
		body = bodyBuffer.toString();

	}

	private int readChunkSize() throws Exception
	{
		String sizeLine = input.readLine();
		return Integer.parseInt(sizeLine, 16);
	}

	private void readCRLF() throws Exception
	{
		input.read(2);
	}

  /**
   * @return the status of the response
   */
	public int getStatus()
	{
		return status;
	}

  /**
   * @return the body of the response
   */
	public String getBody()
	{
		return body;
	}

  /**
   * @param key
   * @return the value of the specified header, null if it doesn't exist
   */
	public String getHeader(String key)
	{
		return headers.get(key);
	}

  /**
   * @param key
   * @return true if the parsed response contains the specified header
   */
	public boolean hasHeader(String key)
	{
		return headers.containsKey(key);
	}

  /**
   * Returns a hash of headers in the response.
   * @return
   */
  public HashMap<String, String> getHeaders()
  {
    return headers;
  }

  /**
   * @return a human readable representation of the response.
   */
	public String toString()
	{
		StringBuffer buffer = new StringBuffer();
		buffer.append("Status: ").append(status).append("\n");
		buffer.append("Headers: ").append("\n");
    for(Object o : headers.keySet())
    {
      String key = (String) o;
      buffer.append("\t").append(key).append(": ").append(headers.get(key)).append("\n");
    }
		buffer.append("Body: ").append("\n");
		buffer.append(body);
		return buffer.toString();
	}

  /**
   * A convenience method that will perform a complete HTTP request, returning a parsed response.
   * @param hostname
   * @param hostPort
   * @param builder
   * @return parsed response.
   * @throws Exception
   */
	public static ResponseParser performHttpRequest(String hostname, int hostPort, RequestBuilder builder) throws Exception
	{
		Socket socket = new Socket(hostname, hostPort);
		OutputStream socketOut = socket.getOutputStream();
		InputStream socketIn = socket.getInputStream();
		builder.send(socketOut);
		socketOut.flush();
		ResponseParser parser = new ResponseParser(socketIn);
		socket.close();
		return parser;
	}
}
