var ELEMENT_NODE = 1;
var TEXT_NODE = 3;
var COMMENT_NODE = 8;

var PROCESS_FUNC = "processFunc";
var IF_RESULT = "ifResult";
var IF_RESULT_SUCCESS = "success";
var IF_RESULT_FAILURE = "failure";

var PARSE_RESULT = "___parseExpressionResult";
var PARSE_RESULT_FULL_OBJECT = 1;
var PARSE_RESULT_STRING = 2;

var LOOP_CONTROL_BREAK = "BREAK";
var LOOP_CONTROL_CONTINUE = "CONTINUE";
var BREAK_CONTROL_EXCEPTION = {"loopControl": true, "controlType": LOOP_CONTROL_BREAK};
var CONTINUE_CONTROL_EXCEPTION = {"loopControl": true, "controlType": LOOP_CONTROL_CONTINUE};

function TemplateEngine()
{
	//this.previousElem = null;
	this.controlNodes = new Object();
	
	this.templateContext = {previousElem: null};
	
	this.setContext = function(context){
		this.templateContext = new Object();
		$.extend(this.templateContext, context);
	};
	
	this.controlNodes['if'] = ['test'];
	this.controlNodes['if'][PROCESS_FUNC] = function(elem, attrVals, processChildren, templateContext){
		var cond = attrVals["test"];
		var parseType = attrVals["test" + PARSE_RESULT];
		var context = templateContext;
		var res = (parseType == PARSE_RESULT_FULL_OBJECT)? cond: eval(cond);
		
		if(res)
		{
			processChildren(elem.contents(), templateContext);
			elem.data(IF_RESULT, IF_RESULT_SUCCESS);
		}
		else
		{
			elem.data(IF_RESULT, IF_RESULT_FAILURE);
		}
	};
	
	this.controlNodes['else-if'] = ['test'];
	this.controlNodes['else-if'][PROCESS_FUNC] = function(elem, attrVals, processChildren, templateContext){
		var ifCondRes = $(templateContext.previousElem).data(IF_RESULT);
		
		if(!ifCondRes)
		{
			throw "A <else-if> condition is used without matching <if>";
		}
		
		if(ifCondRes == IF_RESULT_SUCCESS)
		{
			elem.data(IF_RESULT, IF_RESULT_SUCCESS);
			return;
		}
		
		var cond = attrVals["test"];
		var parseType = attrVals["test" + PARSE_RESULT];
		var context = templateContext;
		var res = (parseType == PARSE_RESULT_FULL_OBJECT)? cond: eval(cond);
		
		if(res)
		{
			processChildren(elem.contents(), templateContext);
			elem.data(IF_RESULT, IF_RESULT_SUCCESS);
		}
		else
		{
			elem.data(IF_RESULT, IF_RESULT_FAILURE);
		}
	};
	
	
	this.controlNodes['else'] = [];
	this.controlNodes['else'][PROCESS_FUNC] = function(elem, attrVals, processChildren, templateContext){
		var ifCondRes = $(templateContext.previousElem).data(IF_RESULT);
		
		if(!ifCondRes)
		{
			throw "A <else> condition is used without matching <if>";
		}
		
		if(ifCondRes == IF_RESULT_SUCCESS)
		{
			return;
		}
		
		processChildren(elem.contents(), templateContext);
	};

	
	this.controlNodes['for-each'] = ['data', 'loop-var', 'index-var'];
	this.controlNodes['for-each'][PROCESS_FUNC] = function(elem, attrVals, processChildren, templateContext){
		var data = attrVals["data"];
		
		if(!data)
		{
			return;
		}
		
		if(!$.isPlainObject(data) && !$.isArray(data))
		{
			console.error("Invalid object/array specified fro 'data' of <for-each>: " + data);
			return;
		}
		
		var loopVar = attrVals["loop-var"];
		var indexVar = attrVals["index-var"];
		var val = null;
		
		for(var idx = 0; idx < data.length; idx++)
		{
			val = data[idx];
			templateContext[loopVar] = val;
			templateContext[indexVar] = idx;
			
			try
			{
				processChildren(elem.contents(), templateContext);
			}catch(ex)
			{
				if(ex["loopControl"])
				{
					if(ex["controlType"] == LOOP_CONTROL_BREAK)
					{
						break;
					}
					else if(ex["controlType"] == LOOP_CONTROL_CONTINUE)
					{
						continue;
					}
				}
				
				throw ex;
			}
		};
	};

	this.controlNodes['for'] = ['start', 'end', 'loop-var'];
	this.controlNodes['for'][PROCESS_FUNC] = function(elem, attrVals, processChildren, templateContext){
		var start = parseInt(attrVals["start"]);
		var end = parseInt(attrVals["end"]);
		var loopVar = attrVals["loop-var"];
		
		for(var i = start; i < end; i++)
		{
			templateContext[loopVar] = i;
			
			try
			{
				processChildren(elem.contents(), templateContext);
			}catch(ex)
			{
				if(ex["loopControl"])
				{
					if(ex["controlType"] == LOOP_CONTROL_BREAK)
					{
						break;
					}
					else if(ex["controlType"] == LOOP_CONTROL_CONTINUE)
					{
						continue;
					}
				}
				
				throw ex;
			}

		}
	};
	
	this.controlNodes['break'] = [];
	this.controlNodes['break'][PROCESS_FUNC] = function(elem, attrVals, processChildren, templateContext){
		throw BREAK_CONTROL_EXCEPTION;
	};
	
	this.controlNodes['continue'] = [];
	this.controlNodes['continue'][PROCESS_FUNC] = function(elem, attrVals, processChildren, templateContext){
		throw CONTINUE_CONTROL_EXCEPTION;
	};
	
	this.controlNodes['set-var'] = ['name', 'expr'];
	this.controlNodes['set-var'][PROCESS_FUNC] = function(elem, attrVals, processChildren, templateContext){
		var name = attrVals["name"];
		
		if(name.indexOf("_") != 0)
		{
			throw "Invalid var name used in <set-var>. Var name should start with underscore (_). Var name used: " + name;
		}
		
		var context = templateContext;
		var exprVal = eval(attrVals["expr"]);
		
		templateContext[name] = exprVal;
	};
	
	this.getTemplate = function(name) {
		if(this.templates)
		{
			return this.templates[name];
		}
		
		this.templates = new Object();
		
		var templatesUrls = $.configuration["templatesUrls"];
		
		if(!templatesUrls)
		{
			throw "No template url(s) is configured. Use $.setConfiguration('templatesUrl')";
		}
		
		if(!$.isArray(templatesUrls))
		{
			templatesUrls = [templatesUrls];
		}
		
		var templatesParent = null;
		var children = null;
		var childName = null;
		var child = null;
		
		for(var t = 0; t < templatesUrls.length; t++)
		{
			templatesParent = makeJsonCall(templatesUrls[t], null, {cache: false, dataType: "xml"});
			templatesParent = templatesParent.documentElement;
			children = templatesParent.children;
			
			for(var i = 0; i < children.length; i++)
			{
				if(children[i].nodeName == "template")
				{
					child = $(children[i]);
					childName = child.attr("name");
					
					this.templates[childName] = child;
				}
			}
		}
		
		return this.templates[name];
	};
	
	
	this.processTemplate = function(templateName, targetSelector){
		try
		{
			var children = this.getTemplate(templateName).contents();
			return this.processTemplateContents(children, targetSelector);
		}catch(ex)
		{
			console.error("An error occurred while processing template: " + templateName);
			console.error(ex);
			
			throw "An error occurred while processing template: '" + templateName + "'. Error: " + ex;
		}
	};
		
	this.processTemplateContents = function(children, targetSelector){
		
		var processElement = null;
		var templateEngine = this;
		
		this.templateContext["$res"] = "";
		
		var processChildren = function(children, templateContext){
			
			if(!children)
			{
				return;
			}
			
			$.each(children, function(idx, child){
				processElement(child, templateContext);
			});
		};
		
		var processControlElement = function(domElem, templateContext){
			var expectedArgs = templateEngine.controlNodes[domElem.nodeName.toLowerCase()];
			var attrVals = new Object();
			var elem = $(domElem);
			
			//Validate required attributes are present
			for(var i = 0; i < expectedArgs.length; i++)
			{
				if(!elem.attr(expectedArgs[i]))
				{
					throw "Expected attribute '" + expectedArgs[i] + "' is not specified in <" + elem.nodeName + '>';
				}
				
				attrVals[expectedArgs[i]] = parseExpressions(elem.attr(expectedArgs[i]), templateContext);
				attrVals[expectedArgs[i] + PARSE_RESULT] = templateContext[PARSE_RESULT];
			}
			
			templateEngine.controlNodes[domElem.nodeName.toLowerCase()][PROCESS_FUNC](elem, attrVals, processChildren, templateContext);
			
			templateContext.previousElem = elem;
		};
		
		var processNormalElement = function(element, templateContext){
			
			if(element.tagName.toLowerCase() == "br")
			{
				templateContext["$res"] += "<BR/>";
				return;
			}
			
			templateContext["$res"] += '<' + element.tagName + ' ';
			
			if(element.attributes)
			{
				$.each(element.attributes, function(idx, attr){
					var attrVal = attr.value;
					
					attrVal = parseExpressions(attrVal, templateContext);
					templateContext["$res"] += attr.name + '="' + attrVal + '" '; 
				});
			}
			
			templateContext["$res"] += '>';
			
			processChildren($(element).contents(), templateContext);
			
			templateContext["$res"] += '</' + element.tagName + '>';
		};
		
		processElement = function(element, templateContext){
			if(element.nodeType == ELEMENT_NODE)
			{
				if(templateEngine.controlNodes[element.nodeName.toLowerCase()])
				{
					processControlElement(element, templateContext);
				}
				else
				{
					processNormalElement(element, templateContext);
				}
				
				templateContext.previousElem = element;
				return;
			}
			
			if(element.nodeType == TEXT_NODE)
			{
				templateContext["$res"] += parseExpressions(element.nodeValue, templateContext);
			}
			
			return;
		};
		
		processChildren(children, this.templateContext);
		
		if(targetSelector) 
		{
			$(targetSelector).html(this.templateContext["$res"]);
		}
		
		return this.templateContext["$res"];
	};
	
}

$.templateEngine = new TemplateEngine();


function parseExpressions(template, params, throwError){
	if(!template)
	{
		params[PARSE_RESULT] = PARSE_RESULT_STRING; 
		return "";
	}
	
	var fullPattern = /^\{\{([\w\-\.\(\)\,]+\$?)\}\}$/;
	var res = null;
	
	var parseExpresion = function(expression){
		var stringExpr = (expression.indexOf("$") > 0);
		expression = stringExpr? expression.substr(0, expression.length - 1) : expression;
		
		//set var context so that it is available in eval expressions
		var context = params;
		
		//add "context." prefix to expression, so that all expressions refer to context variable above
		expression = "context." + expression;
		
		var res = null;
		
		try
		{
			res = eval(expression);
		}catch(ex)
		{
			console.error("An error occurred while parsing expression '" + expression + "'. Error: " + ex);
		}
		
		if(!res && stringExpr)
		{
			res = "";
		}
		
		return res;
		/*
		var parts = expression.split(".");
		var current = params;
		
		for(var i = 0; i < parts.length; i++)
		{
			if(!current || !current[parts[i]])
			{
				if(stringExpr)
				{
					return "";
				}
				
				return null;
			}
			
			current = current[parts[i]];
		}
		
		return current;
		*/
	};
	
	if((res = fullPattern.exec(template)))
	{
		params[PARSE_RESULT] = PARSE_RESULT_FULL_OBJECT;
		return parseExpresion(res[1]);
	}
	
	params[PARSE_RESULT] = PARSE_RESULT_STRING;
	
	var pattern = /\{\{([\w\-\.\[\]\(\)\,\/\"\'\<\>]+\$?)\}\}/g;
	res = template.replace(pattern, function(match, p1){
		var val = parseExpresion(p1);
		
		if(!val && throwError)
		{
			throw "Failed to fetch value for property: " + p1;
		}
		
		return val;
	});
	
	return res;
};
