//- Copyright ©2009 Micah Martin.  All Rights Reserved
//- MMHTTP and all included source files are distributed under terms of the GNU LGPL.

package mmhttp.server;

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

    assertEquals(MockResponder.class, factory.responderFor("/test/blah").getClass());
  }

  @Test
  public void shouldNotFoundResponder() throws Exception
  {
    assertEquals(NotFoundResponder.class, factory.responderFor("blah").getClass());
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
