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

package org.wso2.carbon.sequences.ui.client;

import org.apache.axiom.om.OMElement;
import org.apache.axis2.AxisFault;
import org.apache.axis2.client.Options;
import org.apache.axis2.client.ServiceClient;
import org.apache.axis2.context.ConfigurationContext;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.wso2.carbon.CarbonConstants;
import org.wso2.carbon.mediator.service.builtin.SequenceMediator;
import org.wso2.carbon.sequences.common.SequenceEditorException;
import org.wso2.carbon.sequences.common.to.ConfigurationObject;
import org.wso2.carbon.sequences.common.to.SequenceInfo;
import org.wso2.carbon.sequences.stub.types.SequenceAdminServiceStub;
import org.wso2.carbon.ui.CarbonUIUtil;
import org.wso2.carbon.utils.ServerConstants;

import javax.servlet.ServletConfig;
import javax.servlet.http.HttpSession;
import java.util.ArrayList;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Client class which is being called by the <em>sequence-editor</em> component user interface JSPs.
 * This represents client invocation methods for the sequence editor admin service
 */
public class SequenceAdminClient implements EditorUIClient {

    private static final Log log = LogFactory.getLog(SequenceAdminClient.class);
    private SequenceAdminServiceStub sequenceAdminStub;

    public SequenceAdminClient(ServletConfig config, HttpSession session) throws AxisFault {

        String backendServerURL = CarbonUIUtil.getServerURL(config.getServletContext(), session);
        ConfigurationContext configContext = (ConfigurationContext)
                config.getServletContext().getAttribute(CarbonConstants.CONFIGURATION_CONTEXT);
        String cookie = (String) session.getAttribute(ServerConstants.ADMIN_SERVICE_COOKIE);
        String serviceURL = backendServerURL + "SequenceAdminService";
        sequenceAdminStub = new SequenceAdminServiceStub(configContext, serviceURL);
        ServiceClient client = sequenceAdminStub._getServiceClient();
        Options option = client.getOptions();
        option.setManageSession(true);
        option.setProperty(org.apache.axis2.transport.http.HTTPConstants.COOKIE_STRING, cookie);
    }

    public SequenceInfo[] getSequences(int pageNumber, int sequencePerPage)
            throws SequenceEditorException {
        List<SequenceInfo> sequences = new ArrayList<SequenceInfo>();
        try {
            org.wso2.carbon.sequences.stub.types.common.to.SequenceInfo[] temp =
                    sequenceAdminStub.getSequences(pageNumber, sequencePerPage);
            if (temp == null || temp.length == 0 || temp[0] == null) {
                return null;
            }

            for (org.wso2.carbon.sequences.stub.types.common.to.SequenceInfo info : temp) {
                SequenceInfo seqInfo = new SequenceInfo();
                seqInfo.setEnableStatistics(info.getEnableStatistics());
                seqInfo.setEnableTracing(info.getEnableTracing());
                seqInfo.setName(info.getName());
                seqInfo.setDescription(info.getDescription());
                sequences.add(seqInfo);
            }
        } catch (Exception e) {
            handleException("Couldn't retrieve the information of the sequences", e);
        }

        if (sequences.size() > 0) {
            return sequences.toArray(new SequenceInfo[sequences.size()]);
        }
        return null;
    }

    private boolean isServiceSatisfySearchString(String searchString,String sequenceName) {
        if (searchString != null) {
            String regex = searchString.toLowerCase().
                    replace("..?", ".?").replace("..*", ".*").
                    replaceAll("\\?", ".?").replaceAll("\\*", ".*?");

            Pattern pattern = Pattern.compile(regex);
            Matcher matcher = pattern.matcher(sequenceName.toLowerCase());

            return regex.trim().length() == 0 || matcher.find();
        }
        return false;
    }

    public SequenceInfo[] getSequencesSearch(int pageNumber, int sequencePerPage, String searchText)
            throws SequenceEditorException {
        List<SequenceInfo> sequences = new ArrayList<SequenceInfo>();
        try {
            org.wso2.carbon.sequences.stub.types.common.to.SequenceInfo[] temp =
                    sequenceAdminStub.getSequences(pageNumber, sequenceAdminStub.getSequencesCount());
            if (temp == null || temp.length == 0 || temp[0] == null) {
                return null;
            }

            for (org.wso2.carbon.sequences.stub.types.common.to.SequenceInfo info : temp) {
                SequenceInfo seqInfo = new SequenceInfo();
                seqInfo.setEnableStatistics(info.getEnableStatistics());
                seqInfo.setEnableTracing(info.getEnableTracing());
                seqInfo.setName(info.getName());
                seqInfo.setDescription(info.getDescription());
                if (this.isServiceSatisfySearchString(searchText, seqInfo.getName())) {
                    sequences.add(seqInfo);
                }
            }
        } catch (Exception e) {
            handleException("Couldn't retrieve the information of the sequences", e);
        }

        if (sequences.size() > 0) {
            return sequences.toArray(new SequenceInfo[sequences.size()]);
        }
        return null;
    }

    public int getSequencesCount() throws SequenceEditorException {
        try {
            return sequenceAdminStub.getSequencesCount();
        } catch (Exception e) {
            handleException("Couldn't retrieve the sequence element count", e);
        }
        return 0;
    }
    private OMElement getSequence(String sequenceName) throws SequenceEditorException {
        OMElement element = null;
        try {
            element = sequenceAdminStub.getSequence(sequenceName).getFirstElement();
        } catch (Exception e) {
            handleException("Couldn't retrieve the sequence element with name '"
                    + sequenceName + "'", e);
        }
        return element;
    }

    public void saveSequence(SequenceMediator sequence) throws SequenceEditorException {
        OMElement sequenceElem = sequence.serialize(null);
        try {
            sequenceAdminStub.saveSequence(sequenceElem);
        } catch (Exception e) {
            handleException("Error in saving the sequence with the configuration '"
                    + sequenceElem + "'", e);
        }
    }

    public void addSequence(SequenceMediator sequence) throws SequenceEditorException {
        OMElement sequenceElem = null;
        try {
            sequenceElem = sequence.serialize(null);
            sequenceAdminStub.addSequence(sequenceElem);
        } catch (NullPointerException e) {
            handleException("Error in adding the sequence with the configuration '" + sequenceElem + "' : Please make" +
                    " sure to click the update button after configuring a mediator.", e);
        } catch (Exception e) {
            if (e.getMessage().toString().toLowerCase().contains("this name already exists".toLowerCase())) {
                //Error Related to name already exits
                handleException(e.getMessage(), e);

            } else {
                handleException("Error in adding the sequence with the configuration '"
                        + sequenceElem + "'. " + e.getMessage(), e);
            }
        }
    }

    public void addDynamicSequence(String key, SequenceMediator sequence)
            throws SequenceEditorException {
        OMElement sequenceElem = sequence.serialize(null);
        try {
            sequenceAdminStub.addDynamicSequence(key, sequenceElem);
        } catch (Exception e) {
            handleException("Error in adding dynamic sequence with configuration '"
                    + sequenceElem + "' to the registry with key '" + key + "'", e);
        }
    }

    public OMElement getDynamicSequence(String key) throws SequenceEditorException {
        OMElement dynamicSequence = null;
        try {
            dynamicSequence = sequenceAdminStub.getDynamicSequence(key);
        } catch (Exception e) {
            handleException("Couldn't get dynamic sequence with key '" + key + "'", e);
        }
        return dynamicSequence;
    }

    public void saveDynamicSequence(String key, SequenceMediator sequence)
            throws SequenceEditorException {
        OMElement sequenceElem = sequence.serialize(null);
        try {
            sequenceAdminStub.saveDynamicSequence(key, sequenceElem);
        } catch (Exception e) {
            handleException("Error in saving dynamic sequence with configuration '"
                    + sequenceElem + "' for key '" + key + "'", e);
        }
    }

    public String enableTracing(String sequenceName) throws SequenceEditorException {
        String state = null;
        try {
            state = sequenceAdminStub.enableTracing(sequenceName);
        } catch (Exception e) {
            handleException("Couldn't enable tracing for the sequence '"
                    + sequenceName + "'", e);
        }
        return state;
    }

    public String disableTracing(String sequenceName) throws SequenceEditorException {
        String state = null;
        try {
            state = sequenceAdminStub.disableTracing(sequenceName);
        } catch (Exception e) {
            handleException("Couldn't disable tracing for the sequence '"
                    + sequenceName + "'", e);
        }
        return state;
    }

    public String enableStatistics(String sequenceName) throws SequenceEditorException {
        String state = null;
        try {
            state = sequenceAdminStub.enableStatistics(sequenceName);
        } catch (Exception e) {
            handleException("Couldn't enable statistics for the sequence '"
                    + sequenceName + "'", e);
        }
        return state;
    }

    public String disableStatistics(String sequenceName) throws SequenceEditorException {
        String state = null;
        try {
            state = sequenceAdminStub.disableStatistics(sequenceName);
        } catch (Exception e) {
            handleException("Couldn't disable statistics for the sequence '"
                    + sequenceName + "'", e);
        }
        return state;
    }

    public void deleteSequence(String sequenceName) throws SequenceEditorException {
        try {
            sequenceAdminStub.deleteSequence(sequenceName);
        } catch (Exception e) {
            handleException("Couldn't delete the sequence '" + sequenceName + "'", e);
        }
    }

    public String getEntryNamesString() throws SequenceEditorException {
        String localRegistryKeys = null;
        try {
            localRegistryKeys = sequenceAdminStub.getEntryNamesString();
        } catch (Exception e) {
            handleException("Couldn't get local registry Keys", e);
        }
        return localRegistryKeys;
    }

    public void updateDynamicSequence(String key, SequenceMediator sequence)
            throws SequenceEditorException {
        try {
            OMElement sequenceElem = sequence.serialize(null);
            sequenceAdminStub.deleteDynamicSequence(key);
            sequenceAdminStub.addDynamicSequence(key, sequenceElem);
        } catch (Exception e) {
            handleException("Couldn't update sequence with key '" + key + "'", e);
        }
    }

    public void deleteDynamicSequence(String key) throws SequenceEditorException {
        try {
            sequenceAdminStub.deleteDynamicSequence(key);
        } catch (Exception e) {
            handleException("Couldn't delete sequence with key '" + key + "'", e);
        }
    }

    public SequenceInfo[] getDynamicSequences(int pageNumber, int sequencePerPage)
            throws SequenceEditorException {
        List<SequenceInfo> sequences = new ArrayList<SequenceInfo>();
        try {
            org.wso2.carbon.sequences.stub.types.common.to.SequenceInfo[] temp =
                    sequenceAdminStub.getDynamicSequences(pageNumber, sequencePerPage);
            if (temp == null || temp.length == 0 || temp[0] == null) {
                return null;
            }

            for (org.wso2.carbon.sequences.stub.types.common.to.SequenceInfo info : temp) {
                SequenceInfo seqInfo = new SequenceInfo();
                seqInfo.setName(info.getName());
                sequences.add(seqInfo);
            }
        } catch (Exception e) {
            handleException("Couldn't retrieve the information of the sequences", e);
        }

        if (sequences.size() > 0) {
            return sequences.toArray(new SequenceInfo[sequences.size()]);
        }
        return null;
    }

    public SequenceInfo[] getDynamicSequencesSearch(int pageNumber, int sequencePerPage, String searchText)
            throws SequenceEditorException {
        List<SequenceInfo> sequences = new ArrayList<SequenceInfo>();
        try {
            org.wso2.carbon.sequences.stub.types.common.to.SequenceInfo[] temp =
                    sequenceAdminStub.getDynamicSequences(pageNumber, sequencePerPage);
            if (temp == null || temp.length == 0 || temp[0] == null) {
                return null;
            }

            for (org.wso2.carbon.sequences.stub.types.common.to.SequenceInfo info : temp) {
                SequenceInfo seqInfo = new SequenceInfo();
                seqInfo.setName(info.getName());
                if (this.isServiceSatisfySearchString(searchText, seqInfo.getName())) {
                    sequences.add(seqInfo);
                }
            }
        } catch (Exception e) {
            handleException("Couldn't retrieve the information of the sequences", e);
        }

        if (sequences.size() > 0) {
            return sequences.toArray(new SequenceInfo[sequences.size()]);
        }
        return null;
    }

    public int getDynamicSequenceCount() throws SequenceEditorException {
        try {
            return sequenceAdminStub.getDynamicSequenceCount();
        } catch (Exception e) {
            handleException("Couldn't retrieve the dynamic sequence element count", e);
        }
        return 0;
    }

    public ConfigurationObject[] getDependents(String sequence) throws SequenceEditorException {
        try {
            org.wso2.carbon.sequences.stub.types.common.to.ConfigurationObject[] tempDependents =
                    sequenceAdminStub.getDependents(sequence);
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

    public SequenceMediator getSequenceMediator(String sequenceName)
            throws SequenceEditorException {
        OMElement ele = getSequence(sequenceName);
        if (ele != null) {
            SequenceMediator sequence = new SequenceMediator();
            sequence.build(ele);
            return sequence;
        }
        return null;
    }

    private void handleException(String message, Throwable e) throws SequenceEditorException {
        log.error(message, e);
        throw new SequenceEditorException(message, e);
    }
    /**
     * Delete selected sequences from synapse configuration
     *
     * @param SequenceNames
     * @throws Exception
     */
    public void deleteSelectedSequence(String[] SequenceNames) throws Exception {
        try {
            sequenceAdminStub.deleteSelectedSequence(SequenceNames);

        } catch (Exception e) {
            handleException("Couldn't delete Selected sequences ", e);
        }
    }
    /**
     * Delete all Sequences in the synapse configuration
     *
     * @throws Exception
     */
    public void deleteAllSequenceGroups() throws Exception {
        try {
            sequenceAdminStub.deleteAllSequence();
        } catch (Exception e) {
            handleException("Couldn't delete all sequences ", e);
        }
    }
}
