/*
 *  Copyright (c) 2012, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
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

package org.wso2.carbon.endpoint.ui.endpoints;

import org.apache.axiom.om.OMElement;
import org.apache.synapse.config.xml.endpoints.DefinitionFactory;
import org.apache.synapse.endpoints.Template;

/**
 * This interface provides the basic structure of a endpoint for the endpoint editor ui,
 */
public interface IEndpoint {

    /**
     * Serialize this endpoint in to the corresponding Synapse endpoint configuration.
     *
     * @param parent if present the serialize node will be added to the parent
     * @return the Synapse endpoint configuration XML
     */
    public OMElement serialize(OMElement parent);

    /**
     * Populate this endpoint from the Synapse endpoint configuration.
     *
     * @param elem        synapse endpoint configuration
     * @param isAnonymous whether the endpoint has a name
     */
    public void build(OMElement elem, boolean isAnonymous);

    /**
     * Populate this endpoint from the Synapse endpoint template configuration.
     *
     * @param template synapse endpoint template configuration
     * @param factory  Template definition factory
     */
    public void build(Template template, DefinitionFactory factory);

    /**
     * Get the tag name of the endpoint
     *
     * @return the xml element tag name
     */
    public String getTagLocalName();

}
