
function entitlementMediatorValidate(){
    if(document.getElementById('remoteServiceUrl').value == ''){
        CARBON.showWarningDialog(enti18n["valid.remoteservice.required"]);
        return false;
    }
    return true;
}