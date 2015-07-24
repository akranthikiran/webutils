package com.fw.webutil.controller;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.bind.annotation.RestController;

import com.fw.webutil.common.GenericResponse;
import com.fw.webutil.common.InvalidParameterException;
import com.fw.webutil.common.model.dynamic.FieldDef;
import com.fw.webutil.common.model.dynamic.FieldType;
import com.fw.webutil.dao.DynamicDataDao;
import com.fw.webutil.dao.LovDao;

@RestController
@RequestMapping("/action/dynamicFields")
public class DynamicFieldController extends BaseController
{
	@Autowired
	private DynamicDataDao dynamicDataDao;

	@Autowired
	private LovDao lovDao;

	@RequestMapping(value = "/fetch/{id}")
	@ResponseBody
	public FieldDef fetch(@PathVariable("id") String id)
	{
		return dynamicDataDao.getDynamicField(id);
	}
	
	private void validateFieldDef(FieldDef fieldDef)
	{
		if(fieldDef.getFieldType() == FieldType.COMPLEX)
		{
			throw new InvalidParameterException("Currently complex-types / List-of-values are not supported");
		}

		if(fieldDef.getFieldType() == FieldType.LIST_OF_VALUES)
		{
			if(fieldDef.getDataId() == null)
			{
				throw new InvalidParameterException("No lov-id (data-id) is specified for LOV field-def");
			}
			
			try
			{
				lovDao.checkForLovIdValidity(fieldDef.getDataId());
			}catch(Exception ex)
			{
				throw new InvalidParameterException("Invalid lov-id (data-id) is specified for LOV field-def: " + fieldDef.getDataId());
			}
		}
	}

	@RequestMapping(value = "/create")
	@ResponseBody
	public GenericResponse create(@RequestBody FieldDef fieldDef)
	{
		validateFieldDef(fieldDef);
		
		dynamicDataDao.addDynamicField(fieldDef);
		return GenericResponse.SUCCESS;
	}

	@RequestMapping(value = "/update")
	@ResponseBody
	public GenericResponse update(@RequestBody FieldDef fieldDef)
	{
		validateFieldDef(fieldDef);
		
		dynamicDataDao.updateDynamicField(fieldDef);
		return GenericResponse.SUCCESS;
	}

	@RequestMapping(value = "/delete/{id}")
	@ResponseBody
	public GenericResponse delete(@PathVariable("id") String id)
	{
		dynamicDataDao.deleteDynamicField(id);
		return GenericResponse.SUCCESS;
	}
}
