package org.wso2.carbon.inbound.endpoint.protocol.http2;


import org.apache.synapse.inbound.InboundProcessorParams;
import org.wso2.carbon.inbound.endpoint.protocol.http2.common.InboundHttp2Constants;

import java.util.Properties;

/**
 * Created by chanakabalasooriya on 9/1/16.
 */
public class InboundHttp2Configuration {
    private final int port;
    private final String name;
    private final String dispatchPattern;
    private final Properties properties;
    private String bossThreadPoolSize;
    private String workerThreadPoolSize;
    private int so_blcklog;

    public Properties getProperties() {
        return properties;
    }


    private InboundHttp2Configuration(InboundHttp2Configuration.InboundHttp2ConfigurationBuilder builder) {
        this.port = builder.port;
        this.name = builder.name;
        this.properties=builder.properties;
        this.dispatchPattern = builder.dispatchPattern;
        this.workerThreadPoolSize = builder.workerThreadPoolSize;
        this.bossThreadPoolSize = builder.bossThreadPoolSize;
        this.so_blcklog=builder.so_blcklog;
    }

    public String getBossThreadPoolSize() {
        return bossThreadPoolSize;
    }

    public String getWorkerThreadPoolSize() {
        return workerThreadPoolSize;
    }

    public int getSoBacklog() {
        return so_blcklog;
    }

    public int getPort() {
        return port;
    }

    public String getName() {
        return name;
    }

    public String getDispatchPattern() {
        return dispatchPattern;
    }

    public static class InboundHttp2ConfigurationBuilder{
        private final int port;
        private final String name;
        private String dispatchPattern;
        private Properties properties;
        private String bossThreadPoolSize;
        private String workerThreadPoolSize;
        private int so_blcklog;

        public InboundHttp2ConfigurationBuilder(int port,String name,InboundProcessorParams params) {
            properties = params.getProperties();

            this.name = ((name!=null)||(name!=""))?name:params.getName();
            this.port = (port>0)?port:Integer.parseInt(properties.getProperty(InboundHttp2Constants.INBOUND_PORT));

            if(properties.getProperty(InboundHttp2Constants.INBOUND_ENDPOINT_PARAMETER_DISPATCH_FILTER_PATTERN)!=null) {
                this.dispatchPattern = properties.getProperty(
                        InboundHttp2Constants.INBOUND_ENDPOINT_PARAMETER_DISPATCH_FILTER_PATTERN);
            }else{
                this.dispatchPattern=null;
            }

            if(properties.getProperty(InboundHttp2Constants.INBOUND_BOSS_THREAD_POOL_SIZE)!=null) {
                this.bossThreadPoolSize = properties.getProperty(
                        InboundHttp2Constants.INBOUND_BOSS_THREAD_POOL_SIZE);
            }else{
                this.bossThreadPoolSize="1";
            }
            if(properties.getProperty(InboundHttp2Constants.INBOUND_WORKER_THREAD_POOL_SIZE)!=null) {
                this.workerThreadPoolSize = properties.getProperty(
                        InboundHttp2Constants.INBOUND_WORKER_THREAD_POOL_SIZE);
            }else {
                this.workerThreadPoolSize="1";
            }
            if(properties.getProperty(InboundHttp2Constants.INBOUND_SO_BACKLOG)!=null) {
                this.so_blcklog = Integer.parseInt(properties.getProperty(InboundHttp2Constants.INBOUND_SO_BACKLOG));
            }else{
                this.so_blcklog=1024;
            }
        }

        public InboundHttp2Configuration build() {
            return new InboundHttp2Configuration(this);
        }
        
    }

}
