/*
 * *****************************************************************
 * Define basic configuration and methods
 * *********************************************************************
 */

//TODO: When a method is called on model.setters object, the method is invoked in setters context. Which needs to be corrected
var STD_DATE_FORMAT = new SimpleDateFormat("dd/MM/yyyy");
var SERVER_DATE_FORMAT = new RegExp("(\\d+)\\-(\\d+)\\-(\\d+)", "g");//  yyyy-mm-dd

var EVENT_TYPE_VALUE_CHANGED = "valueChanged";
var EVENT_TYPE_LOV_LIST_CHANGED = "lovListChanged";
var CONTENT_TYPE_JSON = "application/json";

var DATA_ATTR_MODEL = "__model_object";

Array.prototype.deleteElement = function(element){
	var index = this.indexOf(element);
	
	if(index < 0)
	{
		return;
	}
	
	this.splice(index, 1);
};

Array.prototype.deleteAtIndex = function(index){
	
	if(index < 0 || index >= this.length)
	{
		return;
	}
	
	this.splice(index, 1);
};

Array.prototype.clear = function(){
	
	this.splice(0, this.length);
};

String.prototype.startsWith = function(substr){
	var index = this.indexOf(substr);
	
	return (index == 0) ? true : false;
};

String.prototype.endsWith = function(substr){
	var index = this.indexOf(substr);
	var end = -2;
	
	if(index >= 0)
	{
		end = index + substr.length;
	}
	
	return (end >= (this.length - 1)) ? true : false;
};

function storeLocalValue(key, data)
{
	var jsonStr = JSON.stringify(data);
	localStorage.setItem(key, jsonStr);
}

function fetchLocalValue(key)
{
	var value = localStorage.getItem(key);
	
	if(!value || value == "undefined")
	{
		return null;
	}
	
	return JSON.parse(value);
}

function isDefined(value)
{
	return (value != null && value != undefined);
}

function parseServerDate(str){
	if(!str)
	{
		return null;
	}
	
	SERVER_DATE_FORMAT.lastIndex = 0;
	var match = SERVER_DATE_FORMAT.exec(str);
	
	if(!match)
	{
		return null;
	}
	
	var year = parseInt(match[1]);
	var month = parseInt(match[2]) - 1; //javascript date's month starts from zero
	var day = parseInt(match[3]);
	
	return new Date(year, month, day);
}




$.configuration = {
	modelUrl: null,
	templatesUrls: null,
	lovUrl: null
};

$.setConfiguration = function(config){
	$.extend($.configuration, config);
};

$.getConfiguration = function(name){
	return $.configuration[name];
};

function makeJsonCall(url, data, config)
{
	if(url.indexOf($.contextPath) != 0)
	{
		url = $.contextPath + url;
	}
	
	var dataTypeVal = (config && config.dataType)? config.dataType: "json";
	var contentType = (config && isDefined(config.contentType))? config.contentType : 'application/x-www-form-urlencoded; charset=UTF-8';
	
	if(dataTypeVal == "void")
	{
		dataTypeVal = null;
	}
	
	var isAsync = (config && config.async)? true: false;
	var isCache = (config && config.cache)? config.cache: false;
	var methodType = (config && config.methodType)? config.methodType: "POST";
	var processData = (config && isDefined(config.processData))? config.processData : undefined;
	
	
	$(document).data("makeJsonCall.data", null);
	
	$.ajax({
		  type: methodType,
		  dataType: dataTypeVal,
		  url: url,
		  data: data,
		  async: isAsync,
		  cache: isCache,
		  "contentType": contentType,
		  "processData": processData,
		  
		  success: function(resData, textStatus, jqXHR){
			  $(document).data("makeJsonCall.data", {status: jqXHR.statusCode().status, data: resData});
		  },
		  error: function(jqXHR, textStatus, errorThrown){
			  var resData = null;
			  
			  try
			  {
				  resData = $.parseJSON(jqXHR.responseText);
			  }catch(ex)
			  {
				  console.error("Failed to parsed response text.");
				  console.error(ex);
				  console.error("Response text: " + jqXHR.responseText);
			  }
			  
			  $(document).data("makeJsonCall.data", {status: jqXHR.statusCode().status, data: resData, error: errorThrown});
		  }
	});
	
	var result = $(document).data("makeJsonCall.data");
	
	if(result != null)
	{
		if(result.status >= 200 && result.status <= 300)
		{
			return result.data;
		}
		else
		{
			if(result.status == 401)
			{
				location.reload();
			}
			
			if(result.data)
			{
				throw result.data;
			}
			else
			{
				throw result.error;
			}
		}
	}
	else
	{
		//for async call result will not be set
		if(isAsync)
		{
			return {};
		}
		
		throw {code: 0, message: "Failed to communicate with server"};
	}
}

function makeJsonBodyCall(url, data)
{
	return makeJsonCall(url, JSON.stringify(data), {contentType: CONTENT_TYPE_JSON});
}

function makeCachedJsonCall(cacheName, url, data, config)
{
	/*
	var resObject = fetchLocalValue(cacheName);
	
	//TODO: Check if this value is out of data with server
	if(resObject != null)
	{
		return resObject;
	}
	
	resObject = makeJsonCall(url, data, config);
	
	if(!isDefined(resObject))
	{
		storeLocalValue(cacheName, resObject);
	}
	
	return resObject;
	*/
	
	return makeJsonCall(url, data, config);
}

$.error = function(mssg, title){
	$.alert(mssg, "Error");
};

$.alert = function(mssg, title){
	//alert(mssg);
	var alertBox = $("#__alertBox");
	
	if(!alertBox || alertBox.length == 0)
	{
		$('body').append('<div id="__alertBox"></div>');
		alertBox = $("#__alertBox");
		
		$.templateEngine.processTemplate("alertBox", "#__alertBox");
		$.parseDirectives("#__alertBox");
		
		$("#__alertBox").dialog({
			width: 450,
			height: 220,
			title: "Alert",
			autoOpen: false,
			modal: true,
			resizeable: false
		});
		
		$("#__alertBox input[type='button']").on("click", function(){
			$("#__alertBox").dialog("close");
		});
	}
	
	if(!title)
	{
		title = "Alert";
	}

	$("#__alertBox .alertContent").html(mssg);
	$("#__alertBox").dialog({"title": title});
	$("#__alertBox").dialog("open");
};

$.confirmBox = function(mssg, config){
	
	if(!config)
	{
		config = {};
	}
	
	var yesText = config["yesText"];
	var noText = config["noText"];
	var onYes = config["onYes"];
	var onNo = config["onNo"];
	var title = config["title"];
	var yesClass = config["yesClass"];
	var noClass = config["noClass"];
	var configuration = config["configuration"];
	
	yesText = (yesText) ? yesText : "Yes";
	noText = (noText) ? noText : "No";
	title = (title) ? title : "Confirm";
	yesClass = (yesClass) ? yesClass : "confirm_yes_button";
	noClass = (noClass) ? noClass : "confirm_no_button";
	
	var confirmBox = $("#__confirmBox");
	
	if(!confirmBox || confirmBox.length == 0)
	{
		$('body').append('<div id="__confirmBox"></div>');
		alertBox = $("#__confirmBox");
		
		$.templateEngine.processTemplate("confirmBox", "#__confirmBox");
		$.parseDirectives("#__confirmBox");
		
		$("#__confirmBox").dialog({
			width: 10,
			height: 10,
			title: "Confirm",
			autoOpen: false,
			modal: true,
			resizable: false,
			beforeClose: function(event, ui) {
				//if close is because of no button
				if($(this).data("__byButton") == "true")
				{
					return;
				}
				
				//prevent default and trigger no button click
				event.preventDefault();
				$(this).find(".noButton").trigger("click");
			}
		});
	}
	
	$("#__confirmBox .confirmContent").html(mssg);
	$("#__confirmBox .yesButton").val(yesText);
	$("#__confirmBox .noButton").val(noText);
	$("#__confirmBox").dialog({"title": title});
	$("#__confirmBox").data("__byButton", "false");
	
	if(yesClass.length > 0)
	{
		$("#__confirmBox .yesButton").addClass(yesClass);
	}
	
	if(noClass.length > 0)
	{
		$("#__confirmBox .noButton").addClass(noClass);	
	}
		
	//add yes listener
	$("#__confirmBox .yesButton").off("click").on("click", {"onYes": onYes, "yesClass": yesClass, "configuration": configuration}, function(event){
		var data = event.data;
		
		if(data.onYes)
		{
			try
			{
				data.onYes(data.configuration);
			}catch(ex)
			{
				console.error("Confirm Yes callback Error: " + ex);
			}
		}
		
		if(data.yesClass.length > 0)
		{
			$("#__confirmBox .yesButton").removeClass(data.yesClass);
		}
		
		$("#__confirmBox").data("__byButton", "true");
		$("#__confirmBox").dialog("close");
	});
	
	$("#__confirmBox .noButton").off("click").on("click", {"onNo": onNo, "noClass": noClass, "configuration": configuration}, function(event){
		var data = event.data;
		
		if(data.onNo)
		{
			try
			{
				data.onNo(data.configuration);
			}catch(ex)
			{
				console.error("Confirm No callback Error: " + ex);
			}
		}
		
		if(data.noClass.length > 0)
		{
			$("#__confirmBox .noButton").removeClass(data.noClass);
		}
		
		$("#__confirmBox").data("__byButton", "true");
		$("#__confirmBox").dialog("close");
	});

	$("#__confirmBox").dialog({
		"width": $(document).width(),
		"height": $(document).height()
	});
	
	$("#__confirmBox").dialog("open");


	var width = $("#__confirmBox .confirmContent").width() + 50; //50 for padding
	var height = $("#__confirmBox .confirmContent").height() //content height
				+ $("#__confirmBox .yesButton").height()  // buttons height
				+ 20 //for gap between message and buttons
				+ $("#__confirmBox").parent().find(".ui-dialog-titlebar").height()
				+ 50; //dialog padding
	
	$("#__confirmBox").dialog({
		"width": width,
		"height": height
	});
};

$.confirm = function(mssg){
	return confirm(mssg);
};

/*
 * *****************************************************************
 * controllers
 * *********************************************************************
 */
//this method on controllers will be called when document is loaded
var CONTROLLER_ON_READY_METHOD_NAME = "_onReady";

//this method on controllers will be called when document is loaded and all directives are processed
var CONTROLLER_ON_FW_READY_METHOD_NAME = "_onFwReady";

//this method will be called when content using this controller is loaded
var CONTROLLER_CONTENT_LOADED_METHOD_NAME = "_onPageLoaded";

//this attribute specifies parent controller
var CONTROLLER_EXTENDS = "extends";

$.controllers = {};

$.addController = function(name, controller){
	
	if($.controllers[name])
	{
		throw "Controller with specified name already exists: " + name;
	}
	
	$.controllers[name] = controller;
	controller["_actualFunctions"] = {};
	
	for(var attr in controller)
	{
		if($.isFunction(controller[attr]))
		{
			controller["_actualFunctions"][attr] = controller[attr]; 
			controller[attr] = $.proxy(controller[attr], controller);
		}
	}

	controller["name"] = name;
	
	var extendController = function(controller, parentController){
		if(parentController["_parent"])
		{
			extendController(controller, parentController["_parent"]);
		}
		
		for(var attr in parentController)
		{
			if(controller[attr])
			{
				continue;
			}
			
			if($.isFunction(parentController[attr]))
			{
				var proxyFunc = $.proxy(parentController["_actualFunctions"][attr], controller);
				controller[attr] = proxyFunc;
			}
			else
			{
				controller[attr] = parentController[attr];
			}
		}
		
	};
	
	if(controller[CONTROLLER_EXTENDS])
	{
		var parentController = $.controllers[controller[CONTROLLER_EXTENDS]];
		
		if(!parentController)
		{
			throw "Invalid parent-controller '" + controller[CONTROLLER_EXTENDS] + "' is specified in controller: '" + name + "'";
		}

		extendController(controller, parentController);
		controller["_parent"] = parentController;
	}
};

$.getControllerByName = function(name) {
	return $.controllers[name]; 
};

$(document).ready(function(){
	var method = null;
	
	for(var name in $.controllers)
	{
		method = $.controllers[name][CONTROLLER_ON_READY_METHOD_NAME];
		
		if(method && $.isFunction(method))
		{
			try
			{
				method();
			}catch(ex)
			{
				console.error("An error occurred while invoking on ready function on controller: " + name);
				console.error(ex);
			}
		}
	}
});

/*
 * *****************************************************************
 * Define list of possible DataTypes
 * *********************************************************************
 */
$["__dataTypes"] = new Object();
$["__dataTypes"]["int"] = function(fieldDetails, field, value){
	if(!value || value.length <= 0)
	{
		return 0;
	}
	
	if(!/^\d+$/.test(value))
	{
		throw {"field": field,
				"message": "Invalid integer (" + value + ") specified.",
				"field-message": "Invalid integer",
				"value": value,
				"fieldLabel": fieldDetails.label
		};
	}
	
	return parseInt(value);
};
$["__dataTypes"]["float"] = function(fieldDetails, field, value){
	
	if(!value || value.length <= 0)
	{
		return 0;
	}
	
	if(!/^\d+\.\d+$/.test(value) && !/^\d+$/.test(value))
	{
		throw {"field": field,
			"message": "Invalid decimal-value (" + value + ") specified. Decimal value should of format 0.0",
			"field-message": "Invalid Decimal value",
			"value": value,
			"fieldLabel": fieldDetails.label
		};
	}
	
	return parseFloat(value);
};
$["__dataTypes"]["string"] = function(fieldDetails, field, value){
	return value;
};
$["__dataTypes"]["boolean"] = function(fieldDetails, field, value){
	value = "" + value;
	
	if("true" == value.toLowerCase())
	{
		return true;
	}
	
	return false;
};
$["__dataTypes"]["multiLine"] = function(fieldDetails, field, value){
	return value;
};
$["__dataTypes"]["date"] = function(fieldDetails, field, value){
	if(!value)
	{
		return null;
	}
	
	if(!(value instanceof Date))
	{
		throw "Non-date value specified for date-field: " + field;
	}
	
	return value;
};
$["__dataTypes"]["date"]["toPlainValue"] = function(value) {
	
	if(!value)
	{
		return null;
	}
	
	return STD_DATE_FORMAT.format(value);
};

$["__dataTypes"]["lov"] = function(fieldDetails, field, value){
	
	if(!value || value.length == 0)
	{
		return null;
	}
	
	if( ((typeof value) != "string") )
	{
		throw {"field": field,
			"message": "Invalid LOV-value type (" + (typeof value) + ") specified. Only strings are allowed for LOV.",
			"field-message": "Invalid LOV value",
			"value": value,
			"fieldLabel": fieldDetails.label
		};
	}
	
	for(var i = 0; i < fieldDetails.values.length; i++)
	{
		if(fieldDetails.values[i].value == value)
		{
			return value;
		}
	}
	
	throw {"field": field,
		"message": "Invalid LOV-value (" + value + ") specified. Specified value is not present in target LOV list.",
		"field-message": "Unknown LOV value",
		"value": value,
		"fieldLabel": fieldDetails.label
	};
};


/*
 * *****************************************************************
 * Define list of possible validators
 * *********************************************************************
 */

$["__validators"] = new Object();
$["__validators"]["required"] = function(validatorConfig, field, fieldDetails, value)
{
	if(!isDefined(value) || ("" + value).length == 0)
	{
		throw {"field": field,
			"message": "No/invalid value specified for mandatory field - " + fieldDetails.label,
			"field-message": "Value is mandatory",
			"value": value,
			"fieldLabel": fieldDetails.label
		};
	}
};
$["__validators"]["required"]["init"] = function(validatorConfig, fieldDetails){
	fieldDetails["__isMandatory"] = true;
};
$["__validators"]["required"]["global"] = true;


$["__validators"]["pattern"] = function(validatorConfig, field, fieldDetails, value)
{
	if(!value || ("" + value).length == 0)
	{
		return;
	}
	
	var pattern = new RegExp(validatorConfig.values["regexp"]);
	
	if(!pattern.test(value))
	{
		var message = validatorConfig.errorMessage;
		
		if(!message)
		{
			message = "Invalid value specified for field '" + field + "'. Not matching required pattern.";
		}
		
		throw {"field": field,
			"message": message,
			"field-message": "Not matching required pattern",
			"value": value,
			"fieldLabel": fieldDetails.label
		};
	}
};
$["__validators"]["pattern"]["init"] = function(validatorConfig, fieldDetails){
	var patternStr = validatorConfig.values["regexp"];
	
	if(!patternStr.startsWith("^"))
	{
		patternStr = "^" + patternStr;
	}
	
	if(!patternStr.endsWith("$"))
	{
		patternStr = patternStr + "$";
	}
	
	validatorConfig.values["regexp"] = patternStr;
};


$["__validators"]["mispattern"] = function(validatorConfig, fiel, fieldDetails, value)
{
	if(!value || ("" + value).length == 0)
	{
		return;
	}
	
	var pattern = new RegExp(validatorConfig.values["regexp"]);
	
	if(pattern.test(value))
	{
		throw {"field": field,
			"message": "Invalid value specified for field '" + field + "'. Mathing with negative pattern.",
			"field-message": "Values of this pattern are not allowed",
			"value": value,
			"fieldLabel": fieldDetails.label
		};
	}
};
$["__validators"]["minLength"] = function(validatorConfig, field, fieldDetails, value)
{
	if(!value || !value.length)
	{
		return;
	}
	
	var minLen = validatorConfig.values["value"];
	
	if(value.length < minLen)
	{
		throw {"field": field,
			"message": "Invalid value specified field '" + field + "'. Value should be minimum of length: " + minLen,
			"field-message": "Value should be minimum of length: " + minLen,
			"value": value,
			"fieldLabel": fieldDetails.label
		};
	}
};
$["__validators"]["maxLength"] = function(validatorConfig, field, fieldDetails, value)
{
	if(!value || !value.length)
	{
		return;
	}
	
	var maxLen = validatorConfig.values["value"];
	
	if(value.length > maxLen)
	{
		throw {"field": field,
			"message": "Invalid value specified for field '" + field + "'. Value can be maximum of length: " + maxLen,
			"field-message": "Value should be maximum of length: " + maxLen,
			"value": value,
			"fieldLabel": fieldDetails.label
		};
	}
};
$["__validators"]["mandatoryOption"] = function(validatorConfig, field, fieldDetails, value, bean)
{
	if(value != null && value != undefined && (((typeof value) != "string") || value.length > 0))
	{
		return;
	}

	var fields = validatorConfig.values["fields"];
	var otherVal = null, otherFld = null;
	var fieldsStr = field;
	
	for(var i = 0; i < fields.length; i++)
	{
		otherFld = fields[i];
		otherVal = bean["getters"][otherFld]();
		
		if(otherVal != null && otherVal != undefined && (((typeof otherVal) != "string") || otherVal.length > 0))
		{
			return;
		}
		
		fieldsStr += ", " + otherFld;
	}
	
	throw {"field": field,
		"message": "One of the following field is mandatory: " + fieldsStr,
		"field-message": "One of the following field is mandatory: " + fieldsStr,
		"fieldLabel": fieldDetails.label
	};
};
$["__validators"]["mandatoryOption"]["global"] = true;

$["__validators"]["pastOrToday"] = function(validatorConfig, field, fieldDetails, value, bean)
{
	if(!value || (!(value instanceof Date)))
	{
		return;
	}

	var today = new Date();
	
	if(today.getTime() >= value.getTime())
	{
		return;
	}
	
	throw {"field": field,
		"message": "Date should be a past date or today's date",
		"field-message": "Date should be a past date or today's date",
		"fieldLabel": fieldDetails.label
	};
};
$["__validators"]["futureOrToday"] = function(validatorConfig, field, fieldDetails, value, bean)
{
	if(!value || (!(value instanceof Date)))
	{
		return;
	}

	var today = new Date();
	
	if(today.getTime() <= value.getTime())
	{
		return;
	}
	
	throw {"field": field,
		"message": "Date should be a future date or today's date",
		"field-message": "Date should be a future date or today's date",
		"fieldLabel": fieldDetails.label
	};
};
$["__validators"]["thisYearOnly"] = function(validatorConfig, field, fieldDetails, value, bean)
{
	if(!value || (!(value instanceof Date)))
	{
		return;
	}
	
	var today = new Date();
	
	if(today.getFullYear() == value.getFullYear())
	{
		return;
	}
	
	throw {"field": field,
		"message": "Date value should be of current year only",
		"field-message": "Date value should be a current year only",
		"fieldLabel": fieldDetails.label
	};
};
$["__validators"]["greaterThanDateField"] = function(validatorConfig, field, fieldDetails, value, bean)
{
	if(!value || (!(value instanceof Date)))
	{
		return;
	}

	var otherFld =  validatorConfig.values["field"];
	
	if(!bean["getters"][otherFld])
	{
		throw "Invalid field name '" + otherFld + "' specified for 'greaterThanDateField' configuration of field: " + field;
	}
	
	otherVal = bean["getters"][otherFld]();
	
	if(!otherVal || (!(value instanceof Date)))
	{
		return;
	}
	
	if(otherVal.getTime() < value.getTime())
	{
		return;
	}
	
	var otherFieldDetails = bean["__fieldsByName"][otherFld];
	
	throw {"field": field,
		"message": "'" + fieldDetails.label + "' value should be greater than '" + otherFieldDetails.label + "'",
		"field-message": "'" + fieldDetails.label + "' value should be greater than '" + otherFieldDetails.label + "'",
		"fieldLabel": fieldDetails.label
	};
};

$["__validators"]["minValue"] = function(validatorConfig, field, fieldDetails, value)
{
	if(!value || ((typeof value) != "number") )
	{
		return;
	}
	
	var minVal = validatorConfig.values["value"];
	
	if(value < minVal)
	{
		throw {"field": field,
			"message": "Invalid value specified field '" + field + "'. Value should be greater than or equal to: " + minVal,
			"field-message": "Value should be greater than or equal to: " + minVal,
			"value": value,
			"fieldLabel": fieldDetails.label
		};
	}
};
$["__validators"]["maxValue"] = function(validatorConfig, field, fieldDetails, value)
{
	if(!value || ((typeof value) != "number") )
	{
		return;
	}
	
	var maxVal = validatorConfig.values["value"];
	
	if(value > maxVal)
	{
		throw {"field": field,
			"message": "Invalid value specified field '" + field + "'. Value should be less than or equal to: " + maxVal,
			"field-message": "Value should be less than or equal to: " + maxVal,
			"value": value,
			"fieldLabel": fieldDetails.label
		};
	}
};
$["__validators"]["lessThan"] = function(validatorConfig, field, fieldDetails, value, bean)
{
	if(!value || ((typeof value) != "number") )
	{
		return;
	}
	
	var otherFld =  validatorConfig.values["field"];
	
	if(!bean["getters"][otherFld])
	{
		throw "Invalid field name '" + otherFld + "' specified for 'lessThan' configuration of field: " + field;
	}
	
	otherVal = bean["getters"][otherFld]();
	
	if(value >= otherVal)
	{
		throw {"field": field,
			"message": "Invalid value specified field '" + field + "'. Value should be less than: " + otherFld,
			"field-message": "Value should be less than: " + otherFld,
			"value": value,
			"fieldLabel": fieldDetails.label
		};
	}
};
$["__validators"]["lessThanEquals"] = function(validatorConfig, field, fieldDetails, value, bean)
{
	if(!value || ((typeof value) != "number") )
	{
		return;
	}
	
	var otherFld =  validatorConfig.values["field"];
	
	if(!bean["getters"][otherFld])
	{
		throw "Invalid field name '" + otherFld + "' specified for 'lessThanEquals' configuration of field: " + field;
	}
	
	otherVal = bean["getters"][otherFld]();
	
	if(value > otherVal)
	{
		throw {"field": field,
			"message": "Invalid value specified field '" + field + "'. Value should be less than or equal to: " + otherFld,
			"field-message": "Value should be less than or equal to: " + otherFld,
			"value": value,
			"fieldLabel": fieldDetails.label
		};
	}
};
$["__validators"]["greaterThan"] = function(validatorConfig, field, fieldDetails, value, bean)
{
	if(!value || ((typeof value) != "number") )
	{
		return;
	}
	
	var otherFld =  validatorConfig.values["field"];
	
	if(!bean["getters"][otherFld])
	{
		throw "Invalid field name '" + otherFld + "' specified for 'greaterThan' configuration of field: " + field;
	}
	
	otherVal = bean["getters"][otherFld]();
	
	if(value <= otherVal)
	{
		throw {"field": field,
			"message": "Invalid value specified field '" + field + "'. Value should be greater than: " + otherFld,
			"field-message": "Value should be greater than: " + otherFld,
			"value": value,
			"fieldLabel": fieldDetails.label
		};
	}
};
$["__validators"]["greaterThanEquals"] = function(validatorConfig, field, fieldDetails, value, bean)
{
	if(!value || ((typeof value) != "number") )
	{
		return;
	}
	
	var otherFld =  validatorConfig.values["field"];
	
	if(!bean["getters"][otherFld])
	{
		throw "Invalid field name '" + otherFld + "' specified for 'greaterThanEquals' configuration of field: " + field;
	}
	
	otherVal = bean["getters"][otherFld]();
	
	if(value < otherVal)
	{
		throw {"field": field,
			"message": "Invalid value specified field '" + field + "'. Value should be greater than or equal to: " + otherFld,
			"field-message": "Value should be greater than or equal to: " + otherFld,
			"value": value,
			"fieldLabel": fieldDetails.label
		};
	}
};

/*
 * *****************************************************************
 * Code to define custom data-types
 * *********************************************************************
 */
$["__objectTypes"] = new Object();


/*
 * List of field-details properties
		name - Name of the field
		label - Label of the field (can be null)
		description - Description
		defaultValue - Default Value
		fieldType - Type of field (STRING, MULTI_LINE_STRING, INTEGER, FLOAT, BOOLEAN, DATE, COMPLEX, LIST_OF_VALUES)
		type - Name of type of field (string, multiLine, int, float, boolean, date, complex, lov)
		
		multiValued - true/false. Indicates if this field is multi valued.
		required - true/false. Specifies whether this field is mandatory.
		searchable - true/false. Specified whether this field can be part of search query.
		displayable - true/false. Indicates whether this field should be displayed in query result.
		identifiable - true/false. Indicates whether this field should be part of the model identification
		extendedField - true/false. Indicates whether this field is extended field or normal field.
		
		clientConfiguration - special configuration for clients
			lovType - For lov fields only. Indicates lov type - [ENUM_TYPE, QUERY_TYPE, CUSTOM_TYPE]
			queryName - Query name for lov field of type QUERY_TYPE
			parentField - Parent field for dependent lov field.
			
		validatorConfigurations - Validations for fields
			name - Name of the validator
			validatorType - Type of validator
			global - true/false. If true, this validation should not be performed at field value change. Instead it should be performed at the end as part of validate() method.
			errorMessage - Message to be displayed in case of validation failure
			
		values - List of values for LOV field.
 * 	
 */
$["__objectBase"] = function(){
	this["__className"] = "";
	
	this["__fieldData"] = null;
	this["__indexes"] = null;
	this["__propertyChangeListeners"] = null;
	this["__fieldsByName"] = {};
	
	this["__initialize"] = function(){
		this["__fieldData"] = new Object();
		this["__indexes"] = new Object();
		this["__propertyChangeListeners"] = new Object();
	};
	
	this["toPlainObject"] = function(){
		var res = new Object();
		var fieldData = this["__fieldData"];
		var fieldDetailsLst = this.fields;
		var attr = null, fieldDetails = null;
		var fieldValue = null;
		
		for(var i = 0; i < fieldDetailsLst.length; i++)
		{
			fieldDetails = fieldDetailsLst[i];
			attr = fieldDetails.name;
			
			if(!isDefined(fieldData[attr]))
			{
				continue;
			}
			
			if(fieldDetails.multiValued)
			{
				if(fieldData[attr].length == 0)
				{
					continue;
				}
				
				fieldValue = new Array();
				
				for(var j = 0; j < fieldData[attr].length; j++)
				{
					if(!fieldData[attr][j])
					{
						fieldValue[j] = null;
						continue;
					}
					
					if(fieldDetails.isComplex)
					{
						fieldValue[j] = fieldData[attr][j].toPlainObject();
					}
					else
					{
						if($["__dataTypes"][fieldDetails.type]["toPlainValue"])
						{
							fieldValue[j] = $["__dataTypes"][fieldDetails.type]["toPlainValue"](fieldData[attr][j]);					
						}
						else
						{
							fieldValue[j] = fieldData[attr][j];
						}
					}
				}
			}
			else if(fieldDetails.isComplex)
			{
				fieldValue = fieldData[attr].toPlainObject();
			}
			else
			{
				if($["__dataTypes"][fieldDetails.type]["toPlainValue"])
				{
					fieldValue = $["__dataTypes"][fieldDetails.type]["toPlainValue"](fieldData[attr]);					
				}
				else
				{
					fieldValue = fieldData[attr];
				}
			}
			
			if(fieldDetails.extendedField)
			{
				if(!res["extendedProperties"])
				{
					res["extendedProperties"] = [];
				}
				
				res["extendedProperties"].push({"name": attr, "jsonValue": JSON.stringify(fieldValue)});
			}
			else
			{
				res[attr] = fieldValue;
			}
		}
		
		return res;
	};
	
	this["toJson"] = function(){
		return JSON.stringify(this.toPlainObject());
	};
	
	this["__invokeChangeListeners"] = function(prop, eventType){
		eventType = eventType? eventType : EVENT_TYPE_VALUE_CHANGED;
		
		//var len = this["__propertyChangeListeners"].length;
		var event = {
					"model": this,
					"field": prop, 
					"newValue": this["__fieldData"][prop],
					"eventType": eventType
		};
		
		var eventData = null;
		var listener = null;
		
		for(var listenerName in this["__propertyChangeListeners"])
		{
			listener = this["__propertyChangeListeners"][listenerName];
			
			try
			{
				eventData = listener["data"];
				
				if(!eventData)
				{
					eventData = null;
				}
				
				event.data = eventData;
				
				listener(event);
			}catch(err)
			{
				console.error("An error occurred while invoking property-change-listener: " + err);
			}
		}
	};
	
	this["__validateField"] = function(fieldDetails, field, value, isGlobalValidation) {
		if(fieldDetails.validatorConfigurations)
		{
			var validations = fieldDetails.validatorConfigurations;
			var validation = null;
			
			for(var i = 0; i < validations.length; i++)
			{
				validation = validations[i];
				
				if(!$["__validators"][validation.name])
				{
					continue;
				}
				
				if(!isGlobalValidation)
				{
					if($["__validators"][validation.name]["global"])
					{
						continue;
					}
				}
				
				$["__validators"][validation.name](validation, field, fieldDetails, value, this);
			}
		}
		
		if(value && fieldDetails.isComplex)
		{
			try
			{
				if(fieldDetails.multiValued)
				{
					for(var i = 0; i < value.length; i++)
					{
						value[i].validate();
					}
				}
				else
				{
					value.validate();
				}
			}catch(err)
			{
				throw "Invalid value specified for field: " + fieldDetails.label + "<BR/>Root Cause: " + err;
			}
		}
	};
	
	this["__addIndexes"] = function(fieldDetails, field, value) {

		if(!value || !fieldDetails.isComplex || !fieldDetails.indexedBy)
		{
			return;
		}
		
		var indexes = this["__indexes"][field];
		
		if(!indexes)
		{
			indexes = new Object();
			this["__indexes"][field] = indexes;
		}
		
		var indexedFields = fieldDetails.indexedBy;
		var fldIndex = null, valIndexArr = null, fldValue = null;
		
		for(var i = 0; i < indexedFields.length; i++)
		{
			fldValue = value[indexedFields[i]];
			
			if(!fldValue)
			{
				continue;
			}
			
			fldIndex = indexes[indexedFields[i]];
			
			if(!fldIndex)
			{
				fldIndex = new Object();
				indexes[indexedFields[i]] = fldIndex;
			}
			
			valIndexArr = fldIndex[fldValue];
			
			if(!valIndexArr)
			{
				valIndexArr = new Array();
			}
			
			valIndexArr.push(value);
		}
	};
	
	this["__postValueChange"] = function(fieldDetails) {
		if(fieldDetails.lovChildren)
		{
			var childField = null;
			var parentValue = this["__fieldData"][fieldDetails.name];
			
			for(var i = 0; i < fieldDetails.lovChildren.length; i++)
			{
				childField = this["__fieldsByName"][fieldDetails.lovChildren[i]];
				
				if(!parentValue)
				{
					childField.values = new Array();
				}
				else
				{
					childField.values = makeJsonCall($.configuration.lovUrl, {
																	name: childField.clientConfiguration.queryName, 
																	lovType: childField.clientConfiguration.lovType,
																	parentId: parentValue});
				}
				
				this["__invokeChangeListeners"](childField.name, EVENT_TYPE_LOV_LIST_CHANGED);
				//this["__setField"](childField, null);
				this["__fieldData"][childField.name] = null;
				this["__invokeChangeListeners"](childField.name);
			}
		}
	};
	
	this["__setField"] = function(fieldDetails, value) {
		var field = fieldDetails.name;
		
		if(!fieldDetails)
		{
			throw "Invalid field name specified with setter: " + field;
		}
		
		var parsedValue = value;
		
		if(!fieldDetails.isComplex)
		{
			parsedValue = $["__dataTypes"][fieldDetails.type](fieldDetails, field, value);
		}
		
		this["__validateField"](fieldDetails, field, parsedValue);
		
		this["__fieldData"][field] = parsedValue;
		this["__invokeChangeListeners"](field);
		
		this["__postValueChange"](fieldDetails);
	};
	
	this["__addField"] = function(fieldDetails, value) {
		var field = fieldDetails.name;
		
		var parsedValue = value;
		
		if(!fieldDetails.isComplex)
		{
			parsedValue = $["__dataTypes"][fieldDetails.type](fieldDetails, field, value);
		}
		
		this["__validateField"](fieldDetails, field, parsedValue);
		
		if(!this["__fieldData"][field])
		{
			this["__fieldData"][field] = new Array();
		}
		
		this["__fieldData"][field].push(parsedValue);
		this["__addIndexes"](fieldDetails, field, parsedValue);
		this["__invokeChangeListeners"](field);
	};

	
	this["__removeAll"] = function(fieldDetails) {
		var field = fieldDetails.name;
		
		this["__fieldData"][field] = new Array();
		this["__invokeChangeListeners"](field);
	};

	this["__getField"] = function(fieldDetails) {
		var field = fieldDetails.name;
		return this["__fieldData"][field];
	};
	
	this["validate"] = function() {
		var fldValue = null;
		var fields = this.fields;
		
		for(var i = 0; i < fields.length; i++)
		{
			fldValue = this["__fieldData"][fields[i].name];
			this["__validateField"](fields[i], fields[i].name, fldValue, true);
		}
	};

	this["__getAllByIndex"] = function(fieldDetails, index, val){
		var field = fieldDetails.name;
		var indexes = this["__indexes"][field];
		
		if(!indexes)
		{
			return null;
		}
		
		var fldIndex = indexes[index];
		
		if(!fldIndex)
		{
			return null;
		}
		
		var valIndexArr = fldIndex[val];
		
		if(!valIndexArr)
		{
			return null;
		}
		
		return valIndexArr;
	};

	this["__getByIndex"] = function(field, index, val){
		var indexed = this["__getAllByIndex"](field, index, val);
		
		if(!indexed || (indexed.length == 0))
		{
			return null;
		}
		
		return indexed[0];
	};
	
	this["getAllFieldNames"] = function() {
		var fields = [];
		
		for(var attr in this["__fieldsByName"])
		{
			fields.push(attr);
		}
		
		return fields;
	};
	
	this["getFieldDetails"] = function(name) {
		return this["__fieldsByName"][name];
	};
	
	this["isValidField"] = function(name) {
		if(this["__fieldsByName"][name])
		{
			return true;
		}
		
		return false;
	};
	
	this["isReadOnlyField"] = function(name) {
		if(this["__fieldsByName"][name])
		{
			return false;
		}
		
		return (this["__fieldsByName"].readOnly == true) ? true : false;
	};
	
	this["getFieldType"] = function(name) {
		if(this["__fieldsByName"][name])
		{
			return this["__fieldsByName"][name].type;
		}
		
		return null;
	};

	/*
	this["setFieldType"] = function(name, type) {
		if(this["__fieldsByName"][name])
		{
			if(!$["__dataTypes"][type])
			{
				throw "Invalid data-type specified: " + type;
			}
			
			this["__fieldsByName"][name].type = type;
		}
	};
	*/
	
	this["getLovValues"] = function(name) {
		var field = this["__fieldsByName"][name];
		
		if(!field || !field.clientConfiguration.lovType)
		{
			return null;
		}
		
		//TODO: later remove this when caching is implemented on  makeCachedJsonCall
			// so that caching is done there not here
		if(field.clientConfiguration.lovType == "ENUM_TYPE")
		{
			return field.values;
		}
		
		//for parent field, the lov values will be fetched during parent property change
			//so simple return cached values
		if(field.clientConfiguration.parentField)
		{
			return field.values;
		}
		
		var lovType = field.clientConfiguration.lovType;
		var lovName = field.clientConfiguration.queryName;
		
		return makeCachedJsonCall("lov." + lovType + "." + lovName, $.configuration.lovUrl, {name: lovName, lovType: lovType}) 
	};
	
	this["addPropertyChangeListener"] = function(name, eventData, callback){
		
		if(!$.isFunction(callback))
		{
			throw "Invalid function specified for property-change-listener: " + callback;
		}
		
		this["__propertyChangeListeners"][name] = callback;
		
		if(eventData)
		{
			callback["data"] = eventData;
		}
	};

	this["removePropertyChangeListener"] = function(name){
		var listener = this["__propertyChangeListeners"][name];
		
		if(listener == null)
		{
			return false;
		}

		delete this["__propertyChangeListeners"][name];
		return true;
	};
	
	this["getClassName"] = function(){
		return this["__className"];
	};
	
	this["copyTo"] = function(newObj, deepCopy, ignoreListeners){
		var len = 0;
		
		for(var attr in this["__fieldData"])
		{
			if(!isDefined(this["__fieldData"][attr]))
			{
				if(isDefined(newObj["__fieldData"][attr]))
				{
					delete newObj["__fieldData"][attr];
					
					if(!ignoreListeners)
					{
						newObj["__invokeChangeListeners"](attr);
					}
				}
				
				continue;
			}
			
			if(deepCopy)
			{
				if(this["__fieldsByName"][attr].multiValued)
				{
					newObj["__fieldData"][attr] = new Array();
					len = this["__fieldData"][attr].length;
					
					for(var i = 0; i < len; i++)
					{
						if(this["__fieldsByName"][attr].isComplex)
						{
							newObj["__fieldData"][attr].push(this["__fieldsByName"][attr][i].clone(true));
						}
						else
						{
							newObj["__fieldData"][attr].push(this["__fieldsByName"][attr][i]);
						}
					}
				}
				else
				{
					if(this["__fieldsByName"][attr].isComplex)
					{
						newObj["__fieldData"][attr] = this["__fieldData"][attr].clone(true);
					}
					else
					{
						newObj["__fieldData"][attr] = this["__fieldData"][attr];
					}
				}
			}
			else
			{
				newObj["__fieldData"][attr] = this["__fieldData"][attr];
			}

			if(!ignoreListeners)
			{
				newObj["__invokeChangeListeners"](attr);
			}
		}
	},

	this["copyFromPlainObject"] = function(plainObject, ignoreListeners){
		var len = 0;
		
		for(var attr in this["__fieldsByName"])
		{
			if(!isDefined(plainObject[attr]))
			{
				if(isDefined(this["__fieldData"][attr]))
				{
					delete this["__fieldData"][attr];
					
					if(!ignoreListeners)
					{
						this["__invokeChangeListeners"](attr);
					}
				}
				
				continue;
			}
			
			if(this["__fieldsByName"][attr].multiValued)
			{
				this["__fieldData"][attr] = new Array();
				len = plainObject[attr].length;
				
				for(var i = 0; i < len; i++)
				{
					if(this["__fieldsByName"][attr].isComplex)
					{
						var newObj = $.create(this["__fieldsByName"][attr].type);
						newObj.copyFromPlainObject(plainObject[attr][i], ignoreListeners);
						
						this["__fieldData"][attr].push(newObj);
					}
					else
					{
						this["__fieldData"][attr].push(plainObject[attr][i]);
					}
				}
			}
			else
			{
				if(this["__fieldsByName"][attr].isComplex)
				{
					this["__fieldData"][attr] = $.create(this["__fieldsByName"][attr].type);
					this["__fieldData"][attr].copyFromPlainObject(plainObject[attr], ignoreListeners);
				}
				else if(this["__fieldsByName"][attr].type == 'date')
				{
					this["__fieldData"][attr] = parseServerDate(plainObject[attr]);
				}
				else
				{
					this["__fieldData"][attr] = plainObject[attr];
				}
			}

			if(!ignoreListeners)
			{
				this["__invokeChangeListeners"](attr);
			}
		}
	},

	this["clone"] = function(deepClone){
		var newObj = $.create(this["__className"]);
		this.copyTo(newObj, deepClone, true);
		return newObj;
	};

	this["reset"] = function(){
		var newObj = $.create(this["__className"]);
		var modifiedAttr = [];
		
		for(var attr in this["__fieldData"])
		{
			if(modifiedAttr.indexOf(attr) < 0)
			{
				modifiedAttr.push(attr);
			}
		}

		for(var attr in newObj["__fieldData"])
		{
			if(modifiedAttr.indexOf(attr) < 0)
			{
				modifiedAttr.push(attr);
			}
		}

		this["__fieldData"] = newObj["__fieldData"];

		for(var i = 0; i < modifiedAttr.length; i++)
		{
			this["__invokeChangeListeners"](modifiedAttr[i]);
		}
	};
};

function toInitUpperCase(str)
{
	var firstChar = "" + str.charAt(0);
	return (firstChar.toUpperCase() + str.substr(1));
}

$.reloadModelType = function(name){
	
	if(!$["__objectTypes"][name])
	{
		return;
	}
	
	var configuration = $["__objectTypes"][name]["__configuration"];
	$["__objectTypes"][name] = null;
	
	$.define(name, configuration, true);
};

$.define = function(name, modelObj, ignore){
	if($["__objectTypes"][name])
	{
		if(!ignore)
		{
			throw "A type with specified name is already defined: " + name;
		}
		
		return $["__objectTypes"][name];
	}
	
	var newModelType = new $["__objectBase"]();
	
	newModelType["__configuration"] = modelObj;
	newModelType["__className"] = name;
	newModelType["setters"] = new Object();
	newModelType["adders"] = new Object();
	newModelType["getters"] = new Object();
	newModelType["removers"] = new Object();
	newModelType.label = modelObj.label;
	
	if(modelObj.serverType)
	{
		if(!modelObj.typeCategory)
		{
			modelObj.typeCategory = "STATIC_TYPE";
		}
		
		var modelData = makeJsonCall($.configuration.modelUrl + modelObj.serverType, {modelType: modelObj.typeCategory});
		
		if(modelData.error)
		{
			$.error("An error occured while fetching model data-structure for type: " + modelObj.serverType + 
					"<BR/>Error: " + modelData.error);
			return null;
		}
		
		if(!newModelType.label)
		{
			newModelType.label = modelData.label;
		}
		
		newModelType["fields"] = modelData["fields"];
	}
	
	if(!newModelType.label)
	{
		newModelType.label = name;
	}
	
	if(modelObj.fields)
	{
		if(newModelType["fields"])
		{
			$.extend(newModelType["fields"], modelObj.fields);
		}
		else
		{
			newModelType["fields"] = modelObj["fields"];
		}
	}
	
	if(newModelType.fields)
	{
		var fldName = null;
		var fields = newModelType.fields;
		var indexedFields = null;
		var funcName = null;
		var requirePostProcess = new Array();
		
		for(var i = 0; i < fields.length; i++)
		{
			if(newModelType["__fieldsByName"][fields[i].name])
			{
				throw "Failed to create model of type - " + name + " Reason: Duplicate field name encountered: " + fields[i].name; 
 			}
			
			if(!fields[i].label)
			{
				fields[i].label = fields[i].name;
			}
			
			newModelType["__fieldsByName"][fields[i].name] = fields[i];
			
			if(!$["__dataTypes"][fields[i].type] && !$["__objectTypes"][fields[i].type])
			{
				var newType = null;
				
				if(fields[i].serverType)
				{
					newType = $.define(fields[i].type, {"serverType": fields[i].serverType}, true);
				}
				
				if(!newType)
				{
					throw "Invalid data-type (" + fields[i].type + ") specified for field: " + fields[i].name;
				}
			}
			
			if(fields[i].type == "lov" && !fields[i].values)
			{
				if(!fields[i].clientConfiguration || !fields[i].clientConfiguration.queryName)
				{
					throw "Neither values nor query-name is defined for LOV field: " + fields[i].name;
				}
				
				if(!fields[i].clientConfiguration.lovType)
				{
					fields[i].clientConfigration.lovType = "ENUM_TYPE";
				}
				
				if(!fields[i].clientConfiguration.parentField)
				{
					fields[i].values = makeJsonCall($.configuration.lovUrl, {name: fields[i].clientConfiguration.queryName, lovType: fields[i].clientConfiguration.lovType});
				}
				else
				{
					requirePostProcess.push(fields[i]);
				}
				
				if(!fields[i].values)
				{
					fields[i].values = new Array();
				}
			}
			
			if(!$["__dataTypes"][fields[i].type])
			{
				fields[i]["isComplex"] = true;
			}
			
			fldName = toInitUpperCase(fields[i].name);
			
			if(fields[i].multiValued)
			{
				var nonPluralName = fldName;
				
				if(fldName.lastIndexOf("s") == (fldName.length - 1))
				{
					nonPluralName = fldName.substr(0, fldName.length - 1);
				}
				
				//add adder method
				//-----------------------------
				newModelType["add" + nonPluralName] = function(val){
					var field = arguments.callee["field"];
					this["__addField"](field, val);
				};
				
				newModelType["add" + nonPluralName]["field"] = fields[i];
				
				newModelType["adders"][fields[i].name] = newModelType["add" + nonPluralName];

				//add setter method
				//-----------------------------
				newModelType["set" + fldName] = function(val){
					var field = arguments.callee["field"];
					
					if(!$.isArray(val))
					{
						throw "Non-array value specified for multi-valued property: " + field.name;
					}
					
					for(var i = 0; i < val.length; i++)
					{
						this["adders"][field.name](val[i]);
					}
				};
				
				newModelType["set" + fldName]["field"] = fields[i];
				newModelType["setters"][fields[i].name] = newModelType["set" + fldName];

				//Add remove all method
				//-----------------------------
				newModelType["removeAll" + fldName] = function(){
					var field = arguments.callee["field"];
					this["__removeAll"](field);
				};
				
				newModelType["removeAll" + fldName]["field"] = fields[i];
				
				newModelType["removers"][fields[i].name] = newModelType["removeAll" + fldName];
				
				//Add index methods
				//-----------------------------

				if(fields[i]["complex"] && fields[i]["indexedBy"])
				{
					indexedFields = fields[i]["indexedBy"];
					
					for(var j = 0; j < indexedFields.length; j++)
					{
						//check if the field exists in complex type
						if(!$["__objectTypes"][fields[i].type][indexedFields[i]])
						{
							throw "Invalid index field (" + indexedFields[i] + ") is specified for field: " + fldName;
						}

						//Add simple index based getter for getting single value
						funcName = "get" + nonPluralName + "By" + toInitUpperCase(indexedFields[i]);
						
						newModelType[funcName] = function(val){
							var field = this[arguments.callee["field"]];
							var index = this[arguments.callee["index"]];
							this["__getByIndex"](field, index, val);
						};
						
						newModelType[funcName]["field"] = fields[i];
						newModelType[funcName]["index"] = indexedFields[i];

						//Add simple index based getter for getting array
						funcName = "getAll" + nonPluralName + "By" + toInitUpperCase(indexedFields[i]);
						
						newModelType[funcName] = function(val){
							var field = this[arguments.callee["field"]];
							var index = this[arguments.callee["index"]];
							this["__getAllByIndex"](field, index, val);
						};
						
						newModelType[funcName]["field"] = fields[i];
						newModelType[funcName]["index"] = indexedFields[i];
					}
				}
			}
			else
			{
				newModelType["set" + fldName] = function(val){
					var field = arguments.callee["field"];
					this["__setField"](field, val);
				};
				
				newModelType["set" + fldName]["field"] = fields[i];
				newModelType["setters"][fields[i].name] = newModelType["set" + fldName];
			}

			newModelType["get" + fldName] = function(){
				var field = arguments.callee["field"];
				return this["__getField"](field);
			};
			
			newModelType["get" + fldName]["field"] = fields[i];
			newModelType["getters"][fields[i].name] = newModelType["get" + fldName];
			
			
			//Initialize validators on fields
			if(fields[i].validatorConfigurations)
			{
				var validations =  fields[i].validatorConfigurations;
				var validatorName = null;
				
				for(var v = 0; v < validations.length; v++)
				{
					validatorName = validations[v].name;
					
					if($["__validators"][validatorName] && $["__validators"][validatorName]["init"])
					{
						$["__validators"][validatorName]["init"](validations[v], fields[i]);
					}
				}
			}
		}
		
		var parentName = null;
		
		//post processing for fields which require it
		for(var i = 0; i < requirePostProcess.length; i++)
		{
			if(requirePostProcess[i].type == "lov")
			{
				parentName = requirePostProcess[i].clientConfiguration.parentField;
				
				if(!newModelType["__fieldsByName"][parentName])
				{
					throw "Invalid parent field '" + parentName + "' specified for field: " + requirePostProcess[i].name;
				}
				
				if(!newModelType["__fieldsByName"][parentName].lovChildren)
				{
					newModelType["__fieldsByName"][parentName].lovChildren = new Array();
				}
				
				newModelType["__fieldsByName"][parentName].lovChildren.push(requirePostProcess[i].name);
			}
		}
	}
	
	$["__objectTypes"][name] = newModelType;
	return newModelType;
};

var __nextInstanceId = 1;

$.create = function(name, config, ignoreExtraFields){
	var modelType = $["__objectTypes"][name];
	
	if(!modelType)
	{
		throw "Invalid model type specfied: " + name;
	}
	
	var model = new Object();
	
	//copy all attributes from type to new model object
	for(var attr in modelType)
	{
		//if function, proxy the function and copy it to this model object
		if($.isFunction(modelType[attr]))
		{
			model[attr] = $.proxy(modelType[attr], model);
			continue;
		}

		model[attr] = modelType[attr];
	}
	
	//create new set of setters and getters array
	//	And copy from modelType. While copying proxy the methods to current model
	model.setters = new Array();
	model.getters = new Array();
	model.removers = new Array();
	model.adders = new Array();
	
	var proxyMethods = function(sourceArray, targetArray) {
		for(var method in sourceArray)
		{
			targetArray[method] = $.proxy(sourceArray[method], model);
		}
	};

	proxyMethods(modelType.setters, model.setters);
	proxyMethods(modelType.adders, model.adders);
	proxyMethods(modelType.getters, model.getters);
	proxyMethods(modelType.removers, model.removers);
	
	/*
	for(var setter in modelType.setters)
	{
		model.setters[setter] = $.proxy(modelType.setters[setter], model);
	}
	
	for(var getter in modelType.getters)
	{
		model.getters[getter] = $.proxy(modelType.getters[getter], model);
	}
	
	for(var remover in modelType.removers)
	{
		model.removers[remover] = $.proxy(modelType.removers[remover], model);
	}
	*/
	
	//Set an unique id to this model, useful for debugging 
	model["__id"] = __nextInstanceId;
	
	//Call initalize while create new memory for storing data for thise model
	model["__initialize"]();
	
	
	//increment the id to maintain uniqueness
	__nextInstanceId++;

	//copy data from configuration
	if(config)
	{
		mergeExtendedFields(config, modelType);
		
		for(var attr in config)
		{
			//if function make proxy out of it
			if($.isFunction(config[attr]))
			{
				model[attr] = $.proxy(config[attr], model);
				continue;
			}
			
			//if approp setter not found for specified attribute throw error
			if(!modelType["setters"][attr])
			{
				if(ignoreExtraFields == true)
				{
					continue;
				}
				
				throw "Invalid object attribute specified: " + attr;
			}
			
			//if array is specified, which is valid case for multiValued valued field
			if($.isArray(config[attr]))
			{
				var len = config[attr].length;
				
				for(var i = 0; i < len; i++)
				{
					model["setters"][attr](config[attr][i]);
				}
			}
			else
			{
				model["setters"][attr](config[attr]);
			}
		}
	}
	
	return model;
};

/***********************************************************************************
		Utility functions
**************************************************************************************/
function mergeExtendedFields(modelPlain, modelType)
{
	if(!modelPlain["extendedProperties"])
	{
		return;
	}
	
	var extendedFields = modelPlain["extendedProperties"];
	
	for(var i = 0; i < extendedFields.length; i++)
	{
		//TODO: In case extended field contains complex value, model object needs to be constructed
		modelPlain[extendedFields[i].name] = extendedFields[i].value;
	}
}
