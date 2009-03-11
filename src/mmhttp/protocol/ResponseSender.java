//- ©2009 Micah Martin.  All Rights Reserved
//- MMHTTP and all included source files are distributed under terms of the GNU LGPL.

package mmhttp.protocol;

import java.net.Socket;

public interface ResponseSender
{
	public void send(byte[] bytes) throws Exception;
	public void close() throws Exception;
	public Socket getSocket() throws Exception; //TODO-MdM maybe get rid of this method.
}
