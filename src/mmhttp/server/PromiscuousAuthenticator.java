//- Copyright �2009 Micah Martin.  All Rights Reserved
//- MMHTTP and all included source files are distributed under terms of the GNU LGPL.

package mmhttp.server;

public class PromiscuousAuthenticator extends Authenticator
{
	public boolean isAuthenticated(String username, String password)
	{
		return true;
	}
}
