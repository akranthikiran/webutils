package com.fw.webutil.service.jobs;

/**
 * Base interface for job-types
 * @author akiran
 */
public interface IExecutable<C>
{
	public String execute(C configuration);
}
