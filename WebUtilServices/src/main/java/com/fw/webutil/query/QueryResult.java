package com.fw.webutil.query;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

public class QueryResult implements IQueryResult<ResultRecord>
{
	private Map<String, Integer> columnIndexes = new HashMap<>();
	private List<ResultRecord> records = new LinkedList<>();
	private List<String> columnNames;
	
	public QueryResult(List<String> columnNames)
	{
		if(columnNames == null)
		{
			throw new NullPointerException("Column names can not be null");
		}
		
		int index = 0;
		
		for(String name: columnNames)
		{
			columnIndexes.put(name, index);
			index++;
		}
		
		this.columnNames = new ArrayList<>(columnNames);
	}
	
	int getIndex(String column)
	{
		Integer index = columnIndexes.get(column);
		
		if(index == null)
		{
			return -1;
		}
		
		return index;
	}

	public void addRow(List<Object> values)
	{
		if(values == null)
		{
			throw new NullPointerException("Values can not be null");
		}
		
		if(values.size() != columnIndexes.size())
		{
			throw new IllegalArgumentException("Number of columns of record (" + values.size() + ")  is not matching with result column count: " + columnIndexes.size());
		}
		
		records.add(new ResultRecord(this, values));
	}
	
	@Override
	public List<ResultRecord> getRecords()
	{
		return records;
	}
	
	@Override
	public int getRecordCount()
	{
		return records.size();
	}
	
	@Override
	public int getColumnCount()
	{
		return columnIndexes.size();
	}
	
	@Override
	public List<String> getColumns()
	{
		return columnNames;
	}
}
