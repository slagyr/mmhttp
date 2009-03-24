//- Copyright ©2009 Micah Martin.  All Rights Reserved
//- MMHTTP and all included source files are distributed under terms of the GNU LGPL.

package mmhttp.protocol;

import mmsocketserver.StreamReader;

import java.io.*;

/**
 * A derivative of Response that is ideal for large data sets.  For example, if you have a large file that you wish to
 * transmit, using SimpleResponse would reqire that the entire file be loaded into memory.  Instead, use
 * InputStreamResponse and the data will be loaded and and transmitted in 1000 byte segments.
 */
public class InputStreamResponse extends Response
{
	private StreamReader reader;
	private int contentSize = 0;

  /**
   * This initiated the transmition of data.  Date will be read in 1000 bytes at a time and transmitted to the client.
   *
   * @param sender
   * @throws Exception
   */
	public void readyToSend(ResponseSender sender) throws Exception
	{
		addStandardHeaders();
		sender.send(makeHttpHeaders().getBytes());
		while(!reader.isEof())
			sender.send(reader.readBytes(1000));
		reader.close();
		sender.close();
	}

	protected void addSpecificHeaders()
	{
		addHeader("Content-Length", getContentSize() + "");
	}

  /**
   * @return The number of bytes of data to be transmitted, not including protocol overhead.
   */
	public int getContentSize()
	{
		return contentSize;
	}

  /**
   * Sets the body of the response.  There is no efficient way to count the bytes in the stream prior to sending
   * so it neccessary to provide an accurate count of bytes here.
   *
   * @param input
   * @param size - number of bytes in the stream
   */
	public void setBody(InputStream input, int size)
	{
		reader = new StreamReader(input);
		contentSize = size;
	}

  /**
   * A shorcut to send a file.  An InputStream will be created from the specified file.
   *
   * @param file
   * @throws Exception
   */
	public void setBody(File file) throws Exception
	{
		FileInputStream input = new FileInputStream(file);
		int size = (int)file.length();
		setBody(input, size);
	}
}
