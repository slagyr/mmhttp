//- Copyright ©2009 Micah Martin.  All Rights Reserved
//- MMHTTP and all included source files are distributed under terms of the GNU LGPL.

package mmhttp.protocol;


/**
 * This class supports Base 64 encoding and decoding.  In this library it is primarily used for HTTP digest authentication.
 *
 * <pre>
 * RFC 2045 - Multipurpose Internet Mail Extensions (MIME) Part One:
 * Format of Internet Message Bodies
 * section 6.8.  Base64 Content-Transfer-Encoding
 * The encoding process represents 24-bit groups of input bits as output
 * strings of 4 encoded characters.  Proceeding from left to right, a
 * 24-bit input group is formed by concatenating 3 8bit input groups.
 * These 24 bits are then treated as 4 concatenated 6-bit groups, each
 * of which is translated into a single digit in the base64 alphabet.
 * When encoding a bit stream via the base64 encoding, the bit stream
 * must be presumed to be ordered with the most-significant-bit first.
 * That is, the first bit in the stream will be the high-order bit in
 * the first 8bit byte, and the eighth bit will be the low-order bit in
 * the first 8bit byte, and so on.
 * </pre>
 *
 */
public class Base64
{
	private static final byte[] base64Alphabet = "ABCDEFGHIJKLMNOPQRSTUVWXYZabcdefghijklmnopqrstuvwxyz0123456789+/".getBytes();
	private static final byte pad = '=';


  /**
   * Decodes a block of Base 64 encoded text.
   *
   * @param text to be decoded
   * @return decoded text
   * @throws Exception
   */
	public static String decode(String text) throws Exception
	{
		return new String(decode(text.getBytes("UTF-8")));
	}

  /**
   * Decodes an array of encoded bytes.
   *
   * @param bytes to be decoded
   * @return decoded bytes
   */
	public static byte[] decode(byte[] bytes)
	{
		int lengthOfDecoding = getLengthOfDecoding(bytes);
		byte[] decoding = new byte[lengthOfDecoding];
		int decodingIndex = 0;

		for(int index = 0; index < bytes.length; index += 4)
		{
			byte v1 = getValueFor(bytes[index]);
			byte v2 = getValueFor(bytes[index + 1]);
			byte v3 = getValueFor(bytes[index + 2]);
			byte v4 = getValueFor(bytes[index + 3]);

			byte c1 = (byte)((v1 << 2) + (v2 >> 4));
			byte c2 = (byte)((v2 << 4) + (v3 >> 2));
			byte c3 = (byte)((v3 << 6) + v4);

			decoding[decodingIndex++] = c1;
			if(c2 != 0)
				decoding[decodingIndex++] = c2;
			if(c3 != 0)
				decoding[decodingIndex++] = c3;
		}
		return decoding;
	}

  /**
   * Encodes a block of text.
   *
   * @param text to encode
   * @return encoded text
   * @throws Exception
   */
	public static String encode(String text) throws Exception
	{
		return new String(encode(text.getBytes()));
	}

  /**
   * Encodes an array of bytes.
   *
   * @param bytes to encode
   * @return encoded bytes
   */
	public static byte[] encode(byte[] bytes)
	{
		int inputLength = bytes.length;

		int lengthOfEncoding = getLengthOfEncoding(bytes);
		byte[] encoding = new byte[lengthOfEncoding];
		int encodingIndex = 0;

		int index = 0;
		while(index < inputLength)
		{
			byte c1 = bytes[index++];
			byte c2 = index >= inputLength ? 0 : bytes[index++];
			byte c3 = index >= inputLength ? 0 : bytes[index++];

			byte v1 = abs((byte)(c1 >> 2));
			byte v2 = abs((byte)(((c1 << 4) & 63) + (c2 >> 4)));
			byte v3 = abs((byte)(((c2 << 2) & 63) + (c3 >> 6)));
			byte v4 = abs((byte)(c3 & 63));

			encoding[encodingIndex++] = base64Alphabet[v1];
			encoding[encodingIndex++] = base64Alphabet[v2];
			if(v3 != 0)
				encoding[encodingIndex++] = base64Alphabet[v3];
			else
				encoding[encodingIndex++] = '=';
			if(v4 != 0)
				encoding[encodingIndex++] = base64Alphabet[v4];
			else
				encoding[encodingIndex++] = '=';
		}
		return encoding;
	}

	private static int getLengthOfDecoding(byte[] bytes)
	{
		if(bytes.length == 0)
			return 0;

		int lengthOfOutput = (int)(bytes.length * .75);

		for(int i = bytes.length - 1; bytes[i] == pad; i--)
				lengthOfOutput--;

		return lengthOfOutput;
	}

	private static int getLengthOfEncoding(byte[] bytes)
	{
		boolean needsPadding = (bytes.length % 3 != 0);

		int length = ((int)(bytes.length / 3)) * 4;
		if(needsPadding)
			length += 4;

		return length;
	}

  /**
   * A helper method which is public only for testing purposes.
   *
   * @param b
   * @return byte
   */
	public static byte getValueFor(byte b)
	{
		if(b == pad)
			return (byte)0;
		for(int i = 0; i < base64Alphabet.length; i++)
		{
			if(base64Alphabet[i] == b)
				return (byte)i;
		}
		return -1;
	}

	private static byte abs(byte b)
	{
		return (byte)Math.abs(b);
	}
}
