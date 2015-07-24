package com.fw.webutil.common.validator;

import javax.validation.ConstraintValidator;
import javax.validation.ConstraintValidatorContext;

import com.fw.webutil.common.validator.annotations.BoxLimit;

public class BoxLimitValidator implements ConstraintValidator<BoxLimit, String>
{
	private int maxColumns;
	private int maxRows;
	
	@Override
	public void initialize(BoxLimit matchWith)
	{
		this.maxColumns = matchWith.maxColumns();
		this.maxRows = matchWith.maxRows();
	}
	
	@Override
	public boolean isValid(String value, ConstraintValidatorContext context)
	{
		if(value == null)
		{
			return false;
		}

		String lines[] = value.toString().split("\\n");
		
		if(maxRows > 0 && lines.length > maxRows)
		{
			return false;
		}
		
		if(maxRows > 0)
		{
			for(String line: lines)
			{
				if(line.length() > maxColumns)
				{
					return false;
				}
			}
		}
		
		return true;
	}

}
