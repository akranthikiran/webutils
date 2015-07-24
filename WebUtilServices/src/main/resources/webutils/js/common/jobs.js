$.addController("JobController", {
	name: "Job Controller",
	jobName: null,
	executionId: null,
	status: null,
	closed: true,
	
	REFRESH_RATE: 2000,
	
	displayJobDetails: function(jobName){
		var progressDetails = null;
		
		try
		{
			progressDetails = makeJsonCall('/action/jobs/getJobProgress', {"jobName": jobName});
		}catch(ex)
		{
			if(ex.code && ex.code == 'NO_JOB_FOUND')
			{
				progressDetails = null;
			}
			else
			{
				ex = (ex.message) ? ex.message : ex;
				$.error("An error occurred while fetching job execution details for job: " + jobName + "<BR/>Error: " + ex);
				return;
			}
		}

		var controller = this;
		
		if(progressDetails == null)
		{
			$.confirmBox("'" + jobName + "' was not executed before. Would you like to start the job?", {
				configuration: {"jobName": jobName, "controller": controller},
				
				onYes: function(configuration) {
					configuration.controller.startJob(configuration.jobName);
				},
			});
		}
		else
		{
			if(progressDetails.jobStatus == 'IN_PROGRESS' || progressDetails.jobStatus == 'CREATED')
			{
				$.confirmBox("'" + jobName + "' is currently running. Would you like to view the job progress?", {
					configuration: {"jobName": jobName, "controller": controller, "progressDetails": progressDetails},
					
					onYes: function(configuration) {
						var controller = configuration.controller;
						
						controller.setJobDetails(configuration.progressDetails);
						controller.viewProgress();
					},
				});
			}
			else
			{
				$.confirmBox("'" + jobName + "' was last executed on '" + progressDetails.startTime + "' and last execution staus was '" + progressDetails.jobStatus + "'.<BR/>" +
						" Would you like to re-execute the job?", {
					configuration: {"jobName": jobName, "controller": controller, "progressDetails": progressDetails},
					
					onYes: function(configuration) {
						var controller = configuration.controller;
						
						controller.startJob(configuration.jobName);
					},
					
					onNo: function() {
						alert("no is clicked");
					}
				});
			}
		}
	},
	
	setJobDetails: function(progressDetails) {
		this.jobName = progressDetails.jobName;
		this.executionId = progressDetails.executionId;
	},
	
	startJob: function(jobName) {
		var progressDetails = null;
		
		try
		{
			progressDetails = makeJsonCall('/action/jobs/startJob', {"jobName": jobName});
		}catch(ex)
		{
			ex = (ex.message) ? ex.message : ex;
			$.error("An error occurred while starting job: " + jobName + "<BR/>Error: " + ex);
			return;
		}
		
		this.setJobDetails(progressDetails);
		this.setProgressValues(progressDetails);
		
		this.viewProgress();
	},
	
	setProgressValues: function(progressDetails) {
		var progressDlg = $("#jobProgressDialog");
		progressDlg.find(".status").text(progressDetails.jobStatus);
		progressDlg.find(".startTime").text(progressDetails.startTime);
		progressDlg.find(".lastUpdateTime").text(progressDetails.lastUpdateTime);
		progressDlg.find("textarea[name='progessLog']").val(progressDetails.log);
		
		if(progressDetails.jobStatus == 'COMPLETED')
		{
			$("#jobDetailsProgess").progressbar("value", 100);
		}
		else
		{
			$("#jobDetailsProgess").progressbar("value", progressDetails.progress);
		}
		
		this.status = progressDetails.jobStatus;
		
		if(progressDetails.jobStatus == 'STOPPED')
		{
			progressDlg.find(".status").css("color", "red");
			progressDlg.find("textarea[name='progessLog']").css("background", "gray");
			$("#jobDetailsProgess").css("background", "gray");
		}
	},
	
	checkProgress: function() {
		var progressDetails = null;
		
		try
		{
			progressDetails = makeJsonCall('/action/jobs/getJobProgress', {"jobName": this.jobName, "executionId": this.executionId});
		}catch(ex)
		{
			ex = (ex.message) ? ex.message : ex;
			$.error("An error occurred while fetching job progress: " + jobName + "<BR/>Error: " + ex);
			return;
		}
		
		this.setProgressValues(progressDetails);

		if(progressDetails.jobStatus == 'FAILED')
		{
			$.error("Job Failed! Please check log for more details.");
			return;
		}

		if(progressDetails.jobStatus == 'COMPLETED')
		{
			$.error("Job is completed successfully!");
			return;
		}

		if(progressDetails.jobStatus == 'STOPING' || progressDetails.jobStatus == 'STOPPED')
		{
			$.error("Job is being stopped. Please check log file for more details!");
			return;
		}

		if(progressDetails.progress < 100 && this.closed == false)
		{
			setTimeout(this.checkProgress, this.REFRESH_RATE);
		}
	},
	
	viewProgress: function() {
		var progressDlg = $("#jobProgressDialog");
		progressDlg.find(".status").css("color", "");
		progressDlg.find("textarea[name='progessLog']").css("background", "");
		$("#jobDetailsProgess").css("background", "");

		$("#jobProgressDialog").dialog("open");
		this.closed = false;
		this.checkProgress();
	},
	
	requestStopJob: function() {
		try
		{
			makeJsonCall('/action/jobs/stopJob', {"jobName": this.jobName});
		}catch(ex)
		{
			ex = (ex.message) ? ex.message : ex;

			$.error("Failed to stop the job. Server Error: " + ex + "<BR/>" +
					"<I>(If error is inappropriate please refresh the page and then try!)</I>");
			return;
		}
	},
	
	stopJob: function() {
		if(this.status != 'IN_PROGRESS')
		{
			$.error("Current job is not under progress!");
			return;
		}
		
		var controller = this;
			
		$.confirmBox("Are you sure you want to stop job '" + this.jobName + "'?", {
			configuration: {"controller": controller},
			
			onYes: function(configuration) {
				//make server call to stop the job
				configuration.controller.requestStopJob();
			},
		});
	},
	
	closeProgress: function() {
		$("#jobProgressDialog").dialog("close");
		this.closed = true;
	},
	
});