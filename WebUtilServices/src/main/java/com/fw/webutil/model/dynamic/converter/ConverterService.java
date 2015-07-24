package com.fw.webutil.model.dynamic.converter;

import java.util.HashMap;
import java.util.Map;

import javax.annotation.PostConstruct;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationContext;
import org.springframework.stereotype.Service;

import com.fw.webutil.common.InvalidParameterException;
import com.fw.webutil.common.model.dynamic.FieldDef;
import com.fw.webutil.common.model.dynamic.FieldType;

@Service
public class ConverterService
{
	@Autowired
	private ApplicationContext applicationContext;
	
	private Map<FieldType, IConverter> typeToConverter = new HashMap<>();
	
	@PostConstruct
	private void init()
	{
		Map<String, IConverter> converters = applicationContext.getBeansOfType(IConverter.class);
		FieldTypeConverter fieldTypeConverter = null;
		FieldType fieldTypes[] = null;
		
		for(IConverter converter: converters.values())
		{
			fieldTypeConverter = converter.getClass().getAnnotation(FieldTypeConverter.class);
			
			if(fieldTypeConverter == null)
			{
				continue;
			}
			
			fieldTypes = fieldTypeConverter.fieldTypes();
			
			for(FieldType fieldType: fieldTypes)
			{
				typeToConverter.put(fieldType, converter);
			}
		}
	}

	public Object parseString(String strValue, FieldDef fieldDef)
	{
		IConverter converter = typeToConverter.get(fieldDef.getFieldType());
		
		if(converter == null)
		{
			return null;
		}
		
		if(strValue == null || strValue.trim().length() == 0)
		{
			return  null;
		}
		
		//TODO: Take care of multi value conversion
		try
		{
			return converter.parse(strValue, fieldDef);
		}catch(Exception ex)
		{
			throw new InvalidParameterException("Failed to parse value for field of type '" + fieldDef.getFieldType() + "'. Value: " + strValue, ex);
		}
	}

	public Object parseLabel(String strValue, FieldDef fieldDef, IBeanWrapper beanWrapper)
	{
		IConverter converter = typeToConverter.get(fieldDef.getFieldType());
		
		if(converter == null)
		{
			return null;
		}
		
		if(strValue == null || strValue.trim().length() == 0)
		{
			return  null;
		}
		
		//TODO: Take care of multi value conversion
		try
		{
			return converter.parseLabel(strValue, fieldDef, beanWrapper);
		}catch(Exception ex)
		{
			throw new InvalidParameterException("Failed to parse label for field of type '" + fieldDef.getFieldType() + "'. Value: " + strValue, ex);
		}
	}

	public Object parseJson(String jsonValue, FieldDef fieldDef)
	{
		IConverter converter = typeToConverter.get(fieldDef.getFieldType());
		
		if(converter == null)
		{
			return null;
		}
		
		if(jsonValue == null || jsonValue.trim().length() == 0)
		{
			return  null;
		}
		
		//TODO: Take care of multi value conversion
		try
		{
			return converter.parseJson(jsonValue, fieldDef);
		}catch(Exception ex)
		{
			throw new InvalidParameterException("Failed to parse json-value for field of type '" + fieldDef.getFieldType() + "'. Value: " + jsonValue, ex);
		}
	}
}
