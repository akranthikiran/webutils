package com.fw.webutil.service.jobs;

/**
 * Thrown if there was any problem in starting job execution
 * @author akiran
 */
public class JobException extends RuntimeException
{
	private static final long serialVersionUID = 1L;

	public JobException()
	{
		super();
	}

	public JobException(String message, Throwable cause)
	{
		super(message, cause);
	}

	public JobException(String message)
	{
		super(message);
	}

	public JobException(Throwable cause)
	{
		super(cause);
	}

}
