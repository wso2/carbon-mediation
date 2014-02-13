/*
*  Copyright (c) 2005-2010, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
*
*  WSO2 Inc. licenses this file to you under the Apache License,
*  Version 2.0 (the "License"); you may not use this file except
*  in compliance with the License.
*  You may obtain a copy of the License at
*
*    http://www.apache.org/licenses/LICENSE-2.0
*
* Unless required by applicable law or agreed to in writing,
* software distributed under the License is distributed on an
* "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
* KIND, either express or implied.  See the License for the
* specific language governing permissions and limitations
* under the License.
*/
package org.wso2.carbon.sequences.ui.client;

import org.apache.axiom.om.OMElement;
import org.wso2.carbon.mediator.service.builtin.SequenceMediator;
import org.wso2.carbon.sequences.common.SequenceEditorException;
import org.wso2.carbon.sequences.common.to.ConfigurationObject;
import org.wso2.carbon.sequences.common.to.SequenceInfo;

public interface EditorUIClient<T extends SequenceMediator> {

    public SequenceInfo[] getSequences(int pageNumber, int sequencePerPage)
            throws SequenceEditorException;

    public int getSequencesCount() throws SequenceEditorException;

    public void saveSequence(T sequence) throws SequenceEditorException;

    public void addSequence(T sequence) throws SequenceEditorException;

    public void addDynamicSequence(String key, T sequence)
            throws SequenceEditorException;

    public OMElement getDynamicSequence(String key) throws SequenceEditorException;

    public void saveDynamicSequence(String key, SequenceMediator sequence)
            throws SequenceEditorException;

    public String enableTracing(String sequenceName) throws SequenceEditorException;

    public String disableTracing(String sequenceName) throws SequenceEditorException;

    public String enableStatistics(String sequenceName) throws SequenceEditorException;

    public String disableStatistics(String sequenceName) throws SequenceEditorException;

    public void deleteSequence(String sequenceName) throws SequenceEditorException;

    public String getEntryNamesString() throws SequenceEditorException;

    public void updateDynamicSequence(String key, SequenceMediator sequence)
            throws SequenceEditorException;

    public void deleteDynamicSequence(String key) throws SequenceEditorException;

    public SequenceInfo[] getDynamicSequences(int pageNumber, int sequencePerPage)
            throws SequenceEditorException;

    public int getDynamicSequenceCount() throws SequenceEditorException;

    public ConfigurationObject[] getDependents(String sequence) throws SequenceEditorException;

    public SequenceMediator getSequenceMediator(String sequenceName)
            throws SequenceEditorException ;

}
