package com.fw.webutil.controller;

import java.io.IOException;
import java.lang.annotation.Annotation;
import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.TreeMap;

import javax.annotation.PostConstruct;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.bind.annotation.RestController;

import com.fw.webutil.common.ICommonConstants;
import com.fw.webutil.common.annotations.Label;
import com.fw.webutil.common.model.ActionModel;
import com.fw.webutil.common.model.UserRoles;
import com.fw.webutil.common.security.IRole;
import com.fw.webutil.common.security.IRoleGroup;
import com.fw.webutil.security.ISecurityService;
import com.fw.webutil.security.IUser;
import com.fw.webutil.service.ClassScannerService;
import com.fw.webutil.util.CommonUtils;

@RestController
@RequestMapping("/action")
public class CommonController
{
	private static RequestMethod DEFAULT_METHODS[] = {RequestMethod.POST};
	@Autowired
	private ISecurityService securityService;
	
	@Autowired
	private ClassScannerService classScanService;
	
	private List<ActionModel> actionModels;
	
	private void loadActions(Class<?> cls, Map<String, ActionModel> nameToModel)
	{
		if(cls.getName().startsWith("java"))
		{
			return;
		}
		
		String clsRequestMapping = null;
		RequestMapping requestMapping = cls.getAnnotation(RequestMapping.class);
		
		if(requestMapping != null)
		{
			clsRequestMapping = requestMapping.value()[0];
		}
		else
		{
			clsRequestMapping = "";
		}
		
		Label label = cls.getAnnotation(Label.class);
		String classActionName = (label != null) ? label.value() : null;
		
		String actionName = null;
		RequestMethod requestMethods[] = null;
		Annotation fullParamAnnotations[][] = null;
		boolean bodyExpected = false;
		
		for(Method method: cls.getMethods())
		{
			if(Modifier.isStatic(method.getModifiers()))
			{
				continue;
			}
			
			requestMapping = method.getAnnotation(RequestMapping.class);
			label = method.getAnnotation(Label.class);
			
			if(requestMapping == null)
			{
				continue;
			}

			actionName = (label == null) ? method.getName() : label.value(); 
			
			if(classActionName != null)
			{
				actionName = classActionName + "." + actionName;
			}
			
			if(nameToModel.containsKey("actionName"))
			{
				throw new IllegalStateException("Duplicate action configuration encountered for action: " + actionName);
			}
			
			requestMethods = requestMapping.method();
			
			if(requestMethods.length == 0)
			{
				requestMethods = DEFAULT_METHODS;
			}
			
			fullParamAnnotations = method.getParameterAnnotations();
			bodyExpected = false;
			
			if(fullParamAnnotations != null)
			{
				for(Annotation paramAnnotations[]: fullParamAnnotations)
				{
					if(paramAnnotations == null || paramAnnotations.length == 0)
					{
						continue;
					}
					
					for(Annotation annotation: paramAnnotations)
					{
						if(RequestBody.class.equals(annotation.annotationType()))
						{
							bodyExpected = true;
						}
					}
				}
			}
			
			
			nameToModel.put(actionName, new ActionModel(actionName, clsRequestMapping + requestMapping.value()[0], requestMethods[0].name(), bodyExpected));
		}
	}
	
	@PostConstruct
	private void init()
	{
		if(actionModels != null)
		{
			return;
		}
		
		Map<String, ActionModel> nameToModel = new TreeMap<>();
		
		Set<Class<?>> types = new HashSet<>(classScanService.getClassesWithAnnotation(Controller.class));
		types.addAll(classScanService.getClassesWithAnnotation(RestController.class));
		
		for(Class<?> cls: types)
		{
			loadActions(cls, nameToModel);
		}
		
		this.actionModels = new ArrayList<>(nameToModel.values());
	}
	
	@RequestMapping(value = "/authenticate")
	@ResponseBody
	public void authenticate(@RequestParam("userName") String userName, @RequestParam("password") String password, 
			HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException
	{
		IUser user = securityService.authenticateAndFetch(userName, password);
		
		if(user == null)
		{
			request.setAttribute(ICommonConstants.LOGIN_ERROR_KEY, "Invalid user-name/password specified. Please check!");
			//request.getRequestDispatcher(ICommonConstants.URL_LOGIN).forward(request, response);
			request.getRequestDispatcher(ICommonConstants.URL_LOGIN_PAGE).forward(request, response);
			return;
		}
		
		request.getSession().setAttribute(ICommonConstants.SES_ATTR_USER_DETAILS, user);
		request.setAttribute(ICommonConstants.REQ_ATTR_USER_DETAILS, user);
		
		request.getRequestDispatcher(ICommonConstants.URL_HOME).forward(request, response);
	}

	@RequestMapping(value = "/logout")
	@ResponseBody
	public void logout(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException
	{
		request.getSession().removeAttribute(ICommonConstants.SES_ATTR_USER_DETAILS);
		request.removeAttribute(ICommonConstants.REQ_ATTR_USER_DETAILS);
		
		request.getRequestDispatcher(ICommonConstants.URL_LOGOUT_PAGE).forward(request, response);
	}

	@RequestMapping(value = "/fetchActions")
	@ResponseBody
	public List<ActionModel> fetchActions() throws ServletException, IOException
	{
		return actionModels;
	}

	@RequestMapping(value = "/fetchUserRolesAndGroups")
	@ResponseBody
	public UserRoles fetchUserRolesAndGroups() throws ServletException, IOException
	{
		IUser user = securityService.getCurrentUser();
		Collection<IRole<?>> roles = user.getRoles();
		Collection<IRoleGroup> groups = user.getRoleGroups();
		
		return new UserRoles(CommonUtils.toStringList(roles), CommonUtils.toStringList(groups));
	}
}
