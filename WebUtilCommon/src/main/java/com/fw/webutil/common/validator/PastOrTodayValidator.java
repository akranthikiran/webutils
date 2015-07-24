package com.fw.webutil.common.validator;

import java.util.Date;

import javax.validation.ConstraintValidator;
import javax.validation.ConstraintValidatorContext;

import com.fw.webutil.common.validator.annotations.PastOrToday;

public class PastOrTodayValidator implements ConstraintValidator<PastOrToday, Date>
{
	@Override
	public void initialize(PastOrToday matchWith)
	{
	}
	
	@Override
	public boolean isValid(Date value, ConstraintValidatorContext context)
	{
		Date today = new Date();
		return (value.compareTo(today) <= 0);
	}

}
