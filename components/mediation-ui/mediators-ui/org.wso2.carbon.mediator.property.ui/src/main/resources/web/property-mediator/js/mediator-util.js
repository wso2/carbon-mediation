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
    displayElement("type_row", isDisply);
    displayElement("pattern_row", isDisply);
    displayElement("group_row", isDisply);
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

function propertyMediatorValidate() {
    var radio = document.getElementById('set');
    var name = document.getElementById('mediator.property.name');
    if (name && name.value == "") {
        CARBON.showErrorDialog(propertyMediatorJsi18n["specify.name"]);
        return false;
    }
    if (radio && radio.checked) {
        var val = document.getElementById('mediator.property.val_ex');
        var type = document.getElementById('type_select');

        var check = document.getElementById("value");
        if (check && check.checked) {
            if (type && type.value == "OM") {
                var text = document.getElementById("om_text");
                if (text && text.value == "") {
                    CARBON.showErrorDialog(propertyMediatorJsi18n["specify.element"]);
                }
            } else if (val && val.value == "") {
                CARBON.showErrorDialog(propertyMediatorJsi18n["specify.value"]);
            }
        } else {
            if (val && val.value == "") {
                CARBON.showErrorDialog(propertyMediatorJsi18n["specify.expression"]);
            }
        }
    }
    return true;
}

function expressionChanged() {
    var exprEle = document.getElementById('expression');
    var type = document.getElementById('type_select').value;
//    alert(exprEle.checked);
    if (exprEle && exprEle.checked) {
        displayElement('namespace_col', true);

        displayElement('mediator.property.nmsp_button', true);
        displayElement('expressionLabel', true);
        displayElement('valueLabel', false);
        if (type == "OM") {
            displayElement('namespace_col', true);
            displayElement('value_col', true);
            displayElement('om_text_td', false);
        }
    } else {
        displayElement('namespace_col', false);
        displayElement('mediator.property.nmsp_button', false);
        displayElement('expressionLabel', false);
        displayElement('valueLabel', true);
        hideNameSpaceEditor('nsEditor', 'nsEditorLink');

        if (type == "OM") {
            displayElement('namespace_col', false);
            displayElement('value_col', false);
            displayElement('om_text_td', true);
        }
    }
}


function typeChanged() {
    var val = document.getElementById('type_select').value;
    var exprEle = document.getElementById('expression');
    if (val && val == "OM") {

        //valueRadio.disabled = "disabled";
        //expressionRadio.disabled = "disabled";
        if (exprEle && exprEle.checked) {
            displayElement('namespace_col', true);
            displayElement('value_col', true);
            displayElement('om_text_td', false);
        } else {
            displayElement('namespace_col', false);
            displayElement('value_col', false);
            displayElement('om_text_td', true);
        }
        displayElement('pattern_row', false);
        displayElement('group_row', false);
    } else if (val){
        expressionChanged();

        displayElement('value_col', true);
        displayElement('om_text_td', false);
        if (val == "STRING") {
            displayElement('pattern_row', true);
            displayElement('group_row', true);
        } else {
            displayElement('pattern_row', false);
            displayElement('group_row', false);            
        }

        //valueRadio.disabled = "";
        //expressionRadio.disabled = "";
    }
}

