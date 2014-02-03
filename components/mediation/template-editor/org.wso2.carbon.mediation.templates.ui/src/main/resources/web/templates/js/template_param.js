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

function addTemplateParameter() {

    /*
     if (!isValidProperties(nameemptymsg, valueemptymsg)) {
     return false;
     }
     */
    var propertyCount = document.getElementById("templatePropertyCount");
    var i = propertyCount.value;
    var currentCount = parseInt(i);

//    handleParamAdd('template', currentCount);
    currentCount = currentCount + 1;

    propertyCount.value = currentCount;

    var propertytable = document.getElementById("templatePropertyTable");
    propertytable.style.display = "";
    var propertytbody = document.getElementById("templatePropertyBody");

    var propertyRaw = document.createElement("tr");
    propertyRaw.setAttribute("id", "templatePropertyRaw" + i);

    var nameTD = document.createElement("td");
    nameTD.innerHTML = "<input type='text' name='templatePropertyName" + i + "' id='templatePropertyName" + i + "'" +
                       "class='esb-edit small_textbox' />";


    var deleteTD = document.createElement("td");
    deleteTD.innerHTML = "<a href='#' class='delete-icon-link' onclick='deleteTemplateProperty(" + i + ")'>" + seqEditi18n["template.parameter.delete"] + "</a>";
//    deleteTD.innerHTML =  "<a href='#' class='delete-icon-link' onclick='deleteproperty(" + i + ");return false;'>" + logi18n["mediator.log.action.delete"] + "</a>";

    propertyRaw.appendChild(nameTD);
    propertyRaw.appendChild(deleteTD);

    propertytbody.appendChild(propertyRaw);
    return true;
}

function deleteProperty(i) {
    var propRow = document.getElementById("templatePropertyRaw" + i);
    var propName = document.getElementById("templatePropertyName" + i);
    if (propRow != undefined && propRow != null) {
        var parentTBody = propRow.parentNode;
        if (parentTBody != undefined && parentTBody != null) {
            parentTBody.removeChild(propRow);
            if (!isContainRaw(parentTBody)) {
                var propertyTable = document.getElementById("templatePropertyTable");
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


function handleParamAdd(templateName, count) {
    var propertyReqStr = "";
    i = 0;
    j = 0;
    for (i = 0; i < count; i++) {
        var paramElem = document.getElementById("templatePropertyName" + i);
        if (paramElem != null) {
            var paramName = paramElem.value;
            if (paramName != null && paramName != "") {
                propertyReqStr = propertyReqStr + "&param" + j + "=" + paramName;
                j++;
            }
        }
    }
    jQuery.ajax({
                    type: 'POST',
                    url: '../templates/process_template_params-ajaxprocessor.jsp',
                    data: 'paramCount=' + (j + 1) + propertyReqStr,
                    success: function(msg) {
                        handleSuccess(msg);
                    },
                    error: function(msg) {
//                        CARBON.showErrorDialog('<fmt:message key="template.trace.enable.link"/>' +
//                                               ' ' + templateName);
                    },
                    async: false

                });
}

function getParamCount() {
    var propertyCount = document.getElementById("templatePropertyCount");
    var i = propertyCount.value;
    return parseInt(i);
}

function handleSuccess(msg) {

}
