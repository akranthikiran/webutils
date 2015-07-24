package com.fw.webutil.util;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.Enumeration;
import java.util.List;
import java.util.zip.ZipEntry;
import java.util.zip.ZipFile;

/**
 * Represents a Zip file, to which files can be added. At end the entire content can be written
 * to some output stream
 * 
 * @author akiran
 */
public class ZipFileReader
{
	private ZipFile zipFile;
	
	public ZipFileReader(File file)
	{
		if(!file.exists())
		{
			throw new IllegalArgumentException("Specified file does not exist: " + file.getAbsolutePath());
		}
		
		try
		{
			zipFile = new ZipFile(file);
		}catch(Exception ex)
		{
			throw new IllegalStateException("An error occurred while opening specified zip file: " + file.getAbsolutePath());
		}
	}
	
	public List<String> getFileNames()
	{
		Enumeration<? extends ZipEntry> entries = zipFile.entries();
		List<String> res = new ArrayList<String>();
		
		while(entries.hasMoreElements())
		{
			res.add(entries.nextElement().getName());
		}
		
		return res;
	}
	
	public InputStream getFile(String fileName)
	{
		ZipEntry zipEntry = zipFile.getEntry(fileName);
		
		if(zipEntry == null)
		{
			return null;
		}
		
		try
		{
			return zipFile.getInputStream(zipEntry);
		}catch(IOException ex)
		{
			throw new IllegalStateException("An error occurred while reading zip-entry: " + fileName, ex);
		}
	}

	public String readTextFile(String fileName)
	{
		ZipEntry zipEntry = zipFile.getEntry(fileName);
		
		if(zipEntry == null)
		{
			return null;
		}
		
		try
		{
			InputStream is = zipFile.getInputStream(zipEntry);
			StringBuilder res = new StringBuilder();
			byte buff[] = new byte[1024];
			int read = 0;
			
			while((read = is.read(buff)) > 0)
			{
				res.append(new String(buff, 0, read));
			}
			
			return res.toString();
		}catch(IOException ex)
		{
			throw new IllegalStateException("An error occurred while reading zip-entry: " + fileName, ex);
		}
	}
}
