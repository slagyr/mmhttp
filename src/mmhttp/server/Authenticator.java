//- Copyright ©2009 Micah Martin.  All Rights Reserved
//- MMHTTP and all included source files are distributed under terms of the GNU LGPL.

package mmhttp.server;

import mmhttp.protocol.Request;

/**
 * Base class for all authentication implementations.  HTTP 1.1 allows for digest authentication whereby a username
 * and password may be provided in the request headers.  To make use of the digest authentication and require login for
 * various parts of your site, you must provide the server with an implementation of Authenticator.
 */
public abstract class Authenticator
{
  /**
   * This is the primary method used for authenticating a user for a given request.  Typically, you will not wat to
   * override this method, though you may.
   *
   * By default the credentials will be pulled out of the request and cheched for validity.  If the credentials are not
   * valid, the Responder is checked to see if it is restricted.  If the Responder is restricted and the credentials
   * are not valid, then the request will not be processed an Unauthorized response will be returned.
   *
   * @param server
   * @param request
   * @param privilegedResponder
   * @return a Responder, either the requested one or an UnauthorizedResponder
   * @throws Exception
   */
  public Responder authenticate(Server server, Request request, Responder privilegedResponder) throws Exception
  {
    request.parseCredentials();
	  String username = request.getAuthorizationUsername();
	  String password = request.getAuthorizationPassword();

    if(isAuthenticated(username, password))
      return privilegedResponder;
    else if(!isRestricted(privilegedResponder))
      return privilegedResponder;
    else
      return verifyOperationIsSecure(privilegedResponder);
  }

  private Responder verifyOperationIsSecure(Responder privilegedResponder)
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

  /**
   * Should be overridden to determine if the specifed Responder should require a valid user to be logged in.
   *
   * @param privilegedResponder
   * @return false by default
   */
  public boolean isRestricted(Responder privilegedResponder)
  {
    return false;  
  }

  /**
   * Should be overriden to validate the credentials.
   *
   * @param username
   * @param password
   * @return true is the user credentials are valid
   * @throws Exception
   */
  public abstract boolean isAuthenticated(String username, String password) throws Exception;

  public String toString()
  {
    return getClass().getName();
  }
}

