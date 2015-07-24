package com.fw.webutil.common;

import java.util.HashMap;
import java.util.Map;

public enum LovDataType
{
	STRING(String.class)
	{
		@Override
		public Object parse(String str)
		{
			return str;
		}
	}, 
	
	INTEGER(Integer.class)
	{
		@Override
		public Object parse(String str)
		{
			return Integer.parseInt(str);
		}
	}, 
	
	FLOAT(Float.class)
	{
		@Override
		public Object parse(String str)
		{
			return Float.parseFloat(str);
		}
	};

	private static Map<Class<?>, LovDataType> javaToType;
	
	private Class<?> javaType;
	
	private LovDataType(Class<?> javaType)
	{
		this.javaType = javaType;
	}

	public Class<?> getJavaType()
	{
		return javaType;
	}

	public static LovDataType getDataType(Class<?> javaType)
	{
		if(javaToType == null)
		{
			LovDataType types[] = LovDataType.values();
			Map<Class<?>, LovDataType> typeMap = new HashMap<>();
			
			for(LovDataType type: types)
			{
				typeMap.put(type.javaType, type);
			}
			
			LovDataType.javaToType = typeMap;
		}
		
		return javaToType.get(javaType);
	}


	public abstract Object parse(String str);
}
