package com.fw.webutil.common.validator;

import java.lang.reflect.InvocationTargetException;
import java.util.Date;

import javax.validation.ConstraintValidatorContext;

import org.apache.commons.beanutils.BeanUtils;

import com.fw.webutil.common.validator.annotations.LessThanEquals;

public class LessThanEqualsValidator implements ICrossFieldValidator<LessThanEquals, Object>
{
	private String lessThanField;
	private Object bean;
	
	@Override
	public void initialize(LessThanEquals matchWith)
	{
		this.lessThanField = matchWith.field();
	}
	
	@Override
	public void setObject(Object object)
	{
		this.bean = object;
	}

	@Override
	public boolean isValid(Object value, ConstraintValidatorContext context)
	{
		if(bean == null)
		{
			return true;
		}
		
		Object otherValue = null;
		
		try
		{
			otherValue = BeanUtils.getSimpleProperty(bean, lessThanField);
		}catch(IllegalAccessException | InvocationTargetException | NoSuchMethodException ex)
		{
			throw new IllegalStateException("Invalid/inaccessible property \"" + lessThanField +"\" specified with matchWith validator in bean: " + bean.getClass().getName());
		}
		
		if(otherValue == null || !value.getClass().equals(otherValue.getClass()))
		{
			return true;
		}
		
		if(otherValue instanceof Number)
		{
			return (((Number)value).doubleValue() <= ((Number)otherValue).doubleValue());
		}
		
		if(otherValue instanceof Date)
		{
			Date dateValue = (Date)value;
			Date otherDateValue = (Date)otherValue;
			
			return (dateValue.compareTo(otherDateValue) <= 0);
		}
		
		return true;
	}

}
