package com.fw.webutil.common.validator;

import java.lang.reflect.InvocationTargetException;

import javax.validation.ConstraintValidatorContext;

import org.apache.commons.beanutils.BeanUtils;

import com.fw.webutil.common.validator.annotations.MatchWith;

public class MatchWithValidator implements ICrossFieldValidator<MatchWith, String>
{
	private String matchWithField;
	private Object bean;
	
	@Override
	public void initialize(MatchWith matchWith)
	{
		this.matchWithField = matchWith.field();
	}
	
	@Override
	public void setObject(Object object)
	{
		this.bean = object;
	}

	@Override
	public boolean isValid(String value, ConstraintValidatorContext context)
	{
		if(bean == null)
		{
			return true;
		}
		
		Object otherValue = null;
		
		try
		{
			otherValue = BeanUtils.getSimpleProperty(bean, matchWithField);
		}catch(IllegalAccessException | InvocationTargetException | NoSuchMethodException ex)
		{
			throw new IllegalStateException("Invalid/inaccessible property \"" + matchWithField +"\" specified with matchWith validator in bean: " + bean.getClass().getName());
		}
		
		if(value == null)
		{
			return (otherValue == null);
		}
		
		return value.equals(otherValue);
	}

}
