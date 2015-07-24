package com.fw.webutil.model.dynamic.converter;

import com.fw.webutil.common.model.dynamic.FieldDef;
import com.fw.webutil.common.model.dynamic.FieldType;

@FieldTypeConverter(fieldTypes = {FieldType.INTEGER})
public class IntTypeConverter extends AbstractConverter
{
	@Override
	public Object parse(String strValue, FieldDef fieldDef) throws ClassNotFoundException
	{
		Class<?> targetType = Class.forName(fieldDef.getServerType());
		Long val = Long.parseLong(strValue);
		
		if(val == null)
		{
			return null;
		}
		
		if(Integer.class.equals(targetType) || int.class.equals(targetType))
		{
			return val.intValue();
		}

		if(Short.class.equals(targetType) || short.class.equals(targetType))
		{
			return val.shortValue();
		}

		return val;
	}

	@Override
	public Object parseJson(String strValue, FieldDef fieldDef) throws Exception
	{
		Class<?> targetType = Class.forName(fieldDef.getServerType());
		long val = OBJECT_MAPPER.readValue(strValue, Long.class);
		
		if(Integer.class.equals(targetType) || int.class.equals(targetType))
		{
			return (int)val;
		}

		if(Short.class.equals(targetType) || short.class.equals(targetType))
		{
			return (short)val;
		}

		return val;
	}
}