
function kerberosMediatorValidate(){
    keytabvalue = false;
    if (document.getElementById('keytabauthgroup').checked) {
        keytabvalue = true;
    }

    if(keytabvalue){
        if(document.getElementById('loginContextName').value == ''){
            CARBON.showErrorDialog(enti18n["valid.lcn.required"]);
            return false;
        }
    } else {
    	if(document.getElementById('clientPrincipal').value == ''){
            CARBON.showErrorDialog(enti18n["valid.cpn.required"]);
            return false;
        }
        if(document.getElementById('password').value == ''){
            CARBON.showErrorDialog(enti18n["valid.password.required"]);
            return false;
        }
    }

/*    if(document.getElementById('krb5Config').value == ''){
        CARBON.showErrorDialog(enti18n["valid.krb5Config.required"]);
        return false;
	}*/
	if(document.getElementById('spn').value == '' && document.getElementById('spnConfigKey').value == ''){
        CARBON.showErrorDialog(enti18n["valid.spn.required"]);
        return false;
    }

/*	if(document.getElementById('password').value == '' && document.getElementById('keytabConfig').value ==''){
        CARBON.showErrorDialog(enti18n["valid.password.required"]);
        return false;
    }*/
    return true;
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

function clearValues(elementId) {
    if (elementId == 'keytab') {
        document.getElementById('clientPrincipal').value = '';
        document.getElementById('password').value = '';
    } else {
        document.getElementById('loginContextName').value = '';
        document.getElementById('krb5Config').value = '';
        document.getElementById('loginConfig').value = '';
    }
}

function onTypeSelectionChange(typeElementName, nstbId) {
	var propertyType = getSelectedValue(typeElementName);
	if (propertyType != null) {
		setElementType(propertyType, typeElementName, nstbId);
	}
}

function setElementType(type, name, nstbId) {
	var nsEditorButtonTD = document.getElementById(nstbId);
	if (nsEditorButtonTD == null || nsEditorButtonTD == undefined) {
		return;
	}
	if ("expression" == type) {
		nsEditorButtonTD.style.display = "";
	} else {
		nsEditorButtonTD.style.display = "none";
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
