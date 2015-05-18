/*
 * Copyright (c) 2014, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
 * 
 * WSO2 Inc. licenses this file to you under the Apache License,
 * Version 2.0 (the "License"); you may not use this file except
 * in compliance with the License.
 * You may obtain a copy of the License at
 * 
 * http://www.apache.org/licenses/LICENSE-2.0
 * 
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied. See the License for the
 * specific language governing permissions and limitations
 * under the License.
 */

function foreachMediatorValidate() {
    var expression = document.getElementById("itr_expression");
    if (expression && expression.value == "") {
        CARBON.showErrorDialog(foreachi18n["mediator.foreach.msg.specifyexpression"]);
        return false;
    }
    return true;
}

function getSelectedValue(id) {
    var variableType = document.getElementById(id);
    var variableType_indexstr = null;
    var variableType_value = null;
    if (variableType != null) {
        variableType_indexstr = variableType.selectedIndex;
        if (variableType_indexstr != null) {
            variableType_value = variableType.options[variableType_indexstr].value;
        }
    }
    return variableType_value;
}

function errorReturnCallback(){
    return false;
}

function getElementById(id) {
        return document.getElementById(id);
}


function hideSeqRegistryOption(){
    hideElem('mediator.foreach.seq.reg');
    hideElem('mediator.foreach.seq.reg.link_1');
    hideElem('mediator.foreach.seq.reg.link_2');
    setSeqValueType('anonymous');
    disableEnableEndPointOnSequencRef('');
}

function showSeqRegistryOption(){
    showElem('mediator.foreach.seq.reg');
    showElem('mediator.foreach.seq.reg.link_1');
    showElem('mediator.foreach.seq.reg.link_2');
    setSeqValueType('pickFromRegistry');
}

function hideElem(objid) {
    var theObj = document.getElementById(objid);
    theObj.style.display = "none";
}

function showElem(objid) {
    var theObj = document.getElementById(objid);
    theObj.style.display = "";
}

function seqNoneClicked(){
    setSeqValueType('none');
}

function setSeqValueType(type){
    var seqType = document.getElementById('mediator.foreach.seq.type');
    seqType.value = type;
}

function setSelected(type, option) {
    var element;
    element = document.getElementById(type + option);
    element.setAttribute('checked', 'checked');
    element.onclick();
}

function submitMediatorEditorForm(){
    var options = {
        dataType: 'text/xml',
        async: false
    };

    jQuery('#mediator-editor-form').ajaxForm(options);
    jQuery('#mediator-editor-form').submit();
}


jQuery(document).ready(function() {

    setSelected('mediator.foreach.seq.radio.', whichSeq)
    if(whichSeq == 'anon'){

    } else if(whichSeq == 'reg'){
        showSeqRegistryOption();
    }
});

