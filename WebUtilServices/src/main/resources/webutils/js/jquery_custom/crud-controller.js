$.addController("CrudController", {
	modelType: null,
	modelLabel: null,
	searchQueryId: null,
	searchResultsId: null,
	modifyButtonsContainerId: null,
	
	searchResultsChanged: function(event) {
		var results = event.results;
		
		var enableModify = (results && (results.recordCount > 0));
		
		if(enableModify)
		{
			$("#" + this.modifyButtonsContainerId +" .fw-modify-buttons").css("display", "inline");
		}
		else
		{
			$("#" + this.modifyButtonsContainerId +" .fw-modify-buttons").css("display", "none");
		}
	},
	
	addModel: function(){
		$.displayModelForm(this.modelType, this.modelAdded);
	},
	
	modelAdded: function(newModel){
		try
		{
			$.getControllerByName("ActionsController").invokeAction(this.modelType + ".create", null, newModel.toPlainObject());
			this.refreshSearch();
			return this.modelLabel + " is created successfully!";
		}catch(ex)
		{
			console.error(ex);
			
			if(ex.message)
			{
				throw ex.message;
			}
			
			throw "Failed to create " + this.modelLabel + ".<BR/>Server Error: " + ex.message;
		}
	},
	
	deleteModel: function(){
		var selectedRecord = getSelectedSearchResult(this.searchResultsId);
		
		if(!selectedRecord) {
			$.alert("Please select a record and then try!");
			return;
		}
		
		var modelId = selectedRecord.getColumn("ID");
		
		if(!$.confirm("Are you sure you want to delete selected " + this.modelLabel + "?"))
		{
			return;
		}
		
		try
		{
			$.getControllerByName("ActionsController").invokeAction(this.modelType + ".delete", [modelId]);
			this.refreshSearch();
			$.alert("Successfully deleted selected " + this.modelLabel);
		}catch(ex)
		{
			console.error(ex);
			
			if(ex.message)
			{
				ex = ex.message;
			}
			
			$.error("Failed to delete " + this.modelLabel + " with id: " + modelId + "<BR/>Server Error: " + ex);
		}
	},
	
	editModel: function(){
		var selectedRecord = getSelectedSearchResult(this.searchResultsId);
		
		if(!selectedRecord) {
			$.alert("Please select a " + this.modelLabel + " and then try!");
			return;
		}
		
		var modelId = selectedRecord.getColumn("ID");
		var model = null;
		
		try
		{
			model = $.getControllerByName("ActionsController").invokeAction(this.modelType + ".fetch", [modelId]);
		}catch(ex)
		{
			console.error(ex);
			console.error("An error occurred while fetching " + this.modelLabel + " with id: " + modelId + "\n Error: " + ex);
		}
		
		if(!model)
		{
			$.error("Failed to fetch " + this.modelLabel + " with id: " + modelId + "<BR/>Please refresh the page and then try!");
			return;
		}
		
		$.displayModelForm(this.modelType, this.modelUpdated, model);
	},
	
	modelUpdated: function(updateModel, actualModel) {
		try
		{
			updateModel.setId(actualModel.id);
			$.getControllerByName("ActionsController").invokeAction(this.modelType + ".update", null, updateModel.toPlainObject());
			
			this.refreshSearch();
			return this.modelLabel + " is updated successfully!";
		}catch(ex)
		{
			console.error(ex);
			
			if(ex.message)
			{
				ex = ex.message;
			}
			
			throw "Failed to update " + this.modelLabel + ".<BR/>Server Error: " + ex.message;
		}
	},
	
	refreshSearch: function() {
		refreshSearchResults(this.searchQueryId);
	},
	
});


