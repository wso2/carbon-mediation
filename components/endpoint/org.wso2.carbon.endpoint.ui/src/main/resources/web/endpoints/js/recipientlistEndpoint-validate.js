function validateRecipientlistEndpoint(isAnonymous, validateChildEpsAvailability) {
    if (!isAnonymous && isEmptyField('listEndpointName')) {
        CARBON.showWarningDialog(jsi18n['name.field.cannot.be.empty']);
        return false;
    }

    var elementId = document.getElementById('childEndpoint-0');
    if (validateChildEpsAvailability && (elementId == null || elementId == undefined)) {
        CARBON.showWarningDialog(jsi18n['no.child.endpoints.added']);
        return false;
    }
    return true;
}