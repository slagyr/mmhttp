//- Copyright ©2009 Micah Martin.  All Rights Reserved
//- MMHTTP and all included source files are distributed under terms of the GNU LGPL.

package mmhttp.server;

import java.lang.reflect.Constructor;
import java.util.regex.Pattern;
import java.util.ArrayList;

/**
 * <p>The spawning place of all Responders.</p>
 * <p/>
 * <p>Every Server will have an instance of a ResponderFactory. The role of the ResponderFactory is to create a
 * Responder based on the resource requested. Users must therefore register their customer Responders along with a
 * regular expresion to describe resources that should be handled by the Responder.</p>
 *
 * @see Responder
 * @see Server
 */
public class ResponderFactory
{
  private ArrayList<Registration> registrations = new ArrayList<Registration>();

  /**
   * Used in the event that none of the registered Responders match a request.  Default: NotFoundResponder
   */
  private Registration defaultRegistration = new ResponderRegistration(null, new NotFoundResponder());

  /**
   * Set the default (not found) responder using a Responder class.
   *
   * @param responderClass
   */
  public void setDefault(Class responderClass)
  {
    defaultRegistration = new ClassRegistration(null, responderClass);
  }

  /**
   * Set the default (not found) responder using a Responder instance.
   *
   * @param responder
   */
  public void setDefault(Responder responder)
  {
    defaultRegistration = new ResponderRegistration(null, responder);
  }

  /**
   * Used in the event that an error occurs during processing.  Default: ErrorResponder
   */
  private Registration errorRegistration = new ResponderRegistration(null, new BuiltinErrorResponder());

  /**
   * Set the error responder using a Responder class.
   *
   * @param responderClass
   */
  public void setError(Class responderClass)
  {
    errorRegistration = new ClassRegistration(null, responderClass);
  }

  /**
   * Set the error responder using a Responder instance.
   *
   * @param responder
   */
  public void setError(ErrorResponder responder)
  {
    errorRegistration = new ResponderRegistration(null, responder);
  }

  /**
   * <p>Use this method to register your custom Responders. Note: The Server has a convenience method to register
   * Responders so you need not use this menthod directory.</p>
   * <p/>
   * <p>To register a Responder you pass in a regular expression String and a Responder class.
   * For example: <code>server.responderFactory.register("abc\d{3}", AlphaNumericResponder.class)</code>
   * This will cause all incoming requests for 'abc' follow by 3 digits to be handled by an instance of
   * AlphaNumericResponder.</p>
   * <p/>
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
    registrations.add(new ClassRegistration(pattern, klass));
  }

  /**
   * <p>Registers a Responder object rather than a Responder class.  This is suitable for Responders
   * that don't use any member variables, and hence, are not susceptible to concurrent update problems.</p>
   *
   * @param regex
   * @param responder
   */
  public void register(String regex, Responder responder)
  {
    Pattern pattern = Pattern.compile(regex);
    registrations.add(new ResponderRegistration(pattern, responder));
  }

  /**
   * This method is used to retreives the first matching Registration for a given resource. You may find this
   * method handy to test your registrations.
   *
   * @param resource
   * @return the matching responder class
   */
  public Registration registrationFor(String resource)
  {
    for(Registration registration : registrations)
    {
      if(registration.pattern.matcher(resource).matches())
        return registration;
    }
    return defaultRegistration;
  }

  public Responder responderFor(String resource) throws Exception
  {
    Registration registration = registrationFor(resource);
    return registration.getResponder();
  }

  public ErrorResponder getErrorResponder()
  {
    try
    {
      return (ErrorResponder)errorRegistration.getResponder();
    }
    catch(Exception e)
    {
      return new BuiltinErrorResponder();
    }
  }

  private abstract static class Registration
  {
    public Pattern pattern;

    public Registration(Pattern pattern)
    {
      this.pattern = pattern;
    }

    public abstract Responder getResponder() throws Exception;

  }

  private static class ClassRegistration extends Registration
  {
    public Class klass;

    public ClassRegistration(Pattern pattern, Class klass)
    {
      super(pattern);
      this.klass = klass;
    }

    public Responder getResponder() throws Exception
    {
      Constructor constructor = klass.getConstructor();
      return (Responder) constructor.newInstance();
    }
  }

  private class ResponderRegistration extends Registration
  {
    private Responder responder;

    public ResponderRegistration(Pattern pattern, Responder responder)
    {
      super(pattern);
      this.responder = responder;
    }

    public Responder getResponder() throws Exception
    {
      return responder;
    }
  }
}
