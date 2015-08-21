<templates>
	<template name="addModelContents">
		<div class="contentLayer" style="overflow: auto;position: absolute; top: 0px; left: 0px; right: 0px;" fw-model="{{modelType.__className}}">
			<table class="dataTable" style="width:95%;">
				<for-each data="{{modelType.fields}}" loop-var="field" index-var="idx">
					<if test="{{field.readOnly}}">
						<continue/>
					</if>
					
					<if test="{{config.skipFields.indexOf(context.field.name)}} &gt;= 0">
						<continue/>
					</if>
					
					<if test="'{{field.type}}' == 'complex'">
						<continue/>
					</if>
					
					<if test="'{{field.multiValued}}' == 'true'">
						<if test="'{{field.type}}' != 'lov'">
							<continue/>
						</if>
					</if>
					
					<set-var name="_fldConfig" expr="context.config.fieldConfig[context.field.name]"/>
					<set-var name="_disabled" expr="(context._fldConfig &amp;&amp; (context._fldConfig.disabled == 'true')) ? 'true' : 'false'"/>
					
					<set-var name="_fieldValue" expr="context.field.__value ? context.field.__value : context.field.defaultValue"/>
					
					<tr>
						<td style="white-space: nowrap;">
							<set-var name="_labelClass" expr="{{field.__isMandatory}}?'mandatoryLabel':'siteLabel'"/>
							 
							<span class="{{_labelClass}}">
								<if test="{{field.description}}">
									<tooltip position="left" helpIcon="true">{{field.description}}</tooltip>
								</if>
								
								<if test="{{field.label}}">
									{{field.label}}
								</if>
								<else>
									{{field.name}}
								</else>
							</span>
						</td>
						<td style="width: 90%;">
							<if test="'{{field.type}}' == 'multiLine'">
								<set-var name="_rows" expr="(context._fldConfig &amp;&amp; context._fldConfig.rows) ? context._fldConfig.rows : 5"/>
								
								<textarea name="{{field.name}}" fw-field="{{field.name}}" style="width: 100%;" rows="{{_rows}}" fw-disabled="{{_disabled}}">{{_fieldValue$}}</textarea>
							</if>
							<else-if test="'{{field.type}}' == 'lov'">
								<if test="'{{field.multiValued}}' == 'true'">
									<table style="width: 100%; border-collapse: collapse;">
										<td style="padding: 0px;padding-right: 2px;">
											<div class="ui-widget-content _multiLov_{{field.name}} siteLabel" fw-field-name="{{field.name}}" 
														style="overflow: auto;width: 100%;height: 8em;">
												<if test="context.modelToUpdate &amp;&amp; context.modelToUpdate[context.field.name]">
													<for-each data="{{field.values}}" loop-var="value" index-var="valIdx">
														<if test="context.modelToUpdate[context.field.name].indexOf(context.value.value) &gt;= 0">
															{{value.label}}<BR/>
														</if>
													</for-each>
												</if>
											</div>
										</td>
										<td valign="top" style="width: 1px; padding: 0px;">
											<a href="#" style="text-decoration: none; color: blue;font-size: 0.8em;" fw-tooltip="Edit" 
																		onclick="$.displayModelForm['displayLovMultiSelectionDialog']('._multiLov_{{field.name}}')">
												<i class="icon-edit"></i>
											</a>
										</td>
									</table>
								</if>
								<else>
									<select name="{{field.name}}" fw-field="{{field.name}}" style="width: 100%;" fw-disabled="{{_disabled}}">
										<option value=""></option>
										
										<for-each data="{{field.values}}" loop-var="value" index-var="valIdx">
											<if test="'{{field.__value}}' == '{{value.value}}'">
												<option value="{{value.value}}" selected="selected">{{value.label}}</option>
											</if>
											<else>
												<option value="{{value.value}}">{{value.label}}</option>
											</else>
										</for-each>
									</select>
								</else>
							</else-if>
							<else-if test="'{{field.type}}' == 'boolean'">
								<if test="'{{_fieldValue}}' == 'true'">
									<input type="checkbox" name="{{field.name}}" fw-field="{{field.name}}" checked="checked" fw-disabled="{{_disabled}}"/>
								</if>
								<else>
									<input type="checkbox" name="{{field.name}}" fw-field="{{field.name}}" fw-disabled="{{_disabled}}"/>
								</else>
							</else-if>
							<else-if test="'{{field.type}}' == 'date'">
								<date-picker name="{{field.name}}" fw-field="{{field.name}}" value="{{field.__value$}}" fw-disabled="{{_disabled}}"></date-picker>
							</else-if>
							<else-if test="'{{field.type}}' == 'int'">
								<input type="number" name="{{field.name}}" fw-field="{{field.name}}"  style="width: 100%;" value="{{_fieldValue$}}" fw-disabled="{{_disabled}}"/>
							</else-if>
							<else>
								<input type="text" name="{{field.name}}" fw-field="{{field.name}}"  style="width: 100%;" value="{{_fieldValue$}}" fw-disabled="{{_disabled}}"/>
							</else>
						</td>
					</tr>
				</for-each>
			</table>
		</div>
		
		<div class="buttonLayer" style="position: absolute; bottom: 5px;right: 5px;background: white;">
			<set-var name="_actionButtonLabel" expr="{{__forUpdate}}?'Update {{modelType.label}}':'Create {{modelType.label}}'"/>
			<set-var name="_actionButtonLabel" expr="context.config.actionButtonLabel ? context.config.actionButtonLabel : '{{_actionButtonLabel}}'"/>
			<set-var name="_actionButtonStyle" expr="context.config.actionButtonStyle ? context.config.actionButtonStyle : ' '"/>
			<set-var name="_actionButtonClass" expr="context.config.actionButtonClass ? context.config.actionButtonClass : ' '"/>
			
			<set-var name="_cancelButtonLabel" expr="context.config.cancelButtonLabel ? context.config.cancelButtonLabel :'Cancel'"/>
			<set-var name="_cancelButtonStyle" expr="context.config.cancelButtonStyle ? context.config.cancelButtonStyle : ' '"/>
			<set-var name="_cancelButtonClass" expr="context.config.cancelButtonClass ? context.config.cancelButtonClass : ' '"/>
			
			<input type="button" value="{{_actionButtonLabel}}" name="createObject" style="{{_actionButtonStyle}}" class="_actionButtonClass"/>
			
			<input type="button" value="{{_cancelButtonLabel}}" name="cancelCreate" class="{{_cancelButtonClass}}" style="{{_cancelButtonStyle}}"/>
		</div>
	</template>
	
	<template name="lovMultiSelectDialog">
		<table style="width: 100%;">
			<tr>
				<th>
					Available {{fieldType.label}}
				</th>
				<th>
				</th>
				<th>
					Selected {{fieldType.label}}
				</th>
			</tr>
			<tr>
				<td style="width: 47%;">
					<select multiple="multiple" class="avaiableOptions siteLabel" style="height: 10em; width: 100%;">
						<for-each data="{{fieldType.values}}" loop-var="value" index-var="idx">
							<if test="{{selectedValues.indexOf(context.value.value)}} &gt;= 0">
								<continue/>
							</if>
							
							<option value="{{value.value}}">{{value.label}}</option>
						</for-each>
					</select>
				</td>
				<td valign="middle" style="width: 1px;;text-align: center;">
					<set-var name="_buttonStyle" expr="'display: block; width: 2em;margin: 0.2em;'"/>
					
					<input type="button" value=" &gt; " style="{{_buttonStyle}}" class="moveRight"/>
					<input type="button" value=" &lt; " style="{{_buttonStyle}}" class="moveLeft"/>
					<input type="button" value=" &gt;&gt; " style="{{_buttonStyle}}" class="moveAllRight"/>
					<input type="button" value=" &lt;&lt; " style="{{_buttonStyle}}" class="moveAllLeft"/>
				</td>
				<td style="width: 47%;">
					<select multiple="multiple" class="selectedOptions siteLabel" style="height: 10em; width: 100%;">
						<for-each data="{{fieldType.values}}" loop-var="value" index-var="idx">
							<if test="{{selectedValues.indexOf(context.value.value)}} &gt;= 0">
								<option value="{{value.value}}">{{value.label}}</option>
							</if>
						</for-each>
					</select>
				</td>
			</tr>
		</table>
		
		<div style="position: absolute; right: 0.5em; bottom: 0.5em;">
			<input type="button" value="Okay" class="okayButton"/>
			<input type="button" value="Cancel" class="cancelButton"/>
		</div>
	</template>	
	
	<template name="alertBox">
		<table style="width: 100%;">
			<tr>
				<td>
					<div style="width: 100%;height: 100px;overflow: auto;font-size: 0.8em;padding: 5px;" class="alertContent ui-widget-content ui-corner-all">
					</div>
				</td>
			</tr>
			<tr>
				<td style="text-align: right;">
					<input type="button" name="alertOk" value="Okay"/>
				</td>
			</tr>
		</table>
	</template>

	<template name="confirmBox">
		<div class="confirmContent messageStyle" style="display: inline;">
			
		</div>
		<div style="position: absolute;right: 0px; bottom: 0.2em;">
			<a class="yesButton linkButton">Yes</a>
			<a class="noButton linkButton">No</a>
		</div>
	</template>



	<template name="searchQueryWidget">
		<fieldset class="ui-widget-content searchBorder">
			<if test="{{title}}">
				<legend class="siteLabel" style="color:blue;font-weight:bold;">{{title}}</legend>		
			</if>
			
			{{headHtml}}
			
			<for-each data="{{modelType.fields}}" loop-var="field" index-var="idx">
			
				<if test="'{{field.type}}' == 'complex'">
					<continue/>
				</if>
				
				<if test="{{field.readOnly}}">
					<input type="hidden" name="{{field.name}}" fw-field="{{field.name}}"/>
					<continue/>
				</if>
						
				<div class="searchField">
					<set-var name="_labelClass" expr="{{field.__isMandatory}}?'mandatoryLabel':'siteLabel'"/>
					
					<span class="{{_labelClass}}" style="margin-left: 5px; margin-right: 5px;">
						<if test="{{field.description}}">
							<tooltip position="left" helpIcon="true">{{field.description}}</tooltip>
						</if>
						
						<if test="{{field.label}}">
							{{field.label}}
						</if>
						<else>
							{{field.name}}
						</else>
					</span>
					<if test="'{{field.type}}' == 'multiLine'">
						<textarea name="{{field.name}}" fw-field="{{field.name}}"></textarea>
					</if>
					<else-if test="'{{field.type}}' == 'lov'">
						<select name="{{field.name}}" fw-field="{{field.name}}">
							<option value="">&amp;nbsp;&amp;nbsp;&amp;nbsp;&amp;nbsp;</option>
							<for-each data="{{field.values}}" loop-var="value" index-var="valIdx">
								<option value="{{value.value}}">{{value.label}}</option>
							</for-each>
						</select>
					</else-if>
					<else-if test="'{{field.type}}' == 'boolean'">
						<input type="checkbox" name="{{field.name}}" fw-field="{{field.name}}"/>
					</else-if>
					<else-if test="'{{field.type}}' == 'date'">
						<date-picker name="{{field.name}}" fw-field="{{field.name}}"/>
					</else-if>
					<else>
						<input type="text" name="{{field.name}}" fw-field="{{field.name}}"/>
					</else>
				</div>
			</for-each>
			
			<div style="bottom: 5px;right: 5px;display: block;margin-top: 10px;">
				<input type="hidden" name="searchExecuted" value="false"/>
				<input type="button" value="Search" name="searchButton"/>
			</div>
		</fieldset>
	</template>

	<template name="fileChooserTemplate">
		<div>
			<span class="{{labelClass}}">{{label}}</span>
			<input type="text" name="choosenFile" readonly="readonly" size="25" class="{{textClass}}"/>
			<input type="button" name="browseButton" value="Browse..."/>
			<input type="file" name="{{name}}" id="{{id}}" style="display: none;"/>
		</div>
	</template>
	
	<template name="fileUploadDialog">
		<table>
			<tr>
				<td class="siteLabel">
					{{configuration.fileLabel}} : 
				</td>
				<td  class="siteLabel">
					<file-selector label=" " name="selectedFile"></file-selector>
				</td>
			</tr>
			{{configuration.customUi}}
		</table>
		<div style="position: absolute;right: 0px; bottom: 0.2em;">
			<a class="uploadButton confirm_yes_button linkButton">{{configuration.uploadLabel}}</a>
			<a class="cancelButton confirm_no_button linkButton">{{configuration.cancelLabel}}</a>
		</div>
	</template>
	
</templates>