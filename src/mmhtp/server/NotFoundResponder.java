package mmhtp.server;

import mmhtp.protocol.Response;
import mmhtp.protocol.Request;
import mmhtp.protocol.SimpleResponse;

public class NotFoundResponder implements Responder
{
	public Response makeResponse(Server server, Request request) throws Exception
	{
		SimpleResponse response = new SimpleResponse(404);
		response.setContent(makeHtml());
		return response;
	}

	private String makeHtml() throws Exception
	{
    return "<html><head><title>Not Found</title></head><body>Not Found</body></html>";
	}

}
