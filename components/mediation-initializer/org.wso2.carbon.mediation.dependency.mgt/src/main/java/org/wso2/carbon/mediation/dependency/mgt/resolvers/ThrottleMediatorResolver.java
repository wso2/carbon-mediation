/*
 * Copyright 2005-2007 WSO2, Inc. (http://wso2.com)
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

package org.wso2.carbon.mediation.dependency.mgt.resolvers;

import org.wso2.carbon.mediation.dependency.mgt.ConfigurationObject;
import org.wso2.carbon.mediation.dependency.mgt.DependencyResolverFactory;
import org.wso2.carbon.mediation.dependency.mgt.DependencyResolver;
import org.apache.synapse.Mediator;
import org.apache.synapse.mediators.throttle.ThrottleMediator;


import java.util.List;
import java.util.ArrayList;

public class ThrottleMediatorResolver extends AbstractDependencyResolver {

    public List<ConfigurationObject> resolve(Mediator m) {
        if (!(m instanceof ThrottleMediator)) {
            return null;
        }

        List<ConfigurationObject> providers = new ArrayList<ConfigurationObject>();
        ThrottleMediator throttleMediator = (ThrottleMediator) m;
        if (throttleMediator.getOnAcceptMediator() != null) {
            DependencyResolver resolver = DependencyResolverFactory.getInstance().
                    getResolver(throttleMediator.getOnAcceptMediator());
            List<ConfigurationObject> sequenceProviders = resolver.resolve(
                    throttleMediator.getOnAcceptMediator());
            for (ConfigurationObject o : sequenceProviders) {
                addProvider(o, providers);
            }
        } else if (throttleMediator.getOnAcceptSeqKey() != null) {
            addProvider(new ConfigurationObject(ConfigurationObject.TYPE_SEQUENCE,
                    throttleMediator.getOnAcceptSeqKey()), providers);
        }

        if (throttleMediator.getOnRejectMediator() != null) {
            DependencyResolver resolver = DependencyResolverFactory.getInstance().
                    getResolver(throttleMediator.getOnRejectMediator());
            List<ConfigurationObject> sequenceProviders = resolver.resolve(
                    throttleMediator.getOnRejectMediator());
            for (ConfigurationObject o : sequenceProviders) {
                addProvider(o, providers);
            }
        } else if (throttleMediator.getOnRejectSeqKey() != null) {
            addProvider(new ConfigurationObject(ConfigurationObject.TYPE_SEQUENCE,
                    throttleMediator.getOnRejectSeqKey()), providers);
        }

        return providers;
    }
}
