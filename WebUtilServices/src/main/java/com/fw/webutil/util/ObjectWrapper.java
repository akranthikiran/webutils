package com.fw.webutil.util;

public class ObjectWrapper<T>
{
	private T object;
	
	public ObjectWrapper()
	{}
	
	public ObjectWrapper(T val)
	{
		this.object = val;
	}

	public T getObject()
	{
		return object;
	}

	public void setObject(T object)
	{
		this.object = object;
	}
}
