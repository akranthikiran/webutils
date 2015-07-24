package com.fw.webutil.common.validator;

import java.lang.annotation.Annotation;
import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;

import javax.validation.Constraint;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.MessageSource;
import org.springframework.validation.Errors;
import org.springframework.validation.Validator;

import com.fw.webutil.common.validator.annotations.BeanValidator;

public class GlobalValidator implements Validator
{
	@Autowired
	private MessageSource messageSource;
	
	@Autowired
	private Validator validator;
	
	@SuppressWarnings("rawtypes")
	private static class AnnotationDetails
	{
		private Annotation annotation;
		private ICrossFieldValidator validators[];
		
		public AnnotationDetails(Annotation annotation, ICrossFieldValidator crossFieldValidators[])
		{
			this.annotation = annotation;
			this.validators = crossFieldValidators;
		}
	}
	
	private Map<Class<?>, Object> validatorMap = new HashMap<>();
	
	@Override
	public boolean supports(Class<?> clazz)
	{
		return true;
	}

	@Override
	public void validate(Object target, Errors errors)
	{
		validator.validate(target, errors);
		
		validateFields(target, errors);
		
		BeanValidator beanValidator = target.getClass().getAnnotation(BeanValidator.class);
		Validator validator = null;
		
		if(beanValidator != null)
		{
			validator = (Validator)getValidator(beanValidator.validatedBy());
			validator.validate(target, errors);
		}
	}
	
	private void validateFields(Object target, Errors errors)
	{
		Class<?> type = target.getClass();
		
		while(type != null)
		{
			if(type.getName().startsWith("java"))
			{
				break;
			}
			
			try
			{
				validateType(type, target, errors);
			}catch(IllegalAccessException e)
			{
				throw new IllegalStateException("An error occurred while performing validation on: " + target, e);
			}
			
			type = type.getSuperclass();
		}
	}
	
	@SuppressWarnings("rawtypes")
	private AnnotationDetails getConstraint(Field field)
	{
		Annotation annotations[] = field.getAnnotations();
		Constraint constraint = null;
		Class<?> types[] = null;
		
		List<ICrossFieldValidator> validatorLst = new ArrayList<>();
		
		for(Annotation annotation: annotations)
		{
			constraint = annotation.annotationType().getAnnotation(Constraint.class);
			
			if(constraint == null)
			{
				continue;
			}
			
			types = constraint.validatedBy();
			
			if(types == null || types.length == 0)
			{
				continue;
			}
			
			for(Class<?> type: types)
			{
				if(!ICrossFieldValidator.class.isAssignableFrom(type))
				{
					continue;
				}
				
				validatorLst.add((ICrossFieldValidator)getValidator(type));
			}
			
			if(validatorLst.isEmpty())
			{
				continue;
			}
			
			return new AnnotationDetails(annotation, validatorLst.toArray(new ICrossFieldValidator[0]));
		}
		
		return null;
	}
	
	private Object getValidator(Class<?> type)
	{
		Object validator =  validatorMap.get(type);
		
		if(validator == null)
		{
			try
			{
				validator = type.newInstance();
			}catch(Exception e)
			{
				throw new IllegalStateException("An error occurred while creating validator instance: "+ type.getName(), e);
			}
			
			validatorMap.put(type, validator);
		}
		
		return validator;
	}
	
	private void addError(Field field, Annotation annotation, Errors errors)
	{
		Locale locale = null;
		
		//TODO: Need to check how to get local here (without depending on j2ee classes)
		
		String errorCode = annotation.getClass().getSimpleName() + "." + field.getName();
		String mssg = messageSource.getMessage(errorCode, null, null, locale);
		
		if(mssg != null)
		{
			errors.rejectValue(field.getName(), errorCode);
			return;
		}
		
		errorCode = annotation.getClass().getSimpleName();
		mssg = messageSource.getMessage(errorCode, null, null, locale);
		
		if(mssg != null)
		{
			errors.rejectValue(field.getName(), errorCode, "error" + annotation.getClass().getSimpleName() + "." + field.getName());
			return;
		}
	}
	
	@SuppressWarnings({"rawtypes", "unchecked"})
	private void validateType(Class<?> type, Object target, Errors errors) throws IllegalAccessException
	{
		Field fields[] = type.getDeclaredFields();
		AnnotationDetails annotationDetails = null;
		
		for(Field field: fields)
		{
			annotationDetails = getConstraint(field);
			
			if(annotationDetails == null)
			{
				continue;
			}
			
			for(ICrossFieldValidator validator: annotationDetails.validators)
			{
				validator.initialize(annotationDetails.annotation);
				validator.setObject(target);
				
				if(!field.isAccessible())
				{
					field.setAccessible(true);
				}

				if(!validator.isValid(field.get(target), null))
				{
					addError(field, annotationDetails.annotation, errors);
				}
			}
		}
	}

}
