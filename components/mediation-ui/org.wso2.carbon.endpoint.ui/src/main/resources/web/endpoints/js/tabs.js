/*
 *  Copyright (c) 2012, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
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

function executeShowDesign() {
    document.getElementById("childEndpointDesign").innerHTML = "";
    var url = 'listEndpointDesigner/childEndpoint-edit-ajaxprocessor.jsp';
    jQuery("#childEndpointDesign").load(url, null, function (responseText, status, XMLHttpRequest) {
        if (status != "success") {
            CARBON.showErrorDialog(jsi18n["mediator.design.load.error"]);
        }
    });

    showObj("childEndpoint-form-header");
    showObj("childEndpoint-form-tab");
    showObj("childEndpointDesign");
}

function addCustomParam(formData, jqForm, options) {
    formData[formData.length] = {name : "followupAction", value : "source"};
}

function hide(objid) {
    var theObj = document.getElementById(objid);
    theObj.style.display = "none";
}

function showObj(objid) {
    var theObj = document.getElementById(objid);
    theObj.style.display = "";
}

//This function returns the other li node from a 2 element ul
function slectOtheLi(theLi) {
    var theUL = theLi.parentNode;
    for (var i = 0; i < theUL.childNodes.length; i++) {
        if (theUL.childNodes[i].nodeName == "LI" && theUL.childNodes[i] != theLi) {
            return theUL.childNodes[i];
        }
    }
}