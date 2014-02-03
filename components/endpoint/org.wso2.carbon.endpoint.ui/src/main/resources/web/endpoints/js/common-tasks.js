/*
 ~  Copyright (c) 2012, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
 ~
 ~  Licensed under the Apache License, Version 2.0 (the "License");
 ~  you may not use this file except in compliance with the License.
 ~  You may obtain a copy of the License at
 ~
 ~        http://www.apache.org/licenses/LICENSE-2.0
 ~
 ~  Unless required by applicable law or agreed to in writing, software
 ~  distributed under the License is distributed on an "AS IS" BASIS,
 ~  WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 ~  See the License for the specific language governing permissions and
 ~  limitations under the License.
 */

function submitEndpointData(type, isAnonymous, isFromTemplateEditor) {

    if (!eval("validate" + type + "Endpoint(" + isAnonymous + "," + isFromTemplateEditor + ")")) {
        return false;
    }

    if (document.getElementById('endpointProperties') != null) {
        document.getElementById('endpointProperties').value = populateServiceParams("headerTable");
    }

    var options = {
        // dataType: 'text/xml',
        success:       directToSubmitPage
    };

    jQuery('#endpoint-editor-form').ajaxForm(options);
    jQuery('#endpoint-editor-form').submit();

}

function submitDynamicEndpointData(type, isFromTemplateEditor) {

    if (!eval("validate" + type + "Endpoint(false," + isFromTemplateEditor + ")")) {
        return false;
    }

    var key = document.getElementById('synRegKey').value;
    if (key == '') {
        CARBON.showWarningDialog(jsi18n['empty.key.field']);
        return false;
    }

    if (document.getElementById('endpointProperties') != null) {
        document.getElementById('endpointProperties').value = populateServiceParams("headerTable");
    }

    var options = {
        // dataType: 'text/xml',
        success:       directToSubmitDynamicEndpointPage
    };

    jQuery('#endpoint-editor-form').ajaxForm(options);
    jQuery('#endpoint-editor-form').submit();

    return true;
}

function directToSubmitDynamicEndpointPage() {

    var key = document.getElementById('synRegKey').value;
    var registry;
    if (document.getElementById("config_reg").checked == true) {
        registry = 'conf';
    } else {
        registry = 'gov';
    }

    jQuery.ajax({
                    type: 'POST',
                    url: 'ajaxprocessors/submitDynamicEndpoint-ajaxprocessor.jsp',
                    data: 'registry=' + registry + '&regKey=' + key,
                    success: function(msg) {
                        var msgString = trimStr(msg.toString())
                        var index = msgString.indexOf('<div>');
                        if (msgString.indexOf('<div>Error:') == index) {
                            CARBON.showErrorDialog(msgString.substring(index + 17));
                        } else {
                            location.href = msgString.substring(index + 17);
                        }
                    }
                });
}

function switchToSource(type, isAnonymous, isFromTemplateEditor) {

    if (!eval("validate" + type + "Endpoint(" + isAnonymous + "," + isFromTemplateEditor + ")")) {
        return false;
    }

    if (document.getElementById('endpointProperties') != null) {
        document.getElementById('endpointProperties').value = populateServiceParams("headerTable");
    }

    var options = {
        //dataType: 'text/xml',
        success:       directToSource
    };

    jQuery('#endpoint-editor-form').ajaxForm(options);
    jQuery('#endpoint-editor-form').submit();
}

function directToSource() {
    location.href = "sourceView.jsp?retainlastbc=true";
}

function directToSubmitPage() {

    jQuery.ajax({
                    type: 'POST',
                    url: 'ajaxprocessors/submitEndpoint-ajaxprocessor.jsp',
                    data: 'data=null',
                    success: function(msg) {
                        var msgString = trimStr(msg.toString())
                        var index = msgString.indexOf('<div>');

                        if (msgString.indexOf('<div>Error:') == index) {
                            CARBON.showErrorDialog(msgString.substring(index + 17));
                        } else {
                            location.href = msgString.substring(index + 17);
                        }
                    }
                });
}

function showSaveAsForm(show, isFromTemplateEditor) {
    var formElem = document.getElementById('saveAsForm');
    if (show) {
        formElem.style.display = "";
        var keyField = document.getElementById('synRegKey');
        if (keyField.value == '') {
            if (isFromTemplateEditor == true) {
                keyField.value = document.getElementById("templateName").value;
            } else {
                keyField.value = document.getElementById("endpointName").value;
            }
        }
    } else {
        formElem.style.display = "none"
    }
}
function trimStr(str) {
    return str.replace(/^\s+|\s+$/g, '');
}
