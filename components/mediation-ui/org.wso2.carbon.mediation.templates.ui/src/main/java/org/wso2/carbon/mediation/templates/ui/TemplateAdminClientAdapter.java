/**
 *  Copyright (c) 2009, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
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

package org.wso2.carbon.mediation.templates.ui;

import org.apache.axiom.om.OMElement;
import org.apache.axis2.AxisFault;
import org.apache.axis2.client.Options;
import org.apache.axis2.client.ServiceClient;
import org.apache.axis2.context.ConfigurationContext;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.wso2.carbon.CarbonConstants;
import org.wso2.carbon.mediation.service.templates.TemplateMediator;
import org.wso2.carbon.mediation.templates.stub.types.TemplateAdminServiceStub;
import org.wso2.carbon.mediation.templates.stub.types.common.TemplateInfo;
import org.wso2.carbon.mediator.service.builtin.SequenceMediator;
import org.wso2.carbon.sequences.common.SequenceEditorException;
import org.wso2.carbon.sequences.common.to.SequenceInfo;
import org.wso2.carbon.sequences.ui.client.SequenceAdminClient;
import org.wso2.carbon.ui.CarbonUIUtil;
import org.wso2.carbon.utils.ServerConstants;

import javax.servlet.ServletConfig;
import javax.servlet.http.HttpSession;
import java.util.ArrayList;
import java.util.List;

/**
 * Client class which is being called by the <em>template-editor</em> component user interface JSPs.
 * This class presents itself as an adapter on the sequence editor admin service
 */
public class TemplateAdminClientAdapter extends SequenceAdminClient {

    private static final Log log = LogFactory.getLog(TemplateAdminClientAdapter.class);
    private TemplateAdminServiceStub templateAdminStub;

    public TemplateAdminClientAdapter(ServletConfig config, HttpSession session) throws AxisFault {
        super(config, session);
        String backendServerURL = CarbonUIUtil.getServerURL(config.getServletContext(), session);
        ConfigurationContext configContext = (ConfigurationContext)
                config.getServletContext().getAttribute(CarbonConstants.CONFIGURATION_CONTEXT);
        String cookie = (String) session.getAttribute(ServerConstants.ADMIN_SERVICE_COOKIE);
        String serviceURL = backendServerURL + "TemplateAdminService";
        templateAdminStub = new TemplateAdminServiceStub(configContext, serviceURL);
        ServiceClient client = templateAdminStub._getServiceClient();
        Options option = client.getOptions();
        option.setManageSession(true);
        option.setProperty(org.apache.axis2.transport.http.HTTPConstants.COOKIE_STRING, cookie);
    }

    public SequenceInfo[] getSequences(int pageNumber, int sequencePerPage)
            throws SequenceEditorException {
        return getTemplates(pageNumber, sequencePerPage);
    }

    public SequenceInfo[] getTemplates(int pageNumber, int templatesPerPage)
            throws SequenceEditorException {
        List<SequenceInfo> sequences = new ArrayList<SequenceInfo>();
        try {
            TemplateInfo[] temp =
                    templateAdminStub.getTemplates(pageNumber, templatesPerPage);
            if (temp == null || temp.length == 0 || temp[0] == null) {
                return null;
            }

            for (TemplateInfo info : temp) {
                SequenceInfo seqInfo = new SequenceInfo();
                seqInfo.setEnableStatistics(info.getEnableStatistics());
                seqInfo.setEnableTracing(info.getEnableTracing());
                seqInfo.setName(info.getName());
                seqInfo.setDescription(info.getDescription());
                seqInfo.setArtifactContainerName(info.getArtifactContainerName());
                seqInfo.setIsEdited(info.getIsEdited());
                sequences.add(seqInfo);
            }
        } catch (Exception e) {
            handleException("Couldn't retrieve the information of the templates", e);
        }

        if (sequences.size() > 0) {
            return sequences.toArray(new SequenceInfo[sequences.size()]);
        }
        return null;
    }


    public int getSequencesCount() throws SequenceEditorException {
        return getTemplatesCount();
    }

    public int getTemplatesCount() throws SequenceEditorException {
        try {
            return templateAdminStub.getTemplatesCount();
        } catch (Exception e) {
            handleException("Couldn't retrieve the template element count", e);
        }
        return 0;
    }

    private OMElement getTemplate(String templateName) throws SequenceEditorException {
        OMElement element = null;
        try {
            element = templateAdminStub.getTemplate(templateName).getFirstElement();
        } catch (Exception e) {
            handleException("Couldn't retrieve the template element with name '"
                            + templateName + "'", e);
        }
        return element;
    }

    public void saveSequence(SequenceMediator sequence) throws SequenceEditorException {
        saveTemplate(sequence);
    }

    public void saveTemplate(SequenceMediator template) throws SequenceEditorException {
        OMElement sequenceElem = template.serialize(null);
        try {
            templateAdminStub.saveTemplate(sequenceElem);
        } catch (Exception e) {
            handleException("Error in saving the template with the configuration '"
                            + sequenceElem + "'", e);
        }
    }

    public void addSequence(SequenceMediator sequence) throws SequenceEditorException {
        addTemplate(sequence);
    }

    public void addTemplate(SequenceMediator template) throws SequenceEditorException {
        OMElement sequenceElem = template.serialize(null);
        try {
            templateAdminStub.addTemplate(sequenceElem);
        } catch (Exception e) {
            handleException("Error in adding the template with the configuration '"
                            + sequenceElem + "'", e);
        }
    }

    public void addDynamicSequence(String key, SequenceMediator sequence)
            throws SequenceEditorException {
        addDynamicTemplate(key, sequence);
    }

    public void addDynamicTemplate(String key, SequenceMediator sequence)
            throws SequenceEditorException {
        OMElement sequenceElem = sequence.serialize(null);
        try {
            templateAdminStub.addDynamicTemplate(key, sequenceElem);
        } catch (Exception e) {
            handleException("Error in adding dynamic template with configuration '"
                            + sequenceElem + "' to the registry with key '" + key + "'", e);
        }
    }

    public OMElement getDynamicSequence(String key) throws SequenceEditorException {
        return getDynamicTemplate(key);
    }

    public OMElement getDynamicTemplate(String key) throws SequenceEditorException {
        OMElement dynamicSequence = null;
        try {
            dynamicSequence = templateAdminStub.getDynamicTemplate(key);
        } catch (Exception e) {
            handleException("Couldn't get dynamic template with key '" + key + "'", e);
        }
        return dynamicSequence;
    }

    public void saveDynamicSequence(String key, SequenceMediator sequence)
            throws SequenceEditorException {
        saveDynamicTemplate(key, sequence);
    }

    public void saveDynamicTemplate(String key, SequenceMediator template)
            throws SequenceEditorException {
        OMElement sequenceElem = template.serialize(null);
        try {
            templateAdminStub.saveDynamicTemplate(key, sequenceElem);
        } catch (Exception e) {
            handleException("Error in saving dynamic template with configuration '"
                            + sequenceElem + "' for key '" + key + "'", e);
        }
    }

    public String enableTracing(String templateName) throws SequenceEditorException {
        String state = null;
        try {
            state = templateAdminStub.enableTracing(templateName);
        } catch (Exception e) {
            handleException("Couldn't enable tracing for the template '"
                            + templateName + "'", e);
        }
        return state;
    }

    public String disableTracing(String templateName) throws SequenceEditorException {
        String state = null;
        try {
            state = templateAdminStub.disableTracing(templateName);
        } catch (Exception e) {
            handleException("Couldn't disable tracing for the template '"
                            + templateName + "'", e);
        }
        return state;
    }

    public String enableStatistics(String templateName) throws SequenceEditorException {
        String state = null;
        try {
            state = templateAdminStub.enableStatistics(templateName);
        } catch (Exception e) {
            handleException("Couldn't enable statistics for the template '"
                            + templateName + "'", e);
        }
        return state;
    }

    public String disableStatistics(String templateName) throws SequenceEditorException {
        String state = null;
        try {
            state = templateAdminStub.disableStatistics(templateName);
        } catch (Exception e) {
            handleException("Couldn't disable statistics for the template '"
                            + templateName + "'", e);
        }
        return state;
    }

    public void deleteSequence(String sequenceName) throws SequenceEditorException {
        deleteTemplate(sequenceName);
    }

    public void deleteTemplate(String templateName) throws SequenceEditorException {
        try {
            templateAdminStub.deleteTemplate(templateName);
        } catch (Exception e) {
            handleException("Couldn't delete the sequence '" + templateName + "'", e);
        }
    }

/*
    public String getEntryNamesString() throws SequenceEditorException {
        String localRegistryKeys = null;
        try {
            localRegistryKeys = templateAdminStub.getEntryNamesString();
        } catch (Exception e) {
            handleException("Couldn't get local registry Keys", e);
        }
        return localRegistryKeys;
    }
*/

    public void updateDynamicSequence(String key, SequenceMediator sequence)
            throws SequenceEditorException {
        updateDynamicTemplate(key, sequence);
    }

    public void updateDynamicTemplate(String key, SequenceMediator template)
            throws SequenceEditorException {
        try {
            OMElement templateElem = template.serialize(null);
            templateAdminStub.updateDynamicTemplate(key, templateElem);
        } catch (Exception e) {
            handleException("Couldn't update template with key '" + key + "'", e);
        }
    }

    public void deleteDynamicSequence(String key) throws SequenceEditorException {
        deleteDynamicTemplate(key);
    }

    public void deleteDynamicTemplate(String key) throws SequenceEditorException {
        try {
            templateAdminStub.deleteDynamicTemplate(key);
        } catch (Exception e) {
            handleException("Couldn't delete template with key '" + key + "'", e);
        }
    }

    public SequenceInfo[] getDynamicSequences(int pageNumber, int sequencePerPage)
            throws SequenceEditorException {
        return getDynamicTemplates(pageNumber, sequencePerPage);
    }

    public SequenceInfo[] getDynamicTemplates(int pageNumber, int sequencePerPage)
            throws SequenceEditorException {
        List<SequenceInfo> sequences = new ArrayList<SequenceInfo>();
        try {
            TemplateInfo[] temp =
                    templateAdminStub.getDynamicTemplates(pageNumber, sequencePerPage);
            if (temp == null || temp.length == 0 || temp[0] == null) {
                return null;
            }

            for (TemplateInfo info : temp) {
                SequenceInfo seqInfo = new SequenceInfo();
                seqInfo.setName(info.getName());
                sequences.add(seqInfo);
            }
        } catch (Exception e) {
            handleException("Couldn't retrieve the information of the templates", e);
        }

        if (sequences.size() > 0) {
            return sequences.toArray(new SequenceInfo[sequences.size()]);
        }
        return null;
    }

    public int getDynamicSequenceCount() throws SequenceEditorException {
        return getDynamicTemplateCount();
    }

    public int getDynamicTemplateCount() throws SequenceEditorException {
        try {
            return templateAdminStub.getDynamicTemplateCount();
        } catch (Exception e) {
            handleException("Couldn't retrieve the dynamic template element count", e);
        }
        return 0;
    }

/*
    public ConfigurationObject[] getDependents(String sequence) throws SequenceEditorException {
        try {
            org.wso2.carbon.sequences.ui.types.common.to.ConfigurationObject[] tempDependents =
                    templateAdminStub.getDependents(sequence);
            if (tempDependents != null && tempDependents.length > 0 && tempDependents[0] != null) {
                ConfigurationObject[] dependents = new ConfigurationObject[tempDependents.length];
                for (int i = 0; i < dependents.length; i++) {
                    dependents[i] = new ConfigurationObject(tempDependents[i].getType(),
                            tempDependents[i].getResourceId());
                }
                return dependents;
            }
        } catch (Exception e) {
            handleException("Couldn't get the dependents for the sequence : " + sequence, e);
        }
        return null;
    }
*/

    public SequenceMediator getSequenceMediator(String sequenceName)
            throws SequenceEditorException {
        return getTemplateMediator(sequenceName);
    }

    public SequenceMediator getTemplateMediator(String templateName)
            throws SequenceEditorException {
        OMElement ele = getTemplate(templateName);
        if (ele != null) {
            SequenceMediator sequence = new TemplateMediator();
            sequence.build(ele);
            return sequence;
        }
        return null;
    }

    private void handleException(String message, Throwable e) throws SequenceEditorException {
        log.error(message, e);
        throw new SequenceEditorException(message, e);
    }
}
