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

import org.apache.synapse.config.SynapseConfiguration;
import org.apache.synapse.config.Entry;
import org.apache.synapse.mediators.base.SequenceMediator;
import org.apache.synapse.mediators.builtin.SendMediator;
import org.apache.synapse.endpoints.*;
import org.apache.synapse.core.axis2.ProxyService;
import org.wso2.carbon.mediation.dependency.mgt.ConfigurationObject;

public class BasicDependencyTrackingTest extends DependencyMgtTestCase {

    public void testDependencyMgtOnAdd() {

        System.out.println("Testing mediation dependency management while adding entries...");

        SynapseConfiguration synapseConfig = createSynapseConfig();

        // Add a local entry
        Entry entry = createEntry("sec_policy");
        synapseConfig.addEntry(entry.getKey(), entry);

        // Add an endpoint which is dependent on the local entry 'sec_policy'
        Endpoint endpoint = createEndpoint("endpoint");
        initEndpoint(endpoint, entry.getKey());
        synapseConfig.addEndpoint(endpoint.getName(), endpoint);

        // Test
        assertDependency(ConfigurationObject.TYPE_ENTRY, entry.getKey(), endpoint.getName());

        // Add a sequence which is dependent on the endpoint
        SequenceMediator seq1 = createSequence("seq1", null);
        SendMediator send = new SendMediator();
        IndirectEndpoint endpointRef = new IndirectEndpoint();
        endpointRef.setKey(endpoint.getName());
        send.setEndpoint(endpointRef);
        seq1.addChild(send);
        synapseConfig.addSequence(seq1.getName(), seq1);

        // Test
        assertDependency(ConfigurationObject.TYPE_ENDPOINT, endpoint.getName(), seq1.getName());

        // Add another sequence which is dependent on the sequence 'seq1'
        SequenceMediator seq2 = createSequence("seq2", seq1.getName());
        synapseConfig.addSequence(seq2.getName(), seq2);

        // Test
        assertDependency(ConfigurationObject.TYPE_SEQUENCE, seq1.getName(), seq2.getName());

        SequenceMediator seq3 = new SequenceMediator();
        seq3.setName("seq3");
        synapseConfig.addSequence(seq3.getName(), seq3);

        Entry wsdl = new Entry("wsdl");
        synapseConfig.addEntry(wsdl.getKey(), wsdl);

        // Add a proxy service
        ProxyService proxy = createProxy("proxy", seq2.getName(), seq3.getName(), wsdl.getKey());
        synapseConfig.addProxyService(proxy.getName(), proxy);

        // Test
        assertDependency(ConfigurationObject.TYPE_SEQUENCE, seq2.getName(), proxy.getName());
        assertDependency(ConfigurationObject.TYPE_SEQUENCE, seq3.getName(), proxy.getName());
        assertDependency(ConfigurationObject.TYPE_ENTRY, wsdl.getKey(), proxy.getName());

        System.out.println("All tests were successful...");
    }

    public void testDependencyMgtOnRemove() {
        System.out.println("Testing mediation dependency management while removing entries...");

        SynapseConfiguration synapseConfig = createSynapseConfig();

        Entry entry = createEntry("sec_policy");
        synapseConfig.addEntry(entry.getKey(), entry);

        Endpoint endpoint = createEndpoint("endpoint");
        initEndpoint(endpoint, entry.getKey());
        synapseConfig.addEndpoint(endpoint.getName(), endpoint);

        SequenceMediator seq1 = createSequence("seq1", null);
        SendMediator send = new SendMediator();
        IndirectEndpoint endpointRef = new IndirectEndpoint();
        endpointRef.setKey(endpoint.getName());
        send.setEndpoint(endpointRef);
        seq1.addChild(send);
        synapseConfig.addSequence(seq1.getName(), seq1);

        SequenceMediator seq2 = createSequence("seq2", seq1.getName());
        synapseConfig.addSequence(seq2.getName(), seq2);

        SequenceMediator seq3 = new SequenceMediator();
        seq3.setName("seq3");
        synapseConfig.addSequence(seq3.getName(), seq3);

        Entry wsdl = new Entry("wsdl");
        synapseConfig.addEntry(wsdl.getKey(), wsdl);

        ProxyService proxy = createProxy("proxy", seq2.getName(), seq3.getName(), wsdl.getKey());
        synapseConfig.addProxyService(proxy.getName(), proxy);

        assertDependency(ConfigurationObject.TYPE_SEQUENCE, seq2.getName(), proxy.getName());
        assertDependency(ConfigurationObject.TYPE_SEQUENCE, seq3.getName(), proxy.getName());
        assertDependency(ConfigurationObject.TYPE_ENTRY, wsdl.getKey(), proxy.getName());
        synapseConfig.removeProxyService(proxy.getName());
        assertNoDependency(ConfigurationObject.TYPE_SEQUENCE, seq2.getName());
        assertNoDependency(ConfigurationObject.TYPE_SEQUENCE, seq3.getName());
        assertNoDependency(ConfigurationObject.TYPE_ENTRY, wsdl.getKey());

        assertDependency(ConfigurationObject.TYPE_SEQUENCE, seq1.getName(), seq2.getName());
        synapseConfig.removeSequence(seq2.getName());
        assertNoDependency(ConfigurationObject.TYPE_SEQUENCE, seq1.getName());

        assertDependency(ConfigurationObject.TYPE_ENDPOINT, endpoint.getName(), seq1.getName());
        synapseConfig.removeSequence(seq1.getName());
        assertNoDependency(ConfigurationObject.TYPE_ENDPOINT, endpoint.getName());

        assertDependency(ConfigurationObject.TYPE_ENTRY, entry.getKey(), endpoint.getName());
        synapseConfig.removeEntry(entry.getKey());
        assertNoDependency(ConfigurationObject.TYPE_ENTRY, entry.getKey());

        System.out.println("All tests were successful...");
    }

    public void testDependencyMgtOnUpdate() {
        System.out.println("Testing mediation dependency management while updating entries...");

        SynapseConfiguration synapseConfig = createSynapseConfig();

        Entry entry1 = createEntry("entry1");
        Entry entry2 = createEntry("entry2");
        synapseConfig.addEntry(entry1.getKey(), entry1);
        synapseConfig.addEntry(entry2.getKey(), entry2);

        Endpoint endpoint = createEndpoint("endpoint");
        initEndpoint(endpoint, entry1.getKey());
        synapseConfig.addEndpoint(endpoint.getName(), endpoint);

        assertDependency(ConfigurationObject.TYPE_ENTRY, entry1.getKey(), endpoint.getName());
        synapseConfig.removeEndpoint(endpoint.getName());
        initEndpoint(endpoint, entry2.getKey());
        synapseConfig.addEndpoint(endpoint.getName(), endpoint);
        assertNoDependency(ConfigurationObject.TYPE_ENTRY, entry1.getKey());
        assertDependency(ConfigurationObject.TYPE_ENTRY, entry2.getKey(), endpoint.getName());

        Endpoint endpoint2 = createEndpoint("endpoint2");
        initEndpoint(endpoint2, null);
        synapseConfig.addEndpoint(endpoint2.getName(), endpoint2);

        SequenceMediator seq1 = createSequence("seq1", null);
        SendMediator send = new SendMediator();
        IndirectEndpoint endpointRef = new IndirectEndpoint();
        endpointRef.setKey(endpoint.getName());
        send.setEndpoint(endpointRef);
        seq1.addChild(send);
        synapseConfig.addSequence(seq1.getName(), seq1);

        assertDependency(ConfigurationObject.TYPE_ENDPOINT, endpoint.getName(), seq1.getName());
        synapseConfig.removeSequence(seq1.getName());
        seq1.removeChild(0);
        send = new SendMediator();
        endpointRef = new IndirectEndpoint();
        endpointRef.setKey(endpoint2.getName());
        send.setEndpoint(endpointRef);
        seq1.addChild(send);
        synapseConfig.addSequence(seq1.getName(), seq1);
        assertNoDependency(ConfigurationObject.TYPE_ENDPOINT, endpoint.getName());
        assertDependency(ConfigurationObject.TYPE_ENDPOINT, endpoint2.getName(), seq1.getName());

        SequenceMediator seq2 = createSequence("seq2", null);
        synapseConfig.addSequence(seq2.getName(), seq2);

        ProxyService proxy = createProxy("proxy", seq1.getName(), null, null);
        synapseConfig.addProxyService(proxy.getName(), proxy);

        assertDependency(ConfigurationObject.TYPE_SEQUENCE, seq1.getName(), proxy.getName());
        synapseConfig.removeProxyService(proxy.getName());
        proxy = createProxy("proxy", null, seq2.getName(), null);
        synapseConfig.addProxyService(proxy.getName(), proxy);
        assertNoDependency(ConfigurationObject.TYPE_SEQUENCE, seq1.getName());
        assertDependency(ConfigurationObject.TYPE_SEQUENCE, seq2.getName(), proxy.getName());        

        System.out.println("All tests were successful...");
    }

    public void testInactiveDependency() {
        SynapseConfiguration synapseConfig = createSynapseConfig();

        Endpoint endpoint = createEndpoint("endpoint");
        initEndpoint(endpoint, null);
        synapseConfig.addEndpoint(endpoint.getName(), endpoint);

        SequenceMediator sequence = createSequence("sequence", null);
        SendMediator send = new SendMediator();
        IndirectEndpoint target = new IndirectEndpoint();
        target.setKey(endpoint.getName());
        send.setEndpoint(target);
        sequence.addChild(send);
        synapseConfig.addSequence(sequence.getName(), sequence);
        assertDependency(ConfigurationObject.TYPE_ENDPOINT, endpoint.getName(), sequence.getName());

        SequenceMediator sequence2 = createSequence("sequence2", sequence.getName());
        synapseConfig.addSequence(sequence2.getName(), sequence2);
        assertDependency(ConfigurationObject.TYPE_SEQUENCE, sequence.getName(), sequence2.getName());

        synapseConfig.removeSequence(sequence.getName());
        assertDependency(ConfigurationObject.TYPE_ENDPOINT, endpoint.getName(), sequence.getName());
        assertNoActiveDependency(ConfigurationObject.TYPE_ENDPOINT, endpoint.getName());
    }

    private Entry createEntry(String key) {
        return new Entry(key);
    }                

}
