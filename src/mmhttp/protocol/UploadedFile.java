//- Copyright ©2009 Micah Martin.  All Rights Reserved
//- MMHTTP and all included source files are distributed under terms of the GNU LGPL.

package mmhttp.protocol;

import mmhttp.util.FileUtil;

import java.io.File;

/**
 * When parsing a request that contains uploaded files, the file data will be bundled up in instance of this class.  The
 * actual file content is stored in a temporary file. 
 */
public class UploadedFile
{
	private String name;
	private String type;
	private File file;

  /**
   * @param name
   * @param type
   * @param file
   */
	public UploadedFile(String name, String type, File file)
	{
		this.name = name;
		this.type = type;
		this.file = file;
	}

  /**
   * @return the file's name
   */
	public String getName()
	{
		return name;
	}

  /**
   * @return the files type
   */
	public String getType()
	{
		return type;
	}

  /**
   * @return the File, which points to the temp file where the content is stored.
   */
	public File getFile()
	{
		return file;
	}

  /**
   * @return a human readable representation of the object.
   */
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

  /**
   * @return true if the file has a valid name.
   */
	public boolean isUsable()
	{
		return (name != null && name.length() > 0);
	}

  /**
   * Deletes the temp file containing the uploaded file content. 
   */
	public void delete()
	{
		file.delete();
	}
}
