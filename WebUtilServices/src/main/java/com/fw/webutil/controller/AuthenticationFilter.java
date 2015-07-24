package com.fw.webutil.controller;

import java.io.IOException;

import javax.servlet.Filter;
import javax.servlet.FilterChain;
import javax.servlet.FilterConfig;
import javax.servlet.ServletException;
import javax.servlet.ServletRequest;
import javax.servlet.ServletResponse;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import com.fw.webutil.common.ICommonConstants;
import com.fw.webutil.security.IUser;

/**
 * Servlet Filter implementation class AuthenticationFilter
 */
public class AuthenticationFilter implements Filter
{
	@Override
	public void destroy()
	{
	}

	@Override
	public void doFilter(ServletRequest request, ServletResponse response, FilterChain chain) throws IOException,ServletException
	{
		HttpServletRequest httpRequest = (HttpServletRequest)request;
		HttpServletResponse httpResponse = (HttpServletResponse)response;
		
		IUser userDetails = (IUser)httpRequest.getSession().getAttribute(ICommonConstants.SES_ATTR_USER_DETAILS);
		
		//TODO: Remove this condition
		/*
		if(userDetails == null)
		{
			WebApplicationContext webContext = WebApplicationContextUtils.getRequiredWebApplicationContext(httpRequest.getServletContext());
			ISecurityService securityService = webContext.getBean(ISecurityService.class);
			userDetails = securityService.authenticateAndFetch("akranthikiran@gmail.com", "password");
			httpRequest.getSession().setAttribute(ICommonConstants.SES_ATTR_USER_DETAILS, userDetails);
		}
		*/
		
		String actualPageUrl = httpRequest.getRequestURI();
		String contextPath = httpRequest.getContextPath();
		
		if(!"/".equals(contextPath) && actualPageUrl.startsWith(contextPath))
		{
			actualPageUrl = actualPageUrl.substring(contextPath.length());
		}
		
		if(userDetails != null)
		{
			httpRequest.setAttribute(ICommonConstants.REQ_ATTR_USER_DETAILS, userDetails);
			
			if(actualPageUrl.startsWith("/page/"))
			{
				httpRequest.getRequestDispatcher(ICommonConstants.URL_SPA_PAGE).forward(httpRequest, response);
				return;
			}
			
			chain.doFilter(request, response);
			return;
		}
		
		if(actualPageUrl.startsWith("/css/") || actualPageUrl.startsWith("/js/") || actualPageUrl.startsWith("/images/") || actualPageUrl.startsWith("/auth/")
				|| actualPageUrl.startsWith(ICommonConstants.URL_LOGIN) 
				|| actualPageUrl.startsWith(ICommonConstants.URL_LOGIN_PAGE) 
				|| actualPageUrl.startsWith(ICommonConstants.URL_AUTHENTICATE)
				|| actualPageUrl.startsWith(ICommonConstants.URL_LOGOUT_PAGE)
				|| actualPageUrl.startsWith(ICommonConstants.URL_LOGOUT))
		{
			chain.doFilter(request, response);
			return;
		}
		
		httpRequest.setAttribute(ICommonConstants.REQ_ATTR_ACTUAL_PAGE_URL, actualPageUrl);
		
		httpResponse.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
		httpRequest.getRequestDispatcher(ICommonConstants.URL_LOGIN_PAGE).forward(httpRequest, response);
	}

	@Override
	public void init(FilterConfig arg0) throws ServletException
	{
	}
}