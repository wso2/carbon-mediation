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

    function filterList(query) {
        jqNew(".storeRow").hide();

        jqNew(".storeRow").filter(function() {
            return jqNew(this).text().toLowerCase().indexOf(query.toLowerCase()) !== -1;
        }).show();
    }

    function getStoreData(storeName, pageNumber) {
        $("#txtFilter").val("");
        var queryParam = {"store": storeName, "page": pageNumber};
        jqNew.ajax({
            type: "GET",
            url: "getMessages-ajaxprocessor.jsp",
            data: queryParam,
            success: function(data) {
                var json = jqNew.parseJSON(data);
                if(json.success === true) {
                    populateTable(json);
                    jqNew("#currentPage").text(pageNumber);
                } else {
                    if(json.resultsSize === 0) {
                        CARBON.showInfoDialog(jsi18n["store.empty"]);
                    } else {
                        CARBON.showErrorDialog(jsi18n["could.not.retrieve.messages"]);
                    }
                }
            },
            error: function() {
                CARBON.showErrorDialog(jsi18n["could.not.retrieve.messages"]);
            }
        });
    }

    function searchStore(storeName, query) {
        $("#txtFilter").val("");
        var queryParam = {"store": storeName, "query": query};
        jqNew.ajax({
            type: "POST",
            url: "search-ajaxprocessor.jsp",
            data: queryParam,
            success: function(data) {
                var json = jqNew.parseJSON(data);
                if(json.success  === true) {
                    populateTable(json);
                } else {
                    CARBON.showErrorDialog(jsi18n["could.not.find.any.matching.messages"]);
                }
            },
            error: function() {
                CARBON.showErrorDialog(jsi18n["could.not.retrieve.messages"]);
            }
        });
    }

    function purgeStore(storeName) {
        var queryParam = {"store": storeName};
        jqNew.ajax({
            type: "POST",
            url: "purge-ajaxprocessor.jsp",
            data: queryParam,
            success: function(data) {
                var json = jqNew.parseJSON(data);
                if(json.success  === true) {
                    CARBON.showInfoDialog(jsi18n["all.messages.purged"]);
                } else {
                    CARBON.showErrorDialog(jsi18n["could.not.purge.messages"]);
                }
            },
            error: function() {
                CARBON.showErrorDialog(jsi18n["could.not.purge.messages"]);
            }
        });
    }

    function populateTable(json) {
        var htmlStr = "";
        jqNew.each(json.resultsArray, function() {
            htmlStr = htmlStr + "<tr class=\"storeRow\"><td class=\"filterDate\">" + this.date + "</td><td class=\"filterMessageId\">" + this.messageId + "</td>" +
                "<td class=\"filterControlId\">" + this.controlId + "</td><td class=\"filterRawMessage\" style=\"width: inherit;height: 55px;display: block;border-bottom:0px;overflow: auto;\">" + this.rawMessage + "</td><td>" + this.actions + "</td></tr>";
        });

        jqNew("#storeTableBody").html(htmlStr);
        jqNew("#totalPages").text(json.totalPages);
    }

    function nextPage(storeName) {
        var next = parseInt(jqNew("#currentPage").text()) + 1;
        if(next >= parseInt(jqNew("#totalPages").text())) {
            next = parseInt(jqNew("#totalPages").text());
        }
        getStoreData(storeName, next);
    }

    function previousPage(storeName) {
        var prev = parseInt(jqNew("#currentPage").text()) - 1;
        if(prev <= 0) {
            prev = 1;
        }
        getStoreData(storeName, prev);
    }

    function init() {
        // UI event handlers
        jqNew("#btnStore").click(function() {
            getStoreData(jqNew("#store").val(), 1);
        });

        jqNew("#btnFilter").click(function() {
            filterList(jqNew("#txtFilter").val());
        });

        jqNew("#txtFilter").keyup(function(event) {
            if(event.keyCode === 13) {
                filterList(jqNew("#txtFilter").val());
            } else {
                if(jqNew(this).val() === '') {
                    jqNew(".storeRow").show();
                    jqNew(".colour").removeClass("colour");
                }
            }
        });

        jqNew("#txtSearch").keyup(function(event) {
           if(event.keyCode === 13) {
               searchStore(jqNew("#store").val(), jqNew(this).val());
           } else {
               if(jqNew(this).val() === '') {
                   getStoreData(jqNew("#store").val(), 1);
               }
           }
        });

        jqNew("#btnSearch").click(function() {
            searchStore(jqNew("#store").val(), jqNew("#txtSearch").val());
        });

        jqNew("#nextPage").click(function() {
            nextPage(jqNew("#store").val());
        });

        jqNew("#prevPage").click(function() {
            previousPage(jqNew("#store").val());
        });

        jqNew("#purgeMessages").click(function() {
           CARBON.showConfirmationDialog(jsi18n["confirm.purge"],
               function() {
                    purgeStore(jqNew("#store").val());
                },
                function() {});
        });

        if (typeof g_store !== 'undefined') {
            getStoreData(g_store, 1);

            $("#store option").each(function(){
                if($(this).val() === g_store){
                    $(this).attr("selected", "selected");
                }
            });
        } else {
            getStoreData(jqNew("#store").val(), 1);
        }
    }

    if(g_avail) {
        jqNew(document).ajaxStart( function() {
            jqNew("#messageTable").hide();
            jqNew("#loadingDiv").show();
        } ).ajaxStop ( function(){
            jqNew("#messageTable").show();
            jqNew("#loadingDiv").hide();
        });

        init();
    }

});