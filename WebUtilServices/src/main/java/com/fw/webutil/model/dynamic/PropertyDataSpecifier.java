package com.fw.webutil.model.dynamic;

import java.lang.annotation.Annotation;
import java.lang.reflect.Method;
import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class PropertyDataSpecifier implements IDataSpecifier
{
	private String name;
	private Method getter;
	private Method setter;
	
	public PropertyDataSpecifier(Method getter, Method setter)
	{
		if(setter == null && getter == null)
		{
			throw new NullPointerException("Both setter and getter can not be null");
		}
		
		this.getter = getter;
		this.setter = setter;
	}

	@Override
	public Class<?> getType()
	{
		if(getter != null)
		{
			return getter.getReturnType();
		}
		
		Class<?> args[] = setter.getParameterTypes();
		return args[0];
	}

	@Override
	public <A extends Annotation> A getAnnotation(Class<A> annotationType)
	{
		A annotation = (getter != null) ? getter.getAnnotation(annotationType) : null;
		
		if(annotation == null && setter != null)
		{
			annotation = setter.getAnnotation(annotationType);
		}
		
		return annotation;
	}

	@Override
	public String getName()
	{
		if(this.name != null)
		{
			return this.name;
		}
		
		String name = null; 
		
		if(getter != null)
		{
			name = getter.getName();
			
			if(name.startsWith("get"))
			{
				name = name.substring(3);
			}
			else if(name.startsWith("is"))
			{
				name = name.substring(2);
			}
			else
			{
				throw new IllegalArgumentException("Invalid getter specified: " + getter.getName() + "()");
			}
		}
		else
		{
			name = setter.getName();

			if(name.startsWith("set"))
			{
				name = name.substring(3);
			}
			else
			{
				throw new IllegalArgumentException("Invalid setter specified: " + setter.getName() + "()");
			}
			
			name = name.substring(3);
		}

		name =  name.substring(0, 1).toLowerCase() + name.substring(1);
		this.name = name;
		
		return name;
	}

	@Override
	public Type getGenericType()
	{
		if(getter != null)
		{
			return getter.getGenericReturnType();
		}
		
		Type param[] = setter.getGenericParameterTypes();
		return param[0];
	}

	@Override
	public Annotation[] getAnnotations()
	{
		List<Annotation> annotations = new ArrayList<>();
		
		if(getter != null)
		{
			annotations.addAll(Arrays.asList(getter.getAnnotations()));
		}
		
		if(setter != null)
		{
			annotations.addAll(Arrays.asList(setter.getAnnotations()));
		}
		
		return annotations.toArray(new Annotation[0]);
	}
}
