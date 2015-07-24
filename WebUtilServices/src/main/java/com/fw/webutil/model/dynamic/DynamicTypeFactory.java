package com.fw.webutil.model.dynamic;

import java.beans.BeanInfo;
import java.beans.IndexedPropertyDescriptor;
import java.beans.IntrospectionException;
import java.beans.Introspector;
import java.beans.PropertyDescriptor;
import java.lang.reflect.Field;
import java.lang.reflect.Modifier;
import java.lang.reflect.ParameterizedType;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.apache.commons.collections4.CollectionUtils;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;

import com.fw.webutil.common.IExtensible;
import com.fw.webutil.common.IMessageProvider;
import com.fw.webutil.common.LovDataType;
import com.fw.webutil.common.annotations.DefaultValue;
import com.fw.webutil.common.annotations.Description;
import com.fw.webutil.common.annotations.FieldMethod;
import com.fw.webutil.common.annotations.LOV;
import com.fw.webutil.common.annotations.Label;
import com.fw.webutil.common.annotations.LovType;
import com.fw.webutil.common.annotations.Model;
import com.fw.webutil.common.annotations.MultilineText;
import com.fw.webutil.common.annotations.ReadOnly;
import com.fw.webutil.common.annotations.ServerField;
import com.fw.webutil.common.annotations.Service;
import com.fw.webutil.common.annotations.SystemLov;
import com.fw.webutil.common.annotations.SystemLovType;
import com.fw.webutil.common.model.dynamic.ClientConfiguration;
import com.fw.webutil.common.model.dynamic.DynamicType;
import com.fw.webutil.common.model.dynamic.FieldDef;
import com.fw.webutil.common.model.dynamic.FieldType;
import com.fw.webutil.common.model.dynamic.ValidatorConfiguration;
import com.fw.webutil.dao.DynamicDataDao;
import com.fw.webutil.dao.LovDao;
import com.fw.webutil.model.dynamic.validator.ValidatorConfiguraionFactory;

@Service
public class DynamicTypeFactory
{
	private static Logger logger = LogManager.getLogger(DynamicTypeFactory.class);
	
	@Autowired
	private ValidatorConfiguraionFactory validatorConfiguraionFactory;
	
	@Autowired
	private DynamicDataDao dynamicDataDao;
	
	@Autowired
	private LovDao lovDao;
	
	private Map<Class<?>, DynamicType> staticDynamicTypes = new HashMap<>();
	
	private String getFieldMessage(Class<?> target, IDataSpecifier dataSpecifier, String messageType, IMessageProvider messageProvider)
	{
		String message = messageProvider.getMessage(target.getName() + "." + dataSpecifier.getName() + "." + messageType);
		
		if(message != null)
		{
			return message;
		}
		
		message = messageProvider.getMessage(target.getSimpleName() + "." + dataSpecifier.getName() + "." + messageType);
		
		if(message != null)
		{
			return message;
		}
		
		message = messageProvider.getMessage(dataSpecifier.getName() + "." + messageType);
		
		return message;
	}
	
	public String getDefaultLabel(String name)
	{
		char ch = name.charAt(0);
		String label = Character.toUpperCase(ch) + name.substring(1);
		label = label.replaceAll("([A-Z])", " $1");

		return label.trim();
	}
	
	public String getModelName(Class<?> beanType)
	{
		Model model = beanType.getAnnotation(Model.class);
		String modelName = (model == null) ? beanType.getSimpleName() : model.name();
		
		if(modelName.trim().length() == 0)
		{
			modelName = beanType.getSimpleName();
		}
		
		return modelName;
	}
	
	private void getEnumLovDetails(FieldDef fieldDef, Class<?> fieldType)
	{
		fieldDef.setFieldType(FieldType.LIST_OF_VALUES);
		fieldDef.setServerType(fieldType.getName());
		
		ClientConfiguration clientConfiguration = new ClientConfiguration();
		fieldDef.setClientConfiguration(clientConfiguration);
		clientConfiguration.setLovType(LovType.ENUM_TYPE);
		clientConfiguration.setQueryName(fieldType.getName());

		fieldDef.setClientConfiguration(clientConfiguration);
	}
	
	private void getCustomLovDetails(FieldDef fieldDef, IDataSpecifier field, Class<?> parentCls)
	{
		LOV lovAnnotation = field.getAnnotation(LOV.class);
		
		fieldDef.setFieldType(FieldType.LIST_OF_VALUES);
		fieldDef.setServerType(field.getType().getName());
		
		ClientConfiguration clientConfiguration = new ClientConfiguration();
		fieldDef.setClientConfiguration(clientConfiguration);
		clientConfiguration.setLovType(LovType.QUERY_TYPE);
		clientConfiguration.setQueryName(lovAnnotation.query());
		
		if(lovAnnotation.parentField().length() > 0)
		{
			try
			{
				parentCls.getDeclaredField(lovAnnotation.parentField());
			}catch(Exception ex)
			{
				throw new IllegalStateException("Invalid parent field '" + lovAnnotation.parentField() + "' specified on lov field '" + 
								field.getName() + "' in class: " + parentCls.getName(), ex);
			}
			
			clientConfiguration.setParentField(lovAnnotation.parentField());
		}

		fieldDef.setClientConfiguration(clientConfiguration);
	}
	
	private void getSystemLovDetails(FieldDef fieldDef, IDataSpecifier field, Class<?> parentCls)
	{
		SystemLov sysLovAnnotation = field.getAnnotation(SystemLov.class);
		
		Class<?> sysLovType = sysLovAnnotation.value();
		SystemLovType sysLovTypeAnnotation = sysLovType.getAnnotation(SystemLovType.class);
		
		if(sysLovTypeAnnotation == null)
		{
			throw new IllegalStateException("A system-lov field '" + fieldDef.getName() + "' is marked with non-system-lov type '" 
						+ sysLovType.getName() + "' in class - " + parentCls.getName());
		}
		
		LovDataType fieldType = LovDataType.getDataType(field.getType());
		
		if(fieldType != sysLovTypeAnnotation.type())
		{
			throw new IllegalStateException("Field type '" + field.getType().getName() + "' is not matching with system-lov annotation type '" 
					+ sysLovTypeAnnotation.type() + "' for field '" + fieldDef.getName() + "' in class - " + parentCls.getName());
		}
		
		fieldDef.setFieldType(FieldType.LIST_OF_VALUES);
		fieldDef.setServerType(field.getType().getName());
		
		String lovId = lovDao.fetchLovId(sysLovTypeAnnotation.name());
		
		ClientConfiguration clientConfiguration = new ClientConfiguration();
		fieldDef.setClientConfiguration(clientConfiguration);
		clientConfiguration.setLovType(LovType.CUSTOM_TYPE);
		clientConfiguration.setQueryName(lovId);
		
		fieldDef.setClientConfiguration(clientConfiguration);
		fieldDef.setDataId(lovId);
	}

	private FieldDef getFieldDef(Class<?> parentCls, IDataSpecifier field, IMessageProvider messageProvider)
	{
		FieldDef fieldDef = new FieldDef();
		
		String name = field.getName();
		fieldDef.setName(field.getName());
		
		String label = getFieldMessage(parentCls, field, "label", messageProvider);
		
		if(label == null)
		{
			Label labelAnnotation = field.getAnnotation(Label.class);
			
			if(labelAnnotation != null)
			{
				label = labelAnnotation.value();
			}
			else
			{
				label = getDefaultLabel(name);
			}
		}
		
		fieldDef.setLabel(label.trim());
		
		String description = getFieldMessage(parentCls, field, "desc", messageProvider);
		
		if(description == null)
		{
			Description descriptionAnn = field.getAnnotation(Description.class);
			
			if(descriptionAnn != null)
			{
				description = descriptionAnn.value();
			}
		}
		
		fieldDef.setDescription(description);
		
		//fetch and set the default value if any
		DefaultValue defaultValue = field.getAnnotation(DefaultValue.class);
		
		if(defaultValue != null)
		{
			fieldDef.setDefaultValue(defaultValue.value());
		}
		

		Class<?> fieldType = field.getType();
		
		if(fieldType.isEnum())
		{
			getEnumLovDetails(fieldDef, fieldType);
		}
		else if(field.getAnnotation(LOV.class) != null)
		{
			getCustomLovDetails(fieldDef, field, parentCls);
		}
		else if(field.getAnnotation(SystemLov.class) != null)
		{
			getSystemLovDetails(fieldDef, field, parentCls);
		}
		else if(Collection.class.isAssignableFrom(fieldType))
		{
			ParameterizedType parameterizedType = (ParameterizedType)field.getGenericType();
			Class<?> collectionType = (Class<?>)parameterizedType.getActualTypeArguments()[0];

			fieldDef.setMultiValued(true);
			
			//change field type to collection type
			fieldType = collectionType;
		}
		else if(fieldType.isArray())
		{
			fieldDef.setMultiValued(true);
			
			fieldType = fieldType.getComponentType();
			getEnumLovDetails(fieldDef, fieldType);
		}
		
		if(fieldDef.getFieldType() == null)
		{
			FieldType dynFieldType = FieldType.getFieldType(fieldType);
			
			if(dynFieldType == null)
			{
				fieldDef.setFieldType(FieldType.COMPLEX);
				
				Model modelAnnotation = fieldType.getAnnotation(Model.class);

				if(modelAnnotation == null)
				{
					throw new IllegalStateException("Non model complex type is used for model field '" + field.getName() + "' of model-type: " + parentCls.getName());
				}
				
				String fieldServerType = modelAnnotation.name();
				
				if(fieldServerType == null || fieldServerType.length() == 0)
				{
					fieldServerType = fieldType.getSimpleName();
				}
				
				fieldDef.setServerType(fieldServerType);
			}
			else if(dynFieldType == FieldType.STRING)
			{
				if(field.getAnnotation(MultilineText.class) != null)
				{
					fieldDef.setFieldType(FieldType.MULTI_LINE_STRING);
				}
				else
				{
					fieldDef.setFieldType(FieldType.STRING);
				}
			}
			else
			{
				fieldDef.setFieldType(dynFieldType);
			}
		}
		
		if(field.getAnnotation(ReadOnly.class) != null)
		{
			fieldDef.setReadOnly(true);
		}
		
		//fetch validation details
		Collection<ValidatorConfiguration> validatorConfigurations = validatorConfiguraionFactory.getValidatorConfigurations(parentCls, field);

		//set validators to field-def if not empty
		if(CollectionUtils.isNotEmpty(validatorConfigurations))
		{
			fieldDef.setValidatorConfigurations(new ArrayList<>(validatorConfigurations));
		}
		
		return fieldDef;
	}

	private List<FieldDef> fetchFieldDetails(Class<?> cls, IMessageProvider messageProvider)
	{
		Field fields[] = cls.getDeclaredFields();
		ServerField serverField = null;
		
		FieldDef fieldDef = null;
		List<FieldDef> fieldDefLst = new ArrayList<>();
		
		for(Field field: fields)
		{
			if(Modifier.isStatic(field.getModifiers()))
			{
				continue;
			}
			
			serverField = field.getAnnotation(ServerField.class);
			
			if(serverField != null)
			{
				continue;
			}
			
			fieldDef = getFieldDef(cls, new FieldDataSpecifier(field), messageProvider);
			fieldDefLst.add(fieldDef);
		}
		
		
		//Fetch the properties from the specified bean class
		try
		{
			BeanInfo beanInfo = Introspector.getBeanInfo(cls);
			PropertyDescriptor propertyDescriptors[] = beanInfo.getPropertyDescriptors();
			PropertyDataSpecifier propDataSpecifier = null;
			
			if(propertyDescriptors != null)
			{
				for(PropertyDescriptor propDesc: propertyDescriptors)
				{
					if(propDesc instanceof IndexedPropertyDescriptor)
					{
						continue;
					}
					
					propDataSpecifier = new PropertyDataSpecifier(propDesc.getReadMethod(), propDesc.getWriteMethod());
					
					if(propDataSpecifier.getAnnotation(FieldMethod.class) == null)
					{
						continue;
					}
					
					fieldDef = getFieldDef(cls, propDataSpecifier, messageProvider);
					fieldDefLst.add(fieldDef);
				}
			}
			
			return fieldDefLst;
		}catch(IntrospectionException ex)
		{
			throw new IllegalStateException("An error occurred while introspecting properties of class: " + cls.getName(), ex);
		}
	}
	
	public void fetchDynamicFieldDetails(Class<?> cls, DynamicType dynamicType)
	{
		DynamicType dbTypeInst = dynamicDataDao.getDynamicTypeByName(dynamicType.getName());
		
		if(dbTypeInst == null)
		{
			return;
		}
		
		dynamicType.addFields(dbTypeInst.getFields());
		
		dynamicType.setLabel(dbTypeInst.getLabel());
		dynamicType.setDescription(dbTypeInst.getDescription());
	}
	
	/**
	 * For extensible types, dynamic fields will get added. And extended fields should not be cached here
	 * as they may be changed by user at any time. Caching of dynamic fields should be done dynamic dao.
	 * 
	 * @param beanType
	 * @param messageProvider
	 * @return
	 */
	private DynamicType getStaticDetails(Class<?> beanType, IMessageProvider messageProvider)
	{
		DynamicType dynamicType = staticDynamicTypes.get(beanType);
		boolean isDynamicType = IExtensible.class.isAssignableFrom(beanType);
		
		if(dynamicType != null)
		{
			return isDynamicType ? dynamicType.clone() : dynamicType;
		}

		String modelName = getModelName(beanType);
		
		dynamicType = new DynamicType();
		dynamicType.setName(modelName);
		
		Label label = beanType.getAnnotation(Label.class);
		
		if(label != null)
		{
			dynamicType.setLabel(label.value());
		}
		else
		{
			dynamicType.setLabel(getDefaultLabel(beanType.getSimpleName()));
		}
		
		Class<?> cls = beanType;
		List<FieldDef> fieldDefLst = null;
		
		while(cls != null)
		{
			if(cls.getName().startsWith("java"))
			{
				break;
			}
			
			try
			{
				fieldDefLst = fetchFieldDetails(cls, messageProvider);
				
				//while adding if child classes already specified field, dont override it by parent class field
				dynamicType.addFieldsAtStarting(fieldDefLst, false);
			}catch(RuntimeException ex)
			{
				logger.error("An error occurred while fetching field details for class: " + cls.getName(), ex);
				throw ex;
			}
			
			cls = cls.getSuperclass();
		}
		
		staticDynamicTypes.put(beanType, dynamicType);
		return isDynamicType ? dynamicType.clone() : dynamicType;
	}
	
	public DynamicType buildDynamicType(Class<?> beanType, IMessageProvider messageProvider)
	{
		boolean isDynamicType = IExtensible.class.isAssignableFrom(beanType);
		DynamicType dynamicType = getStaticDetails(beanType, messageProvider);

		if(isDynamicType)
		{
			fetchDynamicFieldDetails(beanType, dynamicType);
		}

		return dynamicType;
	}

}
