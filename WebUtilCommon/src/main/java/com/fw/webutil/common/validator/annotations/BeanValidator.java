package com.fw.webutil.common.validator.annotations;

import org.springframework.validation.Validator;

public @interface BeanValidator
{
	public Class<? extends Validator> validatedBy();
}
