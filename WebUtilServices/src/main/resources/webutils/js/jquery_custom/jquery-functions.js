$.parseTemplate = function(template, params){
	if(!template)
	{
		return;
	}
	
	var pattern = /\%([\w\-\.]+)\%/g;
	var res = template.replace(pattern, function(match, p1){
		
		if(!params[p1])
		{
			return "";
		}
		
		return params[p1];
	});
	
	return res;
};

$.copyMissingAttributes = function(target, source){
	$.each(source, function(name, val){
		if(!target[name])
		{
			target[name] = source[name];
		}
	});
};

/*
 
 {
 	fields = [
 		{label: 'Test1', description: 'Some Desc', type: 'text', name: ''
 		 	extraAttrs: {},
 		 	defaultValue: "",
 		 	preColumnContent: function(row){},
 		 	fldCssStyle: "",
 		 	fldCssClass: "",
 		 	disabled: false,
 		 	mandatory: false,
 		 	enumValues: 
 		},
 	],
 	tableCssClass: "",
 	tableCssStyle: "",
 	
 	globalFieldProps: {
	 	labelCssClass: "",
	 	tooltipPosition: "",
	}
 }
 
 */
$.createForm = function(divSelector, data){
	var tableTemplate = '<table class="%tableCssClass%" style="%tableCssStyle%">%tableContent%</table>';
	var rowTemplate = '<tr>%preColumnContentHtml%'+
							'<td><span class="%labelCssClass%" style="%labelCssStyle%" >'+
								'<tooltip position="%tooltipPosition%" helpIcon="true">%description%</tooltip>'+
								'%label%</span>'+
							'</td>'+
							'<td>%fieldUi%</td>'+
						'</tr>';
	
	var html = "";
	
	$.each(data.fields, function(idx, fld){
		
		fld["index"] = idx;
		
		var extraAttrs = fld.extraAttrs;
		var extraAttrHtml = "";
		
		
		if(extraAttrs)
		{
			$.each(extraAttrs, function(name, val){
				extraAttrHtml += name + '="' + val + '" ';
			});
		}
		
		fld["extraAttrHtml"] = extraAttrHtml;
		
		if(fld.disabled)
		{
			extraAttrHtml += 'disabled="disabled" ';
		}
		
		if(data.globalFieldProps)
		{
			$.copyMissingAttributes(fld, data.globalFieldProps);
		}

		var fldHtml = "";
		
		if(fld.type == "SIMPLE_TEXT")
		{
			fldHtml = '<input type="text" name="%name%" %extraAttrHtml% style="%fldCssStyle%" class="%fldCssClass%" value="%value%" fw-required="%mandatory%" />';
		}
		else if(fld.type == "MULTI_LINE_TEXT")
		{
			fldHtml = '<textarea name="%name%" %extraAttrHtml% style="%fldCssStyle%" class="%fldCssClass%" fw-required="%mandatory%">%value%</textarea>';
		}
		else if(fld.type == "INT")
		{
			fldHtml = '<input type="text" name="%name%" %extraAttrHtml% style="%fldCssStyle%" class="%fldCssClass%" value="%value%" fw-pattern="\\d+" fw-required="%mandatory%"/>';
		}
		else if(fld.type == "FLOAT")
		{
			fldHtml = '<input type="text" name="%name%" %extraAttrHtml% style="%fldCssStyle%" class="%fldCssClass%" value="%value%" fw-pattern="\\d+|\\d+\\.\\d*" fw-required="%mandatory%"/>';
		}
		else if(fld.type == "DATE")
		{
			fldHtml = '<dateField name="%name%" %extraAttrHtml% cssStyle="%fldCssStyle%" value="%value%" fw-required="%mandatory%"/>';
		}
		else if(fld.type == "COMPLEX")
		{
			fldHtml = 'Complex type currently not supported';
		}
		else if(fld.type == "FILE_SELECTOR")
		{
			fldHtml = '<file-selector name="%name%" %extraAttrHtml% style="%fldCssStyle%" class="%fldCssClass%" fw-required="%mandatory%">%value%</file-selector>';
		}
		else if(fld.type == "ENUM")
		{
			fldHtml = '<select name="%name%" %extraAttrHtml% style="%fldCssStyle%" class="%fldCssClass%" fw-required="%mandatory%">';
			
			$.each(fld.enumValues, function(idx, value){
				if(fld.value == value)
				{
					fldHtml += '<option value="' + value + '" selected="selected">' + value + '</option>';
				}
				else
				{
					fldHtml += '<option value="' + value + '">' + value + '</option>';
				}
			});
			
			fldHtml += '</select>';
		}
		
		fld["extraAttrHtml"] = extraAttrHtml;
		
		fldHtml = $.parseTemplate(fldHtml, fld);
		
		fld["fieldUi"] = fldHtml;
		
		if(data["preColumnContent"])
		{
			fld["preColumnContentHtml"] = '<td>' + data["preColumnContent"](fld) + "</td>";
		}
		
		html += $.parseTemplate(rowTemplate, fld);
	});
	
	data["tableContent"] = html;
	$(divSelector).html($.parseTemplate(tableTemplate, data));
	
	$.parseDirectives(divSelector);
};