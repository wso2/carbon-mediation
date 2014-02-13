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
package org.wso2.carbon.mediation.templates.common.factory;

import org.apache.axiom.om.OMElement;
import org.apache.synapse.SynapseConstants;
import org.apache.synapse.endpoints.Template;
import org.apache.synapse.mediators.template.TemplateMediator;
import org.wso2.carbon.mediation.templates.common.EndpointTemplateInfo;
import org.wso2.carbon.mediation.templates.common.TemplateInfo;

import javax.xml.namespace.QName;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.Comparator;

public class TemplateInfoFactory {

    /**
     * Creates an instance of {@link org.wso2.carbon.mediation.templates.common.TemplateInfo} from the
     * provided <code>sequenceMediator</code>
     *
     * @param sequenceMediator of which the {@link org.wso2.carbon.mediation.templates.common.TemplateInfo}
     * object needs to be created
     * @return info object relevant to the sequenceMediator
     */
    public static TemplateInfo createTemplateInfo(TemplateMediator sequenceMediator) {

        TemplateInfo templateInfo = new TemplateInfo();
        templateInfo.setName(sequenceMediator.getName());
        templateInfo.setDescription(sequenceMediator.getDescription());

        if (sequenceMediator.isStatisticsEnable()) {
            templateInfo.setEnableStatistics(true);
        } else {
            templateInfo.setEnableStatistics(false);
        }

        if(sequenceMediator.getTraceState() == SynapseConstants.TRACING_ON) {
            templateInfo.setEnableTracing(true);
        } else {
            templateInfo.setEnableTracing(false);
        }

        return templateInfo;
    }

    public static EndpointTemplateInfo createTemplateInfo(Template endpointTemplate) {

        EndpointTemplateInfo templateInfo = new EndpointTemplateInfo();
        templateInfo.setTemplateName(endpointTemplate.getName());
        templateInfo.setDescription(endpointTemplate.getDescription());
        templateInfo.setEndpointType(getEndpointTypeFromTemplate(endpointTemplate.getElement()));
        return templateInfo;
    }

    public static String getEndpointTypeFromTemplate(OMElement templateEl) {
        String type = "none";
        OMElement addressElement = templateEl.getFirstChildWithName(new QName(SynapseConstants.SYNAPSE_NAMESPACE,
                                                                              "address"));
        if (addressElement != null) {
            type = "address";
            return type;
        }

        OMElement wsdlElement = templateEl.getFirstChildWithName(new QName(SynapseConstants.SYNAPSE_NAMESPACE,
                                                                           "wsdl"));
        if (wsdlElement != null) {
            type = "wsdl";
            return type;
        }

        OMElement defaultElement = templateEl.getFirstChildWithName(new QName(SynapseConstants.SYNAPSE_NAMESPACE,
                                                                              "default"));
        if (defaultElement != null) {
            type = "default";
            return type;
        }

        OMElement lbElement = templateEl.getFirstChildWithName(new QName(SynapseConstants.SYNAPSE_NAMESPACE,
                                                                         "loadbalance"));
        if (lbElement != null) {
            type = "loadbalance";
            return type;
        }

        OMElement httpElement = templateEl.getFirstChildWithName(new QName(SynapseConstants.SYNAPSE_NAMESPACE,
                                                                            "http"));
        if (httpElement != null) {
            type = "http";
            return type;
        }

        return type;
    }


    /**
     * Creates an array of {@link org.wso2.carbon.mediation.templates.common.TemplateInfo} instances
     * corresponds to the <code>templateMediators</code> collection
     *
     * @param templateMediators of which the
     * {@link org.wso2.carbon.mediation.templates.common.TemplateInfo} array needs to be created
     * @return an array of TemplateInfo instances representing the sequences
     */
    public static TemplateInfo[] getSortedTemplateInfoArray(
            Collection<TemplateMediator> templateMediators) {

        ArrayList<TemplateInfo> templateInfoList = new ArrayList<TemplateInfo>();
        for (TemplateMediator templateMediator : templateMediators) {
            templateInfoList.add(createTemplateInfo(templateMediator));
        }

        Collections.sort(templateInfoList, new Comparator<TemplateInfo>() {
            public int compare(TemplateInfo info1, TemplateInfo info2) {
                return info1.getName().compareToIgnoreCase(info2.getName());
            }
        });

        return templateInfoList.toArray(new TemplateInfo[templateInfoList.size()]);
    }

    public static EndpointTemplateInfo[] getSortedTemplateInfoArray(
            Collection<Template> templateMediators) {

        ArrayList<EndpointTemplateInfo> templateInfoList = new ArrayList<EndpointTemplateInfo>();
        for (Template templateMediator : templateMediators) {
            templateInfoList.add(createTemplateInfo(templateMediator));
        }

        Collections.sort(templateInfoList, new Comparator<EndpointTemplateInfo>() {
            public int compare(EndpointTemplateInfo info1, EndpointTemplateInfo info2) {
                return info1.getTemplateName().compareToIgnoreCase(info2.getTemplateName());
            }
        });

        return templateInfoList.toArray(new EndpointTemplateInfo[templateInfoList.size()]);
    }

}
