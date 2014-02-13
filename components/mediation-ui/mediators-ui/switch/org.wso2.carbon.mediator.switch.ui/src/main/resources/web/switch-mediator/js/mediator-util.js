function switchMediatorValidate() {
    var srcXpath = document.getElementById('sourceXPath').value;
    if(srcXpath == null || srcXpath==""){
        CARBON.showErrorDialog(switchi18n['must.specify.source.xpath']);
        return false;
    }
    return true;
}