/*
 *  Copyright (c) 2019, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
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

function addproperty(nameemptymsg, valueemptymsg) {

    if (!isValidProperties(nameemptymsg, valueemptymsg)) {
        return false;
    }

    var propertyCount = document.getElementById("propertyCount");
    var i = propertyCount.value;

    var currentCount = parseInt(i);
    currentCount = currentCount + 1;

    propertyCount.value = currentCount;

    var propertytable = document.getElementById("propertytable");
    propertytable.style.display = "";
    var propertytbody = document.getElementById("propertytbody");

    var propertyRaw = document.createElement("tr");
    propertyRaw.setAttribute("id", "propertyRaw" + i);

    var nameTD = document.createElement("td");
    nameTD.innerHTML = "<input type='text' name='propertyName" + i + "' id='propertyName" + i + "'" +
                       " />";

    var valueTD = document.createElement("td");
    valueTD.innerHTML = "<input type='text' name='propertyValue" + i + "' id='propertyValue" + i + "'" +
                        " class='esb-edit small_textbox' />";

    var deleteTD = document.createElement("td");
    deleteTD.innerHTML =  "<a href='#' class='delete-icon-link' onclick='deleteproperty(" + i + ");return false;'>" +
        logi18n["mediator.jsontransform.action.delete"] + "</a>";

    propertyRaw.appendChild(nameTD);
    propertyRaw.appendChild(valueTD);
    propertyRaw.appendChild(deleteTD);

    propertytbody.appendChild(propertyRaw);
    return true;
}

function isValidProperties(nameemptymsg, valueemptymsg) {

    var nsCount = document.getElementById("propertyCount");
    var i = nsCount.value;

    var currentCount = parseInt(i);

    if (currentCount >= 1) {
        for (var k = 0; k < currentCount; k++) {
            var prefix = document.getElementById("propertyName" + k);
            if (prefix != null && prefix != undefined) {
                if (prefix.value == "") {
                    CARBON.showWarningDialog(nameemptymsg)
                    return false;
                }
            }
            var uri = document.getElementById("propertyValue" + k);
            if (uri != null && uri != undefined) {
                if (uri.value == "") {
                    CARBON.showWarningDialog(valueemptymsg)
                    return false;
                }
            }
        }
    }
    return true;
}

function deleteproperty(i) {
    CARBON.showConfirmationDialog(logi18n["mediator.jsontransform.delete.confirm"],function(){
    var propRow = document.getElementById("propertyRaw" + i);
    if (propRow != undefined && propRow != null) {
        var parentTBody = propRow.parentNode;
        if (parentTBody != undefined && parentTBody != null) {
            parentTBody.removeChild(propRow);
            if (!isContainRaw(parentTBody)) {
                var propertyTable = document.getElementById("propertytable");
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


function jsontransformMediatorValidate() {
    return isValidProperties(logi18n["mediator.jsontransform.property.name.empty"], logi18n["mediator.jsontransform.property.value.empty"]);
}

