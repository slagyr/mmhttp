package mmhttp.server;

import mmhttp.protocol.Response;
import mmhttp.protocol.Request;

public interface ErrorResponder extends Responder
{
  Response makeResponse(Server server, Request request, Exception e) throws Exception;
}
