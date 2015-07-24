package com.fw.webutil.common.validator;

import java.util.regex.Pattern;

import javax.validation.ConstraintValidator;
import javax.validation.ConstraintValidatorContext;

import com.fw.webutil.common.validator.annotations.Mispattern;

public class MispatternValidator implements ConstraintValidator<Mispattern, String> 
{
	private Pattern pattern;
	
	@Override
	public void initialize(Mispattern constraintAnnotation)
	{
		String regexp = constraintAnnotation.regexp();
		pattern = Pattern.compile(regexp);
	}

	@Override
	public boolean isValid(String value, ConstraintValidatorContext context)
	{
		return !pattern.matcher(value).find();
	}

}
