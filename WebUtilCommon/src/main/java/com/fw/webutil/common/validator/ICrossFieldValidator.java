package com.fw.webutil.common.validator;

import java.lang.annotation.Annotation;

import javax.validation.ConstraintValidator;

public interface ICrossFieldValidator<A extends Annotation, T> extends ConstraintValidator<A, T> 
{
	public void setObject(Object object);
}
