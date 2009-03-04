package mmhtp.server;

import mmhtp.protocol.Request;

public abstract class Authenticator
{
  public Authenticator()
  {
  }

  public Responder authenticate(Server server, Request request, Responder privilegedResponder) throws Exception
  {
    request.getCredentials();
	  String username = request.getAuthorizationUsername();
	  String password = request.getAuthorizationPassword();

    if(isAuthenticated(username, password))
      return privilegedResponder;
    else if(!isRestricted(privilegedResponder))
      return privilegedResponder;
    else
      return verifyOperationIsSecure(privilegedResponder, server, request);
  }

  private Responder verifyOperationIsSecure(Responder privilegedResponder, Server server, Request request)
  {
    try
    {
      if(isRestricted(privilegedResponder))
        return new UnauthorizedResponder();
      else
        return privilegedResponder;
    }
    catch(Exception e)
    {
      e.printStackTrace();
      return new UnauthorizedResponder();
    }
  }

  public boolean isRestricted(Responder privilegedResponder)
  {
    return false;  
  }

  public abstract boolean isAuthenticated(String username, String password) throws Exception;

  public String toString()
  {
    return getClass().getName();
  }
}

