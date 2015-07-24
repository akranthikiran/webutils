
/**
 * modelToUpdate - Should be plain model object, that needs to be updated
 * 
 * Config Fields
 * 		 skipFields[]			Array of model field names that needs to be skipped
 * 		 fieldConfig{}			Customized configuration for fields
 * 			disabled				"true"/"false". Marks the field as disabled by default
 * 			rows					For multi-lines text number of rows
 * 		defaultValues{}			Name to value map for specifying default values (this will override the model values during update operation)
 * 		propertyChangeListener	Property change listener for the model under create/update
 * 									In callback event, the container of the fields is passed as "modelContainer"
 * 		width					Width of the dialog. Can be in % of document width, like 80%. Height is auto-adjusted depdending on fields and document height
 * 		title					Customized title for dialog
 * 		actionButtonLabel		Label for action button
 * 		actionButtonStyle		Style for action button
 * 		actionButtonClass		Class for action button
 * 
 * 		cancelButtonLabel		Cancel button label
 * 		cancelButtonStyle		Cancel button style
 * 		cancelButtonClass		Cancel button class
 * 		ignoreExtraValues		Ignores extra mappings in default values
 */
$.displayModelForm = function(modelTypeName, callBack, modelToUpdate, configuration){
	var modelType = $["__objectTypes"][modelTypeName]; 
	
	if(!modelType)
	{
		throw "Invalid/unknown model-type specified: " + modelTypeName;
	}
	
	if(!configuration)
	{
		configuration = {"skipFields": [], "fieldConfig" : {}, "propertyChangeListener": null};
	}
	else
	{
		configuration.skipFields = configuration.skipFields ? configuration.skipFields : [];
		configuration.fieldConfig = configuration.fieldConfig ? configuration.fieldConfig : {};
	}
	
	var templateContext = {"modelType": modelType, "__forUpdate": false, "config": configuration, "modelToUpdate": modelToUpdate};
	
	var form = $("#__displayModelForm");
	
	if(!form || form.length == 0)
	{
		$('body').append('<div id="__displayModelForm"></div>');
		form = $("#__displayModelForm");
	}
	
	form.empty();
	
	var dlgTitle = null;
	
	if(modelToUpdate)
	{
		mergeExtendedFields(modelToUpdate, modelType);
		
		dlgTitle = "Update " + modelType.label;
		templateContext["__forUpdate"] = true;
	}
	else
	{
		dlgTitle = "Add " + modelType.label;
	}
	
	if(configuration.title)
	{
		dlgTitle = configuration.title;
	}
	
	$.templateEngine.setContext(templateContext);
	$.templateEngine.processTemplate("addModelContents", "#__displayModelForm");
	$.parseDirectives("#__displayModelForm");

	var modelContainer = $('#__displayModelForm [fw-model="' + modelTypeName + '"]');
	var model = modelContainer.data(DATA_ATTR_MODEL);

	if(modelToUpdate)
	{
		//TODO: Check if we can use copyTo function instead of explicit copy of properties
		var fields = modelType.fields;
		var value = null;
		
		for(var i = 0; i < fields.length; i++)
		{
			value = modelToUpdate[fields[i].name];
			
			if(!isDefined(value))
			{
				continue;
			}
			
			try
			{
				if(fields[i].type == 'date')
				{
					value = parseServerDate(value);
				}
				
				model["setters"][fields[i].name](value);
			}catch(ex)
			{
				console.error(ex);
				
				if(ex.message)
				{
					ex = ex.message;
				}
				
				console.error("Error in setting '" + fields[i].name + "' for model-type '" + modelTypeName + "'. Error: " + ex);
			}
		}
	}
	else if(model)
	{
		model.reset();
	}
	
	if(configuration.defaultValues)
	{
		for(var fld in configuration.defaultValues)
		{
			try
			{
				model["setters"][fld](configuration.defaultValues[fld]);
			}catch(ex)
			{
				if(configuration.ignoreExtraValues == true)
				{
					continue;
				}
				
				console.error("An error occurred while setting default value field: " + fld);
				console.error(ex);
			}
		}
	}
	
	if(configuration.propertyChangeListener && $.isFunction(configuration.propertyChangeListener))
	{
		model.addPropertyChangeListener("displayModelFormListener", {"modelContainer":  modelContainer}, configuration.propertyChangeListener);
	}
	
	configuration.width = isDefined(configuration.width) ? configuration.width : 400;
	
	var widthStr = "" + configuration.width;
	
	if(widthStr.indexOf("%") == (widthStr.length - 1))
	{
		configuration.width = parseInt(widthStr.substr(0, widthStr.length - 1));
		configuration.width = $(document).width() * configuration.width / 100;
	}
	
	$("#__displayModelForm").dialog({
		width: configuration.width,
		height: $(document).height(),
		title: dlgTitle,
		autoOpen: false,
		modal: true,
		resizeable: false
	});
	
	$("#__displayModelForm input[type='button'][name='createObject']").off("click").on("click", 
					{"modelName": modelTypeName, "callBack": callBack, "configuration": configuration}, function(event){
		var eventData = event.data;
		var modelName = eventData.modelName;
		var callBack = eventData.callBack;
		var configuration = eventData.configuration;
		
		var modelContainer = $('#__displayModelForm [fw-model="' + modelName + '"]');
		var model = modelContainer.data(DATA_ATTR_MODEL);

		try
		{
			model.validate();
		}catch(err)
		{
			$.error("Please correct below errors and try!" +
					'<BR/>Field: ' + err.fieldLabel + 
					'<BR/>Value Specified: ' + err.value +
					'<BR/>Error: <span style="font-weight:bold;color: red;">' + err.message + "</span>");
			return;
		}

		var mssg = null;

		if(callBack)
		{
			try
			{
				mssg = callBack(model, modelToUpdate);
			}catch(ex)
			{
				$.error("Please correct below errors and try!" +
						'<BR/>Error: ' + ex); 
				return;
			}
		}
		
		//remove property change listener on model if any
		if(configuration.propertyChangeListener && $.isFunction(configuration.propertyChangeListener))
		{
			model.removePropertyChangeListener("displayModelFormListener");
		}
		
		$("#__displayModelForm").dialog("close");
		
		if(mssg)
		{
			$.alert(mssg);
		}
		
	});
	
	$("#__displayModelForm input[type='button'][name='cancelCreate']").off("click").on("click", 
				{"configuration": configuration}, function(event){
		var eventData = event.data;
		var configuration = eventData.configuration;
					
		//remove property change listener on model if any
		if(configuration.propertyChangeListener && $.isFunction(configuration.propertyChangeListener))
		{
			model.removePropertyChangeListener("displayModelFormListener");
		}
		
		$("#__displayModelForm").dialog("close");
	});
	
	$("#__displayModelForm").dialog("open");
	
	var dlgHeight = $("#__displayModelForm .contentLayer").height() + //content layer height
				$("#__displayModelForm .buttonLayer").height() +  //button layer height
				($("#__displayModelForm").parent().children(".ui-dialog-titlebar").height() * 3); //title bar height and 2 for padding
	
	if(dlgHeight >= $(document).height())
	{
		dlgHeight = $(document).height();
	}
	
	$("#__displayModelForm").dialog({
		height: dlgHeight
	});
	
	$("#__displayModelForm .contentLayer").css("bottom", "3em");
};

/*
 * Function that enables multi selection of LOV types
 */
$.displayModelForm["displayLovMultiSelectionDialog"] = function(fieldUiDivLocator){
	
	//find the field ui element from locator
	var modelForm = $('#__displayModelForm');
	var fieldUiDiv = modelForm.find(fieldUiDivLocator);
	
	//from the field determine the field-name and the model
	var fieldName = fieldUiDiv.attr("fw-field-name");
	var model = findModelForElement(fieldUiDiv);
	
	
	//get and validate field details
	var fieldDetails = model.getFieldDetails(fieldName);
	
	if(!fieldDetails || fieldDetails.fieldType != 'LIST_OF_VALUES' || !fieldDetails.multiValued)
	{
		throw "Invalid/non-lov/non-multivalued field specifiedfor lov-multi selection - " + fieldName;
	}
	
	//get or create multi selection form. Then clean the existing ui and releate data/listeners
	var form = $("#__displayLovMultiSelectForm");
	
	if(!form || form.length == 0)
	{
		$('body').append('<div id="__displayLovMultiSelectForm"></div>');
		form = $("#__displayLovMultiSelectForm");
	}
	
	form.empty();
	
	//compute the label for dialog
	var dlgTitle = fieldDetails.label ? fieldDetails.label : fieldDetails.name;
	
	var fieldValues = model["getters"][fieldName]();
	
	if(!fieldValues)
	{
		fieldValues = [];
	}
	
	//process the template and populate the ui in dialog as per the field
	$.templateEngine.setContext({
		"fieldType":  fieldDetails,
		"selectedValues": fieldValues 
	});
	
	$.templateEngine.processTemplate("lovMultiSelectDialog", "#__displayLovMultiSelectForm");
	$.parseDirectives("#__displayLovMultiSelectForm");
	
	var moveItems = function(source, dest, selectedOnly) {
		var selectedCount = 0;
		var itemQuery = selectedOnly ? ":selected" : "option";
		
		source.find(itemQuery).each(function(){
			var option = $(this);
			
			dest.append($("<option></option>").attr("value", option.val()).html(option.html() ) );
			option.remove();
			
			selectedCount++;
	    });
		
		return (selectedCount > 0);
	};
	
	//add the functionality for the buttons inside the form dialog
	form.find(".moveRight").on("click", {"moveItems": moveItems}, function(event){
		var availableLst = $("#__displayLovMultiSelectForm .avaiableOptions");
		var selectedLst = $("#__displayLovMultiSelectForm .selectedOptions");

		if(!event.data.moveItems(availableLst, selectedLst, true))
		{
			$.alert("Please select items in available list that needs to be moved to selected list and then try!");
		}
	});
	
	form.find(".moveLeft").on("click", {"moveItems": moveItems}, function(event){
		var availableLst = $("#__displayLovMultiSelectForm .avaiableOptions");
		var selectedLst = $("#__displayLovMultiSelectForm .selectedOptions");

		if(!event.data.moveItems(selectedLst, availableLst, true))
		{
			$.alert("Please select items in selected list that needs to be removed and then try!");
		}
	});
	
	form.find(".moveAllRight").on("click", {"moveItems": moveItems}, function(event){
		var availableLst = $("#__displayLovMultiSelectForm .avaiableOptions");
		var selectedLst = $("#__displayLovMultiSelectForm .selectedOptions");
		
		event.data.moveItems(availableLst, selectedLst, false);
	});
	
	form.find(".moveAllLeft").on("click", {"moveItems": moveItems}, function(event){
		var availableLst = $("#__displayLovMultiSelectForm .avaiableOptions");
		var selectedLst = $("#__displayLovMultiSelectForm .selectedOptions");
		
		event.data.moveItems(selectedLst, availableLst, false);
	});
	
	form.find(".okayButton").on("click", {"model": model, "fieldName": fieldName, "fieldUiDiv": fieldUiDiv}, function(event){
		var data = event.data;
		var selectedLst = $("#__displayLovMultiSelectForm .selectedOptions");

		//remove all values of field from model
		data.model["removers"][data.fieldName]();
		
		var html = "";
		
		//from selected options add values to model and to the field ui
		selectedLst.find("option").each(function(){
			var option = $(this);

			data.model["adders"][data.fieldName](option.val());
			html += option.html() + "<BR/>";
	    });
		
		//set new html to field ui
		data.fieldUiDiv.html(html);		
		$("#__displayLovMultiSelectForm").dialog("close");
	});
	
	form.find(".cancelButton").on("click", function(){
		$("#__displayLovMultiSelectForm").dialog("close");
	});
	
	form.dialog({
		width: $(document).width() * 0.6,
		height: 300,
		title: dlgTitle,
		autoOpen: false,
		modal: true,
		resizeable: false
	});
	
	form.dialog("open");
};
