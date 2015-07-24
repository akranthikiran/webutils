package com.fw.webutil.common;

public class CurrentlyInUseException extends PersistenceException
{
	private static final long serialVersionUID = 1L;
	
	public CurrentlyInUseException(String message)
	{
		super(message);
	}
}
