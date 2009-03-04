package mmhtp.server;

import mmhtp.protocol.Response;
import mmhtp.protocol.Request;
import mmhtp.protocol.SimpleResponse;


public class UnauthorizedResponder implements Responder
{
	public Response makeResponse(Server server, Request request) throws Exception
	{
		SimpleResponse response = new SimpleResponse(401);
		response.addHeader("WWW-Authenticate", "Basic realm=\"" + server.realm + "\"");

		response.setContent("<html><head><title>Unauthorized</title></head><body>Unauthorized</body></html>");

		return response;
	}
}
