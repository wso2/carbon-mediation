var propertyOperationStarted = false;

String.prototype.startsWith = function(prefix) {
	return this.indexOf(prefix) == 0;
}

function loadPropertiesDiv(resourcePath, page) {
	var fillingDiv = "resourceProperties";
	if ($('updateFix')) {
		$('updateFix').parentNode.removeChild($('updateFix'));
	}
	var tempSpan = document.createElement('span');
	tempSpan.id = "updateFix";
	sessionAwareFunction(function() {
		new Ajax.Request('../mediation_secure_vault/properties-ajaxprocessor.jsp', {
			method : 'post',
			parameters : {
				path : resourcePath,
				page : page
			},
			onSuccess : function(transport) {
				$(fillingDiv).innerHTML = transport.responseText;
				$('propertiesList').style.display = "";
				$('propertiesIconExpanded').style.display = "";
				$('propertiesIconMinimized').style.display = "none";

				YAHOO.util.Event.onAvailable('updateFix', function() {
					$('propertiesList').style.display = "";
				});
			},
			onFailure : function(transport) {
				showRegistryError(transport.responseText);
			}
		});
	}, org_wso2_carbon_mediation_secure_vault_ui_jsi18n["session.timed.out"]);
}

function setProperty() {
	if (propertyOperationStarted) {
		CARBON
				.showWarningDialog(org_wso2_carbon_mediation_secure_vault_ui_jsi18n["property.operation.in.progress"]);
		return;
	}
	propertyOperationStarted = true;
	sessionAwareFunction(
			function() {

				var reason = "";
				reason += validateEmptySV(document.getElementById('propName'),
				    org_wso2_carbon_mediation_secure_vault_ui_jsi18n["property.name"]);
				if (reason === "") {
					reason += validateForInputSV(document.getElementById('propName'),
					    org_wso2_carbon_mediation_secure_vault_ui_jsi18n["property.name"]);
				}
				if (reason === "") {
                	reason += validateEmptySV(document.getElementById('propValue'),
                	    org_wso2_carbon_mediation_secure_vault_ui_jsi18n["property.value"]);
                }
				if(reason === ""){
					if(document.getElementById('propValue').value != document.getElementById('propValueConfirm').value){
					    reason += org_wso2_carbon_mediation_secure_vault_ui_jsi18n["property.value.confirmation.invalid"];
					}
				}
				
				var propertyName = document.getElementById('propName').value;
				var propertyValue = document.getElementById('propValue').value;

				if (propertyName.startsWith("registry.")) {
					reason = org_wso2_carbon_mediation_secure_vault_ui_jsi18n["property.name.cannot.be.hidden"];
				}
				// Check for the previously entered property
				var foundPropName = false;
				var allNodes = document.getElementById("propertiesList")
						.getElementsByTagName("*");
			  
				for ( var i = 0; i < allNodes.length; i++) {
					if (YAHOO.util.Dom.hasClass(allNodes[i],
							"propEditNameSelector")) {
						if (allNodes[i].value == $('propName').value)
							reason += org_wso2_carbon_mediation_secure_vault_ui_jsi18n["duplicate.entry.please.choose.another.name"];
					}
				}

				if (reason != "") {
					CARBON.showWarningDialog(reason);
					return false;
				} else {
  					//alert("about to submit1");
					cleanField($('propName'));
					cleanField($('propValue'));
					new Ajax.Request(
							'../mediation_secure_vault/properties-ajaxprocessor.jsp',
							{
								method : 'post',
								parameters : {
									name : propertyName,
									value : propertyValue
								},

								onSuccess : function(transport) {
									window.location.reload();
                                                                        
								},

								onFailure : function(transport) {
									CARBON
											.showErrorDialog(transport.responseText);
									propertyOperationStarted = false;
									return;
								}
							});
					showHideCommon('propertiesAddDiv');
				}
				return true;
			},
			org_wso2_carbon_mediation_secure_vault_ui_jsi18n["session.timed.out"]);
	propertyOperationStarted = false;
   
}

function editProperty(rowid) {
	if (propertyOperationStarted) {
		CARBON
				.showWarningDialog(org_wso2_carbon_mediation_secure_vault_ui_jsi18n["property.operation.in.progress"]);
		return;
	}
	propertyOperationStarted = true;
	sessionAwareFunction(
			function() {
				var reason = "";

				var resourcePath = document
						.getElementById('propRPath_' + rowid).value;
				var oldPropertyName = document.getElementById('oldPropName_'
						+ rowid).value;
				var propertyName = document.getElementById('propName_' + rowid);
				reason += validateEmptySV(
						propertyName,
						org_wso2_carbon_mediation_secure_vault_ui_jsi18n["property.name"]);
				reason += validateForInputSV(
						propertyName,
						org_wso2_carbon_mediation_secure_vault_ui_jsi18n["property.name"]);
				var propertyValue = document.getElementById('propValue_'
						+ rowid);
				var propertyValueConfim = document.getElementById('propValueConfirm_'
						+ rowid);

				// Check for the previously entered property
				var duplicatePropNameCount = 0;
				var allNodes = document.getElementById("propertiesList")
						.getElementsByTagName("*");

				for ( var i = 0; i < allNodes.length; i++) {
					if (YAHOO.util.Dom.hasClass(allNodes[i],
							"propEditNameSelector")) {
						if (allNodes[i].value == propertyName.value) {
							duplicatePropNameCount = duplicatePropNameCount + 1;

						}
					}
				}

				if (duplicatePropNameCount > 1) {
					reason += org_wso2_carbon_mediation_secure_vault_ui_jsi18n["duplicate.entry.please.choose.another.name"];
				}
				
				if(reason === ""){
					if(propertyValue.value != propertyValueConfim.value){
						reason += org_wso2_carbon_mediation_secure_vault_ui_jsi18n["property.value.confirmation.invalid"];
					}
				}

				if (reason === "") {
					new Ajax.Request(
							'../mediation_secure_vault/properties-ajaxprocessor.jsp',
							{
								method : 'post',
								parameters : {
									path : resourcePath,
									oldName : oldPropertyName,
									name : propertyName.value,
									value : propertyValue.value
								},

								onSuccess : function(transport) {
									window.location.reload();
								},

								onFailure : function(transport) {
									CARBON
											.showErrorDialog(transport.responseText);
									propertyOperationStarted = false;
									return;
								}
							});
				} else {
					CARBON.showWarningDialog(reason,
							function() {
								$('propName_' + rowid).value = $('oldPropName_'
										+ rowid).value;
							});
				}
			},
			org_wso2_carbon_mediation_secure_vault_ui_jsi18n["session.timed.out"]);
	propertyOperationStarted = false;
}

function removeProperty(propertyName) {
	if (propertyOperationStarted) {
		CARBON
				.showWarningDialog(org_wso2_carbon_mediation_secure_vault_ui_jsi18n["property.operation.in.progress"]);
		return;
	}
	propertyOperationStarted = true;
	sessionAwareFunction(
			function() {
				CARBON
						.showConfirmationDialog(
								org_wso2_carbon_mediation_secure_vault_ui_jsi18n["are.you.sure.you.want.to.delete"]
										+ " <strong>'"
										+ propertyName
										+ "'</strong> "
										+ org_wso2_carbon_mediation_secure_vault_ui_jsi18n["permanently"],
								function() {
									var resourcePath = document
											.getElementById('propRPath').value;
									new Ajax.Request(
											'../mediation_secure_vault/properties-ajaxprocessor.jsp',
											{
												method : 'post',
												parameters : {
													path : resourcePath,
													name : propertyName,
													remove : 'true'
												},

												onSuccess : function(transport) {
													window.location.reload();
												},

												onFailure : function(transport) {
													CARBON
															.showErrorDialog(transport.responseText);
													propertyOperationStarted = false;
													return;
												}
											});
								}, null);
			},
			org_wso2_carbon_mediation_secure_vault_ui_jsi18n["session.timed.out"]);
	propertyOperationStarted = false;
}
function showProperties() {
	if ($('propertiesIconExpanded').style.display == "none") {
		// We have to expand all and hide sum
		$('propertiesIconExpanded').style.display = "";
		$('propertiesIconMinimized').style.display = "none";
		$('propertiesExpanded').style.display = "";
		$('propertiesMinimized').style.display = "none";
	} else {
		$('propertiesIconExpanded').style.display = "none";
		$('propertiesIconMinimized').style.display = "";
		$('propertiesExpanded').style.display = "none";
		$('propertiesMinimized').style.display = "";
	}
}

function showRetention() {
	if ($('retentionIconExpanded').style.display == "none") {
		// We have to expand all and hide sum
		$('retentionIconExpanded').style.display = "";
		$('retentionIconMinimized').style.display = "none";
		$('retentionExpanded').style.display = "";
		$('retentionMinimized').style.display = "none";
	} else {
		$('retentionIconExpanded').style.display = "none";
		$('retentionIconMinimized').style.display = "";
		$('retentionExpanded').style.display = "none";
		$('retentionMinimized').style.display = "";
	}
}

function validateEmptySV(fld, fldName) {
	var error = "";
	fld.value = fld.value.replace(/^\s\s*/, '').replace(/\s\s*$/, '');
	if (fld.value.length == 0) {
		error = "The required field " + fldName + " is not filled";
	}
	return error;
}

function validateForInputSV(fld, fldName) {
  	var error = "";
  	// This regex includes patterns for characters which should not include in the vault key
	var illegalChars = /[~!@#$%^&*()\\\/+=\:;<>'"?[\]{}|\s,]/;
	if (illegalChars.test(fld.value)) {
		error = "The " + fldName + " has illegal characters";
	}
	return error;
}
