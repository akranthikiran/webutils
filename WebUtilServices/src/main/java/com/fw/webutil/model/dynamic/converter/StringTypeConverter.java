package com.fw.webutil.model.dynamic.converter;

import com.fw.webutil.common.model.dynamic.FieldDef;
import com.fw.webutil.common.model.dynamic.FieldType;

@FieldTypeConverter(fieldTypes = {FieldType.STRING, FieldType.MULTI_LINE_STRING})
public class StringTypeConverter extends AbstractConverter
{
	@Override
	public Object parse(String strValue, FieldDef fieldDef)
	{
		return strValue;
	}

	@Override
	public Object parseJson(String strValue, FieldDef fieldDef) throws Exception
	{
		return OBJECT_MAPPER.readValue(strValue, String.class);
	}
}