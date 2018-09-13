
function entitlementMediatorValidate(){
    if(document.getElementById('remoteServiceUrl').value == '' && document.getElementById('remoteServiceUrlKey').value == '') {
        CARBON.showWarningDialog(enti18n["valid.remoteservice.required"]);
        return false;
    }
    if(document.getElementById('remoteServiceUserName').value == '' && document.getElementById('remoteServiceUserNameKey').value == '') {
        CARBON.showWarningDialog(enti18n["valid.remoteservice.user.required"]);
        return false;
    }
    if(document.getElementById('remoteServicePassword').value == '' && document.getElementById('remoteServicePasswordKey').value == '') {
        CARBON.showWarningDialog(enti18n["valid.remoteservice.password.required"]);
        return false;
    }
    if(document.getElementById('callbackClassOptionCustom').checked == true){
        if(document.getElementsByName('callbackClass')[0].value == ''){
            CARBON.showWarningDialog(enti18n["valid.custom.callback.required"]);
            return false;
        }
    }
    if(document.getElementById('callbackClassOptionCustom').checked == false){
        for(var i=0; i<document.getElementsByName('callbackClassOption').length; i++){
            if(document.getElementsByName('callbackClassOption')[i].checked == true){
                document.getElementsByName('callbackClass')[0].value = document.getElementsByName('callbackClassOption')[i].value;
            }
        }
    }
    if(document.getElementById('thrift').checked == true){
        if(document.getElementsByName('thriftHost')[0].value == ''){
            CARBON.showWarningDialog(enti18n["valid.thrift.host.required"]);
            return false;
        } else if (document.getElementsByName('thriftPort')[0].value == ''){
            CARBON.showWarningDialog(enti18n["valid.thrift.port.required"]);
            return false;
        }
    }
    document.getElementsByName('callbackClass')[0].disabled = false;
    document.getElementsByName('thriftHost')[0].disabled = false;
    document.getElementsByName('thriftPort')[0].disabled = false;
    return true;
}

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

function toggleCallback(input){
    if(input.value == 'org.wso2.carbon.identity.entitlement.mediator.callback.UTEntitlementCallbackHandler' ||
        input.value == 'org.wso2.carbon.identity.entitlement.mediator.callback.X509EntitlementCallbackHandler' ||
        input.value == 'org.wso2.carbon.identity.entitlement.mediator.callback.SAMLEntitlementCallbackHandler' ||
        input.value == 'org.wso2.carbon.identity.entitlement.mediator.callback.KerberosEntitlementCallbackHandler'){

        input.ownerDocument.getElementsByName('callbackClass')[0].value = '';
        input.ownerDocument.getElementsByName('callbackClass')[0].disabled = true;

    } else {
        input.ownerDocument.getElementsByName('callbackClass')[0].disabled = false;
    }
}

function toggleClient(client){
    if(client.value != 'thrift'){
        client.ownerDocument.getElementsByName('thriftHost')[0].value = '';
        client.ownerDocument.getElementsByName('thriftHost')[0].disabled = true;
        client.ownerDocument.getElementsByName('thriftPort')[0].value = '';
        client.ownerDocument.getElementsByName('thriftPort')[0].disabled = true;
    } else {
        client.ownerDocument.getElementsByName('thriftHost')[0].disabled = false;
        client.ownerDocument.getElementsByName('thriftPort')[0].disabled = false;
    }
}

function clearTextField(element){
    document.getElementById(element).value = "";
}
