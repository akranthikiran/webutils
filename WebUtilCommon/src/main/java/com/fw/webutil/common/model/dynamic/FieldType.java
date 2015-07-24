package com.fw.webutil.common.model.dynamic;

import java.util.Date;
import java.util.HashMap;
import java.util.Map;

public enum FieldType
{
	STRING("string", String.class),
	
	MULTI_LINE_STRING("multiLine"),
	
	INTEGER("int", Integer.class, int.class, Long.class, long.class, Short.class, short.class),
	
	FLOAT("float", Float.class, float.class, Double.class, double.class),
	
	BOOLEAN("boolean", Boolean.class, boolean.class),
	
	DATE("date", Date.class),
	
	COMPLEX("complex"),
	
	LIST_OF_VALUES("lov");
	
	private static Map<Class<?>, FieldType> typeMap;
	
	private String name;
	private Class<?> javaTypes[];
	
	private FieldType(String name, Class<?>... javaTypes)
	{
		this.name = name;
		
		if(javaTypes == null || javaTypes.length == 0)
		{
			javaTypes = null;
		}
		
		this.javaTypes = javaTypes;
	}
	
	public String getName()
	{
		return name;
	}

	private static Map<Class<?>, FieldType> buildTypeMap()
	{
		Map<Class<?>, FieldType> typeMap = new HashMap<>();
		
		for(FieldType fieldType: FieldType.values())
		{
			if(fieldType.javaTypes == null)
			{
				continue;
			}
			
			for(Class<?> jtype: fieldType.javaTypes)
			{
				typeMap.put(jtype, fieldType);
			}
		}
		
		return typeMap;
	}
	
	public static FieldType getFieldType(Class<?> staticType)
	{
		if(typeMap == null)
		{
			typeMap = buildTypeMap();
		}

		return typeMap.get(staticType);
	}
	
	public Class<?> getDefaultServerType()
	{
		if(javaTypes == null)
		{
			return null;
		}
		
		return javaTypes[0];
	}
}
