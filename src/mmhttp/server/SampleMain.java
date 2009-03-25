//- Copyright ©2009 Micah Martin.  All Rights Reserved
//- MMHTTP and all included source files are distributed under terms of the GNU LGPL.

package mmhttp.server;

import mmhttp.protocol.Response;
import mmhttp.protocol.Request;
import mmhttp.protocol.SimpleResponse;

public class SampleMain
{
  public static void main(String[] args) throws Exception
  {
    Server server = new Server(8002);
    server.register("hello.*", HelloResponder.class);
    server.start();
  }

  public static class HelloResponder implements Responder
  {
    public Response makeResponse(Server server, Request request) throws Exception
    {
      SimpleResponse response = new SimpleResponse(200);
      response.setContent("<h1>Hello World!</h1>");
      return response;
    }
  }
}
