package com.fw.webutil.controller;

import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

import javax.annotation.PostConstruct;
import javax.servlet.http.HttpServletRequest;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.MessageSource;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.bind.annotation.RestController;

import com.fw.webutil.common.InvalidParameterException;
import com.fw.webutil.common.SpringMessageProvider;
import com.fw.webutil.common.annotations.Model;
import com.fw.webutil.common.annotations.ModelType;
import com.fw.webutil.common.model.dynamic.DynamicType;
import com.fw.webutil.dao.DynamicDataDao;
import com.fw.webutil.model.dynamic.DynamicTypeFactory;
import com.fw.webutil.service.ClassScannerService;

@RestController
@RequestMapping("/action/dynamic")
public class DynamicController
{
	@Autowired
	private DynamicTypeFactory dynamicTypeFactory;
	
	@Autowired
	private DynamicDataDao dynamicDataDao;
	
	@Autowired
	private MessageSource messageSource;
	
	@Autowired
	private ClassScannerService classScannerService;
	
	private Map<String, Class<?>> nameToModelType = new HashMap<>();
	
	public static String getModelTypeName(Class<?> modelType)
	{
		Model model = modelType.getAnnotation(Model.class);
		
		if(model == null)
		{
			throw new IllegalArgumentException("Invalid model type specified: " + modelType.getName());
		}
		
		String name = model.name();
		
		if(name.trim().length() == 0)
		{
			name = modelType.getSimpleName();
		}
		
		return name;
	}
	
	@PostConstruct
	private void init()
	{
		Set<Class<?>> modelTypes = classScannerService.getClassesWithAnnotation(Model.class);
		
		if(modelTypes == null)
		{
			return;
		}
		
		String name = null;
		
		for(Class<?> type: modelTypes)
		{
			name = getModelTypeName(type);
			nameToModelType.put(name, type);
		}
	}
	
	@RequestMapping(value = "/model/{modelName}")
	@ResponseBody
	public Object fetchModelDetails(@RequestParam("modelType") ModelType modelType, @PathVariable("modelName") String modelName, HttpServletRequest request)
	{
		if(modelType == ModelType.STATIC_TYPE)
		{
			Class<?> cls = nameToModelType.get(modelName);
			
			if(cls == null)
			{
				throw new InvalidParameterException("Invalid static model type specified: " + modelName);
			}
			
			DynamicType dynamicType = dynamicTypeFactory.buildDynamicType(cls, new SpringMessageProvider(messageSource, request.getLocale()));
			return dynamicType;
		}
		else
		{
			DynamicType dynamicType = dynamicDataDao.getDynamicTypeByName(modelName);
			return dynamicType;
		}
	}

	@SuppressWarnings("rawtypes")
	@RequestMapping(value = "/lov")
	@ResponseBody
	public List<String> getLOVValues(String name)
	{
		boolean isJavaType = (name.indexOf(".") > 0);
		
		if(isJavaType)
		{
			try
			{
				Class<?> enumType = Class.forName(name);
				
				if(!enumType.isEnum())
				{
					throw new InvalidParameterException("Invalid enum type specified: " + name);
				}
				
				Object enumValues[] = enumType.getEnumConstants();
				String names[] = new String[enumValues.length];
				
				int idx = 0;
				
				for(Object obj: enumValues)
				{
					names[idx++] = ((Enum)obj).name();
				}
				
				return Arrays.asList(names);
			}catch(ClassNotFoundException e)
			{
				e.printStackTrace();
			}
		}
		
		throw new UnsupportedOperationException("Dynamic LOV is not supported with current code: " + name);
	}
}
