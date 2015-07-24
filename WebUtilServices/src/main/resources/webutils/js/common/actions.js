$.addController("ActionsController", {
	name: "Actions Controller",
	nameToAction: {},
	PARAM_PATTERN: /\{(\w+)\}/g,
	
	_onReady: function(){
		var actionList = null;
		
		try
		{
			actionList = makeJsonCall($.getConfiguration("actionsUrl"));
		}catch(ex)
		{
			console.error("An error occurred while fetching action details.");
			console.error(ex);
			return;
		}

		for(var i = 0; i < actionList.length; i++)
		{
			this.nameToAction[actionList[i].name] = actionList[i];
		}
	},
	
	getActionUrl: function(actionName, urlParams) {
		var action = this.nameToAction[actionName];
		
		if(!action)
		{
			throw "Invalid action name specified: " + actionName;
		}
		
		var url = action.url;
		var index = 0;
		
		url = url.replace(this.PARAM_PATTERN, function(match, p1){
			
			if(!urlParams)
			{
				throw "No url parameters provided when expected for action: " + actionName;
			}
			
			if($.isArray(urlParams))
			{
				if(index >= urlParams.lengh)
				{
					throw "Insufficient number of url parameters passed for action: " + actionName;
				}

				var idx = index;
				index++;
				
				return urlParams[idx];
			}
			else if($.isObject(urlParams))
			{
				var res = urlParams[p1];
				
				if(!res)
				{
					throw "No value provided for url parameter '" + p1 + "' for action: " + actionName;
				}
				
				return res;
			}
			else
			{
				throw "Non-array and non-object passed for url params for action: " + actionName;
			}
		});
		
		return url;
	},
	
	invokeAction: function(actionName, urlParams, content, config) {
		var url = this.getActionUrl(actionName, urlParams);
		var action = this.nameToAction[actionName];
		
		if(config)
		{
			config.methodType = action.method;
		}
		else
		{
			config = {"methodType": action.method};
		}

		if(action.bodyExpected)
		{
			return makeJsonBodyCall(url, content);
		}
		else
		{
			return makeJsonCall(url, content, config);
		}
	}
});