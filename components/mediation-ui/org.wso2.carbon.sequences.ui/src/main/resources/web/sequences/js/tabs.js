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

function showDesign(thisVar) {

    var options = {
        beforeSubmit:  addCustomParam,  // pre-submit callback
        success:       executeShowDesign  // post-submit callback
    };
    editAreaLoader.delete_instance("mediatorSrc");
    document.getElementById("mediatorSrc").value = editAreaLoader.getValue("mediatorSrc");
    jQuery('#mediator-source-form').ajaxForm(options);
    jQuery('#mediator-source-form').submit();
}

function executeShowDesign() {
    document.getElementById("mediatorDesign").innerHTML = "";
    var url = 'mediator-edit-ajaxprocessor.jsp';
    jQuery("#mediatorDesign").load(url, null, function (responseText, status, XMLHttpRequest) {
        if (status != "success") {
            CARBON.showErrorDialog(jsi18n["mediator.design.load.error"]);
        }
    });

    hide("mediator-sourceview-header");
    showObj("mediator-designview-header");
    showObj("mediator-edit-tab");
    showObj("mediatorDesign");
    hide("mediatorSource");
}

function addCustomParam(formData, jqForm, options) {
    formData[formData.length] = {name : "followupAction", value : "source"};
}

function showSource() {

    var options = {
        beforeSubmit:  addCustomParam,  // pre-submit callback
        success:       executeShowSource  // post-submit callback 
    };

    var funcName = currentMedTLN + "MediatorValidate";
    if (eval("typeof " + funcName + " == 'function'")) {
        if (eval(funcName + "()")) {
            jQuery('#mediator-editor-form').ajaxForm(options);
        } else {
            return;
        }
    } else {
        jQuery('#mediator-editor-form').ajaxForm(options);
    }
    jQuery('#mediator-editor-form').submit();
}

function executeShowSource() {
    var url = 'mediator-source-ajaxprocessor.jsp';
    var xmlData = "";
    jQuery.ajax({
      url: url,
      async:false,
      success: function(data,status) {
        if (status != "success") {
            CARBON.showErrorDialog(jsi18n["mediator.source.load.error"]);
        } else {
            jQuery("#mediatorSource").html(data);
            hide("mediator-designview-header");
            showObj("mediator-sourceview-header");
            showObj("mediator-edit-tab");
            showObj("mediatorSource");
            hide("mediatorDesign");
            xmlData = data;
        }
      }
    });
    var ele = document.getElementById("mediatorSource");
    if (ele != null && ele != undefined) {
         jQuery(document).ready(function(){
                 editAreaLoader.init({
                        id : "mediatorSrc"
                        ,syntax: "xml"
                        ,start_highlight: true
                  });
         });
    }
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