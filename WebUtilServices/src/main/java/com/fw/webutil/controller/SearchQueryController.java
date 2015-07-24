package com.fw.webutil.controller;

import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

import javax.annotation.PostConstruct;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.bind.annotation.RestController;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fw.webutil.common.InvalidParameterException;
import com.fw.webutil.common.Query;
import com.fw.webutil.common.annotations.Model;
import com.fw.webutil.dao.SearchQueryDao;
import com.fw.webutil.query.QueryParamsException;
import com.fw.webutil.query.QueryResult;
import com.fw.webutil.security.ISecurityService;
import com.fw.webutil.service.ClassScannerService;

@RestController
@RequestMapping("/action/queries")
public class SearchQueryController
{
	@Autowired
	private SearchQueryDao searchQueryDao;
	
	@Autowired
	private ObjectMapper objectMapper;
	
	@Autowired
	private ClassScannerService classScannerService;
	
	@Autowired
	private ISecurityService securityService;
	
	private Map<String, Class<?>> nameToQueryType = new HashMap<>();
	
	@PostConstruct
	private void init()
	{
		Set<Class<?>> queryTypes = classScannerService.getClassesWithAnnotation(Query.class);
		
		if(queryTypes == null)
		{
			return;
		}
		
		String name = null;
		Model model = null;
		
		for(Class<?> type: queryTypes)
		{
			model = type.getAnnotation(Model.class);
			
			if(model != null)
			{
				name = model.name();
			}
			
			if(name.trim().length() == 0)
			{
				name = type.getSimpleName();
			}
			
			nameToQueryType.put(name, type);
		}
	}
	
	private void checkForRoles(String roleNames[])
	{
		if(roleNames == null || roleNames.length == 0)
		{
			return;
		}
		
		for(String roleName: roleNames)
		{
			securityService.checkForRoles(securityService.getRole(roleName));
		}
	}
	
	public Object parseQueryAndAuthorize(String queryType, String queryJson)
	{
		Class<?> queryTypeCls = nameToQueryType.get(queryType);
		
		if(queryTypeCls == null)
		{
			throw new QueryParamsException("Non-existing query-bean-type sepcified: " + queryType);
		}
		
		Query query = queryTypeCls.getAnnotation(Query.class);
		Object queryObj = null;
		
		String roleNames[] = query.requiredRoles();
		
		checkForRoles(roleNames);
		
		try
		{
			queryObj = objectMapper.readValue(queryJson, queryTypeCls);
		}catch(Exception ex)
		{
			throw new InvalidParameterException("Failed to load query object from json string: " + queryJson, ex);
		}

		return queryObj;
	}
	
	@RequestMapping(value = "/executeQuery")
	@ResponseBody
	public QueryResult exectueQuery(@RequestParam("queryType") String queryType, @RequestParam("formFieldContent") String queryJson)
	{
		Object queryObj = parseQueryAndAuthorize(queryType, queryJson);
		Query query = queryObj.getClass().getAnnotation(Query.class);
		
		QueryResult result = searchQueryDao.executeQuery(query.queryName(), queryObj);
		
		if(result != null)
		{
			return result;
		}
		
		return new QueryResult(Collections.<String>emptyList());
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
