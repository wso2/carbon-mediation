
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