package com.fw.webutil.service.jobs;

import java.util.Map;
import java.util.Properties;

import org.quartz.SchedulerException;
import org.quartz.impl.StdSchedulerFactory;

/**
 * Quarts scheduler factory extension, which allows the properties to be set using simple setting
 * which in turn can be used in spring configuration file.
 * 
 * @author akiran
 */
public class QuartzScedulerFactory extends StdSchedulerFactory
{
	public void setProperties(Map<String, String> map) throws SchedulerException
	{
		Properties resProperties = new Properties();
		resProperties.putAll(map);
		
		super.initialize(resProperties);
	}
}
