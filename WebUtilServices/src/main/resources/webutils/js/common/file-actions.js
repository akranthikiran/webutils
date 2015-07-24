var FILE_UPLOAD_DIALOG_ID = "__fileUploadForm";
var FILE_UPLOAD_DIALOG_LOCATOR = "#" + FILE_UPLOAD_DIALOG_ID;

$.addController("FileActionsController", {
	name: "File Actions Controller",
	
	invokeFileDownloadAction: function(actionName, urlParams){
		var downloadForm = $("#__fileDownloadForm");
		
		if(!downloadForm || downloadForm.length == 0)
		{
			$('body').append('<form id="__fileDownloadForm" name="__fileDownloadForm" method="post"></form>');
			downloadForm = $("#__fileDownloadForm");
		}
		
		var actionUrl = $.getControllerByName("ActionsController").getActionUrl(actionName, urlParams);
		
		if(actionUrl.indexOf($.contextPath) != 0)
		{
			actionUrl = $.contextPath + actionUrl;
		}
		
		downloadForm.attr("action", actionUrl);
		
		downloadForm.submit();
	},
	
	/**
	 * This function displays a dialog and facilitates file upload to the action-url specified by "actionName" and "urlParams"
	 * 
	 * The configuration object can have following parameters:
	 * 	fileLabel		=>		Label for file field. Default - Selected File
	 *  customUi		=>		Should be html that can added as rows to 2-column html. This will facilitate custom ui components during file upload
	 *  uploadLabel		=>		Label for upload button. Default - Upload File
	 *  cancelLabel		=>		Label for cancel button. Default - Cancel
	 *  
	 *  onBeforeSubmit	=>		Callback method. If specified, this method will be called before request submission. This function is invoked with parameters
	 *  							uploadForm - Represents file upload dialog as jquery element.
	 *  							urlParams   - Url params that were passed to the "invokeFileUploadAction" function. This method update this object, if needed.
	 *  						This method can be used to add custom data to the file upload request. "uploadForm" can be used to get user-input for custom
	 *  						ui elements specified as part of "customUi". And "formData" can be used to add this custom data which will be sent as request.
	 *  onSuccess		=>		Callback method. Called if the request was submitted successfully.
	 *  onError			=>		Callback method. Called if the request resulted in error. This function is invoked with parameters
	 *  							response
	 *  title			=>		Title of upload dialog. Default - Upload File
	 *  dialogWidth		=>		With of dialog. Default - 400
	 *  dialogHeight	=>		Height of dialog. Default - 200
	 */
	invokeFileUploadAction: function(actionName, urlParams, configuration){
		var uploadForm = $(FILE_UPLOAD_DIALOG_LOCATOR);
		
		if(!uploadForm || uploadForm.length == 0)
		{
			$('body').append('<div id="' + FILE_UPLOAD_DIALOG_ID + '" method="post"></div>');
			uploadForm = $(FILE_UPLOAD_DIALOG_LOCATOR);
		}
		
		configuration.fileLabel = configuration.fileLabel ? configuration.fileLabel : "Selected File";
		configuration.customUi = configuration.customUi ? configuration.customUi : "";
		configuration.uploadLabel = configuration.uploadLabel ? configuration.uploadLabel : "Upload File";
		configuration.cancelLabel = configuration.cancelLabel ? configuration.cancelLabel : "Cancel";
		
		
		//remove current html and their events, if any
		uploadForm.empty();
		$.templateEngine.setContext({"configuration" : configuration});
		$.templateEngine.processTemplate("fileUploadDialog", FILE_UPLOAD_DIALOG_LOCATOR);
		
		$.parseDirectives("#" + FILE_UPLOAD_DIALOG_ID);
		
		uploadForm.dialog({
			"title" : configuration.title ? configuration.title : "Upload File",
			width: configuration.dialogWidth ? configuration.dialogWidth : 400, 
			height: configuration.dialogHeight ? configuration.dialogHeight : 200
		});
		
		var eventData = {
			"actionName" : actionName,
			"configuration": configuration,
			"urlParams": urlParams
		};
		
		uploadForm.find(".uploadButton").on("click", eventData, function(event){
			var eventData = event.data;
			var uploadForm = $(FILE_UPLOAD_DIALOG_LOCATOR);
			
			var fileButton = uploadForm.find("input[type='file']");
			var files = fileButton[0].files;
			
			if(!files || files.length <= 0)
			{
				$.alert("Please select a file and then try!");
				return;
			}
			
			var formData = new FormData();
			formData.append(files[0].name, files[0]);
			
			if(eventData.configuration.onBeforeSubmit)
			{
				eventData.configuration.onBeforeSubmit(uploadForm, eventData.urlParams);
			}
			
			var actionUrl = $.getControllerByName("ActionsController").getActionUrl(eventData.actionName, eventData.urlParams);
			
			if(actionUrl.indexOf($.contextPath) != 0)
			{
				actionUrl = $.contextPath + actionUrl;
			}
			
		    $.ajax({
		        url: actionUrl,
		        type: 'POST',
		        data: formData,
		        cache: false,
		        dataType: 'json',
		        processData: false, // Don't process the files
		        contentType: false, // Set content type to false as jQuery will tell the server its a query string request
		        async: false,
		        success: function(data, textStatus, jqXHR)
		        {
	        		if(eventData.configuration.onSuccess)
	        		{
	        			eventData.configuration.onSuccess();
	        		}
	        		
	        		uploadForm.dialog("close");
		        },
		        error: function(jqXHR, textStatus, errorThrown)
		        {
	        		if(eventData.configuration.onError)
	        		{
	        			var response = null;
	        			
	        			try
	        			{
	        				response = $.parseJSON(jqXHR.responseText);
	        			}catch(ex)
	        			{
	        				console.error("Error Response: " + jqXHR.responseText);
	        				console.error(ex);
	        				$.error("There has been an error in uploading file. Please check log files for more details.");
	        			}
	        			
	        			eventData.configuration.onError(response);
	        		}
	        		else
	        		{
	        			$.error("An error occurred while uploading file. Please try refreshing the page!");
	        		}
	        		
	        		uploadForm.dialog("close");
		        }
		    });
			
		});
		
		uploadForm.find(".cancelButton").on("click", {}, function(event){
			$(FILE_UPLOAD_DIALOG_LOCATOR).dialog("close");
		});
		
		uploadForm.dialog("open");
	},
});