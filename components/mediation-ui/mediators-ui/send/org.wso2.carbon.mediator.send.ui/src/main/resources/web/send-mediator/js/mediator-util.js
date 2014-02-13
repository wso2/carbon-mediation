jQuery(document).ready(function() {
    // todo init code goes here
    if (whichEP) {
        setSelected('epOp', whichEP);
        if (whichEP == 'Anon') {
            showEpAddtionalOptions('epAnonAddEdit');
        } else if (whichEP == 'Reg') {
            showEpAddtionalOptions('registryEp');
        } else if (whichEP == 'XPath') {
            showEpAddtionalOptions('xpath');
        }
    }
});


function showEpAddtionalOptions(selectedOp){
    hideEpOps();
    if(selectedOp == 'registryEp') {
        showElem('registryKey');
        showElem('confRegEpLink');
        showElem('govRegEpLink');
    } else if (selectedOp == 'epAnonAddEdit') {
        if (epAction == 'Edit') {
            showElem('epAnonEdit');
            showElem('epAnonClear');
        } else if (epAction == 'Add') {
            showElem('epAnonAdd');
        }
    } else if ('xpath' == selectedOp) {
        showElem('send_xpath');
        showElem('mediator.send.xpath_nmsp');
    }
}

function hideEpOps() {
    hideElem('epAnonAdd');
    hideElem('epAnonEdit');
    hideElem('epAnonClear');
    hideElem('registryKey');
    hideElem('confRegEpLink');
    hideElem('govRegEpLink');
    hideElem('send_xpath');
    hideElem('mediator.send.xpath_nmsp');
}

function hideElem(objid) {
    var theObj = document.getElementById(objid);
    if (theObj) {
        theObj.style.display = "none";
    }
}

function showElem(objid) {
    var theObj = document.getElementById(objid);
    if (theObj) {
        theObj.style.display = "";
    }
}

function setSelected(type, option) {
    var element;
    element = document.getElementById(type + option);
    if (element) {
        element.setAttribute('checked', 'checked');
        element.onclick();
    }
}


function anonEpEdit() {
    window.location.href = '../send-mediator/anon_endpoints_ajaxprocessor.jsp?anonEpAction=edit';
}

function anonEpAdd() {
    window.location.href = '../send-mediator/anon_endpoints_ajaxprocessor.jsp?anonEpAction=add';
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
