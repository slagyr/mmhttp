//- Copyright ©2009 Micah Martin.  All Rights Reserved
//- MMHTTP and all included source files are distributed under terms of the GNU LGPL.

package mmhttp.server;

import mmhttp.protocol.HttpException;
import mmhttp.protocol.Request;
import mmhttp.protocol.Response;
import mmhttp.protocol.ResponseSender;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.InetAddress;
import java.net.InetSocketAddress;
import java.net.Socket;
import java.net.SocketException;
import java.util.GregorianCalendar;

/**
 * This class fills a role in the restaurant metaphore.  In a restaurant, the expediter is a person who stands at the
 * window of the kitchen where she puts in orders, organizes, and adds the final touch on all the orders before they get
 * served.  The Expediter in this context takes a socket connection, oversees the parsing of the request, the building
 * of the response, and the sending of data.
 */
public class Expediter implements ResponseSender
{
	private Socket socket;
	private InputStream input;
	private OutputStream output;
	private Request request;
	private Response response;
	private Server server;
	protected long requestParsingTimeLimit;
	private long requestProgress;
	private long requestParsingDeadline;
	private boolean hasError;

  /**
   * Constructs an Expediter with a fresh socket connection, and the Server from whence it came.
   * @param socket
   * @param server
   * @throws Exception
   */
  public Expediter(Socket socket, Server server) throws Exception
	{
		this.server = server;
		this.socket = socket;
		input = socket.getInputStream();
		output = socket.getOutputStream();
		requestParsingTimeLimit = 10000;
	}

  /**
   * Expedites the request.
   * @throws Exception
   */
	public void start() throws Exception
	{
		try
		{
			Request request = makeRequest();
			makeResponse(request);
			sendResponse();
		}
		catch(SocketException se)
		{
			// can be thrown by makeResponse or sendResponse.
		}
		catch(Throwable e)
		{
			e.printStackTrace();
		}
	}

  /**
   * Sets the time, in milliseconds, to wait for the request to be parsed. Default: 10000 (10 seconds).
   * @param millis
   */
	public void setRequestParsingTimeLimit(long millis)
	{
		requestParsingTimeLimit = millis;
	}

  /**
   * @return requestParsingTimeLimit (milliseconds)
   */
	public long getRequestParsingTimeLimit()
	{
		return requestParsingTimeLimit;
	}

  /**
   * Writes the given bytes to the socket output stream.
   * @param bytes
   * @throws Exception
   */
	public void send(byte[] bytes) throws Exception
	{
		try
		{
			output.write(bytes);
			output.flush();
		}
		catch(IOException stopButtonPressed_probably)
		{
      //okay
		}
	}

  /**
   * Logs the request and closes the socket.
   * @throws Exception
   */
	public void close() throws Exception
	{
		try
		{
			log(socket, request, response);
			socket.close();
		}
		catch(IOException e)
		{
			e.printStackTrace();
		}
	}

  /**
   * @return the socket used in construction.
   * @throws Exception
   */
	public Socket getSocket() throws Exception
	{
		return socket;
	}

  /**
   * Instantiates the request.
   * @return Request
   * @throws Exception
   */
	public Request makeRequest() throws Exception
	{
		request = new Request(input);

		InetAddress remoteAddress = ((InetSocketAddress) socket.getRemoteSocketAddress()).getAddress();
		request.setRemoteAddress(remoteAddress.getHostAddress());
		request.setRemoteHostName(remoteAddress.getHostName());

		return request;
	}

  /**
   * Initiated the delivery of the response.
   * @throws Exception
   */
	public void sendResponse() throws Exception
	{
		response.readyToSend(this);
	}

	private Response makeResponse(Request request) throws Exception
	{
		try
		{
			Thread parseThread = createParsingThread(request);
			parseThread.start();

			waitForRequest(request);

			if(!hasError)
				response = createGoodResponse(request);
		}
		catch(SocketException se)
		{
			throw(se);
		}
		catch(Exception e)
		{
			response = new BuiltinErrorResponder(e).makeResponse(server, request);
		}
		return response;
	}

  /**
   * Creates the correct response object based on the request and authentication settings.
   * @param request
   * @return Response
   * @throws Exception
   */
	public Response createGoodResponse(Request request) throws Exception
	{
		Response response;
		Responder responder = server.responderFactory.responderFor(request.getResource());
		responder = server.authenticator.authenticate(server, request, responder);
		response = responder.makeResponse(server, request);
		response.addHeader("Server", server.name);
		response.addHeader("Connection", "close");
		return response;
	}

	private void waitForRequest(Request request) throws InterruptedException
	{
		long now = System.currentTimeMillis();
		requestParsingDeadline = now + requestParsingTimeLimit;
		requestProgress = 0;
		while(!hasError && !request.hasBeenParsed())
		{
			Thread.sleep(10);
			if(timeIsUp(now) && parsingIsUnproductive(request))
					reportError(408, new Exception("The client request has been unproductive for too long.  It has timed out and will now longer be processed"));
		}
	}

	private boolean parsingIsUnproductive(Request request)
	{
		long updatedRequestProgress = request.numberOfBytesParsed();
		if(updatedRequestProgress > requestProgress)
		{
			requestProgress = updatedRequestProgress;
			return false;
		}
		else
			return true;
	}

	private boolean timeIsUp(long now)
	{
		now = System.currentTimeMillis();
		if(now > requestParsingDeadline)
		{
			requestParsingDeadline = now + requestParsingTimeLimit;
			return true;
		}
		else
			return false;
	}

	private Thread createParsingThread(final Request request)
	{
    return new Thread()
    {
      public synchronized void run()
      {
        try
        {
          request.parse();
        }
        catch(HttpException e)
        {
          reportError(400, e);
        }
        catch(Exception e)
        {
          reportError(e);
        }
      }
    };
	}

	private void reportError(int status, Exception error)
	{
		try
		{
      response = server.responderFactory.getErrorResponder().makeResponse(server, request, error);
			response.setStatus(status);
			hasError = true;
		}
		catch(Exception e)
		{
			e.printStackTrace();
		}
	}

	private void reportError(Exception e)
	{
		try
		{
			response = server.responderFactory.getErrorResponder().makeResponse(server, request, e);
			hasError = true;
		}
		catch(Exception e1)
		{
			e1.printStackTrace();
		}
	}

  /**
   * Constructs a LogData object representing the loggable data of this request.
   * @param socket
   * @param request
   * @param response
   * @return LogData
   */
	public static LogData makeLogData(Socket socket, Request request, Response response)
	{
		LogData data = new LogData();
		data.host = ((InetSocketAddress) socket.getRemoteSocketAddress()).getAddress().getHostAddress();
		data.time = new GregorianCalendar();
		data.requestLine = request.getRequestLine();
		data.status = response.getStatus();
		data.size = response.getContentSize();
		data.username = request.getAuthorizationUsername();

		return data;
	}

  /**
   * Constructs the LogData and sends it to the Server's Logger for logging.
   * @param s
   * @param request
   * @param response
   * @throws Exception
   */
	public void log(Socket s, Request request, Response response) throws Exception
	{
		if(server.logger != null)
			server.logger.log(makeLogData(s, request, response));
	}
}

