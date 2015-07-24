package com.fw.webutil.dao;

import java.util.Date;
import java.util.List;

import com.fw.persistence.ICrudRepository;
import com.fw.persistence.repository.annotations.Condition;
import com.fw.persistence.repository.annotations.Field;
import com.fw.webutil.entity.job.JobEntity;

public interface IJobRepository extends ICrudRepository<JobEntity>
{
	public List<JobEntity> findAllActiveJobs(@Condition("active") boolean active);
	
	public void updateExecutingFlag(@Field("executing") boolean executing, @Condition("id") String id);
	
	public void updateFirstExecutionTime(@Field("firstExeuctionOn") Date firstExecutionOn, @Condition("id") String id);
	
	public void updateExecutionTimes(@Field("lastExeuctionOn") Date executedOn, @Field("nextExeuctionOn") Date nextExecutionOn, 
			@Field("executing") boolean executing, @Condition("id") String id);
}
