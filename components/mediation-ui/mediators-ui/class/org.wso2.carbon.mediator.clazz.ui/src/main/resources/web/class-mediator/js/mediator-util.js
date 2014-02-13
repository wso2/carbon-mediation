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

// input validator function
function classMediatorValidate() {
    var className = document.getElementById('mediatorInputId').value;
    if(className == null || className==""){
        CARBON.showErrorDialog(classi18n["mediator.clazz.validInputmsg"]);
        return false;
    }
    return true;
}

// this delete a raw from a table
function deleteRowClazz(src) {
    var index = src.parentNode.parentNode.rowIndex;
    var attribTable = document.getElementById('propertytable');
    attribTable.deleteRow(index);
    var noOfRows = attribTable.rows.length;
    if (noOfRows == 1) { // deleting the last raw, hide the tabel header
        document.getElementById('propertytable').style.display = 'none';
        document.getElementById('propertyLabel').style.display = 'none';
    }
}
