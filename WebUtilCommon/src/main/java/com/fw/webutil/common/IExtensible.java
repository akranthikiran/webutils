package com.fw.webutil.common;

import java.util.Collection;
import java.util.Map;

import com.fw.webutil.common.model.dynamic.Property;

public interface IExtensible extends IIdentifiable
{
	public Collection<Property> getExtendedProperties();
	public void setExtendedProperties(Collection<Property> properties);
	
	public Map<String, Property> getExtendedPropertyMap();
	
	public void setExtendedProperty(String name, Object value);
}
