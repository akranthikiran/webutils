package com.fw.webutil.common;

/**
 * Exception to be thrown when no data found with input criteria
 * @author akkink1
 */
public class NoSuchDataException extends RuntimeException
{
	private static final long serialVersionUID = 1L;

	public NoSuchDataException()
	{
		super();
	}

	public NoSuchDataException(String message, Throwable cause)
	{
		super(message, cause);
	}

	public NoSuchDataException(String message)
	{
		super(message);
	}

	public NoSuchDataException(Throwable cause)
	{
		super(cause);
	}

}
