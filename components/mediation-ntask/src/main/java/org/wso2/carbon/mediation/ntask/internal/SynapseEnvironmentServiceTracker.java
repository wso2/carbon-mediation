package org.wso2.carbon.mediation.ntask.internal;

import org.wso2.carbon.mediation.initializer.services.SynapseEnvironmentService;

/**
 * @scr.component name="SynapseEnvironmentServiceTracker.component" immediate="true"
 * @scr.reference name="synapse.environment.service"
 * interface="org.wso2.carbon.mediation.initializer.services.SynapseEnvironmentService"
 * cardinality="1..n" policy="dynamic" 
 * bind="setSynapseEnvironmentService" unbind="unsetSynapseEnvironmentService"
 */
public class SynapseEnvironmentServiceTracker {

	private static SynapseEnvironmentService synapseEnvironmentService = null;

	protected void unsetSynapseEnvironmentService(SynapseEnvironmentService synapseEnvironmentService) {
		synapseEnvironmentService = null;
	}

	protected void setSynapseEnvironmentService(SynapseEnvironmentService synapseEnvironmentService) {
		SynapseEnvironmentServiceTracker.synapseEnvironmentService = synapseEnvironmentService;
	}

	public static SynapseEnvironmentService getSynapseEnvironmentService() {
		return synapseEnvironmentService;
	}
}
