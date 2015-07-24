package com.fw.webutil.common.security;

public interface IRole<G extends IRoleGroup>
{
	public String getName();
	public G[] getGroups();
	public String getDescription();
	
	//public String getGroupName();
}
