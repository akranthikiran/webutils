package com.fw.webutil.common.model.dynamic;

import java.io.Serializable;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;

import com.fw.webutil.common.annotations.Model;
import com.fw.webutil.common.annotations.ServerField;

@Model
public class ValidatorConfiguration implements Serializable
{
	private static final long serialVersionUID = 1L;
	
	private String name;
	private String validatorType;
	private boolean global;
	private String errorMessage;
	
	@ServerField
	public Map<String, Object> values = new HashMap<String, Object>();
	
	public ValidatorConfiguration()
	{}
	
	public ValidatorConfiguration(String name)
	{
		this.name = name;
	}
	
	public String getName()
	{
		return name;
	}

	public void setName(String name)
	{
		this.name = name;
	}

	public String getValidatorType()
	{
		return validatorType;
	}

	public void setValidatorType(String validatorType)
	{
		this.validatorType = validatorType;
	}
	
	public boolean isGlobal()
	{
		return global;
	}

	public void setGlobal(boolean global)
	{
		this.global = global;
	}

	/** 
	 * Adds value to {@link #values values}
	 *
	 * @param name name to be added
	 * @param value value to be added
	 */
	 public void setValue(String name, Object value)
	 {
		 if(values == null)
		 {
			 values = new HashMap<String, Object>();
		 }
		 
		 values.put(name, value);
	 }
	 
	 /** 
	  * Gets value from {@link #values values}
	  *
	  * @param name name for fetching
	  */
	  public Object getValue(String name)
	  {
		  if(values == null)
		  {
			  return null;
		  }
		  
		  return values.get(name);
	  }

	  public Set<String> getValueKeys()
	  {
		  return values.keySet();
	  }

	public String getErrorMessage()
	{
		return errorMessage;
	}

	public void setErrorMessage(String errorMessage)
	{
		this.errorMessage = errorMessage;
	}
}
