//- Copyright ©2009 Micah Martin.  All Rights Reserved
//- MMHTTP and all included source files are distributed under terms of the GNU LGPL.

package mmhttp.server;

import junit.framework.TestCase;
import mmhttp.protocol.MockRequest;
import mmhttp.protocol.Request;
import mmhttp.protocol.Response;
import mmhttp.protocol.ResponseParser;
import mmsocketserver.MockSocket;

import java.io.IOException;
import java.io.PipedInputStream;
import java.io.PipedOutputStream;

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
		socket.setHost("");
		server = new Server();
		server.responderFactory.register("root", MockResponder.class);
		expediter = new Expediter(socket, server);
	}

	public void testSetsRemoteAddressOfRequest() throws Exception
	{
		Request request = expediter.makeRequest();
		assertEquals("127.0.0.1", request.getRemoteAddress());
	}

	public void testSetsRemoteHostNameOnRequest() throws Exception
	{
		Request request = expediter.makeRequest();
		assertEquals("localhost", request.getRemoteHostName());
		assertNotNull(request.getRemoteHostName());
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
		socket.setHost("");
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

