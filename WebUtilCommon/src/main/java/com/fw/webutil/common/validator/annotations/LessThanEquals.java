package com.fw.webutil.common.validator.annotations;

import java.lang.annotation.Documented;
import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

import javax.validation.Constraint;
import javax.validation.Payload;

import com.fw.webutil.common.validator.LessThanEqualsValidator;

@Documented
@Constraint(validatedBy = LessThanEqualsValidator.class)
@Target( { ElementType.METHOD, ElementType.FIELD })
@Retention(RetentionPolicy.RUNTIME)
public @interface LessThanEquals
{
	public String field();
	
	public String message() default "{LessThanEquals}";

	public Class<?>[] groups() default {};

	public Class<? extends Payload>[] payload() default {};

}
