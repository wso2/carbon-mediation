/*
 * Copyright (c) 2015, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
 *
 *   WSO2 Inc. licenses this file to you under the Apache License,
 *   Version 2.0 (the "License"); you may not use this file except
 *   in compliance with the License.
 *   You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 *  Unless required by applicable law or agreed to in writing,
 *  software distributed under the License is distributed on an
 *  "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 *  KIND, either express or implied.  See the License for the
 *  specific language governing permissions and limitations
 *  under the License.
 */

// input validator function
function mlMediatorValidate() {
    var modelName = document.getElementById('mediatorInputId').value;
    if(modelName == null || modelName==""){
        CARBON.showErrorDialog(classi18n["mediator.predict.validInputmsg"]);
        return false;
    }
    return true;
}

// delete a raw from a table
function deleteRow(src) {
    var index = src.parentNode.parentNode.rowIndex;
    var inputVariablesTable = document.getElementById('inputVariablesTable');
    inputVariablesTable.deleteRow(index);
    var noOfRows = inputVariablesTable.rows.length;
    if (noOfRows == 1) { // deleting the last raw, hide the table header
        document.getElementById('inputVariablesTable').style.display = 'none';
        document.getElementById('titleLabel').style.display = 'none';
    }
}
