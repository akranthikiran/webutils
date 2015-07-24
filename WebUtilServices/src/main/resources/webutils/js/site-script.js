var DATA_PAGE_URL = "__PAGE_URL";

$( document ).ajaxError(function( event, jqxhr, settings, exception ) {
	
	if(jqxhr.statusCode().status == 401)
	{
		location.reload();
	}
	
	if(jqxhr.statusCode().status < 500)
	{
		return;
	}
	
	alert("An error occurred while contacting server. Please try refreshing the page!" +
			"\nURL: " + settings.url +
			"\nError: " + exception);
});

$.getControllerByName("SPAController").addInitListener(function(event){
	var activeTabName = event.configuration.attributes["tab"];
	var activeTab = $("[re-tab='" + activeTabName + "']");
	
	if(activeTab.length == 0)
	{
		console.error("No active tab found with name: " + activeTabName);
		return;
	}
	
	var activeTabIndex = activeTab.attr("fw-tab-index");
	$('#contentPageTabs').tabs({active : activeTabIndex});
});

function strToDate(str)
{
	var format = $.getConfiguration("dateFormat");
	var date = $.datepicker.parseDate(format, str);
	
	return date;
}

function changeLocation(location)
{
	var state = {"canBeAnything": true};
	history.pushState(state, "New Title", location);
	//expect(history.state).toEqual(state);
}

//Arguments should be Div-id, url, div-id, url...
function openPage()
{
	if(arguments.length % 2 != 0)
	{
		alert("Invalid number of arguments specified by openPage");
		return;
	}
	
	var currentUrl = null;
	
	for(var i = 0; i < arguments.length; i += 2)
	{
		//currentUrl = $("#" + arguments[i]).data(DATA_PAGE_URL);
		currentUrl = $("#" + arguments[i]).attr("fw-context-manager");
		
		if(currentUrl && currentUrl == arguments[i + 1])
		{
			$.reinitalizeControllers("#" + arguments[i]);
			continue;
		}
		
		$.ajax({
			url: arguments[i + 1],
			async: false,
			cache: false,
			dataType: "html",
			//context: $("#" + arguments[i]),
			context: {"selector": "#" + arguments[i], "url": arguments[i + 1]},
			success: function(data){
				var selector = this.selector;
				var url = this.url;
				var targetElem = $(selector);
				
				$.unregisterDirectives(selector);
				targetElem.empty();
				
				targetElem.html(data);
				//targetElem.data(DATA_PAGE_URL, url);
				targetElem.attr("fw-context-manager", url);
				
				$.parseDirectives(selector);
			}
		});
	}
}


