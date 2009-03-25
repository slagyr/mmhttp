//- Copyright ©2009 Micah Martin.  All Rights Reserved
//- MMHTTP and all included source files are distributed under terms of the GNU LGPL.

package mmhttp.server;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Constructor;
import java.util.regex.Pattern;
import java.util.ArrayList;

/**
 * <p>The spawning place of all Responders.</p>
 *
 * <p>Every Server will have an instance of a ResponderFactory. The role of the ResponderFactory is to create a
 * Responder based on the resource requested. Users must therefore register their customer Responders along with a
 * regular expresion to describe resources that should be handled by the Responder.</p>
 *
 * @see Responder
 * @see Server
 */
public class ResponderFactory
{
  ArrayList<Registration> registrations = new ArrayList<Registration>();
  
  /**
   * Used in the event that none of the registered Responders match a request.
   */
  public Class defaultResponder = NotFoundResponder.class;

  /**
   * <p>Use this method to register your custom Responders. Note: The Server has a convenience method to register
   * Responders so you need not use this menthod directory.</p>
   *
   * <p>To register a Responder you pass in a regular expression String and a Responder class.
   * For example: <code>server.responderFactory.register("abc\d{3}", AlphaNumericResponder.class)</code>
   * This will cause all incoming requests for 'abc' follow by 3 digits to be handled by an instance of
   * AlphaNumericResponder.</p>
   *
   * <p>The order in which Responder are registere is important. Since a given request may match multiple regular
   * expressions, the factory will follow a simple 'first come, first served' policy. That is the first registered
   * Responder to match the requested resource will be instantiated to process the request.</p>
   * 
   * @param regex
   * @param klass
   */
  public void register(String regex, Class klass)
  {
    Pattern pattern = Pattern.compile(regex);
    registrations.add(new Registration(pattern, klass));
  }

  /**
   * This method is used to retreives the first matching Responder class for a given resource. You may find this
   * method handy to test your registrations.
   *
   * @param resource
   * @return the matching responder class
   */
  public Class responderClassFor(String resource)
  {
    for(Registration registration : registrations)
    {
      if(registration.pattern.matcher(resource).matches())
        return registration.klass;
    }
    return defaultResponder;
  }

  /**
   * This will find the correct Responder for the resource and construct it using the default constructor.
   *
   * @param resource
   * @return an instance of the matching responder
   * @throws NoSuchMethodException
   * @throws IllegalAccessException
   * @throws InvocationTargetException
   * @throws InstantiationException
   */
  public Responder responderFor(String resource) throws NoSuchMethodException, IllegalAccessException, InvocationTargetException, InstantiationException
  {
    Class responderClass = responderClassFor(resource);
    Constructor constructor = responderClass.getConstructor();
    return (Responder)constructor.newInstance();
  }

  private static class Registration
  {
    public Registration(Pattern pattern, Class klass)
    {
      this.pattern = pattern;
      this.klass = klass;
    }

    public Pattern pattern;
    public Class klass;
  }
}
