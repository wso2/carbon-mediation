// input validator function
function mlMediatorValidate() {
    var modelName = document.getElementById('mediatorInputId').value;
    if(modelName == null || modelName==""){
        CARBON.showErrorDialog(classi18n["mediator.ml.validInputmsg"]);
        return false;
    }
    return true;
}

// delete a raw from a table
function deleteRow(src) {
    var index = src.parentNode.parentNode.rowIndex;
    var inputVariablesTable = document.getElementById('inputVariablesTable');
    inputVariablesTable.deleteRow(index);
    var noOfRows = inputVariablesTable.rows.length;
    if (noOfRows == 1) { // deleting the last raw, hide the table header
        document.getElementById('inputVariablesTable').style.display = 'none';
        document.getElementById('titleLabel').style.display = 'none';
    }
}
