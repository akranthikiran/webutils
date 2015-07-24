package com.fw.webutil.dao;

import java.security.InvalidParameterException;
import java.sql.SQLException;
import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.locks.ReentrantLock;

import javax.annotation.PostConstruct;

import org.apache.commons.collections4.CollectionUtils;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;

import com.fw.dao.qry.ConnectionSource;
import com.fw.dao.qry.QueryManager;
import com.fw.dao.qry.Transaction;
import com.fw.dao.qry.impl.BeanQueryFilter;
import com.fw.dao.qry.impl.MapQueryFilter;
import com.fw.dao.qry.impl.XMLQueryFactory;
import com.fw.persistence.PersistenceException;
import com.fw.persistence.UniqueConstraintViolationException;
import com.fw.webutil.common.ICacheManager;
import com.fw.webutil.common.IExtensible;
import com.fw.webutil.common.UnsatisfiedRelationException;
import com.fw.webutil.common.annotations.Dao;
import com.fw.webutil.common.annotations.LovType;
import com.fw.webutil.common.annotations.Model;
import com.fw.webutil.common.model.dynamic.ClientConfiguration;
import com.fw.webutil.common.model.dynamic.DynamicInstance;
import com.fw.webutil.common.model.dynamic.DynamicType;
import com.fw.webutil.common.model.dynamic.FieldDef;
import com.fw.webutil.common.model.dynamic.FieldType;
import com.fw.webutil.common.model.dynamic.Property;
import com.fw.webutil.entity.LovEntity;
import com.fw.webutil.model.dynamic.DynamicTypeFactory;
import com.fw.webutil.model.dynamic.converter.ConverterService;
import com.fw.webutil.service.ClassScannerService;

@Dao
public class DynamicDataDao
{
	private static Logger logger = LogManager.getLogger(DynamicDataDao.class);
	
	private QueryManager queryManager;
	private MapQueryFilter mapQueryFilter = new MapQueryFilter();
	private BeanQueryFilter beanQueryFilter = new BeanQueryFilter();
	
	private ReentrantLock reentrantLock = new ReentrantLock();
	
	@Autowired
	private ICacheManager cacheManager;
	
	@Autowired
	private ClassScannerService classScannerService;
	
	@Autowired
	private DynamicTypeFactory dynamicTypeFactory;
	
	@Autowired
	private ConverterService converterService;

	@Autowired
	private LovDao lovDao;
	
	@Autowired
	private ConnectionSource connectionSource;

	@PostConstruct
	private void init()
	{
		queryManager = XMLQueryFactory.loadFromXML("/queries/common-queries.xml", connectionSource, false);
		
		/*
		try
		{
			List<DynamicType> dynamicTypes = queryManager.fetchBeans("fetchDynamicTypes");
			cacheManager.cacheBeans(dynamicTypes);
		}catch(SQLException ex)
		{
			logger.error("An error occurred while initalizing dynamic-data-dao", ex);
			throw new PersistenceException("An error occurred while initalizing dynamic-data-dao", ex);
		}
		*/

		//Fetch model types from class path entries
		Set<Class<?>> modelTypes = classScannerService.getClassesWithAnnotation(Model.class);
		
		if(modelTypes != null)
		{
			String name = null;
			String label = null;
			
			for(Class<?> type: modelTypes)
			{
				if(!IExtensible.class.isAssignableFrom(type))
				{
					continue;
				}
				
				name = type.getAnnotation(Model.class).name();
				
				if(name.trim().length() == 0)
				{
					name = type.getSimpleName();
				}
				
				try
				{
					label = dynamicTypeFactory.getDefaultLabel(name);
					queryManager.executeUpdate("addDynamicType", beanQueryFilter.setBean(new DynamicType(null, name, label, label, true, null)));
				}catch(SQLException ex)
				{
					logger.error("An error occurred while registering extensible types", ex);
					throw new PersistenceException("An error occurred while registering extensible types", ex);
				}
			}
		}
	}
	
	/*
	public DynamicType getDynamicType(Class<? extends IIdentifiable> staticType)
	{
		if(staticType == null)
		{
			throw new NullPointerException("Static-type can not be null");
		}
		
		if(!IIdentifiable.class.isAssignableFrom(staticType))
		{
			throw new IllegalStateException("Specified static-type is not IIdentifiable - " + staticType.getName());
		}
		
		reentrantLock.lock();
		
		try
		{
			DynamicType dynamicType = cacheManager.getBeanByName(DynamicType.class, staticType.getName());
			
			if(dynamicType != null)
			{
				return dynamicType;
			}
			
			dynamicType = (DynamicType)queryManager.fetchBean("fetchDynamicTypes", mapQueryFilter.setValues("name", staticType.getName()));
			
			if(dynamicType != null)
			{
				cacheManager.cacheBean(dynamicType);
			}
			
			
			int count = queryManager.executeUpdate("addDynamicType", mapQueryFilter.setValues("name", staticType.getName(), 
																"label", staticType.getName(),
																"description", staticType.getName(),
																"javaType", true));
			
			if(count <= 0)
			{
				throw new PersistenceException("An error occurred while creating dynamic instance of static-type: " + staticType.getName());
			}
			
			dynamicType = (DynamicType)queryManager.fetchBean("fetchDynamicTypes", mapQueryFilter.setValues("name", staticType.getName()));
			cacheManager.cacheBean(dynamicType);
			
			return dynamicType;
		}catch(SQLException ex)
		{
			throw new PersistenceException("An exception occurred while fetching dynamic type by name: " + staticType.getName(), ex);
		}finally
		{
			reentrantLock.unlock();
		}
	}
	*/
	
	private void processDynamicType(DynamicType dynamicType)
	{
		List<FieldDef> fields = dynamicType.getFields();
		
		if(fields == null)
		{
			return;
		}
		
		ClientConfiguration clientConfiguration = null;
		LovEntity lov = null;
		
		for(FieldDef field: fields)
		{
			if(field.getFieldType() != FieldType.LIST_OF_VALUES)
			{
				continue;
			}
			
			lov = lovDao.fetchLov(field.getDataId());

			field.setServerType(lov.getType().getJavaType().getName());
			
			clientConfiguration = new ClientConfiguration();
			field.setClientConfiguration(clientConfiguration);
			
			clientConfiguration.setLovType(LovType.CUSTOM_TYPE);
			clientConfiguration.setQueryName(field.getDataId());
		}
	}
	
	
	/**
	 * Method to fetch dynamic details for java-type. These beans will not be cached as it will not contain static field info.
	 * @param name
	 * @return
	 */
	public DynamicType getDynamicTypeByName(String name)
	{
		if(name == null)
		{
			throw new NullPointerException("Name can not be null");
		}
		
		reentrantLock.lock();
		
		try
		{
			DynamicType dynamicType = cacheManager.getBeanByName(DynamicType.class, name);
			
			if(dynamicType != null)
			{
				return dynamicType;
			}
			
			dynamicType = (DynamicType)queryManager.fetchBean("fetchDynamicTypes", mapQueryFilter.setValues("name", name));
			
			if(dynamicType == null)
			{
				return null;
			}
			
			processDynamicType(dynamicType);
			cacheManager.cacheBean(dynamicType);
			
			return dynamicType;
		}catch(SQLException ex)
		{
			logger.error("An exception occurred while fetching dynamic type by name: " + name, ex);
			throw new PersistenceException("An exception occurred while fetching dynamic type by name: " + name, ex);
		}finally
		{
			reentrantLock.unlock();
		}
	}
	
	public DynamicType getDynamicType(String id)
	{
		reentrantLock.lock();
		
		try
		{
			DynamicType dynamicType = cacheManager.getBeanById(DynamicType.class, id);
			
			if(dynamicType != null)
			{
				return dynamicType;
			}
			
			dynamicType = (DynamicType)queryManager.fetchBean("fetchDynamicTypes", mapQueryFilter.setValues("id", id));
			processDynamicType(dynamicType);
			
			cacheManager.cacheBean(dynamicType);
			return dynamicType;
		}catch(SQLException ex)
		{
			logger.error("An exception occurred while fetching dynamic type: " + id, ex);
			throw new PersistenceException("An exception occurred while fetching dynamic type: " + id, ex);
		}finally
		{
			reentrantLock.unlock();
		}
	}
	
	public FieldDef getDynamicField(String id)
	{
		reentrantLock.lock();
		
		try
		{
			FieldDef fieldDef =  (FieldDef)queryManager.fetchBean("fetchDynamicField", mapQueryFilter.setValues("id", id));
			return fieldDef;
		}catch(SQLException ex)
		{
			logger.error("An exception occurred while fetching dynamic field: " + id, ex);
			throw new PersistenceException("An exception occurred while fetching dynamic field: " + id, ex);
		}finally
		{
			reentrantLock.unlock();
		}
	}
	
	public void addDynamicField(FieldDef fieldDef)
	{
		reentrantLock.lock();
		
		try
		{
			checkForFieldValidity(fieldDef.getParentId(), null, fieldDef.getName());
			
			int count = queryManager.executeUpdate("addDynamicField", beanQueryFilter.setBean(fieldDef));

			if(count <= 0)
			{
				logger.error("Failed to save dynamic field: " + fieldDef);
				throw new PersistenceException("Failed to save dynamic field: " + fieldDef);
			}
			
			cacheManager.clearBeansWithIds(DynamicType.class, Arrays.asList(fieldDef.getParentId()));

		}catch(SQLException ex)
		{
			logger.error("An exception occurred while fetching adding field: " + fieldDef, ex);
			throw new PersistenceException("An exception occurred while fetching adding field: " + fieldDef, ex);
		}finally
		{
			reentrantLock.unlock();
		}
	}
	
	public void updateDynamicField(FieldDef fieldDef)
	{
		reentrantLock.lock();
		
		try
		{
			checkForFieldValidity(fieldDef.getParentId(), fieldDef.getId(), fieldDef.getName());
			
			int count = queryManager.executeUpdate("updateDynamicField", beanQueryFilter.setBean(fieldDef));

			if(count <= 0)
			{
				logger.error("Failed to update dynamic field: " + fieldDef);
				throw new PersistenceException("Failed to update dynamic field: " + fieldDef);
			}
			
			cacheManager.clearBeansWithIds(DynamicType.class, Arrays.asList(fieldDef.getParentId()));

		}catch(SQLException ex)
		{
			logger.error("An exception occurred while fetching updating field: " + fieldDef, ex);
			throw new PersistenceException("An exception occurred while fetching updating field: " + fieldDef, ex);
		}finally
		{
			reentrantLock.unlock();
		}
	}
	
	public void deleteDynamicField(String id)
	{
		reentrantLock.lock();
		
		try
		{
			String parentId = queryManager.fetchString("fetchFieldParent", mapQueryFilter.setValues("id", id));
			
			if(parentId == null)
			{
				throw new InvalidParameterException("Invalid field id specified: " + id);
			}
			
			queryManager.executeUpdate("clearDynamicDataByField", mapQueryFilter.setValues("fieldDefId", id));
			
			int count = queryManager.executeUpdate("deleteDynamicField", mapQueryFilter.setValues("id", id));

			if(count <= 0)
			{
				return;
			}

			cacheManager.clearBeansWithIds(DynamicType.class, Arrays.asList(parentId));
		}catch(SQLException ex)
		{
			logger.error("An exception occurred while fetching deleting field: " + id, ex);
			throw new PersistenceException("An exception occurred while fetching deleting field: " + id, ex);
		}finally
		{
			reentrantLock.unlock();
		}
	}
	
	private void checkForFieldValidity(String parentDynamicTypeId, String id, String fieldName) throws SQLException
	{
		int count = queryManager.fetchInt("isValidDynamicTypeId", mapQueryFilter.setValues("id", parentDynamicTypeId));
		
		if(count <= 0)
		{
			logger.error("Invalid parent dynamic type id specified: " + parentDynamicTypeId);
			throw new UnsatisfiedRelationException("parentId", "Invalid parent dynamic type id specified: " + parentDynamicTypeId);
		}
		
		if(id != null)
		{
			count = queryManager.fetchInt("isValidDynamicFieldId", mapQueryFilter.setValues("id", id));

			if(count <= 0)
			{
				logger.error("Invalid field id specified: " + id);
				throw new InvalidParameterException("Invalid field id specified: " + id);
			}
		}
		
		count = queryManager.fetchInt("checkForUniqueFieldName", mapQueryFilter.setValues("parentId", parentDynamicTypeId, "name", fieldName, "id", id));
		
		if(count > 0)
		{
			logger.error("A field with specified name '" + fieldName + "' already exists under dynamic type: " + parentDynamicTypeId);
			throw new UniqueConstraintViolationException("name", "A field with specified name '" + fieldName + "' already exists under dynamic type: " + parentDynamicTypeId);
		}
		
	}

	/*
	public void createOrUpdateDynamicType(String name, String label, String description, boolean javaType, List<FieldDef> fieldDefLst)
	{
		if(name == null)
		{
			throw new NullPointerException("Name can not be null");
		}
		
		if(label == null)
		{
			throw new NullPointerException("Label can not be null");
		}
		
		if(fieldDefLst == null || fieldDefLst.isEmpty())
		{
			throw new IllegalArgumentException("Field def list can not be null or empty");
		}
		
		reentrantLock.lock();
		
		Transaction transaction = null;
		
		try
		{
			DynamicType dynamicType = cacheManager.getBeanByName(DynamicType.class, name);
			
			if(dynamicType == null)
			{
				dynamicType = (DynamicType)queryManager.fetchBean("fetchDynamicTypes", mapQueryFilter.setValues("name", name));
			}
			
			transaction = queryManager.newTransaction();
			
			//if type is already existing remove deleted fields
			if(dynamicType != null)
			{
				dynamicType.setLabel(label);
				dynamicType.setDescription(description);
				
				Map<String, FieldDef> newFieldMap = CommonUtils.toMap(fieldDefLst, "name", String.class);
				
				for(FieldDef field: dynamicType.getFields())
				{
					if(!newFieldMap.containsKey(field.getName()))
					{
						queryManager.executeUpdate("removeDynamicField", mapQueryFilter.setValues("fieldName", field.getName()));
					}
				}
			}
			else
			{
				dynamicType = new DynamicType(null, name, label, description, javaType, Collections.<FieldDef>emptyList());
			}
			
			int count = queryManager.executeUpdate("addDynamicType", beanQueryFilter.setBean(dynamicType));
			
			if(count < 0)
			{
				logger.error("Failed to update create dynamic type entry: " + dynamicType);
				throw new PersistenceException("Failed to update create dynamic type entry: " + dynamicType);
			}
			
			dynamicType = (DynamicType)queryManager.fetchBean("fetchDynamicTypes", mapQueryFilter.setValues("name", name));
			
			for(FieldDef fieldDef: fieldDefLst)
			{
				fieldDef.setParentId(dynamicType.getId());
				count = queryManager.executeUpdate("addDynamicField", beanQueryFilter.setBean(fieldDef));
				
				if(count < 0)
				{
					logger.error("Failed to create/update field-def: " + fieldDef);
					throw new PersistenceException("Failed to create/update field-def: " + fieldDef);
				}	
			}
			
			transaction.commit();
			
			cacheManager.clearBeansWithIds(DynamicType.class, dynamicType.getId());
		}catch(SQLException ex)
		{
			logger.error("An error occurred while setting dynamic fields", ex);
			
			transaction.rollback();
			throw new PersistenceException("An error occurred while setting dynamic fields", ex);
		}catch(RuntimeException ex){
			logger.error("An error occurred while setting dynamic fields", ex);
			
			transaction.rollback();
			throw ex;
		}finally
		{
			reentrantLock.unlock();
		}
	}
	*/
	
	/*
	public void setDynamicFields(Class<?> staticType, String label, String description, List<FieldDef> fieldDefLst)
	{
		if(staticType == null)
		{
			throw new NullPointerException("Static-type can not be null");
		}
		
		if(!IIdentifiable.class.isAssignableFrom(staticType))
		{
			throw new IllegalArgumentException("Specified static-type is not of type IIdentifiable - " + staticType.getName());
		}
		
		createOrUpdateDynamicType(staticType.getName(), label, description, true, fieldDefLst);
	}
	*/
	
	public void fetchDynamicData(IExtensible bean)
	{
		reentrantLock.lock();
		
		try
		{
			String modelName = dynamicTypeFactory.getModelName(bean.getClass());
			
			List<Property> properties = queryManager.fetchBeans("fetchDynamicData", mapQueryFilter.setValues("typeName", modelName, "entityId", bean.getId()));
			
			if(properties != null)
			{
				bean.setExtendedProperties(properties);
			}
			
		}catch(SQLException ex)
		{
			logger.error("An exception occurred while fetching dynamic data for bean: " + bean, ex);
			throw new PersistenceException("An exception occurred while fetching dynamic data", ex);
		}finally
		{
			reentrantLock.unlock();
		}
	}
	
	public void updateDynamicData(IExtensible extensibleBean)
	{
		if(extensibleBean == null)
		{
			throw new NullPointerException("Extensible-bean can not be null");
		}
		
		if(CollectionUtils.isEmpty(extensibleBean.getExtendedProperties()))
		{
			return;
		}

		String modelName = dynamicTypeFactory.getModelName(extensibleBean.getClass());
		DynamicType dynamicType = getDynamicTypeByName(modelName);
		
		if(dynamicType == null)
		{
			logger.error("No dynamic type found with name: " + modelName);
			throw new PersistenceException("No dynamic type found with name: " + modelName);
		}
		
		createOrUpdateDynamicInstance(extensibleBean, dynamicType);
	}
	
	public void deleteDynamicData(Class<?> entityType, String entityId)
	{
		reentrantLock.lock();
		String modelName = null;
		
		try(Transaction transaction = queryManager.newOrExistingTransaction())
		{
			modelName = dynamicTypeFactory.getModelName(entityType);
			DynamicType dynamicType = getDynamicTypeByName(modelName);
			DynamicInstance instance = cacheManager.getBeanByName(DynamicInstance.class, modelName);
			
			if(instance == null)
			{
				instance = (DynamicInstance)queryManager.fetchBean("fetchDynamicInstance", mapQueryFilter.setValues("name", modelName));
			}
			
			if(instance == null)
			{
				//TODO: Transaction close without commit should not be considered for rollback
					// in this case commit is not required as no changes are done, but added to avoid rollback
				transaction.commit();
				return;
			}

			queryManager.executeUpdate("clearDynamicData", mapQueryFilter.setValues("typeId", dynamicType.getId(), "name", modelName));
			queryManager.executeUpdate("removeDynamicInstance", mapQueryFilter.setValues("typeId", dynamicType.getId(), "entityId", entityId));
			
			transaction.commit();
		}catch(SQLException ex)
		{
			logger.error("An error occurred while removing dynamic instance: [Type: " + modelName + " Entity-id: " + entityId + "]", ex);
			throw new PersistenceException("An error occurred while removing dynamic instance: [Type: " + modelName + " Entity-id: " + entityId + "]", ex);
		}catch(RuntimeException ex)
		{
			logger.error("An error occurred while removing dynamic instance: [Type: " + modelName + " Entity-id: " + entityId + "]", ex);
			throw ex;
		}finally
		{
			reentrantLock.unlock();
		}
	}

	private DynamicInstance createOrUpdateDynamicInstance(IExtensible extensibleBean, DynamicType dynamicType)
	{
		String name = extensibleBean.getName();
		
		if(extensibleBean.getId() == null)
		{
			throw new NullPointerException("Exntensible bean id was null: " + extensibleBean);
		}

		if(name == null)
		{
			throw new NullPointerException("Exntensible bean name was null: " + extensibleBean);
		}

		reentrantLock.lock();
		
		String typeId = dynamicType.getId();
		Map<String, Property> propertyMap = extensibleBean.getExtendedPropertyMap();
		
		try(Transaction transaction = queryManager.newOrExistingTransaction())
		{
			DynamicInstance instance = cacheManager.getBeanByName(DynamicInstance.class, name);
			
			if(instance == null)
			{
				logger.trace("No dynamic instance found on map fetching it from db: [typeId: " + typeId + ", entityId: " + extensibleBean.getId() + "]");
				
				instance = (DynamicInstance)queryManager.fetchBean("fetchDynamicInstance", mapQueryFilter.setValues("typeId", typeId, "entityId", "" + extensibleBean.getId()));
			}
			
			if(instance == null)
			{
				int count = queryManager.executeUpdate("addDynamicInstance", mapQueryFilter.setValues("typeId", typeId, "name", extensibleBean.getId(), "entityId", extensibleBean.getId()));
				
				if(count <= 0)
				{
					logger.error("Failed to create dynamic instance: [Name: " + name + " Type: " + typeId + "]");
					throw new PersistenceException("Failed to create dynamic instance: [Name: " + name + " Type: " + typeId + "]");
				}
				
				instance = (DynamicInstance)queryManager.fetchBean("fetchDynamicInstance", mapQueryFilter.setValues("typeId", typeId, "entityId", extensibleBean.getId()));
			}
			else
			{
				logger.debug("clearing dynamic data from db");
				queryManager.executeUpdate("clearDynamicData", mapQueryFilter.setValues("instanceId", instance.getId()));
			}
			
			String jsonValue = null;
			Property property = null;
			Object value = null;
			
			for(FieldDef fieldDef: dynamicType.getFields())
			{
				if(fieldDef.getFieldType() == FieldType.COMPLEX)
				{
					throw new UnsupportedOperationException("Currently complex types are not supported. Complex dynamic field encountered: " + fieldDef.getName());
				}
				
				property = propertyMap.get(fieldDef.getName());
				
				if(property == null)
				{
					continue;
				}
				
				if(property.getValue() == null)
				{
					jsonValue = property.getJsonValue();
					
					if(jsonValue == null || jsonValue.trim().length() == 0)
					{
						continue;
					}
					
					value = converterService.parseJson(jsonValue, fieldDef);
				}
				else
				{
					value = property.getValue();
				}
				
				queryManager.executeUpdate("addDynamicData", mapQueryFilter.setValues("instanceId", instance.getId(), 
												"name", property.getName(), 
												"value", value));
			}
			
			cacheManager.clearBeansWithIds(DynamicInstance.class, Arrays.asList(instance.getId()));
			
			transaction.commit();
			
			return instance;
		}catch(SQLException ex)
		{
			logger.error("An error occurred while creating/updating dynamic instance: [Name: " + name + " Type: " + typeId + "]", ex);
			throw new PersistenceException("An error occurred while creating/updating dynamic instance: [Name: " + name + " Type: " + typeId + "]", ex);
		}catch(RuntimeException ex)
		{
			logger.error("An error occurred while creating/updating dynamic instance: [Name: " + name + " Type: " + typeId + "]", ex);
			throw ex;
		}finally
		{
			reentrantLock.unlock();
		}
	}
	
}
