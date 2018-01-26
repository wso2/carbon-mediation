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

// input validator function
function classMediatorValidate() {
    var className = document.getElementById('mediatorInputId').value;
    if(className == null || className==""){
        CARBON.showErrorDialog(classi18n["mediator.clazz.validInputmsg"]);
        return false;
    }
    return true;
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

function onPropertyTypeSelectionChange(i, name) {
    var propertyType = getSelectedValue('propertyTypeSelection' + i);
    if (propertyType != null) {
        settype(propertyType, i, name);
    }
}

function settype(type, i, name) {
    var nsEditorButtonTD = document.getElementById("nsEditorButtonTD" + i);
    if (nsEditorButtonTD == null || nsEditorButtonTD == undefined) {
        return;
    }
    if ("expression" == type) {
        resetDisplayStyle("");
        nsEditorButtonTD.innerHTML = "<a href='#nsEditorLink' class='nseditor-icon-link' style='padding-left:40px' onclick=\"showNameSpaceEditor('propertyValue" + i + "')\">" + name + "</a>";
    } else {
        nsEditorButtonTD.innerHTML = "";
        if (!isRemainPropertyExpressions()) {
            resetDisplayStyle("none");
        }
    }
}

function isRemainPropertyExpressions() {
    var nsCount = document.getElementById("propertyCount");
    var i = nsCount.value;

    var currentCount = parseInt(i);

    if (currentCount >= 1) {
        for (var k = 0; k < currentCount; k++) {
            var propertyType = getSelectedValue('propertyTypeSelection' + k);
            if ("expression" == propertyType) {
                return true;
            }
        }
    }
    return false;
}

function resetDisplayStyle(displayStyle) {
    document.getElementById('ns-edior-th').style.display = displayStyle;
    var nsCount = document.getElementById("propertyCount");
    var i = nsCount.value;

    var currentCount = parseInt(i);

    if (currentCount >= 1) {
        for (var k = 0; k < currentCount; k++) {
            var nsEditorButtonTD = document.getElementById("nsEditorButtonTD" + k);
            if (nsEditorButtonTD != undefined && nsEditorButtonTD != null) {
                nsEditorButtonTD.style.display = displayStyle;
            }
        }
    }
}

function deleteproperty(i) {
    CARBON.showConfirmationDialog(clazzi18n["mediator.clazz.delete.confirm"],function(){
    var propRow = document.getElementById("propertyRaw" + i);
    if (propRow != undefined && propRow != null) {
        var parentTBody = propRow.parentNode;
        if (parentTBody != undefined && parentTBody != null) {
            parentTBody.removeChild(propRow);
            if (!isContainRaw(parentTBody)) {
                document.getElementById('propertytable').style.display = 'none';
                document.getElementById('propertyLabel').style.display = 'none';
            }
        }
    }
    });
}

function isContainRaw(tbody) {
    if (tbody.childNodes == null || tbody.childNodes.length == 0) {
        return false;
    } else {
        for (var i = 0; i < tbody.childNodes.length; i++) {
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
