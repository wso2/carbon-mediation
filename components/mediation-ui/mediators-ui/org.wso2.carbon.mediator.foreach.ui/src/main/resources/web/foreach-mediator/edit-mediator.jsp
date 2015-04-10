<%@ page language="java" contentType="text/html; charset=ISO-8859-1"
	pageEncoding="ISO-8859-1"%>
<%--
 ~ Copyright (c) 2014, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
 ~ 
 ~ WSO2 Inc. licenses this file to you under the Apache License,
 ~ Version 2.0 (the "License"); you may not use this file except
 ~ in compliance with the License.
 ~ You may obtain a copy of the License at
 ~ 
 ~ http://www.apache.org/licenses/LICENSE-2.0
 ~ 
 ~ Unless required by applicable law or agreed to in writing,
 ~ software distributed under the License is distributed on an
 ~ "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 ~ KIND, either express or implied. See the License for the
 ~ specific language governing permissions and limitations
 ~ under the License.
 --%>

<%@ page import="org.wso2.carbon.mediator.foreach.ForEachMediator"%>
<%@ page import="org.wso2.carbon.mediator.service.ui.Mediator"%>
<%@ page import="org.wso2.carbon.sequences.ui.util.SequenceEditorHelper"%>
<%@ page import="org.wso2.carbon.sequences.ui.util.ns.NameSpacesRegistrar"%>
<%@ taglib uri="http://java.sun.com/jsp/jstl/fmt" prefix="fmt"%>
<%@ taglib uri="http://wso2.org/projects/carbon/taglibs/carbontags.jar"
	prefix="carbon"%>

<%
	Mediator mediator = SequenceEditorHelper.getEditingMediator(
			request, session);
	if (!(mediator instanceof ForEachMediator)) {
		throw new RuntimeException("Unable to edit the mediator");
	}
	ForEachMediator foreachMediator = (ForEachMediator) mediator;

    String whichSeq = "None";

    String inRegKey = "";

    String sequenceStr = foreachMediator.getSequenceRef();
        if(sequenceStr == null){
            sequenceStr = "";
        }

        if(foreachMediator.getList().isEmpty() && foreachMediator.getSequenceRef() == null){
            whichSeq = "none";
        } else if(foreachMediator.getSequenceRef() != null && !"anon".equals(foreachMediator.getSequenceRef())) {
            whichSeq = "reg";
        } else if(foreachMediator.getSequenceRef() != null    && "anon".equals(foreachMediator.getSequenceRef())){
            whichSeq = "anon";
            //if the sequence is anonymous we have to clear the sequence string
            //in order to avoid it displaying in the registry textbox
            sequenceStr = "";
        }

%>

<fmt:bundle
	basename="org.wso2.carbon.mediator.foreach.ui.i18n.Resources">
	<carbon:jsi18n
		resourceBundle="org.wso2.carbon.mediator.foreach.ui.i18n.JSResources"
		request="<%=request%>" i18nObjectName="foreachi18n" />

<script type="text/javascript">
    var whichSeq = '<%=whichSeq%>'
</script>

	<div>
		<script type="text/javascript"
			src="../foreach-mediator/js/mediator-util.js"></script>
		<table class="normal" width="100%">
			<tr>
				<td>
					<h2>
						<fmt:message key="mediator.foreach.header" />
					</h2>
				</td>
			</tr>
			<tr>
				<td>
					<table class="normal">
						<tr>
							<td><fmt:message key="mediator.foreach.expression" /><span
								class="required">*</span></td>
							<td>
								<%
									NameSpacesRegistrar nameSpacesRegistrar = NameSpacesRegistrar
												.getInstance();
										nameSpacesRegistrar.registerNameSpaces(
												foreachMediator.getExpression(), "itr_expression",
												session);
										if (foreachMediator.getExpression() == null) {
								%> <input value="" id="itr_expression"
								name="itr_expression" type="text"> <%
 	} else {
 %> <input
								value="<%=foreachMediator.getExpression().toString()%>"
								id="itr_expression" name="itr_expression" type="text"> <%
 	}
 %>
							</td>
							<td><a href="#nsEditorLink" class="nseditor-icon-link"
								style="padding-left: 40px"
								onclick="showNameSpaceEditor('itr_expression')"><fmt:message
										key="mediator.foreach.nameSpaces" /></a></td>
						</tr>
					</table>
				</td>
			</tr>
			<tr>
			    <td>
			        <table class="normal">
                        <tr>
                            <td>
                                <h3 class="mediator">Sequence</h3>
                                <input type="hidden" name="mediator.foreach.seq.type"
                                       id="mediator.foreach.seq.type" value="none"/>
                            </td>
                        </tr>
                        <tr>
                            <td>
                                <input id="mediator.foreach.seq.radio.none" name="mediator.foreach.seq.radio"  type="radio" value="none"
                                      onclick="hideSeqRegistryOption(); seqNoneClicked();"/>
                                      None
                            </td>
                        </tr>
                        <tr>
                            <td>
                                <input id="mediator.foreach.seq.radio.anon" name="mediator.foreach.seq.radio" type="radio" onclick="hideSeqRegistryOption()"/>
                                Anonymous
                            </td>
                        </tr>
                        <tr>
                            <td>
                                <input id="mediator.foreach.seq.radio.reg" name="mediator.foreach.seq.radio" type="radio" onclick="showSeqRegistryOption()"/>
                                Pick From Registry
                            </td>
                            <td>
                                <input type="text" name="mediator.foreach.seq.reg"
                                       id="mediator.foreach.seq.reg" value="<%=sequenceStr%>"
                                       style="width:300px;display:none;"
                                       readonly="disabled" />
                            </td>
                            <td>
                                <a href="#registryBrowserLink" id="mediator.foreach.seq.reg.link_1"
                                   class="registry-picker-icon-link"
                                   onclick="showRegistryBrowser('mediator.foreach.seq.reg','/_system/config')">
                                    <fmt:message key="conf.registry.keys"/></a>
                                <a href="#registryBrowserLink" id="mediator.foreach.seq.reg.link_2"
                                   class="registry-picker-icon-link"
                                   onclick="showRegistryBrowser('mediator.foreach.seq.reg','/_system/governance')"><fmt:message
                                        key="gov.registry.keys"/></a>
                            </td>
                        </tr>
                    </table>
			    </td>
			</tr>
		</table>
		<a name="nsEditorLink"></a>
		<div id="nsEditor" style="display: none;"></div>
		<a name="registryBrowserLink"/>
                <div id="registryBrowser" style="display:none;"/>
	</div>
</fmt:bundle>