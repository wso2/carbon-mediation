function addproperty(name, nameemptymsg, valueemptymsg) {

    if (!isValidProperties(nameemptymsg, valueemptymsg)) {
        return false;
    }
    var displayStyleOfNSEditor = document.getElementById('ns-edior-th').style.display;

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
    var nsTD = document.createElement("td");
    nsTD.setAttribute("id", "nsEditorButtonTD" + i);
    nsTD.style.display = displayStyleOfNSEditor;

    var deleteTD = document.createElement("td");
    deleteTD.innerHTML = "<a href='#' class='delete-icon-link' onclick='deleteproperty(" + i + ")' >" + fastXSLTjsi18n["mediator.fastXSLT.action.delete"] + "</a>";

    propertyRaw.appendChild(nameTD);
    propertyRaw.appendChild(typeTD);
    propertyRaw.appendChild(valueTD);
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
                if (prefix.value == "") {
                    CARBON.showWarningDialog(nameemptymsg)
                    return false;
                }
            }
            var uri = document.getElementById("propertyValue" + k);
            if (uri != null && uri != undefined) {
                if (uri.value == "") {
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
    var nsCount = document.getElementById("propertyCount");
    var i = nsCount.value;

    var currentCount = parseInt(i);

    if (currentCount >= 1) {
        for (var k = 0; k < currentCount; k++) {
            var nsEditorButtonTD = document.getElementById("nsEditorButtonTD" + k);
            if (nsEditorButtonTD != undefined && nsEditorButtonTD != null) {
                nsEditorButtonTD.style.display = displayStyle;
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
    choice.appendChild(document.createTextNode(fastXSLTjsi18n["mediator.fastXSLT.text.value"]));
    combo_box.appendChild(choice);

    choice = document.createElement('option');
    choice.value = 'expression';
    choice.appendChild(document.createTextNode(fastXSLTjsi18n["mediator.fastXSLT.text.expression"]));
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
    deleteTD.innerHTML = "<a href='#' class='delete-icon-link' onclick='deletefeature(" + i + ");return false;' >" + fastXSLTjsi18n["mediator.fastXSLT.action.delete"] + "</a>";

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
    if (nsEditorButtonTD == null || nsEditorButtonTD == undefined) {
        return;
    }
    if ("expression" == type) {
        resetDisplayStyle("");
        nsEditorButtonTD.innerHTML = "<a href='#nsEditorLink' class='nseditor-icon-link' style='padding-left:40px' onclick=\"showNameSpaceEditor('propertyValue" + i + "')\">" + name + "</a>";
    } else {
        nsEditorButtonTD.innerHTML = "";
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
    choice.appendChild(document.createTextNode(fastXSLTjsi18n["mediator.fastXSLT.text.true"]));
    combo_box.appendChild(choice);

    choice = document.createElement('option');
    choice.value = 'false';
    choice.appendChild(document.createTextNode(fastXSLTjsi18n["mediator.fastXSLT.text.false"]));
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

function fastXSLTMediatorValidate() {

    var key;
    var keyGroup = document.getElementById("keyGroupDynamic");
    if (keyGroup != null && keyGroup.checked) {
        key = document.getElementById("mediator.fastXSLT.key.dynamic_val");
    } else {
        key = document.getElementById("mediator.fastXSLT.key.static_val");
    }
    if (key && key.value == "") {
        CARBON.showErrorDialog(fastXSLTjsi18n["mediator.fastXSLT.script.key.empty"]);
        return false;
    }
    if (!isValidFeatures(fastXSLTjsi18n["mediator.fastXSLT.feature.name.empty"])) {
        return false;
    }
    return isValidProperties(fastXSLTjsi18n["mediator.fastXSLT.property.name.empty"], fastXSLTjsi18n["mediator.fastXSLT.property.value.empty"]);

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

var resources = Array();

function addResources() {
    var location = document.getElementById('locationText').value;
    var key = document.getElementById('resourceKey').value;
    // trim the input values
    location = location.replace(/^\s*/, "").replace(/\s*$/, "");
    key = key.replace(/^\s*/, "").replace(/\s*$/, "");
    if (location != '' && key != '') {
        if (isResourceAlreadyExists(location)) {
            CARBON.showWarningDialog(fastXSLTjsi18n["mediator.fastXSLT.resource.already.exists"]);
            return;
        }

        addResourceRow(location, key);
        document.getElementById('locationText').value = "";
        document.getElementById('resourceKey').value = "";
    } else {
        CARBON.showWarningDialog(fastXSLTjsi18n["mediator.fastXSLT.empty.location.or.key"]);
    }
}

function addResourceRow(location, key) {
    addRow(location, key, 'resourceTable', 'deleteResourceRow');
    var currentIndex = resources.push(new Array(2)) - 1;
    resources[currentIndex]['fastXSLT.mediator.resource.location'] = location;
    resources[currentIndex]['fastXSLT.mediator.resource.key'] = key;

    setResourceList();
}

function isResourceAlreadyExists(location) {
    var i;
    for (i = 0; i < resources.length; i++) {
        if (resources[i]['fastXSLT.mediator.resource.location'] == location) {
            return true;
        }
    }
    return false;
}

function setResourceList() {
    var i;
    var str = '';
    if (resources.length > 0) {
        str = resources[0]['fastXSLT.mediator.resource.location'] + ',' + resources[0]['fastXSLT.mediator.resource.key'];
        for (i = 1; i < resources.length; i++) {
            str += '::' + resources[i]['fastXSLT.mediator.resource.location'] + ',' + resources[i]['fastXSLT.mediator.resource.key'];
        }
    }
    document.getElementById('resourceList').value = str;
}

function deleteResourceRow(index) {
    CARBON.showConfirmationDialog(fastXSLTjsi18n["mediator.fastXSLT.confirm.resource.deletion"], function () {
        document.getElementById('resourceTable').deleteRow(index);
        resources.splice(index - 1, 1);
        if (resources.length == 0) {
            document.getElementById('resourceTable').style.display = 'none';
        }
        setResourceList();
    });
}

function addRow(param1, param2, table, delFunction) {
    var tableElement = document.getElementById(table);
    var param1Cell = document.createElement('td');
    param1Cell.appendChild(document.createTextNode(param1));

    var param2Cell = document.createElement('td');
    param2Cell.appendChild(document.createTextNode(param2));

    var delCell = document.createElement('td');
    delCell.innerHTML = '<a id="deleteLink" href="#" onClick="' + delFunction + '(this.parentNode.parentNode.rowIndex)" alt="Delete" class="icon-link" style="background-image:url(../admin/images/delete.gif);">Delete</a>';

    var rowtoAdd = document.createElement('tr');
    rowtoAdd.appendChild(param1Cell);
    rowtoAdd.appendChild(param2Cell);
    rowtoAdd.appendChild(delCell);

    tableElement.tBodies[0].appendChild(rowtoAdd);
    tableElement.style.display = "";

    alternateTableRows(tableElement, 'tableEvenRow', 'tableOddRow');
}


