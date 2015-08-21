package com.fw.webutil.common.model.dynamic;

import java.io.Serializable;
import java.lang.reflect.Field;
import java.security.InvalidParameterException;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.validation.constraints.Pattern;

import com.fw.webutil.common.FlagUtil;
import com.fw.webutil.common.IExtensible;
import com.fw.webutil.common.IIdentifiable;
import com.fw.webutil.common.annotations.FieldMethod;
import com.fw.webutil.common.annotations.LOV;
import com.fw.webutil.common.annotations.Label;
import com.fw.webutil.common.annotations.Model;
import com.fw.webutil.common.annotations.MultilineText;
import com.fw.webutil.common.annotations.ReadOnly;
import com.fw.webutil.common.annotations.ServerField;
import com.fw.webutil.common.annotations.Transient;
import com.fw.webutil.common.validator.annotations.MaxLen;
import com.fw.webutil.common.validator.annotations.MinLen;
import com.fw.webutil.common.validator.annotations.Required;

@Model
public class FieldDef implements IIdentifiable, Serializable
{
	private static final long serialVersionUID = 1L;
	
	public static final int FLAG_MULTI_VALUED = 1;
	public static final int FLAG_REQUIRED = 2;
	public static final int FLAG_SEARCHABLE = 4;
	public static final int FLAG_DISPLAYABLE = 8;
	public static final int FLAG_IDENTIFIABLE = 16;
	public static final int FLAG_READ_ONLY = 32;
	
	@ReadOnly
	private String id;
	
	@ReadOnly
	private String parentId;
	
	@Required
	@MinLen(3)
	@MaxLen(30)
	@Pattern(regexp = "\\w+", message = "Name can contain only alpha-numeric characters (spaces are not allowed)")
	private String name;
	
	@Required
	@MinLen(3)
	@MaxLen(30)
	@Pattern(regexp = "\\w[\\w\\ \\-]+\\w", message = "Label can contain only alpha-numeric characters with optional spaces or hyphen (-) in between")
	private String label;
	
	@MaxLen(50)
	@MultilineText
	private String description;
	
	@Required
	private FieldType fieldType;
	
	private String defaultValue;

	//TODO: Do we need to change dataId to lovId? Or should we use it both for LOV id and Complex type id in future??
	@Label("LOV")
	@LOV(query = "lovList")
	private String dataId;
	
	//TODO: yet to be implemented. This should be made into system lov
	@ServerField
	@MinLen(3)
	@MaxLen(30)
	private String groupName;

	//TODO: This is yet to be implemented, and needs to be checked if its needed
	@ServerField
	private Integer order;
	
	private List<ValidatorConfiguration> validatorConfigurations;
	
	@ServerField
	private int flags = 0;
	
	@ReadOnly
	private boolean extendedField;
	
	//Used to send the client with type (complex type name or lov name) information for this field
	@ServerField
	@Transient
	private String serverType;

	@ServerField
	@Transient
	private ClientConfiguration clientConfiguration;
	
	@ServerField	
	@Transient
	private Map<String, ValidatorConfiguration> nameToValidator = new HashMap<>();
	
	public FieldDef(String id, String name, String label, String description, FieldType fieldType, 
			String dataId, String groupName, String parentId, Integer order, int flags,
			List<ValidatorConfiguration> validatorConfigurations, boolean extended)
	{
		this.id = id;
		this.name = name;
		this.label = label;
		this.description = description;
		this.fieldType = fieldType;
		this.dataId = dataId;
		this.groupName = groupName;
		this.parentId = parentId;
		this.order = order;
		this.flags = flags;
		this.extendedField = extended;
		
		Class<?> serverTypeCls = fieldType.getDefaultServerType();
		this.serverType = (serverTypeCls != null) ? serverTypeCls.getName() : null;
		
		setValidatorConfigurations(validatorConfigurations);
	}

	public FieldDef(String name, String label, FieldType fieldType, boolean extended)
	{
		this.name = name;
		this.label = label;
		this.fieldType = fieldType;
		this.extendedField = extended;
		
		Class<?> serverTypeCls = fieldType.getDefaultServerType();
		this.serverType = (serverTypeCls != null) ? serverTypeCls.getName() : null;
	}

	public FieldDef()
	{}

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

	public FieldType getFieldType()
	{
		return fieldType;
	}

	public void setFieldType(FieldType fieldType)
	{
		this.fieldType = fieldType;
	}
	
	public String getType()
	{
		return fieldType.getName();
	}

	public String getGroupName()
	{
		return groupName;
	}

	public void setGroupName(String groupName)
	{
		this.groupName = groupName;
	}

	public String getParentId()
	{
		return parentId;
	}

	public void setParentId(String parentId)
	{
		this.parentId = parentId;
	}

	public Integer getOrder()
	{
		return order;
	}

	public void setOrder(Integer order)
	{
		this.order = order;
	}

	public String getDataId()
	{
		return dataId;
	}

	public void setDataId(String dataId)
	{
		this.dataId = dataId;
	}

	public List<ValidatorConfiguration> getValidatorConfigurations()
	{
		return validatorConfigurations;
	}

	public void setValidatorConfigurations(List<ValidatorConfiguration> validatorConfigurations)
	{
		this.validatorConfigurations = validatorConfigurations;
		nameToValidator.clear();
		
		if(validatorConfigurations != null)
		{
			for(ValidatorConfiguration configuration: validatorConfigurations)
			{
				nameToValidator.put(configuration.getName(), configuration);
			}
		}
	}
	
	public ValidatorConfiguration getValidatorConfiguration(String name)
	{
		return nameToValidator.get(name);
	}
	
	public boolean hasValidatorConfiguration(String name)
	{
		return nameToValidator.containsKey(name);
	}

	public String getServerType()
	{
		return serverType;
	}

	public void setServerType(String serverType)
	{
		this.serverType = serverType;
	}

	//TODO: Need to add ui support for multi valued fields
	public boolean isMultiValued()
	{
		return FlagUtil.getFlag(flags, FLAG_MULTI_VALUED);
	}

	public void setMultiValued(boolean multiValued)
	{
		flags = FlagUtil.setFlag(flags, FLAG_MULTI_VALUED, multiValued);
	}

	@Label("Mandatory")
	@FieldMethod
	public boolean isRequired()
	{
		return FlagUtil.getFlag(flags, FLAG_REQUIRED);
	}

	public void setRequired(boolean required)
	{
		flags = FlagUtil.setFlag(flags, FLAG_REQUIRED, required);
	}
	
	@Label("Search Field")
	@FieldMethod
	public boolean isSearchable()
	{
		return FlagUtil.getFlag(flags, FLAG_SEARCHABLE);
	}

	public void setSearchable(boolean searchable)
	{
		flags = FlagUtil.setFlag(flags, FLAG_SEARCHABLE, searchable);
	}

	@Label("Display Field")
	@FieldMethod
	public boolean isDisplayable()
	{
		return FlagUtil.getFlag(flags, FLAG_DISPLAYABLE);
	}

	public void setDisplayable(boolean displayable)
	{
		flags = FlagUtil.setFlag(flags, FLAG_DISPLAYABLE, displayable);
	}

	@Label("Identity Field")
	@FieldMethod
	public boolean isIdentifiable()
	{
		return FlagUtil.getFlag(flags, FLAG_IDENTIFIABLE);
	}

	public void setIdentifiable(boolean identifiable)
	{
		flags = FlagUtil.setFlag(flags, FLAG_IDENTIFIABLE, identifiable);
	}

	public boolean isReadOnly()
	{
		return FlagUtil.getFlag(flags, FLAG_READ_ONLY);
	}

	public void setReadOnly(boolean readOnly)
	{
		flags = FlagUtil.setFlag(flags, FLAG_READ_ONLY, readOnly);
	}

	public int getFlags()
	{
		return flags;
	}

	public void setFlags(int flags)
	{
		this.flags = flags;
	}
	
	public boolean isExtendedField()
	{
		return extendedField;
	}

	public void setExtendedField(boolean extendedField)
	{
		this.extendedField = extendedField;
	}

	public ClientConfiguration getClientConfiguration()
	{
		return clientConfiguration;
	}

	public void setClientConfiguration(ClientConfiguration clientConfiguration)
	{
		this.clientConfiguration = clientConfiguration;
	}
	
	public String getDefaultValue()
	{
		return defaultValue;
	}

	public void setDefaultValue(String defaultValue)
	{
		this.defaultValue = defaultValue;
	}

	private void setExtendedValue(Object bean, Object value)
	{
		if(value == null)
		{
			return;
		}
		
		if(!(bean instanceof IExtensible))
		{
			throw new IllegalStateException("Tried to set extended field on non-extensible bean [Bean: " + bean.getClass().getName() + ", Field: " + name + "]");
		}
		
		IExtensible extensible = (IExtensible)bean;
		Class<?> expectedType = fieldType.getDefaultServerType();
		
		if(value.getClass().equals(expectedType))
		{
			extensible.setExtendedProperty(name, value);
		}
		else
		{
			throw new InvalidParameterException("Invalid value specified for field '" + name + "'. Expected Type: " + expectedType + ", Specified value type: " + value.getClass().getName());
		}
	}
	
	
	public void setBeanValue(Object bean, Object value) throws IllegalArgumentException, IllegalAccessException, NoSuchFieldException, SecurityException
	{
		if(this.extendedField)
		{
			setExtendedValue(bean, value);
			return;
		}
			
		Field field = bean.getClass().getDeclaredField(name);
		boolean accessible = field.isAccessible();
		
		field.setAccessible(true);
		
		field.set(bean, value);
		
		field.setAccessible(accessible);
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
		builder.append(",").append("Parent-id: ").append(parentId);

		builder.append("]");
		return builder.toString();
	}
}
