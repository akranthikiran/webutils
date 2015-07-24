package com.fw.webutil.entity;

public class LovValueEntity
{
	private String id;
	private String parentLovId;
	private String name;
	private String description;
	private Object value;
	
	public LovValueEntity()
	{
	}

	public LovValueEntity(String id, String parentLovId, String name, String description, String value)
	{
		this.id = id;
		this.parentLovId = parentLovId;
		this.name = name;
		this.description = description;
		this.value = value;
	}

	public String getId()
	{
		return id;
	}

	public void setId(String id)
	{
		this.id = id;
	}
	
	public String getParentLovId()
	{
		return parentLovId;
	}

	public void setParentLovId(String parentLovId)
	{
		this.parentLovId = parentLovId;
	}

	public String getName()
	{
		return name;
	}

	public void setName(String name)
	{
		this.name = name;
	}

	public String getDescription()
	{
		return description;
	}

	public void setDescription(String description)
	{
		this.description = description;
	}

	public Object getValue()
	{
		return value;
	}

	public void setValue(Object value)
	{
		this.value = value;
	}
}
