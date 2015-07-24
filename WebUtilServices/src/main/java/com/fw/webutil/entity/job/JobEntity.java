package com.fw.webutil.entity.job;

import java.util.Date;

import com.fw.persistence.annotations.Column;
import com.fw.persistence.annotations.DataType;
import com.fw.persistence.annotations.IdField;
import com.fw.persistence.annotations.Table;
import com.fw.persistence.annotations.UniqueConstraint;
import com.fw.persistence.annotations.UniqueConstraints;
import com.fw.persistence.conversion.FieldConverter;
import com.fw.persistence.conversion.impl.JsonConverter;

@Table("FW_JOB_ENTITY")
@UniqueConstraints({
	@UniqueConstraint(name = "name", fields = {"name"})
})
public class JobEntity
{
	@IdField
	@Column(type = DataType.LONG)
	private String id;

	@Column(length = 50)
	private String name;
	
	@Column(length = 200)
	private String description;
	
	@Column(length = 50)
	private String jobType;
	
	@Column(type = DataType.STRING, length = 50)
	private JobExecutionType executionType;
	
	@Column(length = 500)
	private String jobSchedule;
	
	private Date firstExeuctionOn;
	
	private Date lastExeuctionOn;
	
	private Date nextExeuctionOn;

	@Column(type = DataType.STRING, length = 1000)
	@FieldConverter(converterType = JsonConverter.class)
	private Object configuration;

	private boolean active = true;
	
	private boolean executing = false;

	public String getId()
	{
		return id;
	}

	public void setId(String id)
	{
		this.id = id;
	}

	public String getName()
	{
		return name;
	}

	public void setName(String name)
	{
		this.name = name;
	}

	public String getDescription()
	{
		return description;
	}

	public void setDescription(String description)
	{
		this.description = description;
	}

	public String getJobType()
	{
		return jobType;
	}

	public void setJobType(String jobType)
	{
		this.jobType = jobType;
	}

	public JobExecutionType getExecutionType()
	{
		return executionType;
	}

	public void setExecutionType(JobExecutionType executionType)
	{
		this.executionType = executionType;
	}

	public String getJobSchedule()
	{
		return jobSchedule;
	}

	public void setJobSchedule(String jobSchedule)
	{
		this.jobSchedule = jobSchedule;
	}

	public Date getFirstExeuctionOn()
	{
		return firstExeuctionOn;
	}

	public void setFirstExeuctionOn(Date firstExeuctionOn)
	{
		this.firstExeuctionOn = firstExeuctionOn;
	}

	public Date getLastExeuctionOn()
	{
		return lastExeuctionOn;
	}

	public void setLastExeuctionOn(Date lastExeuctionOn)
	{
		this.lastExeuctionOn = lastExeuctionOn;
	}

	public Date getNextExeuctionOn()
	{
		return nextExeuctionOn;
	}

	public void setNextExeuctionOn(Date nextExeuctionOn)
	{
		this.nextExeuctionOn = nextExeuctionOn;
	}

	public Object getConfiguration()
	{
		return configuration;
	}

	public void setConfiguration(Object configuration)
	{
		this.configuration = configuration;
	}

	public boolean isActive()
	{
		return this.active;
	}

	public void setActive(boolean active)
	{
		this.active = active;
	}

	public boolean isExecuting()
	{
		return this.executing;
	}

	public void setExecuting(boolean executing)
	{
		this.executing = executing;
	}
}