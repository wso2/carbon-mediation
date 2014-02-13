
function loadViewUI(url, path) {
new Ajax.Updater('customViewUIDiv', url, { method: 'get', evalScripts:true, parameters: {path: path, random:getRandom()} });
}
function loadAddUI(url, path) {
new Ajax.Updater('customAddUIDiv', url, { method: 'get', evalScripts:true, parameters: {path: path, random:getRandom()} });
}
function showRegistryError(msg) {
CARBON.showErrorDialog(msg);
}
function showHideCommon(divId){
var theDiv = document.getElementById(divId);
if(theDiv.style.display=="none"){
theDiv.style.display="";
}else{
theDiv.style.display="none";
}
}
function expandIfNot(name)
{
if(document.getElementById(name+'Expanded').style.display=='none'){
showHideCommon(name+'IconExpanded');
showHideCommon(name+'IconMinimized');
showHideCommon(name+'Expanded');
showHideCommon(name+'Minimized');
}
}
function so_clearInnerHTML(obj) {
// perform a shallow clone on obj
nObj = obj.cloneNode(false);
// insert the cloned object into the DOM before the original one
obj.parentNode.insertBefore(nObj,obj);
// remove the original object
obj.parentNode.removeChild(obj);
}
function cleanField(fld){
fld.value="";
// fld.style.background="White";
}
function disableFields(idHideInput,idShowInput){
var showInput=document.getElementById(idHideInput);
var hideInput=document.getElementById(idShowInput);
hideInput.removeAttribute('disabled');
hideInput.setAttribute('disabled','disabled');
showInput.removeAttribute('disabled');
}
function handlePeerCheckbox(myID, peerID) {
//Only one is checked always, so it is safe to make the peer false
if (myID != peerID) {
var peer = document.getElementById(peerID);
peer.checked = false;
}
}
function set_cookie ( name, value, exp_y, exp_m, exp_d, path, domain, secure )
{
var cookie_string = name + "=" + escape ( value );
if ( exp_y )
{
var expires = new Date ( exp_y, exp_m, exp_d );
cookie_string += "; expires=" + expires.toGMTString();
}
if ( path )
cookie_string += "; path=" + escape ( path );
if ( domain )
cookie_string += "; domain=" + escape ( domain );
if ( secure )
cookie_string += "; secure";
document.cookie = cookie_string;
}
function get_cookie ( cookie_name )
{
var results = document.cookie.match ( '(^|;) ?' + cookie_name + '=([^;]*)(;|$)' );
if ( results )
return ( unescape ( results[2] ) );
else
return null;
}
function delete_cookie ( cookie_name )
{
var cookie_date = new Date ( ); // current date & time
cookie_date.setTime ( cookie_date.getTime() - 1 );
document.cookie = cookie_name += "=; expires=" + cookie_date.toGMTString();
}
function getRandom(){
return Math.floor(Math.random() * 2000);
}

