//- Copyright ©2009 Micah Martin.  All Rights Reserved
//- MMHTTP and all included source files are distributed under terms of the GNU LGPL.

package mmhttp.protocol;

import mmhttp.util.FileUtil;

import java.io.File;

public class UploadedFile
{
	private String name;
	private String type;
	private File file;

	public UploadedFile(String name, String type, File file)
	{
		this.name = name;
		this.type = type;
		this.file = file;
	}

	public String getName()
	{
		return name;
	}

	public String getType()
	{
		return type;
	}

	public File getFile()
	{
		return file;
	}

	public String toString()
	{
		try
		{
			return "name : " + getName() + "; type : " + getType() + "; content : " + FileUtil.getFileContent(file);
		}
		catch(Exception e)
		{
			e.printStackTrace();
			return e.toString();
		}
	}

	public boolean isUsable()
	{
		return (name != null && name.length() > 0);
	}

	public void delete()
	{
		file.delete();
	}
}
