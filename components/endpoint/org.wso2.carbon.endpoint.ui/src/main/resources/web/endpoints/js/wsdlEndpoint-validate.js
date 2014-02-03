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

function validateWSDLEndpoint(isAnonymous, isFromTemplateEditor) {

    if (!isAnonymous) {
        if (isEmptyField('endpointName')) {
            CARBON.showWarningDialog(jsi18n['name.field.cannot.be.empty']);
            return false;
        }
    }

    if (isEmptyField('uriWSDLVal')) {
        CARBON.showWarningDialog(jsi18n['wsdl.uri.field.cannot.be.empty']);
        return false;
    }
    if (isEmptyField('wsdlendpointService')) {
        CARBON.showWarningDialog(jsi18n['service.field.cannot.be.empty']);
        return false;
    }
    if (isEmptyField('wsdlendpointPort')) {
        CARBON.showWarningDialog(jsi18n['port.field.cannot.be.empty']);
        return false;
    }

    if (!isFromTemplateEditor) {

        var wsdluri = trim(document.getElementById('uriWSDLVal').value);
        if (!isValidWSDLURL(wsdluri)) {
            CARBON.showWarningDialog(jsi18n['invalid.address.cannot.proceed']);
            return false;
        }

        var durationVal = getElementValue('suspendDuration');
        if (durationVal != null) {
            if (isNaN(durationVal)) {
                CARBON.showWarningDialog(jsi18n['please.enter.a.numeric.value.to.the.suspend.duration.seconds.field']);
                return false;
            }
        }

        var maxDurationVal = getElementValue('suspendMaxDuration');
        if (maxDurationVal != null) {
            if (isNaN(maxDurationVal)) {
                CARBON.showWarningDialog(jsi18n['please.enter.a.valid.number.to.the.max.duration.seconds.field']);
                return false;
            }
        }

        var factorVal = getElementValue('factor');
        if (factorVal != null) {
            if (isNaN(factorVal)) {
                CARBON.showWarningDialog(jsi18n['please.enter.a.valid.number.to.the.factor.field']);
                return false;
            }
        }

        var retryDelay = getElementValue('retryDelay');
        if (retryDelay != null) {
            if (isNaN(retryDelay)) {
                CARBON.showWarningDialog(jsi18n['please.enter.a.valid.number.to.the.retrydelay.field']);
                return false;
            }
        }

        var retryTimeoutVal = getElementValue('retryTimeOut');
        if (retryTimeoutVal != null) {
            if (isNaN(retryTimeoutVal)) {
                CARBON.showWarningDialog(jsi18n['please.enter.a.valid.number.to.the.retry.field']);
                return false;
            } else if (retryDelay != null && retryTimeoutVal > 0 && retryDelay == 0 ) {
                CARBON.showWarningDialog(jsi18n['please.enter.a.positive.number.to.the.retrydelay.field']);
                return false;
            }
        }
    } else {
        if (isEmptyField('templateName')) {
            CARBON.showWarningDialog(jsi18n['template.name.field.cannot.be.empty']);
            return false;
        }
    }
    return true;
}

// check if a valid WSDL
function isValidWSDL(WSDLDoc) {
    if (window.ActiveXObject) {
        try {
            var doc = new ActiveXObject("Microsoft.XMLDOM");
            doc.async = 'false';
            var isLoaded = doc.loadXML(WSDLDoc);
            if (!isLoaded) {
                return false;
            }
        } catch (e) {
            return false;
        }
    } else {
        var parser = new DOMParser();
        var dom = parser.parseFromString(WSDLDoc, "text/xml");
        if (dom.documentElement.nodeName == 'parsererror') {
            return false;
        }
    }
    if (WSDLDoc.indexOf("wsdl2") != -1) {
        CARBON.showWarningDialog(jsi18n['wsdl2.not.supported']);
        return false;
    } else if (WSDLDoc.indexOf('definitions') == -1) {
        CARBON.showWarningDialog(jsi18n['invalid.wsdl.definition.xml.parse.error']);
        return false;
    } else {
        return true;
    }
}