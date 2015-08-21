package com.fw.webutil.common.validator;

import javax.validation.ConstraintValidator;
import javax.validation.ConstraintValidatorContext;

import com.fw.webutil.common.validator.annotations.MaxLen;

/**
 * Validator of max-length {@link MaxLen} validation.
 * @author akiran
 */
public class MaxLenValidator implements ConstraintValidator<MaxLen, String>
{
	private int maxLength;
	
	/* (non-Javadoc)
	 * @see javax.validation.ConstraintValidator#initialize(java.lang.annotation.Annotation)
	 */
	@Override
	public void initialize(MaxLen maxLen)
	{
		this.maxLength = maxLen.value();
	}
	
	/* (non-Javadoc)
	 * @see javax.validation.ConstraintValidator#isValid(java.lang.Object, javax.validation.ConstraintValidatorContext)
	 */
	@Override
	public boolean isValid(String value, ConstraintValidatorContext context)
	{
		//if no value is specified, ignore validation
		if(value == null)
		{
			return true;
		}
		
		//ensure value length is lesser or equal to specified length value
		return (value.length() <= maxLength);
	}

}
