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

function tasksave(namemsg, classmsg, cronmsg, countmsg, intervalmsg, msgEntryInfo,propertyTableErrorMsg, form) {

    if (!isNameValid(document.getElementById('taskName').value)) {
        CARBON.showWarningDialog(namemsg);
        return false;
    }
    if (document.getElementById('taskClass').value == '') {
        CARBON.showWarningDialog(classmsg);
        return false;
    }

    var trigger = document.getElementById("taskTrigger");
    var triggerValue = getCheckedValue(trigger);
    if (triggerValue == 'simple') {

        var triggerInterval = document.getElementById("triggerInterval");
        if (triggerInterval != undefined && triggerInterval != null) {
            if (!isNumber(triggerInterval.value)) {
                CARBON.showWarningDialog(intervalmsg);
                return false;
            }

        }
    } else {
        var triggerCron = document.getElementById("triggerCron");
        if (triggerCron != undefined && triggerCron != null) {
            if (triggerCron.value == '') {
                CARBON.showWarningDialog(cronmsg);
                return false;
            }
        }
    }

    //Injecting the message to a named sequence or proxy service message property is mandatory to set. This check
    // whether message exists or not when using org.apache.synapse.startup.tasks.MessageInjector class, as if message is
    // not set there will be an exception thrown in the console
    if ((document.getElementById('taskClass') != null) && (document.getElementById('taskClass').value.trim() == 'org.apache.synapse.startup.tasks.MessageInjector')) {
        var propertyTable = document.getElementById('property_table');
        if (propertyTable != null) {
            var propertyTableRows = propertyTable.getElementsByTagName('tr');
            if (propertyTableRows != null) {
                for (var i = 0; i < propertyTableRows.length; i++) {
                    var inputs = propertyTableRows[i].getElementsByTagName('input');
                    if ((inputs != null) && (inputs.length > 0)) {
                        if (inputs[0].value.trim() == 'message') {
                            if (propertyTableRows[i].getElementsByTagName('textarea')[0].value.trim() != '') {
                                break;
                            }
                            if (inputs[2].value.trim() != '') {
                                break;
                            }
                            CARBON.showWarningDialog(msgEntryInfo);
                            return false;
                        }
                    }
                }
            }
        } else {
            CARBON.showWarningDialog(propertyTableErrorMsg);
            return false;
        }
    }

    validateClass(document.getElementById('taskClass').value, document.getElementById('taskGroup').value, form);
    return false;
}

function settrigger(type) {
    var triggerCountTR = document.getElementById("triggerCountTR");
    var triggerIntervalTR = document.getElementById("triggerIntervalTR");
    var triggerCronTR = document.getElementById("triggerCronTR");
    var taskTrigger_hidden = document.getElementById("taskTrigger_hidden");
    taskTrigger_hidden.value = type;
    if ('cron' == type) {
        triggerIntervalTR.style.display = "none";
        triggerCountTR.style.display = "none";
        triggerCronTR.style.display = "";
    } else if ('simple' == type) {
        triggerIntervalTR.style.display = "";
        triggerCountTR.style.display = "";
        triggerCronTR.style.display = "none";
    }
    return true;
}

function getCheckedValue(radioObj) {
    if (!radioObj) {
        return "";
    }
    var radioLength = radioObj.length;
    if (radioLength == undefined) {
        if (radioObj.checked) {
            return radioObj.value;
        } else {
            return "";
        }
    }
    for (var i = 0; i < radioLength; i++) {
        if (radioObj[i].checked) {
            return radioObj[i].value;
        }
    }
    return "";
}

function onpropertyTypechange(index) {
    var indexstr = index.toString();
    var propertyType = document.getElementById(("propertyTypeSelection" + indexstr).toString());
    var propertyType_indexstr = null;
    var propertyType_value = null;
    if (propertyType != null)
    {
        propertyType_indexstr = propertyType.selectedIndex;
        if (propertyType_indexstr != null) {
            propertyType_value = propertyType.options[propertyType_indexstr].value;
        }
    }
    var textField = document.getElementById("textField" + index);
    var textArea = document.getElementById("textArea" + index);
    if (propertyType_value != null && propertyType_value != undefined && propertyType_value != "") {
        if (propertyType_value == 'literal') {
            textField.style.display = "";
            textArea.style.display = "none";

        } else if (propertyType_value == 'xml') {
            textField.style.display = "none";
            textArea.style.display = "";
        }
    }
}


function deleteproperty(i) {
    var propRow = document.getElementById("pr" + i);
    if (propRow != undefined && propRow != null) {
        var parentTBody = propRow.parentNode;
        if (parentTBody != undefined && parentTBody != null) {
            parentTBody.removeChild(propRow);
            if (!isContainRaw(parentTBody)) {
                var propertyTable = document.getElementById("property_table");
                propertyTable.style.display = "none";
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

function deleteRow(name, group){
    CARBON.showConfirmationDialog(taskjsi18n["task.delete.waring"] + " ' " + name + " ' ?", function() {
        document.location.href = "deletetask.jsp?" + "taskName=" + name + "&taskGroup=" + group;
    });
}

function editRow(name, group) {

    document.location.href = "edittask.jsp?" + "taskName=" + name + "&taskGroup=" + group + "&ordinal=1";
}

function editCAppRow(name, group) {

    CARBON.showConfirmationDialog("The changes will not persist to the CAPP after restart or redeploy. Do you want to Edit?", function() {
        $.ajax({
            type: 'POST',
            success: function() {
                document.location.href = "edittask.jsp?" + "taskName=" + name + "&taskGroup=" + group + "&ordinal=1";
            }
        });
    });
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

function autoredioselect() {
    settrigger(document.getElementById("taskTrigger_hidden").value);
}




