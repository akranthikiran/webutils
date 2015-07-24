package com.fw.webutil.util;

import java.lang.reflect.InvocationTargetException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.apache.commons.beanutils.BeanUtils;

public class CommonUtils
{
	@SuppressWarnings("unchecked")
	public static <K, T> Map<K, T> toMap(Collection<T> beans, String keyPropertyName, Class<K> keyType)
	{
		Map<K, T> map = new HashMap<>();
		
		try
		{
			for(T bean: beans)
			{
				map.put((K)BeanUtils.getProperty(bean, keyPropertyName), bean);
			}
		}catch(NoSuchMethodException| InvocationTargetException | IllegalAccessException ex)
		{
			throw new IllegalStateException("An error occurred while building bean map", ex);
		}
		
		return map;
	}
	
	public static List<String> toStringList(Collection<?> items)
	{
		List<String> resLst = new ArrayList<String>(items.size());
		
		for(Object item : items)
		{
			resLst.add(item.toString());
		}
		
		return resLst;
	}
}
