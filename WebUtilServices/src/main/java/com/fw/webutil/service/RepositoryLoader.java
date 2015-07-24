package com.fw.webutil.service;

import java.util.Set;

import javax.annotation.PostConstruct;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;

import com.fw.persistence.ICrudRepository;
import com.fw.persistence.repository.RepositoryFactory;

/**
 * This loader is capable of initializing all the repositories in the classpath.
 * 
 * The class-scanner service is parameterized in this class, so that application configuration can 
 * choose which packages needs to be scanned for repositories.
 * 
 * @author akiran
 */
public class RepositoryLoader
{
	private static final Logger logger = LogManager.getLogger(RepositoryLoader.class);
	
	private ClassScannerService classScannerService;
	
	@Autowired
	private RepositoryFactory repositoryFactory;
	
	public void setClassScannerService(ClassScannerService classScannerService)
	{
		this.classScannerService = classScannerService;
	}
	
	@SuppressWarnings({ "unchecked", "rawtypes" })
	@PostConstruct
	private void init()
	{
		Set<Class<?>> repos = classScannerService.getClassesOfType(ICrudRepository.class);
		
		for(Class<?> type: repos)
		{
			logger.debug("Loading repository: " + type.getName());
			repositoryFactory.getRepository((Class)type);
		}
	}
}
