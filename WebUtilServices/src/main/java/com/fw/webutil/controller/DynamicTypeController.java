package com.fw.webutil.controller;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.bind.annotation.RestController;

import com.fw.webutil.common.model.dynamic.DynamicType;
import com.fw.webutil.dao.DynamicDataDao;

@RestController
@RequestMapping("/action/dynamicTypes")
public class DynamicTypeController
{
	@Autowired
	private DynamicDataDao dynamicDataDao;
	
	@RequestMapping(value = "/fetch")
	@ResponseBody
	public DynamicType fetch(@RequestParam("id") String id)
	{
		return dynamicDataDao.getDynamicTypeByName(id);
	}
}
