package com.fw.webutil.common;

import java.util.Collection;

public interface ICacheManager
{
	public void cacheBean(IIdentifiable bean);
	public void cacheBeans(Collection<? extends IIdentifiable> beans);
	
	public <T extends IIdentifiable> T getBeanById(Class<T> type, Object id);
	public <T extends IIdentifiable> T getBeanByName(Class<T> type, String name);
	
	public void beanAdded(IIdentifiable bean);
	public void beanUpdated(IIdentifiable bean);
	public void beanRemoved(Class<? extends IIdentifiable> type, Object id);
	
	public void clearBeansWithIds(Class<? extends IIdentifiable> type, Collection<? extends Object> ids);
	public void clearBeansWithIds(Class<? extends IIdentifiable> type, Object... ids);
}
