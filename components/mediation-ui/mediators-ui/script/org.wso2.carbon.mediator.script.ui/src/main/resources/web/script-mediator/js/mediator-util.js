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


function scriptMediatorValidate() {
    var script_type = document.getElementById('script_type');
    if (script_type.value == 'regKey') {
        var functionname = document.getElementById('mediator.script.function');
        if (functionname == null || trim(functionname.value) == "" || functionname == undefined || functionname.value == undefined) {
            CARBON.showWarningDialog(scripti18n["mediator.script.function.empty"]);
            return false;
        }
        var reg_key = document.getElementById('regkey');
        var keyGroup = document.getElementById("keyGroupDynamic");
        if (keyGroup != null && keyGroup.checked) {
            reg_key = document.getElementById("mediator.script.key.dynamic_val");
        } else {
            reg_key = document.getElementById("mediator.script.key.static_val");
        }

        if (reg_key && reg_key.value == "") {
            CARBON.showWarningDialog(scripti18n["mediator.script.regkey.empty"])
            return false;
        }
    } else if (script_type.value == 'inline') {
        var key = document.getElementById('mediator.script.source_script');
        if (key == null || key.value == "" || key == undefined || key.value == undefined) {
            CARBON.showWarningDialog(scripti18n["mediator.script.source.empty"]);
            return false;
        }
    }
    return isValidIncludeKey(scripti18n["mediator.script.include.empty"]);
}

function changeFields(script_type) {
    if (script_type == 'inline')
    {
        document.getElementById('source_script').style.display = '';
        document.getElementById('function_row').style.display = 'none';
        document.getElementById('key_type_raw').style.display = 'none';
        document.getElementById('mediator.script.key.static').style.display = 'none';
        document.getElementById('mediator.script.key.dynamic').style.display = 'none';
        document.getElementById('include_key').style.display = 'none';
        document.getElementById('include_key_link').style.display = 'none';
        document.getElementById('')
    }
    if (script_type == 'regKey')
    {
        document.getElementById('source_script').style.display = 'none';
        document.getElementById('function_row').style.display = '';
        document.getElementById('key_type_raw').style.display = '';
        document.getElementById('keyGroupStatic').checked = true;
        document.getElementById('mediator.script.key.static').style.display = '';
        document.getElementById('mediator.script.key.dynamic').style.display = 'none';
        document.getElementById('key_type_raw').style.display = '';
        document.getElementById('include_key').style.display = '';
        document.getElementById('include_key_link').style.display = '';
    }
}

function addIncludeKey(keyemptymsg) {
    if (!isValidIncludeKey(keyemptymsg)) {
        return false;
    }

    var includeKeyCount = document.getElementById('includeKeyCount');
    var i = includeKeyCount.value;

    var currentCount = parseInt(i);
    currentCount = currentCount + 1;

    includeKeyCount.value = currentCount;

    var includeKeytable = document.getElementById('includeKeytable');
    includeKeytable.style.display = "";

    var includeKeytbody = document.getElementById('includeKeytbody');

    var keyRaw = document.createElement("tr");
    keyRaw.setAttribute("id", "includeKeyRaw" + i);

    var keyTD = document.createElement('td');
    keyTD.setAttribute('width', '6%');
    keyTD.innerHTML = "Key";

    var keyNameTD = document.createElement("td");
    keyNameTD.setAttribute('width', '10%');
    keyNameTD.innerHTML = "<input class='longInput' type='text' id='includeKey" + i + "' name='includeKey" + i + "' value='' readonly='true'/>" ;

    var registryID = document.createElement("td");
    registryID.setAttribute('width', '15%');
    registryID.innerHTML = "<a href='#registryBrowserLink'  class='registry-picker-icon-link' "+
                           "onclick=\"showRegistryBrowser('includeKey" + i +"','/_system/config'"+ ")\">Configuration Registry</a>"+
                           "<a href='#registryBrowserLink'  class='registry-picker-icon-link' "+
                           "onclick=\"showRegistryBrowser('includeKey" + i +"','/_system/governance'"+ ")\">Governance Registry</a>";

    var delTD = document.createElement("td");
    delTD.innerHTML = "<a href='#' class='delete-icon-link' onclick='deleteIncludeKey(" + i + ")'>Delete</a>";

    keyRaw.appendChild(keyTD);
    keyRaw.appendChild(keyNameTD);
    keyRaw.appendChild(registryID);
    keyRaw.appendChild(delTD);

    includeKeytbody.appendChild(keyRaw);
    return true;
}

function deleteIncludeKey(i) {
    var keyRow = document.getElementById("includeKeyRaw" + i);
    if (keyRow != undefined && keyRow != null) {
        var parentTBody = keyRow.parentNode;
        if (parentTBody != undefined && parentTBody != null) {
            parentTBody.removeChild(keyRow);
            if (!isContainRaw(parentTBody)) {
                var includeKeyTbody = document.getElementById('includeKeytable');
                includeKeyTbody.style.display = 'none';
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

function isValidIncludeKey(nameemptymsg) {
    var nsCount = document.getElementById('includeKeyCount');
    var i = nsCount.value;
    var currentCount = parseInt(i);
    if (currentCount >= 1) {
        for (var k = 0; k < currentCount; k++) {
            var prefix = document.getElementById("includeKey" + k );
            if (prefix != null && prefix != undefined) {
                if (prefix.value == "") {
                    CARBON.showWarningDialog(nameemptymsg);
                    return false;
                }
            }
        }
    }
    return true;
}

function trim(str){
    return str.replace(/^\s\s*/, '').replace(/\s\s*$/, '');
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