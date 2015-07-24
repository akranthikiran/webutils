var __CONTEXT_MENU_COUNTER = 1;

function FWContextMenu(data)
{
	this.closeMenu = function(){
		$("#" + this.menuId).css("visibility", "hidden");
		$(document).off("keydown", this.keyDown).off("mousedown", this.mouseDown);
	};
	
	this.showMenu = function(x, y){
		$("#" + this.menuId).css("visibility", "visible").css("left", x).css("top", y);
		$(document).on("keydown", this, this.keyDown).on("mousedown", this, this.mouseDown);
	};
	
	this.target = null;
	
	this.setTarget = function(locator){
		$(document).on("contextmenu", locator, this, function(event){
			var menu = event.data;
			menu.showMenu(event.pageX, event.pageY);
			menu.target = this;
			
			event.stopImmediatePropagation();
			event.preventDefault();
		});
	};
	
	this.removeTarget = function(locator){
		$(locator).off("contextmenu");
	};
	
	if(data.id)
	{
		this.menuId = data.id;
	}
	else
	{
		this.menuId = "__contextMenu_" + __CONTEXT_MENU_COUNTER;
		__CONTEXT_MENU_COUNTER++;
	}

	
	var menuObj = this;
	var code = '<ul id="' + this.menuId + '" style="position: absolute;display: block;top: 0px;left:0px;background-color: white;visibility: hidden;" class="ui-helper-hidden ui-menu ui-widget ui-widget-content ui-corner-all ui-menu-icons" role="menu">';
	
	$.each(data.items, function(idx, item){
		
		if(item.label == "-")
		{
			code += '<li class="ui-widget-content ui-menu-divider">-----</li>';
			return;
		}
		
		code += '<li class="ui-menu-item" role="presentation">';
		code += '<a id="' + menuObj.menuId + "_" + idx + '"class="ui-corner-all" href="#" role="fw-menuitem">';
		
		code += item.label + '<span class="ui-icon ui-icon-copy"></span></a></li>';
	});
	
	code += "</ul>";
	
	$(document.body).append($(code));

	$.each(data.items, function(idx, item){
		if(item.action)
		{
			var fullData = new Object();
			fullData["menu"] = menuObj;
			fullData["data"] = item.data;
			
			$("#" + menuObj.menuId + "_" + idx).on("click", fullData, function(event){
				var eventData = event.data;
				var menu = eventData.menu;
				var actionData = eventData["data"];
				item.action(event, menu.target, actionData);
				
				menu.closeMenu();
			});
		}
	});
	
	$("a[role='fw-menuitem']").on("mouseenter", function(){
		$(this).addClass("ui-state-focus");
	}).on("mouseout", function(){
		$(this).removeClass("ui-state-focus");
	});
	
	this.keyDown = function(event){
		var menu = event.data;
		
		if( event.which === $.ui.keyCode.ESCAPE ){
			menu.closeMenu();
		}
	};
	
	this.mouseDown = function(event){
		var menu = event.data;
		// Close menu when clicked outside menu
		if( !$(event.target).closest(".ui-menu-item").length ){
			menu.closeMenu();
		}
	};
}