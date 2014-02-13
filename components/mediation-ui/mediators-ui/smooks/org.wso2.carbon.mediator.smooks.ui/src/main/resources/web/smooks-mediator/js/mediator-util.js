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

function propertyMediatorValidate() {

    var val = document.getElementById('seq_ref');
    if (val && val.value == "") {
        CARBON.showErrorDialog(propertyMediatorJsi18n["specify.value"]);
        return false;
    }

    return true;
}


function typeChanged() {
    var val = document.getElementById('exprTypeSelect').value;
    var namespaceEle = document.getElementById('namespaceEditor');
    var actionElem = document.getElementById('actionSelect')
    if (val && val == "expression") {
        displayElement('namespaceEditor', true);
        displayElement('actionSelect', true);
    } else if (val){
        displayElement('namespaceEditor', false);
        displayElement('actionSelect', false);
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

