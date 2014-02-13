function validateWSDLOptions() {
    var optionsTable = document.getElementById('wsdlOptionsTable');
    if (!optionsTable) {
        return true;
    }

    var selectedIndex = document.getElementById('publishWsdlCombo').selectedIndex;
    if (selectedIndex == 1) {
        var inlineTxt = document.getElementById('wsdlInlineText').value;
        if (inlineTxt == null || inlineTxt == '') {
            CARBON.showErrorDialog(proxyi18n['no.inline.wsdl']);
            return false;
        }
    } else if (selectedIndex == 2) {
        var wsdlUri = document.getElementById('wsdlUriText').value;
        if (wsdlUri == null || wsdlUri == '') {
            CARBON.showErrorDialog(proxyi18n['no.wsdl.url']);
            return false;
        }

    } else if (selectedIndex == 3) {
        var wsdlKey = document.getElementById('wsdlRegText').value;
        if (wsdlKey == null || wsdlKey == '') {
            CARBON.showErrorDialog(proxyi18n['no.wsdl.key']);
            return false;
        }
    }

    return true;
}

function validateTransports() {
    var trpTable = document.getElementById('availableTransports');
    if (!trpTable) {
        return true;
    }

    var availTrpList = document.getElementById('availableTransportsList').value;
    var trpNames = new Array();
    trpNames = availTrpList.split(',');

    var foundTrp = false;
    var i;
    for (i = 0; i < trpNames.length; i++) {
        if (document.getElementById('trp__' + trpNames[i]).checked) {
            foundTrp = true;
            break;
        }
    }

    if (!foundTrp) {
        //CARBON.showErrorDialog(proxyi18n['no.transports']);
        return false;
    }
    return true;
}

function validateTargetEndpoint() {       //TODO This method has no use now remove it if safe
    var targetEndpointMode = document.getElementById('targetEndpointMode');
    if (!targetEndpointMode) {
        return true;
    }

    var i = targetEndpointMode.selectedIndex;
    if (i == 0) {
        var targetURL = document.getElementById('targetURLTxt').value;
        if (targetURL == null || targetURL == '') {
            CARBON.showErrorDialog(proxyi18n['no.target.url']);
            return false;
        }
    } else if (i == 1) {
        var predefEndpoint = document.getElementById('predefEndpointsCombo').selectedIndex;
        if (predefEndpoint < 0) {
            CARBON.showErrorDialog(proxyi18n['invalid.endpoint.select']);
            return false;
        }
    } else if (i == 2) {
        var regKey = document.getElementById('endpointRegText').value;
        if (regKey == null || regKey == '') {
            CARBON.showErrorDialog(proxyi18n['an.endpoint.is.not.selected.from.the.registry']);
            return false;
        }
    } else {
        CARBON.showErrorDialog(proxyi18n['invalid.endpoint']);
        return false;
    }

    return true;
}

function templatesHome() {
    window.location.href = './templates.jsp';
}

function validateProxyName() {
    var name = document.getElementById('proxy_name').value;
    if (name == null || name == '') {
        CARBON.showErrorDialog(proxyi18n['invalid.proxy.name']);
        return false;
    }
    return true;
}

function proxyCreated(name) {
    document.location.href = '../service-mgt/index.jsp';    
}
