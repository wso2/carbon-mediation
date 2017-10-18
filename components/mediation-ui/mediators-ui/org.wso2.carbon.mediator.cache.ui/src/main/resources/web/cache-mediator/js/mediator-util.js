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

function specRefHandler(hitref) {

    var sepTable = document.getElementById("refSpec");
    var tbody = document.getElementById
    ('refSpec').getElementsByTagName("TBODY")[0];
    if (hitref == 'null') {
        if (tbody.innerHTML == '') {
            var row = document.createElement("TR")
            var td1 = document.createElement("TD")
            td1.className = 'leftCol-small';
            td1.appendChild(document.createTextNode("Sequence Ref"))
            var td2 = document.createElement("TD")
            // td2.appendChild (document.createTextNode("column 2"))
            td2.innerHTML = '<input type="text" value="" name="cachRef" size="40"/>';
            var td3 = document.createElement("TD")
            td3.innerHTML = '<img src="../mediator/cache/images/registry_picker_22.gif"/>';
            row.appendChild(td1);
            row.appendChild(td2);
            row.appendChild(td3);
            tbody.appendChild(row);
        }
    }
    else {
        if (tbody.innerHTML == '') {
            var row = document.createElement("TR")
            var td1 = document.createElement("TD")
            td1.width = '30%';
            td1.appendChild(document.createTextNode("Sequence Ref"))
            var td2 = document.createElement("TD")
            td2.innerHTML = '<input type="text" value="' + hitref + '" name="cachRef" size="40"/>';
            var td3 = document.createElement("TD")
            td3.innerHTML = '<img src="../mediator/cache/images/registry_picker_22.gif"/>';
            row.appendChild(td1);
            row.appendChild(td2);
            row.appendChild(td3);
            tbody.appendChild(row);
        }
    }

}

function collectorSelector(dpBox) {
    var timeoutDiv = document.getElementById("timeout");
    var msgSizeDiv = document.getElementById("msgSize");
    var cacheDiv = document.getElementById("hideCacheDetails");
    var cacheHitDiv = document.getElementById("hideCachHit");
    var protocolDiv = document.getElementById("protocolDetails");
    if (dpBox.value == "Collector") {
        timeoutDiv.parentNode.parentNode.style.display = "none";
        msgSizeDiv.parentNode.parentNode.style.display = "none";
        cacheDiv.parentNode.parentNode.style.display = "none";
        cacheHitDiv.parentNode.parentNode.style.display = "none";
        protocolDiv.parentNode.parentNode.style.display = "none";
    }
    else {
        timeoutDiv.parentNode.parentNode.style.display = "";
        msgSizeDiv.parentNode.parentNode.style.display = "";
        cacheDiv.parentNode.parentNode.style.display = "";
        cacheHitDiv.parentNode.parentNode.style.display = "";
        protocolDiv.parentNode.parentNode.style.display = "";
    }
}

function hideDivs() {
    var timeoutDiv = document.getElementById("timeout");
    var msgSizeDiv = document.getElementById("msgSize");
    var cacheDiv = document.getElementById("hideCacheDetails");
    var cacheHitDiv = document.getElementById("hideCachHit");
    var protocolDiv = document.getElementById("protocolDetails");

    timeoutDiv.parentNode.parentNode.style.display = "none";
    msgSizeDiv.parentNode.parentNode.style.display = "none";
    cacheDiv.parentNode.parentNode.style.display = "none";
    cacheHitDiv.parentNode.parentNode.style.display = "none";
    protocolDiv.parentNode.parentNode.style.display = "none";

}


function cacheMediatorValidate() {

    var cacheType = document.getElementsByName("cacheType");
    if (cacheType[0].value == "Collector") {
        return true;
    }
    var cacheTimeout = document.getElementsByName("cacheTimeout");
    var maxMsgSize = document.getElementsByName("maxMsgSize");
    var maxSize = document.getElementsByName("maxSize");


    if (cacheTimeout[0].value != null && cacheTimeout[0].value != "") {
        if (!isNumber(cacheTimeout[0].value)) {
            CARBON.showErrorDialog(cachejsi18n["mediator.cache.cachetimeout.error.only.integers"]);
            return false
        }
        var ct = parseInt(cacheTimeout[0].value);

        if (ct < 0) {
            CARBON.showErrorDialog(cachejsi18n["mediator.cache.cachetimeout.negative.error"]);
            return false;
        }
    }

    if (maxMsgSize[0].value != null && maxMsgSize[0].value != '') {
        if (!isNumber(maxMsgSize[0].value)) {
            CARBON.showErrorDialog(cachejsi18n["mediator.cache.maxmessage.error.only.integers"]);
            return false
        }
        var ct = parseInt(maxMsgSize[0].value);

        if (ct < 0) {
            CARBON.showErrorDialog(cachejsi18n["mediator.cache.maxmessage.negative.error"]);
            return false;
        }
    }

    if (maxSize[0].value != null && maxSize[0].value != '') {
        if (!isNumber(maxSize[0].value)) {
            CARBON.showErrorDialog(cachejsi18n["mediator.cache.maximumsize.error.only.integers"]);
            return false
        }
        var ct = parseInt(maxSize[0].value);

        if (ct < 0) {
            CARBON.showErrorDialog(cachejsi18n["mediator.cache.maximumsize.negative.error"]);
            return false;
        }
    }

    return true;

}

function isNumber(n) {
    return !isNaN(parseFloat(n)) && isFinite(n);
}

