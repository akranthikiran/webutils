package com.fw.webutil.common.validator.annotations;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

import javax.validation.Constraint;
import javax.validation.Payload;

import com.fw.webutil.common.validator.MaxLenValidator;

/**
 * String Max length constraint annotation. Ensure target field value length <= specified length
 * @author akiran
 */
@Constraint(validatedBy = MaxLenValidator.class)
@Target( { ElementType.METHOD, ElementType.FIELD })
@Retention(RetentionPolicy.RUNTIME)
public @interface MaxLen
{
	public String message() default "{com.fw.webutil.common.validator.annotations.MaxLen}";

	public Class<?>[] groups() default {};

	public Class<? extends Payload>[] payload() default {};
	
	/**
	 * Maximum length constraint value
	 * @return
	 */
	public int value();

}
