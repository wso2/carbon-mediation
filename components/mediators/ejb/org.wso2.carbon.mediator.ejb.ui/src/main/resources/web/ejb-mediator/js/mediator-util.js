/*
 *  Copyright (c) 2008, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
 *
 *  Licensed under the Apache License, Version 2.0 (the "License");
 *  you may not use this file except in compliance with the License.
 *  You may obtain a copy of the License at
 *
 *        http://www.apache.org/licenses/LICENSE-2.0
 *
 *  Unless required by applicable law or agreed to in writing, software
 *  distributed under the License is distributed on an "AS IS" BASIS,
 *  WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *  See the License for the specific language governing permissions and
 *  limitations under the License.
 */

function addArgument(nameempty) {
	if (!isValidArgumentes(nameempty)) {
		return false;
	}
	var displayStyleOfNSEditor = document.getElementById('ns-edior-th').style.display;

	var argumentCount = document.getElementById("argumentCount");
	var i = argumentCount.value;
	var currentCount = parseInt(i);
	currentCount = currentCount + 1;

	argumentCount.value = currentCount;

	var argumentTable = document.getElementById("argumenttable");
	argumentTable.style.display = "";
	var argumentbody = document.getElementById("argumenttbody");

	var argumentRaw = document.createElement("tr");
	argumentRaw.setAttribute("id", "argumentRaw" + i);

	var typeTD = document.createElement("td");
	typeTD.appendChild(createproperttypecombobox('propertyTypeSelection' + i,
			i, name));

	var nameTD = document.createElement("td");
	nameTD.appendChild(createinputtextbox("argumentValue" + i), "");

	var nsTD = document.createElement("td");
	nsTD.setAttribute("id", "nsEditorButtonTD" + i);
	nsTD.style.display = displayStyleOfNSEditor;

	var deleteTD = document.createElement("td");
	deleteTD.innerHTML = "<a href='#' class='delete-icon-link' onclick='deleteArgument("
			+ i + ");return false;'>Delete</a>";

	argumentRaw.appendChild(typeTD);
	argumentRaw.appendChild(nameTD);
	argumentRaw.appendChild(nsTD);
	argumentRaw.appendChild(deleteTD);

	argumentbody.appendChild(argumentRaw);
	return true;
}

function isRemainPropertyExpressions() {
	var nsCount = document.getElementById("argumentCount");
	var i = nsCount.value;

	var currentCount = parseInt(i);

	if (currentCount >= 1) {
		for ( var k = 0; k < currentCount; k++) {
			var propertyType = getSelectedValue('propertyTypeSelection' + k);
			if ("expression" == propertyType) {
				return true;
			}
		}
	}
	return false;
}

function settype(type, i, name) {
	var nsEditorButtonTD = document.getElementById("nsEditorButtonTD" + i);
	if (nsEditorButtonTD == null || nsEditorButtonTD == undefined) {
		return;
	}
	if ("expression" == type) {
		resetDisplayStyle("");
		nsEditorButtonTD.innerHTML = "<a href='#nsEditorLink' class='nseditor-icon-link' style='padding-left:40px' onclick=\"showNameSpaceEditor('argumentValue"
				+ i + "')\">" + name + "</a>";
	} else {
		nsEditorButtonTD.innerHTML = "";
		if (!isRemainPropertyExpressions()) {
			resetDisplayStyle("none");
		}
	}
}

function resetDisplayStyle(displayStyle) {
	document.getElementById('ns-edior-th').style.display = displayStyle;
	var nsCount = document.getElementById("argumentCount");
	var i = nsCount.value;

	var currentCount = parseInt(i);

	if (currentCount >= 1) {
		for ( var k = 0; k < currentCount; k++) {
			var nsEditorButtonTD = document.getElementById("nsEditorButtonTD"
					+ k);
			if (nsEditorButtonTD != undefined && nsEditorButtonTD != null) {
				nsEditorButtonTD.style.display = displayStyle;
			}
		}
	}
}

function onPropertyTypeSelectionChange(i, name) {
	var propertyType = getSelectedValue('propertyTypeSelection' + i);
	if (propertyType != null) {
		settype(propertyType, i, name);
	}
}

function onBeanTypeSelectionChange(benTypeElementName) {
	var propertyType = getSelectedValue(benTypeElementName);
	if (propertyType != null) {
		settypeBeanType(propertyType, benTypeElementName);
	}
}

function settypeBeanType(type, name) {
	var nsEditorButtonTD = document.getElementById("nsBeanTypeEditorButtonTD");
	if (nsEditorButtonTD == null || nsEditorButtonTD == undefined) {
		return;
	}
	if ("expression" == type) {
		nsEditorButtonTD.style.display = "";
	} else {
		nsEditorButtonTD.style.display = "none";
	}
}

function getSelectedValue(id) {
	var propertyType = document.getElementById(id);
	var propertyType_indexstr = null;
	var propertyType_value = null;
	if (propertyType != null) {
		propertyType_indexstr = propertyType.selectedIndex;
		if (propertyType_indexstr != null) {
			propertyType_value = propertyType.options[propertyType_indexstr].value;
		}
	}
	return propertyType_value;
}

function createproperttypecombobox(id, i, name) {
	// Create the element:
	var combo_box = document.createElement('select');

	// Set some properties:
	combo_box.name = id;
	combo_box.setAttribute("id", id);
	combo_box.onchange = function() {
		onPropertyTypeSelectionChange(i, name)
	};
	// Add some choices:
	var choice = document.createElement('option');
	choice.value = 'literal';
	choice.appendChild(document.createTextNode('Value'));
	combo_box.appendChild(choice);

	choice = document.createElement('option');
	choice.value = 'expression';
	choice.appendChild(document.createTextNode('Expression'));
	combo_box.appendChild(choice);

	return combo_box;
}

function deleteArgument(i) {
	CARBON
			.showConfirmationDialog(
					"Are you sure, you want to delete this?",
					function() {
						var argumentRow = document.getElementById("argumentRaw"
								+ i);
						if (argumentRow != undefined && argumentRow != null) {
							var parentTBody = argumentRow.parentNode;
							if (parentTBody != undefined && parentTBody != null) {
								parentTBody.removeChild(argumentRow);
								if (!isContainRaw(parentTBody)) {
									var argumentTable = document
											.getElementById("argumenttable");
									argumentTable.style.display = "none";
								}
								var argumentCount = document
										.getElementById("argumentCount");
								var j = argumentCount.value;
								var currentCount = parseInt(j);
								currentCount = currentCount - 1;
								document.getElementById("argumentCount").value = currentCount;
								//alert("done"+document.getElementById("argumentCount").value)
							}
						}
					});
}

function isContainRaw(tbody) {
	if (tbody.childNodes == null || tbody.childNodes.length == 0) {
		return false;
	} else {
		for ( var i = 0; i < tbody.childNodes.length; i++) {
			var child = tbody.childNodes[i];
			if (child != undefined && child != null) {
				if (child.nodeName == "tr" || child.nodeName == "TR") {
					return true;
				}
			}
		}
	}
	return false;
}

function createinputtextbox(id, value) {

	var input = document.createElement('input');
	input.setAttribute('id', id);
	input.name = id;
	input.setAttribute('type', 'text');
	if (value != null && value != undefined) {
		input.setAttribute('value', value);
	}

	return input;
}

function isValidArgumentes(nameemptymsg) {
	var nsCount = document.getElementById("argumentCount");
	var i = nsCount.value;
	var currentCount = parseInt(i);
	if (currentCount >= 1) {
		for ( var k = 0; k < currentCount; k++) {
			var prefix = document.getElementById("argumentName" + k);
			if (prefix != null && prefix != undefined) {
				if (prefix.value == "") {
					CARBON.showWarningDialog(nameemptymsg)
					return false;
				}
			}
		}
	}
	return true;
}

function ejbMediatorValidate() {
	var beanstalk = document.getElementById("beanstalk");
	if (beanstalk && beanstalk.value == "") {
		CARBON.showErrorDialog(ejbjsi18n["mediator.ejb.beanstalk.value.empty"]);
		return false;
	}
	
	var clazz = document.getElementById("clazz");
	if (clazz && clazz.value == "") {
		CARBON.showErrorDialog(ejbjsi18n["mediator.ejb.class.value.empty"]);
		return false;
	}
	
	var ejbMethod = document.getElementById("ejbMethod");
	
	if (ejbMethod && ejbMethod.value == "") {
		CARBON.showErrorDialog(ejbjsi18n["mediator.ejb.method.value.empty"]);
		return false;
	}
	
	var ejbJNDI = document.getElementById("jndiName");
	
	if (ejbJNDI && ejbJNDI.value == "") {
		CARBON.showErrorDialog(ejbjsi18n["mediator.ejb.ejbJNDI.value.empty"]);
		return false;
	}
	
	return true;

}
