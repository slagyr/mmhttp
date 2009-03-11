//- Copyright ©2009 Micah Martin.  All Rights Reserved
//- MMHTTP and all included source files are distributed under terms of the GNU LGPL.

package mmhttp.protocol;

import java.nio.ByteBuffer;

public class SimpleResponse extends Response
{
	private byte[] content = new byte[0];

	public SimpleResponse()
	{
	}

	public SimpleResponse(int status)
	{
		super(status);
	}

	public void readyToSend(ResponseSender sender) throws Exception
	{
		byte[] bytes = getBytes();
		sender.send(bytes);
		sender.close();
	}

	public void setContent(String value) throws Exception
	{
		content = getEncodedBytes(value);
	}

	public void setContent(byte[] value)
	{
		content = value;
	}

	public String getContent()
	{
		return new String(content);
	}

	public byte[] getContentBytes()
	{
		return content;
	}

	public String getText()
	{
		return new String(getBytes());
	}

	public byte[] getBytes()
	{
		addStandardHeaders();
		byte[] headerBytes = makeHttpHeaders().getBytes();
		ByteBuffer bytes = ByteBuffer.allocate(headerBytes.length + getContentSize());
		bytes.put(headerBytes).put(content);
		return bytes.array();
	}

	public int getContentSize()
	{
		return content.length;
	}

	protected void addSpecificHeaders()
	{
		addHeader("Content-Length", String.valueOf(getContentSize()));
	}
}