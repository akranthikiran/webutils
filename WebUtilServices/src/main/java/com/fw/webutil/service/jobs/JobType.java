package com.fw.webutil.service.jobs;

import java.lang.annotation.ElementType;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Retention;
import java.lang.annotation.Target;

/**
 * Annotation to mark a type as internal job. This job will be executed according to 
 * "cron" string specified in this annotation
 * 
 * @author akiran
 */
@Retention(RetentionPolicy.RUNTIME)
@Target(ElementType.TYPE)
public @interface JobType
{
	public String name();
	public String schedule() default "";
	public boolean internal() default false;
	public boolean runOnStart() default false;
}
