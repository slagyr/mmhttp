//- Copyright ©2009 Micah Martin.  All Rights Reserved
//- MMHTTP and all included source files are distributed under terms of the GNU LGPL.

package mmhttp.server;

import mmhttp.protocol.ResponseSender;
import mmhttp.protocol.Request;
import mmhttp.protocol.Response;
import mmhttp.protocol.HttpException;

import java.net.Socket;
import java.net.SocketException;
import java.net.InetSocketAddress;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.IOException;
import java.util.GregorianCalendar;

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

  public Expediter(Socket s, Server server) throws Exception
	{
		this.server = server;
		socket = s;
		input = s.getInputStream();
		output = s.getOutputStream();
		requestParsingTimeLimit = 10000;
	}

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

	public void setRequestParsingTimeLimit(long t)
	{
		requestParsingTimeLimit = t;
	}

	public long getRequestParsingTimeLimit()
	{
		return requestParsingTimeLimit;
	}

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

	public Socket getSocket() throws Exception
	{
		return socket;
	}

	public Request makeRequest() throws Exception
	{
		request = new Request(input);
		return request;
	}

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
			response = new ErrorResponder(e).makeResponse(server, request);
		}
		return response;
	}

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
					reportError(408, "The client request has been unproductive for too long.  It has timed out and will now longer be processed");
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
          reportError(400, e.getMessage());
        }
        catch(Exception e)
        {
          reportError(e);
        }
      }
    };
	}

	private void reportError(int status, String message)
	{
		try
		{
			response = new ErrorResponder(message).makeResponse(server, request);
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
			response = new ErrorResponder(e).makeResponse(server, request);
			hasError = true;
		}
		catch(Exception e1)
		{
			e1.printStackTrace();
		}
	}

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

	public void log(Socket s, Request request, Response response) throws Exception
	{
		if(server.logger != null)
			server.logger.log(makeLogData(s, request, response));
	}
}

