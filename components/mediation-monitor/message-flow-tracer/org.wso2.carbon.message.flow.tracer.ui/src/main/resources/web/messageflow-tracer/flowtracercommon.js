function flowDetails(messageID) {
    document.location.href = "messageflowdetails.jsp?" + "messageid=" + messageID;
}

function clearAllNew() {
    document.location.href = "index.jsp?op=clear";
}