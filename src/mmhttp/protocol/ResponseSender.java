//- Copyright ©2009 Micah Martin.  All Rights Reserved
//- MMHTTP and all included source files are distributed under terms of the GNU LGPL.

package mmhttp.protocol;

/**
 * Implementers of this interface are responsible for sending responses over a socket.
 *
 * TODO MdM - Having a second look at this, I'm not sure it's really worth keeping around.  
 */
public interface ResponseSender
{
	public void send(byte[] bytes) throws Exception;
	public void close() throws Exception;
}
