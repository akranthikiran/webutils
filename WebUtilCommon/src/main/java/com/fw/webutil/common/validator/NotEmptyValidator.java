package com.fw.webutil.common.validator;

import java.util.Collection;

import javax.validation.ConstraintValidator;
import javax.validation.ConstraintValidatorContext;

import com.fw.webutil.common.validator.annotations.NotEmpty;

public class NotEmptyValidator implements ConstraintValidator<NotEmpty, Object> 
{
	@Override
	public void initialize(NotEmpty constraintAnnotation)
	{
	}

	@SuppressWarnings("rawtypes")
	@Override
	public boolean isValid(Object value, ConstraintValidatorContext context)
	{
		if(value == null)
		{
			return false;
		}
		
		if(value instanceof String)
		{
			return !((String)value).isEmpty();
		}
		
		if(value instanceof Collection)
		{
			return !((Collection)value).isEmpty();
		}
		
		return true;
	}

}
