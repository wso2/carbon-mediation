function fastXSLTMediatorValidate() {

    var key;
    var keyGroup = document.getElementById("keyGroupDynamic");
    if (keyGroup != null && keyGroup.checked) {
        key = document.getElementById("mediator.fastXSLT.key.dynamic_val");
    } else {
        key = document.getElementById("mediator.fastXSLT.key.static_val");
    }
    if (key && key.value == "") {
        CARBON.showErrorDialog(fastXSLTjsi18n["mediator.fastXSLT.script.key.empty"]);
        return false;
    }
    return true;
}

function displayElement(elementId, isDisplay) {
    var toDisplayElement = document.getElementById(elementId);
    if (toDisplayElement != null) {
        if (isDisplay) {
            toDisplayElement.style.display = '';
        } else {
            toDisplayElement.style.display = 'none';
        }
    }
}










