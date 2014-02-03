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

function displaySetProperties(isDisply) {
	var toDisplayElement;
	displayElement("mediator.property.action_row", isDisply);
	displayElement("mediator.property.value_row", isDisply);
	toDisplayElement = document.getElementById("mediator.namespace.editor");
	if (toDisplayElement != null) {
		if (isDisply) {
			toDisplayElement.style.display = '';
		} else {
			toDisplayElement.style.display = 'none';
		}
	}
}

function displayElement(elementId, isDisplay) {
	var toDisplayElement = document.getElementById(elementId);
	if (toDisplayElement != null) {
		if (isDisplay) {
			toDisplayElement.style.display = '';
		} else {
			toDisplayElement.style.display = 'none';
		}
	}
}

function headerMediatorValidate() {
	var name = document.getElementById("mediator.header.name");
	var xmlContent = document.getElementById("mediator.header.inlinexmltext");

	if (name && name.value == "" && !(xmlContent && xmlContent != "")) {
		CARBON.showErrorDialog(headerMediatorJsi18n["header.name.required"]);
		return false;
	}
	var set = document.getElementById("set");
	if (set && set.checked) {
		var val = document.getElementById("mediator.header.val_ex");
		if (val && val.value == "" && xmlContent && xmlContent.value == "") {
			CARBON
					.showErrorDialog(headerMediatorJsi18n["header.valueExp.required"]);
			return false;
		}
	}
	return true;
}

function createNamespaceEditor(elementId, id, prefix, uri) {
	var ele = document.getElementById(elementId);
	if (ele != null) {
		var createEle = document.getElementById(id);
		if (createEle != null) {
			if (createEle.style.display == 'none') {
				createEle.style.display = '';
			} else {
				createEle.style.display = 'none';
			}
		} else {
			ele.innerHTML = '<div id=\"'
					+ id
					+ '\">'
					+ '<table><tbody><tr><td>Prefix</td><td><input width="80" type="text" id=\"'
					+ prefix
					+ '\"+ '
					+ 'name=\"'
					+ prefix
					+ '\" value=""/></td></tr><tr><td>URI</td><td><input width="80" '
					+ 'type="text" id=\"' + uri + '\"+ name=\"' + uri
					+ '\"+ value=""/></td></tr></tbody></table></div>';
		}
	}
}

function headerMediatorValidate() {
	var inlineXML = document.getElementById("inlineXML").checked;
	var value = document.getElementById("value").checked;
	var expression = document.getElementById("expression").checked;
  var isRemove = document.getElementById("remove");

	if ((expression || value) && !isRemove) {
		if (document.getElementById("mediator.header.name").value == null
				|| document.getElementById("mediator.header.name").value == "") {
			CARBON.showErrorDialog(headerMediatorJsi18n["header.mediator.header.name.required"]);
			return false;
		}
		if ((document.getElementById("mediator.header.val_ex").value == null || document
				.getElementById("mediator.header.val_ex").value == "")) {
			CARBON.showErrorDialog(headerMediatorJsi18n["header.mediator.ex_val.required"]);
		}

		document.getElementById("mediator.header.inlinexmltext").value == "";
	}
	if (inlineXML) {
		if (document.getElementById("mediator.header.inlinexmltext").value == null
				|| document.getElementById("mediator.header.inlinexmltext").value == "") {
			CARBON.showErrorDialog(headerMediatorJsi18n["header.mediator.inline.xml.required"]);
			return false;
		}
		document.getElementById("mediator.header.name").value = "";
	}

	return true;

}
