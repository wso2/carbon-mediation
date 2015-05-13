package org.wso2.carbon.business.messaging.hl7.store.admin;

import ca.uhn.hl7v2.HL7Exception;
import ca.uhn.hl7v2.model.Message;
import ca.uhn.hl7v2.parser.DefaultXMLParser;
import ca.uhn.hl7v2.parser.EncodingNotSupportedException;
import ca.uhn.hl7v2.parser.PipeParser;
import ca.uhn.hl7v2.util.Terser;
import ca.uhn.hl7v2.validation.impl.NoValidation;
import org.apache.axiom.om.OMElement;
import org.apache.axiom.soap.SOAPEnvelope;
import org.apache.axiom.soap.impl.llom.soap11.SOAP11Factory;
import org.apache.axis2.AxisFault;
import org.apache.axis2.Constants;
import org.apache.axis2.context.ConfigurationContext;
import org.apache.axis2.description.AxisService;
import org.apache.axis2.description.TransportOutDescription;
import org.apache.axis2.engine.AxisEngine;
import org.apache.commons.lang.StringEscapeUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.synapse.MessageContext;
import org.apache.synapse.config.SynapseConfiguration;
import org.apache.synapse.core.SynapseEnvironment;
import org.apache.synapse.core.axis2.Axis2MessageContext;
import org.apache.synapse.core.axis2.Axis2SynapseEnvironment;
import org.apache.synapse.core.axis2.ProxyService;
import org.apache.synapse.message.store.MessageStore;
import org.apache.synapse.util.PayloadHelper;
import org.wso2.carbon.business.messaging.hl7.common.HL7Constants;
import org.wso2.carbon.business.messaging.hl7.common.HL7Utils;
import org.wso2.carbon.business.messaging.hl7.store.JDBCStore;
import org.wso2.carbon.business.messaging.hl7.store.entity.PersistentHL7Message;
import org.wso2.carbon.business.messaging.hl7.store.entity.TransferableHL7Message;
import org.wso2.carbon.business.messaging.hl7.store.jpa.JPAStore;
import org.wso2.carbon.business.messaging.hl7.store.util.SerializableMessageContext;
import org.wso2.carbon.business.messaging.hl7.store.util.SerializerUtils;
import org.wso2.carbon.business.messaging.hl7.transport.HL7TransportOutInfo;
import org.wso2.carbon.mediation.initializer.AbstractServiceBusAdmin;

import javax.xml.stream.XMLStreamException;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

public class HL7StoreAdminService extends AbstractServiceBusAdmin {

    private static Log log = LogFactory.getLog(HL7StoreAdminService.class);

    public static int MSGS_PER_PAGE = 100;

    /**
     * Get all the Current Message store names defined in the configuration
     *
     * @return array of Strings that contains MessageStore names
     * @throws AxisFault
     */
    public String[] getHL7StoreNames() throws AxisFault {
        SynapseConfiguration configuration = getSynapseConfiguration();

        assert configuration != null;

        Collection<String> hl7Stores = new ArrayList<String>();
        Collection<String> allStores = configuration.getMessageStores().keySet();

        for(String name: allStores) {
            if(getClassName(name).equals(JPAStore.class.getName()) ||
                    getClassName(name).equals(JDBCStore.class.getName())) {
                hl7Stores.add(name);
            }
        }

        return hl7Stores.toArray(new String[hl7Stores.size()]);
    }

    /**
     * Get the number of messages in the Message store with given name
     *
     * @param name of the MessageStore
     * @return number of message stores in the given store
     * @throws AxisFault if Message store does not exists
     */
    public int getSize(String name) throws AxisFault {

        MessageStore store = getMessageStoreImpl(name);
        if (store != null) {
            return store.size();
        } else {
            handleException(log, "Message Store " + name + " does not exist !!!", null);
        }

        //This code block will never reach as handleException method will always returns a Exception
        return 0;
    }

    /**
     * Get the implementation class name of the MessageStore
     *
     * @param name of the MessageStore
     * @return implementation class name of the Message Store
     * @throws AxisFault
     */
    public String getClassName(String name) throws AxisFault {

        MessageStore store = getMessageStoreImpl(name);

        if (store != null) {
            return store.getClass().getName();
        } else {
            handleException(log, "Message Store " + name + " does not exist !!!", null);
        }

        //This code block will never reach as handleException method will always returns a Exception
        return null;
    }

    public List<TransferableHL7Message> getMessages(String storeName) throws AxisFault  {
        JPAStore store = (JPAStore) getMessageStoreImpl(storeName);

        if(store != null) {
            return getTransferableMessages(store, store.getMessages());
        } else {
            handleException(log, "Message Store " + storeName + " does not exist !!!", null);
        }

        return null;
    }

    public String[] getHL7Proxies(String storeName) {
        JPAStore store = (JPAStore) getMessageStoreImpl(storeName);
        Collection<ProxyService> services = store.getSynapseEnvironment().getSynapseConfiguration().getProxyServices();
        ArrayList<String> serviceNames = new ArrayList<String>();
        for(ProxyService service: services) {
            serviceNames.add(service.getName());
        }
        return serviceNames.toArray(new String[serviceNames.size()]);
    }

    private void handleError(String msg) {
        log.error(msg);
    }

    public List<TransferableHL7Message> search(String storeName, String query) throws AxisFault {
        JPAStore store = (JPAStore) getMessageStoreImpl(storeName);

        if(store != null) {
            return getTransferableMessages(store, store.search(query));
        } else {
            handleException(log, "Message Store " + storeName + " does not exist !!!", null);
        }

        return null;
    }

    public int getSearchSize(String storeName, String query) throws AxisFault {
        JPAStore store = (JPAStore) getMessageStoreImpl(storeName);

        if(store != null) {
            List<PersistentHL7Message> results = store.search(query);
            return getTransferableMessages(store, results).size();
        } else {
            handleException(log, "Message Store " + storeName + " does not exist !!!", null);
        }

        return 0;
    }

    private boolean isTrueProxyParameter(String storeName, String proxyName, String parameterName) {
        JPAStore store = (JPAStore) getMessageStoreImpl(storeName);
        SynapseEnvironment synapseEnvironment = store.getSynapseEnvironment();

        ProxyService service = synapseEnvironment.getSynapseConfiguration().getProxyService(proxyName);
        if(service.getParameterMap().containsKey(parameterName)) {
            return Boolean.valueOf((String) service.getParameterMap().get(parameterName));
        } else {
            return false;
        }
    }

    public boolean sendMessage(String msg, String storeName, String proxyName) {
        OMElement message = null;
        Message hl7Message = null;
        String hl7MessageStr = null;
        String xmlMessage = null;
        String messageControlerID = null;
        boolean validationPassed = true;
        msg = msg.replaceAll("\n", "\r\n");
        try {
            PipeParser parser = new PipeParser();
            if (!isTrueProxyParameter(storeName, proxyName, HL7Constants.HL7_VALIDATE_MESSAGE)) {
                parser.setValidationContext(new NoValidation());
            }
            hl7Message = new PipeParser().parse(msg);
            hl7MessageStr = hl7Message.encode();
            xmlMessage = new DefaultXMLParser().encode(hl7Message);
            message = HL7Utils.generateHL7MessageElement(xmlMessage);
        } catch (HL7Exception e) {
            if(isTrueProxyParameter(storeName, proxyName, HL7Constants.HL7_BUILD_RAW_MESSAGE)) {
                validationPassed = false;
                xmlMessage =  "<rawMessage>" + StringEscapeUtils.escapeXml(hl7MessageStr) + "</rawMessage>";
                try {
                    message = HL7Utils.generateHL7MessageElement(xmlMessage);
                } catch (XMLStreamException e2) {
                    handleError("Could not build XML from message. " + e.getMessage());
                    return false;
                }
            } else {
                handleError("Could not build XML from message. " + e.getMessage());
                return false;
            }
        } catch (XMLStreamException e) {
            handleError("Could not build XML from message. " + e.getMessage());
            return false;
        }

        try {
            Terser terser = new Terser(hl7Message);
            messageControlerID = terser.get("/MSH-10");
            if (messageControlerID  == null || messageControlerID .length() == 0) {
                handleError("MSH segment missing required field: Control ID (MSH-10)");
                return false;
            }
        } catch (HL7Exception e) {
            handleError("MSH segment missing required field: Control ID (MSH-10)");
            return false;
        }


        JPAStore store = (JPAStore) getMessageStoreImpl(storeName);
        SynapseEnvironment synapseEnvironment = store.getSynapseEnvironment();

        if (proxyName == null || proxyName.equals("")) {
            handleError("Proxy service name not specified");
        }

        // Prepare axis2 message context
        org.apache.axis2.context.MessageContext axis2MsgCtx =
                new org.apache.axis2.context.MessageContext();

        HL7TransportOutInfo outinfo = new HL7TransportOutInfo();
        outinfo.setMessageControllerID(messageControlerID);
        axis2MsgCtx.setProperty(Constants.OUT_TRANSPORT_INFO, outinfo);

        ConfigurationContext configurationContext = ((Axis2SynapseEnvironment) synapseEnvironment).
                getAxis2ConfigurationContext();
        axis2MsgCtx.setConfigurationContext(configurationContext);
        axis2MsgCtx.setIncomingTransportName(Constants.TRANSPORT_LOCAL);
        axis2MsgCtx.setServerSide(true);

        axis2MsgCtx.setProperty(HL7Constants.HL7_MESSAGE_OBJECT, hl7Message);
        axis2MsgCtx.setProperty(HL7Constants.HL7_VALIDATION_PASSED, validationPassed);

        axis2MsgCtx.setProperty(HL7Constants.HL7_PASS_THROUGH_INVALID_MESSAGES, isTrueProxyParameter(storeName,
                proxyName,HL7Constants.HL7_PASS_THROUGH_INVALID_MESSAGES));

        //there's no transport out when message is injected to proxy via HL7 Store UI
        TransportOutDescription dummyTransportOut = new TransportOutDescription("Dummy Transport Out");
        dummyTransportOut.setSender(new DummyTransportSender());
        axis2MsgCtx.setTransportOut(dummyTransportOut);

        try {
            AxisService axisService = configurationContext.getAxisConfiguration().
                    getService(proxyName);
            if (axisService == null) {
                handleError("Proxy Service: " + proxyName + " not found");
            }
            axis2MsgCtx.setAxisService(axisService);
        } catch (AxisFault axisFault) {
            handleError("Error occurred while attempting to find the Proxy Service");
            return false;
        }

        SOAPEnvelope envelope = new SOAP11Factory().createSOAPEnvelope();

        try {
            PayloadHelper.setXMLPayload(envelope, message.cloneOMElement());
            axis2MsgCtx.setEnvelope(envelope);
        } catch (AxisFault axisFault) {
            handleError("Error in setting the message payload : " + message);
            return false;
        }

        try {
            if (log.isDebugEnabled()) {
                log.debug("injecting message to proxy service : " + proxyName);
            }
            AxisEngine.receive(axis2MsgCtx);
        } catch (AxisFault axisFault) {
            handleError("Error occurred while invoking proxy service : " + proxyName);
            return false;
        }

        return true;
    }

    private List<TransferableHL7Message> getTransferableMessages(JPAStore store, List<PersistentHL7Message> persistentHL7Messages) throws AxisFault {
        List<TransferableHL7Message> transferableHL7Messages = new ArrayList<TransferableHL7Message>();

        for(PersistentHL7Message message: persistentHL7Messages) {
            transferableHL7Messages.add(getTransferableHL7Message(store, message));
        }

        return transferableHL7Messages;
    }

    private TransferableHL7Message getTransferableHL7Message(JPAStore store, PersistentHL7Message message) throws AxisFault {
        org.apache.axis2.context.MessageContext axis2Mc = store.newAxis2Mc();
        MessageContext synapseMc = store.newSynapseMc(axis2Mc);

        try {
            SerializableMessageContext serializableMessageContext = (SerializableMessageContext) SerializerUtils.deserialize(message.getMessage());

            MessageContext persistedMc = SerializerUtils.toMessageContext(serializableMessageContext, axis2Mc, synapseMc);
            Message hl7Message = (Message) ((Axis2MessageContext) persistedMc).getAxis2MessageContext().getProperty(HL7Constants.HL7_MESSAGE_OBJECT);

            TransferableHL7Message transferableHL7Message = new TransferableHL7Message(message.getId(),
                    message.getStoreName(),
                    message.getMessageId(),
                    message.getControlId(),
                    hl7Message.encode(),
                    persistedMc.getEnvelope().getBody().getFirstElement().getFirstElement().toString(),
                    message.getDate().toString(),
                    message.getTimestamp());

            return transferableHL7Message;
        } catch (HL7Exception e) {
            handleException(log,  "Could not encode HL7 message.", null);
        } catch (ClassNotFoundException e) {
            handleException(log, "Could not deserialize message context.", null);
        } catch (IOException e) {
            handleException(log, "Could not deserialize message context.", null);
        }

        return null;
    }

    public List<TransferableHL7Message> getMessagesPaginated(String storeName, int pageNumber) throws HL7Exception, IOException, ClassNotFoundException {
        JPAStore store = (JPAStore)  getMessageStoreImpl(storeName);

        if(store != null) {
            return getTransferableMessages(store, store.getMessages(pageNumber, MSGS_PER_PAGE));
        } else {
            handleException(log, "Message Store " + storeName + " does not exist", null);
        }

        return null;

    }

    public TransferableHL7Message getMessage(String storeName, String messageId) throws HL7Exception, IOException, ClassNotFoundException {
        JPAStore store = (JPAStore) getMessageStoreImpl(storeName);

        if(store != null) {
            return getTransferableHL7Message(store, store.getMessage(messageId));
        } else {
            handleException(log, "Message Store " + storeName + " does not exist !!!", null);
        }

        return null;
    }

    public boolean updateMessage(String storeName, String messageId, String er7Payload, String xmlPayload) {
        JPAStore store = (JPAStore) getMessageStoreImpl(storeName);

        return false;
    }

    public boolean flushMessages(String storeName) {
        JPAStore store = (JPAStore) getMessageStoreImpl(storeName);
        if(store.flushMessages() > 0) {
            return true;
        } else {
            return false;
        }
    }

    private void handleException(Log log, String message, Exception e)
            throws AxisFault {

        if (e == null) {

            AxisFault exception =
                    new AxisFault(message);
            log.error(message, exception);
            throw exception;

        } else {
            message = message + " :: " + e.getMessage();
            log.error(message, e);
            throw new AxisFault(message, e);
        }
    }

    private MessageStore getMessageStoreImpl(String name) {
        SynapseConfiguration configuration = getSynapseConfiguration();

        assert configuration != null;
        assert configuration.getMessageStore(name) instanceof JPAStore;

        return configuration.getMessageStore(name);
    }

}
