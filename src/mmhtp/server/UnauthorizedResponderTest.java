package mmhtp.server;

import org.junit.Assert;
import org.junit.Test;
import mmhtp.protocol.Response;
import mmhtp.protocol.MockRequest;
import mmhtp.protocol.SimpleResponse;

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
