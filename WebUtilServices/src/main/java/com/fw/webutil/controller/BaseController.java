package com.fw.webutil.controller;

import java.io.IOException;

import javax.servlet.http.HttpServletResponse;
import javax.validation.ValidationException;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.bind.annotation.RestController;

import com.fw.persistence.UniqueConstraintViolationException;
import com.fw.webutil.common.GenericResponse;
import com.fw.webutil.common.IResponseCodes;
import com.fw.webutil.common.InvalidParameterException;
import com.fw.webutil.common.UnsatisfiedRelationException;
import com.fw.webutil.security.ISecurityService;
import com.fw.webutil.service.conversion.BeanConversionService;

@RestController
public class BaseController
{
	private static Logger logger = LogManager.getLogger(BaseController.class);
	
	@Autowired
	protected ISecurityService securityService;
	
	@Autowired
	protected BeanConversionService beanConversionService;

	@ExceptionHandler(value={UniqueConstraintViolationException.class, com.fw.persistence.UniqueConstraintViolationException.class})
	@ResponseBody
	public GenericResponse handleJobException(HttpServletResponse response, Exception ex) throws IOException
	{
		Exception uniqueConstrException = (Exception)ex;
	
		logger.error("A unique-constraint-exception occurred while processing request: " + uniqueConstrException);
		
		response.setStatus(HttpServletResponse.SC_BAD_REQUEST);
		
		return new GenericResponse(IResponseCodes.ERROR_UNIQUE_CONSTRAINT_VIOLATION, null, uniqueConstrException.getMessage());
	}
	
	@ExceptionHandler(value={UnsatisfiedRelationException.class})
	@ResponseBody
	public GenericResponse handleUnsatisfiedRelationException(HttpServletResponse response, Exception ex) throws IOException
	{
		UnsatisfiedRelationException unsatisfiedRelationException = (UnsatisfiedRelationException)ex;
	
		logger.error("A UnsatisfiedRelationException occurred while processing request: " + unsatisfiedRelationException);
		
		response.setStatus(HttpServletResponse.SC_BAD_REQUEST);
		
		String field = unsatisfiedRelationException.getFieldName();
		
		return new GenericResponse(IResponseCodes.ERROR_UNSATISFIED_RELATION, field, unsatisfiedRelationException.getMessage());
	}
	
	@ExceptionHandler(value={ValidationException.class})
	@ResponseBody
	public GenericResponse handleValidationException(HttpServletResponse response, Exception ex) throws IOException
	{
		ValidationException validationException = (ValidationException)ex;
	
		logger.error("A ValidationException occurred while processing request: " + validationException);
		
		response.setStatus(HttpServletResponse.SC_BAD_REQUEST);
		
		return new GenericResponse(IResponseCodes.VALIDATION_ERROR, null, validationException.getMessage());
	}

	@ExceptionHandler(value={InvalidParameterException.class})
	@ResponseBody
	public GenericResponse handleInvalidParameterException(HttpServletResponse response, Exception ex) throws IOException
	{
		InvalidParameterException invalidParameterException = (InvalidParameterException)ex;
	
		logger.error("A InvalidParameterException occurred while processing request: " + invalidParameterException);
		
		response.setStatus(HttpServletResponse.SC_BAD_REQUEST);
		
		return new GenericResponse(IResponseCodes.VALIDATION_ERROR, null, invalidParameterException.getMessage());
	}

	@ExceptionHandler(value={Exception.class})
	@ResponseBody
	public GenericResponse handleException(HttpServletResponse response, Exception ex)
	{
		logger.error("An uknown exception occurred while processing request: " + ex);
		
		response.setStatus(HttpServletResponse.SC_BAD_REQUEST);
		
		return new GenericResponse(IResponseCodes.ERROR_UNKNOWN, null, "Unknown Error: " + ex.getMessage());
	}
	
	/*
	protected String handleImportRequest(HttpServletRequest request, String expectedFileType, String expectedFileTypeName) throws FileUploadException
	{
		return handleImportRequest(request, expectedFileType, expectedFileTypeName, null);
	}
	
	protected String handleImportRequest(HttpServletRequest request, String expectedFileType, String expectedFileTypeName, ObjectWrapper<String> fileNameWrapper) throws FileUploadException
	{
		String tempDir = System.getProperty("java.io.tmpdir");
		DiskFileItemFactory factory = new DiskFileItemFactory(0, new File(tempDir));

		// Create a new file upload handler
		ServletFileUpload upload = new ServletFileUpload(factory);

		// Parse the request
		List<FileItem> items = upload.parseRequest(request);
		
		if(items == null || items.isEmpty())
		{
			throw new InvalidParameterException("No files found in the request");
		}
		
		if(items.size() > 1)
		{
			throw new InvalidParameterException("Too many files found in request. Only one file is expected");
		}
		
		FileItem item = items.get(0);
		String uploadedFile = ((DiskFileItem)item).getStoreLocation().getAbsolutePath();
		String fileName = item.getFieldName();
		
		if(!expectedFileType.equalsIgnoreCase(FilenameUtils.getExtension(fileName)))
		{
			throw new InvalidParameterException("Non-" + expectedFileTypeName + " file specified for import"); 
		}
		
		if(fileNameWrapper != null)
		{
			fileNameWrapper.setObject(fileName);
		}

		return uploadedFile;
	}
	*/
}
