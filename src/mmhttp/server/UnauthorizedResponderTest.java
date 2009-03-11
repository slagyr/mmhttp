//- Copyright ©2009 Micah Martin.  All Rights Reserved
//- MMHTTP and all included source files are distributed under terms of the GNU LGPL.

package mmhttp.server;

import org.junit.Assert;
import org.junit.Test;
import mmhttp.protocol.Response;
import mmhttp.protocol.MockRequest;
import mmhttp.protocol.SimpleResponse;

public class UnauthorizedResponderTest extends Assert
{
  @Test
  public void shouldHaveNotFoundResponse() throws Exception
  {
    Server server = new Server();
    Response response = new UnauthorizedResponder().makeResponse(server, new MockRequest("/some/resource"));

    assertEquals(SimpleResponse.class, response.getClass());
    assertEquals(401, response.getStatus());
  }
}
