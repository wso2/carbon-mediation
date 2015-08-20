/*
 * Copyright WSO2, Inc. (http://wso2.com)
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.wso2.carbon.cloud.gateway.agent;

import org.apache.axiom.om.OMAbstractFactory;
import org.apache.axiom.om.OMElement;
import org.apache.axiom.om.OMFactory;
import org.apache.axiom.om.OMNode;
import org.apache.axiom.om.impl.builder.StAXOMBuilder;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.synapse.util.SynapseBinaryDataSource;
import org.wso2.carbon.authenticator.stub.LoginAuthenticationExceptionException;
import org.wso2.carbon.cloud.gateway.agent.client.AuthenticationClient;
import org.wso2.carbon.cloud.gateway.common.CGConstant;
import org.wso2.carbon.cloud.gateway.common.CGException;
import org.wso2.carbon.cloud.gateway.common.CGServerBean;
import org.wso2.carbon.cloud.gateway.common.CGUtils;
import org.wso2.carbon.core.util.CryptoException;
import org.wso2.carbon.core.util.CryptoUtil;
import org.wso2.carbon.registry.api.Collection;
import org.wso2.carbon.registry.api.RegistryException;
import org.wso2.carbon.registry.api.Resource;
import org.wso2.carbon.registry.core.jdbc.utils.Transaction;

import javax.activation.DataHandler;
import java.io.BufferedInputStream;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.net.MalformedURLException;
import java.net.SocketException;
import java.net.URL;
import java.net.URLConnection;
import java.rmi.RemoteException;

public class CGAgentUtils {

    private static Log log = LogFactory.getLog(CGAgentUtils.class);

    /**
     * Prevents this utility class being instantiated.
     */
    private CGAgentUtils(){
    }
    
    /**
     * Returns the session cookie given the admin credentials
     *
     * @param serverUrl  the server url
     * @param userName   user name
     * @param password   password
     * @param domainName Domain Name
     * @param hostName   host name of the remote server
     * @return the session cookie
     * @throws java.net.SocketException throws in case of a socket exception
     * @throws org.wso2.carbon.authenticator.stub.LoginAuthenticationExceptionException
     *                                  throws in case of a authentication failure
     * @throws java.rmi.RemoteException throws in case of a connection failure
     */
    public static String getSessionCookie(String serverUrl, String userName, String password,
                                          String domainName, String hostName) throws
            SocketException, RemoteException, LoginAuthenticationExceptionException {
        AuthenticationClient authClient = new AuthenticationClient();
        return authClient.getSessionCookie(serverUrl, userName, password, hostName, domainName);
    }

    /**
     * Create the {@link org.wso2.carbon.cloud.gateway.common.CGServerBean} from the registry resource
     *
     * @param resource the csg server meta information collection
     * @return the CSGServer bean created from the meta information
     * @throws org.wso2.carbon.cloud.gateway.common.CGException
     *          throws in case of an error
     */
    public static CGServerBean getCGServerBean(Resource resource) throws CGException {
        CGServerBean bean = new CGServerBean();
        try {
            bean.setHost(resource.getProperty(CGConstant.CG_SERVER_HOST));
            bean.setName(resource.getProperty(CGConstant.CG_SERVER_NAME));
            bean.setUserName(resource.getProperty(CGConstant.CG_SERVER_USER_NAME));
            bean.setPort(resource.getProperty(CGConstant.CG_SERVER_PORT));
            bean.setDomainName(resource.getProperty(CGConstant.CG_SERVER_DOMAIN_NAME));

            CryptoUtil cryptoUtil = CryptoUtil.getDefaultCryptoUtil();
            String plainPassWord = new String(cryptoUtil.base64DecodeAndDecrypt(
                    resource.getProperty(CGConstant.CG_SERVER_PASS_WORD)));
            bean.setPassWord(plainPassWord);

            return bean;
        } catch (CryptoException e) {
            throw new CGException("Could not get the CG server information from the resource: " +
                    resource, e);
        }
    }

    /**
     * Persist the server into registry
     *
     * @param registry  registry instance
     * @param csgServer csg server instance
     * @throws org.wso2.carbon.cloud.gateway.common.CGException throws in case of an error
     */
    public static void persistServer(org.wso2.carbon.registry.core.Registry registry,
                                     CGServerBean csgServer) throws CGException {
        boolean isTransactionAlreadyStarted = Transaction.isStarted();
        boolean isTransactionSuccess = true;
        try {
            if (!isTransactionAlreadyStarted) {
                // start a transaction only if we are not in one.
                registry.beginTransaction();
            }

            Collection collection = registry.newCollection();
            if (!registry.resourceExists(CGConstant.REGISTRY_CG_RESOURCE_PATH)) {
                registry.put(CGConstant.REGISTRY_CG_RESOURCE_PATH, collection);
                if (!registry.resourceExists(CGConstant.REGISTRY_SERVER_RESOURCE_PATH)) {
                    registry.put(CGConstant.REGISTRY_SERVER_RESOURCE_PATH, collection);
                }
            }

            Resource resource = registry.newResource();
            resource.addProperty(CGConstant.CG_SERVER_NAME, csgServer.getName());
            resource.addProperty(CGConstant.CG_SERVER_HOST, csgServer.getHost());
            resource.addProperty(CGConstant.CG_SERVER_USER_NAME, csgServer.getUserName());
            resource.addProperty(CGConstant.CG_SERVER_PORT, csgServer.getPort());
            resource.addProperty(CGConstant.CG_SERVER_DOMAIN_NAME, csgServer.getDomainName());

            CryptoUtil cryptoUtil = CryptoUtil.getDefaultCryptoUtil();
            resource.addProperty(CGConstant.CG_SERVER_PASS_WORD,
                    cryptoUtil.encryptAndBase64Encode(csgServer.getPassWord()
                            .getBytes()));

            registry.put(CGConstant.REGISTRY_SERVER_RESOURCE_PATH + "/" + csgServer.getName(),
                    resource);

        } catch (Exception e) {
            isTransactionSuccess = false;
            throw new CGException("Error occurred while saving the content into registry", e);
        } finally {
            if (!isTransactionAlreadyStarted) {
                try {
                    if (isTransactionSuccess) {
                        // commit the transaction since we started it.
                        registry.commitTransaction();
                    } else {
                        registry.rollbackTransaction();
                    }
                } catch (RegistryException re) {
                    log.error("Error occurred while trying to rollback or commit the transaction",
                            re);
                }
            }

        }
    }

    /**
     * Check if we have a client axis2.xml ( for example in ESB)
     *
     * @return true if we have client axis2.xml, false otherwise
     */
    public static boolean isClientAxis2XMLExists() {
        File f = new File(CGConstant.CLIENT_AXIS2_XML);
        return f.exists();
    }

    public static OMNode getOMElementFromURI(String wsdlURI) throws CGException {
        if (wsdlURI == null || "null".equals(wsdlURI)) {
            throw new CGException("Can't create URI from a null value");
        }
        URL url;
        try {
            url = new URL(wsdlURI);
        } catch (MalformedURLException e) {
            throw new CGException("Invalid URI reference '" + wsdlURI + "'", e);
        }
        URLConnection connection;
        connection = getURLConnection(url);
        if (connection == null) {
            throw new CGException("Cannot create a URLConnection for given URL : " + url);
        }
        connection.setReadTimeout(getReadTimeout());
        connection.setConnectTimeout(getConnectTimeout());
        connection.setRequestProperty("Connection", "close"); // if http is being used
        InputStream inStream = null;

        try {
            inStream = connection.getInputStream();
            StAXOMBuilder builder = new StAXOMBuilder(inStream);
            OMElement doc = builder.getDocumentElement();
            doc.build();
            return doc;
        } catch (Exception e) {
            if (log.isDebugEnabled()) {
                log.debug("Reading as XML failed due to ", e);
            }
            return readNonXML(url);
        } finally {
            try {
                if (inStream != null) {
                    inStream.close();
                }
            } catch (IOException e) {
                log.warn("Error while closing the input stream to: " + url, e);
            }
        }
    }

    private static int getReadTimeout() {
        return Integer.parseInt(CGUtils.getStringProperty(
                CGConstant.READTIMEOUT,
                String.valueOf(CGConstant.DEFAULT_READTIMEOUT)));

    }

    private static int getConnectTimeout() {
        return Integer.parseInt(CGUtils.getStringProperty(
                CGConstant.CONNECTTIMEOUT,
                String.valueOf(CGConstant.DEFAULT_CONNECTTIMEOUT)));
    }

    private static OMNode readNonXML(URL url) throws CGException {

        try {
            // Open a new connection
            URLConnection newConnection = getURLConnection(url);
            if (newConnection == null) {
                if (log.isDebugEnabled()) {
                    log.debug("Cannot create a URLConnection for given URL : " + url);
                }
                return null;
            }

            BufferedInputStream newInputStream = new BufferedInputStream(
                    newConnection.getInputStream());

            OMFactory omFactory = OMAbstractFactory.getOMFactory();
            return omFactory.createOMText(
                    new DataHandler(new SynapseBinaryDataSource(newInputStream,
                            newConnection.getContentType())), true);

        } catch (IOException e) {
            throw new CGException("Error when getting a stream from resource's content", e);
        }
    }

    private static URLConnection getURLConnection(URL url) throws CGException {
        URLConnection connection;
        if (url.getProtocol().equalsIgnoreCase("https")) {
            String msg = "Connecting through doesn't support";
            log.error(msg);
            throw new CGException(msg);
        } else {
            try {
                connection = url.openConnection();
            } catch (IOException e) {
                throw new CGException("Could not open the URL connection", e);
            }
        }
        connection.setReadTimeout(getReadTimeout());
        connection.setConnectTimeout(getConnectTimeout());
        connection.setRequestProperty("Connection", "close"); // if http is being used
        return connection;
    }
    
    public static OMElement getWSDLElement(String wsdlLocation) throws CGException {
        OMNode wsdNode = getOMElementFromURI(wsdlLocation);
        OMElement wsdlElement;
        if (wsdNode instanceof OMElement) {
            wsdlElement = (OMElement) wsdNode;
            String asString = wsdlElement.toString();
        } else {
            throw new CGException("Invalid instance type detected when parsing the WSDL '" +
                                   wsdlLocation + "'. Required OMElement type!");
        }
        return wsdlElement;
    }
}
