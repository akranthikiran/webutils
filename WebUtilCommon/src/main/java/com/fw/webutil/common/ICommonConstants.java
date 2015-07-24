package com.fw.webutil.common;

import java.text.DecimalFormat;
import java.text.NumberFormat;
import java.text.SimpleDateFormat;
import java.util.regex.Pattern;

public interface ICommonConstants
{
	public String APP_TITLE = "RealEstate.com";
	
	public Pattern EMAIL_PATTERN = Pattern.compile("[\\w\\.\\-]+\\@[\\w\\.\\-]+"); 
			
	public String URL_LOGIN = "/action/login";
	public String URL_LOGIN_PAGE = "/jsp/login.jsp";
	public String URL_SPA_PAGE = "/jsp/index.jsp";
	
	
	public String URL_LOGOUT = "/action/logout";
	public String URL_LOGOUT_PAGE = "/jsp/logout.jsp";
	public String URL_AUTHENTICATE = "/action/authenticate";
	public String URL_HOME = "/jsp/index.jsp";
	public String URL_ACTIONS = "/action/fetchActions";
	
	
	
	public String LOGIN_ERROR_KEY = "loginErrorMessage";
	
	public String PAGE_HOME = "home.page";
	public String PAGE_LOGIN = "login.page";
	public String PAGE_FIRST_TIME_USER = "first.time.user";
	
	public String SES_ATTR_USER_DETAILS = "sessionUserDetails";
	
	public String REQ_ATTR_USER_DETAILS = "loginUser";
	public String REQ_ATTR_ERROR_CODE = "errorCode";
	public String REQ_ATTR_SITE_ADMIN = "siteAdmin";
	
	public String REQ_ATTR_ERROR_LIST = "errorList";
	
	public String REQ_ATTR_ACTUAL_PAGE_URL = "actualPageUrl";
	
	public String ERR_OPENID_UNSUPPORTED = "error.openId.unsupported";
	public String ERR_OPENID_UNEXPECTED = "error.openId.unexpected";
	
	public String ERR_OPENID_RETURN_UNKNOWN = "error.openId.return.unknown";
	
	public String ERR_AUTH_NON_REGISTERED = "error.auth.non.registered";
	
	public String REQ_ATTR_DEF_MODEL_FORM = "formObject";
	public String REQ_ATTR_MODEL_FORM_ATTR_NAME = "formObjectAttrName";
	public String ATTR_FORM_OBJECT_TYPE = "formObjectType";

	public static SimpleDateFormat DEFAULT_DATE_FORMAT = new SimpleDateFormat("dd/MM/yyyy");
	public static SimpleDateFormat DEFAULT_DATE_TIME_FORMAT = new SimpleDateFormat("dd/MM/yyyy hh:mm aa");
	
	public static NumberFormat DEFAULT_PRICE_FORMAT = new DecimalFormat("##.##");
	public static NumberFormat DEFAULT_NUMBER_FORMAT = new DecimalFormat("##.##");
	
	public static final int WEEK_DAY_COUNT = 7;
}
