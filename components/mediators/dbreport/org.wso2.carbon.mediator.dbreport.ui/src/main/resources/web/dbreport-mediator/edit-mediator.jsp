<%@ page language="java" contentType="text/html; charset=ISO-8859-1" pageEncoding="ISO-8859-1"%>
<%--
  ~  Copyright (c) 2008, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
  ~
  ~  Licensed under the Apache License, Version 2.0 (the "License");
  ~  you may not use this file except in compliance with the License.
  ~  You may obtain a copy of the License at
  ~
  ~        http://www.apache.org/licenses/LICENSE-2.0
  ~
  ~  Unless required by applicable law or agreed to in writing, software
  ~  distributed under the License is distributed on an "AS IS" BASIS,
  ~  WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
  ~  See the License for the specific language governing permissions and
  ~  limitations under the License.
  --%>

<%@ page import="org.apache.synapse.SynapseConstants" %>
<%@ page import="org.wso2.carbon.mediator.service.ui.Mediator" %>
<%@ page import="org.wso2.carbon.mediator.dbreport.DBReportMediator" %>
<%@ page import="org.wso2.carbon.mediator.dbreport.Statement" %>
<%@ page import="org.wso2.carbon.sequences.ui.util.SequenceEditorHelper" %>
<%@ page import="org.wso2.carbon.sequences.ui.util.ns.NameSpacesRegistrar" %>
<%@ page import="javax.xml.namespace.QName" %>
<%@ page import="java.sql.Types" %>
<%@ page import="java.util.HashMap" %>
<%@ page import="java.util.Iterator" %>
<%@ page import="java.util.Set" %>
<%@ page import="java.util.List" %>
<%@ page import="org.wso2.carbon.mediator.dbreport.DBReportMediatorClient" %>
<%@page import="org.wso2.carbon.ui.CarbonUIMessage"%>
<%@ taglib uri="http://java.sun.com/jsp/jstl/fmt" prefix="fmt"%>
<%@ taglib uri="http://wso2.org/projects/carbon/taglibs/carbontags.jar" prefix="carbon" %>

<%!
    boolean qnameEquals(QName qname, String name) {
        return qname.equals(new QName(SynapseConstants.SYNAPSE_NAMESPACE, name));
    }
%>

<%
    String user = "", url = "", driver = "", dsName = "", initCtx = "", passwd = "";
    boolean isPool = true;
    boolean isInline = false;
    boolean displayCommonProps = false;
    boolean displayExisistingDs = false;

    HashMap dsProps, otherProps = null;
    Set otherPropSet = null;
    Iterator otherPropIt = null;
    NameSpacesRegistrar nmspRegistrar = NameSpacesRegistrar.getInstance();

    Mediator mediator = SequenceEditorHelper.getEditingMediator(request, session);
    if (!(mediator instanceof DBReportMediator)) {
        // todo : proper error handling
        throw new RuntimeException("Unable to edit the mediator");
    }
    DBReportMediator dbReportMediator = (DBReportMediator) mediator;

    otherProps = new HashMap();

    dsProps = (HashMap) dbReportMediator.getDataSourceProps();
    if (dsProps != null) {
        Iterator it = dsProps.keySet().iterator();
        while (it.hasNext()) {
            Object o = it.next();
            if (!(o instanceof QName)) {
                otherProps.put(o, dsProps.get(o));
                continue;
            }
            QName key = (QName) o;
            if (key.equals(new QName(SynapseConstants.SYNAPSE_NAMESPACE, "url"))) {
                url = (String) dsProps.get(key);
                isInline = true;
            } else if (key.equals(new QName(SynapseConstants.SYNAPSE_NAMESPACE, "password"))) {
                passwd = (String) dsProps.get(key);
                isInline = true;
            } else if (key.equals(new QName(SynapseConstants.SYNAPSE_NAMESPACE, "user"))) {
                user = (String) dsProps.get(key);
                isInline = true;
            } else if (key.equals(new QName(SynapseConstants.SYNAPSE_NAMESPACE, "driver"))) {
                driver = (String) dsProps.get(key);
                isInline = true;
            } else if (key.equals(new QName(SynapseConstants.SYNAPSE_NAMESPACE, "dsName"))) {
                dsName = (String) dsProps.get(key);
                isPool = false;
            } else if (key.equals(new QName(SynapseConstants.SYNAPSE_NAMESPACE, "icClass"))) {
                initCtx = (String) dsProps.get(key);
                isInline = true;
            } 
        }
    }
    if (!isPool && !isInline) displayExisistingDs = true;
    if (isInline || isPool) displayCommonProps = true;

    otherPropSet = otherProps.keySet();
    otherPropIt = otherPropSet.iterator();


DBReportMediatorClient client=null;
List<String> sourceList =null;

try{
client = DBReportMediatorClient.getInstance(config, session);
sourceList = client.getAllDataSourceInformations();
}catch(Exception e){
response.setStatus(500);
CarbonUIMessage uiMsg = new CarbonUIMessage(CarbonUIMessage.ERROR, e.getMessage(), e);
session.setAttribute(CarbonUIMessage.ID, uiMsg);
%>
<jsp:include page="../admin/error.jsp?<%=e.getMessage()%>"/>
<%
    return;
    }
%>


<fmt:bundle basename="org.wso2.carbon.mediator.dbreport.ui.i18n.Resources">
<carbon:jsi18n
        resourceBundle="org.wso2.carbon.mediator.dbreport.ui.i18n.JSResources"
        request="<%=request%>" i18nObjectName="dbreportMediatorJsi18n"/>    
<div id="dbreport_edit">
<script type="text/javascript" src="../dbreport-mediator/js/mediator-util.js"></script>
<style>
    .icon-link-dbreport{
        background-image: url(../dblookup-mediator/images/data-sources-icon.gif) !important;
    }
</style>
<table class="normal" width="100%">
    <tr><td>
        <h2><fmt:message key="mediator.dbreport.header"/></h2>
    </td></tr>
    <tr><td>
    <table class="normal">
        <tbody>
            <tr>
                <td style="width:150px"><fmt:message key="mediator.dbreport.useTransaction"/></td>
                <td><input type="radio" id="transaction_true" name="transactiongroup" value="true"
                        <%=dbReportMediator.isUseTransaction() ? "checked=\"checked\"" : ""%>/>
                    <label><fmt:message key="mediator.dbreport.useTransaction.true"/></label>
                    <input type="radio" id="transaction_false"  name="transactiongroup" value="false"
                        <%=!dbReportMediator.isUseTransaction()  ? "checked=\"checked\"" : ""%>/>
                    <label><fmt:message key="mediator.dbreport.useTransaction.false"/></label>
                </td>
            </tr>
            <tr>
                <td style="width:150px"><fmt:message key="mediator.dbreport.connInfo"/></td>
                <td><input type="radio" id="radio_pool" onclick="poolOnClick()"
                           name="connectiongroup" value="poolgroup"
                        <%=isPool ? "checked=\"checked\"" : ""%>/>
                    <label><fmt:message key="mediator.dbreport.pool"/></label>
                    <input type="radio" id="radio_datasource" onclick="sourceOnClick()"
                           name="connectiongroup" value="datasourceprop" <%=!isPool ? "checked=\"checked\"" : ""%>/>
                    <label><fmt:message key="mediator.dbreport.dataSource"/></label>
                </td>
            </tr>
            <tr id="sourceGroup" <%=isPool ? "style=\"display:none\";" : ""%>>
                <td style="width:150px"><fmt:message key="mediator.dbreport.source.type"/></td>
                <td><input type="radio" id="sourceTypeInline"
                           onclick="inlineOnClick()" value="inline"
                           name="sourceType"
                        <%=isInline ? "checked=\"checked\"" : ""%>/>
                    <label><fmt:message key="mediator.dbreport.source.inline"/></label>
                    <input type="radio" id="sourceTypeExisting"
                           onclick="existingOnClick()" value="existing"
                           name="sourceType" <%=!isInline ? "checked=\"checked\"" : ""%>/>
                    <label><fmt:message key="mediator.dbreport.source.exist"/></label>
                </td>
            </tr>

            <tr id="dataSourceSelect" <%=!displayExisistingDs ? "style=\"display:none\";" : ""%>>
                <td style="width:150px"><fmt:message key="mediator.dbreport.dsName"/></td>
                <td>
                    <table>
                        <tbody>
                        <tr>
                            <td>
                                <select name="data_source" id="data_source">
                                    <option value="" selected="selected">--SELECT--</option>
                                    <%
                                        if (sourceList != null) {
                                    %>
                                    <%
                                        for (String name : sourceList) {
                                    %>
                                    <%
                                        if (name.equals(dsName)) {
                                    %>
                                    <option  value=<%=name%> selected><%=name%> </option>
                                    <%
                                    } else {
                                    %>
                                    <option  value=<%=name%>><%=name%> </option>
                                    <%
                                        }
                                    %>
                                    <%
                                        }
                                    %>

                                    <%
                                    } else {
                                    %>
                                    <font color="red"><fmt:message key="empty.source"/></font>
                                    <%
                                        }
                                    %>
                                </select>

                            </td>
                        </tr>
                        </tbody>
                    </table>
                </td>
            </tr>

            <tr id="mediator.dbr.driver_row" <%=!isPool ? "style=\"display:none;\"" : ""%>>
                <td style="width:150px">
                    <fmt:message key="mediator.dbreport.driver"/>
                    <font style="color: red; font-size: 8pt;"> *</font>
                </td>
                <td><input style="width:300px" type="text" name="driver" id="driver" class="longTextField" value="<%=driver%>"/></td>
            </tr>
            <tr id="mediator.dbr.inictx_row" <%=!isPool && isInline ? "" : "style=\"display:none;\""%>>
                <td style="width:150px">
                    <fmt:message key="mediator.dbreport.contex"/>
                </td>
                <td><input type="text" style="width:300px" name="init_ctx" id="init_ctx" value="<%=initCtx%>"/></td>
            </tr>
            <tr id="mediator.dbr.ds_row" <%=!isPool && isInline ? "" : "style=\"display:none;\""%>>
                <td>
                    <fmt:message key="mediator.dbreport.dsName"/>
                    <font style="color: red; font-size: 8pt;"> *</font>
                </td>
                <td><input type="text" style="width:300px" name="ext_data_source" id="ext_data_source" value="<%=dsName%>"/></td>
            </tr>
            <tr id='mediator.dbr.url' <%=!displayCommonProps ? "style=\"display:none;\"" : ""%>>
                <td>
                    <fmt:message key="mediator.dbreport.url"/>
                    <font style="color: red; font-size: 8pt;"> *</font>
                </td>
                <td><input type="text" style="width:300px" name="url" id="url" value="<%=url%>"/></td>
            </tr>
            <tr id="mediator.dbr.user" <%=!displayCommonProps ? "style=\"display:none;\"" : ""%>>
                <td>
                    <fmt:message key="mediator.dbreport.user"/>
                    <font style="color: red; font-size: 8pt;"> *</font>
                </td>
                <td><input type="text" style="width:300px"  name="user" id="user" value="<%=user%>"/></td>
            </tr>
            <tr id="mediator.dbr.passwd" <%=!displayCommonProps ? "style=\"display:none;\"" : ""%>>
                <td>
                    <fmt:message key="mediator.dbreport.password"/>
                    <font style="color: red; font-size: 8pt;"> *</font>
                </td>
                <td><input type="password" name="password" id="password" style="width:300px"  value="<%=passwd%>"/></td>
            </tr>
        </tbody>
    </table>
    </td></tr>
    <tr id="dsProps" <%=displayExisistingDs ? "style=\"display:none;\"" : ""%>><td>
    <h3 class="mediator"><fmt:message key="mediator.dbreport.propertyHeader"/></h3>
    <div id="property_table">
        <table id="mediator.dbr.property" class="styledInner" <%=(otherProps.keySet().size() == 0) ? "style=\"display:none\"" : ""%>>
            <thead>
                <tr>
                    <th style="width:150px"><fmt:message key="mediator.dbreport.name"/></th>
                    <th style="width:320px"><fmt:message key="mediator.dbreport.value"/></th>
                    <th><fmt:message key="mediator.dbreport.action"/></th>
                </tr>
            </thead>
            <tbody id="properties">
                <%
                    int no = 0;
                    while (otherPropIt.hasNext()) {
                    Object o = otherPropIt.next();
                    String key = (String) o;
                    String value = (String) otherProps.get(key);
                    no++;
                %>
                <tr>
                    <td>
                        <select name="property<%=no%>" id="property<%=no%>" onchange="dbrOptionChange(<%=no%>);">
                            <option value="Select A Value">Select A Property</option>
                            <option value="autocommit" <%=key.equals("autocommit") ? "selected=\"selected\"" : ""%>>autocommit</option>
                            <option value="isolation" <%=key.equals("isolation") ? "selected=\"selected\"" : ""%>>isolation</option>
                            <option value="maxactive" <%=key.equals("maxactive") ? "selected=\"selected\"" : ""%>>maxactive</option>
                            <option value="maxidle" <%=key.equals("maxidle") ? "selected=\"selected\"" : ""%>>maxidle</option>
                            <option value="maxopenstatements" <%=key.equals("maxopenstatements") ? "selected=\"selected\"" : ""%>>maxopenstatements</option>
                            <option value="maxwait" <%=key.equals("maxwait") ? "selected=\"selected\"" : ""%>>maxwait</option>
                            <option value="minidle" <%=key.equals("minidle") ? "selected=\"selected\"" : ""%>>minidle</option>
                            <option value="poolstatements" <%=key.equals("poolstatements") ? "selected=\"selected\"" : ""%>>poolstatements</option>
                            <option value="testonborrow" <%=key.equals("testonborrow") ? "selected=\"selected\"" : ""%>>testonborrow</option>
                            <option value="testwhileidle" <%=key.equals("testwhileidle") ? "selected=\"selected\"" : ""%>>testwhileidle</option>
                            <option value="validationquery" <%=key.equals("validationquery") ? "selected=\"selected\"" : ""%>>validationquery</option>
                            <option value="initialsize" <%=key.equals("initialsize") ? "selected=\"selected\"" : ""%>>initialsize</option>
                        </select>
                    </td>
                    <td>
                        <% if (key.equals("autocommit")) { %>
                        <select name="property_value<%=no%>" id="property_value<%=no%>" style="width:300px">
                            <option value="true" <%=value.equals("true") ? "selected=\"selected\"" : ""%>>true</option>
                            <option value="false" <%=value.equals("false") ? "selected=\"selected\"" : ""%>>false</option>
                        </select>
                        <%} else if (key.equals("isolation")) { %>
                        <select name="property_value<%=no%>" id="property_value<%=no%>" style="width:300px">
                            <option value="Connection.TRANSACTION_NONE" <%=value.equals("Connection.TRANSACTION_NONE") ? "selected=\"selected\"" : ""%>>Connection.TRANSACTION_NONE</option>
                            <option value="Connection.TRANSACTION_READ_COMMITTED" <%=value.equals("Connection.TRANSACTION_READ_COMMITTED") ? "selected=\"selected\"" : ""%>>Connection.TRANSACTION_READ_COMMITTED</option>
                            <option value="Connection.TRANSACTION_READ_UNCOMMITTED" <%=value.equals("Connection.TRANSACTION_READ_UNCOMMITTED") ? "selected=\"selected\"" : ""%>>Connection.TRANSACTION_READ_UNCOMMITTED</option>
                            <option value="Connection.TRANSACTION_REPEATABLE_READ" <%=value.equals("Connection.TRANSACTION_REPEATABLE_READ") ? "selected=\"selected\"" : ""%>>Connection.TRANSACTION_REPEATABLE_READ</option>
                            <option value="Connection.TRANSACTION_SERIALIZABLE" <%=value.equals("Connection.TRANSACTION_SERIALIZABLE") ? "selected=\"selected\"" : ""%>>Connection.TRANSACTION_SERIALIZABLE</option>
                        </select>
                        <%} else if (key.equals("maxactive")) { %>
                        <input type="text" name="property_value<%=no%>" id="property_value<%=no%>" value="<%=value%>" style="width:300px">
                        <%} else if (key.equals("maxidle")) { %>
                        <input type="text" name="property_value<%=no%>" id="property_value<%=no%>" value="<%=value%>" style="width:300px">
                        <%} else if (key.equals("maxopenstatements")) { %>
                        <input type="text" name="property_value<%=no%>" id="property_value<%=no%>" value="<%=value%>" style="width:300px">
                        <%} else if (key.equals("maxwait")) { %>
                        <input type="text" name="property_value<%=no%>" id="property_value<%=no%>" value="<%=value%>" style="width:300px">
                        <%} else if (key.equals("minidle")) { %>
                        <input type="text" name="property_value<%=no%>" id="property_value<%=no%>" value="<%=value%>" style="width:300px">
                        <%} else if (key.equals("poolstatements")) { %>
                        <select name="property_value<%=no%>" id="property_value<%=no%>" style="width:300px">
                            <option value="true" <%=value.equals("true") ? "selected=\"selected\"" : ""%>>true</option>
                            <option value="false" <%=value.equals("false") ? "selected=\"selected\"" : ""%>>false</option>
                        </select>
                        <%} else if (key.equals("testonborrow")) { %>
                        <select name="property_value<%=no%>" id="property_value<%=no%>" style="width:300px">
                            <option value="true" <%=value.equals("true") ? "selected=\"selected\"" : ""%>>true</option>
                            <option value="false" <%=value.equals("false") ? "selected=\"selected\"" : ""%>>false</option>
                        </select>
                        <%} else if (key.equals("testwhileidle")) { %>
                        <select name="property_value<%=no%>" id="property_value<%=no%>" style="width:300px">
                            <option value="true" <%=value.equals("true") ? "selected=\"selected\"" : ""%>>true</option>
                            <option value="false" <%=value.equals("false") ? "selected=\"selected\"" : ""%>>false</option>
                        </select>
                        <%} else if (key.equals("validationquery")) { %>
                        <input name="property_value<%=no%>" id="property_value<%=no%>" type="text" value="<%=value%>" style="width:300px">
                        <%} else if (key.equals("initialsize")) { %>
                        <input name="property_value<%=no%>" id="property_value<%=no%>" type="text" value="<%=value%>" style="width:300px">
                        <%} else {%>
                        <input name="property_value<%=no%>" id="property_value<%=no%>" type="text" value="<%=value%>" style="width:300px">
                        <%}%>
                    </td>
                    <td><a onclick="dbrDeleteCurrentRow(this);" class="delete-icon-link" href="#"><fmt:message key="mediator.dbreport.delete"/></a></td>
                </tr>
                <%}%>
            </tbody>
        </table>
    </div>
    </td></tr>
    <tr style="display:none"><td>
        <input type="hidden" id="hidden_property" name="hidden_property" value="<%=otherProps.keySet().size() + 1%>">
    </td></tr>
    <tr id="addProp" <%=displayExisistingDs ? "style=\"display:none;\"" : ""%>><td>
    <%--<input type="button" class="button" value="Add Property"--%>
           <%--onclick="javascript:dbrAddRowToTable('mediator.dbr.property', null);"/>--%>
        <a href="#addNameLink"
          onclick="dbrAddRowToTable('mediator.dbr.property', null);"
          class="add-icon-link"><fmt:message key="mediator.dbreport.addProperty"/>
        </a>
    </td></tr>
    <tr><td>
    <h3 class="mediator"><fmt:message key="mediator.dbreport.sqlStatment"/></h3>
    </td></tr>
    <tr><td>
        <%--Add new statement <input type="button" class="button" value="Add" onclick="dbrAddStatement();"/>--%>
        <a href="#addNameLink"
          onclick="dbrAddStatement();"
          class="add-icon-link"><fmt:message key="mediator.dbreport.addStmt"/>
        </a>
    </td></tr>
    <tr><td>
    <div id="queries">
        <% for (int i = 1; i <= dbReportMediator.getStatementList().size(); i++) {%>
        <div id="<%="statement" + i%>" class="tabedBox">
            <% Statement stmt = (Statement) dbReportMediator.getStatementList().get(i - 1);
               String rowSQL = "";
                if (stmt.getRawStatement() != null) {
                    rowSQL = stmt.getRawStatement().trim();
                }
            %>
            <table class="normal">
                <tbody>
                    <tr>
                        <td style="width:80px"><fmt:message key="mediator.dbreport.sql"/><font style="color: red; font-size: 8pt;"> *</font></td>
                        <td style="width:305px"><input type="text" id="sql_val<%=(i)%>" name="sql_val<%=(i)%>" class="longTextField" value="<%=rowSQL%>"/></td>
                        <td><a onclick="dbrDeleteStatement('<%="statement" + (i)%>');" class="delete-icon-link" href="#"><fmt:message key="mediator.dbreport.delete"/></a>
                        </td>
                    </tr>
                </tbody>
            </table>                                                                        
            <h3 class="mediator"><fmt:message key="mediator.dbreport.parameters"/></h3>
            <div class="rowAlone">
                <%--<fmt:message key="mediator.dbreport.addParam"/> <input type="button" class="button" value="Add" onclick="javascript:dbrAddStmtParamTableRow('mediator.dbr.tp<%=i%>', null, <%=i%>);"/>--%>
                <a href="#addNameLink"
                  onclick="dbrAddStmtParamTableRow('mediator.dbr.tp<%=i%>', null, <%=i%>);"
                  class="add-icon-link"><fmt:message key="mediator.dbreport.addParameter"/>
                </a>
            </div>
            <table class="styledInner" id="mediator.dbr.tp<%=i%>" <%=(stmt.getParameters() == null || stmt.getParameters().size() == 0) ? "style=\"display:none\"" : ""%>>
                <thead>
                    <tr>
                        <th style="width:150px"><fmt:message key="mediator.dbreport.paramType"/></th>
                        <th style="width:100px"><fmt:message key="mediator.dbreport.propType"/></th>
                        <th style="width:320px"><fmt:message key="mediator.dbreport.valueExpr"/></th>
                        <th style="width:150px"><fmt:message key="mediator.dbreport.nmsp"/></th>
                        <th><fmt:message key="mediator.dbreport.action"/></th>
                    </tr>
                </thead>
                <tbody id="parameters1">
                <% if (stmt.getParameters() != null) {%>
                    <% for (int j = 1; j <= stmt.getParameters().size(); j++) {
                          Statement.Parameter p = (Statement.Parameter) stmt.getParameters().get(j - 1);
                          boolean isExp = false;
                          String value = "";
                          if (p.getXpath() != null) {
                            nmspRegistrar.registerNameSpaces(p.getXpath(), "parameter_value" + i + "." + j, session);
                              value = p.getXpath().toString();
                              isExp = true;
                          } else {
                              value = p.getPropertyName();
                              isExp = false;
                          }
                    %>
                    <tr id="0parameter_id1">
                        <td>
                            <select name="javaType<%=i + "." + j%>" id="javaType<%=i + "." + j%>">
                                <option value="CHAR" <%=p.getType() == Types.CHAR ? "selected=\"selected\"": ""%>selected="true">CHAR</option>
                                <option value="VARCHAR" <%=p.getType() == Types.VARCHAR ? "selected=\"selected\"": ""%>>VARCHAR</option>
                                <option value="NUMERIC" <%=p.getType() == Types.NUMERIC ? "selected=\"selected\"": ""%>>NUMERIC</option>
                                <option value="DECIMAL" <%=p.getType() == Types.DECIMAL ? "selected=\"selected\"": ""%>>DECIMAL</option>
                                <option value="BIT" <%=p.getType() == Types.BIT ? "selected=\"selected\"": ""%>>BIT</option>
                                <option value="TINYINT" <%=p.getType() == Types.TINYINT ? "selected=\"selected\"": ""%>>TINYINT</option>
                                <option value="SMALLINT" <%=p.getType() == Types.SMALLINT ? "selected=\"selected\"": ""%>>SAMLLINT</option>
                                <option value="INTEGER" <%=p.getType() == Types.INTEGER ? "selected=\"selected\"": ""%>>INTEGER</option>
                                <option value="BIGINT" <%=p.getType() == Types.BIGINT ? "selected=\"selected\"": ""%>>BIGINT</option>
                                <option value="REAL" <%=p.getType() == Types.REAL ? "selected=\"selected\"": ""%>>REAL</option>
                                <option value="DOUBLE" <%=p.getType() == Types.DOUBLE ? "selected=\"selected\"": ""%>>DOUBLE</option>
                                <option value="DATE" <%=p.getType() == Types.DATE ? "selected=\"selected\"": ""%>>DATE</option>
                                <option value="TIME" <%=p.getType() == Types.TIME ? "selected=\"selected\"": ""%>>TIME</option>
                                <option value="TIMESTAMP" <%=p.getType() == Types.TIMESTAMP ? "selected=\"selected\"": ""%>>TIMESTAMP</option>
                            </select>
                        </td>
                        <td>
                            <select id="parameterType<%=i + "." + j%>" name="parameterType<%=i + "." + j%>" onchange="showNSColumn('<%=i%>', '<%=j%>');">
                                <option value="value" <%=!isExp ? "selected='selected'" : ""%>><fmt:message key="mediator.dbreport.value"/></option>
                                <option value="expression" <%=isExp ? "selected='selected'" : ""%>><fmt:message key="mediator.dbreport.exp"/></option>
                            </select>
                        </td>
                        <td><input type="text" style="width:300px" id="parameter_value<%=i + "." + j%>" name="parameter_value<%=i + "." + j%>" value="<%=value%>"/>
                        </td>
                        <td>
                            <% if (isExp) { %>
                            <a href="#" id="paramNS<%=i + "." + j%>" onclick="showNameSpaceEditor('parameter_value<%=i + "." + j%>')" class="nseditor-icon-link" style="padding-left:40px">
                            <fmt:message key="mediator.dbreport.namespace"/>
                           </a>
                            <% } else { %>
                            <a href="#" id="paramNS<%=i + "." + j%>" onclick="showNameSpaceEditor('parameter_value<%=i + "." + j%>')" class="nseditor-icon-link" style="padding-left:40px;display:none">
                            <fmt:message key="mediator.dbreport.namespace"/>
                           </a>
                            <% } %>
                        </td>
                        <td><a onclick="dbrDeleteCurrentRow(this);" class="delete-icon-link" href="#"><fmt:message key="mediator.dbreport.delete"/></a></td>
                    </tr>
                    <% } %>
                </tbody>
            </table>
            <% } %>
            <input type="hidden" name="hidden_parameters<%=i%>" id="hidden_parameters<%=i%>" value="<%=stmt.getParameters().size() + 1%>">
        </div>
        <% } %>
    </div>
    </td></tr>
    <tr style="display:none;"><td>
    <input type="hidden" id="hidden_stmt" name="hidden_stmt" value="<%=dbReportMediator.getStatementList().size() + 1%>">
    <div id="nsEditor" style="display:none;"/>
    </td></tr>
</table>
</div>
</fmt:bundle>

