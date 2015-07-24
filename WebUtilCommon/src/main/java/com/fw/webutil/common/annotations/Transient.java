package com.fw.webutil.common.annotations;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * Marks a field as transient, that is indicates it is not stored in DB.
 * Currently its more of marker annotation, no functionality is attached to it.
 * 
 * @author akkink1
 */
@Retention(RetentionPolicy.RUNTIME)
@Target(ElementType.FIELD)
public @interface Transient
{

}
