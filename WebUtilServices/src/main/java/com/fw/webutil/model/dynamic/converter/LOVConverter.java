package com.fw.webutil.model.dynamic.converter;

import javax.management.InvalidApplicationException;

import org.springframework.beans.factory.annotation.Autowired;

import com.fw.ccg.util.CCGUtility;
import com.fw.ccg.util.InvalidValueException;
import com.fw.webutil.common.InvalidParameterException;
import com.fw.webutil.common.annotations.LovType;
import com.fw.webutil.common.model.dynamic.FieldDef;
import com.fw.webutil.common.model.dynamic.FieldType;
import com.fw.webutil.dao.LovDao;

@FieldTypeConverter(fieldTypes = {FieldType.LIST_OF_VALUES})
public class LOVConverter implements IConverter
{
	@Autowired
	private LovDao lovDao;
	
	private Object getEnumValue(String strValue, Class<?> targetType)
	{
		Enum<?> constants[] = (Enum[])targetType.getEnumConstants();
		
		for(Enum<?> e: constants)
		{
			if(e.name().equals(strValue))
			{
				return e;
			}
		}
		
		return null;
	}
	
	@Override
	public Object parse(String strValue, FieldDef fieldDef) throws ClassNotFoundException
	{
		if(fieldDef.getClientConfiguration().getLovType() == LovType.QUERY_TYPE)
		{
			Class<?> serverType = Class.forName(fieldDef.getServerType());
			return CCGUtility.toObject(strValue, serverType, null);
		}
		else if(fieldDef.getClientConfiguration().getLovType() == LovType.CUSTOM_TYPE)
		{
			Class<?> serverType = Class.forName(fieldDef.getServerType());
			return CCGUtility.toObject(strValue, serverType, null);
		}
		else
		{
			Class<?> targetType = Class.forName(fieldDef.getServerType());
			return getEnumValue(strValue, targetType);
		}		
	}
	
	private String getParentValue(FieldDef fieldDef, IBeanWrapper wrapper)
	{
		String parentField = fieldDef.getClientConfiguration().getParentField();
		
		if(parentField == null)
		{
			return null;
		}

		Object value = wrapper.getFieldValue(fieldDef.getClientConfiguration().getParentField());
		
		if(value != null)
		{
			return value.toString();
		}
		
		throw new InvalidValueException("Failed to fetch parent field '" + parentField + "' value for field - " + fieldDef.getName());
	}

	@Override
	public Object parseLabel(String strValue, FieldDef fieldDef, IBeanWrapper wrapper) throws Exception
	{
		if(fieldDef.getClientConfiguration().getLovType() == LovType.QUERY_TYPE)
		{
			String queryName = fieldDef.getClientConfiguration().getQueryName();
			String parentValue = getParentValue(fieldDef, wrapper);
			
			String lovValue = lovDao.getLovValue(queryName, parentValue, strValue);
			
			if(lovValue == null)
			{
				throw new InvalidParameterException("Invalid LOV label specified: " + strValue);
			}
			
			Class<?> serverType = Class.forName(fieldDef.getServerType());
			return CCGUtility.toObject(lovValue, serverType, null);
		}
		else if(fieldDef.getClientConfiguration().getLovType() == LovType.CUSTOM_TYPE)
		{
			String lovId = fieldDef.getDataId();
			String lovValue = lovDao.getCustomLovValue(lovId, strValue);
			
			if(lovValue == null)
			{
				throw new InvalidParameterException("Invalid LOV label specified: " + strValue);
			}
			
			Class<?> serverType = Class.forName(fieldDef.getServerType());
			return CCGUtility.toObject(lovValue, serverType, null);
		}
		else
		{
			Class<?> targetType = Class.forName(fieldDef.getServerType());
			Object value = getEnumValue(strValue, targetType);
			
			if(value == null)
			{
				throw new InvalidApplicationException("Invalid enum value specified: " + strValue);
			}
			
			return value;
		}		
	}

	@Override
	public Object parseJson(String jsonValue, FieldDef fieldDef) throws Exception
	{
		String strValue = OBJECT_MAPPER.readValue(jsonValue, String.class);
		
		if(strValue == null)
		{
			return null;
		}
		
		return parse(strValue, fieldDef);
	}

	
}