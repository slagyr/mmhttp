package mmhtp.server;

import socketserver.SocketService;
import socketserver.SocketServer;

import java.net.Socket;

public class Server implements SocketServer
{
  private SocketService theService;

  public Logger logger = null;
  public ResponderFactory responderFactory = new ResponderFactory();
  public String realm = "MM-HTTP";
  public Authenticator authenticator = new PromiscuousAuthenticator();
  public String name = "MM-HTTP";
  public int port = 8080;
  public long requestTimeout = 10000;

  public void register(String pattern, Class klass)
  {
    responderFactory.register(pattern, klass);
  }

  public boolean start()
  {
    try
    {
      theService = new SocketService(port, this);
      return true;
    }
    catch(Exception e)
    {
      e.printStackTrace();
    }
    return false;
  }

  public void stop() throws Exception
  {
    if(theService != null)
    {
      theService.close();
      theService = null;
    }
  }

  public void serve(Socket s)
  {
    try
    {
      Expediter sender = new Expediter(s, this);
      sender.setRequestParsingTimeLimit(requestTimeout);
      sender.start();
    }
    catch(Exception e)
    {
      e.printStackTrace();
    }
  }

  public boolean isRunning()
  {
    return theService != null;
  }
}
