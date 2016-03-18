package org.wso2.carbon.bam.service.data.publisher.conf;

import org.wso2.carbon.databridge.agent.DataPublisher;

public class EventPublisherConfig {

	DataPublisher dataPublisher;
	DataPublisher loadBalancingDataPublisher;

	public DataPublisher getDataPublisher() {
		return dataPublisher;
	}

	public void setDataPublisher(DataPublisher dataPublisher) {
		this.dataPublisher = dataPublisher;
	}

	public void setLoadBalancingPublisher(DataPublisher loadBalancingPublisher) {
		this.loadBalancingDataPublisher = loadBalancingPublisher;
	}

	public DataPublisher getLoadBalancingDataPublisher() {
		return loadBalancingDataPublisher;
	}
}
