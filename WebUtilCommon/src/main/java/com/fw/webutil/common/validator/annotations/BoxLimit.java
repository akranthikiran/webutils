package com.fw.webutil.common.validator.annotations;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

import javax.validation.Constraint;
import javax.validation.Payload;

import com.fw.webutil.common.validator.BoxLimitValidator;

@Constraint(validatedBy = BoxLimitValidator.class)
@Target( { ElementType.METHOD, ElementType.FIELD })
@Retention(RetentionPolicy.RUNTIME)
public @interface BoxLimit
{
	public String message() default "{com.fw.webutil.common.validator.annotations.BoxLimit}";

	public Class<?>[] groups() default {};

	public Class<? extends Payload>[] payload() default {};

	public int maxColumns() default 0;
	public int maxRows() default 0;
}
