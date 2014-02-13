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

function throttleMediatorValidate() {
    var throttleId = document.getElementById("throttle_id1");
    if (throttleId && throttleId.value == "") {
        CARBON.showErrorDialog(thottleMediatorJsi18n["throttle.id.required"]);
        return false;
    }

//    var policyInlineOpt = document.getElementById("policygroupInlineId");
//    if (policyInlineOpt && policyInlineOpt.checked) {
//        var throttleInlinePolicyValueId = document.getElementById("inlinepolicy_value");
//        if (throttleInlinePolicyValueId && throttleInlinePolicyValueId.value == "") {
//            CARBON.showErrorDialog(jsi18n["throttle.policyval.required"]);
//            return false;
//        }
//    }

//    var policyValueOpt = document.getElementById("policygroupValueId");
//    if (policyValueOpt && policyValueOpt.checked) {
//        var throttleregPolicyId = document.getElementById("mediator.throttle.regPolicy");
//        if (throttleregPolicyId && throttleregPolicyId.value.replace(/\s+/g,'') == "") {
//            CARBON.showErrorDialog(thottleMediatorJsi18n["throttle.policykey.required"]);
//            return false;
//        }
//    }

    return true;
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
            ele.innerHTML = '<div id=\"' + id + '\">' +
                            '<table><tbody><tr><td>Prefix</td><td><input width="80" type="text" id=\"' + prefix + '\"+ ' +
                            'name=\"' + prefix + '\" value=""/></td></tr><tr><td>URI</td><td><input width="80" ' +
                            'type="text" id=\"' + uri + '\"+ name=\"' + uri + '\"+ value=""/></td></tr></tbody></table></div>';
        }
    }
}

function showInLinedThrottlePolicyEditor(id) {

    if (id == null || id == undefined || id == "") {
        CARBON.showInfoDialog("ID cannot be null or empty");
    }

    var loadingContent = "<div id='workArea'><div id='popupContent'><div class='ajax-loading-message'> <img src='../resources/images/ajax-loader.gif' align='top'/> <span>Throttle policy editor loading please wait ..</span> </div></div></div>";
    CARBON.showPopupDialog(loadingContent, thottleMediatorJsi18n["throttle.policy.editor"], 500, false, null, 900);
    var random = Math.floor(Math.random() * 2000);
    var url = '../throttling/trottle-policy-editor_ajaxprocessor.jsp?popup=true&policyID=' + id ;
    jQuery("#popupContent").load(url, null,
            function(res, status, t) {
                if (status != "success") {
                    CARBON.showWarningDialog(thottleMediatorJsi18n["throttle.policy.error"]);
                }
            });

    return false;
}


