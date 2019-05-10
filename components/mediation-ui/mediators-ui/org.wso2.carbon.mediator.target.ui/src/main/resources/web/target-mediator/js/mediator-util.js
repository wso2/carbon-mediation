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

function targetMediatorValidate() {
    if(document.getElementById('mediator.target.seq.radio.none').checked && document.getElementById('epOpNone').checked){
        CARBON.showWarningDialog(targeti18n["mediator.target.sequence.endpoinit.invalid"]);
        return false;
    }

    if(getElementById('mediator.target.seq.radio.reg').checked){
        var elem = getElementById('mediator.target.seq.reg');
        if(elem && elem.value == ""){
            CARBON.showWarningDialog(targeti18n["mediator.target.registry.sequence.empty"]);
            return false;
        }
    }

    if (getElementById('epOpAnon').checked) {
        if (getElementById('anonAddEdit').value == 'Add') {
            CARBON.showWarningDialog(targeti18n["mediator.target.anon.endpoint.empty"]);
            return false;
        }
    } else if (getElementById('epOpReg').checked) {
        var elem = getElementById('registryKey');
        if (elem && elem.value == "") {
            CARBON.showWarningDialog(targeti18n["mediator.target.registry.endpoint.empty"]);
            return false;
        }
    }
    return true;
}

function errorReturnCallback(){
    return false;
}

function getElementById(id) {
        return document.getElementById(id);
}


function hideSeqRegistryOption(){
    hideElem('mediator.target.seq.reg');
    hideElem('mediator.target.seq.reg.link_1');
    hideElem('mediator.target.seq.reg.link_2');
    setSeqValueType('anonymous');
    disableEnableEndPointOnSequencRef('');
}

function showSeqRegistryOption(){
    showElem('mediator.target.seq.reg');
    showElem('mediator.target.seq.reg.link_1');
    showElem('mediator.target.seq.reg.link_2');
    setSeqValueType('pickFromRegistry');
    disableEnableEndPointOnSequencRef('mediator.target.seq.radio.reg');
}

function hideEpAnnonOption(){
    hideElem('mediator.target.seq.reg');
    hideElem('mediator.target.seq.reg.link_1');
    hideElem('mediator.target.seq.reg.link_2');
  }

function hideEpRegistryOption(){
    hideElem('mediator.target.seq.reg');
    hideElem('mediator.target.seq.reg.link_1');
    hideElem('mediator.target.seq.reg.link_2');
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
    var seqType = document.getElementById('mediator.target.seq.type');
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


function anonEpEdit() {
    submitMediatorEditorForm();
    window.location.href = '../target-mediator/anon_endpoints_ajaxprocessor.jsp?anonEpAction=edit';
}

function anonEpAdd() {
    submitMediatorEditorForm();
    window.location.href = '../target-mediator/anon_endpoints_ajaxprocessor.jsp?anonEpAction=add';
}

function anonEpClear() {
    epAction = "Add";
    hideElem('epAnonClear');
    hideElem('epAnonEdit');
    showElem('epAnonAdd');
}

jQuery(document).ready(function() {
    // todo init code goes here
    setSelected('epOp', whichEP);
    if (whichEP == 'Anon') {
        showEpAddtionalOptions('epAnonAddEdit');
    } else if (whichEP == 'Reg') {
        showEpAddtionalOptions('registryEp');
    }

    setSelected('mediator.target.seq.radio.', whichSeq)
    if(whichSeq == 'anon'){

    } else if(whichSeq == 'reg'){
        showSeqRegistryOption();
    }
});


function showEpAddtionalOptions(selectedOp){
    hideEpOps();
    if(selectedOp == 'registryEp') {
        showElem('registryKey');
        showElem('regEpLink_1');
        showElem('regEpLink_2');
    } else if (selectedOp == 'epAnonAddEdit') {
        if (epAction == 'Edit') {
            showElem('epAnonEdit');
            showElem('epAnonClear');
        } else if (epAction == 'Add') {
            showElem('epAnonAdd');
        }
    }
}

function hideEpOps() {
    hideElem('epAnonAdd');
    hideElem('epAnonEdit');
    hideElem('epAnonClear');
    hideElem('registryKey');
    hideElem('regEpLink_1');
    hideElem('regEpLink_2');
}


function disableEnableEndPointOnSequencRef(selectOp){
    var theObj = document.getElementById(selectOp);
         
    if(theObj != null && theObj.checked){
      document.getElementById('epOpNone').disabled="true";
      document.getElementById('epOpAnon').disabled="true";
      document.getElementById('epOpReg').disabled="true";
    }else{
      document.getElementById('epOpNone').disabled="";
      document.getElementById('epOpAnon').disabled="";
      document.getElementById('epOpReg').disabled="";
    }
}

//function dummyFunction(){
//
//}
//
//function showReference(){
//      document.getElementById('seq_ref_row').style.display = '';
//}
