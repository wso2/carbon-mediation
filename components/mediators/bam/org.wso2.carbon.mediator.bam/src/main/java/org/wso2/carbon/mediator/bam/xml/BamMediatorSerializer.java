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

package org.wso2.carbon.mediator.bam.xml;

import org.apache.synapse.config.xml.AbstractMediatorSerializer;
import org.apache.synapse.Mediator;
import org.apache.axiom.om.OMElement;
import org.wso2.carbon.mediator.bam.BamMediator;

/**
 * Extracts the current message payload/header data according to the given configuration.
 * Extracted information is sent as an event.
 */
public class BamMediatorSerializer extends AbstractMediatorSerializer {

    public OMElement serializeSpecificMediator(Mediator mediator) {
        assert mediator instanceof BamMediator : "BAM mediator is expected";

        BamMediator bamMediator = (BamMediator) mediator;
        OMElement bam = fac.createOMElement("bam", synNS);

        OMElement serverProfileElement = fac.createOMElement("serverProfile", synNS);
        serverProfileElement.addAttribute(fac.createOMAttribute("name", nullNS, bamMediator.getServerProfile().split("/")[bamMediator.getServerProfile().split("/").length-1]));

        OMElement streamConfigElement = fac.createOMElement("streamConfig", synNS);
        streamConfigElement.addAttribute(fac.createOMAttribute("name", nullNS, bamMediator.getStream().getStreamConfiguration().getName()));
        streamConfigElement.addAttribute(fac.createOMAttribute("version", nullNS, bamMediator.getStream().getStreamConfiguration().getVersion()));
        serverProfileElement.addChild(streamConfigElement);

        bam.addChild(serverProfileElement);

        return bam;
    }

    public String getMediatorClassName() {
        return BamMediator.class.getName();
    }
}
