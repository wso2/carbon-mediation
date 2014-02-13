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

//This is the methord called when the menu items are selected
function childMenuItemClicked(e, e2, objx) {
    //Extracting the obj elements
    var typetoAdd = objx[0];
    var addingOn = objx[1];
    var todo = objx[2];

    var sequenceName = document.getElementById("sequence.name").value;
    if (sequenceName == "") {
        CARBON.showWarningDialog("Please specify the sequence name before adding mediators");
        return;
    }

    var url = "design_sequence-ajaxprocessor.jsp?position=" + addingOn + "&mediatorName="
            + typetoAdd + "&type=" + todo + "&sequenceName="
            + document.getElementById("sequence.name").value + "&onErrorKey="
            + document.getElementById("sequence.onerror.key").value + "&mediatorAction=add";

    jQuery("#treePane").load(url, null, function (responseText, status, XMLHttpRequest) {
        if (status != "success") {
            CARBON.showErrorDialog(jsi18n["mediator.design.load.error"]);
        } else {
            initMediators();
            var ele = document.getElementById("addMediatorPosition");
            if (ele != null && ele != undefined) {
                var pos = ele.value;
                if (pos != null && pos != "") {
                    showMediatorConfig(pos);
                }
            }
        }
    });
}

//This method reads the dom tree of the html tree and register events
// and callback funtions for them
function initMediators() {

    var allDivs = document.getElementById("treePane").getElementsByTagName("*");
    var addChildNodes = new Array();
    var addSiblingNodes = new Array();
    var moveupNodes = new Array();
    var movedownNodes = new Array();
    var deleteNodes = new Array();
    var mediatorNodes = new Array();
    var toolbarNodes = new Array();
    var rootNode;

    for (var i = 0; i < allDivs.length; i++) {
            if (YAHOO.util.Dom.hasClass(allDivs[i], "addChildStyle")) {
                addChildNodes.push(allDivs[i]);
            }
            if (YAHOO.util.Dom.hasClass(allDivs[i], "addSiblingStyle")) {
                addSiblingNodes.push(allDivs[i]);
            }
            if (YAHOO.util.Dom.hasClass(allDivs[i], "moveUpStyle")) {
                moveupNodes.push(allDivs[i]);
            }
            if (YAHOO.util.Dom.hasClass(allDivs[i], "moveDownStyle")) {
                movedownNodes.push(allDivs[i]);
            }
            if (YAHOO.util.Dom.hasClass(allDivs[i], "deleteStyle")) {
                deleteNodes.push(allDivs[i]);
            }
            if (YAHOO.util.Dom.hasClass(allDivs[i], "mediatorLink")) {
                mediatorNodes.push(allDivs[i]);
            }
            if (YAHOO.util.Dom.hasClass(allDivs[i], "sequenceToolbar")) {
                toolbarNodes.push(allDivs[i]);
            }
            if (YAHOO.util.Dom.hasClass(allDivs[i], "root-mediator")) {
                rootNode = allDivs[i];
            }

    }
    
    for (i = 0; i < addChildNodes.length; i++) {
        //Add event listners for link for add Child Nodes
        YAHOO.util.Event.addListener(addChildNodes[i], "click", addNodesCallback,
                [addChildNodes[i],addChildNodes[i].parentNode.parentNode.parentNode.id,"child"]);
    }
    for (i = 0; i < addSiblingNodes.length; i++) {
        //Add event listners for link for add Child Nodes
        YAHOO.util.Event.addListener(addSiblingNodes[i], "click", addNodesCallback,
                [addSiblingNodes[i],
                        addSiblingNodes[i].parentNode.parentNode.parentNode.id, "sibling"]);
    }
    for (i = 0; i < moveupNodes.length; i++) {
        //Add event listners for link for move up
        YAHOO.util.Event.addListener(moveupNodes[i], "click", moveupCallback,
                [moveupNodes[i],moveupNodes[i].parentNode.parentNode.parentNode.id]);
    }
    for (i = 0; i < movedownNodes.length; i++) {
        //Add event listners for link for move down
        YAHOO.util.Event.addListener(movedownNodes[i], "click", movedownCallback,
                [movedownNodes[i],movedownNodes[i].parentNode.parentNode.parentNode.id]);
    }
    for (i = 0; i < deleteNodes.length; i++) {
        //Add event listners for link for delete
        YAHOO.util.Event.addListener(deleteNodes[i], "click", deleteCallback,
                [deleteNodes[i],deleteNodes[i].parentNode.parentNode.parentNode.id,"sibling"]);
    }

    for (i = 0; i < mediatorNodes.length; i++) {
        //Add event listners for link for add Child Nodes
        YAHOO.util.Event.addListener(mediatorNodes[i], "click", mediatorCallback,
                [mediatorNodes[i],mediatorNodes[i].id,mediatorNodes,toolbarNodes]);
    }

    YAHOO.util.Event.addListener(rootNode, "click", rootCallback,
            [rootNode,toolbarNodes,mediatorNodes]);

}

function focusRootMediator() {
    var allDivs = document.getElementById("treePane").getElementsByTagName("*");
    var mediatorNodes = new Array();
    var toolbarNodes = new Array();
    var rootNode;

    for (var i = 0; i < allDivs.length; i++) {
        if (YAHOO.util.Dom.hasClass(allDivs[i], "mediatorLink")) {
            mediatorNodes.push(allDivs[i]);
        }
        if (YAHOO.util.Dom.hasClass(allDivs[i], "sequenceToolbar")) {
            toolbarNodes.push(allDivs[i]);
        }
        if (YAHOO.util.Dom.hasClass(allDivs[i], "root-mediator")) {
            rootNode = allDivs[i];
        }
    }

    rootCallback(null, [rootNode,toolbarNodes,mediatorNodes]);
}

function rootCallback(e,obj) {
    var mediatorNodes=obj[2];

    for (i = 0; i < mediatorNodes.length; i++) {
        //Add event listners for link for add Child Nodes
        if(YAHOO.util.Dom.hasClass(mediatorNodes[i].parentNode,"selected-node")) {
            YAHOO.util.Dom.removeClass(mediatorNodes[i].parentNode,"selected-node");
        }
    }

    //Do the toolbar stuff
    var toolbarNodes = obj[1];
    for (i = 0; i < toolbarNodes.length; i++) {
            toolbarNodes[i].style.display = "none";
    }

   //show the wanted toolbar
    var mediatorNode = obj[0].parentNode;
    var mediatorNodeChildren =mediatorNode.childNodes;
    for(i=0;i<mediatorNodeChildren.length;i++){
        if(mediatorNodeChildren[i].nodeName == "DIV"){
            if(YAHOO.util.Dom.hasClass(mediatorNodeChildren[i],"sequenceToolbar")) {
                mediatorNodeChildren[i].style.display = "";
            }
        }
    }

    hideMediatorTab();
}

function hideMediatorTab() {
    /* get the mediator edit pane and set it to empty */
    var element = document.getElementById('mediator-designview-header');
    if (element != null && element != undefined) {
        element.style.display = "none";
    }
    element = document.getElementById('mediator-sourceview-header');
    if (element != null && element != undefined) {
        element.style.display = "none";
    }
    element = document.getElementById('mediator-edit-tab');
    if (element != null && element != undefined) {
        element.style.display = "none";
    }
    element = document.getElementById('mediatorDesign');
    if (element != null && element != undefined) {
        element.innerHTML = "";
    }
    element = document.getElementById('mediatorSource');
    if (element != null && element != undefined) {
        element.innerHTML = "";
    }
}

function moveupCallback(e,obj) {
    var clickedMediatorNodeId = obj[1];

    var url = "design_sequence-ajaxprocessor.jsp?mediatorID="
            + clickedMediatorNodeId + "&mediatorAction=moveup";

    jQuery("#treePane").load(url, null, function (responseText, status, XMLHttpRequest) {
        if (status != "success") {
            CARBON.showErrorDialog(jsi18n["mediator.design.load.error"]);
        } else {
            initMediators();
            focusRootMediator();
        }
    });
}

function movedownCallback(e,obj) {
    var clickedMediatorNodeId = obj[1];

    var url ="design_sequence-ajaxprocessor.jsp?mediatorID="
            + clickedMediatorNodeId + "&mediatorAction=movedown";

    jQuery("#treePane").load(url, null, function (responseText, status, XMLHttpRequest) {
        if (status != "success") {
            CARBON.showErrorDialog(jsi18n["mediator.design.load.error"]);
        } else {
            initMediators();
            focusRootMediator();
        }
    });
}

function deleteCallback(e,obj) {
    var clickedMediatorNodeId = obj[1];

    CARBON.showConfirmationDialog(jsi18n["mediator.delete.confirmation"], function() {
        var url = "design_sequence-ajaxprocessor.jsp?mediatorID="
                + clickedMediatorNodeId + "&mediatorAction=delete";
        jQuery("#treePane").load(url, null, function (responseText, status, XMLHttpRequest) {
            if (status != "success") {
                CARBON.showErrorDialog(jsi18n["mediator.design.load.error"]);
            } else {
                initMediators();
                /* get the mediator edit pane and set it to empty */
                hideMediatorTab();
                /* set the focus to root mediator */
                focusRootMediator();
            }
        });
    });
}

function mediatorCallback(e, obj) {
    showObj("mediator-designview-header");
    hide("mediator-sourceview-header");
    showObj("mediator-edit-tab");
    hide("mediatorSource");
    showObj("mediatorDesign");
    var url = 'mediator-edit-ajaxprocessor.jsp?mediatorID=' + obj[1];
    jQuery("#mediatorDesign").load(url, null, function (responseText, status, XMLHttpRequest) {
        if (status != "success") {
            CARBON.showErrorDialog(jsi18n["mediator.design.load.error"]);
        }
    });
    var mediatorNodes=obj[2];

    for (i = 0; i < mediatorNodes.length; i++) {
        //Add event listners for link for add Child Nodes
        if(YAHOO.util.Dom.hasClass(mediatorNodes[i].parentNode,"selected-node")) {
            YAHOO.util.Dom.removeClass(mediatorNodes[i].parentNode,"selected-node");
        }
    }
    YAHOO.util.Dom.addClass(obj[0].parentNode, "selected-node");

    //Do the toolbar stuff
    var toolbarNodes = obj[3];
    for (i = 0; i < toolbarNodes.length; i++) {
            toolbarNodes[i].style.display = "none";
    }

   //show the wanted toolbar
    var mediatorNode = obj[0].parentNode;
    var mediatorNodeChildren =mediatorNode.childNodes;
    for(i=0;i<mediatorNodeChildren.length;i++){
        if(YAHOO.util.Dom.hasClass(mediatorNodeChildren[i],"sequenceToolbar")) {
           mediatorNodeChildren[i].style.display = ""; 
        }
    }
}
var menuCount = 0;
function addNodesCallback(e,obj) {
    menuCount++;
    var clickedLink = obj[0];
    var clickedMediatorNodeId = obj[1];

    var parms = [clickedLink,'bl','tr'];

    selectNode(document.getElementById(clickedMediatorNodeId));
    //Select the form
    if(menuCount>=2) {
           
	    oMenu.destroy();
	    oMenu = new YAHOO.widget.Menu("basicmenu");
	    oMenu.clearContent();
	    oMenu.addItems(aMenuItems);

    }
    var g = oMenu.getItems().length;
    for(var i=0;i<g;i++){
        var rootMenuItem = oMenu.getItem(i).cfg.getProperty("submenu").getItems();
        for(var j=0;j<rootMenuItem.length;j++){
            rootMenuItem[j].cfg.setProperty("onclick", { fn: childMenuItemClicked, obj:
                    [rootMenuItem[j].id, clickedMediatorNodeId, obj[2]] })
        }
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
     if(icon.id =="treeColapser"){
      isRoot = true;
      }

    if (isRoot) {
      var iconChilds =icon.parentNode.childNodes;
      for (var i = 0; i < iconChilds.length; i++) {
        if(iconChilds[i].nodeName == "DIV"){
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
