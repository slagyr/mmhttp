//- Copyright ©2009 Micah Martin.  All Rights Reserved
//- MMHTTP and all included source files are distributed under terms of the GNU LGPL.

package mmhttp.server;

import java.util.GregorianCalendar;

/**
 * An object representing all the loggable data of a request.
 */
public class LogData implements Cloneable
{
	public String host;
	public GregorianCalendar time;
	public String requestLine;
	public int status;
	public int size;
	public String username;

	public Object clone() throws CloneNotSupportedException
	{
		LogData newData = (LogData) super.clone();
		newData.time = (GregorianCalendar) time.clone();

		return newData;
	}
}