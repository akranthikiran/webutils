package com.fw.webutil.model.dynamic.converter;

import com.fw.webutil.common.model.dynamic.FieldDef;
import com.fw.webutil.common.model.dynamic.FieldType;

@FieldTypeConverter(fieldTypes = {FieldType.FLOAT})
public class FloatTypeConverter extends AbstractConverter
{
	@Override
	public Object parse(String strValue, FieldDef fieldDef) throws ClassNotFoundException
	{
		Class<?> targetType = Class.forName(fieldDef.getServerType());
		
		double val = Double.parseDouble(strValue);
		
		if(Float.class.equals(targetType) || float.class.equals(targetType))
		{
			return (float)val;
		}
		
		return val;
	}

	@Override
	public Object parseJson(String strValue, FieldDef fieldDef) throws Exception
	{
		Class<?> targetType = Class.forName(fieldDef.getServerType());
		Double val = OBJECT_MAPPER.readValue(strValue, Double.class);
		
		if(val == null)
		{
			return null;
		}
		
		if(Float.class.equals(targetType) || float.class.equals(targetType))
		{
			return val.floatValue();
		}
		
		return val;
	}
}