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

function eventMediatorValidate(){

    var topic = document.getElementById("topicVal");
    if (topic && topic.value == "") {
            CARBON.showErrorDialog(eventi18n["specify.topic"]);
            return false;
    }

    return true;
}