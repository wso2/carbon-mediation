var DEFAULT_DIV_ID = 'nsEditor';

//private method
function show_ns_editor(dID, id, mode, linkID) {

    var sufix = "";
    if (linkID != undefined && linkID != null && linkID != "null" && linkID != "") {
        document.getElementById(linkID).style.display = "none";
        sufix = "&linkID=" + linkID;
    }

    if (mode != undefined && mode != null) {
        sufix = "&editorMode=" + mode;
    }

    var url = 'ns_editor-ajaxprocessor.jsp?currentID=' + id + '&divID=' + dID + sufix;

    var loadingContent = "<div id='workArea' style='overflow-x:hidden;'><div id='popupContent'><div class='ajax-loading-message'> <img src='../resources/images/ajax-loader.gif' align='top'/> <span>" + jsi18n["ns.editor.waiting.text"] + "</span> </div></div></div>";
    CARBON.showPopupDialog(loadingContent, jsi18n["ns.editor.title"], 300, false, null, 550);
        var stringData = "null";
        jQuery("#popupContent").load(url, stringData,
                function(res, status, t) {
                    if (status != "success") {
                        CARBON.showWarningDialog(jsi18n["ns.editor.load.error"]);
                    }
                });


    return false;
}

function showNameSpaceEditorOnDiv(dID, id) {
    return show_ns_editor(dID, id);
}
function showNameSpaceEditor(id) {
    return show_ns_editor('nsEditor', id);
}
function showNameSpaceEditorHideLink(id, linkID) {
    return show_ns_editor('nsEditor', id, null, linkID);
}
function showSingleNameSpaceEditorOnDiv(dID, id) {
    return show_ns_editor(dID, id, 'single');
}
function showSingleNameSpaceEditor(id) {
    return show_ns_editor('nsEditor', id, 'single');
}

function showMultiNameSpaceEditorOnDiv(dID, id) {
    return show_ns_editor(dID, id);
}
function showMultiNameSpaceEditor(id) {
    return show_ns_editor('nsEditor', id);
}

function addNameSpace(namedelete,prefixemptymsg,uriemptymsg) {
    if (!isValidNameSpaces(prefixemptymsg, uriemptymsg)) {
        return false;
    }

    var nsCount = document.getElementById("nsCount");
    var i = nsCount.value;

    var currentCount = parseInt(i);

    currentCount = currentCount + 1;

    nsCount.value = currentCount;

    var nstable = document.getElementById("nsTable");
    nstable.style.display = "";
    var nstbody = document.getElementById("nsTBody");

    var nsRaw = document.createElement("tr");
    nsRaw.setAttribute("id", "nsTR" + i);

    var prefixTD = document.createElement("td");
    prefixTD.innerHTML = "<input type='text' style='width:100px' name='prefix" + i + "' id='prefix" + i + "'" +
                         " />";

    var uriTD = document.createElement("td");
    uriTD.innerHTML = "<input type='text' name='uri" + i + "' class='longInput' id='uri" + i + "'" +
                      " />";

    var actionTD = document.createElement("td");
    actionTD.innerHTML = "<a href='#' class='delete-icon-link' onclick='deletensraw(" + i + ")' >" + namedelete + "</a>";

    nsRaw.appendChild(prefixTD);
    nsRaw.appendChild(uriTD);
    nsRaw.appendChild(actionTD);

    nstbody.appendChild(nsRaw);
    return true;
}

function deletensraw(i) {
    CARBON.showConfirmationDialog(jsi18n["ns.editor.delete.confirmation"], function() {
        var propRow = document.getElementById("nsTR" + i);
        if (propRow != undefined && propRow != null) {
            var parentTBody = propRow.parentNode;
            if (parentTBody != undefined && parentTBody != null) {
                parentTBody.removeChild(propRow);
            }
        }
    });
}

function clearSingleNameSpace() {
    var uri = document.getElementById("uri0");
    if (uri) {
        uri.value = "";
    }

    var uri = document.getElementById("prefix0");
    if (uri) {
        uri.value = "";
    }
}

function isValidNameSpaces(prefixemptymsg, uriemptymsg, isSinle) {

    var nsCount = document.getElementById("nsCount");
    var i = nsCount.value;

    var currentCount = parseInt(i);

    var uri ;
    var prefix;
    var k;
    if (!isSinle) {
        if (currentCount >= 1) {
            for (k = 0; k < currentCount; k++) {
                prefix = document.getElementById("prefix" + k);
                if (prefix != null && prefix != undefined) {
                    if (prefix.value == "") {
                        CARBON.showWarningDialog(prefixemptymsg)
                        return false;
                    }
                }
                uri = document.getElementById("uri" + k);
                if (uri != null && uri != undefined) {
                    if (uri.value == "") {
                        CARBON.showWarningDialog(uriemptymsg)
                        return false;
                    }

                    if(!isValidURI(uri.value)) {
                        CARBON.showWarningDialog(jsi18n["ns.editor.invalid.uri"] + uri.value);
                        return false;
                    }
                }
            }
        }
    } else {
        for (k = 0; k < currentCount; k++) {
            prefix = document.getElementById("prefix" + k);
            if (prefix != null && prefix != undefined) {
                if (prefix.value != "") {
                    uri = document.getElementById("uri" + k);
                    if (uri.value == "") {
                        CARBON.showWarningDialog(uriemptymsg)
                        return false;
                    }
                }
            }
            uri = document.getElementById("uri" + k);
            if (uri != null && uri != undefined) {
                if (uri.value != "") {
                    if (prefix.value == "") {
                        uri = document.getElementById("uri" + k);
                        CARBON.showWarningDialog(prefixemptymsg)
                        return false;

                    }
                    if(!isValidURI(uri.value)) {
                        uri.value = "";
                        CARBON.showWarningDialog(jsi18n["ns.editor.invalid.uri"] + uri.value);
                        return false;

                    }
                }
            }
        }
    }
    return true;
}

function isValidURI(uri) {
	var regexp = /((ftp|http|https):\/\/|(urn):)(\w+:{0,1}\w*@)?(\S+)(:[0-9]+)?(\/|\/([\w#!:.?+=&%@!\-\/]))?/;
	return regexp.test(uri);
}

function saveNameSpace(divID, id, linkID,prefixemptymsg,uriemptymsg, isSingle) {
    if (!isValidNameSpaces(prefixemptymsg, uriemptymsg, isSingle)) {
        return false;
    }

    if (linkID != undefined && linkID != null && linkID!="null" && linkID!="") {
        document.getElementById(linkID).style.display = "";
    }

    var nsCount = document.getElementById("nsCount");
    var count = parseInt(nsCount.value);
    var referenceString = "";
    for (var i = 0; i < count; i++) {
        var prefixID = "prefix" + i;
        var prefix = document.getElementById(prefixID);
        var uriID = "uri" + i;
        var uri = document.getElementById(uriID);
        if (prefix != null && prefix != undefined && uri != null && uri != undefined) {
            var prefixValue = prefix.value;
            var uriValue = uri.value;
            if (prefixValue != undefined && uriValue != undefined && uriValue != "") {
                referenceString += "&" + prefixID + "=" + prefixValue + "&" + uriID + "=" + uriValue;
            }
        }
    }
    var url = 'ns_save-ajaxprocessor.jsp?currentID=' + id + "&nsCount=" + count + referenceString;
    var stringData = "null";
    jQuery.post(url, stringData,
            function(data, status) {
                if (status != "success") {
                    CARBON.showWarningDialog(jsi18n["ns.editor.load.error"]);
                }
            });
    hideNameSpaceEditor(divID);
    CARBON.closeWindow();
    return false;
}

function hideNameSpaceEditor(divID, linkID) {
    CARBON.closeWindow();
    return false;
    /*
    if (linkID != undefined && linkID != null && linkID!="null" && linkID!="") {
        var linkObj = document.getElementById(linkID);
        if (linkObj) {
            linkObj.style.display = "";
        }
    }

    var nsDiv = document.getElementById(divID);
    if (nsDiv != null && nsDiv != undefined) {
        nsDiv.style.display = "none";
        nsDiv.innerHTML = "";
    }
    return false;
    */
}
