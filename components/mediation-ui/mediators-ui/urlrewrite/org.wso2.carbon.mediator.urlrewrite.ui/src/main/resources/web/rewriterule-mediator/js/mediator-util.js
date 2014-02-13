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


/**
 * Add new action rows to the action table
 */
function addAction(emptyAction,emptyFragment) {

	if (!isValidVaribles(emptyAction, emptyFragment)) {
        return false;
    }
	
	var displayStyleOfRegexEditor = document.getElementById('regex-th').style.display;
	var displayStyleOfNSEditor = document.getElementById('ns-edior-th').style.display;
	  
	var actionCount = document.getElementById("actionCount");
	var i = actionCount.value;
	var currentCount = parseInt(i);
	currentCount = currentCount + 1;

	actionCount.value = currentCount;

	var actiontable = document.getElementById("actiontable");
	actiontable.style.display = '';

	var actiontbody = document.getElementById("actiontbody");

	var actionRow = document.createElement("tr");
	actionRow.setAttribute("id","actionRow" + i);


	var fragment_selectTD = document.createElement("td");
	fragment_selectTD.appendChild(createFragmentOption("fragment_select" + i,i));


	var action_selectTD = document.createElement("td");
	action_selectTD.appendChild(createActionOption("action_select" + i, i));	

	 var typeTD = document.createElement("td");
	 typeTD.appendChild(createOptionTypeCombobox('optionTypeSelection' + i, i, name));
	
	var value_selectTD = document.createElement("td");

	value_selectTD.innerHTML = "<input type='text' name='mediator.urlrewrite.valuetxt" + i + "' id='mediator.urlrewrite.valuetxt" + i + "'" +
	" class='esb-edit small_textbox' />";



	var nsTD = document.createElement("td");
    nsTD.setAttribute("id", "nsEditorButtonTD" + i);
    nsTD.style.display = displayStyleOfNSEditor;
	
	var regex_selectTD = document.createElement("td");
	regex_selectTD.innerHTML = "<input type='text' name='mediator.urlrewrite.regex" + i + "' id='mediator.urlrewrite.regex" + i + "'" +
	" class='esb-edit small_textbox' />";
	regex_selectTD.style.display = displayStyleOfRegexEditor;

	var deleteTD = document.createElement("td");
	deleteTD .setAttribute("id", "deleteButtonTD" + i);
	deleteTD.appendChild(createActionDeleteLink(i));


	actionRow.appendChild(action_selectTD);
	actionRow.appendChild(fragment_selectTD);
	actionRow.appendChild(typeTD);
	actionRow.appendChild(value_selectTD);	
	actionRow.appendChild(nsTD);
	actionRow.appendChild(regex_selectTD);
	actionRow.appendChild(deleteTD);

	actiontbody.appendChild(actionRow);

	return true;
}

/**
 * Create URL  fragments as combo box options
 * @param id
 * @return
 */
function createFragmentOption(id, i) {

	var combo_box = document.createElement('select');
	combo_box.name = id;
	combo_box.setAttribute("id", id);
	combo_box.style.width = '150px';
    combo_box.onchange = function () {
		onFragmentTypeSelectionChange(id, i)
	};
	
	var choice = document.createElement('option');
	choice.value = 'protocol';
	choice.appendChild(document.createTextNode(urlrewritejsi18n['mediator.urlrewrite.protocol']));
	combo_box.appendChild(choice);

	choice = document.createElement('option');
	choice.value = 'host';
	choice.appendChild(document.createTextNode(urlrewritejsi18n['mediator.urlrewrite.host']));
	combo_box.appendChild(choice);

	choice = document.createElement('option');
	choice.value = 'port';
	choice.appendChild(document.createTextNode(urlrewritejsi18n['mediator.urlrewrite.port']));
	combo_box.appendChild(choice);

	choice = document.createElement('option');
	choice.value = 'path';
	choice.appendChild(document.createTextNode(urlrewritejsi18n['mediator.urlrewrite.path']));
	combo_box.appendChild(choice);

	choice = document.createElement('option');
	choice.value = 'query';
	choice.appendChild(document.createTextNode(urlrewritejsi18n['mediator.urlrewrite.query']));
	combo_box.appendChild(choice);

	choice = document.createElement('option');
	choice.value = 'ref';
	choice.appendChild(document.createTextNode(urlrewritejsi18n['mediator.urlrewrite.ref']));
	combo_box.appendChild(choice);

	choice = document.createElement('option');
	choice.value = 'user';
	choice.appendChild(document.createTextNode(urlrewritejsi18n['mediator.urlrewrite.user']));
	combo_box.appendChild(choice);

	choice = document.createElement('option');
	choice.value = 'full';
	choice.appendChild(document.createTextNode(urlrewritejsi18n['mediator.urlrewrite.full']));
	combo_box.appendChild(choice);

	return combo_box;
}

/**
 * Create 'action' options as combo-box selects
 * @param id
 * @param i
 * @return
 */
function createActionOption(id, i) {
	var combo_box = document.createElement('select');
	combo_box.name = id;
	combo_box.setAttribute("id", id);
	combo_box.style.width = '150px';
	combo_box.onchange = function () {
		onActionTypeSelectionChange(id, i)
	};

	var choice = document.createElement('option');
	choice.value = 'replace';
    choice.setAttribute("id","actionReplace"+i);
    choice.setAttribute("name","actionReplace"+i);
	choice.appendChild(document.createTextNode(urlrewritejsi18n['mediator.urlrewrite.Replace']));
	combo_box.appendChild(choice);

	choice = document.createElement('option');
	choice.value = 'remove';
	choice.setAttribute("id","actionRemove"+i);
    choice.setAttribute("name","actionRemove"+i);
	choice.appendChild(document.createTextNode(urlrewritejsi18n['mediator.urlrewrite.Remove']));
	combo_box.appendChild(choice);

	choice = document.createElement('option');
	choice.value = 'append';
    choice.setAttribute("id","actionAppend"+i);
    choice.setAttribute("name","actionAppend"+i);
	choice.appendChild(document.createTextNode(urlrewritejsi18n['mediator.urlrewrite.Append']));
	combo_box.appendChild(choice);


	choice = document.createElement('option');
	choice.value = 'prepend';
    choice.setAttribute("id","actionPrepend"+i);
    choice.setAttribute("name","actionPrepend"+i);
	choice.appendChild(document.createTextNode(urlrewritejsi18n['mediator.urlrewrite.Prepend']));
	combo_box.appendChild(choice);


	choice = document.createElement('option');
	choice.value = 'set';
    choice.setAttribute("id","actionSet"+i);
    choice.setAttribute("name","actionSet"+i);
	choice.appendChild(document.createTextNode(urlrewritejsi18n['mediator.urlrewrite.Set']));
	combo_box.appendChild(choice);

	return combo_box;

}

/**
 * Select an action
 * @param id
 * @param i
 * @return
 */
function onActionTypeSelectionChange(id, i) {

	var actionType = getSelectedValue(id);
    if (actionType != null) {
		settype(actionType, i);
	}
}

/**
 * Table style changes regarding an action selection
 * @param type
 * @param i
 * @return
 */
function settype(type, i) {

	var valueTD = document.getElementById('mediator.urlrewrite.valuetxt' + i);
	var regexTD = document.getElementById('mediator.urlrewrite.regex' + i);
	var nsEditorButtonTD = document.getElementById('nsEditorButtonTD' + i);


	
	if ( 'set' == type ||  'append' == type || 'prepend' == type) {	
		valueTD.innerHTML="<input type='text' name='mediator.urlrewrite.valuetxt" + i + "' id='mediator.urlrewrite.valuetxt" + i + "'" +
		" class='esb-edit small_textbox' />";

		regexTD.innerHTML = "";			
		resetValueDisplayStyle(type, "");	        
		resetRegexDisplayStyle(type, "none");
		resetOptionDisplayStyle(type, "");	
	}

	if('replace' == type){

		valueTD.innerHTML="<input type='text' name='mediator.urlrewrite.valuetxt" + i + "' id='mediator.urlrewrite.valuetxt" + i + "'" +
		" class='esb-edit small_textbox' />";
		regexTD.innerHTML = "<input type='text' name='mediator.urlrewrite.regex" + i + "' id='mediator.urlrewrite.regex" + i + "'" +
		" class='esb-edit small_textbox' />";

		resetValueDisplayStyle(type, "");
		resetRegexDisplayStyle(type, "");
		resetOptionDisplayStyle(type, "");	
	}
	
	if('remove' == type) {

		valueTD.innerHTML= "";	
		regexTD.innerHTML= "";	

		resetValueDisplayStyle(type, "none");
		resetRegexDisplayStyle(type, "none");	
		resetOptionDisplayStyle(type, "none");	

	}
}

/**
 * Go through all the rows in the table and apply the style only for that particular action row.
 * @param type
 * @param displayStyle
 * @return
 */
function resetnsEditorButtonDisplayStyle(type, displayStyle) {
	var nsCount = document.getElementById("actionCount");
	var i = nsCount.value;
	var currentCount = parseInt(i);

	if (currentCount >= 1) {
		for (var k = 0; k < currentCount; k++) {			
			var nsEditorButtonTD = document.getElementById('nsEditorButtonTD' + k);
			var actionType = getSelectedValue('action_select' + k);
			if(type == actionType) {
				if (nsEditorButtonTD != undefined && nsEditorButtonTD != null) {
					nsEditorButtonTD.style.display = displayStyle;
				}
			}
		}
	}
}

/**
* Go through all the rows in the table and apply the style only for that particular action row.
* @param type
* @param displayStyle
* @return
*/
function resetValueDisplayStyle(type, displayStyle) {
	var nsCount = document.getElementById("actionCount");
	var i = nsCount.value;

	var currentCount = parseInt(i);

	if (currentCount >= 1) {
		for (var k = 0; k < currentCount; k++) {
			var valueTD = document.getElementById('mediator.urlrewrite.valuetxt' + k);        	
			var actionType = getSelectedValue('action_select' + k);
			if(type == actionType) {
				if (valueTD != undefined && valueTD != null) {				
					valueTD.style.display = displayStyle;
				}
			}
		}

	}
}

/**
* Go through all the rows in the table and apply the style only for that particular action row.
* @param type
* @param displayStyle
* @return
*/
function resetRegexDisplayStyle(type, displayStyle) {	
	var nsCount = document.getElementById("actionCount");
	var i = nsCount.value;

	var currentCount = parseInt(i);
	var j = currentCount-1;

	if (currentCount >= 1) {

		for (var k = 0; k < currentCount; k++) {
			var regexTD = document.getElementById('mediator.urlrewrite.regex' + k);   
			var actionType = getSelectedValue('action_select' + k);
			if(type == actionType) {
				if (regexTD != undefined && regexTD != null) {			
					regexTD.style.display = displayStyle;
				}
			}

		}

	}
}

function resetOptionDisplayStyle(type, displayStyle) {	
	var nsCount = document.getElementById("actionCount");
	var i = nsCount.value;

	var currentCount = parseInt(i);
	var j = currentCount-1;

	if (currentCount >= 1) {

		for (var k = 0; k < currentCount; k++) {
			var optionTD = document.getElementById('optionTypeSelection' + k);   
			var actionType = getSelectedValue('action_select' + k);
			if(type == actionType) {
				if (optionTD != undefined && optionTD != null) {			
					optionTD.style.display = displayStyle;
				}
			}

		}

	}
}

function isContainRaw(tbody) {
	if (tbody.childNodes == null || tbody.childNodes.length == 0) {
		return false;
	} else {
		for (var i = 0; i < tbody.childNodes.length; i++) {
			var child = tbody.childNodes[i];
			if (child != undefined && child != null) {
				if (child.nodeName == "tr" || child.nodeName == "TR") {
					return true;
				}
			}
		}
	}
	return false;
}



function createActionDeleteLink(i) {

	var deletehref = document.createElement('a');
	deletehref.setAttribute("href", "#");
	deletehref.className = 'delete-icon-link';
	deletehref.appendChild(document.createTextNode(urlrewritejsi18n["delete"]));
	deletehref.onclick = function () {
		deleteAction(i)
	};
	return deletehref;
}

function deleteAction(i) {
	var actionRow = document.getElementById('actionRow' + i);
	if (actionRow != undefined && actionRow != null) {
		var parentTBody = actionRow.parentNode;
		if (parentTBody != undefined && parentTBody != null) {
			parentTBody.removeChild(actionRow);
			if (!isContainRaw(parentTBody)) {
				var actionTable = document.getElementById("actiontable");
				actionTable.style.display = "none";
			}
		}
	}
	var nsCount = document.getElementById("actionCount");
	if(nsCount != null){
		  nsCount =  nsCount -1;
		  document.getElementById("actionCount").value =nsCount;
	}
}

function getSelectedValue(id) {
	var actionType = document.getElementById(id);
	var actionType_indexstr = null;
	var actionType_value = null;
	if (actionType != null) {
		actionType_indexstr = actionType.selectedIndex;
		if (actionType_indexstr != null) {
			actionType_value = actionType.options[actionType_indexstr].value;
		}
	}
	return actionType_value;
}

function isValidVaribles(actionemptymsg, fragmentemptymsg) {

    var nsCount = document.getElementById("actionCount");
    var i = nsCount.value;
    var currentCount = parseInt(i);

    if (currentCount >= 1) {
        for (var k = 0; k < currentCount; k++) {
            var actionRow = document.getElementById("actionRow" + k);
            if (actionRow != null && actionRow!= undefined) {
                var prefix = document.getElementById("action_select" + k);
                if (prefix != null && prefix != undefined) {
                    if (prefix.value == "") {
                        CARBON.showWarningDialog(actionemptymsg)
                        return false;
                    }
                }
                var prefix2 = document.getElementById("fragment_select" + k);
                if (prefix2 != null && prefix2 != undefined) {
                    if (prefix2.value == "") {
                        CARBON.showWarningDialog(fragmentemptymsg)
                        return false;
                    }
                }
                               
            }
        }
    }
    return true;
}

function createOptionTypeCombobox(id, i, name) {
    // Create the element:
    var combo_box = document.createElement('select');

    // Set some properties:
    combo_box.name = id;
    combo_box.setAttribute("id", id);
    combo_box.onchange = function () {
        onOptionTypeSelectionChange(i, name)
    };
    // Add some choices:
    var choice = document.createElement('option');
    choice.value = 'literal';
    choice.appendChild(document.createTextNode('Value'));
    combo_box.appendChild(choice);

    choice = document.createElement('option');
    choice.value = 'expression';
    choice.appendChild(document.createTextNode('Expression'));
    combo_box.appendChild(choice);

    return combo_box;
}

function onOptionTypeSelectionChange(i) {
    var propertyType = getSelectedValue('optionTypeSelection' + i);
    if (propertyType != null) {
        setPropertyType(propertyType, i);
    }
}

function setPropertyType(type, i) {
    var nsEditorButtonTD = document.getElementById("nsEditorButtonTD" + i);
    if (nsEditorButtonTD == null || nsEditorButtonTD == undefined) {
        return;
    }
    if ("expression" == type) {
        resetDisplayStyle("");
        nsEditorButtonTD.innerHTML = "<a href='#nsEditorLink' id='nsEditorButtonTD" + i + "' class='nseditor-icon-link' style='padding-left:40px' onclick=\"showNameSpaceEditor('mediator.urlrewrite.valuetxt" + i + "')\"> </a>";
    } else {
        nsEditorButtonTD.innerHTML = "";
        if (!isRemainPropertyExpressions()) {
            resetDisplayStyle("none");
        }
    }
}

function getSelectedValue(id) {
    var propertyType = document.getElementById(id);
    var propertyType_indexstr = null;
    var propertyType_value = null;
    if (propertyType != null) {
        propertyType_indexstr = propertyType.selectedIndex;
        if (propertyType_indexstr != null) {
            propertyType_value = propertyType.options[propertyType_indexstr].value;
        }
    }
    return propertyType_value;
}

function resetDisplayStyle(displayStyle) {
    document.getElementById('ns-edior-th').style.display = displayStyle;
    var nsCount = document.getElementById("actionCount");
    var i = nsCount.value;

    var currentCount = parseInt(i);

    if (currentCount >= 1) {
        for (var k = 0; k < currentCount; k++) {
            var nsEditorButtonTD = document.getElementById("nsEditorButtonTD" + k);
            if (nsEditorButtonTD != undefined && nsEditorButtonTD != null) {
                nsEditorButtonTD.style.display = displayStyle;
            }
        }
    }
}

function isRemainPropertyExpressions() {
    var nsCount = document.getElementById("actionCount");
    var i = nsCount.value;

    var currentCount = parseInt(i);

    if (currentCount >= 1) {
        for (var k = 0; k < currentCount; k++) {
            var propertyType = getSelectedValue('optionTypeSelection' + k);
            if ("expression" == propertyType) {
                return true;
            }
        }
    }
    return false;
}

function onFragmentTypeSelectionChange(id, i) {
    var fragmentType = getSelectedValue(id);
    if (fragmentType != null ) {
        if (fragmentType == 'full'|| fragmentType == 'port' ) {
            var actionItem = document.getElementById('actionAppend' + i);
            actionItem.style.display = "none";
            actionItem = document.getElementById('actionPrepend' + i);
            actionItem.style.display = "none";
            actionItem = document.getElementById('actionReplace' + i);
            actionItem.style.display = "none";
            actionItem = document.getElementById('actionRemove' + i);
            actionItem.style.display = "none";
            
            var selectedAction = getSelectedValue('action_select' + i);
            if (selectedAction != null && selectedAction != 'set') {
                document.getElementById('action_select' + i).value = 'set';
            }

        } else {
            var actionItem = document.getElementById('actionAppend' + i);
            actionItem.style.display = "";
            actionItem = document.getElementById('actionPrepend' + i);
            actionItem.style.display = "";
            actionItem = document.getElementById('actionReplace' + i);
            actionItem.style.display = "";
            actionItem = document.getElementById('actionRemove' + i);
            actionItem.style.display = "";
        }
    }
}