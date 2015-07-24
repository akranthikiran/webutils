package com.fw.webutil.common.validator;

import java.lang.reflect.InvocationTargetException;
import java.util.Date;

import javax.validation.ConstraintValidatorContext;

import org.apache.commons.beanutils.BeanUtils;

import com.fw.webutil.common.validator.annotations.GreaterThan;

public class GreaterThanValidator implements ICrossFieldValidator<GreaterThan, Object>
{
	private String greaterThanField;
	private Object bean;
	
	@Override
	public void initialize(GreaterThan matchWith)
	{
		this.greaterThanField = matchWith.field();
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
			otherValue = BeanUtils.getSimpleProperty(bean, greaterThanField);
		}catch(IllegalAccessException | InvocationTargetException | NoSuchMethodException ex)
		{
			throw new IllegalStateException("Invalid/inaccessible property \"" + greaterThanField +"\" specified with matchWith validator in bean: " + bean.getClass().getName());
		}
		
		if(otherValue == null || !value.getClass().equals(otherValue.getClass()))
		{
			return true;
		}
		
		if(otherValue instanceof Number)
		{
			return (((Number)value).doubleValue() > ((Number)otherValue).doubleValue());
		}
		
		if(otherValue instanceof Date)
		{
			Date dateValue = (Date)value;
			Date otherDateValue = (Date)otherValue;
			
			return (dateValue.compareTo(otherDateValue) > 0);
		}
		
		return true;
	}

}
