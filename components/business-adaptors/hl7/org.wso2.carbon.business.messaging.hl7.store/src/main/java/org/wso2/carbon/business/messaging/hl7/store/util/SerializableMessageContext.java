package org.wso2.carbon.business.messaging.hl7.store.util;

import org.apache.synapse.message.store.impl.commons.Axis2Message;
import org.apache.synapse.message.store.impl.commons.SynapseMessage;

import java.io.Serializable;

public class SerializableMessageContext implements Serializable {

    private Axis2Message axis2message;

    private SynapseMessage synapseMessage;


    public SynapseMessage getSynapseMessage() {
        return synapseMessage;
    }

    public void setSynapseMessage(SynapseMessage synapseMessage) {
        this.synapseMessage = synapseMessage;
    }

    public Axis2Message getAxis2message() {
        return axis2message;
    }

    public void setAxis2message(Axis2Message axis2message) {
        this.axis2message = axis2message;
    }
}
