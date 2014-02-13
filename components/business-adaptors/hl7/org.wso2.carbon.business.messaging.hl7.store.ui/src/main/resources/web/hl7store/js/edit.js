/*
 ~ Copyright (c) 2014, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
 ~
 ~ Licensed under the Apache License, Version 2.0 (the "License");
 ~ you may not use this file except in compliance with the License.
 ~ You may obtain a copy of the License at
 ~
 ~      http://www.apache.org/licenses/LICENSE-2.0
 ~
 ~ Unless required by applicable law or agreed to in writing, software
 ~ distributed under the License is distributed on an "AS IS" BASIS,
 ~ WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 ~ See the License for the specific language governing permissions and
 ~ limitations under the License.
 */

jqNew(function() {

    // UI event handlers
    jqNew("#btnRawMessage").click(function() {
       updateXmlMessage();
    });

    jqNew("#btnXmlMessage").click(function() {
       updateRawMessage();
    });

    jqNew("#btnSend").click(function() {
        sendXmlMessage();
    });

    function sendXmlMessage() {
        var er7Message = jqNew("#rawMessage").val();
        var queryParam = {"store": g_store, "proxy": jqNew("#proxyList").val(), "er7": er7Message};

        jqNew.ajax({
            type: "POST",
            url: "send-ajaxprocessor.jsp",
            data: queryParam,
            success: function(data){
                var json = jqNew.parseJSON(data);
                if(json.success === true) {
                    CARBON.showInfoDialog(jsi18n["message.sent.successfully"]);
                } else {
                    CARBON.showErrorDialog(jsi18n["message.sent.failed"]);
                }
            }
        });
    }

    function updateRawMessage() {
        var xmlMessage = jqNew("#xmlMessage").val();
        var queryParam = {"store": g_store, "xmlMessage": xmlMessage};
        jqNew.ajax({
            type: "POST",
            url: "getRawMessage-ajaxprocessor.jsp",
            data: queryParam,
            success: function(data){
                var json = jqNew.parseJSON(data);
                populateRawMessage(json);
            }
        });
    }

    function populateRawMessage(json) {
        console.log(json);
        if(json.success === true) {
            jqNew("#rawMessage").val(json.rawMessage);
        } else {
            CARBON.showErrorDialog(json.reason);
        }
    }

    function updateXmlMessage() {
        var er7Message = jqNew("#rawMessage").val();
        var queryParam = {"store": g_store, "rawMessage": er7Message};
        jqNew.ajax({
            type: "POST",
            url: "getXmlMessage-ajaxprocessor.jsp",
            data: queryParam,
            success: function(data){
                var json = jqNew.parseJSON(data);
                populateXmlMessage(json);
            }
        });
    }

    function populateXmlMessage(json) {
        if(json.success === true) {
            jqNew("#xmlMessage").val(json.xmlMessage);
        } else {
            CARBON.showErrorDialog(json.reason);
        }
    }

    function getProxyData(storeName) {
        var queryParam = {"store": storeName};
        jqNew.ajax({
            type: "GET",
            url: "getProxyServices-ajaxprocessor.jsp",
            data: queryParam,
            success: function(data){
                var json = jqNew.parseJSON(data);
                populateProxyList(json);
            }
        });
    }

    function populateProxyList(json) {
        var htmlStr = "";
        jqNew.each(json.services, function() {
            htmlStr = htmlStr + "<option>" + this.name + "</option>";
        });

        jqNew("#proxyList").html(htmlStr);
    }

    function init() {
        if (typeof g_store !== 'undefined') {
            getProxyData(g_store);
        }
    }

    init();

});