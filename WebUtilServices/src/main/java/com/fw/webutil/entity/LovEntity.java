package com.fw.webutil.entity;

import java.util.Collections;
import java.util.List;

import com.fw.webutil.common.FlagUtil;
import com.fw.webutil.common.IIdentifiable;
import com.fw.webutil.common.LovDataType;

public class LovEntity implements IIdentifiable
{
	public static final int FLAG_SYSTEM_LOV = 1;
	
	private String id;
	private String name;
	private String description;
	private int flags;
	private LovDataType type;
	private List<LovValueEntity> values = Collections.emptyList();
	
	public LovEntity()
	{
	}
	
	public LovEntity(String id, String name, String description, int flags, LovDataType type, List<LovValueEntity> values)
	{
		this.id = id;
		this.name = name;
		this.description = description;
		this.flags = flags;
		this.type = type;
		this.values = values;
		
		if(this.values == null)
		{
			this.values = Collections.emptyList();
		}
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
		return FlagUtil.getFlag(flags, FLAG_SYSTEM_LOV);
	}

	public void setSystemLov(boolean systemLov)
	{
		this.flags = FlagUtil.setFlag(flags, FLAG_SYSTEM_LOV, systemLov);
	}
	
	public int getFlags()
	{
		return flags;
	}

	public void setFlags(int flags)
	{
		this.flags = flags;
	}

	public LovDataType getType()
	{
		return type;
	}

	public void setType(LovDataType type)
	{
		this.type = type;
	}

	public List<LovValueEntity> getValues()
	{
		return values;
	}

	public void setValues(List<LovValueEntity> values)
	{
		this.values = values;
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
