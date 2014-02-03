    var blocked = false;
    var msg = "Leave the page?";
    var unloadhandler = function (e) {
        var e = e || window.event;
        if (e) {
            e.returnValue = msg;
        }
        return msg;
    };

    function askforConfirmation(e) {
        var answer = confirm(msg);
        if (answer) {
            window.onbeforeunload = function () {
            };
        }
        else {
            // Stop the href from being followed, even if it's "#"
            if (e != null) {
                YAHOO.util.Event.preventDefault(e);
            }
        }
    }
    YAHOO.util.Event.onDOMReady(function() {

        var workAreaInput = document.getElementById('workArea').getElementsByTagName("input");
        var workAreaTxtArea = document.getElementById('workArea').getElementsByTagName("textarea");


        //workAreaInput = workAreaInput.concat(workAreaTxtArea);

        for (var i = 0; i < workAreaInput.length; i++) {

            if (YAHOO.util.Dom.hasClass(workAreaInput[i],"button")) {
                YAHOO.util.Event.on(workAreaInput[i], "click", function(e) {
                    window.onbeforeunload = function () {
                    };
                });
            } else {
                YAHOO.util.Event.on(workAreaInput[i], "change", function(e) {
                    blockEverything();
                });
            }
        }
        for (i = 0; i < workAreaTxtArea.length; i++) {

            YAHOO.util.Event.on(workAreaTxtArea[i], "change", function(e) {
                blockEverything();
            });
        }
    });
    function blockEverything(){
        if(blocked){
            return;
        }
        blocked = true;
        var allLeftLinks = document.getElementById("menu-panel").getElementsByTagName("a");
        var pageHeaderLinks = document.getElementById("header-div").getElementsByTagName("a");
        var contentHeaderLinks = document.getElementById("page-header-links").getElementsByTagName("a");

        for(var i=0;i<allLeftLinks.length;i++){
            YAHOO.util.Event.on(allLeftLinks[i], "click", function(e) {
                askforConfirmation(e);
            });
        }
        for(var i=0;i<pageHeaderLinks.length;i++){
            YAHOO.util.Event.on(pageHeaderLinks[i], "click", function(e) {
                askforConfirmation(e);
            });
        }
        for(var i=0;i<contentHeaderLinks.length;i++){
            YAHOO.util.Event.on(contentHeaderLinks[i], "click", function(e) {
                askforConfirmation(e);
            });
        }
        window.onbeforeunload = unloadhandler;
    }