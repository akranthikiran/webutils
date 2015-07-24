package com.fw.webutil.entity.job;

import java.util.Date;

import com.fw.persistence.annotations.Column;
import com.fw.persistence.annotations.DataType;
import com.fw.persistence.annotations.ForeignConstraint;
import com.fw.persistence.annotations.ForeignConstraints;
import com.fw.persistence.annotations.IdField;
import com.fw.persistence.annotations.Mapping;
import com.fw.persistence.annotations.Table;

@Table("FW_JOB_EXECUTION")
@ForeignConstraints({
	@ForeignConstraint(name = "jobId", foreignEntity = JobEntity.class, mappings = {@Mapping(from = "jobId", to = "id")})		
})
public class JobExecutionEntity
{
	@IdField
	@Column(type = DataType.LONG)
	private String id;
	
	@Column(type = DataType.LONG)
	private String jobId;
	
	private Date executedOn;
	
	private Date completedOn;
	
	@Column(type = DataType.STRING)
	private JobExecutionStatus status;
	
	@Column(name = "JOB_OUTPUT", type = DataType.CLOB)
	private String output;
	
	public JobExecutionEntity()
	{}
	
	public JobExecutionEntity(String jobId, Date executedOn, Date completedOn, JobExecutionStatus status, String output)
	{
		this.jobId = jobId;
		this.executedOn = executedOn;
		this.completedOn = completedOn;
		this.status = status;
		this.output = output;
	}

	public String getId()
	{
		return id;
	}

	public void setId(String id)
	{
		this.id = id;
	}

	public String getJobId()
	{
		return jobId;
	}

	public void setJobId(String jobId)
	{
		this.jobId = jobId;
	}

	public Date getExecutedOn()
	{
		return executedOn;
	}

	public void setExecutedOn(Date executedOn)
	{
		this.executedOn = executedOn;
	}

	public Date getCompletedOn()
	{
		return completedOn;
	}

	public void setCompletedOn(Date completedOn)
	{
		this.completedOn = completedOn;
	}

	public JobExecutionStatus getStatus()
	{
		return status;
	}

	public void setStatus(JobExecutionStatus status)
	{
		this.status = status;
	}

	public String getOutput()
	{
		return output;
	}

	public void setOutput(String output)
	{
		this.output = output;
	}

}
