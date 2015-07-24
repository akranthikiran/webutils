package com.fw.webutil.util;

import java.io.IOException;
import java.util.Calendar;
import java.util.Date;
import java.util.GregorianCalendar;

import com.fasterxml.jackson.core.JsonParseException;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonMappingException;
import com.fw.dao.qry.QueryFunction;

import conm.fw.common.util.JsonWrapper;

public class QueryFunctions
{
	@QueryFunction(minArgCount = 1)
	public static String toJson(Object value) throws JsonProcessingException
	{
		if(value == null)
		{
			return "";
		}
		
		return JsonWrapper.format(value);
	}
	
	@QueryFunction(minArgCount = 1)
	public static Object parseJson(String jsonVal) throws JsonParseException, JsonMappingException, IOException
	{
		if(jsonVal == null || jsonVal.trim().length() == 0)
		{
			return null;
		}
			
		return JsonWrapper.parse(jsonVal);
	}
	
	@QueryFunction(minArgCount = 1)
	public static String searchString(String str)
	{
		if(str == null)
		{
			return null;
		}
		
		if(str.indexOf("*") >= 0)
		{
			return str.replace("*", "%").toLowerCase();
		}
		
		return "%" + str.toLowerCase() + "%";
	}
	
	@QueryFunction(minArgCount = 0)
	public static int month(Date date)
	{
		if(date == null)
		{
			date = new Date();
		}
		
		GregorianCalendar calendar = new GregorianCalendar();
		calendar.setTime(date);
		
		return (calendar.get(Calendar.MONTH) + 1);
	}

	@QueryFunction(minArgCount = 0)
	public static int year(Date date)
	{
		if(date == null)
		{
			date = new Date();
		}
		
		GregorianCalendar calendar = new GregorianCalendar();
		calendar.setTime(date);
		
		return calendar.get(Calendar.YEAR);
	}
}
