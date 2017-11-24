

function isValidXml(docStr) {
    if (window.ActiveXObject) {
        try {
            var doc = new ActiveXObject("Microsoft.XMLDOM");
            doc.async = "false";
            var hasParse = doc.loadXML(docStr);
            if (!hasParse) {
                CARBON.showErrorDialog('Invalid Configuration');
                return false;
            }
        } catch (e) {
            CARBON.showErrorDialog('Invalid Configuration');
            return false;
        }
    } else {
        var parser = new DOMParser();
        var doc = parser.parseFromString(docStr, "text/xml");
        if (doc.documentElement.nodeName == "parsererror") {
            CARBON.showErrorDialog('Invalid Configuration');
            return false;
        }
    }
    return true;
}

function trim(stringToTrim) {
    return stringToTrim.replace(/^\s+|\s+$/g, "");
}

function store_displayElement(elementId, isDisplay) {
    var toDisplayElement = document.getElementById(elementId);
    if (toDisplayElement != null) {
        if (isDisplay) {
            toDisplayElement.style.display = '';
        } else {
            toDisplayElement.style.display = 'none';
        }
    }
}

function poolOnClick() {
    store_displayElement('store.jdbc.driver_row', true);
    store_displayElement('store.jdbc.inictx_row', false);
    store_displayElement('store.jdbc.ds_row', false);
    store_displayElement('store.jdbc.url', true);
    store_displayElement('store.jdbc.user', true);
    store_displayElement('store.jdbc.passwd', true);
    store_displayElement('store.polling.count', true);
    store_displayElement('store.path', true);
    store_displayElement('sourceGroup', false);
    store_displayElement('dataSourceSelect', false);
    store_displayElement('dsProps', true);
    store_displayElement('addProp', true);
}

function sourceOnClick() {
//    var exist = document.getElementById('sourceTypeExisting');
    store_displayElement('sourceGroup', true);
    store_displayElement('dataSourceSelect', true);
//    if (exist && exist.checked) {
        existingOnClick();
//    } else {
//        inlineOnClick();
//    }
}

function inlineOnClick() {
    store_displayElement('store.jdbc.driver_row', false);
    store_displayElement('store.jdbc.inictx_row', true);
    store_displayElement('store.jdbc.ds_row', true);
    store_displayElement('store.jdbc.url', true);
    store_displayElement('store.jdbc.user', true);
    store_displayElement('store.jdbc.passwd', true);
    store_displayElement('dataSourceSelect', false);
    store_displayElement('dsProps', true);
    store_displayElement('addProp', true);
}

function existingOnClick() {
    store_displayElement('store.jdbc.driver_row', false);
    store_displayElement('store.jdbc.inictx_row', false);
    store_displayElement('store.jdbc.ds_row', false);
    store_displayElement('store.jdbc.url', false);
    store_displayElement('store.jdbc.user', false);
    store_displayElement('store.jdbc.passwd', false);
    store_displayElement('dataSourceSelect', true);
    store_displayElement('dsProps', false);
    store_displayElement('addProp', false);
}

function onEvalTypeChange() {
    var argEval = document.getElementById('resequencer.argEval').value;
    var nsEditorButtonTD = document.getElementById("nsEditorButtonTD");
    if ('xml' == argEval) {
        document.getElementById(nsEditorButtonTD).style.visibility = "visible";
    }else {
        document.getElementById(nsEditorButtonTD).style.visibility = "hidden";
    }
}