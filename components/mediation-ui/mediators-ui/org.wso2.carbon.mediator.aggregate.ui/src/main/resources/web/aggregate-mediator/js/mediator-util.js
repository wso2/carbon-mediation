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

function aggregateMediatorValidate(){
    var expression = document.getElementById("aggregate_expr");
    if (expression && expression.value == "") {
        CARBON.showErrorDialog(aggregatejsi18n["mediator.aggregate.msg.specifyexpression"]);
        return false;
    }
    if(document.getElementById('complete_time').value.match(/^[a-zA-Z]+$/)){
       CARBON.showWarningDialog(aggregatejsi18n["mediator.aggregate.msg.completiontimeout"]);
       return false;
    }
    if(document.getElementById('complete_max').value.match(/^[a-zA-Z]+$/)) {
       CARBON.showWarningDialog(aggregatejsi18n["mediator.aggregate.msg.completionmaxmsg"]);
       return false;
    }
    if(document.getElementById('complete_min').value.match(/^[a-zA-Z]+$/)){
       CARBON.showWarningDialog(aggregatejsi18n["mediator.aggregate.msg.completionminmsg"]);
        return false;
    }
     return true; 
}

function onMessageTypeSelectionChange(benTypeElementName,nstbId) {
	var propertyType = getSelectedValue(benTypeElementName);
	if (propertyType != null) {
		settypeBeanType(propertyType, benTypeElementName,nstbId);
	}
}

function settypeBeanType(type, name,nstbId) {
	var nsEditorButtonTD = document.getElementById(nstbId);
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

function dummyFunction(){

}

function showReference(){
      document.getElementById('seq_ref_row').style.display = '';
}

