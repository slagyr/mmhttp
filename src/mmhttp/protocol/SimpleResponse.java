//- Copyright ©2009 Micah Martin.  All Rights Reserved
//- MMHTTP and all included source files are distributed under terms of the GNU LGPL.

package mmhttp.protocol;

import java.nio.ByteBuffer;

/**
 * The most basic implementation of Response.  Just set the status, headers, and content of the response and it's ready
 * to go.  The content of SimpleResponse is stored in memory so it should not be used for large data sets.
 */
public class SimpleResponse extends Response
{
	private byte[] content = new byte[0];

  /**
   * Default constructor with no configuration.
   */
	public SimpleResponse()
	{
	}

  /**
   * A convenience contructor that will set the port.
   * @param status
   */
	public SimpleResponse(int status)
	{
		super(status);
	}

  /**
   * A convenience contructor that will set the port and content.
   * @param status
   * @param content
   * @throws Exception
   */
	public SimpleResponse(int status, String content) throws Exception
	{
		super(status);
    setContent(content);
	}

  /**
   * Converts the response into HTTP compliant bytes and sends them through the ResponseSender.
   * @param sender
   * @throws Exception
   */
	public void readyToSend(ResponseSender sender) throws Exception
	{
		byte[] bytes = getBytes();
		sender.send(bytes);
		sender.close();
	}

  /**
   * Sets the content (body) of the response. The provided string will be converted into bytes using the UTF-8 encoding.
   * @param value
   * @throws Exception
   */
	public void setContent(String value) throws Exception
	{
		content = getEncodedBytes(value);
	}

  /**
   * Sets the content (body) of the response.
   * @param value
   */
	public void setContent(byte[] value)
	{
		content = value;
	}

  /**
   * @return the content of the response as a String.
   */
	public String getContent()
	{
		return new String(content);
	}

  /**
   * @return the raw bytes of the response body.
   */
	public byte[] getContentBytes()
	{
		return content;
	}

  /**
   * The HTTP response bytes are generated and converted into a String.
   * @return HTTP response string.
   */
	public String getText()
	{
		return new String(getBytes());
	}

  /**
   * The HTTP response bytes are generated and returned.
   * @return HTTP response bytes
   */
	public byte[] getBytes()
	{
		addStandardHeaders();
		byte[] headerBytes = makeHttpHeaders().getBytes();
		ByteBuffer bytes = ByteBuffer.allocate(headerBytes.length + getContentSize());
		bytes.put(headerBytes).put(content);
		return bytes.array();
	}

  /**
   * @return the size of the content (body).  Used in the Content-Length header.
   */
	public int getContentSize()
	{
		return content.length;
	}

	protected void addSpecificHeaders()
	{
		addHeader("Content-Length", String.valueOf(getContentSize()));
	}
}