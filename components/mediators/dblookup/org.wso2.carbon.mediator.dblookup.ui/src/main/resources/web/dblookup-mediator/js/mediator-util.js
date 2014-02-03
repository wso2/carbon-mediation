/*
 *  Copyright (c) 2008, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
 *
 *  Licensed under the Apache License, Version 2.0 (the "License");
 *  you may not use this file except in compliance with the License.
 *  You may obtain a copy of the License at
 *
 *        http://www.apache.org/licenses/LICENSE-2.0
 *
 *  Unless required by applicable law or agreed to in writing, software
 *  distributed under the License is distributed on an "AS IS" BASIS,
 *  WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *  See the License for the specific language governing permissions and
 *  limitations under the License.
 */

var options = new Array();
options[0] = "Select A Property";
options[1] = "autocommit";
options[2] = "isolation";
options[3] = "maxactive";
options[4] = "maxidle";
options[5] = "maxopenstatements";
options[6] = "maxwait";
options[7] = "minidle";
options[8] = "poolstatements";
options[9] = "testonborrow";
options[10] = "testwhileidle";
options[11] = "validationquery";
options[12] = "initialsize";

var javaTypes = new Array();
javaTypes[0] = 'CHAR';
javaTypes[1] = 'VARCHAR';
javaTypes[2] = 'NUMERIC';
javaTypes[3] = 'DECIMAL';
javaTypes[4] = 'BIT';
javaTypes[5] = 'TINYINT';
javaTypes[6] = 'SAMLLINT';
javaTypes[7] = 'INTEGER';
javaTypes[8] = 'BIGINT';
javaTypes[9] = 'REAL';
javaTypes[10] = 'DOUBLE';
javaTypes[11] = 'DATE';
javaTypes[12] = 'TIME';
javaTypes[13] = 'TIMESTAMP';

function showNSColumn(i, j) {
    var select = document.getElementById('parameterType' + i + '.' + j);
    if (select) {
        if (select.value == 'value') {
            dbLookup_displayElement('paramNS' + i + '.' + j, false);
        } else {
            dbLookup_displayElement('paramNS' + i + '.' + j, true);
        }
    }
}


var INPUT_NAME_PREFIX = 'inputName'; // this is being set via script
var RADIO_NAME = 'totallyrad'; // this is being set via script
var ROW_BASE = 1; // first number (for display)

function dbLookup_displayElement(elementId, isDisplay) {
    var toDisplayElement = document.getElementById(elementId);
    if (toDisplayElement != null) {
        if (isDisplay) {
            toDisplayElement.style.display = '';
        } else {
            toDisplayElement.style.display = 'none';
        }
    }
}

function poolOnClick() {
    dbLookup_displayElement('mediator.dbl.driver_row', true); 
    dbLookup_displayElement('mediator.dbl.inictx_row', false);
    dbLookup_displayElement('mediator.dbl.ds_row', false);
    dbLookup_displayElement('mediator.dbl.url', true);
    dbLookup_displayElement('mediator.dbl.user', true);
    dbLookup_displayElement('mediator.dbl.passwd', true);
    dbLookup_displayElement('sourceGroup', false);
    dbLookup_displayElement('dataSourceSelect', false);
    dbLookup_displayElement('dsProps', true);
    dbLookup_displayElement('addProp', true);
}

function sourceOnClick() {
    var exist = document.getElementById('sourceTypeExisting');
    dbLookup_displayElement('sourceGroup', true);
    dbLookup_displayElement('dataSourceSelect', true);
    if (exist && exist.checked) {
        existingOnClick();
    } else {
        inlineOnClick();
    }
}

function inlineOnClick() {
    dbLookup_displayElement('mediator.dbl.driver_row', false);
    dbLookup_displayElement('mediator.dbl.inictx_row', true);
    dbLookup_displayElement('mediator.dbl.ds_row', true);
    dbLookup_displayElement('mediator.dbl.url', true);
    dbLookup_displayElement('mediator.dbl.user', true);
    dbLookup_displayElement('mediator.dbl.passwd', true);
    dbLookup_displayElement('dataSourceSelect', false);
    dbLookup_displayElement('dsProps', true);
    dbLookup_displayElement('addProp', true);
}

function existingOnClick() {
    dbLookup_displayElement('mediator.dbl.driver_row', false);
    dbLookup_displayElement('mediator.dbl.inictx_row', false);
    dbLookup_displayElement('mediator.dbl.ds_row', false);
    dbLookup_displayElement('mediator.dbl.url', false);
    dbLookup_displayElement('mediator.dbl.user', false);
    dbLookup_displayElement('mediator.dbl.passwd', false);    
    dbLookup_displayElement('dataSourceSelect', true);
    dbLookup_displayElement('dsProps', false);
    dbLookup_displayElement('addProp', false);
}
function dblookupOptionChange(row) {
    var property = document.getElementById("property" + row);
    var prop_val;
    if (!property || !property.value) {
        return;
    }

    prop_val = property.value;
    /* remove what we already have */
    var remove = document.getElementById("property_value" + row);
    var td;
    if (remove) {
        td = remove.parentNode;
        if (!td) {
            return;
        }
        td.removeChild(remove);
    }
    /* create select */
    var select = document.createElement("select");
    select.setAttribute("name", "property_value" + row);
    select.setAttribute("id", "property_value" + row);
    select.style.width = "300px";
    var option;
    /* create the text */
    var txtInp = document.createElement('input');
    txtInp.setAttribute('name', "property_value" + row);
    txtInp.setAttribute('id', "property_value" + row);
    txtInp.setAttribute('type', 'text');
    txtInp.setAttribute('value', "");
    txtInp.style.width = "300px";
    txtInp.onclick = function() {propertyValueOnclick(row)};
    if (prop_val == "autocommit" || prop_val == "testonborrow" || prop_val == "testwhileidle" || prop_val == "poolstatements") {
        option = document.createElement("option");
        option.setAttribute("value", "true");
        option.innerHTML = "true";
        select.appendChild(option);

        option = document.createElement("option");
        option.setAttribute("value", "false");
        option.innerHTML = "false";
        select.appendChild(option);

        td.appendChild(select);
    } else if (prop_val == "isolation") {
        option = document.createElement("option");
        option.setAttribute("value", "Connection.TRANSACTION_NONE");
        option.innerHTML = "Connection.TRANSACTION_NONE";
        select.appendChild(option);

        option = document.createElement("option");
        option.setAttribute("value", "Connection.TRANSACTION_READ_COMMITTED");
        option.innerHTML = "Connection.TRANSACTION_READ_COMMITTED";
        select.appendChild(option);

        option = document.createElement("option");
        option.setAttribute("value", "Connection.TRANSACTION_READ_UNCOMMITTED");
        option.innerHTML = "Connection.TRANSACTION_READ_UNCOMMITTED";
        select.appendChild(option);

        option = document.createElement("option");
        option.setAttribute("value", "Connection.TRANSACTION_REPEATABLE_READ");
        option.innerHTML = "Connection.TRANSACTION_REPEATABLE_READ";
        select.appendChild(option);

        option = document.createElement("option");
        option.setAttribute("value", "Connection.TRANSACTION_SERIALIZABLE");
        option.innerHTML = "Connection.TRANSACTION_SERIALIZABLE";
        select.appendChild(option);

        td.appendChild(select);
    } else if (prop_val == "maxactive" || prop_val == "maxidle" || prop_val == "maxopenstatements" || prop_val == "maxwait" || prop_val == "minidle" || prop_val == "initialsize")  {
        td.appendChild(txtInp);
        txtInp.value = "( int )";
    } else if (prop_val == "validationquery") {
        td.appendChild(txtInp);
        txtInp.value = "( string )";
    } else {
        td.appendChild(txtInp);
    }
}

function propertyValueOnclick(row) {
    var remove = document.getElementById("property_value" + row);
    if (remove && remove.value) {
        if (remove.value == "( int )" || remove.value == "( string )") {
            remove.value = "";
        }
    }
    return false;
}

function dblookupMediatorValidate() {
    var field;
    var connectionGroup = document.getElementById('radio_pool');
    var isExisiting = false;
    if (connectionGroup && !connectionGroup.checked) {
        var existing = document.getElementById('sourceTypeExisting');
        if (existing && existing.checked) {
            isExisiting = true;
        }
    }
    if (!isExisiting) {
        if (connectionGroup && connectionGroup.checked) {
            field = document.getElementById('driver');
            if (field && field.value == "") {
                CARBON.showErrorDialog(dblookMediatorJsi18n["dblookup.driver.required"]);
                return false;
            }
        } else {
            field = document.getElementById('init_ctx');
            if (field && field.value == "") {
                CARBON.showErrorDialog(dblookMediatorJsi18n["dblookup.initCtx.required"]);
                return false;
            }

            field = document.getElementById('ext_data_source');
            if (field && field.value == "") {
                CARBON.showErrorDialog(dblookMediatorJsi18n["dblookup.dataSource.required"]);
                return false;
            }
        }
        field = document.getElementById('url');
        if (field && field.value == "") {
            CARBON.showErrorDialog(dblookMediatorJsi18n["dblookup.url.required"]);
            return false;
        }
        field = document.getElementById('user');
        if (field && field.value == "") {
            CARBON.showErrorDialog(dblookMediatorJsi18n["dblookup.user.required"]);
            return false;
        }
        field = document.getElementById('password');
        if (field && field.value == "") {
            CARBON.showErrorDialog(dblookMediatorJsi18n["dblookup.password.required"]);
            return false;
        }
        return validateProperties() && validateStmts();
    } else {
        field = document.getElementById('data_source');
        if (field && field.value == "") {
            CARBON.showErrorDialog(dblookMediatorJsi18n["dblookup.dataSource.required"]);
            return false;
        }
    }
    return validateStmts();
}

function validateProperties() {
    var field = document.getElementById("hidden_property");
    if (field && field.value != "") {
        var val = parseInt(field.value);
        for (var i = 1; i < val; i++) {
            var select = document.getElementById("property" + i);
            if (select) {
                var selectVal = select.value;
                if (selectVal == "Select A Property") {
                    CARBON.showErrorDialog(dblookMediatorJsi18n["dblookup.selectProperty.invalid"]);
                    return false;
                }
                var expresion = document.getElementById("property_value" + i);
                if (expresion && expresion.value == "") {
                    CARBON.showErrorDialog(dblookMediatorJsi18n["dblookup.property.empty"] + " " + selectVal + dblookMediatorJsi18n["dblookup.property.empty.specify"]);
                    return false;
                }
                if (selectVal == "maxactive" || selectVal == "maxidle" || selectVal == "maxopenstatements" || selectVal == "maxwait" || selectVal == "minidle") {
                    if (isNaN(expresion.value)) {
                        CARBON.showErrorDialog(dblookMediatorJsi18n["dblookup.property.number.expected"] + " " + select.value);
                        return false;
                    }
                }
            }
        }
    }
    return true;
}

function validateStmts() {
    var params, j;
    var field = document.getElementById("hidden_stmt");
    if (field && field.value != "") {
        var val = parseInt(field.value);
        for (var i = 1; i < val; i++) {
            /* validate SQL statement */
            field = document.getElementById("sql_val" + i);
            if (field && field.value == "") {
                CARBON.showErrorDialog(dblookMediatorJsi18n["dblookup.sql.required"]);
                return false;
            }
            /* validate Parameters */
            field = document.getElementById("hidden_parameters" + i);
            if (field && field.value != "") {
                params = parseInt(field.value);
                for (j = 1; j < params; j++) {
                    field = document.getElementById("parameter_value" + i + "." + j);
                    if (field && field.value == "") {
                        CARBON.showErrorDialog(dblookMediatorJsi18n["dblookup.parameter.empty"]);
                        return false;
                    }
                }
            }
            /* validate results */
            field = document.getElementById("hidden_results" + i);
            if (field && field.value != "") {
                params = parseInt(field.value);
                for (j = 1; j < params; j++) {
                    field = document.getElementById("property_name" + i + "." + j);
                    if (field && field.value == "") {
                        CARBON.showErrorDialog(dblookMediatorJsi18n["dblookup.result.name.empty"]);
                        return false;
                    }

                    field = document.getElementById("property_value" + i + "." + j);
                    if (field && field.value == "") {
                        CARBON.showErrorDialog(dblookMediatorJsi18n["dblookup.result.column.empty"]);
                        return false;
                    }
                }
            }
        }
    }
    return true;
}

function dblDeleteStatement(divId) {
    CARBON.showConfirmationDialog(dblookMediatorJsi18n['dblookup.delete.confirm'], function() {
        var d = document.getElementById('queries');
        var removeDiv = document.getElementById(divId);
        d.removeChild(removeDiv);
    });
}

function addRowToTable(tableName, num) {
    var tbl = document.getElementById(tableName);
    var nextRow = tbl.tBodies[0].rows.length;
    var iteration = 0;
    var hidden = document.getElementById("hidden_property");
    var val = hidden.value;
    if (val != null && val != "") {
        iteration = parseInt(val);
    }

    if (num == null) {
        num = nextRow;
    } else {
        iteration = num + ROW_BASE;
    }

    if (tbl.tBodies[0].rows.length == 0) {
        tbl.style.display = '';
    }

    var row = tbl.tBodies[0].insertRow(num);

    var cell0 = row.insertCell(0);
    var selectNode = document.createElement('select');
    selectNode.onchange = function(){dblookupOptionChange(iteration);};
    selectNode.setAttribute("name", "property" + iteration);
    selectNode.setAttribute("id", "property" + iteration);
    for (var i = 0; i < options.length; i++) {
        var optionNode = document.createElement("option");
        optionNode.setAttribute("value", options[i]);
        optionNode.innerHTML = options[i];
        if (i == 0) {
            optionNode.setAttribute("selected", "selected");
        }
        selectNode.appendChild(optionNode);
    }
    cell0.appendChild(selectNode);

    var cell1 = row.insertCell(1);
    var txtInp = document.createElement('input');
    txtInp.setAttribute('type', 'text');
    txtInp.setAttribute('name', "property_value" + iteration);
    txtInp.setAttribute('id', "property_value" + iteration);
    txtInp.setAttribute('value', "");
    txtInp.style.width = "300px";
    cell1.appendChild(txtInp);

    var cell2 = row.insertCell(2);
    cell2.innerHTML = "<a href='#' onclick=\"javascript: deleteCurrentRow(this);return false;\" class=\"delete-icon-link\">" + dblookMediatorJsi18n["dblookup.delete"] + "</a>";
    hidden.setAttribute("value", iteration + 1);
}

function deleteCurrentRow(obj)
{
    var delRow = obj.parentNode.parentNode;
    var tbl = delRow.parentNode.parentNode;
    var rIndex = delRow.sectionRowIndex;
    var rowArray = new Array(delRow);
    deleteRows(rowArray);
    if (tbl.tBodies[0].rows.length == 0) {
        tbl.style.display = 'none';
    }
}

function deleteRows(rowObjArray)
{
    for (var i = 0; i < rowObjArray.length; i++) {
        var rIndex = rowObjArray[i].sectionRowIndex;
        rowObjArray[i].parentNode.deleteRow(rIndex);
    }
}

function addStatement()
{
    var div = document.createElement("div");
    var no = 1;
    var hidden = document.getElementById("hidden_stmt");
    var val = hidden.value;
    if (val != null && val != "") {
        no = parseInt(val);
    }
    div.className = "tabedBox";
    div.setAttribute("id", "statement" + no);
    div.innerHTML = "<table class=\"normal\">" +
        "<tbody>" +
            "<tr>" +
                "<td style=\"width:80px\">"+ dblookMediatorJsi18n["dblookup.sql"] +"<font style=\"color: red; font-size: 8pt;\">*</font></td>" +
                "<td style=\"width:305px\"><input style=\"width:300px\" type=\"text\" name=\"sql_val" + no + "\" id=\"sql_val" + no + "\" class=\"longTextField\" value=\"\"/></td>" +
                "<td>" +
                    "<a href=\"#\" class=\"delete-icon-link\" onclick=\"javascript:dblDeleteStatement('statement" + no + "');\">" + dblookMediatorJsi18n["dblookup.delete"] + "</a>" +
                "</td>" +
            "</tr>" +
        "</tbody>" +
    "</table>" +          
    "<h3 class=\"mediator\">" + dblookMediatorJsi18n["dblookup.parameters"] +"</h3>" +
    "<div class=\"rowAlone\"> " +
          "<a href=\"#addNameLink\"" +
          "onclick=\"addStmtParamTableRow('mediator.dbl.tp" + no + "', null," + no + ");\"" +
          "class=\"add-icon-link\">"+ dblookMediatorJsi18n["dblookup.addParam"] + "</a>" +
    "</div>" +
    "<br/>" +
    "<br/>" +
    "<table id=\"mediator.dbl.tp"+ no + "\" class=\"styledInner\" style=\"display:none\">" +
        "<thead>" +
            "<tr>" +
               "<th style=\"width:150px\">" + dblookMediatorJsi18n["dblookup.param"] +"</th>" +
               "<th style=\"width:100px\">" + dblookMediatorJsi18n["dblookup.propType"] +"</th>" +
               "<th style=\"width:320px\">" + dblookMediatorJsi18n["dblookup.valExp"] +"</th>" +
               "<th style=\"width:150px\">" + dblookMediatorJsi18n["dblookup.namespace"] +"</th>" +
               "<th>Action</th>" +
            "</tr>" +
        "</thead>" +
        "<tbody></tbody>" +
    "</table>" +
    "<input type=\"hidden\" name=\"hidden_parameters"+ no +"\" id=\"hidden_parameters"+ no +"\" value=\"1\">" +
    "<br />" +
    "<h3 class=\"mediator\">" + dblookMediatorJsi18n["dblookup.results"] +"</h3> " +
    "<div class=\"rowAlone\">" +                
        "<a href=\"#addNameLink\"" +
          "onclick=\"addStmtResultsTableRow('mediator.dbl.tr" + no + "', null," + no + ");\"" +
          "class=\"add-icon-link\">"+ dblookMediatorJsi18n["dblookup.addResult"] + "</a>" +
    "</div>" +
    "<br/>" +
    "<br/>" +                 
    "<table id=\"mediator.dbl.tr" + no + "\" class=\"styledInner\" style=\"display:none\"> " +
        "<thead>" +
            "<tr>" +
               "<th style=\"width:150px\">" + dblookMediatorJsi18n["dblookup.resultName"] +"</th>" +
               "<th style=\"width:320px\">" + dblookMediatorJsi18n["dblookup.column"] +"</th>" +
               "<th>Action</th>" +
            "</tr>" +
        "</thead>" +
        "<tbody></tbody>" +
    "</table>" +
    "<input type=\"hidden\" name=\"hidden_results"+ no +"\" id=\"hidden_results"+ no +"\" value=\"1\">";
    hidden.setAttribute("value", no + 1);
    var querry = document.getElementById("queries");
    querry.appendChild(div);
}

function addStmtParamTableRow(tableName, num, stmt)
{
    var tbl = document.getElementById(tableName);
    var nextRow = tbl.tBodies[0].rows.length;
    var optionNode;
    var noTwo = 0;
    var hidden = document.getElementById("hidden_parameters" + stmt);
    var val = hidden.value;
    if (val != null && val != "") {
        noTwo = parseInt(val);
    }

    if (num == null) {
        num = nextRow;
    }

    if (tbl.tBodies[0].rows.length == 0) {
        tbl.style.display = '';
    }
    var row = tbl.tBodies[0].insertRow(num);

    var cell0 = row.insertCell(0);
    var selectNode = document.createElement('select');
    selectNode.setAttribute("name", "javaType" + stmt + "." + noTwo);
    for (var i = 0; i < javaTypes.length; i++) {
        optionNode = document.createElement("option");
        optionNode.setAttribute("value", javaTypes[i]);
        optionNode.innerHTML = javaTypes[i];
        if (i == 0) {
            optionNode.setAttribute("selected", "selected");
        }
        selectNode.appendChild(optionNode);
    }
    cell0.appendChild(selectNode);

    var cell1 = row.insertCell(1);
    selectNode = document.createElement('select');
    selectNode.setAttribute("name", "parameterType" + stmt + "." + noTwo);
    selectNode.setAttribute("id", "parameterType" + stmt + "." + noTwo);
    optionNode = document.createElement("option");
    optionNode.value = 'expression';
    optionNode.innerHTML = 'Expression';
    selectNode.appendChild(optionNode);

    optionNode = document.createElement("option");
    optionNode.value = 'value';
    optionNode.innerHTML = 'Value';
    selectNode.appendChild(optionNode);
    cell1.appendChild(selectNode);
    selectNode.onchange = function() {showNSColumn(stmt, noTwo);};

    var cell2 = row.insertCell(2);
    var txtInp = document.createElement('input');
    txtInp.setAttribute('type', 'text');
    txtInp.setAttribute('name', ("parameter_value" + stmt + "." + noTwo));
    txtInp.setAttribute('id', ("parameter_value" + stmt + "." + noTwo));
    txtInp.setAttribute('value', "");
    txtInp.style.width = "300px";
    cell2.appendChild(txtInp);

    var cell3 = row.insertCell(3);
    cell3.innerHTML = "<a id=\"paramNS" + stmt + "." + noTwo + "\" onclick=\"javascript: showNameSpaceEditor('parameter_value"+ stmt + "." + noTwo +"');return false;\" " +
                           "class=\"nseditor-icon-link\" style=\"padding-left:40px\" href=\"#\">"+ dblookMediatorJsi18n["dblookup.nmsp"] + "</a>";

    var cell4 = row.insertCell(4);
    cell4.innerHTML = "<a onclick=\"javascript: deleteCurrentRow(this);return false;\" href=\"#\" class=\"delete-icon-link\">"+ dblookMediatorJsi18n["dblookup.delete"] + "</a>";
    hidden.setAttribute("value", noTwo + 1);
}

function addStmtResultsTableRow(tableName, num, stmt)
{
    var tbl = document.getElementById(tableName);
    var nextRow = tbl.tBodies[0].rows.length;
    var noTwo = 0;
    var hidden = document.getElementById(("hidden_results" + stmt));
    var val = hidden.value;
    if (val != null && val != "") {
        noTwo = parseInt(val);
    }
    if (tbl.tBodies[0].rows.length == 0) {
        tbl.style.display = '';
    }

    if (num == null) {
        num = nextRow;
    }

    var row = tbl.tBodies[0].insertRow(num);

    var cell0 = row.insertCell(0);
    var txtInp = document.createElement('input');
    txtInp.setAttribute('type', 'text');
    txtInp.setAttribute('name', ('property_name' + stmt+ '.' + noTwo));
    txtInp.setAttribute('id', ('property_name' + stmt+ '.' + noTwo));
    txtInp.setAttribute('value', "");
    txtInp.style.width = "120px";
    cell0.appendChild(txtInp);

    var cell1 = row.insertCell(1);
    txtInp = document.createElement('input');
    txtInp.setAttribute('type', 'text');
    txtInp.setAttribute('value', "");
    txtInp.setAttribute('name', ('property_value' + stmt + '.' + noTwo));
    txtInp.setAttribute('id', ('property_value' + stmt + '.' + noTwo));
    txtInp.style.width = "300px";
    cell1.appendChild(txtInp);

    var cell2 = row.insertCell(2);
    cell2.innerHTML = "<a onclick=\"javascript: deleteCurrentRow(this);return false;\" href=\"#\" class=\"delete-icon-link\">"+ dblookMediatorJsi18n["dblookup.delete"] + "</a>";
    hidden.setAttribute("value", (noTwo + 1))
}
