package com.fw.webutil.service.conversion;

import java.lang.annotation.Annotation;
import java.lang.reflect.Field;
import java.lang.reflect.Method;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

class PropertyAccessor
{
	private static Logger logger = LogManager.getLogger(PropertyAccessor.class);
	
	private String name;
	private Method getter, setter;
	
	private String mappingName;

	public PropertyAccessor(String name, Method getter, Method setter)
	{
		this.name = name;
		this.getter = getter;
		this.setter = setter;
		
		//fetch map to annotation and check whether this property needs to be 
			// mapped to different property
		MapTo mapTo = getAnnotation(MapTo.class);
		mappingName = (mapTo != null) ? mapTo.value() : null;
	}
	
	public <A extends Annotation> A getAnnotation(Class<A> type)
	{
		A annotation = (getter != null) ? getter.getAnnotation(type) : null;
		annotation = (annotation == null && setter != null) ? setter.getAnnotation(type) : annotation;
		
		//check for mapTo annotation on corresponding field
		if(annotation == null)
		{
			try
			{
				Class<?> sourceClass = (getter != null) ? getter.getDeclaringClass() : setter.getDeclaringClass();
				Field field = sourceClass.getField(name);
				
				annotation = field.getAnnotation(type);
			}catch(Exception ex)
			{
				//ignore
			}
		}
		
		return annotation;
	}
	
	public String getName()
	{
		return name;
	}
	
	public String getMappingName()
	{
		return (mappingName != null) ? mappingName : name;
	}

	public Object getValue(Object bean)
	{
		if(getter == null)
		{
			return null;
		}
		
		try
		{
			return getter.invoke(bean);
		}catch(Exception ex)
		{
			logger.error("An error occurred while reading property: " + name, ex);
			throw new IllegalStateException("An error occurred while reading property: " + name, ex);
		}
	}
	
	public void setValue(Object bean, Object value)
	{
		if(setter == null)
		{
			return;
		}
		
		try
		{
			if(value == null && getPropertyType().isPrimitive())
			{
				return;
			}
			
			setter.invoke(bean, value);
		}catch(Exception ex)
		{
			logger.error("An error occurred while writing property: " + name, ex);
			throw new IllegalStateException("An error occurred while writing property: " + name, ex);
		}
	}
	
	public Class<?> getPropertyType()
	{
		return getter.getReturnType();
	}
}
