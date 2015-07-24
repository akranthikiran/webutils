$.addController("ExcelController", {
	name: "Excel Controller",
	importType: null,
	actionName: null,
	modelName: null,
	callback: null,
	
	displayForImpot: function(importType, modelName, actionName, callback) {
		this.importType = importType;
		this.actionName = actionName;
		this.modelName = modelName;
		this.callback = callback;

		var dialog = $('#excelImpotDialog');
		dialog.find("input[name='importFile']").val(null);
		dialog.find("input[name='importFile']").trigger("change");
		
		dialog.dialog("open");
	},
	
	getSampleSheet: function(){
		var form = $('#excelImpotDialog form[name="importForm"]');
		var importTypeField = form.find('input[name="importType"]');
		
		importTypeField.val(this.importType);
		form.get(0).submit();
	},
	
	cancelImport: function() {
		$('#excelImpotDialog').dialog("close");
	},
	
	importData: function() {
		var dialog = $('#excelImpotDialog');
		var files = dialog.find("input[name='importFile']").get(0).files;
		
		if(!files || files.length == 0)
		{
			$.error("Please select a file and then try!");
			return;
		}
		
		var file = files[0];
		
		if(!file.name.toLowerCase().endsWith(".xls"))
		{
			$.error("Only excel files are supported for import. Please select valid excel file and then try!");
			return;
		}
		
		var fileRequest = new FormData();
		fileRequest.append(file.name, file);
		
		var importResult = null;
		
		try
		{
			importResult = $.getControllerByName("ActionsController").
						invokeAction(this.actionName, null, fileRequest, {contentType: false, processData: false});//makeJsonCall(url, fileRequest);
		}catch(ex)
		{
			console.error("An error occurred while uploading the selected file");
			console.error(ex);
			
			if(ex.message)
			{
				ex = ex.message;
			}
			
			$.error("An error occurred while uploading the selected file.<BR/>Error: " + ex);
			return;
		}
		
		if(this.callback && $.isFunction(this.callback))
		{
			this.callback();
		}
		
		$('#excelImpotDialog').dialog("close");

		if(importResult.errorCount <= 0)
		{
			$.alert(importResult.successCount + " " + this.modelName + " are imported successfully");
			return;
		}
		
		$.templateEngine.setContext({"result": importResult});
		var html = $.templateEngine.processTemplate("importLogContent");
		
		var logDialog = $("#excelImpotLogDialog");
		
		logDialog.find(".logMessage").html(importResult.successCount + " " + this.modelName + " are imported successfully. " +
				importResult.errorCount + " " + this.modelName + " failed to import.");
		
		var logContainer = logDialog.find(".importLog");
		logContainer.html(html);
		
		logDialog.dialog({
			width: $(document).width() * 0.8,
			height: $(document).height() * 0.8,
		});
		
		logContainer.height(logContainer.parent().innerHeight() * 0.8);
		
		logDialog.dialog("open");
	},
	
	closeLog: function() {
		$("#excelImpotLogDialog").dialog("close");
	},
});