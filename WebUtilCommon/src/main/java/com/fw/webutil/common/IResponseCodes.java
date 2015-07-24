package com.fw.webutil.common;

public interface IResponseCodes
{
	public String ERROR_UNKNOWN = "UNKNOWN";
	
	public String ERROR_UNIQUE_CONSTRAINT_VIOLATION = "UNIQUE_CONSTRAINT_VIOLATION";
	public String ERROR_NON_EXISTING_ENTITY_VIOLATION = "NON_EXISTING_ENTITY";
	public String ERROR_UNSATISFIED_RELATION = "UNSATISFIED_RELATION";
	public String VALIDATION_ERROR = "VALIDATION_ERROR";
	public String OP_ERROR = "OP_ERROR";
	
	public String SUCCESS = "SUCCESS";
}
