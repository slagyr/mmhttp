//- Copyright ©2009 Micah Martin.  All Rights Reserved
//- MMHTTP and all included source files are distributed under terms of the GNU LGPL.

package mmhttp.server;

import mmhttp.protocol.Response;
import mmhttp.protocol.Request;
import mmhttp.protocol.SimpleResponse;

/**
 * Used in the event of an error.  If a Responder should thrown an Exception, the ErrorReponder will be created to
 * respond with a 400 error along with the message of the Exception.
 */
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
