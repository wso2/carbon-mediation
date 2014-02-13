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

function isValidKey(keyemptymsg) {
    var nsCount = document.getElementById("keyCount");
    var i = nsCount.value;
    var currentCount = parseInt(i);
    //currentCount = currentCount + 1;
    if (currentCount > 0) {
        for (var k = 0; k < currentCount; k++) {
            var prefix = document.getElementById("keyValue" + k);
            if (prefix != null && prefix != undefined) {
                if (prefix.value == "") {
                    CARBON.showWarningDialog(keyemptymsg)
                    return false;
                }
            }
        }
    }
    return true;
}

function isValidFeatures(nameemptymsg) {
    var nsCount = document.getElementById("featureCount");
    var i = nsCount.value;
    var currentCount = parseInt(i);
    if (currentCount >= 1) {
        for (var k = 0; k < currentCount; k++) {
            var prefix = document.getElementById("featureName" + k);
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

function addfeature(nameempty) {
    if (!isValidFeatures(nameempty)) {
        return false;
    }
    var featureCount = document.getElementById("featureCount");
    var i = featureCount.value;
    var currentCount = parseInt(i);
    currentCount = currentCount + 1;

    featureCount.value = currentCount;

    var featureTable = document.getElementById("featuretable");
    featureTable.style.display = "";
    var featuretbody = document.getElementById("featuretbody");

    var featureRaw = document.createElement("tr");
    featureRaw.setAttribute("id", "featureRaw" + i);

    var nameTD = document.createElement("td");
    nameTD.appendChild(createinputtextbox("featureName" + i), "");

    var valueTD = document.createElement("td");
    valueTD.appendChild(createboolselectelement("featureValue" + i));

    var deleteTD = document.createElement("td");
    deleteTD.innerHTML = "<a href='#' class='delete-icon-link' onclick='deletefeature(" + i + ");return false;'>Delete</a>";

    featureRaw.appendChild(nameTD);
    featureRaw.appendChild(valueTD)
    featureRaw.appendChild(deleteTD);

    featuretbody.appendChild(featureRaw);
    return true;
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

function deletefeature(i) {
    CARBON.showConfirmationDialog(validate18n["mediator.validate.delete.confirm"],function(){
    var featureRow = document.getElementById("featureRaw" + i);
    if (featureRow != undefined && featureRow != null) {
        var parentTBody = featureRow.parentNode;
        if (parentTBody != undefined && parentTBody != null) {
            parentTBody.removeChild(featureRow);
            if (!isContainRaw(parentTBody)) {
                var featureTable = document.getElementById("featuretable");
                featureTable.style.display = "none";
            }
        }
    }
    });
}

function deleteKey(i) {
    var n = document.getElementById("nKeys");
    var keycount = parseInt(n.value);
    if (keycount < 2) {
        CARBON.showWarningDialog(validate18n["mediator.validate.schema.key.none"]);
        return false;
    }
    CARBON.showConfirmationDialog(validate18n["mediator.validate.delete.confirm"],function(){
    var propRow = document.getElementById("keyRaw" + i);
    if (propRow != undefined && propRow != null) {
        var parentTBody = propRow.parentNode;
        if (parentTBody != undefined && parentTBody != null) {
            parentTBody.removeChild(propRow);
            if (!isContainRaw(parentTBody)) {
                var propertyTable = document.getElementById("keytable");
                propertyTable.style.display = "none";
            }
            var nKeys = document.getElementById("nKeys");
            var n = parseInt(nKeys.value) - 1;
            nKeys.value = n;
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

function validateMediatorValidate() {
    var nameemptymsg = validate18n["mediator.validate.feature.name.empty"];
    var keyemptymsg = validate18n["mediator.validate.schema.key.empty"];

    var nsCount = document.getElementById("keyCount");
    var i = nsCount.value;
    currentCount = parseInt(i);
    if (currentCount >= 0) {
        for (var k = 0; k <= currentCount; k++) {
            var prefix = document.getElementById("keyValue" + k);
            if (prefix != null && prefix != undefined) {
                if (prefix.value == "") {
                    CARBON.showWarningDialog(keyemptymsg)
                    return false;
                }
            }
        }
    }

    nsCount = document.getElementById("featureCount");
    i = nsCount.value;
    currentCount = parseInt(i);
    if (currentCount >= 1) {
        for (k = 0; k < currentCount; k++) {
            prefix = document.getElementById("featureName" + k);
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

function createboolselectelement(id) {
    // Create the element:
    var combo_box = document.createElement('select');

    // Set some properties:
    combo_box.name = id;
    combo_box.setAttribute("id", id);

    // Add some choices:
    var choice = document.createElement('option');
    choice.value = 'true';
    choice.appendChild(document.createTextNode(validate18n["mediator.validator.true"]));
    combo_box.appendChild(choice);

    choice = document.createElement('option');
    choice.value = 'false';
    choice.appendChild(document.createTextNode(validate18n["mediator.validator.false"]));
    combo_box.appendChild(choice);

    return combo_box;
}



function onKeyTypeSelectionChange(i, name) {
    var keyType = getSelectedValue('keyTypeSelection' + i);
    if (keyType != null) {
       settype(keyType, i, name);
    }

}

function settype(type, i, name) {

    var nsEditorButtonTD = document.getElementById("nsEditorButtonTD" + i);
    var regBrowserTD = document.getElementById("regBrowserTD" + i);

    var keyValue = document.getElementById("keyValue" + i);

    if ((nsEditorButtonTD == null || nsEditorButtonTD == undefined) && (regBrowserTD == null || regBrowserTD == undefined)) {
        return;
    }
    if ("dynamic" == type) {
        //resetDisplayStyle("");
        keyValue.readOnly = false;
        if (nsEditorButtonTD != undefined && nsEditorButtonTD != null) {
            nsEditorButtonTD.style.display = "";
            regBrowserTD.style.display = 'none';
        }
        nsEditorButtonTD.innerHTML = "<a href='#nsEditorLink' class='nseditor-icon-link' style='padding-left:40px' onclick=\"showNameSpaceEditor('keyValue" + i + "')\">" + name + "</a>";
        regBrowserTD.innerHTML = "";

    } else {
        keyValue.readOnly = true;
        nsEditorButtonTD.innerHTML = "";
        regBrowserTD.innerHTML = "<a href='#registryBrowserLink'  class='registry-picker-icon-link' " +
                                 "onclick=\"showRegistryBrowser('keyValue" + i + "','/_system/config'" + ")\">Configuration Registry</a>" +
                                 "<a href='#registryBrowserLink'  class='registry-picker-icon-link' " +
                                 "onclick=\"showRegistryBrowser('keyValue" + i + "','/_system/governance'" + ")\">Governance Registry</a>";

        if (regBrowserTD != undefined && regBrowserTD != null) {
            regBrowserTD.style.display = "";
            nsEditorButtonTD.style.display = 'none';
        }
    }
}

function addNewKey(keyEmptyMsg) {

    if (!isValidKey(keyEmptyMsg)) {
        return false;
   }

    var keyCount = document.getElementById("keyCount");
    var nKeys = document.getElementById("nKeys");
    var i = keyCount.value;

    var currentCount = parseInt(i);
    currentCount = currentCount + 1;

    keyCount.value = currentCount;

    var keyTable = document.getElementById("keyTable");
    keyTable.style.display = "";
    var keyTableBody = document.getElementById("keyTableBody");

    var keyRaw = document.createElement("tr");
    keyRaw.setAttribute("id", "keyRaw" + i);

    var typeTD = document.createElement("td");
    typeTD.appendChild(createKeyTypeComboBox('keyTypeSelection' + i, i, name))

    var valueTD = document.createElement("td");
    valueTD.innerHTML = "<input type='text' name='keyValue" + i + "' id='keyValue" + i + "' value='' readonly='true'/>";

    var nsTD = document.createElement("td");
    nsTD.setAttribute("id", "nsEditorButtonTD" + i);
    nsTD.style.display = 'none';

    var regBrowserTD = document.createElement("td");
    regBrowserTD.setAttribute("id", "regBrowserTD" + i);
    regBrowserTD.style.display = "";
    regBrowserTD.innerHTML = "<a href='#registryBrowserLink'  class='registry-picker-icon-link' " +
                             "onclick=\"showRegistryBrowser('keyValue" + i + "','/_system/config'" + ")\">Configuration Registry</a>" +
                             "<a href='#registryBrowserLink'  class='registry-picker-icon-link' " +
                             "onclick=\"showRegistryBrowser('keyValue" + i + "','/_system/governance'" + ")\">Governance Registry</a>";


    var deleteTD = document.createElement("td");
    //deleteTD.innerHTML = "<a href='#' class='delete-icon-link' onclick='deleteKey(" + i + ")' >" + validate18n["mediator.validate.action.delete"] + "</a>";
    deleteTD.innerHTML = "<a href='#' class='delete-icon-link' onclick='deleteKey(" + i + ");return false;'>" + validate18n["mediator.validate.action.delete"] + "</a>";
    // propertyRaw.appendChild(nameTD);
    keyRaw.appendChild(typeTD);
    keyRaw.appendChild(valueTD);
    keyRaw.appendChild(nsTD);
    keyRaw.appendChild(regBrowserTD);
    keyRaw.appendChild(deleteTD);

    keyTableBody.appendChild(keyRaw);
    nKeys.value = parseInt(nKeys.value) + 1;
    return true;
}

function createKeyTypeComboBox(id, i, name) {
    // Create the element:
    var combo_box = document.createElement('select');

    // Set some properties:
    combo_box.name = id;
    combo_box.setAttribute("id", id);
    combo_box.onchange = function () {
        onKeyTypeSelectionChange(i, name)
    };
    // Add some choices:
    var choice = document.createElement('option');
    choice.value = 'static';
    choice.appendChild(document.createTextNode(validate18n["mediator.validate.text.value"]));
    combo_box.appendChild(choice);

    choice = document.createElement('option');
    choice.value = 'dynamic';
    choice.appendChild(document.createTextNode(validate18n["mediator.validate.text.expression"]));
    combo_box.appendChild(choice);

    return combo_box;
}

function getSelectedValue(id) {
    var propertyType = document.getElementById(id);
    var propertyType_indexstr = null;
    var propertyType_value = null;
    if (propertyType != null) {
        propertyType_indexstr = propertyType.selectedIndex;
        if (propertyType_indexstr != null) {
            propertyType_value = propertyType.options[propertyType_indexstr].value;
            //alert("propertyType_value = " + propertyType_value);
        }
    }
    return propertyType_value;
}

var resources = Array();

function addResources() {
	var location = document.getElementById('locationText').value;
    var key = document.getElementById('resourceKey').value;
    // trim the input values
    location = location.replace(/^\s*/, "").replace(/\s*$/, "");
    key = key.replace(/^\s*/, "").replace(/\s*$/, "");
    if (location != '' && key != '') {
        if (isResourceAlreadyExists(location)) {        	
            CARBON.showWarningDialog(validate18n["mediator.validator.resource.already.exists"]);
            return;
        }
       
        addResourceRow(location, key);      
        document.getElementById('locationText').value="";
        document.getElementById('resourceKey').value="";
    } else {
        CARBON.showWarningDialog(validate18n["mediator.validator.empty.location.or.key"]);
    }
}

function addResourceRow(location, key) {	
    addRow(location, key, 'resourceTable', 'deleteResourceRow');
    var currentIndex = resources.push(new Array(2)) - 1;
    resources[currentIndex]['mediator.validator.resource.location'] = location;
    resources[currentIndex]['mediator.validator.resource.key'] = key;

    setResourceList();
}

function isResourceAlreadyExists(location) {
    var i;
    for (i = 0; i < resources.length; i++) {
        if (resources[i]['mediator.validator.resource.location'] == location) {
            return true;
        }
    }
    return false;
}

function setResourceList() {
    var i;
    var str = '';
    if (resources.length > 0) {
        str = resources[0]['mediator.validator.resource.location'] + ',' + resources[0]['mediator.validator.resource.key'];
        for (i = 1; i < resources.length; i++) {
            str += '::' + resources[i]['mediator.validator.resource.location'] + ',' + resources[i]['mediator.validator.resource.key'];
        }
    }
    document.getElementById('resourceList').value = str;
}

function deleteResourceRow(index) {
    CARBON.showConfirmationDialog(validate18n["mediator.xslt.confirm.resource.deletion"] , function() {
        document.getElementById('resourceTable').deleteRow(index);
        resources.splice(index-1, 1);
        if (resources.length == 0) {
            document.getElementById('resourceTable').style.display = 'none';
        }
        setResourceList();
    });
}

function addRow(param1, param2, table, delFunction) {
        var tableElement = document.getElementById(table);
        var param1Cell = document.createElement('td');
        param1Cell.appendChild(document.createTextNode(param1));

        var param2Cell = document.createElement('td');
        param2Cell.appendChild(document.createTextNode(param2));

        var delCell = document.createElement('td');
        delCell.innerHTML='<a id="deleteLink" href="#" onClick="' + delFunction + '(this.parentNode.parentNode.rowIndex)" alt="Delete" class="icon-link" style="background-image:url(../admin/images/delete.gif);">Delete</a>';

        var rowtoAdd = document.createElement('tr');
        rowtoAdd.appendChild(param1Cell);
        rowtoAdd.appendChild(param2Cell);
        rowtoAdd.appendChild(delCell);

        tableElement.tBodies[0].appendChild(rowtoAdd);
        tableElement.style.display = "";

        alternateTableRows(tableElement, 'tableEvenRow', 'tableOddRow');
    }
