import mmhttp.protocol.*;
import mmhttp.server.*;

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
      return new SimpleResponse(200, "<h1>Hello World!</h1>");
    }
  }
}