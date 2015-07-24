function FileManager()
{
	var url = $.contextPath + "/action/fileManager/rest/fetchDocumentTypes";
	var docTypes = makeCachedJsonCall("fileManager.docTypes", url);
	
	$.fileTypeExtensions = new Object();
	
	$.each(docTypes, function(idx, docType){
		$.fileTypeExtensions[docType.name] = docType.extensions; 
	});

	this.eventData = {"contextName": $.contextPath};
	this.initalized = false;
	
	this.eventData.validFiles = new Array();
	this.eventData.validFileCount = 0;
	this.eventData.invalidFileCount = 0;
	
	this.eventData.resetUploadFileData = function(){
		this.validFiles = new Array();
		this.validFileCount = 0;
		this.invalidFileCount = 0;
	};
	
	this.selectFile = function(type, needPreview, resFunc)
	{
		if(!$.fileTypeExtensions[type])
		{
			alert("Invalid/unsupported file type encountered: " + type);
			return;
		}
		
		if(!this.initalized)
		{
			this.init();
			this.initalized = true; 
		}

		//Reset the status
		this.eventData["type"] = type;
		this.eventData.selectedFile = null;
		
		$("#fileManager_fileLister").html("Please provide filter text and filter files!");
		$("#fileManager_fileLister input[type='text'][name='fileFilter']").val("");
		$("#fileManager_fileLister input[type='hidden'][name='fileType']").val(type);
		
		if(needPreview)
		{
			$("#fileManager_fileLister").css("width", "80%");
			$("#fileManager_preview").css("width", "20%");
		}
		else
		{
			$("#fileManager_fileLister").css("width", "100%");
			$("#fileManager_preview").css("width", "0px").css("visibility", "hidden");
		}
		
		$("#fileManager").dialog("open");
		
		this.eventData.resultFunction = resFunc;
	};
	
	this.init = function(){
		$("#fileManager input[type='button'][name='filterBut']").off('click').on("click", this.eventData, function(event){
			var eventData = event.data;
			var filter = $("#fileManager input[type='text'][name='fileFilter']").val();
			fileManager_filterFiles(eventData.type, filter, "#fileManager_fileLister", eventData);
			/*
			var eventData = event.data;
			
			var filter = $("#fileManager_fileLister input[type='text'][name='fileFilter']").val();
			var files = $.realEstate.makeServerCall('/action/query/fetchFiles', {type: eventData.type, filter: filter});
			
			if(files == null || files.length == 0)
			{
				$("#fileManager_fileLister").html("No files found with specified name filter!");
				return;
			}
			
			var html = "";
			
			for(var i = 0; i < files.length; i++)
			{
				html += '<div style="width: 5em;height: 7em;" class="fileIcon">' +
							'	<img src="' + eventData.contextName + '/images/' + eventData.type +'.png" style="display: block;" fw-value="' + files[i] + '"/>' +
							'	<span style="margin-left:auto;margin-right:auto;">' + files[i].name + "</span>" +
							'</div>';
			}
			
			$("#fileManager_fileLister").html(html);
			*/
		});
		
		$("#fileManager input[type='button'][name='uploadFiles']").off('click').on("click", this.eventData, function(event){
			var eventData = event.data;
			var type = eventData["type"];
			
			$("#fileUploader .fileExtensions").html($.fileTypeExtensions[type].toString());
			$("#fileuploader_file_list").html("Please select files to upload!");
			$("#file_uploader_buttons input[type='file'][fw-dynamic='true']").remove();
			$("#file_uploader_buttons input[type='file'][fw-dynamic='true']").prop("files", null);
			
			eventData.resetUploadFileData();
			
			$("#fileUploader").dialog("open");
		});
		
		$("#fileManager_buttons input[type='button'][name='cancelButton']").on("click", function(event){
			$("#fileManager").dialog("close");
		});
		
		$("#fileManager_buttons input[type='button'][name='selectButton']").on("click", this.eventData, function(event){
			var eventData = event.data;
			
			var selectedFile = new Object();
			selectedFile["name"] = $("#fileManager_buttons input[type='text'][name='selectedFile']").val();
			selectedFile["id"] = $("#fileManager_buttons input[type='hidden'][name='selectedFileId']").val();
			
			$("#fileManager").dialog("close");
			
			eventData.resultFunction(selectedFile);
		});
		
		$(document).on("click", "#fileManager_fileLister div[fw-docid]", this.eventData, function(event){
			var eventData = event.data;
			
			if(eventData.selectedFileIcon)
			{
				eventData.selectedFileIcon.children("span").removeClass("selectedItem");
			}
			
			eventData.selectedFileIcon = $(this);
			
			$(this).children("span").addClass("selectedItem");
			
			$("#fileManager_buttons input[type='text'][name='selectedFile']").val($(this).attr("fw-docname"));
			$("#fileManager_buttons input[type='hidden'][name='selectedFileId']").val($(this).attr("fw-docid"));
		});
		
		/**********************************************************************************
		 * *********************File upload dialog events
		 ***********************************************************************************/
		
		$(document).on("change", "#file_uploader_buttons input[type='file']", this.eventData, filesSelectedForUpload);
		
		$("#file_uploader_buttons input[type='button'][name='fileUploaderBrowse']").on("click", function(event){
			var fileButtons = $("#file_uploader_buttons input[type='file']");
			$("#file_uploader_buttons input[type='file'][name='browseFiles_" + fileButtons.length + "']").trigger(jQuery.Event( "click" ));
		});

		$("#file_uploader_buttons input[type='button'][name='fileUploaderUpload']").on("click", this.eventData, uploadSelectedFiles);
		
		$("#file_uploader_buttons input[type='button'][name='fileUploaderClose']").on("click", this.eventData, function(event){
			
			var eventData = event.data;
			
			if(eventData.validFileCount > 0)
			{
				if(!$.confirm("You have selected " + eventData.validFileCount + " valid files to upload.\n" +
						"Are you sure you want to close this window without uploading?"))
				{
					return;
				}
			}
			
			$("#fileUploader").dialog("close");
		});
	};
	
};

$.fileManager = new FileManager();

function filesSelectedForUpload(event)
{
	var eventData = event.data;
	var type = eventData.type;
	
	var fileButtons = $("#file_uploader_buttons input[type='file']");
	var html = '<table border="1" class="dataTable" style="width: 100%;"><tr><th>File Name</th><th>Status</th></tr>';
	var idx, ext, validFile;
	var files = this.files;
	var validFileCount = 0, invalidFileCount = 0;
	
	eventData.validFiles = new Array();
	
	for(var fileButIdx = 0; fileButIdx < fileButtons.length; fileButIdx++)
	{
		files = fileButtons[fileButIdx].files;
		
		if(!files)
		{
			continue;
		}
		
		for(var i = 0; i < files.length; i++)
		{
			html += '<tr><tr><td>' + files[i].name + '</td>';
			
			idx = files[i].name.lastIndexOf(".");
			validFile = false;
	
			if(idx >= 0)
			{
				ext = files[i].name.toLowerCase().substr(idx + 1);
				
				if($.fileTypeExtensions[type].indexOf(ext) >= 0)
				{
					validFile = true;
				}
			}
	
			if(validFile)
			{
				eventData.validFiles[validFileCount] = files[i];
				validFileCount++;
				html += '<td style="font-weight: bold;">Valid file</td>';
			}
			else
			{
				invalidFileCount++;
				html += '<td style="color:red;font-weight: bold;">Invalid file</td>';
			}
			
			html += '</tr>';
		}
	}
	
	eventData.validFileCount = validFileCount;
	eventData.invalidFileCount = invalidFileCount;
	
	html += '</table>';
	$("#fileuploader_file_list").html(html);
	
	$('<input type="file" name="browseFiles_' + (fileButtons.length + 1) + '" multiple="multiple" style="display:none;" fw-dynamic="true"/>').insertAfter($(this));
}

function uploadSelectedFiles(event)
{
	var eventData = event.data;
	
	if(eventData.validFileCount <= 0)
	{
		$.alert("There are no valid files selected to upload. Please select valid files (Using Browse button) and then try!");
		return;
	}
	
	if(eventData.invalidFileCount > 0)
	{
		if(!$.confirm("There are " + eventData.invalidFileCount + " invalid/unsupported files and " + eventData.validFileCount + " valid files.\n " +
				"Do you want to proceed with valid files upload?"))
		{
			return;
		}
	}
	
	var data = new FormData();

	$.each(eventData.validFiles, function(key, value)
	{
		data.append(value.name, value);
	});
	
    $.ajax({
        url: $.contextPath + '/action/fileManager/rest/upload/' + eventData.type,
        type: 'POST',
        data: data,
        cache: false,
        dataType: 'json',
        processData: false, // Don't process the files
        contentType: false, // Set content type to false as jQuery will tell the server its a query string request
        async: false,
        success: function(data, textStatus, jqXHR)
        {
        	if(data.success)
        	{
        		alert(data.updateCount + " File(s) are uploaded successfully!");
        		$("#fileUploader").dialog("close");
        	}
        	else
        	{
        		$.alert('An error occurred on server while uploading files.<BR/>Please Contact administrator if error persists.<BR/>Error Message: ' + data.error);
        	}
        },
        error: function(jqXHR, textStatus, errorThrown)
        {
        	$.alert('An unknown error occurred while uploading files.<BR/>Please Contact administrator if error persists.<BR/>Error Message: : ' + textStatus);
        }
    });
}

function fileManager_filterFiles(type, filter, fileContainer, eventData)
{
	if(!filter || $.trim(filter) <= 0)
	{
		alert("Please provide a filter and then try!");
		return;
	}
	
	var docTemplate = '<div style="margin: 5px;cursor: pointer;" fw-docName="%name%" fw-docId="%id%">' + 
						'<img src="%contextPath%/images/docTypes/%type%.png" style="margin: 5px;"/><span style="padding: 3px;">%name%</span></div>';
    $.ajax({
        url: $.contextPath + '/action/fileManager/rest/fetchDocs',
        type: 'POST',
        data: {'type': type, 'filter': filter},
        cache: false,
        dataType: 'json',
        async: false,
        success: function(data, textStatus, jqXHR)
        {
        	if(data && data.length > 0)
        	{
        		var html = "";
        		
        		for(var i = 0; i < data.length;i++)
        		{
        			data[i].type = type;
        			data[i].contextPath = $.contextPath;
        			html += $.parseTemplate(docTemplate, data[i]);
        		}
        		
        		$(fileContainer).html(html);
        	}
        	else
        	{
        		$(fileContainer).html("No '" + type + "' documents found with specified filter: " + filter);
        	}
        },
        error: function(jqXHR, textStatus, errorThrown)
        {
        	$.alert('An unknown error occurred while fetching files.<BR/>Please Contact administrator if error persists.<BR/>Error Message: : ' + textStatus);
        }
    });
}

