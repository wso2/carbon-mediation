package org.wso2.carbon.mediator.bam.builders;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.synapse.MessageContext;
import org.wso2.carbon.mediator.bam.config.BamMediatorException;
import org.wso2.carbon.mediator.bam.util.BamMediatorConstants;

public class CorrelationDataBuilder {

    private static final Log log = LogFactory.getLog(CorrelationDataBuilder.class);

    public Object[] createCorrelationData(MessageContext messageContext) throws BamMediatorException {
        Object[] correlationData = new Object[BamMediatorConstants.NUM_OF_CONST_CORRELATION_PARAMS];
        int i= 0;
        try{
            correlationData[i] = messageContext.getProperty(BamMediatorConstants.MSG_BAM_ACTIVITY_ID);
            return correlationData;
        } catch (Exception e) {
            String errorMsg = "Error occurred while producing values for Correlation Data. " + e.getMessage();
            log.error(errorMsg, e);
            throw new BamMediatorException(errorMsg, e);
        }
    }
}
