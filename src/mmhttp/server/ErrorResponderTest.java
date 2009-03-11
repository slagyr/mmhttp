//- Copyright ©2009 Micah Martin.  All Rights Reserved
//- MMHTTP and all included source files are distributed under terms of the GNU LGPL.

package mmhttp.server;

import mmhttp.protocol.MockRequest;
import mmhttp.protocol.SimpleResponse;
import junit.framework.TestCase;
import static mmhttp.testutil.RegexTest.*;

public class ErrorResponderTest extends TestCase
{
	public void testResponse() throws Exception
	{
		Responder responder = new ErrorResponder(new Exception("some error message"));
		SimpleResponse response = (SimpleResponse)responder.makeResponse(new Server(), new MockRequest());

		assertEquals(400, response.getStatus());

		String body = response.getContent();

		assertHasRegexp("<html>", body);
		assertHasRegexp("<body", body);
		assertHasRegexp("java.lang.Exception: some error message", body);
	}

	public void testWithMessage() throws Exception
	{
		Responder responder = new ErrorResponder("error Message");
		SimpleResponse response = (SimpleResponse)responder.makeResponse(new Server(), new MockRequest());
		String body = response.getContent();

		assertSubString("error Message", body);
	}
}
