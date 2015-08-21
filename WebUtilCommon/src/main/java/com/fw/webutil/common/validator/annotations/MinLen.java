package com.fw.webutil.common.validator.annotations;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

import javax.validation.Constraint;
import javax.validation.Payload;

import com.fw.webutil.common.validator.MinLenValidator;

/**
 * String Min length constraint annotation. Ensure target field value length >= specified length
 * @author akiran
 */
@Constraint(validatedBy = MinLenValidator.class)
@Target( { ElementType.METHOD, ElementType.FIELD })
@Retention(RetentionPolicy.RUNTIME)
public @interface MinLen
{
	public String message() default "{com.fw.webutil.common.validator.annotations.MinLen}";

	public Class<?>[] groups() default {};

	public Class<? extends Payload>[] payload() default {};
	
	/**
	 * Minimum length constraint value
	 * @return
	 */
	public int value();

}
