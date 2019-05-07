/*
 * Copyright (c) 2019, WSO2 Inc. (http://wso2.com) All Rights Reserved.
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

package org.wso2.carbon.mediator.enrich.ui;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.synapse.config.xml.SynapsePath;
import org.apache.synapse.util.xpath.SynapseXPath;
import org.wso2.carbon.sequences.ui.util.ns.NameSpacesRegistrar;

import javax.servlet.http.HttpSession;

/**
 * This class is used for util functions in Enrich mediator UI.
 */
public class EnrichMediatorUtil {

    private static Log log = LogFactory.getLog(EnrichMediatorUtil.class);

    /**
     * Given the session and synapsePath this method will register namespaces and return the string value.
     *
     * @param session     session from management console.
     * @param synapsePath Xpath or JSONPath.
     * @param id identifier.
     * @return String value of synapsePath.
     */
    public static String getSynapsePathString(HttpSession session, SynapsePath synapsePath, String id) {
        String targetValue = "";
        if (synapsePath != null) {
            if (synapsePath instanceof SynapseXPath) {
                NameSpacesRegistrar nameSpacesRegistrar = NameSpacesRegistrar.getInstance();
                nameSpacesRegistrar.registerNameSpaces(synapsePath, id, session);
            }
            targetValue = synapsePath.toString();
        } else {
            log.warn("Empty expression given for source/ target expression");
        }
        return targetValue;
    }

}
