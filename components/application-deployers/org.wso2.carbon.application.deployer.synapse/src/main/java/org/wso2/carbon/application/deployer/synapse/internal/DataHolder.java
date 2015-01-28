package org.wso2.carbon.application.deployer.synapse.internal;

import org.wso2.carbon.mediation.initializer.services.SynapseEnvironmentService;

import java.util.HashMap;
import java.util.Map;

public class DataHolder {
    private static DataHolder instance;
    private Map<Integer, SynapseEnvironmentService> synapseEnvironmentServices =
            new HashMap<Integer, SynapseEnvironmentService>();


    public static DataHolder getInstance() {
        if (instance == null) {
            instance = new DataHolder();
        }
        return instance;
    }

    public SynapseEnvironmentService getSynapseEnvironmentService(int id) {
        return synapseEnvironmentServices.get(id);
    }

    public void addSynapseEnvironmentService(int id,
                                             SynapseEnvironmentService synapseEnvironmentService) {
        synapseEnvironmentServices.put(id, synapseEnvironmentService);
    }

    public void removeSynapseEnvironmentService(int id) {
        synapseEnvironmentServices.remove(id);
    }

    public Map<Integer, SynapseEnvironmentService> getSynapseEnvironmentServices() {
        return synapseEnvironmentServices;
    }
}
