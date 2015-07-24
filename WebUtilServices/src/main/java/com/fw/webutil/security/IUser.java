package com.fw.webutil.security;

import java.util.Collection;

import com.fw.webutil.common.security.IRole;
import com.fw.webutil.common.security.IRoleGroup;

public interface IUser
{
	public String getId();
	public String getDisplayName();
	public String getMailId();
	
	public boolean hasRole(IRole<?> role);
	public boolean hasRoleGroup(IRoleGroup roleGroup);
	
	public boolean hasRole(String role);
	public boolean hasRoleGroup(String roleGroup);
	
	public Collection<IRole<?>> getRoles();
	
	public Collection<IRoleGroup> getRoleGroups();
}
