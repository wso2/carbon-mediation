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

function hideCorrelationPanel() {
    document.getElementById('correlation_row').style.display = 'none';
}

function showCorrelationPanel() {
    if (document.getElementById('correlationRadio').checked) {
        document.getElementById('correlation_row').style.display = '';
    }
}

function rmsequenceMediatorValidate() {
    if (document.getElementById('correlationRadio').checked) {
        if (document.getElementById('correlation').value == '') {
            CARBON.showWarningDialog(jsi18n["mediator.rmsequence.xpath.value.empty"]);
            return false;
        }
    }
    return true;
}

