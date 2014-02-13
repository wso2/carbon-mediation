function hideElem(objid) {
    var theObj = document.getElementById(objid);
    if (theObj) {
        theObj.style.display = "none";
    }
}
function showElem(objid) {
    var theObj = document.getElementById(objid);
    if (theObj) {
        theObj.style.display = "";
    }
}

var customHeaders = Array();
var customHeadersCount = 0;

function addServiceParamRow(key, value) {
    addRowForSP(key, value, 'headerTable', 'deleteServiceParamRow');
    customHeaders[customHeadersCount] = new Array(2);
    customHeaders[customHeadersCount]['name'] = key;
    customHeaders[customHeadersCount]['value'] = value;
    customHeadersCount++;
}

function addRowForSP(param1, param2, table, delFunction) {
    var tableElement = document.getElementById(table);
    var param1Cell = document.createElement('td');
    var inputElem = document.createElement('input');
    inputElem.type = "text";
    inputElem.name = "spName";
    inputElem.value = param1;
    param1Cell.appendChild(inputElem); //'<input type="text" name="spName" value="'+param1+' />';


    var param2Cell = document.createElement('td');
    inputElem = document.createElement('input');
    inputElem.type = "text";
    inputElem.name = "spValue";
    inputElem.value = param2;
    param2Cell.appendChild(inputElem);

    var delCell = document.createElement('td');
    delCell.innerHTML='<a id="deleteLink" href="#" onClick="' + delFunction + '(this.parentNode.parentNode.rowIndex)" alt="Delete" class="icon-link" style="background-image:url(../admin/images/delete.gif);">Delete</a>';

    var rowtoAdd = document.createElement('tr');
    rowtoAdd.appendChild(param1Cell);
    rowtoAdd.appendChild(param2Cell);
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
    delCell.innerHTML='<a id="deleteLink" href="#" onClick="' + delFunction + '(this.parentNode.parentNode.rowIndex)" alt="Delete" class="icon-link" style="background-image:url(../admin/images/delete.gif);">Delete</a>';

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
