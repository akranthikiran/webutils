package com.fw.webutil.common.model.dynamic;

import java.util.List;

import com.fw.webutil.common.IIdentifiable;

public class DynamicInstance implements IIdentifiable
{
	private String id;
	private String typeId;
	private String name;
	private List<Property> properties;
	
	public DynamicInstance()
	{}
	
	public DynamicInstance(String id, String typeId, String name, List<Property> properties)
	{
		this.id = id;
		this.name = name;
		this.properties = properties;
		this.typeId = typeId;
	}

	public String getId()
	{
		return id;
	}

	public void setId(String id)
	{
		this.id = id;
	}

	public String getTypeId()
	{
		return typeId;
	}

	public void setTypeId(String typeId)
	{
		this.typeId = typeId;
	}

	public String getName()
	{
		return name;
	}

	public void setName(String name)
	{
		this.name = name;
	}

	public List<Property> getProperties()
	{
		return properties;
	}

	public void setProperties(List<Property> properties)
	{
		this.properties = properties;
	}

	/* (non-Javadoc)
	 * @see java.lang.Object#toString()
	 */
	@Override
	public String toString()
	{
		StringBuilder builder = new StringBuilder(super.toString());
		builder.append("[");

		builder.append("Id: ").append(id);
		builder.append(",").append("Name: ").append(name);

		builder.append("]");
		return builder.toString();
	}
}
