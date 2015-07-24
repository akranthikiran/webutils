package com.fw.webutil.service.email;

import java.util.Collections;
import java.util.List;
import java.util.Properties;

import org.apache.commons.lang3.StringUtils;

/**
 * Configuration required by Email Service
 * @author akiran
 */
public class EmailServiceConfiguration
{
	public static final String PROP_SMTP_HOST = "mail.smtp.host";
	public static final String PROP_SMTP_PORT = "mail.smtp.port";
	
	public static final String PROP_USE_AUTH = "mail.smtp.auth";
	public static final String PROP_ENABLE_TTLS = "mail.smtp.starttls.enable";

	/**
	* Smtp host  
	*/
	private String smtpHost;
	private Integer smtpPort;

	private boolean useAuthentication = false;
	private String userName;
	private String password;

	private boolean enableTtls = false;
	
	/**
	* Template resources which gives templates for email service
	*/
	private List<String> templateResources;

	public String getSmtpHost()
	{
		return smtpHost;
	}

	public void setSmtpHost(String smtpHost)
	{
		this.smtpHost = smtpHost;
	}

	public Integer getSmtpPort()
	{
		return smtpPort;
	}

	public void setSmtpPort(Integer smtpPort)
	{
		this.smtpPort = smtpPort;
	}

	public boolean isUseAuthentication()
	{
		return useAuthentication;
	}

	public void setUseAuthentication(boolean useAuthentication)
	{
		this.useAuthentication = useAuthentication;
	}

	public String getUserName()
	{
		return userName;
	}

	public void setUserName(String userName)
	{
		this.userName = userName;
	}

	public String getPassword()
	{
		return password;
	}

	public void setPassword(String password)
	{
		this.password = password;
	}

	public boolean isEnableTtls()
	{
		return enableTtls;
	}

	public void setEnableTtls(boolean enableTtls)
	{
		this.enableTtls = enableTtls;
	}
	
	public List<String> getTemplateResources()
	{
		if(templateResources == null)
		{
			return Collections.emptyList();
		}
		
		return templateResources;
	}

	public void setTemplateResources(List<String> templateResources)
	{
		this.templateResources = templateResources;
	}

	/**
	* Validates required configuration params are provided
	*/
	public void validate()
	{
		if(StringUtils.isEmpty(smtpHost))
		{
			throw new IllegalStateException("No SMTP host is provided");
		}

		if(useAuthentication)
		{
			if(StringUtils.isEmpty(userName) || StringUtils.isEmpty(password))
			{
				throw new IllegalStateException("No username/password is provided");
			}
		}
	}

	/**
	* Converts this configuration into properties compatible with java-mail
	*
	* @return
	*/
	public Properties toProperties()
	{
		Properties props = new Properties();
		
		if(useAuthentication)
		{
			props.put(PROP_USE_AUTH, "true");
		}
		
		if(enableTtls)
		{
			props.put(PROP_ENABLE_TTLS, "true");
		}
		
		props.put(PROP_SMTP_HOST, smtpHost);
		
		if(smtpPort != null)
		{
			props.put(PROP_SMTP_PORT, "" + smtpPort);
		}
		
		return props;
	}
}
