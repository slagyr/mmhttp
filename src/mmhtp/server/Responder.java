package mmhtp.server;

import mmhtp.protocol.Response;
import mmhtp.protocol.Request;

public interface Responder
{
  Response makeResponse(Server server, Request request) throws Exception;
}
