/*
 ~  Copyright (c) 2012, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
 ~
 ~  Licensed under the Apache License, Version 2.0 (the "License");
 ~  you may not use this file except in compliance with the License.
 ~  You may obtain a copy of the License at
 ~
 ~        http://www.apache.org/licenses/LICENSE-2.0
 ~
 ~  Unless required by applicable law or agreed to in writing, software
 ~  distributed under the License is distributed on an "AS IS" BASIS,
 ~  WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 ~  See the License for the specific language governing permissions and
 ~  limitations under the License.
 */

function validateLoadBalanceEndpoint(isAnonymous, validateChildEpsAvailability) {
    if (!isAnonymous && isEmptyField('listEndpointName')) {
        CARBON.showWarningDialog(jsi18n['name.field.cannot.be.empty']);
        return false;
    }

    var elementId = document.getElementById('childEndpoint-0');
    if (validateChildEpsAvailability && (elementId == null || elementId == undefined)) {
        CARBON.showWarningDialog(jsi18n['no.child.endpoints.added']);
        return false;
    }
    return true;
}