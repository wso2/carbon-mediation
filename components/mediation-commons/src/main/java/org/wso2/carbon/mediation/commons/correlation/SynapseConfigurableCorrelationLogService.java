package org.wso2.carbon.mediation.commons.correlation;

import org.apache.synapse.transport.passthru.config.PassThroughCorrelationConfigDataHolder;
import org.osgi.service.component.annotations.Component;
import org.wso2.carbon.logging.correlation.CorrelationLogConfigurable;
import org.wso2.carbon.logging.correlation.bean.ImmutableCorrelationLogConfig;

@Component(
        immediate = true,
        service = CorrelationLogConfigurable.class
)
public class SynapseConfigurableCorrelationLogService implements CorrelationLogConfigurable {

    @Override
    public String getName() {
        return "synapse";
    }

    @Override
    public ImmutableCorrelationLogConfig getConfiguration() {
        return new ImmutableCorrelationLogConfig(PassThroughCorrelationConfigDataHolder.isEnable(),
                new String[0], false);
    }

    @Override
    public void onConfigure(ImmutableCorrelationLogConfig immutableCorrelationLogConfig) {
        PassThroughCorrelationConfigDataHolder.setEnable(immutableCorrelationLogConfig.isEnable());
    }

}
