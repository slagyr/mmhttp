package mmhtp.server;

import mmhtp.protocol.Response;
import mmhtp.protocol.Request;
import mmhtp.protocol.SimpleResponse;

public class ErrorResponder implements Responder
{
	Exception exception;
	private String message;

	public ErrorResponder(Exception e)
	{
		exception = e;
	}

	public ErrorResponder(String message)
	{
		this.message = message;
	}

	public Response makeResponse(Server server, Request request) throws Exception
	{
		SimpleResponse response = new SimpleResponse(400);

    String html = "<html><head><title>Error</title></head><body>";
		if(exception != null)
			html += "<pre>" + makeExceptionString(exception) + "</pre>";
		if(message != null)
			html += "<div style\"text-align: center;\">" + message + "</div>";
    html += "</body></html>";
		response.setContent(html);

		return response;
	}

	public static String makeExceptionString(Exception e)
	{
		StringBuffer buffer = new StringBuffer();
		buffer.append(e.toString()).append("\n");
		StackTraceElement[] stackTreace = e.getStackTrace();
    for(StackTraceElement aStackTreace : stackTreace)
      buffer.append("\t").append(aStackTreace).append("\n");

		return buffer.toString();
	}
}
