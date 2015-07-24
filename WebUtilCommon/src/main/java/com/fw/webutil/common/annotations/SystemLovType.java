package com.fw.webutil.common.annotations;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

import com.fw.webutil.common.LovDataType;

@Retention(RetentionPolicy.RUNTIME)
@Target(ElementType.TYPE)
public @interface SystemLovType
{
	public String name();
	public String description() default "";
	public LovDataType type();
}
