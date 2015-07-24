package com.fw.webutil.query;

public class QueryNotFoundException extends RuntimeException
{
	private static final long serialVersionUID = 1L;
	private String queryName;
	
	public QueryNotFoundException(String queryName)
	{
		super("No query found with specified name: " + queryName);
	}

	public String getQueryName()
	{
		return queryName;
	}
}
