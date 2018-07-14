
function kerberosMediatorValidate(){
    if(document.getElementById('krb5Config').value == '' && document.getElementById('krb5ConfigKey').value == ''){
        CARBON.showErrorDialog(enti18n["valid.krb5Config.required"]);
        return false;
	}
	if(document.getElementById('spn').value == ''){
        CARBON.showErrorDialog(enti18n["valid.spn.required"]);
        return false;
    }
	if(document.getElementById('clientPrincipal').value == ''){
        CARBON.showErrorDialog(enti18n["valid.cpn.required"]);
        return false;
    }
	if(document.getElementById('password').value == '' && document.getElementById('keytabPath').value =='' &&
	    document.getElementById('keyTabKey').value ==''){
        CARBON.showErrorDialog(enti18n["valid.password.required"]);
        return false;
    }
    return true;
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
