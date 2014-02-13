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
 **/

package org.wso2.carbon.mediator.service.ui;

import org.apache.axiom.om.OMElement;

/**
 * Defines a mediator
 */
@SuppressWarnings({"UnusedDeclaration"})
public interface Mediator {

    /**
     * Serialize this mediator in to the corresponding Synapse mediator configuration.
     * @param parent if present the serialize node will be added to the parent
     * @return the Synapse mediator configuration XML
     */
    public OMElement serialize(OMElement parent);

    /**
     * Populate this mediator from the Synapse mediator configuration.
     * @param elem synapse mediator configuration
     */
    public void build(OMElement elem);

    /**
     * Get the tracing state
     * @return tracing state
     */
    public int getTraceState();

    /**
     * Set the tracing state
     * @param traceState trace state to set
     */
    public void setTraceState(int traceState);

    /**
     * Is this mediator audit configurable. Synapse mediators which implements
     * the AuditConfigurable should return true.
     * @return true if the mediator is audit configurable.
     */
    public boolean isAuditConfigurable();

    /**
     * Get the name of the mediator
     * @return name of the mediator
     */
    public String getTagLocalName();

    /**
     * Whether statistics has been enabled
     *
     * @return True if enable , o.w , false
     */
    public boolean isStatisticsEnable();

    /**
     * Disable statistics for mediator
     */
    public void disableStatistics();

    /**
     * Enable statistics for the mediator
     */
    public void enableStatistics();
}
