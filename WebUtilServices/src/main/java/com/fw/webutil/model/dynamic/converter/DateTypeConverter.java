package com.fw.webutil.model.dynamic.converter;

import java.text.ParseException;
import java.util.Date;

import com.fw.webutil.common.ICommonConstants;
import com.fw.webutil.common.model.dynamic.FieldDef;
import com.fw.webutil.common.model.dynamic.FieldType;

@FieldTypeConverter(fieldTypes = {FieldType.DATE})
public class DateTypeConverter extends AbstractConverter
{
	@Override
	public Object parse(String strValue, FieldDef fieldDef)
	{
		try
		{
			return ICommonConstants.DEFAULT_DATE_FORMAT.parse(strValue);
		}catch(ParseException e)
		{
			throw new IllegalArgumentException("An error occurred while parsing date value: " + strValue);
		}
	}

	@Override
	public Object parseJson(String strValue, FieldDef fieldDef) throws Exception
	{
		OBJECT_MAPPER.setDateFormat(ICommonConstants.DEFAULT_DATE_FORMAT);
		return OBJECT_MAPPER.readValue(strValue, Date.class);
	}
}