/*
 *  Copyright (c) 2012, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
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

//This is the method called when the menu items are selected
function childMenuItemClicked(e, e2, objx) {

    //Extracting the obj elements
    var typetoAdd = objx[0];
    var addingOn = objx[1];
    var todo = objx[2];

    var url = "listEndpointDesigner/childEndpoint-ajaxprocessor.jsp?position=" + addingOn + "&childEndpointName="
                      + typetoAdd + "&type=" + todo + "&childEndpointAction=add";

    jQuery("#treePane").load(url, null, function (responseText, status, XMLHttpRequest) {
        if (status != "success") {
            CARBON.showErrorDialog(jsi18n["endpoint.design.load.error"]);
        } else {
            initEndpoints();
            var ele = document.getElementById("addChildEndpointPosition");
            if (ele != null && ele != undefined) {
                var pos = ele.value;
                if (pos != null && pos != "") {
                    showChildEndpointConfig(pos);
                }
            }
        }
    });
}

//This method reads the dom tree of the html tree and register events
// and callback functions for them
function initEndpoints() {

    var allDivs = document.getElementById("treePane").getElementsByTagName("*");
    var addChildNodes = new Array();
    var deleteNodes = new Array();
    var endpointNodes = new Array();
    var toolbarNodes = new Array();
    var rootNode;

    for (var i = 0; i < allDivs.length; i++) {
        if (YAHOO.util.Dom.hasClass(allDivs[i], "addChildStyle")) {
            addChildNodes.push(allDivs[i]);
        }
        if (YAHOO.util.Dom.hasClass(allDivs[i], "deleteStyle")) {
            deleteNodes.push(allDivs[i]);
        }
        if (YAHOO.util.Dom.hasClass(allDivs[i], "endpointLink")) {
            endpointNodes.push(allDivs[i]);
        }
        if (YAHOO.util.Dom.hasClass(allDivs[i], "endpointToolbar")) {
            toolbarNodes.push(allDivs[i]);
        }
        if (YAHOO.util.Dom.hasClass(allDivs[i], "root-endpoint")) {
            rootNode = allDivs[i];
        }
    }
    for (i = 0; i < addChildNodes.length; i++) {
        //Add event listeners for link for add Child Nodes
        YAHOO.util.Event.addListener(addChildNodes[i], "click", addNodesCallback,
                                     [addChildNodes[i],addChildNodes[i].parentNode.parentNode.parentNode.id,"child"]);
    }
    for (i = 0; i < deleteNodes.length; i++) {
        //Add event listeners for link for delete
        YAHOO.util.Event.addListener(deleteNodes[i], "click", deleteCallback,
                                     [deleteNodes[i],deleteNodes[i].parentNode.parentNode.parentNode.id,"sibling"]);
    }

    for (i = 0; i < endpointNodes.length; i++) {
        //Add event listeners for link for add Child Nodes
        YAHOO.util.Event.addListener(endpointNodes[i], "click", endpointCallback,
                                     [endpointNodes[i],endpointNodes[i].id,endpointNodes,toolbarNodes]);
    }
    YAHOO.util.Event.addListener(rootNode, "click", rootCallback,
                                 [rootNode,toolbarNodes,endpointNodes]);
}

function focusRootEndpoint() {
    var allDivs = document.getElementById("treePane").getElementsByTagName("*");
    var endpointNodes = new Array();
    var toolbarNodes = new Array();
    var rootNode;

    for (var i = 0; i < allDivs.length; i++) {
        if (YAHOO.util.Dom.hasClass(allDivs[i], "endpointLink")) {
            endpointNodes.push(allDivs[i]);
        }
        if (YAHOO.util.Dom.hasClass(allDivs[i], "endpointToolbar")) {
            toolbarNodes.push(allDivs[i]);
        }
        if (YAHOO.util.Dom.hasClass(allDivs[i], "root-endpoint")) {
            rootNode = allDivs[i];
        }
    }

    rootCallback(null, [rootNode,toolbarNodes,endpointNodes]);
}

function rootCallback(e, obj) {

    var endpointNodes = obj[2];

    for (i = 0; i < endpointNodes.length; i++) {
        //Add event listners for link for add Child Nodes
        if (YAHOO.util.Dom.hasClass(endpointNodes[i].parentNode, "selected-node")) {
            YAHOO.util.Dom.removeClass(endpointNodes[i].parentNode, "selected-node");
        }
    }

    //Do the toolbar stuff
    var toolbarNodes = obj[1];
    for (i = 0; i < toolbarNodes.length; i++) {
        toolbarNodes[i].style.display = "none";
    }

    //show the wanted toolbar
    var endpointNode = obj[0].parentNode;
    var endpointNodeChildren = endpointNode.childNodes;
    for (i = 0; i < endpointNodeChildren.length; i++) {
        if (endpointNodeChildren[i].nodeName == "DIV") {
            if (YAHOO.util.Dom.hasClass(endpointNodeChildren[i], "endpointToolbar")) {
                endpointNodeChildren[i].style.display = "";
            }
        }
    }
    hideChildEndpointTab();
}

function hideChildEndpointTab() {
    // get the endpoint edit pane and set it to empty
    var element = document.getElementById('childEndpoint-form-header');
    if (element != null && element != undefined) {
        element.style.display = "none";
    }
    element = document.getElementById('childEndpoint-form-tab');
    if (element != null && element != undefined) {
        element.style.display = "none";
    }
    element = document.getElementById('childEndpointDesign');
    if (element != null && element != undefined) {
        element.innerHTML = "";
    }
}

function deleteCallback(e, obj) {
    var clickedEndpointNodeId = obj[1];

    CARBON.showConfirmationDialog(jsi18n["listendpointdesigner.child.endpoint.delete"], function() {
        var url = "listEndpointDesigner/childEndpoint-ajaxprocessor.jsp?childEndpointID="
                          + clickedEndpointNodeId + "&childEndpointAction=delete";
        jQuery("#treePane").load(url, null, function (responseText, status, XMLHttpRequest) {
            if (status != "success") {
                CARBON.showErrorDialog(jsi18n["endpoint.design.load.error"]);
            } else {
                initEndpoints();
                hideChildEndpointTab();
                focusRootEndpoint();
            }
        });
    });
}

function endpointCallback(e, obj) {
    showObj("childEndpoint-form-header");
    showObj("childEndpoint-form-tab");
    showObj("childEndpointDesign");

    var url = 'listEndpointDesigner/childEndpoint-edit-ajaxprocessor.jsp?childEndpointID=' + obj[1];
    jQuery("#childEndpointDesign").load(url, null, function (responseText, status, XMLHttpRequest) {
        if (status != "success") {
            CARBON.showErrorDialog(jsi18n["endpoint.design.load.error"]);
        }
    });

    var endpointNodes = obj[2];
    for (i = 0; i < endpointNodes.length; i++) {
        //Add event listners for link for add Child Nodes
        if (YAHOO.util.Dom.hasClass(endpointNodes[i].parentNode, "selected-node")) {
            YAHOO.util.Dom.removeClass(endpointNodes[i].parentNode, "selected-node");
        }
    }
    YAHOO.util.Dom.addClass(obj[0].parentNode, "selected-node");

    //Do the toolbar stuff
    var toolbarNodes = obj[3];
    for (i = 0; i < toolbarNodes.length; i++) {
        toolbarNodes[i].style.display = "none";
    }

    //show the wanted toolbar
    var endpointNode = obj[0].parentNode;
    var endpointNodeChildren = endpointNode.childNodes;
    for (i = 0; i < endpointNodeChildren.length; i++) {
        if (YAHOO.util.Dom.hasClass(endpointNodeChildren[i], "endpointToolbar")) {
            endpointNodeChildren[i].style.display = "";
        }
    }
}
var menuCount = 0;
function addNodesCallback(e, obj) {

    menuCount++;
    var clickedLink = obj[0];
    var clickedEndpointNodeId = obj[1];

    var parms = [clickedLink,'bl','tr'];

    selectNode(document.getElementById(clickedEndpointNodeId));
    //Select the form
    if (menuCount >= 2) {
        oMenu.destroy();
        oMenu = new YAHOO.widget.Menu("basicmenu");
        oMenu.clearContent();
        oMenu.addItems(aMenuItems);
    }

    for (var i = 0; i < oMenu.getItems().length; i++) {
        oMenu.getItem(i).cfg.setProperty("onclick", { fn: childMenuItemClicked, obj:
                [oMenu.getItem(i).id, clickedEndpointNodeId, obj[2]] })
    }

    //Set the position of the menu
    oMenu.cfg.setProperty('context', parms);
    oMenu.render(document.body);
    oMenu.show();
}

//This is to select the from when someone clicks an icon
function selectNode(obj) {
    var allNodes = document.getElementById("treePane").getElementsByTagName("a");
    for (var i = 0; i < allNodes.length; i++) {
        if (YAHOO.util.Dom.hasClass(allNodes[i], "selected")) {
            YAHOO.util.Dom.removeClass(allNodes[i], "selected");
        }
    }
    selectedFlag = obj.id;
    YAHOO.util.Dom.addClass(obj, "selected");
}

function treeColapse(icon) {
    var parentNode = icon.parentNode;
    var allChildren = parentNode.childNodes;
    var todoOther = "";
    var isRoot = false;
    //Do minimizing for the root node
    if (icon.id == "treeColapser") {
        isRoot = true;
    }

    if (isRoot) {
        var iconChilds = icon.parentNode.childNodes;
        for (var i = 0; i < iconChilds.length; i++) {
            if (iconChilds[i].nodeName == "DIV") {
                if (iconChilds[i].className == "branch-node" && iconChilds[i].style.display == "none") {
                    iconChilds[i].style.display = "";
                    YAHOO.util.Dom.removeClass(icon, "plus-icon");
                    YAHOO.util.Dom.addClass(icon, "minus-icon");
                }
                if (iconChilds[i].className == "branch-node" && iconChilds[i].style.display == "") {
                    iconChilds[i].style.display = "none";
                    YAHOO.util.Dom.removeClass(icon, "minus-icon");
                    YAHOO.util.Dom.addClass(icon, "plus-icon");
                }
            }
        }
    }

    //Do minimizing for the rest of the nodes
    for (var i = 0; i < allChildren.length; i++) {
        if (allChildren[i].nodeName == "UL") {
            if (allChildren[i].style.display == "none") {
                allChildren[i].style.display = "";
                YAHOO.util.Dom.removeClass(icon, "plus-icon");
                YAHOO.util.Dom.addClass(icon, "minus-icon");
                todoOther = "show";
                parentNode.style.height = "auto";
            }
            else {
                allChildren[i].style.display = "none";
                YAHOO.util.Dom.removeClass(icon, "minus-icon");
                YAHOO.util.Dom.addClass(icon, "plus-icon");
                todoOther = "hide";
                parentNode.style.height = "50px";
            }
        }
    }
    for (var i = 0; i < allChildren.length; i++) {
        if (allChildren[i].className == "branch-node") {
            if (todoOther == "hide") {
                allChildren[i].style.display = "none";
            } else {
                allChildren[i].style.display = "";
            }
        }
    }
}

function showChildEndpointConfig(endpointPosition) {
    var allDivs = document.getElementById("treePane").getElementsByTagName("*");
    var endpointNodes = new Array();
    var toolbarNodes = new Array();

    for (var i = 0; i < allDivs.length; i++) {
        if (YAHOO.util.Dom.hasClass(allDivs[i], "endpointLink")) {
            endpointNodes.push(allDivs[i]);
        }
        if (YAHOO.util.Dom.hasClass(allDivs[i], "endpointToolbar")) {
            toolbarNodes.push(allDivs[i]);
        }
    }

    for (i = 0; i < endpointNodes.length; i++) {
        if (endpointNodes[i].getAttribute("id") == endpointPosition) {
            endpointCallback(null, [endpointNodes[i],endpointNodes[i].id,endpointNodes,toolbarNodes]);
        }
    }
}
