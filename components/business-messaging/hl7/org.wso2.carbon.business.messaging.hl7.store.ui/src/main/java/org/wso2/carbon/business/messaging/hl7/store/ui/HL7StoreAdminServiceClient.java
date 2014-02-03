package org.wso2.carbon.business.messaging.hl7.store.ui;

import org.apache.axis2.AxisFault;
import org.apache.axis2.client.Options;
import org.apache.axis2.client.ServiceClient;
import org.apache.axis2.context.ConfigurationContext;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import org.wso2.carbon.business.messaging.hl7.store.entity.xsd.TransferableHL7Message;
import org.wso2.carbon.business.messaging.hl7.store.stub.HL7StoreAdminServiceStub;

import java.rmi.RemoteException;


public class HL7StoreAdminServiceClient {

    private HL7StoreAdminServiceStub stub;

    private static final String adminServiceName = "HL7StoreAdminService";

    private static Log log = LogFactory.getLog(HL7StoreAdminServiceClient.class);

    public HL7StoreAdminServiceClient(String cookie, String backendServerUrl,
                                      ConfigurationContext configurationContext) throws AxisFault {

        String serviceURL = backendServerUrl + adminServiceName;
        stub = new HL7StoreAdminServiceStub(configurationContext, serviceURL);

        ServiceClient client = stub._getServiceClient();
        Options option = client.getOptions();
        option.setManageSession(true);
        option.setProperty(org.apache.axis2.transport.http.HTTPConstants.COOKIE_STRING, cookie);
    }

    public String[] getStoreNames() {
        try {
            return stub.getHL7StoreNames();
        } catch (RemoteException e) {
            return null;
        }
    }

    public int getSize(String storeName) {
        try {
            return stub.getSize(storeName);
        } catch (RemoteException e) {
            return 0;
        }
    }

    public TransferableHL7Message[] getMessages(String storeName, int pageNumber) throws Exception {
        try {
            return stub.getMessagesPaginated(storeName, pageNumber);
        } catch (RemoteException e) {
            handleException("Could not retrieve messages from HL7 Store.");
            return null;
        }
    }

    public TransferableHL7Message getMessage(String storeName, String messageId) throws Exception {
        try {
            return stub.getMessage(storeName, messageId);
        } catch (RemoteException e) {
            handleException("Could not retrieve message " + messageId + " from HL7 Store.");
            return null;
        }
    }

    public String[] getProxServices(String storeName) throws Exception {
        try{
            return stub.getHL7Proxies(storeName);
        } catch (RemoteException e) {
            handleException("Could not retrieve proxy service list.");
            return null;
        }
    }

    public boolean sendMessage(String message, String storeName, String proxyName) throws Exception {
        try {
            return stub.sendMessage(message, storeName, proxyName);
        } catch (RemoteException e) {
            handleException("Could not send message.");
            return false;
        }
    }

    public TransferableHL7Message[] search(String storeName, String query) throws Exception {
        try {
            return stub.search(storeName, query);
        } catch (RemoteException e) {
            handleException("Could not search for messages.");
            return null;
        }
    }

    public boolean purgeMessages(String storeName) throws Exception {
        try {
            return stub.flushMessages(storeName);
        } catch (RemoteException e) {
            handleException("Could not purge store messages.");
            return false;
        }
    }

    public int getSearchSize(String storeName, String query) throws Exception {
        try {
            return stub.getSearchSize(storeName, query);
        } catch (RemoteException e) {
            handleException("Could not search for messages.");
            return 0;
        }
    }

    public String getClassName(String name) throws Exception {
        String className = null;
        try {
            if (name != null) {
                className = stub.getClassName(name);
            } else {
                handleException("Error accessing Message store" + name);
            }
        } catch (Exception e) {
            handleException(e);
        }

        return className;
    }

    private void handleException(Exception e) throws Exception {
        String message = "Error Executing HL7StoreAdminServiceClient" + e.getMessage();
        log.error(message, e);
        throw e;
    }

    private void handleException(String message) throws Exception {
        log.error(message);
        throw new Exception(message);
    }


}
