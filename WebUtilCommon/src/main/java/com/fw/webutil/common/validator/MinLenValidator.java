package com.fw.webutil.common.validator;

import javax.validation.ConstraintValidator;
import javax.validation.ConstraintValidatorContext;

import com.fw.webutil.common.validator.annotations.MinLen;

/**
 * Validator of min-length {@link MinLen} validation.
 * @author akiran
 */
public class MinLenValidator implements ConstraintValidator<MinLen, String>
{
	private int minLength;
	
	/* (non-Javadoc)
	 * @see javax.validation.ConstraintValidator#initialize(java.lang.annotation.Annotation)
	 */
	@Override
	public void initialize(MinLen minLen)
	{
		this.minLength = minLen.value();
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
		
		//ensure value length is greater or equal to specified length value
		return (value.length() >= minLength);
	}

}
