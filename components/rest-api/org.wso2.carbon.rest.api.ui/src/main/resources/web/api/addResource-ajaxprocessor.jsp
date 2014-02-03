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
<%@page import="org.apache.axis2.context.ConfigurationContext" %>
<%@page import="org.wso2.carbon.CarbonConstants" %>
<%@ page import="org.wso2.carbon.rest.api.stub.types.carbon.ResourceData" %>
<%@ page import="org.wso2.carbon.rest.api.ui.client.RestApiAdminClient" %>
<%@ page import="org.wso2.carbon.ui.CarbonUIUtil" %>
<%@ page import="org.wso2.carbon.utils.ServerConstants" %>
<%@ page import="java.util.ArrayList" %>
<%@ page import="java.util.List" %>
<%@ page import="java.util.ResourceBundle" %>
<%@ taglib prefix="fmt" uri="http://java.sun.com/jsp/jstl/fmt" %>
<%@ taglib uri="http://wso2.org/projects/carbon/taglibs/carbontags.jar" prefix="carbon" %>

<%
    String inRegKey = "";
    String outRegKey = "";
    String faultRegKey = "";
    ResourceBundle bundle = ResourceBundle.getBundle(
            "org.wso2.carbon.rest.api.ui.i18n.Resources",
            request.getLocale());
    // sets anonymous sequence options
    String anonInAddEdit, anonOutAddEdit, anonFaultAddEdit;
    anonInAddEdit = anonOutAddEdit = anonFaultAddEdit = bundle.getString("create");
    String url = CarbonUIUtil.getServerURL(this.getServletConfig()
                                                   .getServletContext(), session);
    ConfigurationContext configContext = (ConfigurationContext) config
            .getServletContext().getAttribute(
                    CarbonConstants.CONFIGURATION_CONTEXT);
    String cookie = (String) session
            .getAttribute(ServerConstants.ADMIN_SERVICE_COOKIE);
    RestApiAdminClient client = new RestApiAdminClient(configContext,
                                                       url, cookie, bundle.getLocale());

    String[] sequences = client.getDefinedSequences();

    // the variables holding which option of sequences or endpoints to be selected
    String whichEP, whichFaultSeq, whichInSeq, whichOutSeq;
    whichEP = whichFaultSeq = whichInSeq = whichOutSeq = "None";

    session.setAttribute("index", "-1");
    List<ResourceData> resources = (ArrayList<ResourceData>) session.getAttribute("apiResources");
    session.setAttribute("apiResources", resources);
    session.removeAttribute("isTempResource");
    session.removeAttribute("resource");
    session.removeAttribute("addResource");
    session.removeAttribute("editResource");
    session.removeAttribute("resourceData");
    session.removeAttribute("inSeqXml");
    session.removeAttribute("outSeqXml");
    session.removeAttribute("faultSeqXml");
%>

<fmt:bundle basename="org.wso2.carbon.rest.api.ui.i18n.Resources">

<script type="text/javascript">
    YAHOO.util.Event.onDOMReady(init);
    var anonInAction = '<%=anonInAddEdit%>';
    var anonOutAction = '<%=anonOutAddEdit%>';
    var anonFaultAction = '<%=anonFaultAddEdit%>';

    function init() {
        setSelected('faultSeqOp', '<%=whichFaultSeq%>');
        setSelected('inSeqOp', '<%=whichInSeq%>');
        setSelected('outSeqOp', '<%=whichOutSeq%>');
        hideSeqOps('in');
        hideSeqOps('out');
        hideSeqOps('fault');
        showHideSeqOpsOnLoad();
    }

    function hideSeqOps(sequence) {
        hideElem(sequence + 'ImportSeq');
        hideElem(sequence + 'AnonAddEdit');
        hideElem(sequence + 'AnonClear');
        hideElem(sequence + 'Registry');
    }

    function setSelected(type, option) {
        var element;
        element = getElement(type + option);
        element.setAttribute('checked', 'checked');
    }


    function showHideSeqOpsOnLoad() {
        if ('<%=whichInSeq%>' == 'Anon') {
            showElem('inAnonAddEdit');
            if (anonInAction == '<fmt:message key="anon.edit"/>') {
                showElem('inAnonClear');
            } else {
                hideElem('inAnonClear');
            }
        } else if ('<%=whichInSeq%>' == "Imp") {
            showElem('inImportSeq');
        } else if ('<%=whichInSeq%>' == "Reg") {
            showElem('inRegistry');
        }

        if ('<%=whichOutSeq%>' == 'Anon') {
            showElem('outAnonAddEdit');
            if (anonOutAction == '<fmt:message key="anon.edit"/>') {
                showElem('outAnonClear');
            } else {
                hideElem('outAnonClear');
            }
        } else if ('<%=whichOutSeq%>' == "Imp") {
            showElem('outImportSeq');
        } else if ('<%=whichOutSeq%>' == "Reg") {
            showElem('outRegistry');
        }

        if ('<%=whichFaultSeq%>' == 'Anon') {
            showElem('faultAnonAddEdit');
            if (anonFaultAction == '<fmt:message key="anon.edit"/>') {
                showElem('faultAnonClear');
            } else {
                hideElem('faultAnonClear');
            }
        } else if ('<%=whichFaultSeq%>' == "Imp") {
            showElem('faultImportSeq');
        } else if ('<%=whichFaultSeq%>' == "Reg") {
            showElem('faultRegistry');
        }
    }

    function getElement(id) {
        return document.getElementById(id);
    }

    function hideElem(objid) {
        var theObj = document.getElementById(objid);
        if (theObj) {
            theObj.style.display = "none";
        }

    }

    function showElem(objid) {
        var theObj = document.getElementById(objid);
        if (theObj) {
            theObj.style.display = "";
        }
    }

    function radioClicked(seqence, name) {
        if (name != null) {
            showElem(seqence + name);
            if (name == "Registry") {
                hideElem(seqence + "ImportSeq");
                hideElem(seqence + "AnonAddEdit");
                hideElem(seqence + "AnonClear");
            } else if (name == "ImportSeq") {
                hideElem(seqence + "Registry");
                hideElem(seqence + "AnonAddEdit");
                hideElem(seqence + "AnonClear");
            } else if (name == "AnonAddEdit") {
                hideElem(seqence + "Registry");
                hideElem(seqence + "ImportSeq");
                if (seqence == 'in' && anonInAction == '<fmt:message key="anon.edit"/>') {
                    showElem('inAnonClear');
                } else if (seqence == 'out' && anonOutAction == '<fmt:message key="anon.edit"/>') {
                    showElem('outAnonClear');
                } else if (seqence == 'fault' && anonFaultAction == '<fmt:message key="anon.edit"/>') {
                    showElem('faultAnonClear');
                }
            }
        } else {
            hideElem(seqence + "Registry");
            hideElem(seqence + "ImportSeq");
            hideElem(seqence + "AnonAddEdit");
            hideElem(seqence + "AnonClear");
        }
    }

    // sets the values of the headerTable as the value of serviceParams (i.e. comma separated list of name:value pairs)
    function populateServiceParams() {
        var i;
        var str = '';
        var headerTable = document.getElementById("headerTable");
        for (var j = 1; j < headerTable.rows.length; j++) {
            var parmName = headerTable.rows[j].getElementsByTagName("input")[0].value;
            var parmValue = headerTable.rows[j].getElementsByTagName("input")[1].value;
            if (j == 1) {
                str += parmName + ',' + parmValue;
            } else {
                str += '::' + parmName + ',' + parmValue;
            }
        }

        document.designForm.serviceParams.value = str;
    }

    function anonSeqAddEdit(sequence) {
        if (updateResource('true') != false) {
            location.href = "designToData.jsp?return=anonSequenceHandler.jsp"
                                    + "&originator=manageAPI.jsp&sequence=" + sequence + "&index=-1";
        }
    }

    function anonSeqClear(sequence) {
        if (sequence == 'in') {
            anonInAction = "<fmt:message key="create"/>";
            getElement('inAnonAddEdit').innerHTML = anonInAction;
        } else if (sequence == 'out') {
            anonOutAction = "<fmt:message key="create"/>";
            getElement('outAnonAddEdit').innerHTML = anonOutAction;
        } else if (sequence == 'fault') {
            anonFaultAction = "<fmt:message key="create"/>";
            getElement('faultAnonAddEdit').innerHTML = anonFaultAction;
        }
        hideElem(sequence + 'AnonClear');
        getElement(sequence + 'AnonAddEdit').style.backgroundImage = "url(../admin/images/add.gif);";
    }

</script>

<table class="normal-nopadding" width="100%">
<!-- Methods-->
<tr>
    <td class="leftCol-small">
        <fmt:message key="resource.methods.label"/>
    </td>
    <td style="display:inline; vertical-align:middle;">
        <input type="checkbox" name="methods" value="GET"
               style="display:inline; vertical-align: middle"/>
        <div id="divGet" style="display:inline;">
            <fmt:message key="methods.get.label"/>        </div>
        <input type="checkbox" name="methods" value="POST"
               style="display:inline; vertical-align: middle"/>
        <div id="divPost" style="display:inline;">
            <fmt:message key="methods.post.label"/>        </div>
        <input type="checkbox" name="methods" value="PUT"
               style="display:inline; vertical-align: middle"/>
        <div id="divPut" style="display:inline;">
            <fmt:message key="methods.put.label"/>
        </div>
        <input type="checkbox" name="methods" value="DELETE"
               style="display:inline; vertical-align: middle"/>
        <div id="divDelete" style="display:inline;">
            <fmt:message key="methods.delete.label"/>
        </div>
        <input type="checkbox" name="methods" value="OPTIONS"
               style="display:inline; vertical-align: middle"/>
        <div id="divOptions" style="display:inline;">
            <fmt:message key="methods.options.label"/>
        </div>
    </td>
</tr>
<!-- URL Style-->
<tr>
    <td class="leftCol-small">
        <fmt:message key="resource.urlstyle.label"/>
    </td>
    <td>
        <select id="urlStyle" onchange="urlStyleChanged()">
            <option value="none" selected="selected">
                <fmt:message key="resource.urlstyle.none"/>
            </option>
            <option value="uritemplate">
                <fmt:message key="resource.urlstyle.template"/>
            </option>
            <option value="urlmapping">
                <fmt:message key="resource.urlstyle.mapping"/>
            </option>
        </select>
    </td>
</tr>
<!-- URI-Template / URL-Mapping -->
<tr id="urlRow" style="display:none;">
    <td id="uriTL" style="display:none;" class="leftCol-small">
        <fmt:message key="resource.uritempl.label"/>
    </td>
    <td id="urlML" style="display:none;" class="leftCol-small">
        <fmt:message key="resource.urlmap.label"/>
    </td>
    <td>
        <input type="text" id="urlValue"/>
    </td>
</tr>

<tr>
<table width="100%" class="styledInner">
<table width="100%" class="styledInner">
    <thead>
    <tr>
        <th><fmt:message key="in.sequence.options"/></th>
    </tr>
    </thead>
    <tbody>
    <tr>
        <td>
            <!-- In Sequence -->
            <div id="in1Seq">
                <div id="in1SeqDesign">
                    <table id="inSeqOptionTable" class="normal">
                        <tr>
                            <td class="nopadding">
                                <input id="inSeqOpNone" type="radio" name="inSeqOp"
                                       value="none"
                                       onclick="radioClicked('in', null);"/>
                            </td>
                            <td style="vertical-align:middle;" class="nopadding"><label
                                    for="inSeqOpNone"><fmt:message
                                    key="select.inseq.none"/></label></td>
                            <td class="nopadding"></td>
                        </tr>
                        <tr>
                            <td class="nopadding">
                                <input id="inSeqOpAnon" type="radio" name="inSeqOp"
                                       value="anon"
                                       onclick="radioClicked('in', 'AnonAddEdit');"/>
                            </td>
                            <td style="vertical-align:middle;" class="nopadding"><label
                                    for="inSeqOpAnon"><fmt:message
                                    key="define.inline"/></label></td>
                            <td style="vertical-align:middle;" class="nopadding">
                                <% if (anonInAddEdit.equalsIgnoreCase(bundle.getString("create"))) {%>
                                <a class="icon-link" id="inAnonAddEdit"
                                   style="background-image: url(../admin/images/add.gif);"
                                   onclick="anonSeqAddEdit('in');"><%=anonInAddEdit%>
                                </a>
                                <% } else {%>
                                <a class="icon-link" id="inAnonAddEdit"
                                   style="background-image: url(../admin/images/edit.gif);"
                                   onclick="anonSeqAddEdit('in');"><%=anonInAddEdit%>
                                </a>
                                <% } %>
                            </td>
                            <td class="nopadding">&nbsp;</td>
                            <td style="vertical-align:middle;" class="nopadding">
                                <a class="icon-link"
                                   style="background-image: url(../admin/images/delete.gif);"
                                   id="inAnonClear" onclick="anonSeqClear('in');"><fmt:message
                                        key="clear"/></a>
                            </td>
                        </tr>
                        <tr>
                            <td class="nopadding">
                                <input id="inSeqOpReg" type="radio" name="inSeqOp"
                                       value="registry"
                                       onclick="radioClicked('in', 'Registry');"/>
                            </td>
                            <td style="vertical-align:middle;" class="nopadding"><label
                                    for="inSeqOpReg"><fmt:message
                                    key="pick.from.registry"/></label></td>
                            <td class="nopadding">
                                <table id="inRegistry">
                                    <tr>
                                        <td class="nopadding">
                                            <input type="text" name="api.in.registry"
                                                   id="api.in.registry" value="<%=inRegKey%>"
                                                   style="width:300px"
                                                   readonly="readonly"/>
                                        </td>
                                        <td class="nopadding">
                                            <a class="registry-picker-icon-link"
                                               style="padding-left:20px"
                                               onclick="showRegistryBrowserWithoutLocalEntries('api.in.registry','/_system/config');"><fmt:message
                                                    key="conf.registry"/></a>
                                        </td>
                                        <td class="nopadding">
                                            <a class="registry-picker-icon-link"
                                               style="padding-left:20px"
                                               onclick="showRegistryBrowserWithoutLocalEntries('api.in.registry','/_system/governance');"><fmt:message
                                                    key="gov.registry"/></a>
                                        </td>
                                    </tr>
                                </table>
                            </td>
                        </tr>
                        <tr>
                            <td class="nopadding">
                                <input id="inSeqOpImp" type="radio" name="inSeqOp"
                                       value="import"
                                       onclick="radioClicked('in', 'ImportSeq');"/>
                            </td>
                            <td style="vertical-align:middle;" class="nopadding"><label
                                    for="inSeqOpImp"><fmt:message
                                    key="use.existing.sequence"/></label></td>
                            <td style="vertical-align:middle;" class="nopadding">
                                <select id="inImportSeq" name="inImportSeq">
                                    <option value="none" selected="selected">
                                        <fmt:message key="resource.sequence.none"/>
                                    </option>
                                    <%
                                        if (sequences != null && sequences.length != 0) {
                                            for (int i = 0; i < sequences.length; i++) {
                                    %>
                                    <option value="<%=sequences[i]%>">
                                        <%=sequences[i]%>
                                    </option>
                                    <%
                                            }
                                        }
                                    %>
                                </select>
                            </td>
                        </tr>
                    </table>
                </div>
            </div>
        </td>
    </tr>
    </tbody>
</table>
<br/>
<table width="100%" class="styledInner">
    <thead>
    <tr>
        <th><fmt:message key="outsequence.options"/></th>
    </tr>
    </thead>
    <tbody>
    <tr>
        <td>
            <!-- Out Sequence -->
            <div id="out1Seq">
                <div id="out1SeqDesign">
                    <table id="outSeqOptionTable" class="normal">
                        <tr>
                            <td class="nopadding">
                                <input id="outSeqOpNone" type="radio" name="outSeqOp"
                                       value="none"
                                       onclick="radioClicked('out', null);"/>
                            </td>
                            <td style="vertical-align:middle;" class="nopadding"><label
                                    for="inSeqOpNone"><fmt:message
                                    key="select.outseq.none"/></label></td>
                            <td class="nopadding"></td>
                        </tr>
                        <tr>
                            <td class="nopadding">
                                <input id="outSeqOpAnon" type="radio" name="outSeqOp"
                                       value="anon"
                                       onclick="radioClicked('out', 'AnonAddEdit');"/>
                            </td>
                            <td style="vertical-align:middle;" class="nopadding"><label
                                    for="outSeqOpAnon"><fmt:message
                                    key="define.inline"/></label></td>
                            <td style="vertical-align:middle;" class="nopadding">
                                <% if (anonOutAddEdit.equalsIgnoreCase(bundle.getString("create"))) {%>
                                <a class="icon-link" id="outAnonAddEdit"
                                   style="background-image: url(../admin/images/add.gif);"
                                   onclick="anonSeqAddEdit('out');"><%=anonOutAddEdit%>
                                </a>
                                <% } else {%>
                                <a class="icon-link" id="outAnonAddEdit"
                                   style="background-image: url(../admin/images/edit.gif);"
                                   onclick="anonSeqAddEdit('out');"><%=anonOutAddEdit%>
                                </a>
                                <% } %>
                            </td>
                            <td class="nopadding">&nbsp;</td>
                            <td style="vertical-align:middle;" class="nopadding">
                                <a class="icon-link"
                                   style="background-image: url(../admin/images/delete.gif);"
                                   id="outAnonClear"
                                   onclick="anonSeqClear('out');"><fmt:message
                                        key="clear"/></a>
                            </td>
                        </tr>
                        <tr>
                            <td class="nopadding">
                                <input id="outSeqOpReg" type="radio" name="outSeqOp"
                                       value="registry"
                                       onclick="radioClicked('out', 'Registry');"/>
                            </td>
                            <td style="vertical-align:middle;" class="nopadding">
                                <label
                                        for="outSeqOpReg"><fmt:message
                                        key="pick.from.registry"/></label>
                            </td>
                            <td class="nopadding">
                                <table id="outRegistry">
                                    <tr>
                                        <td class="nopadding">
                                            <input type="text" name="api.out.registry"
                                                   id="api.out.registry"
                                                   value="<%=outRegKey%>"
                                                   style="width:300px"
                                                   readonly="readonly"/>
                                        </td>
                                        <td class="nopadding">
                                            <a class="registry-picker-icon-link"
                                               style="padding-left:20px"
                                               onclick="showRegistryBrowserWithoutLocalEntries('api.out.registry','/_system/config');"><fmt:message
                                                    key="conf.registry"/></a>
                                        </td>
                                        <td class="nopadding">
                                            <a class="registry-picker-icon-link"
                                               style="padding-left:20px"
                                               onclick="showRegistryBrowserWithoutLocalEntries('api.out.registry','/_system/governance');"><fmt:message
                                                    key="gov.registry"/></a>
                                        </td>
                                    </tr>
                                </table>
                            </td>
                        </tr>
                        <tr>
                            <td class="nopadding">
                                <input id="outSeqOpImp" type="radio" name="outSeqOp"
                                       value="import"
                                       onclick="radioClicked('out', 'ImportSeq');"/>
                            </td>
                            <td style="vertical-align:middle;" class="nopadding"><label
                                    for="outSeqOpImp"><fmt:message
                                    key="use.existing.sequence"/></label></td>
                            <td style="vertical-align:middle;" class="nopadding">
                                <select id="outImportSeq" name="outImportSeq">
                                    <option value="none" selected="selected">
                                        <fmt:message key="resource.sequence.none"/>
                                    </option>
                                    <%
                                        if (sequences != null && sequences.length != 0) {
                                            for (int i = 0; i < sequences.length; i++) {
                                    %>
                                    <option value="<%=sequences[i]%>">
                                        <%=sequences[i]%>
                                    </option>
                                    <%
                                            }
                                        }
                                    %>
                                </select>
                            </td>
                        </tr>
                    </table>
                </div>
            </div>
        </td>
    </tr>
    </tbody>
</table>
<br/>
<table width="100%" class="styledInner">
    <thead>
    <tr>
        <th><fmt:message key="api.faultSequence"/></th>
    </tr>
    </thead>
    <tbody>
    <tr>
        <td>
            <!-- Fault Sequence -->
            <div id="fault1Seq">
                <div id="fault1SeqDesign">
                    <table id="faultSeqOptionTable" class="normal">
                        <tr>
                            <td class="nopadding">
                                <input id="faultSeqOpNone" type="radio" name="faultSeqOp"
                                       value="none"
                                       onclick="radioClicked('fault', null);"/>
                            </td>
                            <td style="vertical-align:middle;" class="nopadding"><label
                                    for="inSeqOpNone"><fmt:message
                                    key="select.faultseq.none"/></label></td>
                            <td class="nopadding"></td>
                        </tr>
                        <tr>
                            <td class="nopadding">
                                <input id="faultSeqOpAnon" type="radio" name="faultSeqOp"
                                       value="anon"
                                       onclick="radioClicked('fault', 'AnonAddEdit');"/>
                            </td>
                            <td style="vertical-align:middle;" class="nopadding"><label
                                    for="faultSeqOpAnon"><fmt:message
                                    key="define.inline"/></label></td>
                            <td style="vertical-align:middle;" class="nopadding">
                                <% if (anonFaultAddEdit.equalsIgnoreCase(bundle.getString("create"))) {%>
                                <a class="icon-link" id="faultAnonAddEdit"
                                   style="background-image: url(../admin/images/add.gif);"
                                   onclick="anonSeqAddEdit('fault');"><%=anonFaultAddEdit%>
                                </a>
                                <% } else {%>
                                <a class="icon-link" id="faultAnonAddEdit"
                                   style="background-image: url(../admin/images/edit.gif);"
                                   onclick="anonSeqAddEdit('fault');"><%=anonFaultAddEdit%>
                                </a>
                                <% } %>
                            </td>
                            <td class="nopadding">&nbsp;</td>
                            <td style="vertical-align:middle;" class="nopadding">
                                <a class="icon-link"
                                   style="background-image: url(../admin/images/delete.gif);"
                                   id="faultAnonClear"
                                   onclick="anonSeqClear('fault');"><fmt:message
                                        key="clear"/></a>
                            </td>
                        </tr>
                        <tr>
                            <td class="nopadding">
                                <input id="faultSeqOpReg" type="radio" name="faultSeqOp"
                                       value="registry"
                                       onclick="radioClicked('fault', 'Registry');"/>
                            </td>
                            <td style="vertical-align:middle;" class="nopadding"><label
                                    for="faultSeqOpReg"><fmt:message
                                    key="pick.from.registry"/></label></td>
                            <td class="nopadding">
                                <table id="faultRegistry">
                                    <tr>
                                        <td class="nopadding">
                                            <input type="text" name="api.fault.registry"
                                                   id="api.fault.registry"
                                                   value="<%=faultRegKey%>"
                                                   style="width:300px"
                                                   readonly="readonly"/>
                                        </td>
                                        <td class="nopadding">
                                            <a class="registry-picker-icon-link"
                                               style="padding-left:20px"
                                               onclick="showRegistryBrowserWithoutLocalEntries('api.fault.registry','/_system/config');"><fmt:message
                                                    key="conf.registry"/></a>
                                        </td>
                                        <td class="nopadding">
                                            <a class="registry-picker-icon-link"
                                               style="padding-left:20px"
                                               onclick="showRegistryBrowserWithoutLocalEntries('api.fault.registry','/_system/governance');"><fmt:message
                                                    key="gov.registry"/></a>
                                        </td>
                                    </tr>
                                </table>
                            </td>
                        </tr>
                        <tr>
                            <td class="nopadding">
                                <input id="faultSeqOpImp" type="radio" name="faultSeqOp"
                                       value="import"
                                       onclick="radioClicked('fault', 'ImportSeq');"/>
                            </td>
                            <td style="vertical-align:middle;" class="nopadding"><label
                                    for="faultSeqOpImp"><fmt:message
                                    key="use.existing.sequence"/></label></td>
                            <td style="vertical-align:middle;" class="nopadding">
                                <select id="faultImportSeq" name="faultImportSeq">
                                    <option value="none" selected="selected">
                                        <fmt:message key="resource.sequence.none"/>
                                    </option>
                                    <%
                                        if (sequences != null && sequences.length != 0) {
                                            for (int i = 0; i < sequences.length; i++) {
                                    %>
                                    <option value="<%=sequences[i]%>">
                                        <%=sequences[i]%>
                                    </option>
                                    <%
                                            }
                                        }
                                    %>
                                </select>
                            </td>
                        </tr>
                    </table>
                </div>
            </div>
        </td>
    </tr>
    </tbody>
</table>
<br/>
</table>
</tr>
</table>
<br/>
<input type="button" value="<fmt:message key="update"/>"
       class="button" name="updateBtn" onclick="updateResource()"/>
</fmt:bundle>
