package com.fw.webutil.common;

import java.util.Locale;

import javax.servlet.http.HttpServletRequest;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.MessageSource;
import org.springframework.stereotype.Component;

@Component
public class SpringMessageProvider implements IMessageProvider
{
	@Autowired
	private MessageSource messageSource;
	
	@Autowired
	private HttpServletRequest request;
	
	private Locale locale;
	
	public SpringMessageProvider()
	{}
	
	public SpringMessageProvider(MessageSource messageSource, Locale locale)
	{
		this.messageSource = messageSource;
		this.locale = locale;
	}
	
	@Override
	public String getMessage(String code, Object... args)
	{
		try
		{
			if(locale == null && request != null)
			{
				locale = request.getLocale();
			}
			
			String message = messageSource.getMessage(code, args, locale);
			
			return (message != null) ? message : code;
		}catch(Exception ex)
		{
			return null;
		}
	}

}
