package com.fw.webutil.query;

import java.util.List;

public class ResultRecord implements IResultRecord
{
	private List<Object> values;
	private QueryResult queryResult;
	
	ResultRecord(QueryResult result, List<Object> values)
	{
		this.queryResult = result;
		this.values = values;
	}

	@Override
	public List<Object> getValues()
	{
		return values;
	}
	
	public Object getValueAtIndex(int idx)
	{
		return values.get(0);
	}
	
	public Object getValue(String name)
	{
		int index = queryResult.getIndex(name);
		
		if(index < 0)
		{
			return null;
		}
		
		return values.get(index);
	}
}
