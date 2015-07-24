function getUserRoles()
{
	var userRoles = $.userRoles;
	
	if(!userRoles)
	{
		var actionController = $.getControllerByName("ActionsController");
		userRoles = actionController.invokeAction("fetchUserRolesAndGroups");
		
		$.userRoles = userRoles;
	}
	
	return userRoles;
}

$.addAttrDirective("fw-role-groups", function(idx, domElem, context){
	var elem = $(domElem);
	var groups = elem.attr("fw-role-groups").split(",");
	
	var userRoles = getUserRoles();
	
	//check if the required role-group of the element, is present with user-roles
	for(var i = 0; i < groups.length; i++)
	{
		//if present simply return
		if(userRoles.roleGroups.indexOf(groups[i]) >= 0)
		{
			return;
		}
	}
	
	//if user does not have required role groups
	elem.remove();
}, true, true);

$.addAttrDirective("fw-roles", function(idx, domElem, context){
	var elem = $(domElem);
	var roles = elem.attr("fw-roles").split(",");
	
	var userRoles = getUserRoles();
	
	//check if the required role of the element, is present with user-roles
	for(var i = 0; i < roles.length; i++)
	{
		//if present simply return
		if(userRoles.roles.indexOf(roles[i]) >= 0)
		{
			return;
		}
	}
	
	//if user does not have required roles
	elem.remove();
}, true, true);

