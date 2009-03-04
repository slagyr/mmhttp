package mmhtp.server;

import mmhtp.protocol.Response;
import mmhtp.protocol.Request;
import mmhtp.protocol.SimpleResponse;

public class MockResponder implements Responder
{
  public Response makeResponse(Server server, Request request) throws Exception
  {
    return new SimpleResponse(200);
  }
}
