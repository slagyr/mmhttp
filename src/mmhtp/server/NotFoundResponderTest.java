package mmhtp.server;

import org.junit.Test;
import org.junit.Assert;
import mmhtp.protocol.MockRequest;
import mmhtp.protocol.Response;
import mmhtp.protocol.SimpleResponse;

public class NotFoundResponderTest extends Assert
{
  @Test
  public void shouldHaveNotFoundResponse() throws Exception
  {
    Response response = new NotFoundResponder().makeResponse(null, new MockRequest("/some/resource"));

    assertEquals(SimpleResponse.class, response.getClass());
    assertEquals(404, response.getStatus());
  }
}
