package mmhttp.server;

import org.junit.Test;
import org.junit.Assert;
import mmhttp.protocol.MockRequest;
import mmhttp.protocol.Response;
import mmhttp.protocol.SimpleResponse;

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
