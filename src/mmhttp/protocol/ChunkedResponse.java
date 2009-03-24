//- Copyright ©2009 Micah Martin.  All Rights Reserved
//- MMHTTP and all included source files are distributed under terms of the GNU LGPL.

package mmhttp.protocol;

import java.nio.ByteBuffer;

/**
 * This a special type of Response that supports HTTP 1.1 Chunking.
 *
 * A snippet from the HTTP 1.1 RFC:
 *
 * <i><pre>
 * The chunked encoding modifies the body of a message in
 * order to transfer it as a series of chunks, each with its
 * own size indicator, followed by an OPTIONAL trailer
 * containing entity-header fields. This allows dynamically
 * produced content to be transferred along with the
 * information necessary for the recipient to verify that it
 * has received the full message.
 * </pre></i>
 * 
 */
public class ChunkedResponse extends Response
{
	private ResponseSender sender;
	private int bytesSent = 0;
	private boolean isReadyToSend = false;

  /**
   * Begins the transfer of the HTTP response.  The standard HTTP headers will be delievered upon calling this method.
   *
   * @param sender is responsible for transmitting the data
   * @throws Exception
   */
	public void readyToSend(ResponseSender sender) throws Exception
	{
		this.sender = sender;
		addStandardHeaders();
		sender.send(makeHttpHeaders().getBytes());
		isReadyToSend = true;
		synchronized(this)
		{
			notify();
		}
	}

  /**
   * @return true if the response is accepting data to be transmitted.
   */
	public boolean isReadyToSend()
	{
		return isReadyToSend;
	}

	protected void addSpecificHeaders()
	{
		addHeader("Transfer-Encoding", "chunked");
	}

	private static String asHex(int value)
	{
		return Integer.toHexString(value);
	}

  /**
   * Sends a chunk of text to the client.  The string will be converted to bytes prior to transfer.
   *
   * @param text
   * @throws Exception
   */
	public void add(String text) throws Exception
	{
		if(text != null)
			add(getEncodedBytes(text));
	}

  /**
   * Sends a chunk of data to the client.
   *
   * @param bytes
   * @throws Exception
   */
	public void add(byte[] bytes) throws Exception
	{
		if(bytes == null || bytes.length == 0)
			return;
		String sizeLine = asHex(bytes.length) + CRLF;
		ByteBuffer chunk = ByteBuffer.allocate(sizeLine.length() + bytes.length + 2);
		chunk.put(sizeLine.getBytes()).put(bytes).put(CRLF.getBytes());
		sender.send(chunk.array());
		bytesSent += bytes.length;
	}

  /**
   * Trailing headers are sent to the client after all the chunks have been delievered.  This method does not enforce
   * this requirement so you must make sure not to use this methon until after calling closeChunks().
   *
   * @param key
   * @param value
   * @throws Exception
   *
   * @see #closeChunks
   */
	public void addTrailingHeader(String key, String value) throws Exception
	{
		String header = key + ": " + value + CRLF;
		sender.send(header.getBytes());
	}

  /**
   * Call this method when you are through sending all the chunks.
   *
   * @throws Exception
   */
	public void closeChunks() throws Exception
	{
		sender.send( ("0" + CRLF).getBytes() );
	}

  /**
   * Call this method when you're finished sending trailing headers.
   *
   * @throws Exception
   *
   * @see #addTrailingHeader
   */
	public void closeTrailer() throws Exception
	{
		sender.send(CRLF.getBytes());
	}

  /**
   * Call this method to conclude the response.
   *
   * @throws Exception
   */
	public void close() throws Exception
	{
		sender.close();
	}

  /**
   * This is a short cut to be used if there are not traling headers.  Call it when you are through sending chunks.
   *
   * @throws Exception
   */
	public void closeAll() throws Exception
	{
		closeChunks();
		closeTrailer();
		close();
	}

  /**
   * @return the number of bytes sent as chunks.  It is the amount of pure data transfered not including all the
   * protocol overhead.
   */
	public int getContentSize()
	{
		return bytesSent;
	}
}
