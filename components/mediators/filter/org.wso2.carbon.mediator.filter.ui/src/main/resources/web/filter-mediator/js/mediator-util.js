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

function filterMediatorValidate() {
    var type = document.getElementById("xpath");
    if (type && type.checked) {
        var xpath = document.getElementById("filter_xpath");
        if (xpath && xpath.value == "") {
            CARBON.showErrorDialog(filterMediatorJsi18n["filter.xpath.required"]);
            return false;
        }
    } else {
        var source = document.getElementById("filter_src");
        if (source && source.value == "") {
            CARBON.showErrorDialog(filterMediatorJsi18n["filter.source.required"]);
            return false;
        }
        var regEx = document.getElementById("filter_regex");
        if (regEx && regEx.value == "") {
            CARBON.showErrorDialog(filterMediatorJsi18n["filter.regx.required"]);
            return false;
        }
    }
    return true;
}


