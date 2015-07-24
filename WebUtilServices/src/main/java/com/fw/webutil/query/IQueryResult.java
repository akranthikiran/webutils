package com.fw.webutil.query;

import java.util.List;

public interface IQueryResult<R extends IResultRecord>
{
	public List<R> getRecords();
	public int getRecordCount();
	public int getColumnCount();
	public List<String> getColumns();
}
