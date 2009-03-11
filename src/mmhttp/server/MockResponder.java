//- Copyright ©2009 Micah Martin.  All Rights Reserved
//- MMHTTP and all included source files are distributed under terms of the GNU LGPL.

package mmhttp.server;

import mmhttp.protocol.Response;
import mmhttp.protocol.Request;
import mmhttp.protocol.SimpleResponse;

public class MockResponder implements Responder
{
  public Response makeResponse(Server server, Request request) throws Exception
  {
    return new SimpleResponse(200);
  }
}
