//- Copyright ©2009 Micah Martin.  All Rights Reserved
//- MMHTTP and all included source files are distributed under terms of the GNU LGPL.

package mmhttp.protocol;

import junit.framework.TestCase;

public class Base64Test extends TestCase
{
	public void setUp() throws Exception
	{
	}

	public void tearDown() throws Exception
	{
	}

	public void testGetValueFor() throws Exception
	{
		assertEquals(0, Base64.getValueFor((byte)'A'));
		assertEquals(26, Base64.getValueFor((byte)'a'));
		assertEquals(52, Base64.getValueFor((byte)'0'));
	}

	public void testDecodeNothing() throws Exception
	{
		assertEquals("", Base64.decode(""));
	}

	public void testDecodeOneChar() throws Exception
	{
		assertEquals("a", Base64.decode("YQ=="));
	}

	public void testDecodeTwoChars() throws Exception
	{
		assertEquals("a:", Base64.decode("YTo="));
	}

	public void testDecodeLongSample() throws Exception
	{
		assertEquals("Aladdin:open sesame", Base64.decode("QWxhZGRpbjpvcGVuIHNlc2FtZQ=="));
	}

	public void testEncodeNothing() throws Exception
	{
		assertEquals("", Base64.encode(""));
	}

	public void testEncodeOneChar() throws Exception
	{
		assertEquals("YQ==", Base64.encode("a"));
	}

	public void testEncodeTwoChars() throws Exception
	{
		assertEquals("YTo=", Base64.encode("a:"));
	}

	public void testEncodeThreeChars() throws Exception
	{
		assertEquals("YWJj", Base64.encode("abc"));
	}

	public void testEncodeLongSample() throws Exception
	{
		assertEquals("QWxhZGRpbjpvcGVuIHNlc2FtZQ==", Base64.encode("Aladdin:open sesame"));
	}
}

