package com.fw.webutil.util;

import java.io.IOException;
import java.io.StringWriter;
import java.util.Map;
import java.util.StringTokenizer;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import freemarker.core.Environment;
import freemarker.template.Configuration;
import freemarker.template.TemplateDirectiveBody;
import freemarker.template.TemplateDirectiveModel;
import freemarker.template.TemplateException;
import freemarker.template.TemplateModel;

/**
 * A Singleton representation of Free Marker configuration with custom directives added to it
 * @author akiran
 */
public class FreeMarkerConfiguration
{
	private static Configuration configuration;
	
	public static synchronized Configuration getConfiguration()
	{
		if(configuration != null)
		{
			return configuration;
		}
		
		configuration = new Configuration();
		
		//add custom directive functions
		configuration.setSharedVariable("trim", (TemplateDirectiveModel)FreeMarkerConfiguration::trim);
		configuration.setSharedVariable("indent", (TemplateDirectiveModel)FreeMarkerConfiguration::indent);
		configuration.setSharedVariable("initcap", (TemplateDirectiveModel)FreeMarkerConfiguration::initcap);
		
		return configuration;
	}
	
	/**
	* Free marker directive method. Trims the output content inside this directive-tag 
	* @param env
	* @param params
	* @param loopVars
	* @param body
	* @throws TemplateException
	* @throws IOException
	*/
	@SuppressWarnings("rawtypes")
	private static void trim(Environment env, Map params, TemplateModel[] loopVars, TemplateDirectiveBody body) throws TemplateException, IOException
	{
		StringWriter writer = new StringWriter();
		body.render(writer);
		
		env.getOut().append(writer.toString().trim());
	}
	
	@SuppressWarnings("rawtypes")
	private static void indent(Environment env, Map params, TemplateModel[] loopVars, TemplateDirectiveBody body) throws TemplateException, IOException
	{
		StringWriter writer = new StringWriter();
		body.render(writer);
		
		String actualOutput = writer.toString();
		Object prefix = params.get("prefix");
		
		if(prefix == null || !(prefix instanceof String))
		{
			prefix = "";
		}
		
		StringTokenizer st = new StringTokenizer(actualOutput, "\n");
		StringBuilder builder = new StringBuilder();
		String line = null;
		
		while(st.hasMoreTokens())
		{
			line = st.nextToken().trim();
			builder.append(prefix).append(line);
		}
		
		String output = builder.toString();
		output = output.replace("\\t", "\t");
		output = output.replace("\\n", "\n");
		
		env.getOut().append(output);
	}

	/**
	* Converts each character of each word in the content to upper case and rest to lower case. 
	* @param env
	* @param params
	* @param loopVars
	* @param body
	* @throws TemplateException
	* @throws IOException
	*/
	@SuppressWarnings("rawtypes")
	private static void initcap(Environment env, Map params, TemplateModel[] loopVars, TemplateDirectiveBody body) throws TemplateException, IOException
	{
		StringWriter writer = new StringWriter();
		body.render(writer);
		
		String actualOutput = writer.toString();
		
		Pattern WORD_PATTERN = Pattern.compile("\\w+");
		Matcher matcher = WORD_PATTERN.matcher(actualOutput);
		String word  = null;
		StringBuilder res = new StringBuilder();
		
		while(matcher.find())
		{
			word = matcher.group();
			word = word.toLowerCase();
			
			word = ("" + word.charAt(0)).toUpperCase() + word.substring(1);
		}
		
		env.getOut().append(res.toString());
	}
}
