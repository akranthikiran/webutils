package com.fw.webutil.dao;

import java.security.InvalidParameterException;
import java.sql.SQLException;
import java.util.Collections;
import java.util.List;
import java.util.Set;
import java.util.concurrent.locks.ReentrantLock;

import javax.annotation.PostConstruct;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;

import com.fw.dao.qry.ConnectionSource;
import com.fw.dao.qry.QueryManager;
import com.fw.dao.qry.impl.BeanQueryFilter;
import com.fw.dao.qry.impl.MapQueryFilter;
import com.fw.dao.qry.impl.XMLQueryFactory;
import com.fw.persistence.PersistenceException;
import com.fw.persistence.UniqueConstraintViolationException;
import com.fw.webutil.common.CurrentlyInUseException;
import com.fw.webutil.common.ICacheManager;
import com.fw.webutil.common.ValueLabel;
import com.fw.webutil.common.annotations.Dao;
import com.fw.webutil.common.annotations.SystemLovType;
import com.fw.webutil.entity.LovEntity;
import com.fw.webutil.entity.LovValueEntity;
import com.fw.webutil.service.ClassScannerService;

@Dao
public class LovDao
{
	private static Logger logger = LogManager.getLogger(LovDao.class);
	
	private QueryManager queryManager;
	private MapQueryFilter mapQueryFilter = new MapQueryFilter();
	private BeanQueryFilter beanQueryFilter = new BeanQueryFilter();
	
	private ReentrantLock reentrantLock = new ReentrantLock();
	
	@Autowired
	private ICacheManager cacheManager;
	
	@Autowired
	private ClassScannerService classScannerService;
	
	@Autowired
	private ConnectionSource connectionSource;

	@PostConstruct
	private void init()
	{
		queryManager = XMLQueryFactory.loadFromXML("/queries/lov-queries.xml", connectionSource, false);
		
		Set<Class<?>> types = classScannerService.getClassesWithAnnotation(SystemLovType.class);
		SystemLovType systemLovType = null;
		
		if(types == null)
		{
			return;
		}
		
		try
		{
			for(Class<?> type: types)
			{
				systemLovType = type.getAnnotation(SystemLovType.class);
				queryManager.executeUpdate("addSystemLov", beanQueryFilter.setBean(new LovEntity(null, systemLovType.name(), 
								systemLovType.description(), LovEntity.FLAG_SYSTEM_LOV, systemLovType.type(), null)));
			}
		}catch(Exception ex)
		{
			throw new IllegalStateException("An error occurred while registering system LOVs", ex);
		}
	}
	
	public List<ValueLabel> fetchLovValues(String queryName, String parentId)
	{
		reentrantLock.lock();
		
		try
		{
			List<ValueLabel> lovLst = queryManager.fetchBeans(queryName, mapQueryFilter.setValues("parentId", parentId));
			
			if(lovLst == null)
			{
				lovLst = Collections.emptyList();
			}
			
			return lovLst;
		}catch(SQLException ex){
			logger.error("An error occurred while fetching LOV values with query: " + queryName, ex);
			throw new PersistenceException("An error occurred while fetching LOV values with query: " + queryName, ex);
		}finally
		{
			reentrantLock.unlock();
		}
	}
	
	public String getLovValue(String queryName, String parentId, String label)
	{
		reentrantLock.lock();
		
		try
		{
			ValueLabel valueLabel = (ValueLabel) queryManager.fetchBean(queryName, mapQueryFilter.setValues("parentId", parentId, "name", label));
			
			if(valueLabel == null)
			{
				return null;
			}
			
			return valueLabel.getValue();
		}catch(SQLException ex){
			logger.error("An error occurred while fetching LOV value with [query: " + queryName + ", Label: " + label + "]", ex);
			throw new PersistenceException("An error occurred while fetching LOV value with [query: " + queryName + ", Label: " + label + "]", ex);
		}finally
		{
			reentrantLock.unlock();
		}
	}
	
	/**************************************************************************************************
	 * 				Custom LOV related functions
	 ****************************************************************************************************/

	public LovEntity fetchLov(String id)
	{
		//TODO: LOV should hold values for efficient caching
		reentrantLock.lock();
		
		try
		{
			LovEntity entity = cacheManager.getBeanById(LovEntity.class, id);
			
			if(entity != null)
			{
				return entity;
			}
			
			entity = (LovEntity)queryManager.fetchBean("fetchLovEntity", mapQueryFilter.setValues("id", id));
			
			if(entity != null)
			{
				cacheManager.cacheBean(entity);
			}
			
			return entity;
		}catch(SQLException ex){
			logger.error("An error occurred while fetching LOV: " + id, ex);
			throw new PersistenceException("An error occurred while fetching LOV: " + id, ex);
		}finally
		{
			reentrantLock.unlock();
		}
	}

	public String fetchLovId(String name)
	{
		//TODO: Can we cache this???
		reentrantLock.lock();
		
		try
		{
			return queryManager.fetchString("fetchLovId", mapQueryFilter.setValues("name", name));
		}catch(SQLException ex){
			logger.error("An error occurred while fetching LOV-id with name: " + name, ex);
			throw new PersistenceException("An error occurred while fetching LOV-id with name: " + name, ex);
		}finally
		{
			reentrantLock.unlock();
		}
	}

	public void addLov(LovEntity lovEntity)
	{
		reentrantLock.lock();
		
		try
		{
			checkForLovName(lovEntity.getName(), null);
			
			int count = queryManager.executeUpdate("addLovEntity", beanQueryFilter.setBean(lovEntity));
			
			if(count <= 0)
			{
				logger.error("Failed to create lov entity: " + lovEntity);
				throw new PersistenceException("Failed to create lov entity");
			}

		}catch(SQLException ex){
			logger.error("An error occurred while creating LOV: " + lovEntity, ex);
			throw new PersistenceException("An error occurred while creating LOV: " + lovEntity.getName(), ex);
		}finally
		{
			reentrantLock.unlock();
		}
	}

	public void updateLov(LovEntity lovEntity)
	{
		reentrantLock.lock();
		
		try
		{
			checkForLovIdValidity(lovEntity.getId());
			checkForLovName(lovEntity.getName(), lovEntity.getId());
			
			int count = queryManager.executeUpdate("updateLovEntity", beanQueryFilter.setBean(lovEntity));
			
			if(count <= 0)
			{
				logger.error("Failed to update lov entity: " + lovEntity);
				throw new PersistenceException("Failed to update lov entity");
			}
			
			cacheManager.clearBeansWithIds(LovEntity.class, lovEntity.getId());
			
		}catch(SQLException ex){
			logger.error("An error occurred while updating LOV: " + lovEntity, ex);
			throw new PersistenceException("An error occurred while updating LOV: " + lovEntity.getName(), ex);
		}finally
		{
			reentrantLock.unlock();
		}
	}

	public void deleteLov(String lovId)
	{
		reentrantLock.lock();
		
		try
		{
			//check if specified lov is system lov
			int count = queryManager.fetchInt("checkForSysLovId", mapQueryFilter.setValues("id", lovId));
			
			if(count > 0)
			{
				logger.error("System lov can not be deleted: " + lovId);
				throw new InvalidParameterException("System lov can not be deleted: " + lovId);
			}
			
			checkForLovIdValidity(lovId);
			checkForLovUsage(lovId);
			
			count = queryManager.executeUpdate("deleteLovEntity", mapQueryFilter.setValues("id", lovId));
			
			if(count <= 0)
			{
				logger.error("Failed to delete lov entity: " + lovId);
				throw new PersistenceException("Failed to delete lov entity: " + lovId);
			}
			
			cacheManager.clearBeansWithIds(LovEntity.class, lovId);
			
		}catch(SQLException ex){
			logger.error("An error occurred while deleting LOV: " + lovId, ex);
			throw new PersistenceException("An error occurred while deleting LOV: " + lovId, ex);
		}finally
		{
			reentrantLock.unlock();
		}
	}

	public LovValueEntity fetchLovValue(String id)
	{
		reentrantLock.lock();
		
		try
		{
			LovValueEntity entity = (LovValueEntity)queryManager.fetchBean("fetchLovValueEntity", mapQueryFilter.setValues("id", id));
			
			return entity;
		}catch(SQLException ex){
			logger.error("An error occurred while fetching LOV-value: " + id, ex);
			throw new PersistenceException("An error occurred while fetching LOV-value: " + id, ex);
		}finally
		{
			reentrantLock.unlock();
		}
	}

	public void addLovValue(LovValueEntity lovValueEntity)
	{
		reentrantLock.lock();
		
		try
		{
			//check if parent lov id is present
			checkForLovIdValidity(lovValueEntity.getParentLovId());
			checkForLovValueName(lovValueEntity.getName(), lovValueEntity.getParentLovId(), null);
			
			int count = queryManager.executeUpdate("addLovValue", beanQueryFilter.setBean(lovValueEntity));
			
			if(count <= 0)
			{
				logger.error("Failed to create lov-value entity: " + lovValueEntity);
				throw new PersistenceException("Failed to create lov-value entity");
			}

			cacheManager.clearBeansWithIds(LovEntity.class, lovValueEntity.getParentLovId());
			
		}catch(SQLException ex){
			logger.error("An error occurred while creating LOV: " + lovValueEntity, ex);
			throw new PersistenceException("An error occurred while creating LOV-value: " + lovValueEntity.getName(), ex);
		}finally
		{
			reentrantLock.unlock();
		}
	}

	public void updateLovValue(LovValueEntity lovValueEntity)
	{
		reentrantLock.lock();
		
		try
		{
			checkForLovValueName(lovValueEntity.getName(), lovValueEntity.getParentLovId(), lovValueEntity.getId());
			checkForLovValueIdValidity(lovValueEntity.getId());
			
			int count = queryManager.executeUpdate("updateLovValue", beanQueryFilter.setBean(lovValueEntity));
			
			if(count <= 0)
			{
				logger.error("Failed to update lov-value entity: " + lovValueEntity);
				throw new PersistenceException("Failed to update lov-value entity");
			}
			
			cacheManager.clearBeansWithIds(LovEntity.class, lovValueEntity.getParentLovId());
		}catch(SQLException ex){
			logger.error("An error occurred while updating LOV-value: " + lovValueEntity, ex);
			throw new PersistenceException("An error occurred while updating LOV-value: " + lovValueEntity.getName(), ex);
		}finally
		{
			reentrantLock.unlock();
		}
	}

	public void deleteLovValue(String lovValueId)
	{
		reentrantLock.lock();
		
		try
		{
			checkForLovValueIdValidity(lovValueId);
			
			String lovId = queryManager.fetchString("fetchParentLovId", mapQueryFilter.setValues("id", lovValueId));
			
			int count = queryManager.executeUpdate("deleteLovValue", mapQueryFilter.setValues("id", lovValueId));
			
			if(count <= 0)
			{
				logger.error("Failed to delete lov-value entity: " + lovValueId);
				throw new PersistenceException("Failed to delete lov-value entity: " + lovValueId);
			}
			
			cacheManager.clearBeansWithIds(LovEntity.class, lovId);
			
		}catch(SQLException ex){
			logger.error("An error occurred while deleting LOV-value: " + lovValueId, ex);
			throw new PersistenceException("An error occurred while deleting LOV-value: " + lovValueId, ex);
		}finally
		{
			reentrantLock.unlock();
		}
	}

	public String getCustomLovValue(String lovId, String label)
	{
		reentrantLock.lock();
		
		try
		{
			ValueLabel valueLabel = (ValueLabel) queryManager.fetchBean("fetchCustomLovValues", mapQueryFilter.setValues("parentLovId", lovId, "name", label));

			if(valueLabel == null)
			{
				return null;
			}
			
			return valueLabel.getValue();
		}catch(SQLException ex){
			logger.error("An error occurred while fetching LOV value with [lovId: " + lovId + ", Label: " + label + "]", ex);
			throw new PersistenceException("An error occurred while fetching LOV value with [query: " + lovId + ", Label: " + label + "]", ex);
		}finally
		{
			reentrantLock.unlock();
		}
	}

	public List<ValueLabel> getCustomLovValues(String lovId)
	{
		reentrantLock.lock();
		
		try
		{
			List<ValueLabel> lovLst = queryManager.fetchBeans("fetchCustomLovValues", mapQueryFilter.setValues("parentLovId", lovId));
			
			if(lovLst == null)
			{
				lovLst = Collections.emptyList();
			}

			return lovLst;
		}catch(SQLException ex){
			logger.error("An error occurred while fetching LOV values: " + lovId, ex);
			throw new PersistenceException("An error occurred while fetching LOV values: " + lovId, ex);
		}finally
		{
			reentrantLock.unlock();
		}
	}

	private void checkForLovName(String name, String lovId)
	{
		reentrantLock.lock();
		
		try
		{
			int count = queryManager.fetchInt("checkLovUniqueName", mapQueryFilter.setValues("name", name, "id", lovId));
			
			if(count > 0)
			{
				logger.error("A Lov with specified name already exists: " + name);
				throw new UniqueConstraintViolationException("name", "A Lov with specified name already exists: " + name);
			}
			
		}catch(SQLException ex){
			logger.error("An error occurred while checking for lov name uniqueness [name: " + name + ", lovId: " + lovId + "]", ex);
			throw new PersistenceException("An error occurred while checking for lov name uniqueness: " + name, ex);
		}finally
		{
			reentrantLock.unlock();
		}
	}
	
	public void checkForLovIdValidity(String id)
	{
		reentrantLock.lock();
		
		try
		{
			int count = queryManager.fetchInt("checkLovIdValidity", mapQueryFilter.setValues("id", id));
			
			if(count <= 0)
			{
				logger.error("No LOV exists with specified id: " + id);
				throw new InvalidParameterException("No LOV exists with specified id: " + id);
			}
			
		}catch(SQLException ex){
			logger.error("An error occurred while checking for lov id validity: " + id, ex);
			throw new PersistenceException("An error occurred while checking for lov id validity: " + id, ex);
		}finally
		{
			reentrantLock.unlock();
		}
	}

	private void checkForLovUsage(String lovId)
	{
		reentrantLock.lock();
		
		try
		{
			int count = queryManager.fetchInt("checkLovUsage", mapQueryFilter.setValues("lovId", lovId));
			
			if(count > 0)
			{
				logger.error("Specified Lov is in use: " + lovId);
				throw new CurrentlyInUseException("Specified Lov is in use: " + lovId);
			}
		}catch(SQLException ex){
			logger.error("An error occurred while checking for lov usage: " + lovId, ex);
			throw new PersistenceException("An error occurred while checking for lov usage", ex);
		}finally
		{
			reentrantLock.unlock();
		}
	}

	private void checkForLovValueName(String name, String parentLovId, String valueId)
	{
		reentrantLock.lock();
		
		try
		{
			int count = queryManager.fetchInt("checkLovValueUniqueName", mapQueryFilter.setValues("name", name, "parentLovId", parentLovId, "id", valueId));
			
			if(count > 0)
			{
				logger.error("A Lov-value with specified name already exists: " + name + " under lov: " + parentLovId);
				throw new UniqueConstraintViolationException("name", "A Lov-value with specified name already exists: " + name);
			}
			
		}catch(SQLException ex){
			logger.error("An error occurred while checking for lov-value name uniqueness [name: " + name + ", parentLovId: " + parentLovId + ", Id: " + valueId + "]", ex);
			throw new PersistenceException("An error occurred while checking for lov-value name uniqueness: " + name, ex);
		}finally
		{
			reentrantLock.unlock();
		}
	}
	
	private void checkForLovValueIdValidity(String id)
	{
		reentrantLock.lock();
		
		try
		{
			int count = queryManager.fetchInt("checkLovValueIdValidity", mapQueryFilter.setValues("id", id));
			
			if(count <= 0)
			{
				logger.error("No LOV-value exists with specified id: " + id);
				throw new InvalidParameterException("No LOV-value exists with specified id: " + id);
			}
			
		}catch(SQLException ex){
			logger.error("An error occurred while checking for lov-value id validity: " + id, ex);
			throw new PersistenceException("An error occurred while checking for lov-value id validity: " + id, ex);
		}finally
		{
			reentrantLock.unlock();
		}
	}

}
