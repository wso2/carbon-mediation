function addvariable(name, name2, nameemptymsg, valueemptymsg, typeempty ,leaveAsIs) {

    if (!isValidVaribles(nameemptymsg, valueemptymsg, typeempty)) {
        return false;
    }

    var displayStyleOfNSEditor = document.getElementById('ns-edior-th').style.display;
    var variableCount = document.getElementById("variableCount");
    var i = variableCount.value;

    var currentCount = parseInt(i);
    currentCount = currentCount + 1;

    variableCount.value = currentCount;

    var variabletable = document.getElementById("variabletable");
    variabletable.style.display = "";
    var variabletbody = document.getElementById("variabletbody");

    var variableRaw = document.createElement("tr");
    variableRaw.setAttribute("id", "variableRaw" + i);

    var variableType = document.createElement("td");
    variableType.innerHTML = "<select id='variableType" + i + "' name='variableType" + i + "' >" +
                             "<option value='Select-A-Value'>"+leaveAsIs+"</option><option value='INT'>" + xqueryjsi18n["mediator.xquery.type.INT"] + "</option><option value='INTEGER'>" + xqueryjsi18n["mediator.xquery.type.INTEGER"] + "</option><option value='BOOLEAN'>"
            + xqueryjsi18n["mediator.xquery.type.BOOLEAN"] + "</option><option value='BYTE'>" + xqueryjsi18n["mediator.xquery.type.BYTE"] + "</option><option value='DOUBLE'>" + xqueryjsi18n["mediator.xquery.type.DOUBLE"] + "</option><option value='SHORT'>" + xqueryjsi18n["mediator.xquery.type.SHORT"] + "</option><option value='LONG'>" + xqueryjsi18n["mediator.xquery.type.LONG"] +
                             "</option><option value='FLOAT'>" + xqueryjsi18n["mediator.xquery.type.FLOAT"] + "</option><option value='STRING'>" + xqueryjsi18n["mediator.xquery.type.STRING"] + "</option><option value='DOCUMENT'>" + xqueryjsi18n["mediator.xquery.type.DOCUMENT"] + "</option><option value='DOCUMENT_ELEMENT'>" +
                             xqueryjsi18n["mediator.xquery.type.DOCUMENT_ELEMENT"] + "</option><option value='ELEMENT'>" + xqueryjsi18n["mediator.xquery.type.ELEMENT"] + "</option></select>";
    var nameTD = document.createElement("td");
    nameTD.innerHTML = "<input type='text' name='variableName" + i + "' id='variableName" + i + "'" +
                       "  />";

    var typeTD = document.createElement("td");
    typeTD.appendChild(createproperttypecombobox('variableTypeSelection' + i, i, name, name2))

    var valueTD = document.createElement("td");
    valueTD.innerHTML = "<input type='text' name='variableValue" + i + "' id='variableValue" + i + "'" +
                        "  />";
    var regKeyTD = document.createElement("td");
    regKeyTD.style.display = displayStyleOfNSEditor;
    regKeyTD.setAttribute("id", "registrykeyTD" + i);

//    regKeyTD.innerHTML = "<input id='registryKey" + i + "' name='registryKey" + i + "' type='text' disabled='true' />" +
//                         "<input id='registryKey" + i + "_hidden' name='registryKey" + i + "_hidden' type='hidden'  />";

    var regBrowserTD = document.createElement("td");
    regBrowserTD.style.display = displayStyleOfNSEditor;
    regBrowserTD.setAttribute("id", "registryBrowserButtonTD" + i);
//    regBrowserTD.innerHTML = "<a href='#registryBrowserLink' class='registry-picker-icon-link' onclick=\"showInLinedRegistryBrowser('registryKey" + i + "')\">" + name2 + "</a>";

    var nsTD = document.createElement("td");
    nsTD.style.display = displayStyleOfNSEditor;

    nsTD.setAttribute("id", "nsEditorButtonTD" + i);
    var deleteTD = document.createElement("td");
    deleteTD.innerHTML = "<a href='#' class='delete-icon-link' onclick='deletevariable(" + i + ")' >" + xqueryjsi18n["mediator.xquery.action.delete"] + "</a>";

    variableRaw.appendChild(variableType)
    variableRaw.appendChild(nameTD);
    variableRaw.appendChild(typeTD);
    variableRaw.appendChild(valueTD);
    variableRaw.appendChild(regKeyTD);
    variableRaw.appendChild(regBrowserTD);
    variableRaw.appendChild(nsTD);
    variableRaw.appendChild(deleteTD);

    variabletbody.appendChild(variableRaw);
    return true;
}

function isValidVaribles(nameemptymsg, valueemptymsg, typeempty) {

    var nsCount = document.getElementById("variableCount");
    var i = nsCount.value;

    var currentCount = parseInt(i);

    if (currentCount >= 1) {
        for (var k = 0; k < currentCount; k++) {
            var propRow = document.getElementById("variableRaw" + k);
            if (propRow != null && propRow != undefined) {
                var prefix = document.getElementById("variableName" + k);
                if (prefix != null && prefix != undefined) {
                    if (prefix.value == "") {
                        CARBON.showWarningDialog(nameemptymsg)
                        return false;
                    }
                }
                var type = document.getElementById("variableTypeSelection" + k);
                if (type.value == "literal") {
                    var uri = document.getElementById("variableValue" + k);
                    if (uri != null && uri != undefined) {
                        if (uri.value == "") {
                            CARBON.showWarningDialog(valueemptymsg)
                            return false;
                        }
                    }
                }

                var type = getSelectedValue("variableType" + k);
                if (type == 'Select-A-Value') {
                    CARBON.showWarningDialog(typeempty)
                    return false;
                }
            }
        }
    }
    return true;
}

function resetDisplayStyle(displayStyle) {
    document.getElementById('ns-edior-th').style.display = displayStyle;
    document.getElementById('reg-key-th').style.display = displayStyle;
    document.getElementById('reg-browser-th').style.display = displayStyle;
    var nsCount = document.getElementById("variableCount");
    var i = nsCount.value;

    var currentCount = parseInt(i);

    if (currentCount >= 1) {
        for (var k = 0; k < currentCount; k++) {
            var nsEditorButtonTD = document.getElementById("nsEditorButtonTD" + k);
            if (nsEditorButtonTD != undefined && nsEditorButtonTD != null) {
                nsEditorButtonTD.style.display = displayStyle;
            }
            var registryBrowserButtonTD = document.getElementById("registryBrowserButtonTD" + k);
            if (registryBrowserButtonTD != undefined && registryBrowserButtonTD != null) {
                registryBrowserButtonTD.style.display = displayStyle;
            }
            var registrykeyTD = document.getElementById("registrykeyTD" + k);
            if (registrykeyTD != undefined && registrykeyTD != null) {
                registrykeyTD.style.display = displayStyle;
            }
        }
    }
}
function createproperttypecombobox(id, i, name, name2) {
    // Create the element:
    var combo_box = document.createElement('select');

    // Set some properties:
    combo_box.name = id;
    combo_box.setAttribute("id", id);
    combo_box.onchange = function () {
        onvariableTypeSelectionChange(i, name, name2)
    };
    // Add some choices:
    var choice = document.createElement('option');
    choice.value = 'literal';
    choice.appendChild(document.createTextNode('Value'));
    combo_box.appendChild(choice);

    choice = document.createElement('option');
    choice.value = 'expression';
    choice.appendChild(document.createTextNode('Expression'));
    combo_box.appendChild(choice);

    return combo_box;
}

function deletevariable(i) {
    var propRow = document.getElementById("variableRaw" + i);
    if (propRow != undefined && propRow != null) {
        var parentTBody = propRow.parentNode;
        if (parentTBody != undefined && parentTBody != null) {
            parentTBody.removeChild(propRow);
            if (!isContainRaw(parentTBody)) {
                var variableTable = document.getElementById("variabletable");
                variableTable.style.display = "none";
            }
            if (!isRemainVariableExpressions()) {
                resetDisplayStyle("none");
            }
        }
    }
}

function isRemainVariableExpressions() {
    var nsCount = document.getElementById("variableCount");
    var i = nsCount.value;

    var currentCount = parseInt(i);

    if (currentCount >= 1) {
        for (var k = 0; k < currentCount; k++) {
            var variableType = getSelectedValue('variableTypeSelection' + k);
            if ("expression" == variableType) {
                return true;
            }
        }
    }
    return false;
}

function onvariableTypeSelectionChange(i, name, name2) {
    var variableType = getSelectedValue('variableTypeSelection' + i);
    if (variableType != null) {
        settype(variableType, i, name, name2);
    }
}

function createinputtextbox(id, value) {

    var input = document.createElement('input');
    input.setAttribute('id', id);
    input.name = id;
    input.setAttribute('type', 'text');
    if (value != null && value != undefined) {
        input.setAttribute('value', value);
    }

    return input;
}

function settype(type, i, name, name2) {
    var nsEditorButtonTD = document.getElementById("nsEditorButtonTD" + i);
    var regKeyTD = document.getElementById("registrykeyTD" + i);
    var regBrowserTD = document.getElementById("registryBrowserButtonTD" + i);
    if (nsEditorButtonTD == undefined || nsEditorButtonTD == null || regKeyTD == undefined
            || regKeyTD == null || regBrowserTD == undefined || regBrowserTD == null) {
        return;
    }
    if ("expression" == type) {
        resetDisplayStyle("");
        regBrowserTD.innerHTML = "<a href='#registryBrowserLink' class='registry-picker-icon-link' "+
                                 "onclick=\"showRegistryBrowser('registryKey" + i + "','/_system/config'"+")\">" + " Configuration Registry" + "</a>"+
                                 "<a href='#registryBrowserLink' class='registry-picker-icon-link' "+
                                 "onclick=\"showRegistryBrowser('registryKey" + i + "','/_system/governance'"+")\">" + "Governance Registry" + "</a>";
        regKeyTD.innerHTML = "<input id='registryKey" + i + "' name='registryKey" + i + "' type='text' readonly='true' />";
        nsEditorButtonTD.innerHTML = "<a href='#nsEditorLink' class='nseditor-icon-link' style='padding-left:40px' onclick=\"showNameSpaceEditor('variableValue" + i + "')\">" + name + "</a>";
    } else {
        nsEditorButtonTD.innerHTML = "";
        regBrowserTD.innerHTML = "";
        regKeyTD.innerHTML = "";
        if (!isRemainVariableExpressions()) {
            resetDisplayStyle("none");
        }
    }
}
function createboolselectelement(id) {
    // Create the element:
    var combo_box = document.createElement('select');     

    // Set some properties:
    combo_box.name = id;
    combo_box.setAttribute("id", id);

    // Add some choices:
    var choice = document.createElement('option');
    choice.value = 'true';
    choice.appendChild(document.createTextNode('True'));
    combo_box.appendChild(choice);

    choice = document.createElement('option');
    choice.value = 'false';
    choice.appendChild(document.createTextNode('False'));
    combo_box.appendChild(choice);

    return combo_box;
}


function isContainRaw(tbody) {
    if (tbody.childNodes == null || tbody.childNodes.length == 0) {
        return false;
    } else {
        for (var i = 0; i < tbody.childNodes.length; i++) {
            var child = tbody.childNodes[i];
            if (child != undefined && child != null) {
                if (child.nodeName == "tr" || child.nodeName == "TR") {
                    return true;
                }
            }
        }
    }
    return false;
}

function xqueryMediatorValidate() {
    var key;
    var keyGroup = document.getElementById("keyGroupDynamic");
    if (keyGroup!=null && keyGroup.checked) {
        key = document.getElementById("mediator.xquery.key.dynamic_val");
    } else {
        key = document.getElementById("mediator.xquery.key.static_val");
    }
    if (key && key.value == "") {
        CARBON.showWarningDialog(xqueryjsi18n["mediator.xquery.script.key.empty"]);
        return false;
    }
    return isValidVaribles(xqueryjsi18n["mediator.xquery.variable.name.empty"], xqueryjsi18n["mediator.xquery.variable.value.empty"], xqueryjsi18n["mediator.xquery.variable.type.empty"]);
}

function getSelectedValue(id) {
    var variableType = document.getElementById(id);
    var variableType_indexstr = null;
    var variableType_value = null;
    if (variableType != null) {
        variableType_indexstr = variableType.selectedIndex;
        if (variableType_indexstr != null) {
            variableType_value = variableType.options[variableType_indexstr].value;
        }
    }
    return variableType_value;
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