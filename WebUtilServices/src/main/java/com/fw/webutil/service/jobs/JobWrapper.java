package com.fw.webutil.service.jobs;

import java.io.PrintWriter;
import java.io.StringWriter;
import java.util.Date;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.quartz.Job;
import org.quartz.JobDataMap;
import org.quartz.JobExecutionContext;
import org.quartz.JobExecutionException;
import org.quartz.Trigger;
import org.quartz.TriggerKey;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationContext;
import org.springframework.web.context.support.SpringBeanAutowiringSupport;

import com.fw.persistence.repository.RepositoryFactory;
import com.fw.webutil.dao.IJobExecutionRepository;
import com.fw.webutil.dao.IJobRepository;
import com.fw.webutil.entity.job.JobEntity;
import com.fw.webutil.entity.job.JobExecutionEntity;
import com.fw.webutil.entity.job.JobExecutionStatus;
import com.fw.webutil.service.RepositoryLoader;

public class JobWrapper implements Job
{
	public static final String ATTR_JOB_TYPE = "jobType";
	public static final String ATTR_JOB_ENTITY_ID = "jobEntityId";
	public static final String ATTR_TRIGGER_NAME = "triggerName";
	public static final String ATTR_SIMPLE_JOB_TYPE = "simpleJobType";
	
	public static final String JOB_TYPE_CONFIGURED_JOB = "configuredJob";
	public static final String JOB_TYPE_SIMPLE_JOB = "simpleJob";
	
	public static final String TRIGGER_POSTFIX = "-Trigger";
	
	private static final Logger logger = LogManager.getLogger(RepositoryLoader.class);
	
	@Autowired
	private RepositoryFactory repositoryFactory;
	
	@Autowired
	private JobService jobService;
	
	@Autowired
	private ApplicationContext applicationContext;
	
	private IJobRepository jobRepository;
	private IJobExecutionRepository jobExeutionRepository;
	
	public JobWrapper()
	{
		SpringBeanAutowiringSupport.processInjectionBasedOnCurrentContext(this);
		
		this.jobRepository = repositoryFactory.getRepository(IJobRepository.class);
		this.jobExeutionRepository = repositoryFactory.getRepository(IJobExecutionRepository.class);
	}
	
	private static synchronized void markJobAsExecuting(JobEntity jobEntity, IJobRepository jobRepository)
	{
		if(jobEntity.isExecuting())
		{
			throw new JobException("Specified job is alredy under execution: " + jobEntity.getName());
		}
		
		jobEntity.setExecuting(true);
		jobRepository.updateExecutingFlag(true, jobEntity.getId());
	}
	
	@SuppressWarnings({ "unchecked", "rawtypes" })
	private void executeConfiguredJob(JobDataMap dataMap, JobExecutionContext jobExecutionContext)
	{
		String jobEntityId = dataMap.getString(ATTR_JOB_ENTITY_ID);
		JobEntity jobEntity = jobRepository.findById(jobEntityId);
		
		logger.debug("Executing confiugred job: " + jobEntity.getName());
		
		//try to mark/lock the job-entity for execution
		markJobAsExecuting(jobEntity, jobRepository);
		
		Class<?> jobType = jobService.getJobType(jobEntity.getJobType());
		String output = null;
		Date executedOn = new Date();
		JobExecutionStatus status = null;
		
		try
		{
			//create the job instance and autowire dependencies
			IExecutable<Object> job =  (IExecutable)jobType.newInstance();
			applicationContext.getAutowireCapableBeanFactory().autowireBean(job);
			
			//execute the job and obtain the output
			output = job.execute(jobEntity.getConfiguration());
			status = JobExecutionStatus.SUCCESSFUL;
			
			logger.debug("Job completed successfully");
		}catch(Exception ex)
		{
			logger.error("An error occurred while executing job - " + jobEntity.getName(), ex);
			//in case of error set the exception stack trace as output and status as ERRORED
			StringWriter writer = new StringWriter();
			PrintWriter pwriter = new PrintWriter(writer);
			
			ex.printStackTrace(pwriter);
			output = pwriter.toString();
			status = JobExecutionStatus.ERRORED;
		}
		
		//if this is first execution set first execution date
		if(jobEntity.getFirstExeuctionOn() == null)
		{
			jobRepository.updateFirstExecutionTime(executedOn, jobEntityId);
		}

		//get the corresponding trigger for the job
		String triggerName = dataMap.getString(ATTR_TRIGGER_NAME);
		Trigger trigger = null;
		
		try
		{
			trigger = jobExecutionContext.getScheduler().getTrigger(new TriggerKey(triggerName));
		}catch(Exception ex)
		{
			throw new IllegalStateException("An error occurred while fetching trigger", ex);
		}
		
		//update the job-entity with approp execution dates and times
		jobEntity.setExecuting(false);
		jobRepository.updateExecutionTimes(executedOn, trigger.getFireTimeAfter(new Date()), false, jobEntityId);
		
		//add job execution instance for monitoring
		JobExecutionEntity jobExecutionEntity = new JobExecutionEntity(jobEntityId, executedOn, new Date(), status, output);
		jobExeutionRepository.save(jobExecutionEntity);
	}
	
	@SuppressWarnings({ "rawtypes", "unchecked" })
	private void executeSimpleJob(JobDataMap dataMap)
	{
		String jobTypeName = dataMap.getString(ATTR_SIMPLE_JOB_TYPE);
		
		logger.debug("Executing simple job: " + jobTypeName);
		
		Class<?> jobType = jobService.getJobType(jobTypeName);
		
		try
		{
			//create the job instance and autowire dependencies
			IExecutable<Object> job =  (IExecutable)jobType.newInstance();
			applicationContext.getAutowireCapableBeanFactory().autowireBean(job);
			
			//execute the job and obtain the output
			job.execute(null);
			
			logger.debug("Simple job executed successfully");
		}catch(Exception ex)
		{
			logger.error("An error occurred while executing simple job: " + jobTypeName, ex);
		}
	}
	
	@Override
	public void execute(JobExecutionContext context) throws JobExecutionException
	{
		JobDataMap dataMap = context.getMergedJobDataMap();
		String jobType = dataMap.getString(ATTR_JOB_TYPE);
		
		if(JOB_TYPE_CONFIGURED_JOB.equals(jobType))
		{
			executeConfiguredJob(dataMap, context);
		}
		else
		{
			executeSimpleJob(dataMap);
		}
	}

	public static String getTriggerNameForJob(String name)
	{
		return name + TRIGGER_POSTFIX;
	}
}
