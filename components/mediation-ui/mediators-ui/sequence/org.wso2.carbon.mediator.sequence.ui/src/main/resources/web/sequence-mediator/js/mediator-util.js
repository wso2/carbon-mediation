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


function sequenceMediatorValidate(){
    var key;
    var keyGroup = document.getElementById("keyGroupDynamic");
    if (keyGroup != null && keyGroup.checked) {
        key = document.getElementById("mediator.sequence.key.dynamic_val");
    } else {
        key = document.getElementById("mediator.sequence.key.static_val");
    }
    if (key && key.value == "") {
        CARBON.showErrorDialog(sequencei18n["mediator.sequence.key.empty"]);
        return false;
    }

    return true;
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

