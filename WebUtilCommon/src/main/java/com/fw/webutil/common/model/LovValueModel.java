package com.fw.webutil.common.model;

import javax.validation.constraints.Pattern;

import com.fw.webutil.common.annotations.Model;
import com.fw.webutil.common.annotations.ReadOnly;
import com.fw.webutil.common.validator.annotations.MaxLen;
import com.fw.webutil.common.validator.annotations.MinLen;
import com.fw.webutil.common.validator.annotations.Required;

@Model
public class LovValueModel
{
	@ReadOnly
	private String id;
	
	@ReadOnly
	private String parentLovId;
	
	@Required
	@MinLen(3)
	@Pattern(regexp="\\w[\\w\\s]+\\w", message = "Name can contain aplha-numeric characters with optional spaces in middle")
	private String name;

	private String description;
	
	@Required
	@MaxLen(50)
	private String value;
	
	public LovValueModel()
	{
	}

	/*
	public LovValueModel(LovValueEntity entity)
	{
		try
		{
			BeanUtils.copyProperties(this, entity);
			
			Object value = entity.getValue();
			
			if(value != null)
			{
				this.setValue(value.toString());
			}
		}catch(IllegalAccessException | InvocationTargetException e)
		{
			throw new IllegalStateException("An error occurred while copying from entity to model");
		}
	}
	*/

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

	public String getValue()
	{
		return value;
	}

	public void setValue(String value)
	{
		this.value = value;
	}
}
