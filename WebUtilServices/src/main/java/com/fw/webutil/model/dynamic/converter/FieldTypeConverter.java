package com.fw.webutil.model.dynamic.converter;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

import org.springframework.stereotype.Service;

import com.fw.webutil.common.model.dynamic.FieldType;

@Service
@Retention(RetentionPolicy.RUNTIME)
@Target(ElementType.TYPE)
public @interface FieldTypeConverter
{
	public FieldType[] fieldTypes();
}
