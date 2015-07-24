package com.fw.webutil.common.validator;

import java.util.Calendar;
import java.util.Date;
import java.util.GregorianCalendar;

import javax.validation.ConstraintValidator;
import javax.validation.ConstraintValidatorContext;

import com.fw.webutil.common.validator.annotations.ThisYearOnly;

public class ThisYearOnlyValidator implements ConstraintValidator<ThisYearOnly, Date>
{
	@Override
	public void initialize(ThisYearOnly matchWith)
	{
	}
	
	@Override
	public boolean isValid(Date value, ConstraintValidatorContext context)
	{
		GregorianCalendar today = new GregorianCalendar();
		
		GregorianCalendar valueCalendar = new GregorianCalendar();
		valueCalendar.setTime(value);
		
		return (today.get(Calendar.YEAR) == valueCalendar.get(Calendar.YEAR));
	}

}
