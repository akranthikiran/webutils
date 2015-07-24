var SEARCH_RESULTS_ID = "searchResultsId";
var SEARCH_RESULTS_EXPR = "searchResultsExpr";
var SEARCH_RESULTS_DATA = "searchResultsData";
var SEARCH_RESULTS_RESULTS = "searchResultsResults";

$.searchResults = {};

function executeSearchQuery(searchEvent)
{
	//fetch attributes from search query event
	var eventData = searchEvent.data;
	var searchQueryId = eventData["searchQueryId"];
	var searchResultsId = eventData["searchResultsId"];
	var searchResultExpr = eventData["searchResultExpr"];
	var modelType = eventData["modelType"];
	var modelServerType = eventData["modelServerType"];
	var searchUrl = eventData["searchUrl"];
	
	//search all the fields from search query widget
	var prefix = "#" + searchQueryId;
	
	//try to create model out of the config built
	var model = $(prefix).data(DATA_ATTR_MODEL);
	
	try
	{
		//model = $.create(modelType, modelConfig);
		model.validate();
	}catch(err)
	{
		//in case of error prompt to user and return
		$.error("Please correct below errors and then try!" +
				'<BR/>Field: ' + err.field + 
				'<BR/>Value Specified: ' + err.value +
				'<BR/>Error: <span style="font-weight:bold;color: red;">' + err.message + "</span>");
		return;
	}
	
	if(!searchUrl)
	{
		searchUrl = $.configuration.queryUrl;
	}

	//make the server call with the model object
	var results = makeJsonCall(searchUrl, {queryType: modelServerType, formFieldContent: model.toJson()});
	var event = {"results": results, 
			"searchQueryId": searchQueryId,
			"modelType": modelType,
			"modelServerType": modelServerType,
			
			"getColumn": function(row, column) {
				if(row < 0 || row >= this.results.recordCount)
				{
					return null;
				}
				
				var index = this.results.columns.indexOf(column);
				
				if(index < 0)
				{
					return null;
				}
				
				return this.results.records[row].values[index];
			}
	};
	
	if(searchResultsId)
	{
		var resultAttributes = $("#" + searchResultsId).data("attributes");
		
		if(resultAttributes["onChange"])
		{
			try
			{
				invokeDynamicMethod($("#" + searchResultsId).get(), resultAttributes["onChange"], event);
			}catch(ex)
			{
				console.error("An error occurred while invoking search result on-change event. Method: " + resultAttributes["onChange"]);
				console.error(ex);
			}
		}
		
		if(!results || results.recordCount <= 0)
		{
			$("#" + searchResultsId).css("display", "none");
			$("#__" + searchResultsId + "_empty").css("display", "block");
		}
		else
		{
			$("#__" + searchResultsId + "_empty").css("display", "none");
			$("#" + searchResultsId).css("display", "block");
			
			displaySearchResults(searchResultsId, results);
		}

		if(resultAttributes["onPostChange"])
		{
			try
			{
				invokeDynamicMethod($("#" + searchResultsId).get(), resultAttributes["onPostChange"], event);
			}catch(ex)
			{
				console.error("An error occurred while invoking search result on-change event: " + ex);
			}
		}
	}
	else
	{
		try
		{
			invokeDynamicMethod($("#" + searchQueryId).get(), searchResultExpr, event);
		}catch(ex)
		{
			console.error("An error occurred while invoking search result expression: " + ex);
		}
	}

	$("#" + searchQueryId + " input[type='hidden'][name='searchExecuted']").val("true");
}

function refreshSearchResults(searchQueryId, forcedRefresh)
{
	var searchExecuted = $("#" + searchQueryId + " input[type='hidden'][name='searchExecuted']").val();
	
	if(searchExecuted != "true" && !forcedRefresh)
	{
		return;
	}
	
	$("#" + searchQueryId + " input[type='button'][name='searchButton']").trigger('click');
}

function displaySearchResults(searchResultsId, results)
{
	var parent = $("#" + searchResultsId).parent();
	var resultsElem = $("#" + searchResultsId);
	var attributes = $("#" + searchResultsId).data("attributes");
	
	var gridData = {};
	gridData.width = parent.innerWidth() - resultsElem.position().left - attributes.leftMargin - attributes.rightMargin;
	gridData.height = parent.innerHeight() - resultsElem.position().top - attributes.topMargin - attributes.bottomMargin;
	gridData.title = attributes.title;
	gridData.editable = false;
	gridData.wrap = true;
	gridData.resizable = false;
	gridData.selectionModel = attributes.selectionModel;
	gridData.freezeCols = attributes.freezeColumns;
	gridData.oddRowsHighlight = true;
	
	gridData.colModel = new Array();
	
	var postColumns = null;
	
	if(attributes.postColumnProvider)
	{
		//postColumns = eval(attributes.postColumnProvider);
		var event = {"results": results};
		postColumns = invokeDynamicMethod(resultsElem.get(), attributes.postColumnProvider, event);
	}
	
	var colCount = results.columnCount;
	var fullColCount =  colCount + attributes["preColumns"].length + attributes["postColumns"].length;
	
	if(postColumns)
	{
		fullColCount += postColumns.length;
	}
		
	var colWidth = gridData.width / fullColCount;
	
	if(attributes.minColumnWidth && colWidth < attributes.minColumnWidth)
	{
		colWidth = attributes.minColumnWidth;
	}
	
	var i = 0;
	
	var colRenderer = null;
	
	for(i = 0; i < attributes["preColumns"].length; i++)
	{
		colRenderer = $.proxy(function(ui) {
			var context = {"row": ui.rowData};
			$.templateEngine.setContext(context);
			
			var templateHtml = $(this.html);
			return $.templateEngine.processTemplateContents(templateHtml);
		}, {"html": attributes["preColumns"][i].contents});
		
		gridData.colModel.push({"title": attributes["preColumns"][i].title, "width": colWidth, "render": colRenderer, "align": attributes["preColumns"][i].align});
	}
	
	for(i = 0; i < colCount; i++)
	{
		if(attributes.skipColumns.indexOf(results.columns[i]) >= 0)
		{
			continue;
		}
		
		gridData.colModel.push({"title": results.columns[i], "width": colWidth});
	}

	for(i = 0; i < attributes["postColumns"].length; i++)
	{
		colRenderer = $.proxy(function(ui) {
			var context = {"row": ui.rowData};
			$.templateEngine.setContext(context);
			
			var templateHtml = $(this.html);
			return $.templateEngine.processTemplateContents(templateHtml);
		}, {"html": attributes["postColumns"][i].contents});
		
		gridData.colModel.push({"title": attributes["postColumns"][i].title, "width": colWidth, "render": colRenderer, "align": attributes["postColumns"][i].align});
	}
	
	if(postColumns)
	{
		for(i = 0; i < postColumns.length; i++)
		{
			colRenderer = $.proxy(function(ui) {
				var context = {
						"row": this.results.records[ui.rowIndx], 
						"results": this.results, 
						"columnIndex": this.index,
						"getColumn": function(name){
							var index = this.results.columns.indexOf(name);
							
							if(index < 0)
							{
								return null;
							}
							
							return this.row.values[index];
						}
				};
				return this.renderer(context);
			}, {"results": results, "index": i, "renderer": postColumns[i].renderer});
			
			gridData.colModel.push({"title": postColumns[i].title, "width": colWidth, "render": colRenderer, "align": postColumns[i].align});
		}
	}

	var rowCount = results.recordCount;
	var data = new Array();
	gridData.dataModel = {"data": data};
	
	var row = null;
	var j = 0;
	var cellValue = null;
	
	for(i = 0; i < rowCount; i++)
	{
		row = new Array();
		data.push(row);
		
		for(j = 0; j < attributes["preColumns"].length; j++)
		{
			row.push("");
		}
		
		for(j = 0; j < colCount; j++)
		{
			if(attributes.skipColumns.indexOf(results.columns[j]) >= 0)
			{
				continue;
			}
			
			cellValue = results.records[i].values[j];
			
			if((typeof cellValue) == "string")
			{
				cellValue = cellValue.replace(/\n/g, "<BR/>");
			}

			row.push(cellValue);
		}

		for(j = 0; j < attributes["postColumns"].length; j++)
		{
			row.push("");
		}
		
		if(postColumns)
		{
			for(j = 0; j < postColumns.length; j++)
			{
				row.push("");
			}
		}
	}
	
	for(var i = 0; i < attributes.freezeColumns ; i++)
	{
		gridData.colModel[i].className = "fixedSearchResultColumns";
	}
	
	$("#" + searchResultsId).pqGrid(gridData);
	$("#" + searchResultsId).data(SEARCH_RESULTS_DATA, data);
	$("#" + searchResultsId).data(SEARCH_RESULTS_RESULTS, results);
	
	$("#" + searchResultsId).css("margin-left", attributes.leftMargin)
							.css("margin-top", attributes.topMargin);
	
	$.parseDirectives("#" + searchResultsId);
}

function getSelectedSearchResult(searchResultsId) {
	var grid = $("#" + searchResultsId);
	var gridData = grid.data(SEARCH_RESULTS_DATA);
	var searchResults = grid.data(SEARCH_RESULTS_RESULTS);
	
	//if no search is done previously
	if(!gridData)
	{
		return null;
	}
	
	var arr = grid.pqGrid( "selection", { type:'row', method:'getSelection' } );
	
	if(!arr || arr.length < 1)
	{
		return null;
	}
	
	var row = gridData[arr[0].rowIndx];
	var resultRecord = searchResults.records[arr[0].rowIndx];
	
	row["getColumn"] = $.proxy(function(name){
		var index = this.results.columns.indexOf(name);
		
		if(index < 0)
		{
			return null;
		}
		
		return this.record.values[index];
	}, {"results": grid.data(SEARCH_RESULTS_RESULTS), "row": row, "record": resultRecord});

	return row;
}

function getSelectedSearchResults(searchResultsId) {
	var grid = $("#" + searchResultsId);
	var gridData = grid.data(SEARCH_RESULTS_DATA);
	var searchResults = grid.data(SEARCH_RESULTS_RESULTS);
	
	//if no search is done previously
	if(!gridData)
	{
		return null;
	}
	
	var arr = grid.pqGrid( "selection", { type:'row', method:'getSelection' } );
	
	if(!arr || arr.length < 1)
	{
		return null;
	}
	
	var rows = new Array();
	var resultRecord = null;
	
	for(var i = 0; i < arr.length; i++)
	{
		rows[i] = gridData[arr[i].rowIndx];
		resultRecord = searchResults.records[arr[i].rowIndx];
		
		rows[i]["getColumn"] = $.proxy(function(name){
			var index = this.results.columns.indexOf(name);
			
			if(index < 0)
			{
				return null;
			}
			
			return this.record.values[index];
		}, {"results": grid.data(SEARCH_RESULTS_RESULTS), "row": rows[i], "record": resultRecord});
	}
	
	return rows;
}

function selectAllSearchResults(searchResultsId) {
	var grid = $("#" + searchResultsId);
	
	if(!grid)
	{
		return;
	}
	
	var searchResults = grid.data(SEARCH_RESULTS_RESULTS);

	if(!searchResults)
	{
		return;
	}

	var recCount = searchResults.recordCount;
	
	for(var  i = 0; i < recCount; i++)
	{
		grid.pqGrid( "setSelection", {rowIndx:i} );
	}
}

function clearSearchAndResults(searchQueryId) {
	$("#" + searchQueryId).data(DATA_ATTR_MODEL).reset();
	var resultsId = $("#" + searchQueryId).attr("search-results-id"); 
	
	if(resultsId)
	{
		$("#" + resultsId).css("display", "none");
		$("#__" + resultsId + "_empty").css("display", "none");
	}

	var resultAttributes = $("#" + resultsId).data("attributes");
	
	if(resultAttributes["onChange"])
	{
		var eventData = $("#" + searchQueryId).data("searchConfiguration");
		var modelType = eventData["modelType"];
		var modelServerType = eventData["modelServerType"];
		
		var event = {"results": [], 
				"searchQueryId": searchQueryId,
				"modelType": modelType,
				"modelServerType": modelServerType,
				
				"getColumn": function(row, column) {
					return null;
				}
		};

		try
		{
			invokeDynamicMethod($("#" + resultsId).get(), resultAttributes["onChange"], event);
		}catch(ex)
		{
			console.error("An error occurred while invoking search result on-change event: " + ex);
		}
	}

}

$.addDirective("search-query", function(idx, domElem){
	var elem = $(domElem);
	
	var queryType = elem.attr("queryType");
	var id = elem.attr("id");
	var title = elem.attr("title");
	var searchResultsId = elem.attr("search-results-id");
	var searchResultExpr = elem.attr("onsearch");
	var searchUrl = elem.attr("searchUrl");
	var hidden = elem.attr("hidden");
	
	if(!queryType)
	{
		console.error("No 'queryType' is specified for element <search-query>");
		return;
	}
	
	if(!id)
	{
		console.error("No 'id' is specified for element <search-query>");
		return;
	}
	
	if(!searchResultsId && !searchResultExpr)
	{
		console.error("Both 'search-results-id' & 'onsearch' is not specified for <search-query>");
		return;
	}
	
	if($("#" + searchResultsId).length <= 0)
	{
		console.error("No element found with specified search-results-id: " + searchResultsId);
	}
		
	
	var headContent = " ";
	var lastIdx = queryType.lastIndexOf(".");
	var modelTypeName = queryType.substr(lastIdx + 1);
	var modelType = $.define(modelTypeName, {serverType: queryType}, true);

	var childern = elem.children();
	var searchConfiguration = {
			"defaultValues": {},
			"searchQueryId": id,
			"searchResultsId": searchResultsId,
			"searchResultExpr": searchResultExpr,
			"modelType": modelTypeName,
			"modelServerType": queryType,
			"searchUrl": searchUrl
	};
	
	if(childern)
	{
		childern.each(function(idx, domChildElem){
			var childElem = $(domChildElem);
			
			if(childElem.prop("tagName").toLowerCase() == 'default-value')
			{
				var tagValue = childElem.attr("value");
				var res = null;
				
				if(res = /^\{\{([\$\w\-\.\(\)\,\s]+\$?)\}\}$/.exec(tagValue))
				{
					tagValue = eval(res[1]);
				}
				
				searchConfiguration.defaultValues[childElem.attr("name")] = tagValue;
			}
			else if(childElem.prop("tagName").toLowerCase() == 'header')
			{
				headContent = childElem.html();
			}
			else
			{
				throw "Invalid sub-tag found under <search-query>. Sub-tag: " + childElem.prop("tagName");
			}
		});
	}
	
	var htmlCode = '<div class="" fw-model="' + queryType + '" ';
	var extraAttr = fetchExtraAttributesAsStr(domElem, ["queryType", "title", "onsearch", "searchUrl"]);
	
	htmlCode += extraAttr + '> </div>';
	
	$(htmlCode).insertAfter(elem);
	elem.remove();
	
	$.templateEngine.setContext({"modelType": modelType, "title": title, "headHtml": headContent});
	$.templateEngine.processTemplate("searchQueryWidget", "#" + id);
	$.parseDirectives("#" + id);
	
	var eventData = {
			"searchQueryId": id,
			"searchResultsId": searchResultsId,
			"searchResultExpr": searchResultExpr,
			"modelType": modelTypeName,
			"modelServerType": queryType,
			"searchUrl": searchUrl
	};
	
	$("#" + id + " input[type='button'][name='searchButton']").on("click", eventData, executeSearchQuery);
	$.parseDirectives("#" + id);
	
	//set the default values on the widget
	var prefix = "#" + id;
	var model = $(prefix).data(DATA_ATTR_MODEL);
	
	for(var fld in searchConfiguration.defaultValues)
	{
		try
		{
			model["setters"][fld](searchConfiguration.defaultValues[fld]);
			//$(prefix + ' [fw-field="' + fld + '"]').val(searchConfiguration.defaultValues[fld]); 
		}catch(ex)
		{
			console.error(ex);
			console.error("An error occurred while setting default value for search-field '" + fld + "'. Value: " + searchConfiguration.defaultValues[fld]);
		}
	}
	
	$("#" + id).data("searchConfiguration", searchConfiguration);
	
	if(hidden == "true")
	{
		$("#" + id).css("display", "none");
	}
});

$.addDirective("search-results", function(idx, domElem){
	var elem = $(domElem);
	
	var id = elem.attr("id");
	var title = elem.attr("title");
	var leftMargin = elem.attr("left-margin");
	var rightMargin = elem.attr("right-margin");
	var topMargin = elem.attr("top-margin");
	var bottomMargin = elem.attr("bottom-margin");
	var onChange = elem.attr("onchange");
	var onPostChange = elem.attr("postchange");
	var postColumnProvider = elem.attr("post-column-provider");
	var minColumnWidth = elem.attr("min-colummn-width");
	var freezeColumns = elem.attr("freeze-coumns");
	var selectionModel = elem.attr("selection-model");
	
	if(!id)
	{
		console.error("No 'id' is specified for element <searchQuery>");
		return;
	}
	
	leftMargin = (!leftMargin)? 0 : leftMargin;
	rightMargin = (!rightMargin)? 0 : rightMargin;
	topMargin = (!topMargin)? 0 : topMargin;
	bottomMargin = (!bottomMargin)? 0 : bottomMargin;
	title = (!title)? "Results": title;
	onChange = (!onChange)? null: onChange;
	onPostChange = (!onPostChange)? null: onPostChange;
	freezeColumns = (!freezeColumns)? 0 : parseInt(freezeColumns);
	
	if(selectionModel == 'none')
	{
		selectionModel = null;
	}
	else if(selectionModel == 'multiple')
	{
		selectionModel = {type: 'row', mode: 'block'};
	}
	else
	{
		selectionModel = {type: 'row', mode: 'single'};
	}
	
	minColumnWidth = (!minColumnWidth)? 50: parseInt(minColumnWidth);
	
	
	var childern = elem.children();

	var attributesData = {
		"leftMargin": leftMargin,
		"rightMargin": rightMargin,
		"topMargin": topMargin,
		"bottomMargin": bottomMargin,
		"title": title,
		"preColumns": [],
		"postColumns": [],
		"onChange": onChange,
		"onPostChange": onPostChange,
		"postColumnProvider": postColumnProvider,
		"minColumnWidth": minColumnWidth,
		"freezeColumns": freezeColumns,
		"skipColumns": [],
		"selectionModel": selectionModel,
	};
	
	var emptyMessage = "No records found for specified search criteria!";

	if(childern)
	{
		childern.each(function(idx, domChildElem){
			var childElem = $(domChildElem);
			var targetArray = null;
			
			if(childElem.prop("tagName").toLowerCase() == 'post-column')
			{
				targetArray = attributesData.postColumns;
			}
			else if(childElem.prop("tagName").toLowerCase() == 'pre-column')
			{
				targetArray = attributesData.preColumns;
			}
			else if(childElem.prop("tagName").toLowerCase() == 'empty-message')
			{
				emptyMessage = childElem.html();
				return;
			}
			else if(childElem.prop("tagName").toLowerCase() == 'skip-column')
			{
				attributesData.skipColumns.push(childElem.attr("name"));
				return;
			}
			else
			{
				throw "Invalid sub-tag found under <search-results>. Current tag: " + childElem.prop("tagName");
			}
			
			var title = childElem.attr("title");
			var align = childElem.attr("align");
			
			title = title? title : "";
			align = align? align : "left";
			
			var column = {"title": title, "align": align};
			column["contents"] = childElem.html();
			
			targetArray.push(column);
		});
	}
	
	var htmlCode = '<div ';
	
	var extraAttr = fetchExtraAttributesAsStr(domElem, ["queryType", "title", "search-results-id", "onsearch"]);
	htmlCode += extraAttr + '></div>';
	
	var emptyStyle = elem.attr("empty-style");
	var emptyClass = elem.attr("empty-class");
	
	htmlCode += '<div id="__' + id + '_empty" ';
	
	if(emptyStyle)
	{
		htmlCode += 'style="' + emptyStyle + ';display: none;" ';
	}
	else
	{
		htmlCode += 'style="display: none;" ';
	}
	
	if(emptyClass)
	{
		htmlCode += 'class="' + emptyClass + '" ';
	}
	
	htmlCode += '>' + emptyMessage + '</div>';
	
	$(htmlCode).insertAfter(elem);
	elem.remove();
	
	$("#" + id).data("attributes", attributesData);
});

