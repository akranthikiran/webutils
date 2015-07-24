package com.fw.webutil.security;

import com.fw.webutil.common.security.IRole;
import com.fw.webutil.common.security.IRoleGroup;


public interface ISecurityService
{
	public boolean validateAuthentication(String userId, String password);
	
	public IUser authenticateAndFetch(String userId, String password);
	
	/**
	* This method should throw AuthorizationException if the current does not have all the roles 
	*
	* @param roles
	*/
	public void checkForRoles(IRole<?>... roles);
	
	public boolean hasRole(IRole<?> role);
	
	public boolean hasRoleGroup(IRoleGroup roleGroup);
	
	/**
	* This method should return IRole object with specified name
	* @param name
	* @return
	*/
	public IRole<?> getRole(String name);
	
	public IUser getCurrentUser();
	
	public String encryptPassword(String password);
}
