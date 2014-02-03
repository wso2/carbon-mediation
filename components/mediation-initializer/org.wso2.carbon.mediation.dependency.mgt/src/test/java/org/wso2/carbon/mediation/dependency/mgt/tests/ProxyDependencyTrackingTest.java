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
import org.apache.synapse.mediators.base.SequenceMediator;
import org.apache.synapse.core.axis2.ProxyService;
import org.apache.synapse.mediators.builtin.LogMediator;
import org.wso2.carbon.mediation.dependency.mgt.ConfigurationObject;

public class ProxyDependencyTrackingTest extends DependencyMgtTestCase {

    public void testProxySequenceDependencyMgt() {
        SynapseConfiguration synapseConfig = createSynapseConfig();

        SequenceMediator inSeq = createSequence("in", null);
        SequenceMediator outSeq = createSequence("out", null);
        synapseConfig.addSequence(inSeq.getName(), inSeq);
        synapseConfig.addSequence(outSeq.getName(), outSeq);

        ProxyService proxy1 = createProxy("proxy1", inSeq.getName(), null, null);
        ProxyService proxy2 = createProxy("proxy2", null, outSeq.getName(), null);
        ProxyService proxy3 = createProxy("proxy3", inSeq.getName(), outSeq.getName(), null);
        synapseConfig.addProxyService(proxy1.getName(), proxy1);
        synapseConfig.addProxyService(proxy2.getName(), proxy2);
        synapseConfig.addProxyService(proxy3.getName(), proxy3);

        assertDependency(ConfigurationObject.TYPE_SEQUENCE, inSeq.getName(), proxy1.getName());
        assertNoDependency(ConfigurationObject.TYPE_SEQUENCE, inSeq.getName(), proxy2.getName());
        assertDependency(ConfigurationObject.TYPE_SEQUENCE, inSeq.getName(), proxy3.getName());
        assertNoDependency(ConfigurationObject.TYPE_SEQUENCE, outSeq.getName(), proxy1.getName());
        assertDependency(ConfigurationObject.TYPE_SEQUENCE, outSeq.getName(), proxy2.getName());
        assertDependency(ConfigurationObject.TYPE_SEQUENCE, outSeq.getName(), proxy3.getName());
    }

    public void testProxyWithAnonSequence() {
        SynapseConfiguration synapseConfig = createSynapseConfig();

        SequenceMediator inSeq = createSequence("in", null);
        synapseConfig.addSequence(inSeq.getName(), inSeq);

        ProxyService proxy1 = createProxy("proxy1", inSeq.getName(), null, null);
        SequenceMediator anon = new SequenceMediator();
        anon.addChild(new LogMediator());
        proxy1.setTargetInLineOutSequence(anon);
        synapseConfig.addProxyService(proxy1.getName(), proxy1);

        assertDependency(ConfigurationObject.TYPE_SEQUENCE, inSeq.getName(), proxy1.getName());
    }

    public void testProxyWsdlDependencyMgt() {
        SynapseConfiguration synapseConfig = createSynapseConfig();

        ProxyService proxy = createProxy("proxy", null, null, "test.wsdl");
        synapseConfig.addProxyService(proxy.getName(), proxy);
        assertDependency(ConfigurationObject.TYPE_UNKNOWN, "test.wsdl", proxy.getName());
        synapseConfig.removeProxyService(proxy.getName());
        assertNoDependency(ConfigurationObject.TYPE_UNKNOWN, "test.wsdl");
    }
}
