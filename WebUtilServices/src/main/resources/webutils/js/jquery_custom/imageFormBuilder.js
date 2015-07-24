$.addController("imageFormController", {
	currentRange: {startX: 0, startY: 0, width: 0, heigth: 0, clientX: 0, clientY: 0},
	rangeStarted: false,
	control: null,
	rangeSelector: null,
	rangeControllerMethod: null,
	
	setControl: function(control) {
		this.control = control;
	},
	
	resetRangeLayer: function(newX, newY){
		newX = newX - this.control.offset().left;
		newY = newY - this.control.offset().top;
		
		var x = 0, y = 0, width = 0, height = 0;
		
		if(this.currentRange.startX <= newX)
		{
			x = this.currentRange.startX;
			width = newX - this.currentRange.startX - 5;
			
			if(width < 0)
			{
				width = 0;
			}
			
			this.currentRange.width = width;
		}
		else
		{
			x = newX + 5;
			width = this.currentRange.startX - x + 1;
			
			if(width < 0)
			{
				width = 0;
			}
			
			this.currentRange.width = 0 - width;
		}
		
		if(this.currentRange.startY <= newY)
		{
			y = this.currentRange.startY;
			height = newY - this.currentRange.startY - 5;
			
			if(height < 0)
			{
				height = 0;
			}
			
			this.currentRange.height = height;
		}
		else
		{
			y = newY + 5;
			height = this.currentRange.startY - y + 1;
			
			if(height < 0)
			{
				height = 0;
			}
			
			this.currentRange.height = 0 - height;
		}
		
		var parent = this.control.parent();
		
		if(parent.scrollLeft)
		{
			x += parent.scrollLeft();
			y += parent.scrollTop();
		}

		this.rangeSelector.css("visibility", "visible")
					.css("left", x + "px")
					.css("top", y + "px")
					.css("width", width + "px")
					.css("height", height + "px");
	},
	
	rangeSelectionStarted: function(e){
		this.currentRange = {startX: 0, startY: 0, width: 0, heigth: 0};
		
		this.currentRange.startX = e.pageX - this.control.offset().left;
		this.currentRange.startY = e.pageY - this.control.offset().top;
		
		this.rangeStarted = true;
		$(document).on("keydown", this.handleKeyEvent);
		
		this.resetRangeLayer(e.pageX, e.pageY);
		
		console.debug("Range started at: " + e.pageX + ", " + this.control.offset().left );
	},
	
	rangeSelectionChanged: function(e){
		if(!this.rangeStarted)
		{
			return;
		}
		
		this.resetRangeLayer(e.pageX, e.pageY);
	},

	rangeSelectionEnded: function(e){
		
		if(!this.rangeStarted)
		{
			return;
		}
		
		if(this.currentRange.width != 0 && this.currentRange.height != 0)
		{
			//this.resetRangeLayer(e.pageX, e.pageY);
			
			var x = this.currentRange.startX, y = this.currentRange.startY;
			var width = this.currentRange.width, height = this.currentRange.height;
			
			if(width < 0)
			{
				x = x + width;
				width = 0 - width;
			}
			
			if(height < 0)
			{
				y = y + height;
				height = 0 - height;
			}
		
			this.addHighlightedRegion(x, y, width, height);
		}
		
		this.cancelSelection();
	},
	
	handleKeyEvent: function(event){
		if(event.which == 27)//if escape key is pressed
		{
			this.cancelSelection();
		}
	},
	
	cancelSelection: function(){
		this.rangeSelector.css("visibility", "hidden");
		this.rangeStarted = false;
		$(document).off("keydown", this.handleKeyEvent);
	},
	
	/*
	checkForIntersection: function(x, y, w, h){
		var ranges = this.control.data("selectionRanges");
		
		if(!ranges)
		{
			return false;
		}
		
		var x2 = x + w;
		var y2 = y + w;
		
		var rx2 = 0, ry2= 0;
		
		for(var i = 0; i < ranges.length; i++)
		{
			rx2 = ranges[i].x + ranges[i].width;
			ry2 = ranges[i].y + ranges[i].height;
			
			if(x2 < ranges[i].x || x > rx2)
			{
				return false;
			}
			
			if(y2 < ranges[i].y || y > ry2)
			{
				return false;
			}
		}
		
		return true;
	},
	*/
	
	addHighlightedRegion: function(x, y, w, h){
		var parent = this.control.parent();
		
		var divTag = $("<div/>").css("left", x + "px")
				.css("top", y + "px")
				.css("width", w + "px")
				.css("height", h + "px")
				.css("position", "absolute")
				.addClass("formRegion");
		
		divTag.insertBefore(this.control);
		
		var ranges = this.control.data("selectionRanges");
		
		if(!ranges)
		{
			ranges = new Array();
			this.control.data("selectionRanges", ranges);
		}
		
		var range = {
			"x": Math.round(x), 
			"y": Math.round(y), 
			"width": Math.round(w), 
			"height": Math.round(h)
		};
		ranges.push(range);
		
		if(this.rangeControllerMethod)
		{
			var event = {
				"range": range,
				"rangeElement": divTag,
				"source": this.control
			};
			
			this.rangeControllerMethod(event);
		}
	},

});

/*
 	<fw-image-form class="image1" selector=".image1" image="/images/image.png"/>
 */
$.addDirective("fw-image-form", function(idx, domElem){
	var elem = $(domElem);
	
	if(!checkForMandatoryAttributes(domElem, ["selector", "image"]))
	{
		return;
	}
	
	var selector = elem.attr("selector");
	var imgSrc = elem.attr("image");
	
	var mainController = $.getController(domElem);
	var onRangeSelect = elem.attr("onRangeSelect");
	var rangeControllerMethod = null;
	
	if(mainController && onRangeSelect)
	{
		if(mainController[onRangeSelect])
		{
			rangeControllerMethod = mainController[onRangeSelect];
		}
		else
		{
			console.error("Range controller method '" + onRangeSelect + "' not found under controller: " + mainController);
		}
	}
	
	
	var code = "<img src=\"" + imgSrc + "\" ";
	var extraAttr = fetchExtraAttributesAsStr(domElem, ["image", "selector", "onRangeSelect"]);
	
	code += extraAttr + "/>";
	
	$(code).insertAfter(elem);
	elem.remove();
	
	var controller = $.controllers["imageFormController"];
	
	$(selector).css("cursor", "crosshair");
	
	var setControllerData = function(event){
		var eventData = event.data;
		var controller = eventData.controller;
		
		controller.setControl($(event.target));
		controller["rangeSelector"] = eventData.rangeSelector;
		controller["rangeControllerMethod"] = eventData.rangeControllerMethod;
		return controller;
	};
	
	var mouseDownListener = function(event){
		var controller = setControllerData(event);
		controller.rangeSelectionStarted(event);
	};
	
	var mouseUpListener = function(event){
		var controller = setControllerData(event);
		controller.rangeSelectionEnded(event);
	};
	
	var mouseMouseListener = function(event){
		var controller = setControllerData(event);
		controller.rangeSelectionChanged(event);
	};
	
	var parent = $(selector).parent();
	var rangeSelector = $(parent).children(".rangeSelector");
	
	var eventData = {
			"controller": controller, 
			"rangeSelector": rangeSelector,
			"rangeControllerMethod": rangeControllerMethod
	};
	
	if(rangeSelector.length == 0)
	{
		var selectorCode = '<div class="rangeSelector" style="visibility: hidden; position: absolute; border-width: 2px;border-color: red;border-style: solid;cursor:crosshair;"/>';
		parent.append(selectorCode);
		
		rangeSelector = $($(parent).children(".rangeSelector").first());
		
		rangeSelector.off("mouseup").on("mouseup", eventData, mouseUpListener)
		 		.off("mousemove").on("mousemove", eventData, mouseMouseListener);
	}
	else
	{
		rangeSelector = $(rangeSelector.first());
	}
	
	eventData.rangeSelector = rangeSelector;
	
	$(selector).off("dragstart").on("dragstart", function(){return false;})
			   .off("mousedown").on("mousedown", eventData, mouseDownListener)
			   .off("mouseup").on("mouseup", eventData, mouseUpListener)
			   .off("mousemove").on("mousemove", eventData, mouseMouseListener);
});



