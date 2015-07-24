package com.fw.webutil.service.conversion;

import java.beans.BeanInfo;
import java.beans.Introspector;
import java.beans.PropertyDescriptor;
import java.text.ParseException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.annotation.PostConstruct;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationContext;

import com.fw.ccg.util.CCGUtility;
import com.fw.webutil.common.ICommonConstants;
import com.fw.webutil.common.annotations.Service;

@Service
public class BeanConversionService
{
	@Autowired
	private ApplicationContext applicationContext;
	
	private Map<Class<?>, Map<String, PropertyAccessor>> classToProperties = new HashMap<>();
	private Map<ConverterKey, Converter<?, ?>> converterMap = new HashMap<>();
	
	@PostConstruct
	private void init()
	{
		converterMap.put(new ConverterKey(String.class, Date.class), new Converter<String, Date>()
		{
			@Override
			public Date convert(String property, String source)
			{
				try
				{
					if(source == null || source.trim().length() == 0)
					{
						return null;
					}
					
					return ICommonConstants.DEFAULT_DATE_FORMAT.parse(source);
				}catch(ParseException ex)
				{
					throw new IllegalArgumentException("Invalid date string specified: " + source, ex);
				}
			}
		});

		converterMap.put(new ConverterKey(Date.class, String.class), new Converter<Date, String>()
		{
			@Override
			public String convert(String property, Date source)
			{
				if(source == null)
				{
					return null;
				}
				
				if(property.toLowerCase().contains("time"))
				{
					return ICommonConstants.DEFAULT_DATE_TIME_FORMAT.format(source);
				}
				
				return ICommonConstants.DEFAULT_DATE_FORMAT.format(source);
			}
		});
	}
	
	public <T> T convertToType(Object source, Class<T> targetType)
	{
		if(source == null)
		{
			return null;
		}
		
		if(targetType == null)
		{
			throw new NullPointerException("Target type can not be null.");
		}
		
		try
		{
			T t = targetType.newInstance();
			copyProperties(t, source);
			
			return t;
		}catch(Exception ex)
		{
			throw new IllegalStateException("An error occurred while converting '" + source.getClass().getName() + "' object to type: " + targetType.getName(), ex);
		}
	}


	public <T> List<T> convertListToTypeList(List<? extends Object> sourceList, Class<T> targetType)
	{
		if(sourceList == null || sourceList.isEmpty())
		{
			return Collections.emptyList();
		}
		
		if(targetType == null)
		{
			throw new NullPointerException("Target type can not be null.");
		}
		
		try
		{
			List<T> targetList = new ArrayList<>(sourceList.size());
			
			T t = null;
			
			for(Object source: sourceList)
			{
				t = targetType.newInstance();
				copyProperties(t, source);
				
				targetList.add(t);
			}
			
			return targetList;
		}catch(Exception ex)
		{
			throw new IllegalStateException("An error occurred while converting object-list to list of type: " + targetType.getName());
		}
	}
	
	private boolean isSameType(Class<?> sourceType, Class<?> targetType)
	{
		if(targetType.isAssignableFrom(sourceType))
		{
			return true;
		}
		
		if(sourceType.isPrimitive() || targetType.isPrimitive())
		{
			sourceType = (sourceType.isPrimitive()) ? CCGUtility.getWrapperClass(sourceType) : sourceType;
			targetType = (targetType.isPrimitive()) ? CCGUtility.getWrapperClass(targetType) : targetType;
			
			if(sourceType.equals(targetType))
			{
				return true;
			}
		}
		
		return false;
	}
	
	@SuppressWarnings({"unchecked", "rawtypes"})
	public void copyProperties(Object target, Object source)
	{
		Map<String, PropertyAccessor> sourceProperties = getProperties(source.getClass());
		Map<String, PropertyAccessor> targetProperties = getProperties(target.getClass());
		
		PropertyAccessor targetAccessor = null;
		PropertyAccessor sourceAccessor = null;
		Converter converter = null;
		
		for(String prop: sourceProperties.keySet())
		{
			sourceAccessor = sourceProperties.get(prop);
			targetAccessor = targetProperties.get(sourceAccessor.getMappingName());
			
			if(targetAccessor == null)
			{
				continue;
			}

			converter = getConverter(sourceAccessor, targetAccessor);
			
			if(converter == null)
			{
				if(isSameType(sourceAccessor.getPropertyType(), targetAccessor.getPropertyType()))
				{
					targetAccessor.setValue(target, sourceAccessor.getValue(source));
				}
				
				continue;
			}
			
			targetAccessor.setValue(target, converter.convert(sourceAccessor.getName(), sourceAccessor.getValue(source)));
		}
	}
	
	@SuppressWarnings({"rawtypes"})
	private Converter getConverter(PropertyAccessor sourceAccessor, PropertyAccessor targetAccessor)
	{
		LovMapping lovMapping = sourceAccessor.getAnnotation(LovMapping.class);
		
		if(lovMapping != null)
		{
			LovConverter converter = new LovConverter(lovMapping.query());
			applicationContext.getAutowireCapableBeanFactory().autowireBean(converter);
			
			return converter;
		}
		
		ConverterKey converterKey = new ConverterKey(sourceAccessor.getPropertyType(), targetAccessor.getPropertyType());
		Converter converter = converterMap.get(converterKey);
		
		return converter;
	}
	
	private Map<String, PropertyAccessor> getProperties(Class<?> type)
	{
		Map<String, PropertyAccessor> propMap = classToProperties.get(type);
		
		if(propMap != null)
		{
			return propMap;
		}
		
		synchronized(BeanConversionService.class)
		{
			try
			{
				BeanInfo beanInfo = Introspector.getBeanInfo(type);
				PropertyDescriptor propertyDescriptors[] = beanInfo.getPropertyDescriptors();
				
				propMap = new HashMap<>();
				
				for(PropertyDescriptor desc: propertyDescriptors)
				{
					propMap.put(desc.getName(), new PropertyAccessor(desc.getName(), desc.getReadMethod(), desc.getWriteMethod()));
				}
				
				classToProperties.put(type, propMap);
				return propMap;
			}catch(Exception ex)
			{
				throw new IllegalStateException("An error occurred while introspecting class: " + type.getName(), ex);
			}
		}
	}
}
