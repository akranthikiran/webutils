package com.fw.webutil.entity.job;

public class JobSchedule
{
	/**
	 * Time when this job should be exeucted
	 */
	private Time timeToExecute;

	private RepetitionType repetitionType;

	private long delayInSeconds;

	public Time getTimeToExecute()
	{
		return timeToExecute;
	}

	public void setTimeToExecute(Time timeToExecute)
	{
		this.timeToExecute = timeToExecute;
	}

	public RepetitionType getRepetitionType()
	{
		return repetitionType;
	}

	public void setRepetitionType(RepetitionType repetitionType)
	{
		this.repetitionType = repetitionType;
	}

	public long getDelayInSeconds()
	{
		return delayInSeconds;
	}

	public void setDelayInSeconds(long delayInSeconds)
	{
		this.delayInSeconds = delayInSeconds;
	}

}
