package org.wso2.carbon.mediation.artifactuploader.ui;

import org.apache.axis2.AxisFault;
import org.apache.axis2.Constants;
import org.apache.axis2.client.Options;
import org.apache.axis2.client.ServiceClient;
import org.apache.axis2.context.ConfigurationContext;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.wso2.carbon.mediation.artifactuploader.stub.SynapseArtifactUploaderAdminStub;

import javax.activation.DataHandler;
import java.rmi.RemoteException;
import java.util.Locale;

/**
 * Client wrapper class for SynapseArtifactUploader back-end service
 */
public class SynapseArtifactUploaderClient {

    private static final Log log = LogFactory.getLog(SynapseArtifactUploaderClient.class);

    public SynapseArtifactUploaderAdminStub stub;

    public SynapseArtifactUploaderClient(String cookie, String backendServerURL,
                                         ConfigurationContext configCtx, Locale locale)
            throws AxisFault {

        String serviceURL = backendServerURL + "SynapseArtifactUploaderAdmin";
        stub = new SynapseArtifactUploaderAdminStub(configCtx, serviceURL);
        ServiceClient client = stub._getServiceClient();
        Options option = client.getOptions();
        option.setManageSession(true);
        option.setProperty(org.apache.axis2.transport.http.HTTPConstants.COOKIE_STRING, cookie);
        option.setProperty(Constants.Configuration.ENABLE_MTOM, Constants.VALUE_TRUE);
    }

    public boolean uploadArtifact(String fileName, DataHandler dataHandler) throws AxisFault {
        try {
            stub.uploadArtifact(fileName, dataHandler);
        } catch (RemoteException ex) {
            handleException("Cannot Upload Artifact", ex);
        }
        return true;
    }

    public String[] getArtifacts() throws AxisFault {
        try {
            return stub.getArtifacts();
        } catch (RemoteException e) {
            handleException("Unable to retrieve artifact List", e);
        }
        return new String[0];
    }

    private void handleException(String msg, Exception ex) throws AxisFault {
        log.error(msg, ex);
        throw new AxisFault(msg, ex);
    }

    public boolean removeArtifact(String fileName) throws AxisFault {
        try {
            return stub.removeArtifact(fileName);
        } catch (RemoteException e) {
            handleException("Unable to remove artifact", e);
        }
        return false;
    }
}
