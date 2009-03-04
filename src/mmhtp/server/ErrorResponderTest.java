package mmhtp.server;

import mmhtp.protocol.MockRequest;
import mmhtp.protocol.SimpleResponse;
import junit.framework.TestCase;
import static mmhtp.testutil.RegexTest.*;

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
