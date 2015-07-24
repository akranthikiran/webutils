package com.fw.webutil.common.model.dynamic;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.validation.constraints.Max;
import javax.validation.constraints.Min;
import javax.validation.constraints.Pattern;

import org.apache.commons.lang3.SerializationUtils;

import com.fw.webutil.common.IIdentifiable;
import com.fw.webutil.common.annotations.Model;
import com.fw.webutil.common.annotations.MultilineText;
import com.fw.webutil.common.annotations.ReadOnly;
import com.fw.webutil.common.annotations.ServerField;
import com.fw.webutil.common.validator.annotations.NotEmpty;
import com.fw.webutil.common.validator.annotations.Required;

@Model
public class DynamicType implements IIdentifiable, Cloneable, Serializable
{
	private static final long serialVersionUID = 1L;

	@ReadOnly
	private String id;

	@Required
	@Min(3)
	@Max(30)
	@Pattern(regexp = "[\\w ]+")
	private String name;

	@Required
	@Min(3)
	@Max(30)
	private String label;

	@Max(500)
	@MultilineText
	private String description;

	@NotEmpty
	private List<FieldDef> fields;

	@ServerField
	private boolean javaType;

	@ServerField
	private Map<String, FieldDef> fieldMap = new HashMap<String, FieldDef>();

	public DynamicType()
	{}

	public DynamicType(String id, String name, String label, String description, boolean javaType, List<FieldDef> fields)
	{
		this.id = id;
		this.name = name;
		this.label = label;
		this.javaType = javaType;
		this.description = description;

		setFields(fields);
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

	public String getLabel()
	{
		return label;
	}

	public void setLabel(String label)
	{
		this.label = label;
	}

	public String getDescription()
	{
		return description;
	}

	public void setDescription(String description)
	{
		this.description = description;
	}

	public List<FieldDef> getFields()
	{
		return fields;
	}

	public void setFields(List<FieldDef> fields)
	{
		if(fields == null)
		{
			return;
		}

		this.fields = fields;

		for(FieldDef field : fields)
		{
			fieldMap.put(field.getName(), field);
		}
	}

	/** 
	 * Adds value to {@link #fields Fields}
	 *
	 * @param field field to be added
	 */
	public void addField(FieldDef field)
	{
		if(fields == null)
		{
			fields = new ArrayList<FieldDef>();
		}

		fields.add(field);
		fieldMap.put(field.getName(), field);
	}

	public void addFields(Collection<FieldDef> fieldDefLst)
	{
		if(fieldDefLst == null || fieldDefLst.isEmpty())
		{
			return;
		}

		if(fields == null)
		{
			fields = new ArrayList<FieldDef>();
		}

		for(FieldDef field : fieldDefLst)
		{
			fields.add(field);
			fieldMap.put(field.getName(), field);
		}
	}

	public void addFieldsAtStarting(Collection<FieldDef> fieldDefLst, boolean override)
	{
		if(fieldDefLst == null || fieldDefLst.isEmpty())
		{
			return;
		}

		if(fields == null)
		{
			fields = new ArrayList<FieldDef>();
		}
		
		List<FieldDef> newFields = new ArrayList<FieldDef>(fieldDefLst.size());
		FieldDef oldField = null;
		
		for(FieldDef newField: fieldDefLst)
		{
			oldField = fieldMap.get(newField.getName());
			
			if(oldField != null)
			{
				if(!override)
				{
					continue;
				}
				
				this.fields.remove(oldField);
				fieldMap.remove(oldField.getName());
			}
			
			newFields.add(newField);
		}
		
		fields.addAll(0, newFields);

		for(FieldDef field : newFields)
		{
			fieldMap.put(field.getName(), field);
		}
	}

	public boolean hasField(String name)
	{
		return fieldMap.containsKey(name);
	}
	
	public FieldDef getField(String name)
	{
		return fieldMap.get(name);
	}

	public boolean isJavaType()
	{
		return javaType;
	}

	public void setJavaType(boolean javaType)
	{
		this.javaType = javaType;
	}
	
	@Override
	public DynamicType clone()
	{
		return (DynamicType)SerializationUtils.clone(this);
	}
}
