package com.fw.webutil.query;

public class QueryParamsException extends RuntimeException
{
	private static final long serialVersionUID = 1L;

	public QueryParamsException(String message)
	{
		super(message);
	}

	public QueryParamsException(String message, Throwable cause)
	{
		super(message, cause);
	}
}
