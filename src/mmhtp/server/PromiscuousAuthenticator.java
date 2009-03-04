package mmhtp.server;

public class PromiscuousAuthenticator extends Authenticator
{
	public boolean isAuthenticated(String username, String password)
	{
		return true;
	}
}
