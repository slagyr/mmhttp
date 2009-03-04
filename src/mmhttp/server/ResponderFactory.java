package mmhttp.server;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Constructor;
import java.util.regex.Pattern;
import java.util.ArrayList;

public class ResponderFactory
{
  ArrayList<Registration> registrations = new ArrayList<Registration>();

  public void register(String regex, Class klass)
  {
    Pattern pattern = Pattern.compile(regex);
    registrations.add(new Registration(pattern, klass));
  }

  public Class responderClassFor(String resource)
  {
    for(Registration registration : registrations)
    {
      if(registration.pattern.matcher(resource).matches())
        return registration.klass;
    }
    return NotFoundResponder.class;
  }

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
