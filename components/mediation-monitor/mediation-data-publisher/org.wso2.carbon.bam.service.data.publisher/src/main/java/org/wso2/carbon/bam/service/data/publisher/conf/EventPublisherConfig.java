package org.wso2.carbon.bam.service.data.publisher.conf;


import org.wso2.carbon.databridge.agent.thrift.Agent;
import org.wso2.carbon.databridge.agent.thrift.AsyncDataPublisher;
import org.wso2.carbon.databridge.agent.thrift.lb.LoadBalancingDataPublisher;

public class EventPublisherConfig {

    AsyncDataPublisher dataPublisher;
    LoadBalancingDataPublisher loadBalancingDataPublisher;
    static Agent agent = new Agent();

    public AsyncDataPublisher getDataPublisher() {
        return dataPublisher;
    }

    public void setDataPublisher(AsyncDataPublisher dataPublisher) {
        this.dataPublisher = dataPublisher;
    }

    public void setLoadBalancingPublisher(LoadBalancingDataPublisher loadBalancingPublisher){
      this.loadBalancingDataPublisher =  loadBalancingPublisher;
    }

    public LoadBalancingDataPublisher getLoadBalancingDataPublisher(){
        return loadBalancingDataPublisher;
    }

    public static Agent getAgent(){
         return agent;
    }


}
