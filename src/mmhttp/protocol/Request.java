//- Copyright ©2009 Micah Martin.  All Rights Reserved
//- MMHTTP and all included source files are distributed under terms of the GNU LGPL.

package mmhttp.protocol;

import mmsocketserver.StreamReader;

import java.io.*;
import java.util.*;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.net.URLDecoder;

/**
 * This class is one of the primary work horses of MM HTTP.  It parses incoming HTTP Requests and provided accessors
 * to the data.
 *
 * Although compliant to the HTTP 1.1 protocol, it is by no means complete.  For example, only the GET and POST
 * methods are supported.  But is does support multipart content and file uploads.
 */
public class Request
{
	private static final Pattern requestLinePattern = Pattern.compile("(\\p{Upper}+?) ([^\\s]+)");
	private static final Pattern requestUriPattern = Pattern.compile("([^?]+)\\??(.*)");
	private static final Pattern queryStringPattern = Pattern.compile("([^=]*)=?([^&]*)&?");
	private static final Pattern headerPattern = Pattern.compile("([^:]*): (.*)");
	private static final Pattern boundaryPattern = Pattern.compile("boundary=(.*)");
	private static final Pattern multipartHeaderPattern = Pattern.compile("([^ =]+)=\\\"([^\"]*)\\\"");

	private static Collection allowedMethods = buildAllowedMethodList();

	protected StreamReader input;
	protected String requestURI;
	protected String resource;
	protected String queryString;
	protected HashMap<String, Object> inputs = new HashMap<String, Object>();
	protected HashMap<String, String> headers = new HashMap<String, String>();
	protected String entityBody = "";
	protected String requestLine;
	protected String authorizationUsername;
	protected String authorizationPassword;
	protected String remoteAddress;
	protected String remoteHostName;
	protected String remoteInetAddressHostName;
	private boolean hasBeenParsed;
	private long bytesParsed = 0;


	private static Set buildAllowedMethodList()
	{
		Set<String> methods = new HashSet<String>(20);
		methods.add("GET");
		methods.add("POST");
		return methods;
	}

	protected Request()
	{
	}

  /**
   * Constructs a new Request with the provided InputStream, however the stream will not be read and the request
   * will not be parsed until calling parse().
   *
   * @param input
   * @throws Exception
   */
	public Request(InputStream input) throws Exception
	{
		this.input = new StreamReader(new BufferedInputStream(input));
	}

  /**
   * Parses the request.
   *
   * @throws Exception
   */
	public void parse() throws Exception
	{
		readAndParseRequestLine();
		headers = parseHeaders(input);
		parseEntityBody();
		hasBeenParsed = true;
	}

	private void readAndParseRequestLine() throws Exception
	{
		requestLine = input.readLine();
		Matcher match = requestLinePattern.matcher(requestLine);
		checkRequestLine(match);
		requestURI = match.group(2);
		parseRequestUri(requestURI);
	}

	private HashMap<String, String> parseHeaders(StreamReader reader) throws Exception
	{
		HashMap<String, String> headers = new HashMap<String, String>();
		String line = reader.readLine();
		while(!"".equals(line))
		{
			Matcher match = headerPattern.matcher(line);
			if(match.find())
			{
				String key = match.group(1);
				String value = match.group(2);
				headers.put(key.toLowerCase(), value);
			}
			line = reader.readLine();
		}
		return headers;
	}

	private void parseEntityBody() throws Exception
	{
		if(hasHeader("Content-Length"))
		{
			String contentType = (String) getHeader("Content-Type");
			if(contentType != null && contentType.startsWith("multipart/form-data"))
			{
				Matcher match = boundaryPattern.matcher(contentType);
				match.find();
				parseMultiPartContent(match.group(1));
			}
			else
			{
				entityBody = input.read(getContentLength());
				parseQueryString(entityBody);
			}
		}
	}

  /**
   * @return Returns the number of bytes in the content of the request. (The value of the Content-Length header)
   */
	public int getContentLength()
	{
		return Integer.parseInt((String) getHeader("Content-Length"));
	}

	private void parseMultiPartContent(String boundary) throws Exception
	{
		boundary = "--" + boundary;

		int numberOfBytesToRead = getContentLength();
		accumulateBytesReadAndReset();
		input.readUpTo(boundary);
		while(numberOfBytesToRead - input.numberOfBytesConsumed() > 10)
		{
			input.readLine();
			HashMap<String, String> headers = parseHeaders(input);
			String contentDisposition = headers.get("content-disposition");
			Matcher matcher = multipartHeaderPattern.matcher(contentDisposition);
			while(matcher.find())
				headers.put(matcher.group(1), matcher.group(2));

			String name = headers.get("name");
			Object value;
			if(headers.containsKey("filename"))
				value = createUploadedFile(headers, input, boundary);
			else
				value = input.readUpTo("\r\n" + boundary);

			inputs.put(name, value);
		}
	}

	private void accumulateBytesReadAndReset()
	{
		bytesParsed += input.numberOfBytesConsumed();
		input.resetNumberOfBytesConsumed();
	}

	private Object createUploadedFile(HashMap headers, StreamReader reader, String boundary) throws Exception
	{
		String filename = (String) headers.get("filename");
		String contentType = (String) headers.get("content-type");
		File tempFile = File.createTempFile("FitNesse", ".uploadedFile");
		OutputStream output = new BufferedOutputStream(new FileOutputStream(tempFile));
		reader.copyBytesUpTo("\r\n" + boundary, output);
		output.close();
		return new UploadedFile(filename, contentType, tempFile);
	}

	private void checkRequestLine(Matcher match) throws HttpException
	{
		if(!match.find())
			throw new HttpException("The request string is malformed and can not be parsed");
		if(!allowedMethods.contains(match.group(1)))
			throw new HttpException("The " + match.group(1) + " method is not currently supported");
	}

	private void parseRequestUri(String requestUri)
	{
		Matcher match = requestUriPattern.matcher(requestUri);
		match.find();
		resource = stripLeadingSlash(match.group(1));
		queryString = match.group(2);
		parseQueryString(queryString);
	}

	protected void parseQueryString(String queryString)
	{
		Matcher match = queryStringPattern.matcher(queryString);
		while(match.find())
		{
			String key = match.group(1);
			String value = decodeContent(match.group(2));
			inputs.put(key, value);
		}
	}

  /**
   * @return the Request Line: the first line of the request.
   */
	public String getRequestLine()
	{
		return requestLine;
	}

  /**
   * @return the Request Uri: the resource being requested + the query string.
   */
	public String getRequestUri()
	{
		return requestURI;
	}

  /**
   * @return the resource being requested.
   */
	public String getResource()
	{
		return resource;
	}

  /**
   * @return the query string.  Individual values in the query string should be retrieved via the getInput() method.
   *
   * @see #getInput
   */
	public String getQueryString()
	{
		return queryString;
	}

  /**
   * Tells you if the specified input was included in the query string or multipart data.
   *
   * @param key
   * @return true if the input was provided
   */
	public boolean hasInput(String key)
	{
		return inputs.containsKey(key);
	}

  /**
   * Retrives the value of inputs that were included in the query string or multipart data of the request.
   *
   * The return value will typically be a string unless the input represents an uploaded file in which case the return
   * value would be of type UploadedFile. null is returned if the specified input doesn't exist.
   *
   * @param key
   * @return the value of the input.
   * @see UploadedFile
   */
	public Object getInput(String key)
	{
		return inputs.get(key);
	}

  /**
   * Return the hash of inputs included in the query string or multipart data of the request.
   *
   * @return inputs
   */
  public HashMap<String, Object> getInputs()
  {
    return inputs;
  }

  /**
   * @param key
   * @return true if the header was included in the request
   */
	public boolean hasHeader(String key)
	{
		return headers.containsKey(key.toLowerCase());
	}

  /**
   * Retrieves the value of HTTP headers in the request.  null is returned if the header was not present.
   * @param key
   * @return the value of the header
   */
	public String getHeader(String key)
	{
		return headers.get(key.toLowerCase());
	}

  /**
   * Returns a hash of all the headers in the request.
   *
   * @return headers
   */
  public HashMap<String, String> getHeaders()
  {
    return headers;
  }

  /**
   * @return the entity body of the request.
   */
	public String getBody()
	{
		return entityBody;
	}

	private String stripLeadingSlash(String url)
	{
		return url.substring(1);
	}

  /**
   * @return a handy string representation of the request, useful for debugging
   */
	public String toString()
	{
		StringBuffer buffer = new StringBuffer();
		buffer.append("--- Request Start ---").append("\n");
		buffer.append("Request URI:  ").append(requestURI).append("\n");
		buffer.append("Resource:     ").append(resource).append("\n");
		buffer.append("Query String: ").append(queryString).append("\n");
    buffer.append("Hearders: (");
    buffer.append(headers.size());
    buffer.append(")\n");
    addMap(headers, buffer);
    buffer.append("Form Inputs: (").append(inputs.size()).append(")\n");
		addMap(inputs, buffer);
		buffer.append("Entity Body: ").append("\n");
		buffer.append(entityBody).append("\n");
		buffer.append("--- End Request ---\n");

		return buffer.toString();
	}

	private void addMap(HashMap map, StringBuffer buffer)
	{
		if(map.size() == 0)
		{
			buffer.append("\tempty");
		}
    for(Object o : map.keySet())
    {
      String key = (String) o;
      String value = map.get(key) != null ? escape(map.get(key).toString()) : null;
      buffer.append("\t");
      buffer.append(escape(key));
      buffer.append(" \t-->\t ");
      buffer.append(value);
      buffer.append("\n");
    }
	}

	private String escape(String foo)
	{
		return foo.replaceAll("[\n\r]+", "|");
	}

  /**
   * Decodes the URL unescaping all the escaped characters.
   *
   * @param content
   * @return the decoded URL
   */
	public static String decodeContent(String content)
	{
		String escapedContent = null;
		try
		{
			escapedContent = URLDecoder.decode(content, "UTF-8");
		}
		catch(UnsupportedEncodingException e)
		{
			escapedContent = "URLDecoder Error";
		}
		return escapedContent;
	}

  /**
   * @return true if parse() completed successfully.
   */
	public boolean hasBeenParsed()
	{
		return hasBeenParsed;
	}

  /**
   * Helper method public only for testing.
   *
   * Decodes the Base 64 digest autentication.
   *
   * @param headerValue
   * @return the userpass string
   * @throws Exception
   */
	public String getUserpass(String headerValue) throws Exception
	{
		String encodedUserpass = headerValue.substring(6);
		return Base64.decode(encodedUserpass);
	}

  /**
   * The digest authentication parameters are not used by default.  This method should be called to parse and load
   * the username and password.  Be sure to call this before calling getAuthorizationUsername() or
   * getAuthorizationPassword().
   *
   * @throws Exception
   */
	public void parseCredentials() throws Exception
	{
		if(hasHeader("Authorization"))
		{
			String authHeader = getHeader("Authorization").toString();
			String userpass = getUserpass(authHeader);
      String[] values = userpass.split(":");
			if(values.length == 2)
			{
				authorizationUsername = values[0];
				authorizationPassword = values[1];
			}
		}
	}

  /**
   * @return the digest authorization username.
   */
	public String getAuthorizationUsername()
	{
		return authorizationUsername;
	}

  /**
   * @return the digest authorization password.
   */
	public String getAuthorizationPassword()
	{
		return authorizationPassword;
	}

  /**
   * Sometimes clients will stop transmitting halfway through a request.  This method gives some indiction of parsing
   * progress being made.
   *
   * @return exactly what you might think
   */
	public long numberOfBytesParsed()
	{
		return bytesParsed + input.numberOfBytesConsumed();
	}

	public void setRemoteAddress(String remoteAddress)
	{
		this.remoteAddress = remoteAddress;
	}

	public String getRemoteAddress()
	{
		return remoteAddress;
	}

	public String getRemoteHostName()
	{
		return remoteHostName;
	}

	public void setRemoteHostName(String remoteHostName)
	{
		this.remoteHostName = remoteHostName;
	}
}
