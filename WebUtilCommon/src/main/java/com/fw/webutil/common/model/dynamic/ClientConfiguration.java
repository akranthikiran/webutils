package com.fw.webutil.common.model.dynamic;

import java.io.Serializable;

import com.fw.webutil.common.annotations.LovType;

public class ClientConfiguration implements Serializable
{
	private static final long serialVersionUID = 1L;
	
	private LovType lovType;
	/**
	 * This holds query name for lovType = QUERY_TYPE
	 */
	private String queryName;
	private String parentField;

	public LovType getLovType()
	{
		return lovType;
	}

	public void setLovType(LovType lovType)
	{
		this.lovType = lovType;
	}

	public String getParentField()
	{
		return parentField;
	}

	public void setParentField(String parentField)
	{
		this.parentField = parentField;
	}

	public String getQueryName()
	{
		return queryName;
	}

	public void setQueryName(String queryName)
	{
		this.queryName = queryName;
	}
}
