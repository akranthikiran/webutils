/*******************************************************************************************/
//Declare constants
/*******************************************************************************************/
var VALIDATOR_ATTR_VALUE = "value";

var IS_ERRORED = "isErrored";

var ERR_MSSG_FORM_ERRORS = "Please correct the errors and then submit!";

var DATA_UNREGISTER_LISTENERTS = "__unregisterListeners";
var DATA_CONTROLLERS = "__controllers";
var DATA_EXECUTION_CONTEXT = "__executionContext";

var ATTR_LISTENER_DATA = "__Listener_data";

var DATA_ATTR_CURRENT_LOCATION = "fw-current-location";

var FUNC_ATTR_REPEATABLE = "__attr_repeatable";

var ELEM_ATTR_PROCESSED = "__attr_processed_";

/*******************************************************************************************/
//Declare the directive and validation functions
/*******************************************************************************************/
$.directives = {};
$.attrDirectives = {names: []};
$.securityAttrDirectives = {names: []};
$.validators = {};
$.onReadyFunctions = new Array();

$.tree_listeners = {};

$.addDirective = function(name, func){
	$.directives[name.toLowerCase()] = func;
};

$.addAttrDirective = function(name, func, repeatable, secured){
	var attrDirectives = (secured == true) ? $.securityAttrDirectives : $.attrDirectives;
	
	if(attrDirectives[name.toLowerCase()])
	{
		throw "Duplicate attribute-directive specified: " + name;
	}
	
	attrDirectives[name.toLowerCase()] = func;
	attrDirectives.names.push(name.toLowerCase());
	
	if(repeatable == true)
	{
		func[FUNC_ATTR_REPEATABLE] = true;
	}
	else
	{
		func[FUNC_ATTR_REPEATABLE] = false;
	}
};

$.addValidator = function(name, func){
	$.validators[name] = func;
};

$.onFWready = function(readyFunc){
	$.onReadyFunctions[$.onReadyFunctions.length] = readyFunc;
};

$.concatArray = function(target, source){
	if(!source)
	{
		return;
	}
	
	for(var i = 0; i < source.length; i++)
	{
		target[target.length] = source[i];
	}
};

function DirectiveExecutionContext(elemSelector)
{
	this.parent = null;
	
	var elem = (!elemSelector) ? $(document) : $(elemSelector);
	
	if(!elem || elem.length == 0)
	{
		throw "DirectiveExecutionContext: Invalid element selector specified: " + elemSelector;
	}
	
	elem = elem.first();
	
	if(elem.attr(DATA_ATTR_CURRENT_LOCATION))
	{
		this.parent = elem;
	}
	
	var parentLst = elem.parents("[" + DATA_ATTR_CURRENT_LOCATION + "]");
	
	if(!parentLst || parentLst.length == 0)
	{
		this.parent = $(document);
	}
	else
	{
		this.parent = $(parentLst.first());
	}
	
	//Unregister listeners
	this.unregisterListeners = this.parent.data(DATA_UNREGISTER_LISTENERTS);
	
	if(!this.unregisterListeners)
	{
		this.unregisterListeners = new Array();
		this.parent.data(DATA_UNREGISTER_LISTENERTS, this.unregisterListeners);
	}
	
	//Controllers
	this.controllers = this.parent.data(DATA_CONTROLLERS);

	if(!this.controllers)
	{
		this.controllers = new Object();
		this.parent.data(DATA_CONTROLLERS, this.controllers);
	}

	this.addUnregisterListener = $.proxy(function(data, listener){
		if(!listener)
		{
			return;
		}
		
		this.unregisterListeners.push(listener);
		listener[ATTR_LISTENER_DATA] = data;
	}, this);
	
	
	//register an event listener and unregister listenet to an element
	this.registerElementListener =  $.proxy(function(element, event, data, listener, context){
		var finalListener = listener;
		
		if(context)
		{
			finalListener = $.proxy(listener, context);
		}
		
		element.on(event, data, finalListener);
		
		this.addUnregisterListener({"element": element, "listener": finalListener, "event": event}, function(event){
			var eventData = event.data;
			eventData.element.off(eventData.event, eventData.listener);
		});
	}, this);
	
	//list of elements with processed flags
	this.processedElements = new Object();
	
	this.flagProcessedElement = $.proxy(function(elem, attrName){
		if(!this.processedElements[attrName])
		{
			this.processedElements[attrName] = new Array();
		}
		
		this.processedElements[attrName].push(elem);
	}, this);
	
	this.addController = $.proxy(function(controller){
		if(this.controllers[controller.name])
		{
			return;
		}
		
		this.controllers[controller.name] = controller;
	}, this);
	
	this.initialize = $.proxy(function(){
		var event = {
				"parent": this.parent,
				data: null
		};
		
		var loadedMethod = null;
		
		//initialize controllers
		for(var name in this.controllers)
		{
			loadedMethod = this.controllers[name][CONTROLLER_CONTENT_LOADED_METHOD_NAME];
			
			if(loadedMethod && $.isFunction(loadedMethod))
			{
				try
				{
					loadedMethod(event);
				}catch(ex)
				{
					console.error("An error occurred while invoking page load method on controller: " + name);
					console.error(ex);
				}
			}
		}
	}, this);
	
	this.unregister = $.proxy(function(){
		
		//Execute unregister listeners
		var data = null;
		
		var event = {
				"parent": this.parent,
				data: null
		};
		
		for(var i = 0; i < this.unregisterListeners.length; i++)
		{
			event.data = this.unregisterListeners[i][ATTR_LISTENER_DATA];
			this.unregisterListeners[i](event);
		}
		
		event.data = null;
		
		//Execute controllers finalize methods
		for(var name in this.controllers)
		{
			if(this.controllers[name]["finalize"] && $.isFunction(this.controllers[name]["finalize"]))
			{
				this.controllers[name]["finalize"](event);
			}
		}
		
		//Remove processed flag on elements
		for(var attrName in this.processedElements)
		{
			for(var i = 0; i < this.processedElements[attrName].length; i++)
			{
				this.processedElements[attrName][i].removeData(ELEM_ATTR_PROCESSED + attrName, null);
			}
		}
		
		//Remove listeners and controllers
		this.parent.data(DATA_UNREGISTER_LISTENERTS, null);
		this.parent.data(DATA_CONTROLLERS, null);
		this.processedElements = new Object();
	}, this);
};

$.parseDirectives = function(parentSelector){
	
	var prefix = "";
	
	if(parentSelector)
	{
		var parentElement = $(parentSelector);
		
		if(!parentElement || parentElement.length <= 0)
		{
			console.error("Invalid parent selector specified: " + parentSelector);
			return;
		}
		
		prefix = parentSelector + " ";
	}
	
	var executionContext = $(document).data("DATA_EXECUTION_CONTEXT");
	var executionContextSet = false;
	
	if(!executionContext)
	{
		executionContext = new DirectiveExecutionContext(parentSelector);
		$(document).data("DATA_EXECUTION_CONTEXT", executionContext);
		
		executionContextSet = true;
	}
	
	var processAttributeDirectives = function(attrDirectives) {
		var name = null, directiveFunc = null;
		var attributeSelector = null;
		
		for(var attrNameIdx = 0; attrNameIdx < attrDirectives.names.length; attrNameIdx++)
		{
			name = attrDirectives.names[attrNameIdx];
			directiveFunc = attrDirectives[name];
			
			if(parentSelector)
			{
				attributeSelector = parentSelector + "[" + name + "]," + prefix + "[" + name + "]";
			}
			else
			{
				attributeSelector = prefix + "[" + name + "]";
			}
			
			$(attributeSelector).each(function(idx, domElem){
				directiveFunc(idx, domElem, executionContext);
			});
		}
	};

	//first process secured attribute directives then proceed for custom directives and attribute directives
	processAttributeDirectives($.securityAttrDirectives);
	
	//process custom directives
	$.each($.directives, function(name, directiveFunc){
		$(prefix + name).each(function(idx, domElem){
			directiveFunc(idx, domElem, executionContext);
		});
	});
	
	//process custom attribute directives
	processAttributeDirectives($.attrDirectives);

	/*
	$.each($.validators, function(validatorName, validatorFunc){
		var registerFunc = function(idx, domElem){
			var elem = $(domElem);
			var data = new Object();
			var name = $(this).attr("name");
			
			if(!name)
			{
				console.error("Name is not specified for input field with field-validation - " + validatorName + ". Name is mandatory for field validation");
				return;
			}
			
			data[VALIDATOR_ATTR_VALUE] = $(this).attr("fw-" + validatorName);
			
			var validatorFuncWrapper = function(event){
				
				var fldName = $(this).attr("name");
				var errorElemKey = 'span[fw-field="' + fldName +'"][fw-validation="' + validatorName +'"]';
				var errorElem = $(errorElemKey);
				
				//Hide all error elements of this field
				$('span[fw-field="' + fldName +'"]').removeClass("showError");
				$('span[fw-field="' + fldName +'"]').addClass("hiddenError");
				
				if(!validatorFunc($(this), event, event.data))
				{
					$(this).data(IS_ERRORED, true);
					errorElem.addClass("showError");
					errorElem.removeClass("hiddenError");
					
					event.stopImmediatePropagation();
				}
				else
				{
					errorElem.addClass("hiddenError");
					errorElem.removeClass("showError");
				}
			};
			
			//elem.on("change", data, validatorFuncWrapper);
			//elem.on("keyup", data, validatorFuncWrapper);
			
			executionContext.registerElementListener(elem, "change", data, validatorFuncWrapper);
			executionContext.registerElementListener(elem, "keyup", data, validatorFuncWrapper);
		};
		
		$(prefix + "input[fw-" + validatorName + "]," + 
				prefix + "select[fw-" + validatorName + "]," +
				prefix + "textarea[fw-" + validatorName + "]").each(registerFunc);
	});
	*/
	
	//Apply jquery styles to all input elements and other data-elements
	$(prefix + 'input[type="text"],' + prefix + "select," + prefix + "textarea").addClass("ui-widget-content ui-corner-all");
	
	$(prefix + "table.dataTable td").removeClass("dataCell");
	$(prefix + "table.dataTable td").addClass("dataCell");
	
	$(prefix + 'input[type="button"],' + prefix + 'input[type="submit"]').button();
	
	if(executionContextSet)
	{
		executionContext.initialize();
		$(document).data("DATA_EXECUTION_CONTEXT", null);
	}
};

$.unregisterDirectives = function(parentSelector){
	var executionContext = new DirectiveExecutionContext(parentSelector);
	executionContext.unregister();
};

$.reinitalizeControllers = function(parentSelector){
	var executionContext = new DirectiveExecutionContext(parentSelector);
	executionContext.initialize();
};

/*******************************************************************************************/
//Parse Directives and validators on load
/*******************************************************************************************/

$(document).ready(function(){
	$.parseDirectives();
	
	for(var controllerName in $.controllers)
	{
		if( $.isFunction($.controllers[controllerName][CONTROLLER_ON_FW_READY_METHOD_NAME]) )
		{
			$.controllers[controllerName][CONTROLLER_ON_FW_READY_METHOD_NAME]();
		}
	}
	
	$("form").each(function(idx, domForm){
		var form = $(domForm);
		
		if(form.attr("fw-validate") == "true")
		{
			var checkErrors = function(event){
				var hasErrors = false;
				
				$(this).find("input, select, textarea").each(function(idx, domElem){
					var elem = $(domElem);
					
					elem.data(IS_ERRORED, false);
					
					elem.trigger("change");
					
					if(elem.data(IS_ERRORED))
					{
						hasErrors = true;
					}
				});
				
				if(hasErrors)
				{
					alert(ERR_MSSG_FORM_ERRORS);
					event.stopPropagation();
					return false;
				}
				
				return true;
			};
			
			form.submit(checkErrors);
		}
	});
	
	$.each($.onReadyFunctions, function(idx, func){
		func();
	});
});

/*******************************************************************************************/
//Default directives and validators declaration
/*******************************************************************************************/


function createTooltip(position, elem, ttContent)
{
	var myPosition = "center top+20";
	var atPosition = "center bottom";
	var arrowClass = "arrow";
	
	if(position == "bottom")
	{
		myPosition = "center top+15";
		atPosition = "center bottom";
	}
	else if(position == "top")
	{
		myPosition = "center bottom-15";
		atPosition = "center top";
	}
	else if(position == "left")
	{
		myPosition = "right-15 center";
		atPosition = "left center";
		
		arrowClass = "arrowHor";
	}
	else if(position == "right")
	{
		myPosition = "left+15 center";
		atPosition = "right center";
		
		arrowClass = "arrowHor";
	}
	
	elem.tooltip({
		items: elem.prop("tagName"),
		content: ttContent,
		delay: 5000,
	    position: {
		        my: myPosition,
		        at: atPosition,
		        /*
		        using: function( position, feedback ) {
		          $( this ).css( position );
		          $( "<div>" )
		            .addClass( arrowClass )
		            .addClass( feedback.vertical )
		            .addClass( feedback.horizontal )
		            .appendTo( this );
		        }
		        */
	    },
		open: function(event, ui) {
			var tooltip = ui.tooltip;
			setTimeout(function(tooltip){tooltip.hide();}, 4000, tooltip);
		}
	});
}

$.addAttrDirective("fw-tooltip", function(idx, domElem){
	var elem = $(domElem);
	
	var ttContent = elem.attr("fw-tooltip");
	
	if(!ttContent)
	{
		return;
	}
	
	var position = elem.attr("fw-tooltip-position");
	createTooltip(position, elem, ttContent);
}, true);

$.addAttrDirective("fw-controller", function(idx, domElem, context){
	var elem = $(domElem);
	
	var controllerName = elem.attr("fw-controller");
	
	if(!controllerName)
	{
		return;
	}
	
	if(!$.controllers[controllerName])
	{
		console.error("Non-existing/invalid controller name specified: " + controllerName);
		return;
	}
	
	$(domElem).data("controller", $.controllers[controllerName]);
	context.addController($.controllers[controllerName]);
});

$.getController = function(domElem, elemRepr){
	var elem = $(domElem);
	var parent = null;
	
	if(elem.attr("fw-controller") != null)
	{
		parent = elem;
	}
	else
	{
		parent = elem.parents("[fw-controller]");
	}
	
	if(!parent || parent.length == 0)
	{
		if(elemRepr)
		{
			console.error("No parent controller found for " + elemRepr);
		}
		
		return null;
	}
	
	parent = $(parent.first());
	
	var controllerName = parent.attr("fw-controller");
	var controller = $.controllers[controllerName];
	
	return controller;
};

function sliceArguments(argArray, from) {
	if(!argArray || argArray.length <= from)
	{
		return null;
	}
	
	var args = new Array();

	for(var i = from; i < argArray.length; i++)
	{
		args.push(argArray[i]);
	}
	
	return args;
};

function invokeController(event, method) {
	var source = event.currentTarget;
	var args = sliceArguments(arguments, 2);
	
	var controller = $.getController(source, "controller-method: " + method);

	if(!controller)
	{
		console.error("No controller found for element: " + source.prop("tagName") + " [Controller-method: " + method + "]");
		return;
	}

	var controllerMethod = controller[method];
	
	if(!controllerMethod || !$.isFunction(controllerMethod))
	{
		console.error("Invalid controller method encountered: " + method + "[Controller: " + controller.name + "]");
		return;
	}
	
	return controllerMethod.apply(controller, args);
}

function invokeDynamicMethod(sourceElement, methodName)
{
	var CONTROLLER_PREFIX = "controller:";
	
	var args = sliceArguments(arguments, 2);
	
	if(methodName.startsWith(CONTROLLER_PREFIX))
	{
		methodName = methodName.substr(CONTROLLER_PREFIX.length);
		args.unshift({"currentTarget": sourceElement}, methodName);
		
		return invokeController.apply(this, args);
	}
	
	window[methodName].apply(this, args);
}

$.addAttrDirective("fw-controller-method", function(idx, domElem, context){
	var elem = $(domElem);
	
	var methodName = elem.attr("fw-controller-method");
	
	if(!methodName)
	{
		return;
	}
	
	if(elem.prop("tagName") != "INPUT" && elem.prop("tagName") != "SELECT" && elem.prop("tagName") != "A")
	{
		console.error("'fw-controller-method' attribute can be specified for <input>/<select>/<a> tag. The attribute found on: " + elem.prop("tagName"));
		return;
	}

	var controller = $.getController(domElem, "controller-method: " + methodName);

	if(!controller)
	{
		console.error("No controller found for element: " + elem.prop("tagName") + " [Controller-method: " + methodName + "]");
		return;
	}

	var controllerMethod = controller[methodName];
	
	if(!controllerMethod || !$.isFunction(controllerMethod))
	{
		console.error("Invalid controller method encountered: " + methodName + "[Controller: " + controller.name + "]");
		return;
	}
	
	if(controller["setControl"])
	{
		controller["setControl"](parent);
	}
	
	//var callback = controllerMethod;//$.proxy(controllerMethod, controller);
	
	if(elem.attr("type") == "button" || elem.prop("tagName") == "A")
	{
		//elem.off("click").on('click', {"element": elem}, callback);
		//context.registerElementListener(elem, "click", {"element": elem}, callback);
		elem.attr('onclick', "invokeController(event, '" + methodName + "')");
	}
	else
	{
		//elem.off("change").on('change', {"element": elem}, callback);
		//context.registerElementListener(elem, "change", {"element": elem}, callback);
		elem.attr('onchange', "invokeController(event, '" + methodName + "')");
	}
});

$.addAttrDirective("fw-model", function(idx, domElem, context){
	var elem = $(domElem);
	var modelType = elem.attr("fw-model");
	
	var model = null;
	
	try
	{
		model = $.create(modelType);
	}catch(ex)
	{
		console.error("Failed to created model of type '" + modelType + "' specified on tag - " + elem.prop("tagName"));
		return;
	}
	
	elem.data(DATA_ATTR_MODEL, model);
});

function findModelForElement(elem)
{
	var parent = elem.parents("[fw-model]");
	
	if(!parent || parent.length == 0)
	{
		return null;
	}
	
	parent = $(parent.first());
	return parent.data(DATA_ATTR_MODEL);
}

$.addAttrDirective("fw-field", function(idx, domElem, context){
	var elem = $(domElem);
	var model = findModelForElement(elem);
	
	if(!model)
	{
		console.error("No parent model found for parameterized model-field defined on tag: " + elem.prop("tagName"));
		return;
	}
	/*
	var parent = elem.parents("[fw-model]");
	
	if(!parent || parent.length == 0)
	{
		console.error("No parent model found for parameterized model-field defined on tag: " + elem.prop("tagName"));
		return;
	}
	
	parent = $(parent.first());
	*/
	var modelField = elem.attr("fw-field");
	elem.attr("fw-model-id", model["__id"]);
	
	/*
	if(!model)
	{
		console.error("No model object is attached to parent model element: " + elem.prop("tagName"));
		return;
	}
	*/
	
	if(!model.isValidField(modelField))
	{
		console.error("No field found with name '" + modelField + "' in model of type '" + model.getClassName() + "'. Used on tag: " + elem.prop("tagName"));
		return;
	}
	
	if(elem.prop("tagName") == 'SELECT')
	{
		var values = model.getLovValues(modelField);
		elem.html("");
		elem.append($("<option></option>").attr("value", "").html("&nbsp;&nbsp;&nbsp;&nbsp;"));
		
		if(values)
		{
			for(var i = 0; i < values.length; i++)
			{
				elem.append($("<option></option>").attr("value", values[i].value).text(values[i].label));
			}
		}
	}
	
	var getElementValue = function(elem) {
		var value = elem.val();
		
		if(elem.is("input:checkbox"))
		{
			value = (elem.prop("checked")) ? "true" : "false";
		}
		else if(elem.attr("fw-field-type") == 'date-picker')
		{
			value = elem.datepicker("getDate");
		}
		else if(value.length == 0)//if empty string make it into null
		{
			value = null;
		}

		return value;
	};
	
	var setElementValue = function(elem, value) {
		
		if(!isDefined(value))
		{
			value = "";
		}
		
		if(elem.is("input:checkbox"))
		{
			if(("" + value).toLowerCase() == "true")
			{
				value = true;
			}
			else
			{
				value = false;
			}
			
			elem.prop("checked", value);
		}
		else if(elem.attr("fw-field-type") == "date-picker")
		{
			if(!value)
			{
				elem.val("");
			}
			else
			{
				elem.datepicker("setDate", value);
			}
		}
		else
		{
			elem.val(value);
		}
	};

	var value = getElementValue(elem);
	
	if(isDefined(value))
	{
		try
		{
			model["setters"][modelField](value);
		}catch(ex)
		{
			//ignore
		}
	}
	
	setElementValue(elem, model["getters"][modelField]());
	
	var eventConfiguration = {
		"model": model, 
		"field": modelField, 
		"getElementValue": getElementValue,
		"setElementValue": setElementValue,
		
		"element": elem
	};


	//register change listener to target element. So that on change of element value attached model property is updated
	//context.registerElementListener(elem, "change", eventConfiguration, function(event){
	elem.off("change").on("change", eventConfiguration, function(event){
		var model = event.data.model;
		var field = event.data.field;
		var value = event.data.getElementValue($(this));
		
		try
		{
			model["setters"][field](value);
		}catch(ex)
		{
			if(ex.message)
			{
				$.error("Errror: " + ex.message + "<BR/>Field: " + ex.fieldLabel);
			}
			else
			{
				$.error("An error occurred while setting value for field: " + field + "<BR/>Error: " + ex);
			}
			
			event.data.setElementValue($(this), model["getters"][field]());
			$(this).focus();
		}
	});
	
	var propertyChangeListener = function(event) {
		var model = event.data.model;
		var field = event.data.field;
		var eventType = event.eventType;
		var elem = event.data.element;

		if(event.field != field)
		{
			return;
		}
		
		if(eventType == EVENT_TYPE_VALUE_CHANGED)
		{
			event.data.setElementValue(elem, event.newValue);
			return;
		}
		
		if(eventType == EVENT_TYPE_LOV_LIST_CHANGED && elem.prop("tagName") == 'SELECT')
		{
			var values = model.getLovValues(field);
			elem.html("");
			
			elem.append($("<option></option>").attr("value", "").html("&nbsp;&nbsp;&nbsp;&nbsp;"));
			
			for(var i = 0; i < values.length; i++)
			{
				elem.append($("<option></option>").attr("value", values[i].value).text(values[i].label));
			}
		}
	};
	
	//the property change listener need not be unregistered, as next time it will simply replace existing listener with same name
	model.addPropertyChangeListener("fw-field-listener-4-" + modelField, eventConfiguration, propertyChangeListener);

	/*
	context.addUnregisterListener({"model": model, "listener": propertyChangeListener}, function(event){
		var eventData = event.data;
		
		if(!eventData.model.removePropertyChangeListener(eventData.listener))
		{
			console.error("Failed to remove property-change listener from model [Model: " + eventData.model.getClassName() + "]");
		}
	});
	*/
});

$.addAttrDirective("fw-disabled", function(idx, domElem, context){
	var elem = $(domElem);
	var disabled = elem.attr("fw-disabled");
	
	if(disabled == 'true')
	{
		elem.prop('disabled', true);
	}
	else
	{
		elem.prop('disabled', false);
	}
});

/*
$.addAttrDirective("fw-data-url", function(idx, domElem, context){
	var DATA_LISTENER_ATTACHED = "__listenerAttached";
	
	var elem = $(domElem);
	
	var attachedModel = elem.data(DATA_LISTENER_ATTACHED);
	
	if(attachedModel)
	{
		return;
	}
	
	var url = elem.attr("fw-data-url");
	
	if(!url)
	{
		return;
	}
	
	if(elem.prop("tagName") != "SELECT")
	{
		console.error("'fw-data-url' attribute can be specified for <select> tag only. The attribute found on: " + elem.prop("tagName"));
		return;
	}

	var hasExpressions = /\{\{([\w\-\.]+)\}\}/.test(url);
	
	if(!hasExpressions)
	{
		url = $.contextPath + url;
		var data = makeCachedJsonCall(url, url);
		var idFld = elem.attr("fw-id-field")? elem.attr("fw-id-field"): "id";
		var labelFld = elem.attr("fw-label-field")? elem.attr("fw-label-field"): "label";
		
		$.each(data, function(idx, item){
			elem.append($("<option></option>").attr("value", item[idFld]).text(item[labelFld]));
		});
		
		return;
	}
	
	var parent = elem.parents("[fw-controller]");
	
	if(!parent || parent.length == 0)
	{
		console.error("No parent controller found for parameterized data-url: " + url);
		return;
	}
	
	parent = $(parent.first());
	
	var controllerName = parent.attr("fw-controller");
	var model = $.controllers[controllerName].model;
	
	if(!model || model.addPropertyListener)
	{
		console.error("No/model model found for controller '" + controllerName + "' needed by parameterized data-url: " + url);
		return;
	}
	
	var propertyChanged = function(model, property){
		var url = this.attr("fw-data-url");
		
		if(url.indexOf(property) < 0)
		{
			return;
		}
		
		try
		{
			url = parseExpressions(url, model, true);
		}catch(err)
		{
			return;
		}
		
		url = $.contextPath + url;
		
		var data = makeCachedJsonCall(url, url);
		var idFld = this.attr("fw-id-field")? this.attr("fw-id-field"): "id";
		var labelFld = this.attr("fw-label-field")? this.attr("fw-label-field"): "label";
		var combo = this;
		
		combo.html("");
		
		$.each(data, function(idx, item){
			combo.append($("<option></option>").attr("value", item[idFld]).text(item[labelFld]));
		});
	};
	
	var callback = $.proxy(propertyChanged, elem);
	model.addPropertyChangeListener(callback);
	
	elem.data(DATA_LISTENER_ATTACHED, model);
	
	context.addUnregisterListener({"model": model, "listener": callback}, function(event){
		var eventData = event.data;
		
		if(!eventData.model.removePropertyChangeListener(eventData.listener))
		{
			console.error("Failed to remove property-change listener from model [Model: " + eventData.model.getClassName() + "]");
		}
	});
});
*/

$.addDirective("dialog", function(idx, domElem){
	var elem = $(domElem);
	var id = elem.attr("id");
	
	if(!id)
	{
		console.error("Mandatory attribute 'id' is not specified for <dialog>");
		return;
	}
	
	var dlgWidth = elem.attr("width")? elem.attr("width") : "100";
	var dlgHeight = elem.attr("height")? elem.attr("height") : "100";
	
	var PERCENT_PATTERN = /(\d+)\%/;
	var match = PERCENT_PATTERN.exec(dlgWidth);
	
	if(match)
	{
		dlgWidth = $(document).width() * parseInt(match[1]) / 100;
	}
	
	match = PERCENT_PATTERN.exec(dlgHeight);
	
	if(match)
	{
		dlgHeight = $(document).height() * parseInt(match[1]) / 100;
	}

	var extraAttr = fetchExtraAttributesAsStr(domElem, ["width", "height", "title", "id"]);
	
	var html = '<div id="' + id + '" ' + extraAttr + ">" + elem.html() + '</div>';
	$(html).insertAfter(elem);
	elem.remove();
	
	$("#" + id).dialog({
		width: dlgWidth,
		height: dlgHeight,
		title: elem.attr("title"),
		autoOpen: false,
		modal: true,
		resizeable: false
	});
	
	$.parseDirectives("#" + id);
});


$.addDirective("tooltip", function(idx, domElem){
	var elem = $(domElem);
	
	var ttContent = elem.html();
	
	if(!ttContent)
	{
		return;
	}
	
	var position = elem.attr("position");
	
	var parent = elem.parent();
	parent = $(parent);
	
	if(!parent)
	{
		return;
	}
	
	if(elem.attr("helpIcon") == "true")
	{
		parent.addClass("helpIcon");
	}
	
	createTooltip(position, parent, ttContent);
	
	setTimeout(function(elem){elem.remove();}, 10, elem);
	//elem.remove();
});

function fetchExtraAttributesAsStr(elem, skipAttr)
{
	var attrCode = '';
	
	if(!elem.attributes || elem.attributes.length <= 0)
	{
		return;
	}
	
	$.each(elem.attributes, function(idx, attr){
		var name = attr.name;
		
		if(skipAttr && skipAttr.indexOf(name) >= 0)
		{
			return;
		}
		
		if(name.toLowerCase().indexOf("on") == 0)
		{
			return;
		}
		
		attrCode += attr.name + '="' + attr.value + '" ';
	});
	
	return attrCode;
}

$.addDirective("date-picker", function(idx, domElem, context){
	var elem = $(domElem);
	
	var value = elem.attr("value");
	
	if(!value)
	{
		value = "";
	}
	
	var selector = null;
	var deleteId = null;
	
	if(elem.attr("id"))
	{
		selector = "#" + id;
		deleteId = "__dataPicker_del_" + elem.attr("id");
	}
	else if(elem.attr("name"))
	{
		selector = 'input[fw-field-type="date-picker"][name="' + elem.attr("name") + '"]';
		deleteId = "__dataPicker_del_name_" + elem.attr("name");
	}
	else
	{
		console.error("No id/name is specified on <date-picker> element");
		return;
	}
	
	var dateFormat = elem.attr("date-format");
	
	if(!dateFormat)
	{
		dateFormat = "dd/mm/yy";
	}
	
	var extraAttr = fetchExtraAttributesAsStr(domElem, []);
	
	var htmlCode = '<div style="display: inline-block;white-space: nowrap;">';
	htmlCode += '<input type="text" value="' + value + '" fw-field-type="date-picker" readonly="true" ';
	htmlCode += extraAttr;
	htmlCode += "/>";
	htmlCode += '<a id="' + deleteId + '" href="#" style="text-decoration: none;color: red;font-size: 0.9em;margin-left: 0.3em;" title="Clear Date">' + 
				'<i class="icon-circledelete"></i></a>';
	
	var parent = $(elem.parent());
	$(htmlCode).insertAfter(elem);
	elem.remove();
	
	var today = new Date();
	var maxYear = today.getFullYear() + 50;
	
	var newElem = parent.find(selector).first();
	$(newElem).datepicker({
		"dateFormat": dateFormat,
		changeMonth: true,
		changeYear: true,
		yearRange: "-100:+50"
	});
	
	context.registerElementListener($(parent.find("#" + deleteId).first()), "click", {"field": $(newElem)}, function(event){
		event.data.field.val("");
		event.data.field.trigger("change");
	});
});

$.addDirective("progress-bar", function(idx, domElem, context){
	var elem = $(domElem);
	
	var selector = null;
	
	if(elem.attr("id"))
	{
		selector = "#" + elem.attr("id");
	}
	else if(elem.attr("name"))
	{
		selector = 'div[name="' + elem.attr("name") + '"]';
	}
	else
	{
		console.error("No id/name is specified on <progress-bar> element");
		return;
	}
	
	var labelClass = elem.attr("label-class");
	var extraAttr = fetchExtraAttributesAsStr(domElem, ["label-class"]);
	
	labelClass = labelClass ? labelClass : "";
	
	var htmlCode = '<div ' + extraAttr + '>' + 
			'<div class="__label ' + labelClass + '" style="position: absolute;left: 50%;margin-top: 0.5em;font-weight: bold;"></div>' +
			'</div>';
	
	$(htmlCode).insertAfter(elem);
	elem.remove();

	$(selector).progressbar({
		value: 0,
		change: function() {
			var progressbar = $(this);
			progressbar.find(".__label").text(progressbar.progressbar( "value" ) + "%" );
		},
		complete: function() {
			$(this).find(".__label").text( "Complete!" );
		}
	});
	
	$(selector).progressbar("value", 0);
});


$.addDirective("error", function(idx, domElem){
	var elem = $(domElem);
	
	var field = elem.attr("field");
	var validation = elem.attr("validation");
	
	if(!field)
	{
		console.error("Attribute 'field' is not specified for element '<error>'");
		return;
	}
	
	if(!validation)
	{
		console.error("Attribute 'validation' is not specified for element '<error>'");
		return;
	}
	
	var id = elem.attr("id");
	
	if(!id)
	{
		id = "__err_" + field + "_" + validation + "_" + $.now();
	}
	
	var elemCode = '<span id="' + id + '"/>'; 
	$(elemCode).insertAfter(elem);
	elem.remove();
	
	var createElem = $("#" + id);
	
	$.each(domElem.attributes, function(idx, attr){
		var name = attr.name;
		
		if(name == "id")
		{
			return;
		}
		
		if(name == "field" || name == "validation")
		{
			name = "fw-" + name;
		}
		
		createElem.attr(name, attr.value);
	});
	
	//createElem.css("visibility", "hidden");
	//createElem.css("position", "absolute");
	createElem.addClass("hiddenError");
	createElem.html(elem.html());
});

function checkForMandatoryAttributes(domElem, attrLst)
{
	var res = true;
	var elem = $(domElem);
	
	$.each(attrLst, function(idx, attr){
		
		if(!elem.attr(attr))
		{
			console.error("Mandatory attribute '" + attr + "' is not specified for element <" + elem.prop("tagName") + ">");
			res = false;
		}
	});
	
	return res;
}

$.addDirective("tree", function(idx, domElem){
	var elem = $(domElem);
	
	var id = elem.attr("id");
	
	if(!id)
	{
		console.error("Attribute 'id' is not specified for element '<error>'");
		return;
	}

	var elemCode = '<div id="' + id + '">';
	//var contextMenu = null;
	
	var addChildCode = function(sourceElem)
	{
		var childern = sourceElem.children();

		if(!childern || childern.length <= 0)
		{
			return;
		}

		childern.each(function(idx, domChildElem){
			var childElem = $(domChildElem);
			
			if(childElem.prop("tagName").toLowerCase() != 'tree-item')
			{
				console.error("Unsupported tag \"" + childElem.prop("tagName") + "\" under <tree> tag. Ignoring it. Id: " + id);
				return;
			}
			
			var id = childElem.attr("id"); 
			
			if(!id)
			{
				console.error("Mandatory attribute 'id' is not specified foe tag <tree-item>. Id: " + id);
				return;
			}
			
			if(idx == 0)
			{
				elemCode += "<ul>";
			}
			
			var extraAttr = fetchExtraAttributesAsStr(domChildElem, ["id", "label", "state-opened"]);
			
			elemCode += '<li id="' + id + '" ' + extraAttr;
			
			if(childElem.attr("state-opened"))
			{
				elemCode += ' class="jstree-open" ';
			}

			elemCode += '>' + childElem.attr("label");
			
			addChildCode(childElem);
			
			elemCode += "</li>";
			
			if(childElem.attr("onSelect"))
			{
				$.tree_listeners[id] = childElem.attr("onSelect");
			}
		});
		
		elemCode += "</ul>";
	};
	
	addChildCode(elem);
	elemCode += "</div>";
	
	$(elemCode).insertAfter(elem);
	elem.remove();
	
	var jstreeObj = $('#' + id).jstree({
		"core" : {
			"themes" : { "dots" : false },
			"multiple" : false,
			"plugins" : ["contextmenu"]		
		}
	});
	
	$('#' + id).data("_widget_tree", jstreeObj);
	
	$('#' + id).on("select_node.jstree", function (event, data) {
		if($.tree_listeners[data.node.id])
		{
			eval($.tree_listeners[data.node.id]);
		}
	});
	
	//TODO: unregister event mechanism for tree
});

$.addDirective("tabs", function(idx, domElem){
	var elem = $(domElem);
	
	var tabsId = elem.attr("id");
	
	if(!tabsId)
	{
		console.error("Attribute 'id' is not specified for element '<tabs>'");
		return;
	}

	
	var extraAttr = fetchExtraAttributesAsStr(domElem, ["id"]);
	
	var headCode = '<div id="' + tabsId + '"' + extraAttr + '><ul>';
	var contentCode = '';
	var activeTabIdx = 0;
	var tabIndex = 0;
	
	var events = new Object();
	var contentIds = new Array();
	
	var addChildCode = function(sourceElem)
	{
		var childern = sourceElem.children();

		if(!childern || childern.length <= 0)
		{
			return;
		}

		childern.each(function(idx, domChildElem){
			var childElem = $(domChildElem);
			
			if(childElem.prop("tagName").toLowerCase() != 'tab')
			{
				console.error("Unsupported tag \"" + childElem.prop("tagName") + "\" under <tabs> tag. Ignoring it!");
				return;
			}
			
			var id = childElem.attr("id");
			
			if(!id)
			{
				console.error("Mandatory attribute 'id' is not specified foe tag <tab>.");
				return;
			}
			
			var labelElem = childElem.children('label');
			
			if(labelElem.length <= 0)
			{
				console.error("Mandatory sub-element 'label' is not specified foe tag <tab>. Id: " + id);
				return;
			}
			
			var label = labelElem.first().html();
			
			var contentElem = childElem.children('content');
			var content = '';
			var contentAttr = "";
			
			if(contentElem.length > 0)
			{
				content = contentElem.first().html();
				contentAttr = fetchExtraAttributesAsStr(contentElem[0], ["fw-tab-index"]);
				contentAttr += ' fw-tab-index="' + tabIndex + '" ';
			}
			
			if("true" == childElem.attr("active"))
			{
				activeTabIdx = tabIndex;
			}
			
			var extraAttr = fetchExtraAttributesAsStr(domChildElem, ["id", "active"]);
			
			headCode += '<li> <a href="#' + id + '" ' + extraAttr + '>' + label + "</a></li>";
			
			contentCode += '<div id="' + id + '" style="min-height: 100%;overflow: auto;padding: 0px;" ' + contentAttr + '>';
			contentCode += content;
			contentCode += "</div>";
			
			$.each(domChildElem.attributes, function(idx, attr){
				var name = attr.name;
				
				if(name.toLowerCase().indexOf("on") == 0)
				{
					var event = name.slice(2).toLowerCase();
					
					if(!events[event])
					{
						events[event] = new Object();
					}
					
					events[event][id] = attr.value;
				}
			});
			
			contentIds[contentIds.length] = id;
			tabIndex++;
		});
	};
	
	addChildCode(elem);
	
	headCode += "</ul>";
	contentCode += "</div>";
	
	var fullCode = headCode + contentCode;
	
	$(fullCode).insertAfter(elem);
	elem.remove();

	$('#' + tabsId).tabs({
			active : 		activeTabIdx,
			heightStyle: "fill"
	});
	
	//Add event code invocation logic
	$.each(events, function(eventName, tabArray){
		$('#' + tabsId).on('tabs' + eventName, null, tabArray, function(event, ui){
			var eventTabArray = event.data;
			
			if(eventTabArray[ui.newPanel.attr('id')])
			{
				eval(eventTabArray[ui.newPanel.attr('id')]);
			}
		});
	});
	
	//TODO: During unregistration we need way to unregister tab events
	
	
	//Reparse the content-layer's content
	$.each(contentIds, function(idx, contentId){
		$.parseDirectives("#" + contentId);
	});
});

$.addDirective("file-selector", function(idx, domElem, context){
	var elem = $(domElem);

	var textClass = elem.attr("textClass") ? elem.attr("textClass") : "siteLabel";
	var labelClass = elem.attr("labelClass") ? elem.attr("labelClass") : "siteLabel";
	var label = elem.attr("label") ? elem.attr("label") : "Selected File: ";
	var name = elem.attr("name") ? elem.attr("name") : "";
	var id = elem.attr("id") ? elem.attr("id") : "";

	$.templateEngine.setContext({
		"labelClass": labelClass,
		"label": label,
		"textClass": textClass,
		"name": name,
		"id": id
	});
	
	var html = $.templateEngine.processTemplate("fileChooserTemplate");
	
	$(html).insertAfter(elem);
	
	var newElem = elem.next();
	newElem = $(newElem);
	
	newElem.find("input[name='browseButton']").on("click", function(){
		$(this).parent().find("input[type='file']").trigger('click');
	});

	newElem.find("input[type='file']").on("change", function(){
		var file = $(this).val();
		var fileName = (file) ? "" + file : "";
		
		$(this).parent().find("input[type='text']").val(fileName);
	});

	elem.remove();
});

/*
$.addDirective("file-selector", function(idx, domElem, context){
	var elem = $(domElem);
	var name = elem.attr("name");
	var id = elem.attr("id");
	var fileType = elem.attr("file-type");
	var needPreview = elem.attr("need-preview");
	
	if(!name || !id)
	{
		console.error("Mandatory attribute 'name' is not specified for <file-selector>");
		return;
	}

	var style = elem.attr("cssStyle");
	var cls = elem.attr("cssClass");
	var value = elem.html();
	
	if(!style)
	{
		style="";
	}
	
	if(!cls)
	{
		cls="";
	}
	
	var disabledStatus = "";
	
	if(elem.attr("disabled") == "disabled")
	{
		disabledStatus = ' disabled="disabled" ';
	}
	
	var extraAttrStr = fetchExtraAttributesAsStr(domElem, ["cssStyle", "cssClass", "class", "style"]);
	
	var html = '<div style="' + style + '" class="' + cls + '"><table><tr><td style="width: 100%;">' +
					'<input type="text" name="' + name + '_display" id="' + id + '_display" style="width: 100%;" disabled="disabled"/>' +
					'<textarea style="display: none" name="' + name + '" id="' + id + '" fw-complex="' + name + '" ' + extraAttrStr + '>' + value + '</textarea>' +
					'</td><td><input type="button" id="__button_' + id + '" value="..." fw-complex="' + name + '" ' + disabledStatus + '/></td></tr></table></div>';
	$(html).insertAfter(elem);
	elem.remove();
	
	//$("#__button_" + id).off("click").on("click", {"id": id, "type": fileType, "preview": needPreview}, function(event){
	context.registerElementListener($("#__button_" + id), "click", {"id": id, "type": fileType, "preview": needPreview}, function(event){
		var id = event.data.id;
		var type = event.data.type;
		var preview = event.data.preview;
		
		$.fileManager.selectFile(type, preview, function(selectedFile){
			
			if(!selectedFile)
			{
				selecteFile = "";
			}
			
			//$("#" + id + "_display").val(selectedFile.name);
			$("#" + id).val(JSON.stringify(selectedFile));
			$("#" + id).trigger('change');
		});
	});
	
	//$("#" + id).on("change", {"id": id}, function(event){
	context.registerElementListener($("#" + id), "change", {"id": id}, function(event){
		var eventData = event.data;
		var value = $.trim($(this).val());
		var id = eventData.id;
		
		if(value.length == 0)
		{
			$("#" + id + "_display").val("");
			return;
		}
		
		var valueObj = $.parseJSON(value);
		$("#" + id + "_display").val(valueObj.name);
	});
	
	$("#" + id).trigger('change');
});
*/


$.addDirective("data-grid", function(idx, domElem){
	var elem = $(domElem);
	var id = elem.attr("id");
	
	if(!id)
	{
		console.error("Mandatory attribute 'id' is not specified for <data-grid>");
		return;
	}
	
	var width = elem.attr('width')? elem.attr('width') : null;
	var height = elem.attr('height')? elem.attr('height') : "100px";
	var title = elem.attr('title')? elem.attr('title') : "Title";
	var resizable = elem.attr('resizable')? elem.attr('resizable') : false;
	var cssStyle = elem.attr('style')? elem.attr('style') : true;
	var cssClass = elem.attr('class')? elem.attr('class') : true;
	
	var gridConfig = {"height": height, "title": title, "resizable": resizable};
	
	if(width)
	{
		gridConfig["width"] = width;
	}
	else
	{
		gridConfig["width"] = "auto";
	}
	
	gridConfig.colModel = new Array();
	
	$.each(elem.children("column"), function(idx, domChild){
		var child = $(domChild);
		var title = child.attr("title")? child.attr("title") : "";
		var eidtable = ("true" == child.attr("editable"));
		var align = child.attr("title")? child.attr("align") : "left";
		var dataType = child.attr("dataType")? child.attr("dataType") : "string";
		var colWidth = child.attr("width")? child.attr("width") : 100;
		
		gridConfig.colModel.push({
			"title": title,
			"editable": eidtable,
			"align": align,
			"dataType": dataType,
			"width": colWidth
		});
	});
	
	gridConfig.dataModel = {data: []};
	gridConfig.dataModel.data.push(['<img src="/RealEstate/images/paint-brushes.png"/>', "Name", "address", 2, 3]);
	
	$('<div id="' + id + '" style="' + cssStyle + '" class="' + cssClass + '"></div>').insertAfter(elem);
	elem.remove();
	
	$("#" + id).pqGrid(gridConfig);
	$("#" + id).css("width", gridConfig["width"]);
	
	var parent = $("#" + id).parent().get(0);
	
	$("#" + id).css("height", $(parent).height());
	setTimeout(function(id){$("#" + id).pqGrid( "refresh" );}, 4000, id);
});


$.addValidator("required", function(elem, event, data){
	if(data[VALIDATOR_ATTR_VALUE] != "true")
	{
		return true;
	}
	
	var value = elem.val();
	
	if(!value || $.trim(value) == "")
	{
		return false;
	}
	
	return true;
});

$.addValidator("pattern", function(elem, event, data){
	//For full match the pattern should be of format ^abc$
	
	var pattern = new RegExp(data[VALIDATOR_ATTR_VALUE]);
	
	var value = elem.val();
	
	if(value == "")
	{
		return true;
	}
	
	if(!pattern.test(value))
	{
		return false;
	}
	
	return true;
});

$.addValidator("mispattern", function(elem, event, data){
	//For full match the pattern should be of format ^abc$
	
	var pattern = new RegExp(data[VALIDATOR_ATTR_VALUE]);
	
	var value = elem.val();
	
	if(value == "")
	{
		return true;
	}
	
	if(pattern.test(value))
	{
		return false;
	}
	
	return true;
});

$.addValidator("minLength", function(elem, event, data){
	var minLen = parseInt(data[VALIDATOR_ATTR_VALUE]);
	var value = elem.val();
	
	if(value.length() < minLen)
	{
		return false;
	}
	
	return true;
});

$.addValidator("maxLength", function(elem, event, data){
	var maxLen = parseInt(data[VALIDATOR_ATTR_VALUE]);
	var value = elem.val();
	
	if(value.length() > maxLen)
	{
		return false;
	}
	
	return true;
});

$.addValidator("matchWith", function(elem, event, data){
	var otherFld = $("#" + data[VALIDATOR_ATTR_VALUE]);
	
	if(!otherFld)
	{
		console.error("Invalid match-element-id '" + data[VALIDATOR_ATTR_VALUE] + "' specified for field: " + elem.attr("name"));
		return;
	}
	
	var value = elem.val();
	var otherValue = otherFld.val();
	
	if(value != otherValue)
	{
		return false;
	}
	
	return true;
});


function getMaxHeight(element)
{
	element = $(element);
	var parent = element.parent();
	
	var height = parent.innerHeight() - element.position().top;
	
	return height;
}

