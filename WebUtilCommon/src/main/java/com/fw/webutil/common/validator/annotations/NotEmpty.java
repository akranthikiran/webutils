package com.fw.webutil.common.validator.annotations;

import java.lang.annotation.Documented;
import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

import javax.validation.Constraint;
import javax.validation.Payload;

import com.fw.webutil.common.validator.NotEmptyValidator;

@Documented
@Constraint(validatedBy = NotEmptyValidator.class)
@Target( { ElementType.METHOD, ElementType.FIELD })
@Retention(RetentionPolicy.RUNTIME)
public @interface NotEmpty
{
	public String message() default "{com.fw.webutil.common.validator.annotations.NotEmpty}";

	public Class<?>[] groups() default {};

	public Class<? extends Payload>[] payload() default {};
}
