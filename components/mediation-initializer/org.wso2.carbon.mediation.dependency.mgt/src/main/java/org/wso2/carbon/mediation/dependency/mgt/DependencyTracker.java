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

package org.wso2.carbon.mediation.dependency.mgt;

import org.apache.synapse.config.AbstractSynapseObserver;
import org.apache.synapse.config.Entry;
import org.apache.synapse.core.axis2.ProxyService;
import org.apache.synapse.endpoints.*;
import org.apache.synapse.Mediator;
import org.apache.synapse.Startup;
import org.apache.synapse.eventing.SynapseEventSource;
import org.apache.synapse.mediators.base.SequenceMediator;

import java.util.List;

/**
 * Observes the Synapse configuration and constructs a model which represents the
 * inter dependencies among various configuration items. The model is dynamically
 * updated to reflect the changes made to the Synapse configuration at runtime. This
 * class also enables querying the model to resolve dependencies among various configuration
 * items.
 */
public class DependencyTracker extends AbstractSynapseObserver {

    private DependencyGraph dependencyGraph = new DependencyGraph();

    public DependencyTracker() {
        if (log.isDebugEnabled()) {
            log.debug("Synapse configuration dependency tracker initialized...");
        }
    }

    public void entryAdded(Entry entry) {
        ConfigurationObject entryObject = findEntry(entry.getKey());
        if (entryObject == null) {
            entryObject = new ConfigurationObject(ConfigurationObject.TYPE_ENTRY,
                    entry.getKey());
            dependencyGraph.add(entryObject);
        } else if (entryObject.getType() == ConfigurationObject.TYPE_UNKNOWN) {
            dependencyGraph.resolveObject(entryObject, ConfigurationObject.TYPE_ENTRY);
        }
    }

    public void entryRemoved(Entry entry) {
        dependencyGraph.remove(ConfigurationObject.TYPE_ENTRY, entry.getKey());
    }

    public void sequenceAdded(Mediator sequence) {
        if (!(sequence instanceof SequenceMediator)) {
            // This is not a sequence mediator instance
            // So we don't know how this can be of any use
            return;
        }

        SequenceMediator seqMediator = (SequenceMediator) sequence;
        ConfigurationObject seqObject = findSequence(seqMediator.getName());
        if (seqObject == null) {
            seqObject = new ConfigurationObject(ConfigurationObject.TYPE_SEQUENCE,
                    seqMediator.getName());
            dependencyGraph.add(seqObject);
        } else if (seqObject.getType() == ConfigurationObject.TYPE_UNKNOWN) {
            dependencyGraph.resolveObject(seqObject, ConfigurationObject.TYPE_SEQUENCE);
        }

        resolveSequenceMediator(seqMediator, seqObject, true);
    }

    public void endpointAdded(Endpoint endpoint) {
        if (endpoint instanceof IndirectEndpoint) {
            // We cannot refer to indirect endpoints defined at top level from anywhere
            // So just ignore it
            return;
        }

        ConfigurationObject endpointObject = findEndpoint(endpoint.getName());
        if (endpointObject == null) {
            // If the endpoint is not present in the model add it
            endpointObject = new ConfigurationObject(ConfigurationObject.TYPE_ENDPOINT,
                    endpoint.getName());
            dependencyGraph.add(endpointObject);
        } else if (endpointObject.getType() == ConfigurationObject.TYPE_UNKNOWN) {
            // We just managed to resolve the type of an type unknown object
            dependencyGraph.resolveObject(endpointObject, ConfigurationObject.TYPE_ENDPOINT);
        }

        resolveEndpoint(endpoint, endpointObject);
    }

    public void endpointRemoved(Endpoint endpoint) {
        dependencyGraph.remove(ConfigurationObject.TYPE_ENDPOINT, endpoint.getName());
    }

    public void proxyServiceAdded(ProxyService proxyService) {
        // First add the proxy service to the dependency graph
        ConfigurationObject proxyObject = new ConfigurationObject(
                ConfigurationObject.TYPE_PROXY, proxyService.getName());
        dependencyGraph.add(proxyObject);

        resolveProxyService(proxyService, proxyObject);
    }

    public void proxyServiceRemoved(ProxyService proxy) {
        dependencyGraph.remove(ConfigurationObject.TYPE_PROXY, proxy.getName());
    }

    public boolean hasDependents(int type, String id) {
        ConfigurationObject configObj = dependencyGraph.find(type, id);
        return configObj != null && dependencyGraph.hasDependents(configObj);
    }

    public boolean hasActiveDependents(int type, String id) {
        ConfigurationObject configObj = dependencyGraph.find(type, id);
        return configObj != null && dependencyGraph.hasActiveDependents(configObj);
    }

    /**
     * Finds the objects which are dependent on the specified object
     *
     * @param type integer values representing the type of the object
     * @param id unique ID value of the object
     * @return an array of dependent objects or null if there are no dependents 
     */
    public ConfigurationObject[] getDependents(int type, String id) {
        ConfigurationObject configObj = dependencyGraph.find(type, id);
        if (configObj != null) {
            return dependencyGraph.getDependents(configObj);
        }
        return null;
    }

    private ConfigurationObject findEndpoint(String key) {
        // First search for a matching endpoint definition
        ConfigurationObject endpoint = dependencyGraph.find(ConfigurationObject.TYPE_ENDPOINT,
                key);
        if (endpoint != null) {
            return endpoint;
        }

        // This could be an entry
        endpoint = dependencyGraph.find(ConfigurationObject.TYPE_ENTRY, key);
        if (endpoint != null) {
            return endpoint;
        }

        // Do we at least have an object of type UNKNOWN with the same key value
        return dependencyGraph.find(ConfigurationObject.TYPE_UNKNOWN, key);
    }

    private ConfigurationObject findSequence(String key) {
        // First search for a matching sequence definition
        ConfigurationObject sequence = dependencyGraph.find(ConfigurationObject.TYPE_SEQUENCE,
                key);
        if (sequence != null) {
            return sequence;
        }

        // This could be an entry
        sequence = dependencyGraph.find(ConfigurationObject.TYPE_ENTRY, key);
        if (sequence != null) {
            return sequence;
        }

        // Do we at least have an object of type UNKNOWN with the same key value
        return dependencyGraph.find(ConfigurationObject.TYPE_UNKNOWN, key);
    }

    private ConfigurationObject findEntry(String key) {
        ConfigurationObject entry = dependencyGraph.find(ConfigurationObject.TYPE_ENTRY, key);
        if (entry != null) {
            return entry;
        }

        return dependencyGraph.find(ConfigurationObject.TYPE_UNKNOWN, key);
    }

    private ConfigurationObject createUnknownObject(String key) {
        ConfigurationObject unknownObject = new ConfigurationObject(
                ConfigurationObject.TYPE_UNKNOWN, key);
        dependencyGraph.add(unknownObject);
        return unknownObject;
    }

    private void resolveProxyService(ProxyService proxyService,
                                     ConfigurationObject dependentObject) {

        // Find dependencies with endpoints
        if (proxyService.getTargetEndpoint() != null) {
            ConfigurationObject endpointObject = findEndpoint(proxyService.getTargetEndpoint());
            if (endpointObject == null) {
                endpointObject = createUnknownObject(proxyService.getTargetEndpoint());
            }
            dependencyGraph.createEdge(endpointObject, dependentObject);

        } else if (proxyService.getTargetInLineEndpoint() instanceof IndirectEndpoint) {
            IndirectEndpoint indirectEndpoint = (IndirectEndpoint) proxyService.
                    getTargetInLineEndpoint();
            ConfigurationObject endpointObject = findEndpoint(indirectEndpoint.getKey());
            if (endpointObject == null) {
                endpointObject = createUnknownObject(indirectEndpoint.getKey());
            }
            dependencyGraph.createEdge(endpointObject, dependentObject);
        }

        // Find dependencies with sequences
        if (proxyService.getTargetInSequence() != null) {
            ConfigurationObject inSeqObject = findSequence(proxyService.getTargetInSequence());
            if (inSeqObject == null) {
                inSeqObject = createUnknownObject(proxyService.getTargetInSequence());
            }
            dependencyGraph.createEdge(inSeqObject, dependentObject);
        } else if (proxyService.getTargetInLineInSequence() != null) {
            resolveSequenceMediator(proxyService.getTargetInLineInSequence(),
                    dependentObject, false);
        }

        if (proxyService.getTargetOutSequence() != null) {
            ConfigurationObject outSeqObject = findSequence(proxyService.getTargetOutSequence());
            if (outSeqObject == null) {
                outSeqObject = createUnknownObject(proxyService.getTargetOutSequence());
            }
            dependencyGraph.createEdge(outSeqObject, dependentObject);
        } else if (proxyService.getTargetInLineOutSequence() != null) {
            resolveSequenceMediator(proxyService.getTargetInLineOutSequence(),
                    dependentObject, false);
        }

        if (proxyService.getTargetFaultSequence() != null) {
            ConfigurationObject faultSeqObject = findSequence(proxyService.getTargetFaultSequence());
            if (faultSeqObject == null) {
                faultSeqObject = createUnknownObject(proxyService.getTargetFaultSequence());
            }
            dependencyGraph.createEdge(faultSeqObject, dependentObject);
        } else if (proxyService.getTargetInLineFaultSequence() != null) {
            resolveSequenceMediator(proxyService.getTargetInLineFaultSequence(),
                    dependentObject, false);
        }

        if (proxyService.getWSDLKey() != null) {
            ConfigurationObject wsdlObject = findEntry(proxyService.getWSDLKey());
            if (wsdlObject == null) {
                wsdlObject = createUnknownObject(proxyService.getWSDLKey());
            }
            dependencyGraph.createEdge(wsdlObject, dependentObject);
        }
    }

    private void resolveSequenceMediator(SequenceMediator sequence,
                                               ConfigurationObject dependentObject, boolean reset) {

        if (reset) {
            dependencyGraph.removeDependencies(dependentObject);
        }

        DependencyResolver resolver = DependencyResolverFactory.getInstance().
                getResolver(sequence);
        List<ConfigurationObject> providers = resolver.resolve(sequence);

        if (providers != null) {
            for (ConfigurationObject o : providers) {
                ConfigurationObject provider = null;
                switch (o.getType()) {
                    case ConfigurationObject.TYPE_ENDPOINT:
                        provider = findEndpoint(o.getId());
                        break;

                    case ConfigurationObject.TYPE_SEQUENCE:
                        provider = findSequence(o.getId());
                        break;

                    case ConfigurationObject.TYPE_ENTRY:
                        provider = findEntry(o.getId());
                        break;
                }

                if (provider == null) {
                    provider = createUnknownObject(o.getId());                    
                }
                dependencyGraph.createEdge(provider, dependentObject);
            }
        }
    }

    private void resolveEndpoint(Endpoint endpoint, ConfigurationObject dependentObject) {
        if (!(endpoint instanceof AbstractEndpoint)) {
            return;
        }

        AbstractEndpoint abstractEndpoint = (AbstractEndpoint) endpoint;
        EndpointDefinition def = abstractEndpoint.getDefinition();
        if (def == null) {
            return;
        }

        dependencyGraph.removeDependencies(dependentObject);

        resolveEntry(def.getWsSecPolicyKey(), dependentObject);
        resolveEntry(def.getWsRMPolicyKey(), dependentObject);
        resolveEntry(def.getInboundWsSecPolicyKey(), dependentObject);
        resolveEntry(def.getOutboundWsSecPolicyKey(), dependentObject);

        List<Endpoint> children = abstractEndpoint.getChildren();
        if (children != null) {
            for (Endpoint child : children) {
                if (child instanceof IndirectEndpoint) {
                    String key = ((IndirectEndpoint) child).getKey();
                    ConfigurationObject childEndpoint = findEndpoint(key);
                    if (childEndpoint == null) {
                        childEndpoint = createUnknownObject(key);
                    }
                    dependencyGraph.createEdge(childEndpoint, dependentObject);
                }
            }
        }
    }

    private void resolveEntry(String entryKey, ConfigurationObject dependentObject) {
        if (entryKey == null) {
            return;
        }

        dependencyGraph.removeDependencies(dependentObject);

        ConfigurationObject entryObject = findEntry(entryKey);
        if (entryObject == null) {
            entryObject = createUnknownObject(entryKey);
        }
        dependencyGraph.createEdge(entryObject, dependentObject);
    }

    public void eventSourceAdded(SynapseEventSource eventSource) {
        // Do nothing
    }

    public void eventSourceRemoved(SynapseEventSource eventSource) {
        // Do nothing
    }

    public void sequenceRemoved(Mediator sequence) {
        if (sequence instanceof SequenceMediator) {
            dependencyGraph.remove(ConfigurationObject.TYPE_SEQUENCE,
                    ((SequenceMediator) sequence).getName());
        }
    }

    public void startupAdded(Startup startup) {
        // Do nothing
    }

    public void startupRemoved(Startup startup) {
        // Do nothing
    }
}
