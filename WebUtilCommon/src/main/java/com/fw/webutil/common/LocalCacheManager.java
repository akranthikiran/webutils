package com.fw.webutil.common;

import java.util.Arrays;
import java.util.Collection;
import java.util.HashMap;
import java.util.Map;

public class LocalCacheManager implements ICacheManager
{
	private static final String FIELD_ID = "ID";
	private static final String FIELD_NAME = "NAME";
	
	private static class CacheKey
	{
		private Class<?> type;
		private String field;
		private Object value;
		
		public CacheKey(Class<?> type, String field, Object value)
		{
			this.type = type;
			this.field = field;
			this.value = value;
		}
		
		/* (non-Javadoc)
		 * @see java.lang.Object#equals(java.lang.Object)
		 */
		@Override
		public boolean equals(Object obj)
		{
			if(obj == this)
			{
				return true;
			}

			if(!(obj instanceof LocalCacheManager.CacheKey))
			{
				return false;
			}

			LocalCacheManager.CacheKey other = (LocalCacheManager.CacheKey)obj;
			return type.equals(other.type) && field.equals(other.field) && value.equals(other.value);
		}

		/* (non-Javadoc)
		 * @see java.lang.Object#hashcode()
		 */
		@Override
		public int hashCode()
		{
			return type.hashCode() + field.hashCode() + value.hashCode();
		}
	}
	
	private Map<CacheKey, Object> keyToBean = new HashMap<>();

	@Override
	public void cacheBean(IIdentifiable bean)
	{
		if(bean == null)
		{
			return;
		}
		
		keyToBean.put(new CacheKey(bean.getClass(), FIELD_NAME, bean.getName()), bean);
		keyToBean.put(new CacheKey(bean.getClass(), FIELD_ID, bean.getId()), bean);
	}

	@Override
	public void cacheBeans(Collection<? extends IIdentifiable> beans)
	{
		if(beans == null)
		{
			return;
		}
		
		for(IIdentifiable bean: beans)
		{
			cacheBean(bean);
		}
	}

	@SuppressWarnings("unchecked")
	@Override
	public <T extends IIdentifiable>T getBeanById(Class<T> type, Object id)
	{
		return (T)keyToBean.get(new CacheKey(type, FIELD_ID, id));
	}

	@SuppressWarnings("unchecked")
	@Override
	public <T extends IIdentifiable>T getBeanByName(Class<T> type, String name)
	{
		return (T)keyToBean.get(new CacheKey(type, FIELD_NAME, name));
	}

	@Override
	public void beanAdded(IIdentifiable bean)
	{
		cacheBean(bean);
	}

	@Override
	public void beanUpdated(IIdentifiable bean)
	{
		cacheBean(bean);
	}

	@Override
	public void beanRemoved(Class<? extends IIdentifiable> type, Object id)
	{
		clearBeansWithIds(type, Arrays.asList(id));
	}

	@Override
	public void clearBeansWithIds(Class<? extends IIdentifiable> type, Collection<? extends Object> ids)
	{
		if(ids == null || ids.isEmpty())
		{
			return;
		}
		
		IIdentifiable bean = null;
		
		for(Object id: ids)
		{
			bean = this.getBeanById(type, id);
			
			if(bean == null)
			{
				continue;
			}
			
			keyToBean.remove(new CacheKey(bean.getClass(), FIELD_NAME, bean.getName()));
			keyToBean.remove(new CacheKey(bean.getClass(), FIELD_ID, bean.getId()));
		}
			
	}

	@Override
	public void clearBeansWithIds(Class<? extends IIdentifiable> type, Object... ids)
	{
		this.clearBeansWithIds(type, Arrays.asList(ids));
	}
	
	

}
