//- Copyright ©2009 Micah Martin.  All Rights Reserved
//- MMHTTP and all included source files are distributed under terms of the GNU LGPL.

package mmhttp.server;

import mmsocketserver.SocketService;
import mmsocketserver.SocketServer;

import java.net.Socket;
import java.net.InetAddress;

/**
 * This is where the action begins.  The Server implements the SocketService.  Imagine a restaurant.  A restaurant
 * provides a service.  A server serves each table that comes in for the service.  Similarly, this framework (MM-HTTP)
 * provides an HTTP service and the Server serves each socket connecting for the service.
 *
 * When started the Server will start listening to the specified port on a new Thread.  Every connection will be
 * processed in a separate thread.
 */
public class Server implements SocketServer
{
  private SocketService theService;

  /**
   * A Logger object that will log each request.  Defaults to null;
   */
  public Logger logger = null;
  /**
   * The ResponderFactory will create Responders for each request.  Create on instantiation.
   */
  public ResponderFactory responderFactory = new ResponderFactory();
  /**
   * Displayed by the browser when promting for user credentials. Default: "MM-HTTP"
   */
  public String realm = "MM-HTTP";
  /**
   * Default: PromiscuousAuthenticator.  Can be set to a custom Authenticator.
   */
  public Authenticator authenticator = new PromiscuousAuthenticator();
  /**
   * Server name the will be used in the Server header of each response.
   */
  public String name = "MM-HTTP";
  /**
   *  The port that the server will listen to.  Default: 8080
   */
  public int port = 8080;
  /**
   * Time to wait, in milliseconds, for a request to parse. Default: 10000.
   */
  public long requestTimeout = 10000;
  /**
   * InetAddress to specify specific interfaces.  Defaults to null, which will listen to all interfaces.
   */
  public InetAddress host;

  /**
   * Constructs a new Server.  All of the fields of this class are public so that configuration can be done
   * after construction, and before starting the server.
   */
  public Server()
  {
  }

  /**
   * A convenience constructor that sets the port.
   * @param port
   */
  public Server(int port)
  {
    this.port = port;
  }

  /**
   * Shortcut to ResponderFactory.register.
   * @param pattern
   * @param klass
   */
  public void register(String pattern, Class klass)
  {
    responderFactory.register(pattern, klass);
  }

  public void register(String pattern, Responder responder)
  {
    responderFactory.register(pattern, responder);
  }

  /**
   * Sets the default Responder.
   * @param klass
   */
  public void setDefaultResponder(Class klass)
  {
    responderFactory.setDefault(klass);
  }

  /**
   * Sets the default Responder.
   * @param responder
   */
  public void setDefaultResponder(Responder responder)
  {
    responderFactory.setDefault(responder);
  }

  /**
   * Sets the error Responder.
   * @param klass
   */
  public void setErrorResponder(Class klass)
  {
    responderFactory.setError(klass);
  }

  /**
   * Sets the error Responder.
   * @param responder
   */
  public void setErrorResponder(ErrorResponder responder)
  {
    responderFactory.setError(responder);
  }

  /**
   * Starts the server.
   * @throws Exception
   */
  public void start() throws Exception
  {
    theService = new SocketService(port, this, host);
  }

  /**
   * Stops the server gracefully waiting for all the request threads to complete.
   * @throws Exception
   */
  public void stop() throws Exception
  {
    if(theService != null)
    {
      theService.close();
      theService = null;
    }
  }

  /**
   * Serves a socket connection.
   * @param socket
   */
  public void serve(Socket socket)
  {
    try
    {
      Expediter sender = new Expediter(socket, this);
      sender.setRequestParsingTimeLimit(requestTimeout);
      sender.start();
    }
    catch(Exception e)
    {
      e.printStackTrace();
    }
  }

  /**
   * @return true if the server is running
   */
  public boolean isRunning()
  {
    return theService != null;
  }

  /**
   * @return the ResponderFactory
   */
  public ResponderFactory getResponderFactory()
  {
    return responderFactory;
  }
}
