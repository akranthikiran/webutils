package com.fw.webutil.model.dynamic;

import java.lang.annotation.Annotation;
import java.lang.reflect.Type;

public interface IDataSpecifier
{
	public String getName();
	
	public Type getGenericType();
	public Class<?> getType();
	
	public <A extends Annotation> A getAnnotation(Class<A> annotationType);
	public Annotation[] getAnnotations();
}
