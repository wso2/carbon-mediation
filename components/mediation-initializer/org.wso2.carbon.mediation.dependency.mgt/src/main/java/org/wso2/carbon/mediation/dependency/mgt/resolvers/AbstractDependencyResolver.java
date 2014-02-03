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

import org.wso2.carbon.mediation.dependency.mgt.DependencyResolver;
import org.wso2.carbon.mediation.dependency.mgt.ConfigurationObject;
import org.wso2.carbon.mediation.dependency.mgt.DependencyResolverFactory;
import org.apache.synapse.Mediator;
import org.apache.synapse.endpoints.IndirectEndpoint;
import org.apache.synapse.mediators.ListMediator;
import org.apache.synapse.mediators.eip.Target;

import java.util.List;

public abstract class AbstractDependencyResolver implements DependencyResolver {

    public abstract List<ConfigurationObject> resolve(Mediator m);

    protected void addProvider(ConfigurationObject o, List<ConfigurationObject> providers) {
        for (ConfigurationObject provider : providers) {
            if (provider.equals(o)) {
                return;
            }
        }
        providers.add(o);
    }

    protected void resolveListMediator(ListMediator listMediator,
                                       List<ConfigurationObject> providers) {

        List<Mediator> children = listMediator.getList();
        DependencyResolverFactory resolverFactory = DependencyResolverFactory.getInstance();
        for (Mediator child : children) {
            DependencyResolver resolver = resolverFactory.getResolver(child);
            if (resolver == null) {
                continue;
            }

            List<ConfigurationObject> childDependencies = resolver.resolve(child);
            if (childDependencies != null) {
                for (ConfigurationObject o : childDependencies) {
                    addProvider(o, providers);
                }
            }
        }
    }

    protected void resolveTarget(Target target, List<ConfigurationObject> providers) {
        if (target.getEndpointRef() != null) {
            addProvider(new ConfigurationObject(ConfigurationObject.TYPE_ENDPOINT,
                    target.getEndpointRef()), providers);
        }

        if (target.getSequenceRef() != null) {
            addProvider(new ConfigurationObject(ConfigurationObject.TYPE_SEQUENCE,
                    target.getSequenceRef()), providers);
        }

        if (target.getEndpoint() != null && target.getEndpoint() instanceof IndirectEndpoint) {
            IndirectEndpoint indirectEndpoint = (IndirectEndpoint) target.getEndpoint();
            addProvider(new ConfigurationObject(ConfigurationObject.TYPE_ENDPOINT,
                    indirectEndpoint.getKey()), providers);
        }

        if (target.getSequence() != null) {
            DependencyResolver resolver = DependencyResolverFactory.getInstance().
                    getResolver(target.getSequence());
            List<ConfigurationObject> sequenceProviders = resolver.resolve(target.getSequence());
            for (ConfigurationObject o : sequenceProviders) {
                addProvider(o, providers);
            }
        }
    }


}
