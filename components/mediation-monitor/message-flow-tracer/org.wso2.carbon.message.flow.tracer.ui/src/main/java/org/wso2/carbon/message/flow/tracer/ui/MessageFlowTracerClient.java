package org.wso2.carbon.message.flow.tracer.ui;

import org.apache.axis2.client.Options;
import org.apache.axis2.client.ServiceClient;
import org.apache.axis2.context.ConfigurationContext;
import org.apache.synapse.messageflowtracer.data.xsd.MessageFlowComponentEntry;
import org.apache.synapse.messageflowtracer.data.xsd.MessageFlowTraceEntry;
import org.wso2.carbon.message.flow.tracer.data.xsd.Edge;
import org.wso2.carbon.message.flow.tracer.stub.MessageFlowTracerServiceStub;

import java.rmi.RemoteException;

public class MessageFlowTracerClient {
    private MessageFlowTracerServiceStub stub;

    public MessageFlowTracerClient(ConfigurationContext configCtx, String backendServerURL, String cookie) throws Exception{
        String serviceURL = backendServerURL + "MessageFlowTracerService";
        stub = new MessageFlowTracerServiceStub(configCtx, serviceURL);
        ServiceClient client = stub._getServiceClient();
        Options options = client.getOptions();
        options.setManageSession(true);
        options.setProperty(org.apache.axis2.transport.http.HTTPConstants.COOKIE_STRING, cookie);
    }

    public MessageFlowTraceEntry[] getMessageFlows() throws Exception{
        try{
            return stub.getMessageFlows();
        }catch (RemoteException e) {
            String msg = "Error"
                    + " . Backend service may be unavailable";
            throw new Exception(msg, e);
        }
    }

    public String[] getMessageFlowTraceInLevel(String messageId) throws Exception {
        try {
            return stub.getMessageFlowInLevels(messageId);
        } catch (RemoteException e) {
            String msg = "Error"
                    + " . Backend service may be unavailable";
            throw new Exception(msg, e);
        }
    }

    public String getAllComponents(String messageId) throws Exception {
        try {
            return stub.getAllComponents(messageId);
        } catch (RemoteException e) {
            String msg = "Error"
                    + " . Backend service may be unavailable";
            throw new Exception(msg, e);
        }
    }

    public Edge[] getAllEdges(String messageId) throws Exception{
        try {
            return stub.getAllEdges(messageId);
        } catch (RemoteException e) {
            String msg = "Error"
                    + " . Backend service may be unavailable";
            throw new Exception(msg, e);
        }
    }

    public MessageFlowComponentEntry[] getComponentInfo(String messageId, String componentId) throws Exception {
        try {
            return stub.getComponentInfo(messageId,componentId);
        } catch (RemoteException e) {
            String msg = "Error"
                    + " . Backend service may be unavailable";
            throw new Exception(msg, e);
        }
    }

    public void clearAll() throws Exception {
        try {
            stub.clearAll();
        } catch (RemoteException e) {
            String msg = "Error"
                    + " . Backend service may be unavailable";
            throw new Exception(msg, e);
        }
    }
}
