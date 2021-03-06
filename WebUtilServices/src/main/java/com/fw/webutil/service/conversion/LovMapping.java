package com.fw.webutil.service.conversion;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

@org.springframework.stereotype.Service
@Retention(RetentionPolicy.RUNTIME)
@Target(ElementType.TYPE)
public @interface LovMapping
{
	public String query();
}
