package mmhtp.server;

import org.junit.Assert;
import org.junit.Test;
import org.junit.Before;

public class ResponderFactoryTest extends Assert
{
  private ResponderFactory factory;

  @Before
  public void setUp() throws Exception
  {
    factory = new ResponderFactory();
  }

  @Test
  public void shouldRegisterAResponder() throws Exception
  {
    factory.register("/test.*", MockResponder.class);

    assertEquals(MockResponder.class, factory.responderClassFor("/test/blah"));
  }

  @Test
  public void shouldNotFoundResponder() throws Exception
  {
    assertEquals(NotFoundResponder.class, factory.responderClassFor("blah"));   
  }

  @Test
  public void shouldConstructTheResponder() throws Exception
  {
    factory.register("/test.*", MockResponder.class);
    Responder responder = factory.responderFor("/test/blah");
    assertEquals(MockResponder.class, responder.getClass());

    responder = factory.responderFor("blah");
    assertEquals(NotFoundResponder.class, responder.getClass());
  }
}
