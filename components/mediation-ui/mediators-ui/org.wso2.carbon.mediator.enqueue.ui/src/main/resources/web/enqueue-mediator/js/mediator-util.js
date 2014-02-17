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


function enqueueMediatorValidate() {

    var val = document.getElementById('executor');
    if (val && val.value == "") {
        CARBON.showErrorDialog(enqueueMediatorJsi18n["specify.executor"]);
        return false;
    }

    val = document.getElementById('priority');
    if (val && val.value == "") {
        CARBON.showErrorDialog(enqueueMediatorJsi18n["specify.priority"]);
        return false;
    }

    val = document.getElementById('enqueue.mediator.sequence');
    if (val && val.value == "") {
        CARBON.showErrorDialog(enqueueMediatorJsi18n["specify.sequence"]);
        return false;
    }
    
    return true;
}


