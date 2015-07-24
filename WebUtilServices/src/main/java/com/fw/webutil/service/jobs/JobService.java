package com.fw.webutil.service.jobs;

import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import javax.annotation.PostConstruct;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.quartz.CronScheduleBuilder;
import org.quartz.JobBuilder;
import org.quartz.JobDetail;
import org.quartz.JobKey;
import org.quartz.Scheduler;
import org.quartz.SchedulerException;
import org.quartz.Trigger;
import org.quartz.TriggerBuilder;
import org.springframework.beans.factory.annotation.Autowired;

import com.fw.persistence.repository.RepositoryFactory;
import com.fw.webutil.dao.IJobRepository;
import com.fw.webutil.entity.job.JobEntity;
import com.fw.webutil.service.ClassScannerService;
import com.fw.webutil.service.RepositoryLoader;

/**
 * This service helps in scheduling the jobs. All the available job types will be scanned in class path
 * and added for configuration.
 * 
 * The class-scanner-service is made parameterized, so that application can choosed which packages to scan.
 * @author akiran
 */
public class JobService
{
	private static final Logger logger = LogManager.getLogger(RepositoryLoader.class);

	@Autowired
	private RepositoryFactory repositoryFactory;
	
	private ClassScannerService classScannerService;
	
	@Autowired
	private QuartzScedulerFactory quartzScedulerFactory;

	private Scheduler scheduler;
	
	private IJobRepository jobEntityRepository;
	
	private Map<String, Class<?>> jobTypeMap = new HashMap<>();
	
	private Set<String> configurableJobTypes = new HashSet<String>();

	public void setClassScannerService(ClassScannerService classScannerService)
	{
		this.classScannerService = classScannerService;
	}
	
	/**
	* Loads all the job types in class path
	*/
	private void loadJobTypes()
	{
		//load the job types
		Set<Class<?>> jobTypes = classScannerService.getClassesWithAnnotation(JobType.class);
		
		if(jobTypes != null)
		{
			JobType jobType = null;
			
			for(Class<?> type : jobTypes)
			{
				if(!IExecutable.class.isAssignableFrom(type))
				{
					throw new IllegalStateException("A non-executable (not implementing IExecutable) is marked as @JobType - " + type.getName());
				}
				
				jobType = type.getAnnotation(JobType.class);
				
				jobTypeMap.put(jobType.name(), type);
				
				if(!jobType.internal())
				{
					configurableJobTypes.add(jobType.name());
				}
			}
		}
	}
	
	/**
	* Loads the jobs configured in DB and adds them to quartz scheduler
	 * @throws SchedulerException 
	*/
	private void initializeConfiguredJobs()
	{
		List<JobEntity> jobs = jobEntityRepository.findAllActiveJobs(true);
		
		if(jobs == null)
		{
			return;
		}
		
		for(JobEntity jobEntity : jobs)
		{
			scheduleConfiguredJob(jobEntity);
			//TODO: schedule the jobs which are not executed because of restart 
		}
	}
	
	@SuppressWarnings({ "unchecked", "rawtypes" })
	private void initializeInternalJobs() throws SchedulerException
	{
		JobType jobType = null;
		String cronSchedule = null;
		Trigger trigger = null;
		JobDetail jobDetail = null;
		boolean runOnStart = false;
		
		TriggerBuilder triggerBuilder = null;
		
		for(Class<?> jobTypeClass: this.jobTypeMap.values())
		{
			jobType = jobTypeClass.getAnnotation(JobType.class);
			cronSchedule = jobType.schedule();
			runOnStart = jobType.runOnStart();
			
			if(cronSchedule == null && !runOnStart)
			{
				continue;
			}
			
			logger.debug("Initalizing internal job: " + jobTypeClass.getName());
			
			//create a trigger based on job cron string
			triggerBuilder = TriggerBuilder.newTrigger();
			
			if(cronSchedule != null)
			{
				triggerBuilder.withSchedule(CronScheduleBuilder.cronSchedule(cronSchedule)); 
			}
			
			if(runOnStart)
			{
				triggerBuilder.startNow();
			}
			
			trigger = triggerBuilder.build();
			
			//create the job detail from job entity
			jobDetail = JobBuilder.newJob()
					.ofType(JobWrapper.class)
					.usingJobData(JobWrapper.ATTR_JOB_TYPE, JobWrapper.JOB_TYPE_SIMPLE_JOB)
					.usingJobData(JobWrapper.ATTR_SIMPLE_JOB_TYPE, jobType.name())
					.build();

			//add the job to quartz scheduler
			scheduler.scheduleJob(jobDetail, trigger);
		}
	}
	
	public Class<?> getJobType(String name)
	{
		return jobTypeMap.get(name);
	}
	
	@PostConstruct
	private void loadJobs() throws SchedulerException
	{
		logger.debug("Loading jobs...");
		
		//get the required DB repositories
		jobEntityRepository = repositoryFactory.getRepository(IJobRepository.class);
		
		//load all available job types
		loadJobTypes();
		
		//initialize quartz scheduler
		scheduler = quartzScedulerFactory.getScheduler();
		scheduler.start();
		
		//initialize and add jobs to quartz scheduler
		initializeConfiguredJobs();
		
		//initialize internal jobs and add them to scheduler
		initializeInternalJobs();
	}
	
	public Set<String> getConfigurableJobTypes()
	{
		return Collections.unmodifiableSet(this.configurableJobTypes);
	}
	
	public void cancelConfiguredJob(String name)
	{
		JobKey jobKey = new JobKey(name);
		
		try
		{
			scheduler.deleteJob(jobKey);
		}catch(SchedulerException ex)
		{
			throw new IllegalStateException("An error occurred while canceling configured job: " + name, ex);
		}
	}

	public void scheduleConfiguredJob(JobEntity jobEntity)
	{
		Trigger trigger = null;
		JobDetail jobDetail = null;
		String triggerName = null;
		
		triggerName = JobWrapper.getTriggerNameForJob(jobEntity.getName());
		
		//create a trigger based on job cron string
		trigger = TriggerBuilder.newTrigger()
				.withIdentity(triggerName)
				.withSchedule(CronScheduleBuilder.cronSchedule(jobEntity.getJobSchedule()))
				.forJob(jobEntity.getName())
				.build();
		
		//create the job detail from job entity
		jobDetail = JobBuilder.newJob()
				.withIdentity(new JobKey(jobEntity.getName()))
				.ofType(JobWrapper.class)
				.usingJobData(JobWrapper.ATTR_JOB_ENTITY_ID, jobEntity.getId())
				.usingJobData(JobWrapper.ATTR_JOB_TYPE, JobWrapper.JOB_TYPE_CONFIGURED_JOB)
				.usingJobData(JobWrapper.ATTR_TRIGGER_NAME, triggerName)
				.build();

		try
		{
			//add the job to quartz scheduler
			scheduler.scheduleJob(jobDetail, trigger);
		}catch(Exception ex)
		{
			throw new IllegalStateException("An error occurred while scheduling job - " + jobEntity.getName(), ex);
		}
	}
}
