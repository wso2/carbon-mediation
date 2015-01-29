/*
 * Copyright (c) 2014, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
 * 
 * WSO2 Inc. licenses this file to you under the Apache License,
 * Version 2.0 (the "License"); you may not use this file except
 * in compliance with the License.
 * You may obtain a copy of the License at
 * 
 * http://www.apache.org/licenses/LICENSE-2.0
 * 
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied. See the License for the
 * specific language governing permissions and limitations
 * under the License.
 */

function showTargetInfo(){
    document.getElementById('taget_info_button').style.display = 'none';
    document.getElementById('target_info_div').style.display = '';
}

function foreachMediatorValidate() {
    var expression = document.getElementById("itr_expression");
    if (expression && expression.value == "") {
        CARBON.showErrorDialog(foreachi18n["mediator.foreach.msg.specifyexpression"]);
        return false;
    }

   
    return true;
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