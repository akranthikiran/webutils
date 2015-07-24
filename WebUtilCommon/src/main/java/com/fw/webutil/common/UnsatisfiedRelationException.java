package com.fw.webutil.common;

public class UnsatisfiedRelationException extends PersistenceException
{
	private static final long serialVersionUID = 1L;
	
	private String fieldName;

	public UnsatisfiedRelationException(String fieldName, String message)
	{
		super(message);
		this.fieldName = fieldName;
	}

	public String getFieldName()
	{
		return fieldName;
	}
}
