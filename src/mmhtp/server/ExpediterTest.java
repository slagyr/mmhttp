package mmhtp.server;

import mmhtp.protocol.ResponseParser;
import mmhtp.protocol.MockRequest;
import mmhtp.protocol.Response;
import mmhtp.protocol.Request;

import java.io.PipedInputStream;
import java.io.PipedOutputStream;
import java.io.IOException;

import socketserver.MockSocket;
import junit.framework.TestCase;

public class ExpediterTest extends TestCase
{
	private Expediter expediter;
	private MockSocket socket;
	private Server server;
	private PipedInputStream clientInput;
	private PipedOutputStream clientOutput;
	private ResponseParser response;

  public void setUp() throws Exception
	{
		socket = new MockSocket();
		server = new Server();
		server.responderFactory.register("root", MockResponder.class);
		expediter = new Expediter(socket, server);
	}

	public void testAuthenticationGetsCalled() throws Exception
	{
		server.authenticator = new StoneWallAuthenticator();
		MockRequest request = new MockRequest();
		Response response = expediter.createGoodResponse(request);
		assertEquals(401, response.getStatus());
	}

	public void testClosedSocketMidResponse() throws Exception
	{
		try
		{
			MockRequest request = new MockRequest();
			Response response = expediter.createGoodResponse(request);
			socket.close();
			response.readyToSend(expediter);
		}
		catch(IOException e)
		{
			fail("no IOException should be thrown");
		}
	}

  public void testIncompleteRequestsTimeOut() throws Exception
  {
	  final Expediter sender = preparePipedExpediter();

	  Thread senderThread = makeSendingThread(sender);
	  senderThread.start();
		Thread parseResponseThread = makeParsingThread();
		parseResponseThread.start();
    Thread.sleep(sender.requestParsingTimeLimit + 100);

	  parseResponseThread.join();

	  assertEquals(408, response.getStatus());
  }

	private Expediter preparePipedExpediter() throws Exception
	{
		PipedInputStream socketInput = new PipedInputStream();
		clientOutput = new PipedOutputStream(socketInput);
		clientInput = new PipedInputStream();
		PipedOutputStream socketOutput = new PipedOutputStream(clientInput);
		MockSocket socket = new MockSocket(socketInput, socketOutput);
		final Expediter sender = new Expediter(socket, server);
		sender.requestParsingTimeLimit = 200;
		return sender;
	}

	public void testCompleteRequest() throws Exception
	{
		final Expediter sender = preparePipedExpediter();

		Thread senderThread = makeSendingThread(sender);
		senderThread.start();
		Thread parseResponseThread = makeParsingThread();
		parseResponseThread.start();

		clientOutput.write("GET /root HTTP/1.1\r\n\r\n".getBytes());
		clientOutput.flush();

		parseResponseThread.join();

		assertEquals(200, response.getStatus());
	}

	public void testSlowButCompleteRequest() throws Exception
	{
		final Expediter sender = preparePipedExpediter();

		Thread senderThread = makeSendingThread(sender);
		senderThread.start();
		Thread parseResponseThread = makeParsingThread();
		parseResponseThread.start();

		byte[] bytes = "GET /root HTTP/1.1\r\n\r\n".getBytes();
		try
		{
			for(int i = 0; i < bytes.length; i++)
			{
				byte aByte = bytes[i];
				clientOutput.write(aByte);
				clientOutput.flush();
				Thread.sleep(20);
			}
		}
		catch(IOException pipedClosed)
		{
		}

		parseResponseThread.join();

		assertEquals(200, response.getStatus());
	}

	private Thread makeSendingThread(final Expediter sender)
	{
		Thread senderThread = new Thread(new Runnable()
    {
      public void run()
      {
        try
        {
          sender.start();
        }
        catch(Exception e)
        {
          e.printStackTrace();
        }
      }
    });
		return senderThread;
	}

	private Thread makeParsingThread()
	{
		Thread parseResponseThread = new Thread(new Runnable()
		{
			public void run()
			{
				try
				{
					response = new ResponseParser(clientInput);
				}
				catch(Exception e)
				{
					e.printStackTrace();
				}
			}
		});
		return parseResponseThread;
	}

	class StoneWallAuthenticator extends Authenticator
	{
    public Responder authenticate(Server server, Request request, Responder privilegedResponder) throws Exception
    {
      return new UnauthorizedResponder();
    }

		public boolean isAuthenticated(String username, String password)
		{
			return false;
		}
	}

}

