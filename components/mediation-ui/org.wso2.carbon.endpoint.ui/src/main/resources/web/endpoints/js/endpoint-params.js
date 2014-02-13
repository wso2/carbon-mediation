/*
 *  Copyright (c) 2012, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
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


function addServiceParams(tableName) {
    addServiceParamRow("", "", "",tableName);
    if (document.getElementById(tableName).style.display == "none") {
        document.getElementById(tableName).style.display = "";
    }
}

var customHeaders = Array();
var customHeadersCount = 0;

function addServiceParamRow(key, value, scope, tableName) {
    addRowForSP(key, value, scope, tableName, 'deleteEndpointParamRow');
    customHeaders[customHeadersCount] = new Array(3);
    customHeaders[customHeadersCount]['name'] = key;
    customHeaders[customHeadersCount]['value'] = value;
    customHeaders[customHeadersCount]['scope'] = scope;
    customHeadersCount++;
}

function addRowForSP(param1, param2, param3, tableName, delFunction) {
    var tableElement = document.getElementById(tableName);
    var param1Cell = document.createElement('td');
    var inputElem = document.createElement('input');
    inputElem.type = "text";
    inputElem.name = "paramName";
    inputElem.value = param1;
    param1Cell.appendChild(inputElem); //'<input type="text" name="spName" value="'+param1+' />';

    var param2Cell = document.createElement('td');
    inputElem = document.createElement('input');
    inputElem.type = "text";
    inputElem.name = "paramValue";
    inputElem.value = param2;
    param2Cell.appendChild(inputElem);

    var param3Cell = document.createElement('td');
    inputElem = document.createElement('select');
    inputElem.name = "paramScope";
    inputElem.value = param3;
    param3Cell.appendChild(inputElem);

    var option = document.createElement('option');
    option.value = 'default';
    option.appendChild(document.createTextNode('Synapse'));
    if (param3 == 'default') {
        option.selected = true;
    }
    inputElem.appendChild(option);

    option = document.createElement('option');
    option.value = 'transport';
    option.appendChild(document.createTextNode('Transport'));
    if (param3 == 'transport') {
        option.selected = true;
    }
    inputElem.appendChild(option);

    option = document.createElement('option');
    option.value = 'axis2';
    option.appendChild(document.createTextNode('Axis2'));
    if (param3 == 'axis2') {
        option.selected = true;
    }
    inputElem.appendChild(option);

    option = document.createElement('option');
    option.value = 'axis2-client';
    option.appendChild(document.createTextNode('axis2-client'));
    if (param3 == 'axis2-client') {
        option.selected = true;
    }
    inputElem.appendChild(option);

    var delCell = document.createElement('td');
    delCell.innerHTML = '<a id="deleteLink" onClick="' + delFunction + '(this.parentNode.parentNode.rowIndex,\''+tableName+'\')" alt="Delete" class="icon-link" style="background-image:url(../admin/images/delete.gif);">Delete</a>';

    var rowtoAdd = document.createElement('tr');
    rowtoAdd.appendChild(param1Cell);
    rowtoAdd.appendChild(param2Cell);
    rowtoAdd.appendChild(param3Cell);
    rowtoAdd.appendChild(delCell);

    tableElement.tBodies[0].appendChild(rowtoAdd);
    tableElement.style.display = "";

    alternateTableRows(tableElement, 'tableEvenRow', 'tableOddRow');
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

function isParamAlreadyExist(paramName) {
    var i;
    for (i = 0; i < customHeadersCount; i++) {
        if (customHeaders[i]['name'] == paramName) {
            return true;
        }
    }
    return false;
}

function deleteEndpointParamRow(index,tableName) {
    CARBON.showConfirmationDialog('This will delete the parameter. Sure to delete? Click Yes to confirm', function() {
        document.getElementById(tableName).deleteRow(index);
        if (document.getElementById(tableName).rows.length == 1) {
            document.getElementById(tableName).style.display = 'none';
        }
    });
}

function populateServiceParams(tableName) {
    var i;
    var str = '';
    var headerTable = document.getElementById(tableName);
    for (var j = 1; j < headerTable.rows.length; j++) {
        var propertyName = headerTable.rows[j].getElementsByTagName("input")[0].value;
        var propertyValue = headerTable.rows[j].getElementsByTagName("input")[1].value;
        var propertyScope = headerTable.rows[j].getElementsByTagName("select")[0].value;

        if (propertyName == "" | propertyValue == "") {
            continue;
        }

        if (str == "") {
            str += propertyName + ',' + propertyValue + ',' + propertyScope;
        } else {
            str += '::' + propertyName + ',' + propertyValue + ',' + propertyScope;
        }
    }
    return str;
}