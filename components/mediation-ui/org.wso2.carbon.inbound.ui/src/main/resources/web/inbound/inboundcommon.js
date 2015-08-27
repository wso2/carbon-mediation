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
    if (document.getElementById('interval') != null && isNaN(document.getElementById('interval').value)) {
        CARBON.showWarningDialog(msg3);
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

    if(kafkaSpecialParameters != null){
        for(var i = 0;i<kafkaSpecialParameters.length;i++){
            if(document.getElementById(kafkaSpecialParameters[i]) != null) {
                if (document.getElementById(kafkaSpecialParameters[i]).value.trim() == '') {
                    CARBON.showWarningDialog(msg5);
                    return false;
                }
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
    if (document.getElementById('interval') != null && isNaN(document.getElementById('interval').value)) {
        CARBON.showWarningDialog(msg3);
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

    if(kafkaSpecialParameters != null){
        for(var i = 0;i<kafkaSpecialParameters.length;i++){
            if(document.getElementById(kafkaSpecialParameters[i]) != null) {
                if (document.getElementById(kafkaSpecialParameters[i]).value.trim() == '') {
                    CARBON.showWarningDialog(msg5);
                    return false;
                }
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
	    	    if(iParamMax < 0 || iParamMax < iParamCount){
		    		var table = document.getElementById(tableID);
		    		var rowCount = table.rows.length;
		    		table.deleteRow((rowCount-2));
		    		iParamCount--;	    	    	
	    	    }
	        }catch(e) {
	            //alert(e);
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

function showSpecialFields(specialParams, inboundDescriptionOfParams, topicListParams) {
    var strSplitter = "~:~";
    var consumerType = document.getElementById('consumer.type').value.trim();
    var specialFieldsArea = '<table id="tblSpeInput" name="tblSpeInput" cellspacing="0" cellpadding="0" border="0">';
        allSpecialParams = specialParams.split(",");
        splitedInboundDescription = inboundDescriptionOfParams.replace("{","").replace("}","").split(",");
        for(var i=0; i<allSpecialParams.length; i++){
        var specialParam = allSpecialParams[i];
        if((consumerType == "highlevel" && (specialParam.indexOf(strSplitter) > -1 && specialParam.split(strSplitter)[0].trim() == "topics/topic.filter")) || (consumerType == "simple" && consumerType == specialParam.split(".")[0])){
            var val = "";
            if(inboundDescriptionOfParams != ""){
                for(var j=0; j<splitedInboundDescription.length; j++){
                    if(splitedInboundDescription[j].split("=")[0].trim() == specialParam || (specialParam.indexOf(strSplitter) > -1 && splitedInboundDescription[j].split("=")[0] == specialParam.split(strSplitter)[1].trim())){
                        val = splitedInboundDescription[j].split("=")[1];
                        break;
                    }
                }
            }

            if(consumerType == "highlevel" && (specialParam.indexOf(strSplitter) > -1 && specialParam.split(strSplitter)[0].trim() == "topics/topic.filter")) {
                specialFieldsArea = specialFieldsArea  + '<tr><td style="width:167px">'+specialParam.split(strSplitter)[0].trim()+'<span class="required">*</span></td><td align="left"><select id="topicsOrTopicFilter" name="topicsOrTopicFilter" onchange="javascript:showTopicsOrTopicFilterFields(\''+inboundDescriptionOfParams+'\',\''+topicListParams+'\')">';
                var tLists = specialParam.split(strSplitter);
                for(var t = 1; t < tLists.length; t++){
                    specialFieldsArea = specialFieldsArea + '<option value="'+tLists[t].trim()+'">'+tLists[t].trim()+'</option>';
                }
                specialFieldsArea = specialFieldsArea + '</select></td><td></td></tr>';
                specialFieldsArea = specialFieldsArea + '<tr><td colspan="3"><div id="tDiv"><table>';
                if(tLists[0].trim() == "topic.filter") {
                    var fLists = topicListParams.split(strSplitter);
                    var listval = fLists[1].trim();
                    if(inboundDescriptionOfParams != ""){
                        for(var j=0; j<splitedInboundDescription.length; j++){
                            if((splitedInboundDescription[j].split("=")[0].trim() == "filter.from.whitelist" || splitedInboundDescription[j].split("=")[0].trim() == "filter.from.blacklist") && Boolean(splitedInboundDescription[j].split("=")[1])){
                                listval = splitedInboundDescription[j].split("=")[0].trim();
                                break;
                            }
                        }
                    }
                    specialFieldsArea = specialFieldsArea + '<tr><td style="width:157px">'+fLists[0].trim()+'<span class="required">*</span></td><td align="left"><select id="'+fLists[0].trim()+'" name="'+fLists[0].trim()+'">';
                    for(var l=1; l<fLists.length; l++){
                        if(listval == fLists[l].trim()){
                            specialFieldsArea = specialFieldsArea + '<option value="'+fLists[l].trim()+'" selected>'+fLists[l].trim()+'</option>';
                        } else{
                            specialFieldsArea = specialFieldsArea + '<option value="'+fLists[l].trim()+'">'+fLists[l].trim()+'</option>';
                        }
                    }
                    specialFieldsArea = specialFieldsArea + '</select></td><td></td></tr>';
                }
                specialFieldsArea = specialFieldsArea + '<tr><td style="width:157px">'+specialParam.split(strSplitter)[1].trim()+' name<span class="required">*</span></td><td align="left"><input id="'+specialParam.split(strSplitter)[1].trim()+'" name="'+specialParam.split(strSplitter)[1].trim()+'" class="longInput" type="text" value="'+val+'"/></td><td></td></tr></table></div></td></tr>';
            } else{
                specialFieldsArea = specialFieldsArea + '<tr><td style="width:167px">'+specialParam.replace(consumerType+".","")+'<span class="required">*</span></td><td align="left"><input id="'+specialParam+'" name="'+specialParam+'" class="longInput" type="text" value="'+val+'"/></td><td></td></tr>';
            }
            }
        }
   specialFieldsArea = specialFieldsArea  + '</table>';
   document.getElementById('specialFieldsForm').innerHTML = specialFieldsArea;
}

function showTopicsOrTopicFilterFields(inboundDescriptionOfParams, topicListParams) {
    var strSplitter = "~:~";
    var tField = document.getElementById('topicsOrTopicFilter').value.trim();
    var tFieldsArea = '<table>';
    var val = "";
    splitedInboundDescription = inboundDescriptionOfParams.replace("{","").replace("}","").split(",");
    if(inboundDescriptionOfParams != ""){
        for(var j=0; j<splitedInboundDescription.length; j++){
            if(splitedInboundDescription[j].split("=")[0].trim() == tField){
                val = splitedInboundDescription[j].split("=")[1];
                break;
            }
        }
    }
    if(tField == "topic.filter") {
    var fLists = topicListParams.split(strSplitter);
    var listval = fLists[1].trim();
        if(inboundDescriptionOfParams != ""){
            for(var j=0; j<splitedInboundDescription.length; j++){
                if((splitedInboundDescription[j].split("=")[0].trim() == "filter.from.whitelist" || splitedInboundDescription[j].split("=")[0].trim() == "filter.from.blacklist") && Boolean(splitedInboundDescription[j].split("=")[1])){
                    listval = splitedInboundDescription[j].split("=")[0].trim();
                    break;
                }
            }
        }
        tFieldsArea = tFieldsArea + '<tr><td style="width:157px">'+fLists[0].trim()+'<span class="required">*</span></td><td align="left"><select id="'+fLists[0].trim()+'" name="'+fLists[0].trim()+'">';
        for(var l=1; l<fLists.length; l++){
            if(listval == fLists[l].trim()){
                tFieldsArea = tFieldsArea + '<option value="'+fLists[l].trim()+'" selected>'+fLists[l].trim()+'</option>';
            } else{
                tFieldsArea = tFieldsArea + '<option value="'+fLists[l].trim()+'">'+fLists[l].trim()+'</option>';
            }
        }
        tFieldsArea = tFieldsArea + '</select></td><td></td></tr>';
    }
    tFieldsArea = tFieldsArea + '<tr><td style="width:157px">'+tField.trim()+' name<span class="required">*</span></td><td align="left"><input id="'+tField.trim()+'" name="'+tField.trim()+'" class="longInput" type="text" value="'+val+'"/></td><td></td></tr>';
    tFieldsArea = tFieldsArea + '</table>';
    document.getElementById('tDiv').innerHTML = tFieldsArea;
}

