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

package org.wso2.carbon.sequences.common.factory;

import org.apache.synapse.SynapseConstants;
import org.apache.synapse.mediators.base.SequenceMediator;
import org.wso2.carbon.sequences.common.to.SequenceInfo;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.Comparator;

/**
 * Factory for creating the {@link org.wso2.carbon.sequences.common.to.SequenceInfo} instances
 */
public class SequenceInfoFactory {

    /**
     * Creates an instance of {@link org.wso2.carbon.sequences.common.to.SequenceInfo} from the
     * provided <code>sequenceMediator</code>
     *
     * @param sequenceMediator of which the {@link org.wso2.carbon.sequences.common.to.SequenceInfo}
     * object needs to be created
     * @return info object relevant to the sequenceMediator
     */
    public static SequenceInfo createSequenceInfo(SequenceMediator sequenceMediator) {

        SequenceInfo sequenceInfo = new SequenceInfo();
        sequenceInfo.setName(sequenceMediator.getName());
        sequenceInfo.setDescription(sequenceMediator.getDescription());

        if (sequenceMediator.isStatisticsEnable()) {
            sequenceInfo.setEnableStatistics(true);
        } else {
            sequenceInfo.setEnableStatistics(false);
        }
        
        if(sequenceMediator.getTraceState() == SynapseConstants.TRACING_ON) {
            sequenceInfo.setEnableTracing(true);
        } else {
            sequenceInfo.setEnableTracing(false);
        }

        return sequenceInfo;
    }

    /**
     * Creates an array of {@link org.wso2.carbon.sequences.common.to.SequenceInfo} instances
     * corresponds to the <code>sequenceMediators</code> collection
     *
     * @param sequenceMediators of which the
     * {@link org.wso2.carbon.sequences.common.to.SequenceInfo} array needs to be created
     * @return an array of SequenceInfo instances representing the sequences
     */
    public static SequenceInfo[] getSortedSequenceInfoArray(
            Collection<SequenceMediator> sequenceMediators) {

        ArrayList<SequenceInfo> sequenceInfoList = new ArrayList<SequenceInfo>();
        for (SequenceMediator sequenceMediator : sequenceMediators) {
            sequenceInfoList.add(createSequenceInfo(sequenceMediator));
        }

        Collections.sort(sequenceInfoList, new Comparator<SequenceInfo>() {
            public int compare(SequenceInfo info1, SequenceInfo info2) {
                return info1.getName().compareToIgnoreCase(info2.getName());
            }
        });

        return sequenceInfoList.toArray(new SequenceInfo[sequenceInfoList.size()]);
    }

}
