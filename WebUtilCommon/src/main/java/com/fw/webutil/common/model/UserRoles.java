package com.fw.webutil.common.model;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

public class UserRoles
{
	private List<String> roles = new ArrayList<>();
	private List<String> roleGroups = new ArrayList<>();

	/*
	public UserRoles(Collection<IRole<?>> roles, Collection<IRoleGroup> roleGroups)
	{
		if(roles != null)
		{
			for(IRole<?> role: roles)
			{
				this.roles.add(role.getName());
			}
		}
		
		if(roleGroups != null)
		{
			for(IRoleGroup roleGroup: roleGroups)
			{
				this.roleGroups.add(roleGroup.getName());
			}
		}
	}
	*/

	public UserRoles(Collection<String> roles, Collection<String> roleGroups)
	{
		if(roles != null)
		{
			this.roles.addAll(roles);
		}
		
		if(roleGroups != null)
		{
			this.roleGroups.addAll(roleGroups);
		}
	}

	public List<String> getRoles()
	{
		return roles;
	}

	public void setRoles(List<String> roles)
	{
		this.roles = roles;
	}

	public List<String> getRoleGroups()
	{
		return roleGroups;
	}

	public void setRoleGroups(List<String> roleGroups)
	{
		this.roleGroups = roleGroups;
	}
}
