package com.fw.webutil.service;

import java.lang.annotation.Annotation;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import javax.annotation.PostConstruct;

import org.reflections.Reflections;
import org.reflections.scanners.SubTypesScanner;
import org.reflections.scanners.TypeAnnotationsScanner;

public class ClassScannerService
{
	private List<String> rootPackages = new ArrayList<String>();
	private List<Reflections> reflections;

	public List<String> getRootPackages()
	{
		return rootPackages;
	}

	public void setRootPackages(List<String> rootPackages)
	{
		this.rootPackages = rootPackages;
	}

	public Set<Class<?>> getClassesWithAnnotation(Class<? extends Annotation> annotationType)
	{
		Set<Class<?>> result = new HashSet<Class<?>>();
		Set<Class<?>> classes = null;
		
		for(Reflections reflection: reflections)
		{
			classes = reflection.getTypesAnnotatedWith(annotationType);
			
			if(classes == null)
			{
				continue;
			}
			
			result.addAll(classes);
		}
		
		return result;
	}
	
	@SuppressWarnings({ "unchecked", "rawtypes" })
	public Set<Class<?>> getClassesOfType(Class<?> type)
	{
		Set<Class<?>> result = new HashSet<>();
		Set<Class<?>> classes = null;
		
		for(Reflections reflection: reflections)
		{
			classes = (Set)reflection.getSubTypesOf(type);
			
			if(classes == null)
			{
				continue;
			}
			
			result.addAll(classes);
		}
		
		return result;
	}
	
	@PostConstruct
	public void postLoading()
	{
		if(rootPackages == null || rootPackages.isEmpty())
		{
			throw new NullPointerException("No root-package is not specified");
		}
		
		reflections = new ArrayList<Reflections>(rootPackages.size());
		
		for(String pack: rootPackages)
		{
			reflections.add(new Reflections(pack, new TypeAnnotationsScanner(), new SubTypesScanner()));
		}
	}
}
