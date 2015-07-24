package com.fw.webutil.dao;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationContext;

import com.fw.persistence.IPersistenceContext;
import com.fw.webutil.security.ISecurityService;

/**
 * A common persistence context, which provides current user from ISecurityService
 * 
 * @author akiran
 */
public class CommonPersistenceContext implements IPersistenceContext
{
	@Autowired
	private ApplicationContext applicationContext;
	
	private ISecurityService securityService;

	private boolean auditEnabled = true;
	
	@Override
	public String getCurrentUser()
	{
		/*
		 * NOTE: security-service requires RepositoryFactory and RepositoryFactory requires  CommonPersistenceContext
		 * which in turn requires security-service. A cyclic dependency.
		 * 
		 * In order to over come init problems, this dependency is made lazy
		 */
		
		if(securityService == null)
		{
			securityService = applicationContext.getBean(ISecurityService.class);
		}
		
		return securityService.getCurrentUser().getId();
	}
	
	public void setAuditEnabled(boolean auditEnabled)
	{
		this.auditEnabled = auditEnabled;
	}

	@Override
	public boolean isAuditEnabled()
	{
		return auditEnabled;
	}

}
