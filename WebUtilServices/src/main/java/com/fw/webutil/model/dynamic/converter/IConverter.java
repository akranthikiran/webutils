package com.fw.webutil.model.dynamic.converter;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fw.webutil.common.model.dynamic.FieldDef;

public interface IConverter
{
	public ObjectMapper OBJECT_MAPPER = new ObjectMapper();
	
	public Object parse(String strValue, FieldDef fieldDef) throws Exception;
	
	public Object parseLabel(String strValue, FieldDef fieldDef, IBeanWrapper wrapper) throws Exception;
	
	public Object parseJson(String strValue, FieldDef fieldDef) throws Exception;
}
