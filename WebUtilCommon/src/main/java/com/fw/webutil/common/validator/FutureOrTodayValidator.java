package com.fw.webutil.common.validator;

import java.util.Date;

import javax.validation.ConstraintValidator;
import javax.validation.ConstraintValidatorContext;

import com.fw.webutil.common.validator.annotations.FutureOrToday;

public class FutureOrTodayValidator implements ConstraintValidator<FutureOrToday, Date>
{
	@Override
	public void initialize(FutureOrToday matchWith)
	{
	}
	
	@Override
	public boolean isValid(Date value, ConstraintValidatorContext context)
	{
		Date today = new Date();
		return (value.compareTo(today) >= 0);
	}

}
