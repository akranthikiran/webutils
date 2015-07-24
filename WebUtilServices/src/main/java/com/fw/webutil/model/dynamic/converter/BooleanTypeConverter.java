package com.fw.webutil.model.dynamic.converter;

import com.fw.webutil.common.model.dynamic.FieldDef;
import com.fw.webutil.common.model.dynamic.FieldType;

@FieldTypeConverter(fieldTypes = {FieldType.BOOLEAN})
public class BooleanTypeConverter extends AbstractConverter
{
	@Override
	public Object parse(String strValue, FieldDef fieldDef)
	{
		return "true".equalsIgnoreCase(strValue);
	}

	@Override
	public Object parseJson(String strValue, FieldDef fieldDef) throws Exception
	{
		return OBJECT_MAPPER.readValue(strValue, Boolean.class);
	}

	
}