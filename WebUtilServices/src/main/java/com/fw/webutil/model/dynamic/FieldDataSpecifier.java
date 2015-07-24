package com.fw.webutil.model.dynamic;

import java.lang.annotation.Annotation;
import java.lang.reflect.Field;
import java.lang.reflect.Type;

public class FieldDataSpecifier implements IDataSpecifier
{
	private Field field;

	public FieldDataSpecifier(Field field)
	{
		if(field == null)
		{
			throw new NullPointerException("Field can not be null");
		}
		
		this.field = field;
	}

	@Override
	public Class<?> getType()
	{
		return field.getType();
	}

	@Override
	public <A extends Annotation>A getAnnotation(Class<A> annotationType)
	{
		return field.getAnnotation(annotationType);
	}

	@Override
	public String getName()
	{
		return field.getName();
	}

	@Override
	public Type getGenericType()
	{
		return field.getGenericType();
	}

	@Override
	public Annotation[] getAnnotations()
	{
		return field.getAnnotations();
	}
}
