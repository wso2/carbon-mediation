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

/*
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
 */

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

function getSelectedOperation(obj) {
    return obj.options[obj.selectedIndex].value
}

/**/
function deleteInput(i) {
    var propRow = document.getElementById("inputRaw" + i);
    if (propRow != undefined && propRow != null) {
        var parentTBody = propRow.parentNode;
        if (parentTBody != undefined && parentTBody != null) {
            parentTBody.removeChild(propRow);
            if (!isContainRaw(parentTBody)) {
                var propertyTable = document.getElementById("inputstable");
                propertyTable.style.display = "none";
            }
        }
    }
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

function onInputTypeSelectionChange(i, name) {
    var propertyType = getSelectedValue('inputTypeSelection' + i);
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
        nsEditorButtonTD.innerHTML = "<a href='#nsEditorLink' class='nseditor-icon-link' style='padding-left:40px' onclick=\"showNameSpaceEditor('inputValue" + i + "')\">" + name + "</a>";
    } else {
        nsEditorButtonTD.innerHTML = "";
        if (!isRemainPropertyExpressions()) {
            resetDisplayStyle("none");
        }
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

function resetDisplayStyle(displayStyle) {
    document.getElementById('ns-edior-th').style.display = displayStyle;
    var nsCount = document.getElementById("inputCount");
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

function isRemainPropertyExpressions() {
    var nsCount = document.getElementById("inputCount");
    var i = nsCount.value;

    var currentCount = parseInt(i);

    if (currentCount >= 1) {
        for (var k = 0; k < currentCount; k++) {
            var propertyType = getSelectedValue('inputTypeSelection' + k);
            if ("expression" == propertyType) {
                return true;
            }
        }
    }
    return false;
}

function loadConfigedInputs(val) {

    jQuery.ajax({
        type:'POST',
        url: '../salesforce-mediator/editInputs_ajaxprocessor.jsp',
        data:'operationName=' + val  ,
        success: function(data) {
            jQuery("#configInputs").html(data);

        }
    });
}

function isValidInputs() {

    var nsCount = document.getElementById("inputCount");
    var i = nsCount.value;

    var currentCount = parseInt(i);

    alert("Called");
    if (currentCount >= 1) {
        for (var k = 0; k < currentCount; k++) {

            var uri = document.getElementById("inputValue" + k);
            var req = document.getElementById("inputRequired_hidden" + k);
            var name = document.getElementById("inputName_hidden" + k);
            alert('Value of ' + req);
            if (uri == null || uri == undefined || uri.value == "" && true == req) {

                CARBON.showWarningDialog(name + ': ' + salesforceMediatorJsi18n["mediator.salesforce.inputvalueemptyerror"])
                return false;

            }
        }
    }
    return true;
}

function createproperttypecombobox(id, i, name) {
    // Create the element:
    var combo_box = document.createElement('select');

    // Set some properties:
    combo_box.name = id;
    combo_box.setAttribute("id", id);
    combo_box.onchange = function () {
        onOutputTypeSelectionChange(i, name)
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