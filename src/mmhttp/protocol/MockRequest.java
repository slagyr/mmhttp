//- Copyright ©2009 Micah Martin.  All Rights Reserved
//- MMHTTP and all included source files are distributed under terms of the GNU LGPL.

package mmhttp.protocol;

public class MockRequest extends Request
{
	private Exception parseException = null;

  public MockRequest(String resource)
	{
		this.resource = resource;
	}

	public MockRequest()
	{
		resource = "";
	}

	public void setRemoteAddress(String value)
	{
		remoteAddress = value;
	}

	public void setRequestUri(String value)
	{
		requestURI = value;
	}

	public void setRequestLine(String value)
	{
		requestLine = value;
	}

	public void setResource(String value)
	{
		resource = value;
	}

	public void setBody(String value)
	{
		entityBody = value;
	}

	public void setQueryString(String value)
	{
		queryString = value;
		parseQueryString(value);
	}

	public void addInput(String key, Object value)
	{
		inputs.put(key, value);
	}

	public void addHeader(String key, String value)
	{
		headers.put(key.toLowerCase(), value);
	}

	public void throwExceptionOnParse(Exception e)
	{
		parseException = e;
	}

	public void parseCredentials()
	{
		return;
	}

	public void setCredentials(String username, String password)
	{
		authorizationUsername = username;
		authorizationPassword = password;

	}

	public void parse() throws Exception
	{
		if(parseException != null)
		{
			throw parseException;
		}
	}
}
