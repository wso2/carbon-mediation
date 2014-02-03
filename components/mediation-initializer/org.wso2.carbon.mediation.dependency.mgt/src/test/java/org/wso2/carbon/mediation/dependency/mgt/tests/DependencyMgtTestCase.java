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

import junit.framework.TestCase;
import org.wso2.carbon.mediation.dependency.mgt.services.DependencyManagementService;
import org.wso2.carbon.mediation.dependency.mgt.services.DependencyManagementServiceImpl;
import org.wso2.carbon.mediation.dependency.mgt.DependencyTracker;
import org.wso2.carbon.mediation.dependency.mgt.ConfigurationObject;
import org.apache.synapse.config.SynapseConfiguration;
import org.apache.synapse.endpoints.Endpoint;
import org.apache.synapse.endpoints.AddressEndpoint;
import org.apache.synapse.endpoints.EndpointDefinition;
import org.apache.synapse.endpoints.AbstractEndpoint;
import org.apache.synapse.core.axis2.ProxyService;
import org.apache.synapse.mediators.base.SequenceMediator;
import org.apache.synapse.mediators.Value;
import org.apache.axis2.engine.AxisConfiguration;

public abstract class DependencyMgtTestCase extends TestCase {

    protected DependencyManagementService dependencyMgtSvc;

    protected SynapseConfiguration createSynapseConfig() {
        SynapseConfiguration synapseConfig = new SynapseConfiguration();
        synapseConfig.setAxisConfiguration(new AxisConfiguration());
        DependencyTracker tracker = new DependencyTracker();
        synapseConfig.registerObserver(tracker);
        dependencyMgtSvc = new DependencyManagementServiceImpl(tracker);
        return synapseConfig;
    }

    protected void assertDependency(int type, String id, String dependentId) {
        ConfigurationObject[] dependents = dependencyMgtSvc.getDependents(type, id);
        assertNotNull(dependents);
        assertTrue(dependents.length > 0);
        for (ConfigurationObject d : dependents) {
            if (d.getId().equals(dependentId)) {
                return;
            }
        }
        fail("No dependency found from " + id + " to " + dependentId);
    }

    protected void assertNoDependency(int type, String id, String dependentId) {
        ConfigurationObject[] dependents = dependencyMgtSvc.getDependents(type, id);
        if (dependents != null) {
            for (ConfigurationObject d : dependents) {
                if (d.getId().equals(dependentId)) {
                    fail("Dependency encountered between " + id + " to " + dependentId);
                }
            }
        }
    }

    protected void assertNoDependency(int type, String id) {
        ConfigurationObject[] dependents = dependencyMgtSvc.getDependents(type, id);
        assertTrue(dependents == null || dependents.length == 0);
    }

    protected void assertNoActiveDependency(int type, String id) {
        ConfigurationObject[] dependents = dependencyMgtSvc.getDependents(type, id);
        if (dependents != null && dependents.length > 0) {
            for (ConfigurationObject o : dependents) {
                assertTrue(o.getType() == ConfigurationObject.TYPE_UNKNOWN);
            }
        }
    }

    protected Endpoint createEndpoint(String key) {
        Endpoint endpoint = new AddressEndpoint();
        endpoint.setName(key);
        return endpoint;
    }

    protected void initEndpoint(Endpoint endpoint, String policyKey) {
        if (endpoint instanceof AbstractEndpoint) {
            EndpointDefinition def = new EndpointDefinition();
            def.setAddress("http://localhost:9000/services/SimpleStockQuoteService");
            if (policyKey != null) {
                def.setWsSecPolicyKey(policyKey);
            }
            ((AbstractEndpoint) endpoint).setDefinition(def);
        }
    }

    protected ProxyService createProxy(String name, String inSeq, String outSeq, String wsdl) {
        ProxyService proxy = new ProxyService(name);
        if (inSeq != null) {
            proxy.setTargetInSequence(inSeq);
        }
        if (outSeq != null) {
            proxy.setTargetOutSequence(outSeq);
        }
        if (wsdl != null) {
            proxy.setWSDLKey(wsdl);
        }
        return proxy;
    }

    protected SequenceMediator createSequence(String key, String seqRef) {
        SequenceMediator seq = new SequenceMediator();
        seq.setName(key);
        if (seqRef != null) {
            SequenceMediator ref = new SequenceMediator();
            //Create keyValue from static key(seqRef)
            Value seqRefValue = new Value(seqRef);
            ref.setKey(seqRefValue);
            seq.addChild(ref);
        }
        return seq;
    }
}
