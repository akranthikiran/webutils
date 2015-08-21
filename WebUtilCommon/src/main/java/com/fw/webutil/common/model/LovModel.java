package com.fw.webutil.common.model;

import javax.validation.constraints.Pattern;

import com.fw.webutil.common.LovDataType;
import com.fw.webutil.common.annotations.Model;
import com.fw.webutil.common.annotations.ReadOnly;
import com.fw.webutil.common.validator.annotations.MaxLen;
import com.fw.webutil.common.validator.annotations.MinLen;
import com.fw.webutil.common.validator.annotations.Required;

@Model
public class LovModel
{
	@ReadOnly
	private String id;
	
	@Required
	@MinLen(3)
	@MaxLen(100)
	@Pattern(regexp="\\w[\\w\\s]+\\w", message = "Name can contain aplha-numeric characters with optional spaces in middle")
	private String name;
	
	private String description;
	
	@Required
	private LovDataType type;
	
	@ReadOnly
	private boolean systemLov;
	
	public LovModel()
	{
	}

	public String getId()
	{
		return id;
	}

	public void setId(String id)
	{
		this.id = id;
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

	public boolean isSystemLov()
	{
		return systemLov;
	}

	public void setSystemLov(boolean systemLov)
	{
		this.systemLov = systemLov;
	}
	
	public LovDataType getType()
	{
		return type;
	}

	public void setType(LovDataType type)
	{
		this.type = type;
	}

	/* (non-Javadoc)
	 * @see java.lang.Object#toString()
	 */
	@Override
	public String toString()
	{
		StringBuilder builder = new StringBuilder(super.toString());
		builder.append("[");

		builder.append("Name: ").append(name);
		builder.append(",").append("Type: ").append(type);

		builder.append("]");
		return builder.toString();
	}
}
