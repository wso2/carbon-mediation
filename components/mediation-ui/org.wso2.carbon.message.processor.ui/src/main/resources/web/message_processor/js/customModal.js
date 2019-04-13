/*
*  Copyright (c) 2019, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
*
*  WSO2 Inc. licenses this file to you under the Apache License,
*  Version 2.0 (the "License"); you may not use this file except
*  in compliance with the License.
*  You may obtain a copy of the License at
*
*    http://www.apache.org/licenses/LICENSE-2.0
*
* Unless required by applicable law or agreed to in writing,
* software distributed under the License is distributed on an
* "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
* KIND, either express or implied.  See the License for the
* specific language governing permissions and limitations
* under the License.
*/

if( typeof CARBON ==="undefined" || CARBON) {
    var CARBON = {};
}

var pageLoaded = false;

/**
 * Encode html value using jQuery.
 * @method htmlEncode
 * @param {String} value html value to be encoded
 */
function htmlEncode(value){
  return jQuery('<div/>').text(value).html();
}

jQuery(document).ready(function() {
    pageLoaded = true;
});

CARBON.showConfirmationDialog = function(message, handleYes, handleNo, closeCallback){
    /* This function always assume that your second parameter is handleYes function and third parameter is handleNo function.
     * If you are not going to provide handleYes function and want to give handleNo callback please pass null as the second
     * parameter.
     */
    var strDialog = "<div id='dialog' title='WSO2 Carbon'><div id='messagebox-confirm'><p>" +
                    htmlEncode(message) + "</p></div></div>";

    handleYes = handleYes || function(){return true;};

    handleNo = handleNo || function(){return false;};
    var func = function() {
	    jQuery("#dcontainer").html(strDialog);

	    jQuery("#dialog").dialog({
	        close:function() {
	            jQuery(this).dialog('destroy').remove();
	            jQuery("#dcontainer").empty();
	            if (closeCallback && typeof closeCallback === "function") {
	                closeCallback();
	            }
	            return false;
	        },
	        buttons:{
	            "Yes":function() {
	                jQuery(this).dialog("destroy").remove();
	                jQuery("#dcontainer").empty();
	                handleYes();
	            },
	            "No":function(){
	                jQuery(this).dialog("destroy").remove();
	                jQuery("#dcontainer").empty();
	                handleNo();
	            }
	        },
	        height:160,
	        width:450,
	        minHeight:160,
	        minWidth:330,
	        modal:true
	    });
    };
    if (!pageLoaded) {
        jQuery(document).ready(func);
    } else {
        func();
    }
    return false;
};

CARBON.showCustomModal = function( message, title, windowHeight, okButton, callback, windowWidth) {
    var strDialog = "<div id='dialog' title='" + title + "'><div id='popupDialog'></div>" + htmlEncode(message) + "</div>";
    var requiredWidth = 750;
    if (windowWidth) {
        requiredWidth = windowWidth;
    }
    var func = function() { 
    jQuery("#dcontainer").html(strDialog);

    if (okButton) {
        jQuery("#dialog").dialog({
            close:function() {
                jQuery(this).dialog('destroy').remove();
                jQuery("#dcontainer").empty();
                return false;
            },
            buttons:{
                "Redirect":function() {
                    if (callback && typeof callback === "function")
                        callback();
                    jQuery(this).dialog("destroy").remove();
                    jQuery("#dcontainer").empty();
                    return false;
                },

                "Cancel":function() {
                    jQuery(this).dialog("destroy").remove();
                    jQuery("#dcontainer").empty();
                    //handleCancel();
                }
            },
            height:windowHeight,
            width:requiredWidth,
            minHeight:windowHeight,
            minWidth:requiredWidth,
            modal:true
        });
    } else {
        jQuery("#dialog").dialog({
            close:function() {
                jQuery(this).dialog('destroy').remove();
                jQuery("#dcontainer").empty();
                return false;
            },
            height:windowHeight,
            width:requiredWidth,
            minHeight:windowHeight,
            minWidth:requiredWidth,
            modal:true
        });
    }
	
	jQuery('.ui-dialog-titlebar-close').click(function(){
				jQuery('#dialog').dialog("destroy").remove();
                jQuery("#dcontainer").empty();
				jQuery("#dcontainer").html('');
		});
	
    };
    if (!pageLoaded) {
        jQuery(document).ready(func);
    } else {
        func();
    }
};
