/*
 *  Copyright (c) 2005-2008, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
 *
 *  WSO2 Inc. licenses this file to you under the Apache License,
 *  Version 2.0 (the "License"); you may not use this file except
 *  in compliance with the License.
 *  You may obtain a copy of the License at
 *
 *    http://www.apache.org/licenses/LICENSE-2.0
 *
 *  Unless required by applicable law or agreed to in writing,
 *  software distributed under the License is distributed on an
 *  "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 *  KIND, either express or implied.  See the License for the
 *  specific language governing permissions and limitations
 *  under the License.
 *
 */

package org.wso2.carbon.mediation.dependency.mgt.tests;

import org.apache.synapse.endpoints.Endpoint;
import org.apache.synapse.endpoints.LoadbalanceEndpoint;
import org.apache.synapse.endpoints.IndirectEndpoint;
import org.apache.synapse.endpoints.EndpointDefinition;
import org.apache.synapse.config.SynapseConfiguration;
import org.wso2.carbon.mediation.dependency.mgt.ConfigurationObject;

import java.util.List;
import java.util.ArrayList;

public class EndpointDependencyTrackingTest extends DependencyMgtTestCase {

    public void testLoadbalanceEndpoint() {

        SynapseConfiguration synapseConfig = createSynapseConfig();

        Endpoint endpoint1 = createEndpoint("foo");
        initEndpoint(endpoint1, null);
        Endpoint endpoint2 = createEndpoint("bar");
        initEndpoint(endpoint2, null);

        synapseConfig.addEndpoint(endpoint1.getName(), endpoint1);
        synapseConfig.addEndpoint(endpoint2.getName(), endpoint2);

        LoadbalanceEndpoint lb = new LoadbalanceEndpoint();
        lb.setName("lb");
        lb.setDefinition(new EndpointDefinition());

        IndirectEndpoint child1 = new IndirectEndpoint();
        child1.setKey(endpoint1.getName());
        IndirectEndpoint child2 = new IndirectEndpoint();
        child2.setKey(endpoint2.getName());
        Endpoint child3 = createEndpoint("baz");

        List<Endpoint> children = new ArrayList<Endpoint>();
        children.add(child1);
        children.add(child2);
        children.add(child3);
        lb.setChildren(children);

        synapseConfig.addEndpoint(lb.getName(), lb);
        assertDependency(ConfigurationObject.TYPE_ENDPOINT, child1.getKey(), lb.getName());
        assertDependency(ConfigurationObject.TYPE_ENDPOINT, child2.getKey(), lb.getName());
        assertNoDependency(ConfigurationObject.TYPE_ENDPOINT, child3.getName());
    }
}
