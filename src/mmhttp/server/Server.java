//- ©2009 Micah Martin.  All Rights Reserved
//- MMHTTP and all included source files are distributed under terms of the GNU LGPL.

package mmhttp.server;

import socketserver.SocketService;
import socketserver.SocketServer;

import java.net.Socket;
import java.net.InetAddress;
import java.net.UnknownHostException;

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
  public InetAddress host;

  public Server()
  {
    try
    {
      host = InetAddress.getByName("localhost");
    }
    catch(UnknownHostException e)
    {
      //okay
    }
  }

  public void register(String pattern, Class klass)
  {
    responderFactory.register(pattern, klass);
  }

  public void setDefaultResponder(Class klass)
  {
    responderFactory.defaultResponder = klass;
  }

  public boolean start() throws Exception
  {
    theService = new SocketService(port, this, host);
    return true;
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

  public ResponderFactory getResponderFactory()
  {
    return responderFactory;
  }
}
