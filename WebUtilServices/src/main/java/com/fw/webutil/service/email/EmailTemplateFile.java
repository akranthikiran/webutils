package com.fw.webutil.service.email;

import java.util.HashMap;
import java.util.Map;

/**
 * Represents a email template file which can have multiple email templates
 * 
 * @author akiran
 */
public class EmailTemplateFile
{
	private Map<String, EmailTemplate> templates;

	/**
	 * Adds value to {@link #templates}
	 *
	 * @param value
	 */
	public void addTemplate(EmailTemplate template)
	{
		if(templates == null)
		{
			templates = new HashMap<>();
		}

		templates.put(template.getName(), template);
	}

	public Map<String, EmailTemplate> getTemplates()
	{
		return templates;
	}
}
