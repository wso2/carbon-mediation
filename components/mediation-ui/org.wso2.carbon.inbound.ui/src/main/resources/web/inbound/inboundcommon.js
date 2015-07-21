function isNameValid(namestring) {
    if (namestring != null && namestring != "") {
        for (var j = 0; j < namestring.length; j++)
        {
            var ch = namestring.charAt(j);
            var code = ch.charCodeAt(0);
            if ((code > 47 && code < 59) // number
                    || (code > 64 && code < 91)// capital 
                    || (code > 96 && code < 123)// simple
                    || (code == 46) //dot
                    || (code == 95) // underscore
                    || (code == 2013) // en dash
                    || (code == 2014) // em dash
                    || (code == 45)) // minus sign - hyphen
            {
            }
            else {
                return false;
            }
        }
        return true;
    } else {
        return false;
    }
}

function isNumber(numberstring) {
    if (numberstring != null && numberstring != "") {
        for (var j = 0; j < numberstring.length; j++) {
            var ch = numberstring.charAt(j);
            var code = ch.charCodeAt(0);
            if (code > 47 && code < 59) {
            } else {
                return false;
            }
        }
        return true;
    } else {
        return false;
    }
}


function forward(destinationJSP) {
    location.href = destinationJSP;
}

function addParam(form){
	form.action='newInbound1.jsp?action=addParam';
    form.submit();
    return false;	
}

function inboundsave1(namemsg, typemsg, form , existingList){
  var name;
    if (!isNameValid(document.getElementById('inboundName').value)) {
        CARBON.showWarningDialog(namemsg);
        return false;
    }
     name = document.getElementById('inboundName').value;
         if(existingInbounds != null ){
         for(var i=0; i< existingInbounds.length;i++){
              if(name == existingInbounds[i]){
              CARBON.showErrorDialog("Inbound endpoint already exists please use separate name");
              return false;
              }
             }
            }
    if (document.getElementById('inboundType').value == '') {
        CARBON.showWarningDialog(typemsg);
        return false;
    }
    form.submit();
    return false;	
}

function inboundsave2(msg1,msg2,msg3,msg4,msg5,form){
    if (sequenceRequired && document.getElementById('inboundSequence').value == '') {
        CARBON.showWarningDialog(msg1);
        return false;
    }	
    if (onErrorRequired && document.getElementById('inboundErrorSequence').value == '') {
        CARBON.showWarningDialog(msg2);
        return false;
    }        
    if (classRequired && document.getElementById('inboundClass').value == '') {
        CARBON.showWarningDialog(msg4);
        return false;
    }    
    
    if(requiredParams != null){
    	for(var i = 0;i<requiredParams.length;i++){
    	    if (document.getElementById(requiredParams[i]).value.trim() == '') {
    	        CARBON.showWarningDialog(msg5);
    	        return false;
    	    }    		
    	}
    }
    
    form.submit();
    return false;	
}

function inboundUpdate(msg1,msg2,msg3,msg4,msg5,form){
    if (sequenceRequired && document.getElementById('inboundSequence').value == '') {
        CARBON.showWarningDialog(msg1);
        return false;
    }	
    if (onErrorRequired && document.getElementById('inboundErrorSequence').value == '') {
        CARBON.showWarningDialog(msg2);
        return false;
    }         
    if (classRequired && document.getElementById('inboundClass').value == '') {
        CARBON.showWarningDialog(msg4);
        return false;
    }    	
    
    if(requiredParams != null){
    	for(var i = 0;i<requiredParams.length;i++){
    	    if (document.getElementById(requiredParams[i]).value.trim() == '') {
    	        CARBON.showWarningDialog(msg5);
    	        return false;
    	    }    		
    	}
    }    
    
    form.submit();
    return false;	
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

function deleteRecord(name){
    CARBON.showConfirmationDialog(taskjsi18n["inbound.delete.waring"] + " ' " + name + " ' ?", function() {
        document.location.href = "deleteInbound.jsp?" + "inboundName=" + name ;
    });
}

function editRecord(name) {
    document.location.href = "editInbound.jsp?" + "name=" + name;
}

function onclassnamefieldchange(id) {
    var classnmae = document.getElementById("taskClass").value;
    if (classnmae != null && classnmae != undefined && classnmae != "") {
        document.getElementById(id).style.display = "";
    } else {
        document.getElementById(id).style.display = "none";
    }
}

function validateClass(className, group, form) {
    var url = '../task/validateclass-ajaxprocessor.jsp?taskClass=' + className + "&taskGroup=" + group;
    jQuery.get(url, ({}),
            function(data, status) {
                if (status != "success") {
                    CARBON.showWarningDialog(taskjsi18n['error.occurred']);
                    return false;
                } else {
                    var returnValue = trim(data);
                    if (returnValue != null && returnValue != undefined && returnValue != "") {
                        CARBON.showErrorDialog(returnValue);
                        return false;
                    } else {
                        form.submit();
                    }
                }
            });
    return false;
}
function ltrim(str) {
    for (var k = 0; k < str.length && str.charAt(k) <= " "; k++) ;
    return str.substring(k, str.length);
}
function rtrim(str) {
    for (var j = str.length - 1; j >= 0 && str.charAt(j) <= " "; j--) ;
    return str.substring(0, j + 1);
}

//This function accepts a String and trims the string in both sides of the string ignoring space characters
function trim(stringValue) {
    //   var trimedString = stringValue.replace( /^\s+/g, "" );
    //   return trimedString.replace( /\s+$/g, "" );
    return ltrim(rtrim(stringValue));
}

function goBackOnePage() {
    history.go(-1);
}

function goBackTwoPages() {
    history.go(-2);
}

function autoredioselect() {
    settrigger(document.getElementById("taskTrigger_hidden").value);
}

function addRow(tableID) {
	 
    var table = document.getElementById(tableID);

    var rowCount = table.rows.length;
    var row = table.insertRow((rowCount-1));

    iParamCount++;
    
    var cell1 = row.insertCell(0);
    var element1 = document.createElement("input");
    element1.type = "text";
    element1.name="paramkey" + iParamCount;
    element1.id="paramkey" + iParamCount;
    cell1.appendChild(element1);

    var cell2 = row.insertCell(1);
    var element2 = document.createElement("input");
    element2.name="paramval" + iParamCount;
    element2.id="paramval" + iParamCount;
    cell2.appendChild(element2);


}

function deleteRow(tableID) {
	if(iParamCount > 0){
	    try {
	    	    if(iParamMax < iParamCount){
		    		var table = document.getElementById(tableID);
		    		var rowCount = table.rows.length;
		    		table.deleteRow((rowCount-2));
		    		iParamCount--;	    	    	
	    	    }
	        }catch(e) {
	            alert(e);
	        }
	}
}

function showAdvancedOptions(id) {
    var formElem = document.getElementById(id + '_advancedForm');
    if (formElem.style.display == 'none') {
        formElem.style.display = '';
        document.getElementById(id + '_adv').innerHTML = '<a class="icon-link" ' +
                                                         'onclick="javascript:showAdvancedOptions(\'' + id + '\');" style="background-image: url(images/up.gif);">' + taskjsi18n['hide.advanced.options'] + '</a>';
    } else {
        formElem.style.display = 'none';
        document.getElementById(id + '_adv').innerHTML = '<a class="icon-link" ' +
                                                         'onclick="javascript:showAdvancedOptions(\'' + id + '\');" style="background-image: url(images/down.gif);">' + taskjsi18n['show.advanced.options'] + '</a>';
    }
}

function showSpecialFields(specialParams, inboundDescriptionOfParams) {
    var consumerType = document.getElementById('consumer.type').value;
    var specialFieldsArea = '<table id="tblSpeInput" name="tblSpeInput" cellspacing="0" cellpadding="0" border="0">';
        allSpecialParams = specialParams.split(",");
        splitedInboundDescription = inboundDescriptionOfParams.replace("{","").replace("}","").split(",");
        for(var i=0; i<allSpecialParams.length; i++){
            if((consumerType == "highlevel" && allSpecialParams[i] == "topics") || (consumerType == "simple" && consumerType == allSpecialParams[i].split(".")[0])){
            var val = "";
            if(inboundDescriptionOfParams != ""){
                for(var j=0; j<splitedInboundDescription.length; j++){
                    if(splitedInboundDescription[j].split("=")[0].trim() == allSpecialParams[i]){
                        val = splitedInboundDescription[j].split("=")[1];
                        break;
                    }
                }
            }
                specialFieldsArea = specialFieldsArea  + '<tr><td style="width:167px">'+allSpecialParams[i].replace(consumerType+".","")+'<span class="required">*</span></td><td align="left"><input id="'+allSpecialParams[i]+'" name="'+allSpecialParams[i]+'" class="longInput" type="text" value="'+val+'"/></td><td></td></tr>';
            }
        }
   specialFieldsArea = specialFieldsArea  + '</table>';
   document.getElementById('specialFieldsForm').innerHTML = specialFieldsArea;
}

