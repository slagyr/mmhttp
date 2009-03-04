package mmhttp.server;

import mmhttp.protocol.Response;
import mmhttp.protocol.Request;

public interface Responder
{
  Response makeResponse(Server server, Request request) throws Exception;
}
