package com.fw.webutil.controller;

import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

import javax.servlet.http.HttpServletRequest;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.bind.annotation.RestController;

import com.fw.webutil.common.IMessageProvider;
import com.fw.webutil.common.InvalidParameterException;
import com.fw.webutil.common.ValueLabel;
import com.fw.webutil.common.annotations.Label;
import com.fw.webutil.common.annotations.LovType;
import com.fw.webutil.dao.LovDao;
import com.fw.webutil.security.ISecurityService;

@RestController
@RequestMapping("/action/lov")
public class LovController
{
	@Autowired
	private LovDao lovDao;
	
	@Autowired
	private IMessageProvider messageProvider;
	
	@Autowired
	private HttpServletRequest httpRequest;
	
	@Autowired
	private ISecurityService securityService;
	
	/**
	 * @param lovType
	 * @param name This should be query-name for QUERY_TYPE and this should be id for CUSTOM_TYPE
	 * @param parentId
	 * @return
	 */
	@RequestMapping(value = "/fetch")
	@ResponseBody
	public List<ValueLabel> fetchLOVList(@RequestParam("lovType") LovType lovType, @RequestParam("name") String name, 
			@RequestParam(value = "parentId", required = false) String parentId)
	{
		if(lovType == null)
		{
			throw new NullPointerException("Lov-type can not be null");
		}
		
		switch(lovType)
		{
			case QUERY_TYPE:
				return lovDao.fetchLovValues(name, parentId);
			case ENUM_TYPE:
				return getLOVValues(name);
			case CUSTOM_TYPE:
				return lovDao.getCustomLovValues(name);
		}
		
		throw new IllegalStateException("Failed to feth LOV values for [Type: " + lovType + ", Name: " + name + ", Parent-id: " + parentId + "]");
	}
	
	@SuppressWarnings({ "rawtypes"})
	private List<ValueLabel> getLOVValues(String name)
	{
		try
		{
			Class<?> enumType = Class.forName(name);
			
			if(!enumType.isEnum())
			{
				throw new InvalidParameterException("Invalid enum type specified: " + name);
			}

			//Field enumFields[] = enumType.getFields();
			Object enumValues[] = enumType.getEnumConstants();
			List<ValueLabel> valueLst = new ArrayList<>();
			Enum enumObj = null;
			String label = null;
			Locale locale = httpRequest.getLocale();
			Label labelAnnot = null;
			Field field = null;
			
			for(Object obj: enumValues)
			{
				enumObj = (Enum)obj;
				
				try
				{
					field = enumType.getField(enumObj.name());
				}catch(NoSuchFieldException | SecurityException e)
				{
					//ignore, this should never happen
					e.printStackTrace();
				}
				
				label = messageProvider.getMessage(enumType.getName() + "." + enumObj.name() + ".label", null, locale);
				
				if(label == null)
				{
					label = messageProvider.getMessage(enumObj.name() + ".label", null, locale);
				}
				
				if(label == null && (labelAnnot = field.getAnnotation(Label.class)) != null)
				{
					label = labelAnnot.value();
				}
				
				if(label == null)
				{
					label = enumObj.name();
				}
				
				valueLst.add(new ValueLabel(enumObj.name(), label));
			}
			
			return valueLst;
		}catch(ClassNotFoundException ex)
		{
			throw new InvalidParameterException("Failed to fetch enum LOV for specified type: " + name, ex);
		}
	}

}
