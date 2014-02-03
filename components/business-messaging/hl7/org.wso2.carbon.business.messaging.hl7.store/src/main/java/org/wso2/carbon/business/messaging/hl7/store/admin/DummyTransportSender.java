package org.wso2.carbon.business.messaging.hl7.store.admin;

import org.apache.axis2.AxisFault;
import org.apache.axis2.context.ConfigurationContext;
import org.apache.axis2.context.MessageContext;
import org.apache.axis2.description.HandlerDescription;
import org.apache.axis2.description.Parameter;
import org.apache.axis2.description.TransportOutDescription;
import org.apache.axis2.transport.TransportSender;

public class DummyTransportSender implements TransportSender {

    @Override
    public void cleanup(MessageContext messageContext) throws AxisFault {

    }

    @Override
    public void init(ConfigurationContext configurationContext, TransportOutDescription transportOutDescription) throws AxisFault {

    }

    @Override
    public void stop() {

    }

    @Override
    public void cleanup() {

    }

    @Override
    public void init(HandlerDescription handlerDescription) {

    }

    @Override
    public InvocationResponse invoke(MessageContext messageContext) throws AxisFault {
        return null;
    }

    @Override
    public void flowComplete(MessageContext messageContext) {

    }

    @Override
    public HandlerDescription getHandlerDesc() {
        return null;
    }

    @Override
    public String getName() {
        return "DummyTransportSender";
    }

    @Override
    public Parameter getParameter(String s) {
        return null;
    }
}
