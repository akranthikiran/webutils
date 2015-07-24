package com.fw.webutil.model.dynamic.converter;

import com.fw.webutil.common.model.dynamic.FieldDef;

public abstract class AbstractConverter implements IConverter
{
	@Override
	public Object parseLabel(String strValue, FieldDef fieldDef, IBeanWrapper wrapper) throws Exception
	{
		return parse(strValue, fieldDef);
	}
	
}
