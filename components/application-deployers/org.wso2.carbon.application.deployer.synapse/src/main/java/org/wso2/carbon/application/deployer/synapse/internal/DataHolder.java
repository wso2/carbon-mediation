package org.wso2.carbon.application.deployer.synapse.internal;

import org.wso2.carbon.mediation.initializer.services.SynapseEnvironmentService;
import org.wso2.carbon.mediation.initializer.services.CAppArtifactDataService;

import java.util.HashMap;
import java.util.Map;

/**
 * Data holder to contains synapse environment details for tenants
 */

public class DataHolder {
    private static DataHolder instance;
    private Map<Integer, SynapseEnvironmentService> synapseEnvironmentServices =
            new HashMap<Integer, SynapseEnvironmentService>();

    private CAppArtifactDataService cAppArtifactDataService;

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

    public CAppArtifactDataService getcAppArtifactDataService() {
        return cAppArtifactDataService;
    }

    public void setcAppArtifactDataService(CAppArtifactDataService cAppArtifactDataService) {
        this.cAppArtifactDataService = cAppArtifactDataService;
    }
}
