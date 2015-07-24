package com.fw.webutil.controller;

import static com.fw.webutil.common.GenericResponse.SUCCESS;

import java.io.IOException;

import javax.annotation.PostConstruct;
import javax.validation.Valid;

import org.apache.commons.beanutils.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseBody;

import com.fw.persistence.ICrudRepository;
import com.fw.persistence.repository.RepositoryFactory;
import com.fw.webutil.common.GenericResponse;
import com.fw.webutil.common.IResponseCodes;
import com.fw.webutil.common.security.IRole;

public class CrudController<M, E, R extends ICrudRepository<E>> extends BaseController
{
	@Autowired
	protected RepositoryFactory repositoryFactory;
	
	protected R repository;
	
	private Class<R> repositoryType;
	private Class<M> modelType;
	private Class<E> entityType;
	
	private IRole<?> defaultRole;
	private IRole<?> roleForCreate, roleForUpdate, roleForDelete, roleForRead;
	
	protected CrudController(Class<M> modelType, Class<E> entityType, Class<R> repositoryType)
	{
		this.repositoryType = repositoryType;
		this.modelType = modelType;
		this.entityType = entityType;
	}
	
	protected void setDefaultRole(IRole<?> defaultRole)
	{
		this.defaultRole = defaultRole;
	}
	
	protected void setRoleForCreate(IRole<?> roleForCreate)
	{
		this.roleForCreate = roleForCreate;
	}
	
	protected void setRoleForUpdate(IRole<?> roleForUpdate)
	{
		this.roleForUpdate = roleForUpdate;
	}
	
	protected void setRoleForDelete(IRole<?> roleForDelete)
	{
		this.roleForDelete = roleForDelete;
	}
	
	protected void setRoleForRead(IRole<?> roleForRead)
	{
		this.roleForRead = roleForRead;
	}
	
	@PostConstruct
	private void init()
	{
		repository = repositoryFactory.getRepository(repositoryType);
	}
	
	protected E toEntity(M model)
	{
		E entity = beanConversionService.convertToType(model, entityType);
		return entity;
	}
	
	protected M toModel(E entity)
	{
		M model = beanConversionService.convertToType(entity, modelType);
		return model;
	}
	
	private void checkForRole(IRole<?> role)
	{
		if(role != null)
		{
			securityService.checkForRoles(role);
		}
		else if(defaultRole != null)
		{
			securityService.checkForRoles(defaultRole);
		}
		
	}
	
	@RequestMapping(value = "/create")
	@ResponseBody
	public GenericResponse create(@RequestBody @Valid M model) throws IOException
	{
		checkForRole(roleForCreate);
		
		E entity = toEntity(model);
		repository.save(entity);
		
		try
		{
			BeanUtils.copyProperty(model, "id", BeanUtils.getProperty(entity, "id"));
		}catch(Exception ex)
		{}
		
		return SUCCESS;
	}
	
	@RequestMapping(value = "/fetch/{entityId}")
	@ResponseBody
	public M fetch(@PathVariable("entityId") String entityId) throws IOException
	{
		checkForRole(roleForRead);
		
		E entity = repository.findById(entityId);
		return toModel(entity);
	}
	
	@RequestMapping(value = "/update")
	@ResponseBody
	public GenericResponse update(@RequestBody @Valid M model)
	{
		checkForRole(roleForUpdate);
		
		/*
		if(model.getId() == null)
		{
			throw new InvalidParameterException("No id is specified for input model for update.");
		}
		*/
		
		E entity = toEntity(model);
		repository.update(entity);
		
		return SUCCESS;
	}

	@RequestMapping(value = "/delete/{entityId}")
	@ResponseBody
	public GenericResponse delete(@PathVariable("entityId") String entityId) throws IOException
	{
		checkForRole(roleForDelete);
		
		boolean res = repository.deleteById(entityId);
		return res ? SUCCESS : new GenericResponse(IResponseCodes.ERROR_NON_EXISTING_ENTITY_VIOLATION, "id", "Failed to delete entity with id " + entityId);
	}
	
}
