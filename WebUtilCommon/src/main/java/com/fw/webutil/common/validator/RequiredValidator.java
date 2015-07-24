package com.fw.webutil.common.validator;

import javax.validation.ConstraintValidator;
import javax.validation.ConstraintValidatorContext;

import com.fw.webutil.common.validator.annotations.Required;

public class RequiredValidator implements ConstraintValidator<Required, Object>
{
	@Override
	public void initialize(Required matchWith)
	{
	}
	
	@Override
	public boolean isValid(Object value, ConstraintValidatorContext context)
	{
		if(value == null)
		{
			return false;
		}
		
		if((value instanceof String) && ((String)value).length() <= 0)
		{
			return false;
		}
		
		if((value instanceof Number) && ((Number)value).longValue() == 0)
		{
			return false;
		}
		
		return true;
	}

}
