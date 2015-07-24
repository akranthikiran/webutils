package com.fw.webutil.common.model;

public class ActionModel
{
	private String name;
	private String url;
	private String method;
	private boolean bodyExpected;
	
	public ActionModel(String name, String url, String method, boolean bodyExpected)
	{
		this.name = name;
		this.url = url;
		this.method = method;
		this.bodyExpected = bodyExpected;
	}
	
	public String getName()
	{
		return name;
	}

	public void setName(String name)
	{
		this.name = name;
	}

	public String getUrl()
	{
		return url;
	}

	public void setUrl(String url)
	{
		this.url = url;
	}

	public String getMethod()
	{
		return method;
	}

	public void setMethod(String method)
	{
		this.method = method;
	}

	public boolean isBodyExpected()
	{
		return bodyExpected;
	}

	public void setBodyExpected(boolean bodyExpected)
	{
		this.bodyExpected = bodyExpected;
	}
}
