package com.fw.webutil.security;

public class AuthorizationException extends RuntimeException
{
	private static final long serialVersionUID = 1L;
	
	private String missingRole;
	
	public AuthorizationException(String missingRole, String message)
	{
		super(message);
	}

	public String getMissingRole()
	{
		return missingRole;
	}
}
