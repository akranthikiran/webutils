package com.fw.webutil.common.validator;

import java.lang.reflect.InvocationTargetException;

import javax.validation.ConstraintValidatorContext;

import org.apache.commons.beanutils.BeanUtils;

import com.fw.webutil.common.validator.annotations.MandatoryOption;

public class MandatoryOptionValidator implements ICrossFieldValidator<MandatoryOption, Object>
{
	private String fields[];
	private Object bean;
	
	@Override
	public void initialize(MandatoryOption mandatoryOption)
	{
		this.fields = mandatoryOption.fields();
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
		
		if(value == null)
		{
			return true;
		}
		
		Object otherValue = null;
		String field = null;
		
		try
		{
			for(String otherField: fields)
			{
				field = otherField;
				otherValue = BeanUtils.getSimpleProperty(bean, otherField);
				
				if(otherValue != null)
				{
					return true;
				}
			}
		}catch(IllegalAccessException | InvocationTargetException | NoSuchMethodException ex)
		{
			throw new IllegalStateException("Invalid/inaccessible property \"" + field +"\" specified with MandatoryOption validator in bean: " + bean.getClass().getName());
		}
		
		return false;
	}

}
