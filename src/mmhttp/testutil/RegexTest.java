//- Copyright ©2009 Micah Martin.  All Rights Reserved
//- MMHTTP and all included source files are distributed under terms of the GNU LGPL.

package mmhttp.testutil;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.junit.Assert;

public class RegexTest extends Assert
{
    public static void assertMatches(String regexp, String string)
    {
        assertHasRegexp(regexp, string);
    }

    public static void assertNotMatches(String regexp, String string)
    {
        assertDoesntHaveRegexp(regexp, string);
    }

    public static void assertHasRegexp(String regexp, String output)
    {
        Matcher match = Pattern.compile(regexp, Pattern.MULTILINE | Pattern.DOTALL).matcher(output);
        boolean found = match.find();
        if (!found)
            fail("The regexp <" + regexp + "> was not found.");
    }

    public static void assertDoesntHaveRegexp(String regexp, String output)
    {
        Matcher match = Pattern.compile(regexp, Pattern.MULTILINE).matcher(output);
        boolean found = match.find();
        if (found)
            fail("The regexp <" + regexp + "> was found.");
    }

    public static void assertSubString(String substring, String string)
    {
        if (string.indexOf(substring) == -1)
            fail("substring '" + substring + "' not found.");
    }

    public static void assertNotSubString(String subString, String string)
    {
        if (string.indexOf(subString) > -1)
            fail("expecting substring:'" + subString + "' in string:'" + string + "'");
    }

    public static String divWithIdAndContent(String id, String expectedDivContent)
    {
        return "<div.*?id=\"" + id + "\".*?>" + expectedDivContent + "</div>";
    }
}
