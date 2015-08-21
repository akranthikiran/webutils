package com.fw.webutil.common.validator.annotations;

import java.lang.annotation.Documented;
import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

import javax.validation.Constraint;
import javax.validation.Payload;

import com.fw.webutil.common.validator.GreaterThanValidator;

@Documented
@Constraint(validatedBy = GreaterThanValidator.class)
@Target( { ElementType.METHOD, ElementType.FIELD })
@Retention(RetentionPolicy.RUNTIME)
public @interface GreaterThan
{
	public String field();
	
	public String message() default "{com.fw.webutil.common.validator.annotations.GreaterThan}";

	public Class<?>[] groups() default {};

	public Class<? extends Payload>[] payload() default {};

}
