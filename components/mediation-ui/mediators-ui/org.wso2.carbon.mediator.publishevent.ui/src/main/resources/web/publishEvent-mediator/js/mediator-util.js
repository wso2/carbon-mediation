/*
 * Copyright (c) 2014, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
 *
 * Licensed to the Apache Software Foundation (ASF) under one or more
 * contributor license agreements.  See the NOTICE file distributed with
 * this work for additional information regarding copyright ownership.
 * The ASF licenses this file to You under the Apache License, Version 2.0
 * (the "License"); you may not use this file except in compliance with
 * the License.  You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

function addproperty(name, nameemptymsg, valueemptymsg, propertytype) {

    if (!isValidProperties(nameemptymsg, valueemptymsg, propertytype)) {
        return false;
    }


    switch (propertytype) {
        case "meta":
            var displayStyleOfNSEditor = document.getElementById('meta-ns-editor-th').style.display;

            var metaPropertyCount = document.getElementById("metaPropertyCount");
            var i = metaPropertyCount.value;

            var currentCount = parseInt(i);
            currentCount = currentCount + 1;

            metaPropertyCount.value = currentCount;

            var metapropertytable = document.getElementById("metapropertytable");
            metapropertytable.style.display = "";
            var metapropertytbody = document.getElementById("metapropertytbody");

            var metaPropertyRaw = document.createElement("tr");
            metaPropertyRaw.setAttribute("id", "metaPropertyRaw" + i);

            var metaNameTD = document.createElement("td");
            metaNameTD.innerHTML = "<input type='text' name='metaPropertyName" + i + "' id='metaPropertyName" + i + "'" +
                " />";

            var metaTypeTD = document.createElement("td");
            metaTypeTD.appendChild(createproperttypecombobox('metaPropertyTypeSelection' + i, i, name, propertytype));

            var metaValueTD = document.createElement("td");
            metaValueTD.innerHTML = "<input type='text' name='metaPropertyValue" + i + "' id='metaPropertyValue" + i + "'" +
                " class='esb-edit small_textbox' />";
            var metaNsTD = document.createElement("td");
            metaNsTD.setAttribute("id", "metaNsEditorButtonTD" + i);
            metaNsTD.style.display = displayStyleOfNSEditor;

            var metaValueTypeTD = document.createElement("td");
            metaValueTypeTD.appendChild(createpropertvaluetypecombobox('metaPropertyValueTypeSelection' + i, i, name, propertytype));

            var metaDeleteTD = document.createElement("td");
            metaDeleteTD.innerHTML = "<a href='#' class='delete-icon-link' onclick='deleteMetaProperty(" + i + ");return false;'>" + publishEventMediatorJsi18n["mediator.publishEvent.action.delete"] + "</a>";

            metaPropertyRaw.appendChild(metaNameTD);
            metaPropertyRaw.appendChild(metaTypeTD);
            metaPropertyRaw.appendChild(metaValueTD);
            metaPropertyRaw.appendChild(metaNsTD);
            metaPropertyRaw.appendChild(metaValueTypeTD);
            metaPropertyRaw.appendChild(metaDeleteTD);

            metapropertytbody.appendChild(metaPropertyRaw);
            return true;

        case "correlation":
            var displayStyleOfNSEditor = document.getElementById('correlation-ns-editor-th').style.display;

            var correlationPropertyCount = document.getElementById("correlationPropertyCount");
            var i = correlationPropertyCount.value;

            var currentCount = parseInt(i);
            currentCount = currentCount + 1;

            correlationPropertyCount.value = currentCount;

            var correlationpropertytable = document.getElementById("correlationpropertytable");
            correlationpropertytable.style.display = "";
            var correlationpropertytbody = document.getElementById("correlationpropertytbody");

            var correlationPropertyRaw = document.createElement("tr");
            correlationPropertyRaw.setAttribute("id", "correlationPropertyRaw" + i);

            var correlationNameTD = document.createElement("td");
            correlationNameTD.innerHTML = "<input type='text' name='correlationPropertyName" + i + "' id='correlationPropertyName" + i + "'" +
                " />";

            var correlationTypeTD = document.createElement("td");
            correlationTypeTD.appendChild(createproperttypecombobox('correlationPropertyTypeSelection' + i, i, name, propertytype));

            var correlationValueTD = document.createElement("td");
            correlationValueTD.innerHTML = "<input type='text' name='correlationPropertyValue" + i + "' id='correlationPropertyValue" + i + "'" +
                " class='esb-edit small_textbox' />";
            var correlationNsTD = document.createElement("td");
            correlationNsTD.setAttribute("id", "correlationNsEditorButtonTD" + i);
            correlationNsTD.style.display = displayStyleOfNSEditor;

            var correlationValueTypeTD = document.createElement("td");
            correlationValueTypeTD.appendChild(createpropertvaluetypecombobox('correlationPropertyValueTypeSelection' + i, i, name));

            var correlationDeleteTD = document.createElement("td");
            correlationDeleteTD.innerHTML = "<a href='#' class='delete-icon-link' onclick='deleteCorrelationProperty(" + i + ");return false;'>" + publishEventMediatorJsi18n["mediator.publishEvent.action.delete"] + "</a>";

            correlationPropertyRaw.appendChild(correlationNameTD);
            correlationPropertyRaw.appendChild(correlationTypeTD);
            correlationPropertyRaw.appendChild(correlationValueTD);
            correlationPropertyRaw.appendChild(correlationNsTD);
            correlationPropertyRaw.appendChild(correlationValueTypeTD);
            correlationPropertyRaw.appendChild(correlationDeleteTD);

            correlationpropertytbody.appendChild(correlationPropertyRaw);
            return true;

        case "payload":
            var displayStyleOfNSEditor = document.getElementById('payload-ns-editor-th').style.display;

            var payloadPropertyCount = document.getElementById("payloadPropertyCount");
            var i = payloadPropertyCount.value;

            var currentCount = parseInt(i);
            currentCount = currentCount + 1;

            payloadPropertyCount.value = currentCount;

            var payloadpropertytable = document.getElementById("payloadpropertytable");
            payloadpropertytable.style.display = "";
            var payloadpropertytbody = document.getElementById("payloadpropertytbody");

            var payloadPropertyRaw = document.createElement("tr");
            payloadPropertyRaw.setAttribute("id", "payloadPropertyRaw" + i);

            var payloadNameTD = document.createElement("td");
            payloadNameTD.innerHTML = "<input type='text' name='payloadPropertyName" + i + "' id='payloadPropertyName" + i + "'" +
                " />";

            var payloadTypeTD = document.createElement("td");
            payloadTypeTD.appendChild(createproperttypecombobox('payloadPropertyTypeSelection' + i, i, name, propertytype));

            var payloadValueTD = document.createElement("td");
            payloadValueTD.innerHTML = "<input type='text' name='payloadPropertyValue" + i + "' id='payloadPropertyValue" + i + "'" +
                " class='esb-edit small_textbox' />";
            var payloadNsTD = document.createElement("td");
            payloadNsTD.setAttribute("id", "payloadNsEditorButtonTD" + i);
            payloadNsTD.style.display = displayStyleOfNSEditor;

            var payloadValueTypeTD = document.createElement("td");
            payloadValueTypeTD.appendChild(createpropertvaluetypecombobox('payloadPropertyValueTypeSelection' + i, i, name));

            var payloadDeleteTD = document.createElement("td");
            payloadDeleteTD.innerHTML = "<a href='#' class='delete-icon-link' onclick='deletePayloadProperty(" + i + ");return false;'>" + publishEventMediatorJsi18n["mediator.publishEvent.action.delete"] + "</a>";

            payloadPropertyRaw.appendChild(payloadNameTD);
            payloadPropertyRaw.appendChild(payloadTypeTD);
            payloadPropertyRaw.appendChild(payloadValueTD);
            payloadPropertyRaw.appendChild(payloadNsTD);
            payloadPropertyRaw.appendChild(payloadValueTypeTD);
            payloadPropertyRaw.appendChild(payloadDeleteTD);

            payloadpropertytbody.appendChild(payloadPropertyRaw);
            return true;

        case "arbitrary":
            var displayStyleOfNSEditor = document.getElementById('arbitrary-ns-editor-th').style.display;

            var arbitraryPropertyCount = document.getElementById("arbitraryPropertyCount");
            var i = arbitraryPropertyCount.value;

            var currentCount = parseInt(i);
            currentCount = currentCount + 1;

            arbitraryPropertyCount.value = currentCount;

            var arbitrarypropertytable = document.getElementById("arbitrarypropertytable");
            arbitrarypropertytable.style.display = "";
            var arbitrarypropertytbody = document.getElementById("arbitrarypropertytbody");

            var arbitraryPropertyRaw = document.createElement("tr");
            arbitraryPropertyRaw.setAttribute("id", "arbitraryPropertyRaw" + i);

            var arbitraryNameTD = document.createElement("td");
            arbitraryNameTD.innerHTML = "<input type='text' name='arbitraryPropertyName" + i + "' id='arbitraryPropertyName" + i + "'" +
                " />";

            var arbitraryTypeTD = document.createElement("td");
            arbitraryTypeTD.appendChild(createproperttypecombobox('arbitraryPropertyTypeSelection' + i, i, name, propertytype));

            var arbitraryValueTD = document.createElement("td");
            arbitraryValueTD.innerHTML = "<input type='text' name='arbitraryPropertyValue" + i + "' id='arbitraryPropertyValue" + i + "'" +
                " class='esb-edit small_textbox' />";
            var arbitraryNsTD = document.createElement("td");
            arbitraryNsTD.setAttribute("id", "arbitraryNsEditorButtonTD" + i);
            arbitraryNsTD.style.display = displayStyleOfNSEditor;

            var arbitraryValueTypeTD = document.createElement("td");
            arbitraryValueTypeTD.appendChild(createarbitrarypropertvaluetypecombobox('arbitraryPropertyValueTypeSelection' + i, i, name));

            var arbitraryDeleteTD = document.createElement("td");
            arbitraryDeleteTD.innerHTML = "<a href='#' class='delete-icon-link' onclick='deletearbitraryProperty(" + i + ");return false;'>" + publishEventMediatorJsi18n["mediator.publishEvent.action.delete"] + "</a>";

            arbitraryPropertyRaw.appendChild(arbitraryNameTD);
            arbitraryPropertyRaw.appendChild(arbitraryTypeTD);
            arbitraryPropertyRaw.appendChild(arbitraryValueTD);
            arbitraryPropertyRaw.appendChild(arbitraryNsTD);
            arbitraryPropertyRaw.appendChild(arbitraryValueTypeTD);
            arbitraryPropertyRaw.appendChild(arbitraryDeleteTD);

            arbitrarypropertytbody.appendChild(arbitraryPropertyRaw);
            return true;
    }


}

function isValidProperties(nameemptymsg, valueemptymsg, propertytype) {

    var nsCount = document.getElementById(propertytype + "PropertyCount");
    var i = nsCount.value;

    var currentCount = parseInt(i);

    if (currentCount >= 1) {
        for (var k = 0; k < currentCount; k++) {
            var prefix = document.getElementById(propertytype + "PropertyName" + k);
            if (prefix != null && prefix != undefined) {
                if (prefix.value == "") {
                    CARBON.showWarningDialog(nameemptymsg);
                    return false;
                }
            }
            var uri = document.getElementById(propertytype + "PropertyValue" + k);
            if (uri != null && uri != undefined) {
                if (uri.value == "") {
                    CARBON.showWarningDialog(valueemptymsg);
                    return false;
                }
            }
        }
    }
    return true;
}


function createproperttypecombobox(id, i, name, propertytype) {
    // Create the element:
    var combo_box = document.createElement('select');

    // Set some properties:
    combo_box.name = id;
    combo_box.setAttribute("id", id);

    if (propertytype == "meta") {
        combo_box.onchange = function () {
            onMetaPropertyTypeSelectionChange(i, name)
        };
    } else if (propertytype == "correlation") {
        combo_box.onchange = function () {
            onCorrelationPropertyTypeSelectionChange(i, name)
        };

    } else if (propertytype == "payload") {
        combo_box.onchange = function () {
            onPayloadPropertyTypeSelectionChange(i, name)
        };


    }else if (propertytype == "arbitrary") {
        combo_box.onchange = function () {
            onArbitraryPropertyTypeSelectionChange(i, name)
        };


    }

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

function createpropertvaluetypecombobox(id, i, name, propertytype) {
    // Create the element:
    var combo_box = document.createElement('select');

    // Set some properties:
    combo_box.name = id;
    combo_box.setAttribute("id", id);
    combo_box.onchange = function () {
        onPropertyValueTypeSelectionChange(i, name, propertytype)
    };
    // Add some choices:
    var choice = document.createElement('option');
    choice.value = 'STRING';
    choice.appendChild(document.createTextNode('STRING'));
    combo_box.appendChild(choice);

    choice = document.createElement('option');
    choice.value = 'INTEGER';
    choice.appendChild(document.createTextNode('INTEGER'));
    combo_box.appendChild(choice);

    var choice = document.createElement('option');
    choice.value = 'BOOLEAN';
    choice.appendChild(document.createTextNode('BOOLEAN'));
    combo_box.appendChild(choice);

    choice = document.createElement('option');
    choice.value = 'DOUBLE';
    choice.appendChild(document.createTextNode('DOUBLE'));
    combo_box.appendChild(choice);

    var choice = document.createElement('option');
    choice.value = 'FLOAT';
    choice.appendChild(document.createTextNode('FLOAT'));
    combo_box.appendChild(choice);

    choice = document.createElement('option');
    choice.value = 'LONG';
    choice.appendChild(document.createTextNode('LONG'));
    combo_box.appendChild(choice);

    return combo_box;
}

function createarbitrarypropertvaluetypecombobox(id, i, name, propertytype) {
    // Create the element:
    var combo_box = document.createElement('select');

    // Set some properties:
    combo_box.name = id;
    combo_box.setAttribute("id", id);
    combo_box.onchange = function () {
        onPropertyValueTypeSelectionChange(i, name, propertytype)
    };
    // Add some choices:
    var choice = document.createElement('option');
    choice.value = 'STRING';
    choice.appendChild(document.createTextNode('STRING'));
    combo_box.appendChild(choice);;

    return combo_box;
}

function deleteMetaProperty(i) {
    CARBON.showConfirmationDialog(publishEventMediatorJsi18n["mediator.publishEvent.delete.confirm"], function () {
        var propRow = document.getElementById("metaPropertyRaw" + i);
        if (propRow != undefined && propRow != null) {
            var parentTBody = propRow.parentNode;
            if (parentTBody != undefined && parentTBody != null) {
                parentTBody.removeChild(propRow);
                if (!isContainRaw(parentTBody)) {
                    var propertyTable = document.getElementById("metapropertytable");
                    propertyTable.style.display = "none";
                }
            }
        }
    });
}

function deleteCorrelationProperty(i) {
    CARBON.showConfirmationDialog(publishEventMediatorJsi18n["mediator.publishEvent.delete.confirm"], function () {
        var propRow = document.getElementById("correlationPropertyRaw" + i);
        if (propRow != undefined && propRow != null) {
            var parentTBody = propRow.parentNode;
            if (parentTBody != undefined && parentTBody != null) {
                parentTBody.removeChild(propRow);
                if (!isContainRaw(parentTBody)) {
                    var propertyTable = document.getElementById("correlationpropertytable");
                    propertyTable.style.display = "none";
                }
            }
        }
    });
}

function deletePayloadProperty(i) {
    CARBON.showConfirmationDialog(publishEventMediatorJsi18n["mediator.publishEvent.delete.confirm"], function () {
        var propRow = document.getElementById("payloadPropertyRaw" + i);
        if (propRow != undefined && propRow != null) {
            var parentTBody = propRow.parentNode;
            if (parentTBody != undefined && parentTBody != null) {
                parentTBody.removeChild(propRow);
                if (!isContainRaw(parentTBody)) {
                    var propertyTable = document.getElementById("payloadpropertytable");
                    propertyTable.style.display = "none";
                }
            }
        }
    });
}

function deleteArbitraryProperty(i) {
    CARBON.showConfirmationDialog(publishEventMediatorJsi18n["mediator.publishEvent.delete.confirm"], function () {
        var propRow = document.getElementById("arbitraryPropertyRaw" + i);
        if (propRow != undefined && propRow != null) {
            var parentTBody = propRow.parentNode;
            if (parentTBody != undefined && parentTBody != null) {
                parentTBody.removeChild(propRow);
                if (!isContainRaw(parentTBody)) {
                    var propertyTable = document.getElementById("arbitrarypropertytable");
                    propertyTable.style.display = "none";
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


function onMetaPropertyTypeSelectionChange(i, name) {
    var propertyType = getSelectedValue('metaPropertyTypeSelection' + i);
    if (propertyType != null) {
        settype(propertyType, i, name, "meta");
    }
}

function onCorrelationPropertyTypeSelectionChange(i, name) {
    var propertyType = getSelectedValue('correlationPropertyTypeSelection' + i);
    if (propertyType != null) {
        settype(propertyType, i, name, "correlation");
    }
}

function onPayloadPropertyTypeSelectionChange(i, name) {
    var propertyType = getSelectedValue('payloadPropertyTypeSelection' + i);
    if (propertyType != null) {
        settype(propertyType, i, name, "payload");
    }
}
function onArbitraryPropertyTypeSelectionChange(i, name) {
    var propertyType = getSelectedValue('arbitraryPropertyTypeSelection' + i);
    if (propertyType != null) {
        settype(propertyType, i, name, "arbitrary");
    }
}

function settype(type, i, name, propertytype) {
    var nsEditorButtonTD = document.getElementById(propertytype + "NsEditorButtonTD" + i);

    if (nsEditorButtonTD == null || nsEditorButtonTD == undefined) {
        return;
    }
    if ("expression" == type) {
        resetDisplayStyle("", propertytype);
        nsEditorButtonTD.innerHTML = "<a href='#nsEditorLink' class='nseditor-icon-link' style='padding-left:40px' onclick=\"showNameSpaceEditor('" + propertytype + "PropertyValue" + i + "')\">" + name + "</a>";
    } else {
        nsEditorButtonTD.innerHTML = "";
        if (!isRemainPropertyExpressions(propertytype)) {
            resetDisplayStyle("none", propertytype);
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

function resetDisplayStyle(displayStyle, propretytype) {
    document.getElementById(propretytype + '-ns-editor-th').style.display = displayStyle;
    var nsCount = document.getElementById(propretytype + "PropertyCount");
    var i = nsCount.value;

    var currentCount = parseInt(i);

    if (currentCount >= 1) {
        for (var k = 0; k < currentCount; k++) {
            var nsEditorButtonTD = document.getElementById(propretytype + "NsEditorButtonTD" + k);
            if (nsEditorButtonTD != undefined && nsEditorButtonTD != null) {
                nsEditorButtonTD.style.display = displayStyle;
            }
        }
    }
}

function isRemainPropertyExpressions(propertytype) {
    var nsCount = document.getElementById(propertytype + "PropertyCount");
    var i = nsCount.value;

    var currentCount = parseInt(i);

    if (currentCount >= 1) {
        for (var k = 0; k < currentCount; k++) {
            var propertyType = getSelectedValue(propertytype + 'PropertyTypeSelection' + k);
            if ("expression" == propertyType) {
                return true;
            }
        }
    }
    return false;
}

function publishEventMediatorValidate() {
    var name = document.getElementById('mediator.publishEvent.stream.name');
    if (name && name.value == "") {
        CARBON.showErrorDialog(publishEventMediatorJsi18n["specify.StreamName"]);
        return false;
    }
    var version = document.getElementById('mediator.publishEvent.stream.version');
    if (version && version.value == "") {
        CARBON.showErrorDialog(publishEventMediatorJsi18n["specify.StreamVersion"]);
        return false;
    }

    if (!isValidProperties(publishEventMediatorJsi18n["mediator.publishEvent.property.name.empty"], publishEventMediatorJsi18n["mediator.publishEvent.property.value.empty"], "meta")) {
        return false;
    }
    if (!isValidProperties(publishEventMediatorJsi18n["mediator.publishEvent.property.name.empty"], publishEventMediatorJsi18n["mediator.publishEvent.property.value.empty"], "correlation")) {
        return false;
    }

    if (!isValidProperties(publishEventMediatorJsi18n["mediator.publishEvent.property.name.empty"], publishEventMediatorJsi18n["mediator.publishEvent.property.value.empty"], "payload")) {
        return false;
    }

    return true;
}

function onPropertyValueTypeSelectionChange(i, name, propertytype) {
    return getSelectedValue(propertytype + 'PropertyTypeSelection' + i);
}
