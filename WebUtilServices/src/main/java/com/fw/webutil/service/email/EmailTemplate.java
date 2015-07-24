package com.fw.webutil.service.email;

import java.io.IOException;
import java.io.StringWriter;
import java.util.Map;

import com.fw.webutil.util.FreeMarkerConfiguration;

import freemarker.template.Configuration;
import freemarker.template.Template;

/**
 * Class representing an email template which is composed of a name, subject and a body
 * @author akiran
 */
public class EmailTemplate
{
	private static Configuration configuration = FreeMarkerConfiguration.getConfiguration();

	private String name;

	private Template bodyTemplate;
	private Template subjectTemplate;

	public void setName(String name)
	{
		this.name = name;
	}
	
	private Template toTemplate(String name, String str)
	{
		try
		{
			return new Template(name, str, configuration);
		}catch(IOException ex)
		{
			throw new IllegalArgumentException("An error occurred while processing template content", ex);
		}
	}

	public void setBody(String body)
	{
		this.bodyTemplate = (body != null) ? toTemplate(name + ".body", body) : null;
	}

	public void setSubject(String subject)
	{
		this.subjectTemplate = (subject != null) ? toTemplate(name + ".subject", subject) : null;
	}
	
	public Template getBodyTemplate()
	{
		return bodyTemplate;
	}
	
	public Template getSubjectTemplate()
	{
		return subjectTemplate;
	}
	
	public String getName()
	{
		return name;
	}
	
	/**
	* Executes the specified freemarker template with given context and returns the result.
	* 
	* If template is null, empty string will be returned
	*
	* @param template
	* @param purpose
	* @param name
	* @param context
	* @return
	*/
	private String processTemplate(Template template, String purpose, String name, Map<String, ?> context)
	{
		if(template == null)
		{
			return "";
		}
		
		try
		{
			StringWriter writer = new StringWriter();
			template.process(context, writer);
			
			writer.flush();
			return writer.toString();
		}catch(Exception ex)
		{
			throw new IllegalStateException("An exception occurred while building " + purpose + " of template: " + name, ex);
		}
	}

	/**
	* Process subject template in given context and returns the result
	* 
	* @param context Context
	* @return
	*/
	public String getSubjectFromTemplate(Map<String, ?> context)
	{
		return processTemplate(subjectTemplate, "Subject", name, context);
	}


	/**
	* Process body template in given context and returns the result
	* 
	* @param context Context
	* @return
	*/
	public String getBodyFromTemplate(Map<String, ?> context)
	{
		return processTemplate(bodyTemplate, "Body", name, context);
	}
}
