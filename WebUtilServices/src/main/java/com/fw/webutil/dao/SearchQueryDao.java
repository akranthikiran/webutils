package com.fw.webutil.dao;

import java.util.Arrays;
import java.util.List;
import java.util.concurrent.locks.ReentrantLock;

import javax.annotation.PostConstruct;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;

import com.fw.dao.qry.ConnectionSource;
import com.fw.dao.qry.QueryManager;
import com.fw.dao.qry.impl.BeanQueryFilter;
import com.fw.dao.qry.impl.Record;
import com.fw.dao.qry.impl.XMLQueryFactory;
import com.fw.persistence.PersistenceException;
import com.fw.webutil.common.annotations.Dao;
import com.fw.webutil.query.QueryNotFoundException;
import com.fw.webutil.query.QueryResult;
import com.fw.webutil.security.ISecurityService;
import com.fw.webutil.util.QueryFunctions;

@Dao
public class SearchQueryDao
{
	private static Logger logger = LogManager.getLogger(SearchQueryDao.class);
	
	private static final String PARAM_JSON_COLUMNS = "jsonColumns";
	
	private QueryManager queryManager;
	private BeanQueryFilter beanQueryFilter = new BeanQueryFilter();
	
	private ReentrantLock reentrantLock = new ReentrantLock();
	
	@Autowired
	private ISecurityService securityService;
	
	@Autowired
	private ConnectionSource connectionSource;

	@PostConstruct
	private void init()
	{
		queryManager = XMLQueryFactory.loadFromXML("/queries/search-queries.xml", connectionSource, false);
	}
	
	public QueryResult executeQuery(String queryName, Object queryParams)
	{
		if(!queryManager.hasQuery(queryName))
		{
			throw new QueryNotFoundException(queryName);
		}
		
		reentrantLock.lock();
		
		try
		{
			beanQueryFilter.setBean(queryParams);
			beanQueryFilter.addExtraProperty("activeUserId", securityService.getCurrentUser().getId());
			
			List<Record> records = queryManager.fetchRecords(queryName, beanQueryFilter);
			
			if(records == null || records.isEmpty())
			{
				return null;
			}
			
			String jsonColumnsStr = queryManager.getQueryParam(queryName, PARAM_JSON_COLUMNS);
			String jsonColumns[] = jsonColumnsStr != null ? jsonColumnsStr.split("\\s*\\,\\s*") : null;
			
			Record record = records.get(0);
			QueryResult result = new QueryResult(Arrays.asList(record.getColumnNames()));
			String colValue = null;
			List<String> columns = Arrays.asList(record.getColumnNames());
			
			for(Record rec: records)
			{
				if(jsonColumns != null)
				{
					for(String column: jsonColumns)
					{
						colValue = rec.getString(column);
						
						if(colValue == null || colValue.trim().length() == 0)
						{
							continue;
						}
						
						rec.set(columns.indexOf(column), column, QueryFunctions.parseJson(colValue).toString());
					}
				}
				
				result.addRow(Arrays.asList(rec.getValues()));
			}
			
			return result;
		}catch(Exception ex){
			logger.error("An error occurred while executing search query: " + queryName, ex);
			throw new PersistenceException("An error occurred while executing search query: " + queryName, ex);
		}finally
		{
			reentrantLock.unlock();
		}
	}
}
