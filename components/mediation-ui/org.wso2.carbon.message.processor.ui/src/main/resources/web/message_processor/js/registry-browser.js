var elementId;
var rootPath;
function showRegistryBrowser(id, path) {
    elementId = id;
    rootPath = path;
    showResourceTree(id, setValue , path);
    showMediationResourceTree(id, path);
}

function showRegistryBrowserWithoutLocalEntries(id, path) {
    elementId = id;
    rootPath = path;
    showResourceTree(id, setValue , path);
}

function showMediationResourceTree(id, path) {
    if ($('local-registry-placeholder')) {
        $('local-registry-placeholder').innerHTML = '<table class="styledLeft"><tbody><tr>' +
                                                    '<td class="leftCol-small" style="border-right:none"><br/>Local Registry</td>' +
                                                    '<td style="border-left:none">' +
                                                    '<div id="local-registry-workArea" name="local-registry-workArea" style="margin-top:5px;margin-bottom:5px;"></div>' +
                                                    '</td></tr></tbody></table>';
        showLocalRegBrowser(id);
        $('local-registry-placeholder').style.display = "";
    } else {
        setTimeout("showMediationResourceTree('" + id + "','" + path + "')", 100);
    }
}

function setValue() {
    if (rootPath == "/_system/config") {
        $(elementId).value = $(elementId).value.replace(rootPath, "conf:");
    } else if (rootPath == "/_system/governance") {
        $(elementId).value = $(elementId).value.replace(rootPath, "gov:");
    }
}