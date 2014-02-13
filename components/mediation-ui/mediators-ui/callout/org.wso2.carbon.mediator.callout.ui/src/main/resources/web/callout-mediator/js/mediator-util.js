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

function calloutMediatorValidate() {
    var target;
    var source;
    var isEnvelope = false;

    var sourceGroup = document.getElementById("sourceGroupXPath");
    var sourceGroupEnvelope = document.getElementById("sourceGroupEnvelope");
    if (sourceGroupEnvelope && sourceGroupEnvelope.checked) {
        isEnvelope = true;
    } else if (sourceGroup && sourceGroup.checked) {
        source = document.getElementById("mediator.callout.source.xpath_val");
    } else {
        source = document.getElementById("mediator.callout.source.key_val");
    }
    if (source && source.value == "" && isEnvelope == false) {
        CARBON.showErrorDialog(calloutMediatorJsi18n["callout.source.required"]);
        return false;
    }

    var targetGroup = document.getElementById("targetGroupXPath");
    if (targetGroup && targetGroup.checked) {
        target = document.getElementById("mediator.callout.target.xpath_val");
    } else {
        target = document.getElementById("mediator.callout.target.key_val");
    }
    if (target && target.value == "") {
        CARBON.showErrorDialog(calloutMediatorJsi18n["callout.target.required"]);
        return false;
    }

    var secElement = document.getElementById("wsSecurity");
    if (secElement && secElement.checked) {
        var useDifferentPoliciesElement = document.getElementById("wsSecurityUseDifferentPolicies");
        if (useDifferentPoliciesElement && useDifferentPoliciesElement.checked) {
            outboundPolicy = document.getElementById("wsSecOutboundPolicyKeyID");
            inboundPolicy = document.getElementById("wsSecInboundPolicyKeyID");
            if ((outboundPolicy && outboundPolicy.value == "") && (inboundPolicy && inboundPolicy.value == "")) {
                CARBON.showErrorDialog(calloutMediatorJsi18n["callout.policy.required"]);
                return false;
            }

        } else {
            policy = document.getElementById("wsSecPolicyKeyID");
            if (policy && policy.value == "") {
                CARBON.showErrorDialog(calloutMediatorJsi18n["callout.policy.required"]);
                return false;
            }
        }
    }

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
                            '<table><tbody><tr><td>Prefix</td><td><input width="80" type="text" id=\"'+ prefix + '\"+ ' +
                            'name=\"'+ prefix + '\" value=""/></td></tr><tr><td>URI</td><td><input width="80" ' +
                            'type="text" id=\"'+ uri + '\"+ name=\"'+ uri + '\"+ value=""/></td></tr></tbody></table></div>';
        }
    }
}

function showHideWSSecRows() {
    if (document.getElementById('wsSecurity').checked == true) {
        document.getElementById('tr_ws_sec_policy_key').style.display = '';
        document.getElementById('tr_ws_use_different_policies').style.display = '';

        if (document.getElementById('wsSecurityUseDifferentPolicies').checked == true) {
            document.getElementById('tr_ws_sec_policy_key').style.display = "none";
            document.getElementById('tr_ws_sec_inbound_policy_key').style.display = '';
            document.getElementById('tr_ws_sec_outbound_policy_key').style.display = '';
        }
    } else {
        document.getElementById('tr_ws_sec_policy_key').style.display = "none";
        document.getElementById('tr_ws_use_different_policies').style.display = "none";
        document.getElementById('tr_ws_sec_outbound_policy_key').style.display = "none";
        document.getElementById('tr_ws_sec_inbound_policy_key').style.display = "none";
    }
}

function showHideInOutWSSecRows() {
    if (document.getElementById('wsSecurityUseDifferentPolicies').checked == true) {
        document.getElementById('tr_ws_sec_policy_key').style.display = "none";
        document.getElementById('tr_ws_sec_inbound_policy_key').style.display = '';
        document.getElementById('tr_ws_sec_outbound_policy_key').style.display = '';
    } else {
        document.getElementById('tr_ws_sec_policy_key').style.display = '';
        document.getElementById('tr_ws_sec_outbound_policy_key').style.display = "none";
        document.getElementById('tr_ws_sec_inbound_policy_key').style.display = "none";
    }
}