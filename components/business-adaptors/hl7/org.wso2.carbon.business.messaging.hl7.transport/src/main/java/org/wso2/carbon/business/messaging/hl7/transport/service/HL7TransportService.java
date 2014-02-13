/*
 * Copyright 2005-2008 WSO2, Inc. (http://wso2.com)
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package org.wso2.carbon.business.messaging.hl7.transport.service;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.wso2.carbon.core.transports.AbstractTransportService;
import org.wso2.carbon.core.transports.util.TransportParameter;

/**
 * 
 */
public class HL7TransportService extends AbstractTransportService {

    private static final Log log = LogFactory.getLog(HL7TransportService.class);

    public static final String TRANSPORT_NAME = "hl7";
	public static final String TRANSPORT_CONF = "hl7-transports.xml";

       /**
	 * Instantiates HL7TransportService with a reference to the AxisConfiguration.      
	 */
	public HL7TransportService() {
		super(TRANSPORT_NAME);
	}

    public boolean dependenciesAvailable(TransportParameter[] params) {
        try {
            Class.forName("ca.uhn.hl7v2.llp.LowerLayerProtocol");
            return true;
        } catch (ClassNotFoundException e) {
            log.error("HAPI LowerLayerProtocol not found");
            return false;
        }
    }
}