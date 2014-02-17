/*
 *  Copyright (c) 2008, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
 *
 *  Licensed under the Apache License, Version 2.0 (the "License");
 *  you may not use this file except in compliance with the License.
 *  You may obtain a copy of the License at
 *
 *        http://www.apache.org/licenses/LICENSE-2.0
 *
 *  Unless required by applicable law or agreed to in writing, software
 *  distributed under the License is distributed on an "AS IS" BASIS,
 *  WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *  See the License for the specific language governing permissions and
 *  limitations under the License.
 */
function reloadFromChanges(selectNode, counter) {
    if (selectNode.value == "value") {
        document.getElementById('newReadInfoID' + counter).innerHTML = 'Value:<input type="text" name="readInfo' + counter + '"/>';
    } else if (selectNode.value == "message") {
        document.getElementById('newReadInfoID' + counter).innerHTML = 'Expression:<input type="text" name="readInfo' + counter + '"/>';
    } else if (selectNode.value == "context") {
        document.getElementById('newReadInfoID' + counter).innerHTML = 'Context name:<input type="text" name="readInfo' + counter + '"/>';
    } else if (selectNode.value == "none") {
        deleteRowCommand(selectNode);
    } else {
        // never comes here
    }

}

function enableDisableInputs(counter) {
    var readSelection = document.getElementById('propertySelectReadType' + counter).value;
    var updateSelection = document.getElementById('propertySelectUpdateType' + counter).value;
    if (readSelection == "message" || updateSelection == "message") {
        document.getElementById('mediator.command.prop.xpath.id' + counter).removeAttribute('disabled');
        document.getElementById('mediator.command.prop.ns.link.id' + counter).style.display = '';
        document.getElementById('mediator.command.prop.ns.dummy.id' + counter).style.display = 'none';
    } else {
        document.getElementById('mediator.command.prop.xpath.id' + counter).disabled = 'true';
        document.getElementById('mediator.command.prop.ns.link.id' + counter).style.display = 'none';
        document.getElementById('mediator.command.prop.ns.dummy.id' + counter).style.display = '';
    }
    if (readSelection == "context" || updateSelection == "context") {
        document.getElementById('mediator.command.prop.context.id' + counter).removeAttribute('disabled');
    } else {
        document.getElementById('mediator.command.prop.context.id' + counter).disabled = 'true';
    }
    if (readSelection == "value") {
        document.getElementById('staticValueDiv' + counter).style.display = '';
    } else {
        document.getElementById('staticValueDiv' + counter).style.display = 'none';
    }
}

function deleteRowCommand(src) {
    var index = src.parentNode.parentNode.rowIndex;
    var attribTable = document.getElementById('commandPropTable');
    attribTable.deleteRow(index);
    attribTable.deleteRow(index);
    var noOfRows = attribTable.rows.length;
    if (noOfRows == 1) { // last row deleted, hide the tabel header
        document.getElementById('commandPropTable').style.display = 'none';
        document.getElementById('propertyLabel').style.display = 'none';
    }
}

function pojoCommandMediatorValidate() {
    var className = document.getElementById('mediatorInputId').value;
    var reason = "";
    var propCount = document.getElementById('propertyCount').value;
    var i = 0;
    for (i = 0; i < propCount; ++i) {
        var readSelection = document.getElementById('propertySelectReadType' + i).value;
        var updateSelection = document.getElementById('propertySelectUpdateType' + i).value;
        if (readSelection == "value") {
            reason += validateEmpty('mediator.command.prop.value' + i);
        }
        if (readSelection == "message" || updateSelection == "message") {
            reason += validateEmpty('mediator.command.prop.xpath' + i);
        }
        if (readSelection == "context" || updateSelection == "context") {
            reason += validateEmpty('mediator.command.prop.context' + i);
        }
    }

    if (className == null || className == "" || reason != "") {
        CARBON.showErrorDialog(commandi18n["mediator.command.validInputmsg"]);
        return false;
    }
    return true;
}