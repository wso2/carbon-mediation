function addProperty(prefix) {

    if (!isValidProperties(prefix)) {
        return false;
    }
    var propertyCount = document.getElementById(prefix + "propertyCount");
    var i = propertyCount.value;

    var currentCount = parseInt(i);
    currentCount = currentCount + 1;

    propertyCount.value = currentCount;

    var propertytable = document.getElementById(prefix + "propertytable");
    propertytable.style.display = "";
    var propertytbody = document.getElementById(prefix + "propertytbody");

    var propertyRaw = document.createElement("tr");
    propertyRaw.setAttribute("id", prefix + "propertyRaw" + i);

    var nameTD = document.createElement("td");
    nameTD.innerHTML = "<input type='text' name='" + prefix + "propertyName" + i + "' id='" + prefix + "propertyName" + i + "'" + " />";

    var valueTD = document.createElement("td");
    valueTD.innerHTML = "<input type='text' name='" + prefix + "propertyValue" + i + "' id='" + prefix + "propertyValue" + i + "'" + " />";

    var deleteTD = document.createElement("td");
    deleteTD.innerHTML = "<a href='#' class='delete-icon-link' style='padding-left:40px' onclick=\"deleteProperty('" + i + "','" + prefix + "')\" >" + rulejsi18n["mediator.rule.action.delete"] + "</a>";

    propertyRaw.appendChild(nameTD);
    propertyRaw.appendChild(valueTD);
    propertyRaw.appendChild(deleteTD);

    propertytbody.appendChild(propertyRaw);
    return true;
}

function isValidProperties(prefixId) {

    var nsCount = document.getElementById(prefixId + "Count");
    if (nsCount == null) {
        return true;
    }
    var i = nsCount.value;
    var currentCount = parseInt(i);

    if (currentCount >= 1) {
        for (var k = 0; k < currentCount; k++) {
            var prefix = document.getElementById(prefixId + "propertyName" + k);
            if (prefix != null && prefix != undefined) {
                if (prefix.value == "") {
                    CARBON.showWarningDialog(rulejsi18n["mediator.rule." + prefixId + ".name.empty"]);
                    return false;
                }
            }
            var uri = document.getElementById(prefixId + "propertyValue" + k);
            if (uri != null && uri != undefined) {
                if (uri.value == "") {
                    CARBON.showWarningDialog(rulejsi18n["mediator.rule." + prefixId + ".value.empty"]);
                    return false;
                }
            }
        }
    }
    return true;
}

function resetPropertyDisplayStyle(displayStyle, prefix) {
    document.getElementById(prefix + 'ns-edior-th').style.display = displayStyle;
    var nsCount = document.getElementById(prefix + "propertyCount");
    var i = nsCount.value;

    var currentCount = parseInt(i);

    if (currentCount >= 1) {
        for (var k = 0; k < currentCount; k++) {
            var nsEditorButtonTD = document.getElementById(prefix + "nsEditorButtonTD" + k);
            if (nsEditorButtonTD != undefined && nsEditorButtonTD != null) {
                nsEditorButtonTD.style.display = displayStyle;
            }
        }
    }
}

function deleteProperty(i, prefix) {
    var propRow = document.getElementById(prefix + "propertyRaw" + i);
    if (propRow != undefined && propRow != null) {
        var parentTBody = propRow.parentNode;
        if (parentTBody != undefined && parentTBody != null) {
            parentTBody.removeChild(propRow);
            if (!isContainRaw(parentTBody)) {
                var propertyTable = document.getElementById(prefix + "propertytable");
                propertyTable.style.display = "none";
            }
        }
    }
}

function isRemainPropertyExpressions(prefix) {
    var nsCount = document.getElementById(prefix + "propertyCount");
    var i = nsCount.value;

    var currentCount = parseInt(i);

    if (currentCount >= 1) {
        for (var k = 0; k < currentCount; k++) {
            var propertyType = getSelectedValue(prefix + 'propertyTypeSelection' + k);
            if ("expression" == propertyType) {
                return true;
            }
        }
    }
    return false;
}

function onPropertyTypeSelectionChange(i, prefix) {
    var propertyType = getSelectedValue(prefix + 'propertyTypeSelection' + i);
    if (propertyType != null) {
        setTypeOfProperty(propertyType, i, prefix);
    }

}

function setTypeOfProperty(type, i, prefix) {
    var nsEditorButtonTD = document.getElementById(prefix + "nsEditorButtonTD" + i);
    if (nsEditorButtonTD == null || nsEditorButtonTD == undefined) {
        return;
    }
    if ("expression" == type) {
        resetPropertyDisplayStyle("", prefix);
        nsEditorButtonTD.innerHTML = "<a href='#nsEditorLink' class='nseditor-icon-link' style='padding-left:40px' onclick=\"showNameSpaceEditor('" + prefix + "propertyValue" + i + "')\">" + rulejsi18n["namespaces"] + "</a>";
    } else {
        nsEditorButtonTD.innerHTML = "";
        if (!isRemainPropertyExpressions(prefix)) {
            resetPropertyDisplayStyle("none", prefix);
        }
    }
}
function createBoolSelectElement(id) {
    // Create the element:
    var combo_box = document.createElement('select');

    // Set some properties:
    combo_box.name = id;
    combo_box.setAttribute("id", id);

    // Add some choices:
    var choice = document.createElement('option');
    choice.value = 'true';
    choice.appendChild(document.createTextNode(rulejsi18n["mediator.rule.text.true"]));
    combo_box.appendChild(choice);

    choice = document.createElement('option');
    choice.value = 'false';
    choice.appendChild(document.createTextNode(rulejsi18n["mediator.rule.text.false"]));
    combo_box.appendChild(choice);

    return combo_box;
}

function resetResourceDisplayStyle(displayStyle, prefix) {
    document.getElementById(prefix + 'ns-edior-th').style.display = displayStyle;
    document.getElementById(prefix + 'reg-key-th').style.display = displayStyle;
    document.getElementById(prefix + 'reg-browser-th').style.display = displayStyle;
    var nsCount = document.getElementById(prefix + "Count");
    var i = nsCount.value;

    var currentCount = parseInt(i);

    if (currentCount >= 1) {
        for (var k = 0; k < currentCount; k++) {
            var nsEditorButtonTD = document.getElementById(prefix + "nsEditorButtonTD" + k);
            if (nsEditorButtonTD != undefined && nsEditorButtonTD != null) {
                nsEditorButtonTD.style.display = displayStyle;
            }
            var registryBrowserButtonTD = document.getElementById(prefix + "registryBrowserButtonTD" + k);
            if (registryBrowserButtonTD != undefined && registryBrowserButtonTD != null) {
                registryBrowserButtonTD.style.display = displayStyle;
            }
            var registrykeyTD = document.getElementById(prefix + "registrykeyTD" + k);
            if (registrykeyTD != undefined && registrykeyTD != null) {
                registrykeyTD.style.display = displayStyle;
            }
        }
    }
}
function createPropertyTypeComboBox(id, i, prefix) {
    // Create the element:
    var combo_box = document.createElement('select');

    // Set some properties:
    combo_box.name = id;
    combo_box.setAttribute("id", id);
    combo_box.onchange = function () {
        onPropertyTypeSelectionChange(i, prefix);
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

function onScriptSourceModeSelectionChange() {
    var sourceMode = getSelectedValue("mediator.rule.inline");
    if (sourceMode != null) {
        var scriptEditorButtonTD = document.getElementById("inline_rulescript");
        var regKeyTD = document.getElementById("regkey_rulescript");
        var regBrowserTD = document.getElementById("regbrowser_rulescript");
        if (scriptEditorButtonTD == undefined || scriptEditorButtonTD == null ||
            regKeyTD == undefined || regKeyTD == null ||
            regBrowserTD == undefined || regBrowserTD == null) {
            return;
        }
        if (sourceMode == "inline") {
            regKeyTD.style.display = "none";
            regBrowserTD.style.display = "none";
            scriptEditorButtonTD.style.display = "";
        } else {
            regKeyTD.style.display = "";
            regBrowserTD.style.display = "";
            scriptEditorButtonTD.style.display = "none";
        }
    }
}
function showInLinedRuleScriptPolicyEditor(id) {

    if (id == null || id == undefined || id == "") {
        CARBON.showInfoDialog("ID cannot be null or empty");
    }
    var loadingContent = "<div id='workArea'><div id='popupContent'><div class='ajax-loading-message'> <img src='../resources/images/ajax-loader.gif' align='top'/> <span>Rule Script Policy Editor loading please wait ..</span> </div></div></div>";
    CARBON.showPopupDialog(loadingContent, rulejsi18n["rule.policy.editor"], 500, false, null, 900);

    var url = '../rule-mediator/rulesript_editor-ajaxprocessor.jsp?scriptID=' + id;
    jQuery("#popupContent").load(url, null,
        function(res, status, t) {
            if (status != "success") {
                CARBON.showWarningDialog(rulejsi18n["rule.policy.error"]);
            }
        });
    return false;
}

function saveRuleScript(id) {
    var scriptxml = document.getElementById("inlined_rule_script_source").value;
    if (scriptxml == null) {
        scriptxml = "";
    }
    var url = '../rule-mediator/rulescript_save-ajaxprocessor.jsp?scriptID=' + id;
    jQuery.post(url, ({scriptxml:scriptxml}),
        function(data, status) {
            if (status != "success") {
                CARBON.showWarningDialog(rulejsi18n["rule.policy.error"]);
            }
        });
    CARBON.closeWindow();
    return false;
}
function createInputTextBox(id, value) {

    var input = document.createElement('input');
    input.setAttribute('id', id);
    input.name = id;
    input.setAttribute('type', 'text');
    if (value != null && value != undefined) {
        input.setAttribute('value', value);
    }

    return input;
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

function ruleMediatorValidate() {
    //var sourceMode = getSelectedValue("mediator.rule.inline");  ruleScriptTypeinlined
    if (document.getElementById('ruleScriptTypekey').checked) {
        var key = document.getElementById("mediator.rule.key");
        if (key == null || key == undefined || key.value == undefined || key.value == "") {
            CARBON.showWarningDialog(rulejsi18n["mediator.rule.script.key.empty"]);
            return false;
        }
    }
    else if(document.getElementById('ruleScriptTypeurl').checked){
      var url = document.getElementById("mediator.rule.url");
        if (url == null || url == undefined || url.value == undefined || url.value == "") {
            CARBON.showWarningDialog(rulejsi18n["mediator.rule.script.url.empty"]);
            return false;
        }

    }
    return true;
}

function getSelectedValue(id) {
    var inputType = document.getElementById(id);
    var inputType_indexstr = null;
    var inputType_value = null;
    if (inputType != null) {
        inputType_indexstr = inputType.selectedIndex;
        if (inputType_indexstr != null) {
            inputType_value = inputType.options[inputType_indexstr].value;
        }
    }
    return inputType_value;
}
function showFactEditor(category, i) {

    var suffix = "index=" + i + "&category=" + category;
    var typeInput = document.getElementById(category + "Type" + i);
    var type = null;
    if (typeInput != null && typeInput != undefined) {
        type = typeInput.value;
    }
    if (type != undefined && type != null && type != "null" && type != "") {
        suffix += "&type=" + type;
    }
    var url = '../rule-mediator/fact_slector_mediator-ajaxprocessor.jsp?' + suffix;

    var loadingContent = "<div id='workArea' style='overflow-x:hidden;'><div id='popupContent'><div class='ajax-loading-message'> <img src='../resources/images/ajax-loader.gif' align='top'/> <span>" + "Wating" + "</span> </div></div></div>";
    CARBON.showPopupDialog(loadingContent, rulejsi18n["rule." + category + ".editor"], 200, false, null, 550);

    jQuery("#popupContent").load(url, null,
        function(res, status, t) {
            if (status != "success") {
                CARBON.showWarningDialog(rulejsi18n["rule.facteditor.error"]);
            }
        });
    return false;
}
function addFact(category) {

    var factCount = document.getElementById(category + "Count");
    var i = factCount.value;

    var currentCount = parseInt(i);
    currentCount = currentCount + 1;

    factCount.value = currentCount;

    var facttable = document.getElementById(category + "table");
    facttable.style.display = "";
    var facttbody = document.getElementById(category + "tbody");

    var factRaw = document.createElement("tr");
    factRaw.setAttribute("id", category + "Raw" + i);

    var factTypeTD = document.createElement("td");
    factTypeTD.innerHTML = "<input type='text' name='" + category + "Type" + i + "' id='" +
        category + "Type" + i + "'" + "  />";

//    var factSelectorTD = document.createElement("td");
//    factSelectorTD.appendChild(createFactEditorLLink(category, i));

    var elementNameTD = document.createElement("td");
    elementNameTD.innerHTML = "<input type='text' name='" + category + "ElementName" + i + "' id='" +
        category + "ElementName" + i + "'" + "  />";

    var namespaceTD = document.createElement("td");
    namespaceTD.innerHTML = "<input type='text' name='" + category + "Namespace" + i + "' id='" +
        category + "Namespace" + i + "'" + "  />";

    var xpathTD = document.createElement("td");
    xpathTD.innerHTML = "<input type='text' name='" + category + "Xpath" + i + "' id='" +
        category + "Xpath" + i + "'" + "  />";
    var nsBrowserTD = document.createElement("td");
    nsBrowserTD.setAttribute("id", category + 'NsEditorButtonTD' + i);
    nsBrowserTD.appendChild(createNSEditorLink(category, i));

    var deleteTD = document.createElement("td");
    deleteTD.appendChild(createFactDeleteLink(category, i));

    factRaw.appendChild(factTypeTD);
   // factRaw.appendChild(factSelectorTD);
    factRaw.appendChild(elementNameTD);
    factRaw.appendChild(namespaceTD);
    if(category == 'fact'){
        factRaw.appendChild(xpathTD);
        factRaw.appendChild(nsBrowserTD);
    }
    factRaw.appendChild(deleteTD);
    facttbody.appendChild(factRaw);
    return true;
}
function createFactEditorLLink(category, i) {
    // Create the element:
    var factHref = document.createElement('a');

    // Set some properties:
    factHref.setAttribute("href", "#factEditorLink");
    factHref.style.paddingLeft = '40px';
    factHref.className = "fact-selector-icon-link";
    factHref.appendChild(document.createTextNode(rulejsi18n[category + ".type"]));
    factHref.onclick = function () {
        showFactEditor(category, i)
    };
    return factHref;
}
function createFactDeleteLink(category, i) {
    // Create the element:
    var factHref = document.createElement('a');

    // Set some properties:
    factHref.setAttribute("href", "#");
    factHref.className = 'delete-icon-link';
    factHref.style.paddingLeft = '40px';
    factHref.appendChild(document.createTextNode(rulejsi18n["mediator.rule.action.delete"]));
    factHref.onclick = function () {
        deleteFact(category, i)
    };
    return factHref;
}
function createRegBrowserLink(category, i, root, type) {
    // Create the element:
    var factHref = document.createElement('a');
    // Set some properties:
    factHref.setAttribute("href", "#registryBrowserLink");
    factHref.className = 'registry-picker-icon-link';
    //    factHref.style.paddingLeft = '40px';
    factHref.appendChild(document.createTextNode(rulejsi18n["registry." + type + ".keys"]));
    factHref.onclick = function () {
        showRegistryBrowser(category + 'Value' + i, root)
    };
    return factHref;
}
function createNSEditorLink(category, i) {
    // Create the element:
    var factHref = document.createElement('a');

    // Set some properties:
    factHref.setAttribute("href", "#nsEditorLink");
    factHref.className = 'nseditor-icon-link';
    factHref.appendChild(document.createTextNode(rulejsi18n["namespaces"]));
    factHref.style.paddingLeft = '40px';
    factHref.onclick = function () {
        showNameSpaceEditor(category + 'Value' + i)
    };
    return factHref;
}

function deleteFact(category, i) {
    var propRow = document.getElementById(category + "Raw" + i);
    if (propRow != undefined && propRow != null) {
        var parentTBody = propRow.parentNode;
        if (parentTBody != undefined && parentTBody != null) {
            parentTBody.removeChild(propRow);
            if (!isContainRaw(parentTBody)) {
                var factTable = document.getElementById(category + "table");
                factTable.style.display = "none";
            }
        }
    }
}
function onFactTypeSelectionChange(category, i) {
    //TODO
}
function createFactTypeComboBox(category, i) {
    // Create the element:
    var combo_box = document.createElement('select');

    var id = category + 'TypeSelection' + i;
    // Set some properties:
    combo_box.name = id;
    combo_box.setAttribute("id", id);
    combo_box.onchange = function () {
        onFactTypeSelectionChange(category, i);
    };
    // Add some choices:
    var choice = document.createElement('option');
    choice.value = 'literal';
    choice.appendChild(document.createTextNode('Literal'));
    combo_box.appendChild(choice);

    choice = document.createElement('option');
    choice.value = 'expression';
    choice.appendChild(document.createTextNode('Expression'));
    combo_box.appendChild(choice);

    choice = document.createElement('option');
    choice.value = 'key';
    choice.appendChild(document.createTextNode('Key'));
    combo_box.appendChild(choice);

    return combo_box;
}
function setRuleScriptType(type) {


    var scriptEditorButtonTD = document.getElementById("inline_rulescript");
    var urlTD = document.getElementById("url_rulescript");
    var regKeyTD = document.getElementById("regkey_rulescript");
    var regBrowserTD = document.getElementById("regbrowser_rulescript");
    if (scriptEditorButtonTD == undefined || scriptEditorButtonTD == null ||
        regKeyTD == undefined || regKeyTD == null ||
        regBrowserTD == undefined || regBrowserTD == null) {
        return;
    }
    if ('inlined' == type) {
        regKeyTD.style.display = "none";
        regBrowserTD.style.display = "none";
        urlTD.style.display = "none";
        scriptEditorButtonTD.style.display = "";
    }
    else if ('url' == type) {
        regKeyTD.style.display = "none";
        regBrowserTD.style.display = "none";
        urlTD.style.display = "";
        scriptEditorButtonTD.style.display = "none";

    }
    else {
        regKeyTD.style.display = "";
        regBrowserTD.style.display = "";
        urlTD.style.display = "none";
        scriptEditorButtonTD.style.display = "none";
    }

    /*
     var ruleScriptKeyTR = document.getElementById("ruleScriptKeyTR");
     var ruleScriptSourceTR = document.getElementById("ruleScriptSourceTR");
     var rulesetCreationTR = document.getElementById("rulesetCreationTR");
     var rulesetCreationUploadTR = document.getElementById("rulesetCreationUploadTR");
     var ruleScriptUploadTR = document.getElementById("ruleScriptUploadTR");
     var ruleScriptURLTR = document.getElementById("ruleScriptURLTR");
     if ('key' == type) {
     ruleScriptKeyTR.style.display = "";
     rulesetCreationTR.style.display = "";
     ruleScriptSourceTR.style.display = "none";
     ruleScriptUploadTR.style.display = "none";
     rulesetCreationUploadTR.style.display = "none";
     ruleScriptURLTR.style.display = "none";
     } else if ('upload' == type) {
     ruleScriptSourceTR.style.display = "none";
     ruleScriptKeyTR.style.display = "none";
     rulesetCreationTR.style.display = "none";
     ruleScriptUploadTR.style.display = "";
     rulesetCreationUploadTR.style.display = "";
     ruleScriptURLTR.style.display = "none";
     }
     else if ('url' == type) {
     ruleScriptSourceTR.style.display = "none";
     ruleScriptKeyTR.style.display = "none";
     rulesetCreationTR.style.display = "";
     ruleScriptURLTR.style.display = "";
     rulesetCreationUploadTR.style.display = "none";
     ruleScriptUploadTR.style.display = "none";
     }else {
     ruleScriptSourceTR.style.display = "";
     rulesetCreationTR.style.display = "";
     ruleScriptKeyTR.style.display = "none";
     ruleScriptUploadTR.style.display = "none";
     rulesetCreationUploadTR.style.display = "none";
     ruleScriptURLTR.style.display = "none";
     }*/

    return true;
}


