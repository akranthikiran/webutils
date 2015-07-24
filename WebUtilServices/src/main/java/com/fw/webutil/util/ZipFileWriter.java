package com.fw.webutil.util;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.IOException;
import java.io.OutputStream;
import java.util.zip.ZipEntry;
import java.util.zip.ZipOutputStream;

import org.apache.commons.io.FileUtils;

/**
 * Represents a Zip file, to which files can be added. At end the entire content can be written
 * to some output stream
 * 
 * @author akiran
 */
public class ZipFileWriter
{
	private ByteArrayOutputStream bos = new ByteArrayOutputStream();
	private ZipOutputStream zos = new ZipOutputStream(bos);
	
	private boolean closed = false;
	
	
	public ZipFileWriter()
	{}
	
	public ZipFileWriter(File file)
	{
		if(!file.exists())
		{
			throw new IllegalArgumentException("Specified file does not exist: " + file.getAbsolutePath());
		}
	}
	
	
	/**
	* Adds specified "content" as a file with name "fileName" to result zip file.
	* 
	* @param fileName Name of the file to be added
	* @param content Content of the file
	*/
	public void addFile(String fileName, byte content[])
	{
		if(closed)
		{
			throw new IllegalStateException("Zip file is already closed");
		}
		
		try
		{
			ZipEntry zipEntry = new ZipEntry(fileName);
			zos.putNextEntry(zipEntry);
			
			zos.write(content, 0, content.length);
			
			zos.closeEntry();
		}catch(Exception ex)
		{
			throw new IllegalStateException("An error occurred while adding zip file entry: " + fileName, ex);
		}
	}
	
	/**
	* Adds specified "content" as a file with name "fileName" to result zip file.
	* 
	* @param fileName Name of the file to be added
	* @param content Content of the file
	*/
	public void addFile(String fileName, String content)
	{
		addFile(fileName, content.getBytes());
	}

	/**
	* Adds specified file to zip file
	*
	* @param entryFilePath
	*/
	public void addFile(File entryFilePath)
	{
		addFile(entryFilePath.getName(), entryFilePath);
	}
	
	/**
	* Adds specified file "entryFilePath" to zip file with name "fileName"
	*
	* @param fileName
	* @param entryFilePath
	*/
	public void addFile(String fileName, File entryFilePath)
	{
		byte content[] = null;
		
		try
		{
			content = FileUtils.readFileToByteArray(entryFilePath);
		}catch(IOException ex)
		{
			throw new IllegalStateException("An error occurred while reading input file: " + entryFilePath, ex);
		}
		
		addFile(fileName, content);
	}
	
	/**
	* Writes the current zip contents to specified output stream "os". This will close this zip file.
	*
	* @param os
	*/
	public void writeTo(OutputStream os)
	{
		try
		{
			os.write(toByteArray());
		}catch(Exception ex)
		{
			throw new IllegalStateException("An error occurred while writing zip file content to output stream", ex);
		}
	}
	
	/**
	* Converts current file to byte array.  This will close this zip file.
	*
	* @return
	*/
	public byte[] toByteArray()
	{
		try
		{
			zos.flush();
			
			if(!closed)
			{
				zos.close();
				this.closed = true;
			}

			return bos.toByteArray();
		}catch(Exception ex)
		{
			throw new IllegalStateException("An error occurred while converting to byte array", ex);
		}
	}
	
}
