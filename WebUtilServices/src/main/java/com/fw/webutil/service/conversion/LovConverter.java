package com.fw.webutil.service.conversion;

import org.springframework.beans.factory.annotation.Autowired;

import com.fw.webutil.dao.LovDao;

public class LovConverter implements Converter<String, Object>
{
	@Autowired
	private LovDao lovDao;
	
	private String query;

	public LovConverter(String query)
	{
		this.query = query;
	}

	@Override
	public Object convert(String property, String source)
	{
		return lovDao.getLovValue(query, null, source);
	}
	
	
}
