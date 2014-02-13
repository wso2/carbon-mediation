function addproperty(name, nameemptymsg, valueemptymsg) {

    if (!isValidProperties(nameemptymsg, valueemptymsg)) {
        return false;
    }
    var displayStyleOfNSEditor = document.getElementById('ns-edior-th').style.display;
    var displayStyleOfDynXpath = document.getElementById('dynamic-xpath-th').style.display;

    var propertyCount = document.getElementById("propertyCount");
    var i = propertyCount.value;

    var currentCount = parseInt(i);
    currentCount = currentCount + 1;

    propertyCount.value = currentCount;

    var propertytable = document.getElementById("propertytable");
    propertytable.style.display = "";
    var propertytbody = document.getElementById("propertytbody");

    var propertyRaw = document.createElement("tr");
    propertyRaw.setAttribute("id", "propertyRaw" + i);

    var nameTD = document.createElement("td");
    nameTD.innerHTML = "<input type='text' name='propertyName" + i + "' id='propertyName" + i + "'" +
                       " />";

    var typeTD = document.createElement("td");
    typeTD.appendChild(createproperttypecombobox('propertyTypeSelection' + i, i, name))

    var valueTD = document.createElement("td");
    valueTD.innerHTML = "<input type='text' name='propertyValue" + i + "' id='propertyValue" + i + "'" +
                        " />";
    var dynXPTD = document.createElement("td");
    dynXPTD.setAttribute("id", "dynamicXpathCol" + i);
    dynXPTD.style.display = displayStyleOfDynXpath;

    var nsTD = document.createElement("td");
    nsTD.setAttribute("id", "nsEditorButtonTD" + i);
    nsTD.style.display = displayStyleOfNSEditor;

    var deleteTD = document.createElement("td");
    deleteTD.innerHTML = "<a href='#' class='delete-icon-link' onclick='deleteproperty(" + i + ")' >" + xsltjsi18n["mediator.call.action.delete"] + "</a>";

    propertyRaw.appendChild(nameTD);
    propertyRaw.appendChild(typeTD);
    propertyRaw.appendChild(valueTD);
    propertyRaw.appendChild(dynXPTD);
    propertyRaw.appendChild(nsTD);
    propertyRaw.appendChild(deleteTD);

    propertytbody.appendChild(propertyRaw);
    return true;
}

function isValidProperties(nameemptymsg, valueemptymsg) {

    var nsCount = document.getElementById("propertyCount");
    var i = nsCount.value;

    var currentCount = parseInt(i);

    if (currentCount >= 1) {
        for (var k = 0; k < currentCount; k++) {
            var prefix = document.getElementById("propertyName" + k);
            if (prefix != null && prefix != undefined) {
                if (prefix.value == "" && nameemptymsg != "") {
                    CARBON.showWarningDialog(nameemptymsg)
                    return false;
                }
            }
            var uri = document.getElementById("propertyValue" + k);
            if (uri != null && uri != undefined) {
                if (uri.value == "" && valueemptymsg !="") {
                    CARBON.showWarningDialog(valueemptymsg)
                    return false;
                }
            }
        }
    }
    return true;
}

function isValidFeatures(nameemptymsg) {

    var nsCount = document.getElementById("featureCount");
    var i = nsCount.value;

    var currentCount = parseInt(i);

    if (currentCount >= 1) {
        for (var k = 0; k < currentCount; k++) {
            var prefix = document.getElementById("featureName" + k);
            if (prefix != null && prefix != undefined) {
                if (prefix.value == "") {
                    CARBON.showWarningDialog(nameemptymsg)
                    return false;
                }
            }
        }
    }
    return true;
}

function resetDisplayStyle(displayStyle) {
    document.getElementById('ns-edior-th').style.display = displayStyle;
    document.getElementById('dynamic-xpath-th').style.display = displayStyle;
    var nsCount = document.getElementById("propertyCount");
    var i = nsCount.value;

    var currentCount = parseInt(i);

    if (currentCount >= 1) {
        for (var k = 0; k < currentCount; k++) {
            var nsEditorButtonTD = document.getElementById("nsEditorButtonTD" + k);
            var nsDynXpathTD = document.getElementById("dynamicXpathCol" + k);
            if (nsEditorButtonTD != undefined && nsEditorButtonTD != null) {
                nsEditorButtonTD.style.display = displayStyle;
                nsDynXpathTD.style.display = displayStyle;
            }
        }
    }
}

function createproperttypecombobox(id, i, name) {
    // Create the element:
    var combo_box = document.createElement('select');

    // Set some properties:
    combo_box.name = id;
    combo_box.setAttribute("id", id);
    combo_box.onchange = function () {
        onPropertyTypeSelectionChange(i, name)
    };
    // Add some choices:
    var choice = document.createElement('option');
    choice.value = 'literal';
    choice.appendChild(document.createTextNode(xsltjsi18n["mediator.call.text.value"]));
    combo_box.appendChild(choice);

    choice = document.createElement('option');
    choice.value = 'expression';
    choice.appendChild(document.createTextNode(xsltjsi18n["mediator.call.text.expression"]));
    combo_box.appendChild(choice);

    return combo_box;
}

function deleteproperty(i) {
    var propRow = document.getElementById("propertyRaw" + i);
    if (propRow != undefined && propRow != null) {
        var parentTBody = propRow.parentNode;
        if (parentTBody != undefined && parentTBody != null) {
            parentTBody.removeChild(propRow);
            if (!isContainRaw(parentTBody)) {
                var propertyTable = document.getElementById("propertytable");
                propertyTable.style.display = "none";
            }
            if (!isRemainPropertyExpressions()) {
                resetDisplayStyle("none");
            }
        }
    }
}

function isRemainPropertyExpressions() {
    var nsCount = document.getElementById("propertyCount");
    var i = nsCount.value;

    var currentCount = parseInt(i);

    if (currentCount >= 1) {
        for (var k = 0; k < currentCount; k++) {
            var propertyType = getSelectedValue('propertyTypeSelection' + k);
            if ("expression" == propertyType) {
                return true;
            }
        }
    }
    return false;
}

function addfeature(nameempty) {
    if (!isValidFeatures(nameempty)) {
        return false;
    }
    var propertyCount = document.getElementById("featureCount");
    var i = propertyCount.value;
    var currentCount = parseInt(i);
    currentCount = currentCount + 1;

    propertyCount.value = currentCount;

    var propertytable = document.getElementById("featuretable");
    propertytable.style.display = "";
    var propertytbody = document.getElementById("featuretbody");

    var propertyRaw = document.createElement("tr");
    propertyRaw.setAttribute("id", "featureRaw" + i);

    var nameTD = document.createElement("td");
    nameTD.appendChild(createinputtextbox("featureName" + i, ""));

    var typeTD = document.createElement("td");
    typeTD.appendChild(createboolselectelement('featureValue' + i));
    var deleteTD = document.createElement("td");
    deleteTD.innerHTML = "<a href='#' class='delete-icon-link' onclick='deletefeature(" + i + ");return false;' >" + xsltjsi18n["mediator.call.action.delete"] + "</a>";

    propertyRaw.appendChild(nameTD);
    propertyRaw.appendChild(typeTD);
    propertyRaw.appendChild(deleteTD);

    propertytbody.appendChild(propertyRaw);
    return true;
}

function deletefeature(i) {
    var featureRow = document.getElementById("featureRaw" + i);
    if (featureRow != undefined && featureRow != null) {
        var parentTBody = featureRow.parentNode;
        if (parentTBody != undefined && parentTBody != null) {
            parentTBody.removeChild(featureRow);
            if (!isContainRaw(parentTBody)) {
                var featureTable = document.getElementById("featuretable");
                featureTable.style.display = "none";
            }
        }
    }
}
function onPropertyTypeSelectionChange(i, name) {
    var propertyType = getSelectedValue('propertyTypeSelection' + i);
    if (propertyType != null) {
        settype(propertyType, i, name);
    }

}

function onTemplateSelectionChange() {
    var templateSelected = getSelectedValue('templateSelector');
    if (templateSelected != null && "default" != templateSelected) {
    	var targetEl = document.getElementById("mediator.call.target");
    	var targetElVisible = document.getElementById("mediator.call.target.visible");
        targetEl.value = templateSelected;
     	targetElVisible.value = templateSelected;
        handleParamGet(templateSelected);
//        settype(templateSelected, i, name);
    }

}

function handleParamGet(templateName) {
    jQuery.ajax({
                    type: 'POST',
                    url: '../calltemplate-mediator/get_template_params-ajaxprocessor.jsp',
                    data: 'templateSelect=' + templateName,
                    success: function(msg) {
                        handleSuccess(msg);
                    },
                    error: function(msg) {
//                        CARBON.showErrorDialog('<fmt:message key="template.trace.enable.link"/>' +
//                                               ' ' + templateName);
                    }
                });
}

function handleSuccess(data){
    addproperty("NameSpaces","","");
    jQuery("#propertytbody").html(data);
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


function settype(type, i, name) {
    var nsEditorButtonTD = document.getElementById("nsEditorButtonTD" + i);
    var nsDynXpathTD = document.getElementById("dynamicXpathCol" + i);
    if (nsEditorButtonTD == null || nsEditorButtonTD == undefined) {
        return;
    }
    if ("expression" == type) {
        resetDisplayStyle("");
        nsEditorButtonTD.innerHTML = "<a href='#nsEditorLink' class='nseditor-icon-link' style='padding-left:40px' onclick=\"showNameSpaceEditor('propertyValue" + i + "')\">" + name + "</a>";
        nsDynXpathTD.innerHTML = "<input id='dynamicCheckbox" + i + "' name='" + "dynamicCheckbox" + i + "' type='checkbox'" +
                                       "value='true' />"  ;


    } else {
        nsEditorButtonTD.innerHTML = "";
        nsDynXpathTD.innerHTML = "";
        if (!isRemainPropertyExpressions()) {
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
    choice.appendChild(document.createTextNode(xsltjsi18n["mediator.call.text.true"]));
    combo_box.appendChild(choice);

    choice = document.createElement('option');
    choice.value = 'false';
    choice.appendChild(document.createTextNode(xsltjsi18n["mediator.call.text.false"]));
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

function calltemplateMediatorValidate() {
    var target = document.getElementById("mediator.call.target").value;
    if(target == null || target.trim()==""){
//        alert('validate');
        CARBON.showErrorDialog(xsltjsi18n["call.mediator.target.validInputmsg"]);
        return false;
    }
//    alert('true : target = ' + target);
    return isValidProperties(xsltjsi18n["nameemptyerror"],
                             xsltjsi18n["valueemptyerror"]);

}

function getSelectedValue(id) {
    var propertyType = document.getElementById(id);
    var propertyType_indexstr = null;
    var propertyType_value = null;
    if (propertyType != null) {
        propertyType_indexstr = propertyType.selectedIndex;
        if (propertyType_indexstr != null) {
            propertyType_value = propertyType.options[propertyType_indexstr].value;
        }
    }
    return propertyType_value;
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