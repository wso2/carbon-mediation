function forward(destinationJSP) {
    location.href = destinationJSP;
}
function updateConfiguration(form) {
    return submitconform('update', form);
}

function forceUpdateConfiguration(form) {
    return submitWithForce('update', form);
}

function saveConfigurationToDisk(form) {
    return submitconform('save', form);
}

function submitconform(action, form) {
    var theString = editAreaLoader.getValue("rawConfig");
    var isXml = isValidXml(trim(theString));
    if (!isXml) {
        return false;
    }

    //set the hidden filed value to the new code created
    document.getElementById("rawConfig").value = editAreaLoader.getValue("rawConfig");
    form.action = 'saveconfig.jsp';
    CARBON.showLoadingDialog('Configuration is Updating. It may take few moments. Please wait..');

    var url = 'validate-conf-ajaxprocessor.jsp';

    jQuery.ajax({
        type: "POST",
        url: url,
        data: {synConfig : theString},
        timeout: 120000, 
        success: function(data, textStatus) {                    
                     if(trim(data)=="valid"){
                        form.submit();                        
                        return true;                                            
                    }  else {
                        CARBON.showErrorDialog(configjsi18n["invalid.conf"]);
                        return false;
                    }
                },       
        error: function(request, textStatus, err) {           
            CARBON.showWarningDialog(configjsi18n['error.occurred']); 
        }
    });

}

function submitWithForce(action, form) {
    var theString = editAreaLoader.getValue("rawConfig");
    var isXml = isValidXml(trim(theString));
    if (!isXml) {
        return false;
    }

    //set the hidden filed value to the new code created
    document.getElementById("rawConfig").value = editAreaLoader.getValue("rawConfig");
    form.action = 'saveconfig.jsp?force=true';
    form.submit();
    return true;
}

function isValidXml(docStr) {
    if (window.ActiveXObject) {
        try {
            var doc = new ActiveXObject("Microsoft.XMLDOM");
            doc.async = "false";
            var hasParse = doc.loadXML(docStr);
            if (!hasParse) {
                CARBON.showErrorDialog(configjsi18n["invalid.conf"]);
                return false;
            }
        } catch (e) {
            CARBON.showErrorDialog(configjsi18n["invalid.conf"]);
            return false;
        }
    } else {
        var parser = new DOMParser();
        var doc = parser.parseFromString(docStr, "text/xml");
        if (doc.documentElement.nodeName == "parsererror") {
            CARBON.showErrorDialog(configjsi18n["invalid.conf"]);
            return false;
        }
    }
    return true;
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

function resetConfiguration() {
    var url = 'reset-conf-ajaxprocessor.jsp';

    CARBON.showConfirmationDialog(
            "Are you sure you want to reset?", function() {
        jQuery.post(url, ({}),
                function(data, status) {
                    if (status != "success") {
                        CARBON.showWarningDialog(configjsi18n['error.occurred']);
                    } else {
                        data = data.substring(data.search(/<?xml/) - 2);
                        editAreaLoader.setValue("rawConfig", data);
                        YAHOO.util.Event.onAvailable('rawConfig', function() {
                                editAreaLoader.init({
                                    id : "rawConfig"		// textarea id
                                    ,syntax: "xml"			// syntax to be uses for highgliting
                                    ,start_highlight: true		// to display with highlight mode on start-up
                                });
                        });
                    }
                });
    });

    return false;
}

