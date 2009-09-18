//- Copyright ©2009 Micah Martin.  All Rights Reserved
//- MMHTTP and all included source files are distributed under terms of the GNU LGPL.

package mmhttp.server;

import mmhttp.protocol.Response;
import mmhttp.protocol.Request;
import mmhttp.protocol.MockRequest;

/**
 * <p>An interface that custom Responders must implement.</p>
 *
 * <p>A Responder is responsible for generating a Response based on a Request. For any given request, a Responder
 * will be instantiated to respond to that request alone, then thrown to the garbage colletor. Therefor, instance
 * variable are only useful for handling one request.</p>
 *
 * <p>There are a few build-in Responders but, for interesting behavior, you will have to implement and register some
 * custom Responders of your own.</p>
 *
 * <p>Make sure your custom Responders have a default constructor. This will be used to instantiate them.</p>
 *
 * @see ResponderFactory
 */
public interface Responder
{
  Response makeResponse(Server server, Request request) throws Exception;
}
