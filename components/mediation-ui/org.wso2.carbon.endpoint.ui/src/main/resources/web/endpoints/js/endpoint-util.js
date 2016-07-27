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

// This contains some utility functions shared by two or more endpoints

// check if a given field is empty
function isEmptyField(id) {
    var elementId = document.getElementById(id);
    if (elementId != null && elementId != undefined) {
        if (elementId.value == "" || elementId.value == null || elementId.value == undefined) {
            return true;
        }
    }
    return false;
}

function isValidName(id){
    var elementId = document.getElementById(id);
    var regEx = /[~!@#%^&*()\\\/+=\:;<>'"?[\]{}|\s,]|^$/;
    if (elementId != null && elementId != undefined) {
        if (regEx.test(elementId.value) || elementId.value == null || elementId.value == undefined) {
            return true;
        }
    }
    return false;
}

function hasKeyWord(id) {
    var elementId = document.getElementById(id);


    var endpointName=document.getElementById(id).value;
    var wordList=endpointName.match(/anonymous/g);

    if(wordList != null && wordList != undefined && wordList.length >0 ){
       return true;
    }
    return false;
}

// check the url is a valid one
function isValidURL(url) {
    if (url.search(/['",]/) != -1) { // we have , ' " in the URL
        return false;
    }
    var regx = RegExp("^\\w\\w+:/.*|file:.*|mailto:.*|vfs:.*");
    if (!(url.match(regx))) {
        return false;
    }
    return true;
}

function getElementValue(id) {
    var elementValue = document.getElementById(id);
    if (elementValue != null && elementValue != undefined) {
        elementValue = elementValue.value;
    }
    if (elementValue != null && elementValue != undefined) {
        return elementValue;
    }
    return null;
}

// set the action for time out
function activateDurationField(selectNode) {
    var selectOption = selectNode.options[selectNode.selectedIndex].value;
    var actionDuration = document.getElementById('actionDuration');
    if (selectOption != null && selectOption != undefined) {
        if (selectOption == 'neverTimeout') {
            if (actionDuration != null && actionDuration != undefined) {
                actionDuration.disabled = 'disabled';
                actionDuration.value = 0;
            }
        } else {
            if (actionDuration != null && actionDuration != undefined) {
                actionDuration.disabled = '';
            }
        }
    }
}

// a trim function- remove spaces
function trim(stringToTrim) {
    return stringToTrim.replace(/^\s+|\s+$/g, "");
}
function ltrim(stringToTrim) {
    return stringToTrim.replace(/^\s+/, "");
}
function rtrim(stringToTrim) {
    return stringToTrim.replace(/\s+$/, "");
}

function cancelEndpointData(annonOriginator, isFromTemplateEditor) {
    if (annonOriginator != 'null') {
        if (annonOriginator.toString().indexOf('../sequences') != -1) {
            annonOriginator = annonOriginator + '?cancelled=true&region=region1&item=sequences_menu';
        } else if (annonOriginator.toString().indexOf('../proxy') != -1) {
            annonOriginator = annonOriginator + '?cancelled=true&region=region1&item=proxy_services_menu';
        } else {
            annonOriginator = annonOriginator + '?cancelled=true';
        }
        location.href = annonOriginator;
    } else if (isFromTemplateEditor == 'true') {
        location.href = '../templates/list_templates.jsp?region=region1&item=templates_menu&ordinal=0';
    } else if (annonOriginator == 'null') {
        location.href = '../endpoints/index.jsp?region=region1&item=endpoints_menu&tabs=0';
    }
}

function XMLToString(xmlData) {
    var xmlDoc;
    if (window.ActiveXObject) {
        //for IE
        xmlDoc = new ActiveXObject("Microsoft.XMLDOM");
        xmlDoc.async = "false";
        xmlDoc.loadXML(xmlData);
        return xmlDoc.xml;
    } else if (document.implementation && document.implementation.createDocument) {
        //for Mozila
        parser = new DOMParser();
        xmlDoc = parser.parseFromString(xmlData, "text/xml");
        return (new XMLSerializer()).serializeToString(xmlDoc);
    }
    return null;
}

function xmlToString(xmlObj) {
    if (navigator.appName == "Netscape") {
        var str = new XMLSerializer().serializeToString(xmlObj);
        str = str.replace(' xmlns=""', "");
        return str;
    }
    if (navigator.appName == "Microsoft Internet Explorer") {
        return xmlObj.xml;
    }
    return null;
}

function getText(ele) {
    var strings = [];
    getStrings(ele, strings);
    return strings.join("");
}

function getStrings(n, strings) {
    if (n.nodeType == 3 /* Node.TEXT_NODE */) {
        strings.push(n.data);
    }
    else if (n.nodeType == 1 /* Node.ELEMENT_NODE */) {
        for (var m = n.firstChild; m != null; m = m.nextSibling) {
            getStrings(m, strings);
        }
    }
}

function replaceString(originalStr, originalword, relaceword) {
    if (originalStr == undefined || originalStr == null) {
        return null;
    }

    return originalStr.replace(new RegExp(originalword, 'g'), relaceword);
}

function testURL(url) {
    if (url == '') {
        CARBON.showWarningDialog(jsi18n['invalid.address.empty']);
    } else {
        jQuery.get("ajaxprocessors/testConnection-ajaxprocessor.jsp?type=address&", {'url' : url},
                   function(data, status) {
                       if (data.replace(/^\s+|\s+$/g, '') == 'success') {
                           CARBON.showInfoDialog(jsi18n['valid.address'] + " " + url);
                       } else if (data.replace(/^\s+|\s+$/g, '') == 'unknown') {
                           CARBON.showErrorDialog(jsi18n['unknown.address'] + " " + url);
                       } else if (data.replace(/^\s+|\s+$/g, '') == 'malformed') {
                           CARBON.showErrorDialog(jsi18n['malformed.address'] + " " + url);
                       } else if (data.replace(/^\s+|\s+$/g, '') == 'ssl_error') {
                           CARBON.showErrorDialog(jsi18n['ssl.error'] + " " + url);
                       } else if (data.replace(/^\s+|\s+$/g, '') == 'unknown_service') {
                           CARBON.showErrorDialog(jsi18n['unknown.service'] + " " + url);
                       } else if (data.replace(/^\s+|\s+$/g, '') == 'unsupported') {
                           CARBON.showErrorDialog(jsi18n['unsupported.protocol']);
                       } else {
                           CARBON.showErrorDialog(jsi18n['invalid.address']);
                       }
                   });
    }
}

function showAdvancedOptions(id) {
    var formElem = document.getElementById(id + '_advancedForm');
    if (formElem.style.display == 'none') {
        formElem.style.display = '';
        document.getElementById(id + '_adv').innerHTML = '<a class="icon-link" ' +
                                                         'onclick="javascript:showAdvancedOptions(\'' + id + '\');" style="background-image: url(images/up.gif);">' + jsi18n['hide.advanced.options'] + '</a>';
    } else {
        formElem.style.display = 'none';
        document.getElementById(id + '_adv').innerHTML = '<a class="icon-link" ' +
                                                         'onclick="javascript:showAdvancedOptions(\'' + id + '\');" style="background-image: url(images/down.gif);">' + jsi18n['show.advanced.options'] + '</a>';
    }
}


function convertToValidXML(inputXml) {
    var convertedXML = replaceString(inputXml, "&amp;", "&");
    return replaceString(convertedXML, "&", "&amp;");
}

function encodeCharacters(inputString) {
    return replaceString(inputString,"&","%26");
}

function isValidXml(inputXml) {
    var docStr = convertToValidXML(inputXml);

    if (window.ActiveXObject) {
        try {
            var doc = new ActiveXObject("Microsoft.XMLDOM");
            doc.async = "false";
            var hasParse = doc.loadXML(docStr);
            if (!hasParse) {
                CARBON.showErrorDialog('Invalid Configuration');
                return false;
            }
        } catch (e) {
            CARBON.showErrorDialog('Invalid Configuration');
            return false;
        }
    } else {
        var parser = new DOMParser();
        var doc = parser.parseFromString(docStr, "text/xml");
        if (doc.documentElement.nodeName == "parsererror") {
            CARBON.showErrorDialog('Invalid Configuration');
            return false;
        }
    }
    return true;
}

//This function is added to check whether the WSDL URL is valid and return a boolean value
//It is important to make the ajax request synchronous
function isValidWSDLURL(url) {
    var isValid = false;
    jQuery.ajax({
                    url : "ajaxprocessors/testConnection-ajaxprocessor.jsp",
                    data : {'type': 'wsdl', 'url': url},
                    success : function(data, status) {
                        if (data.replace(/^\s+|\s+$/g, '') == 'success') {
                            isValid = true;
                        }
                    },
                    async : false

                });
    return isValid;
}

function testWSDLConnection(url){
    if (url == '') {
        CARBON.showWarningDialog(jsi18n['wsdl.uri.field.cannot.be.empty']);
    } else {
       jQuery.get("ajaxprocessors/testConnection-ajaxprocessor.jsp?type=wsdl&", {'url' : url},
                   function(data, status) {
                       if (data.replace(/^\s+|\s+$/g, '') == 'success') {
                           CARBON.showInfoDialog(jsi18n['valid.address'] + " " + url);
                       } else if (data.replace(/^\s+|\s+$/g, '') == 'unknown') {
                           CARBON.showErrorDialog(jsi18n['unknown.address'] + " " + url);
                       } else if (data.replace(/^\s+|\s+$/g, '') == 'malformed') {
                           CARBON.showErrorDialog(jsi18n['malformed.address'] + " " + url);
                       } else if (data.replace(/^\s+|\s+$/g, '') == 'ssl_error') {
                           CARBON.showErrorDialog(jsi18n['ssl.error'] + " " + url);
                       } else if (data.replace(/^\s+|\s+$/g, '') == 'unknown_service') {
                           CARBON.showErrorDialog(jsi18n['unknown.service'] + " " + url);
                       } else if (data.replace(/^\s+|\s+$/g, '') == 'unsupported') {
                           CARBON.showErrorDialog(jsi18n['unsupported.protocol']);
                       } else {
                           CARBON.showErrorDialog(jsi18n['invalid.address']);
                       }
                   });
    }
}

function showHideOnSelect(selectID, element) {
    if (document.getElementById(selectID).checked == true) {
        document.getElementById(element).style.display = '';
    } else {
        document.getElementById(element).style.display = "none"
    }
}

function showErrorCodeEditor(inputID) {
    var url = 'ajaxprocessors/errorCodeEditor-ajaxprocessor.jsp?codes=' + document.getElementById(inputID).value + "&inputID=" + inputID;
    var loadingContent = "<div id='workArea' style='overflow-x:hidden;'><div id='popupContent'><div class='ajax-loading-message'> <img src='../resources/images/ajax-loader.gif' align='top'/> <span>" + jsi18n["ns.editor.waiting.text"] + "</span> </div></div></div>";
    CARBON.showPopupDialog(loadingContent, jsi18n["errorcode.editor.title"], 470, false, null, 560);

    jQuery("#popupContent").load(url, null, function (responseText, status, XMLHttpRequest) {
        if (status != "success") {
            CARBON.showErrorDialog(jsi18n["errorcode.editor.load.error"]);
        }
    });
}

function onTemplateSelectionChange() {
    var templateSelected = getSelectedValue('templateSelector');
    if (templateSelected != null && "default" != templateSelected) {
    	var targetEl = document.getElementById("target.template");
        targetEl.value = templateSelected;
        handleParamGet(templateSelected);
    }
}

function handleParamGet(templateName) {
    jQuery.ajax({
                    type: 'POST',
                    url: 'ajaxprocessors/get_template_params-ajaxprocessor.jsp',
                    data: 'templateSelect=' + templateName,
                    success: function(msg) {
                        loadProperties(msg);
                    }
                });
}

function loadProperties(data){
    jQuery("#propertytbody").html(data);
    var count = document.getElementById('propertyCount').value;
    var element = document.getElementById('propertytable');
    if (count == 0 ) {
       element.style.display = "none";
    } else {
        element.style.display = "";
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
