var PAGE_HOME = "home";

/*
 * Single Page Application controller - SPA (Single Page Application)
 */
$.addController("SPAController", {
	name: "SPA Controller",
	pathToConfiguration: {},
	pageToConfiguration: {},
	currentPage: null,
	
	componentToLocation: {},
	
	initListeners: [],
	initialized: false,

	//on ready call back
	_onFwReady: function(){
		var spaConfiguration = $.getConfiguration("spa-views");
		
		for(var i = 0; i < spaConfiguration.length; i++)
		{
			this.pathToConfiguration[spaConfiguration[i].path] = spaConfiguration[i];
			this.pageToConfiguration[spaConfiguration[i].page] = spaConfiguration[i];
		}
		
		if(!this.pageToConfiguration[PAGE_HOME])
		{
			throw "No home SPA page is configured!";
		}
		
		var location = this.getLocation();
		var pageConfig = this.pathToConfiguration[location];
		var pageName = pageConfig ? pageConfig.page : PAGE_HOME;
		
		pageConfig = this.pageToConfiguration[pageName];
		
		this.openPage(pageName, true);
		
		var event = {"page": pageName, "configuration": pageConfig};
		
		for(var i = 0; i < this.initListeners.length; i++)
		{
			try
			{
				this.initListeners[i](event);
			}catch(ex)
			{
				console.error("An error occurred while invoking SPA initialization listeners");
				console.error(ex);
			}
		}
		
		this.initialized = true;
	},
	
	//private method
	getLocation: function() {
		var location = document.location.pathname;
		var start = 0;
		
		if($.contextPath != "/")
		{
			location = location.substr($.contextPath.length);
		}
		
		var end = location.indexOf("?");
		
		if(end > 0)
		{
			location = location.substr(0, end);
		}
		
		return location;
	},
	
	//private method
	loadUrl: function(id, url) {
		var componentToLocation = this.componentToLocation;
		var currentLocation = componentToLocation[id];
		
		if(currentLocation == url)
		{
			return;
		}

		$.unregisterDirectives("#" + id);
		$("#" + id).empty();

		$.ajax({
			"url": url,
			async: false,
			cache: false,
			dataType: "html",
			//context: $("#" + arguments[i]),
			context: {"id": id, "url": url, "componentToLocation": componentToLocation},
			success: function(data){
				var selector = "#" + this.id;
				var url = this.url;
				var targetElem = $(selector);
				
				targetElem.html(data);

				targetElem.attr(DATA_ATTR_CURRENT_LOCATION, url);
				this.componentToLocation[this.id] = url;
				
				$.parseDirectives(selector);
			}
		});
	},
	
	//private method
	setSPAStyles: function(oldPage, newPage) {
		var styleElements = $("[fw-spa-style-expr]");
		var spaController = this;
		
		styleElements.each(function(idx, elem){
			elem = $(elem);
			var styleExpr = elem.attr("fw-spa-style-expr");
			
			var attributes = spaController.pageToConfiguration[newPage].attributes;
			attributes["page"] = newPage;
			
			var newClass = eval(styleExpr);
			
			if(oldPage)
			{
				attributes = spaController.pageToConfiguration[oldPage].attributes;
				attributes["page"] = oldPage;
				
				var oldClass = eval(styleExpr);
				
				if(oldClass)
				{
					elem.removeClass(oldClass);
				}
			}
			
			if(newClass)
			{
				elem.addClass(newClass);
			}
		});
	},
	
	openPage: function(page, internalCall)
	{
		if(!internalCall && !this.initialized)
		{
			return;
		}
		
		var configuration = this.pageToConfiguration[page];
		
		if(!configuration)
		{
			console.error("No SPA configuration found for page: " + page + ". Moving to default page");
			configuration = this.pageToConfiguration[PAGE_HOME];
			page = PAGE_HOME;
		}
		
		var element = null;
		var components = configuration.components;
		var contextPath = ($.contextPath == "/") ? "" : $.contextPath;
		
		for(var elemId in components)
		{
			element = $("#" + elemId);
			
			if(element.length == 0)
			{
				console.error("No element with id '" + elemId + "' found. Specified in SPA configuration of page: " + page);
				continue;
			}
			
			this.loadUrl(elemId, contextPath + components[elemId]);
		}
		
		var state = {"canBeAnything": true};
		history.pushState(state, "Real Estate Data Manager", contextPath + configuration.path);
		
		this.setSPAStyles(this.currentPage, page);
		this.currentPage = page;
	},
	
	
	addInitListener: function(listener) {
		
		if(!$.isFunction(listener))
		{
			throw "Non-function specified as listener. Specified listener: " + listener;
		}
		
		this.initListeners.push(listener);
	},
});