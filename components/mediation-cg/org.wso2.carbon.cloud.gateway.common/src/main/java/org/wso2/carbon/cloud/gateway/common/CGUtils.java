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
package org.wso2.carbon.cloud.gateway.common;

import java.io.*;
import java.net.Socket;
import java.net.SocketException;
import java.util.List;
import java.util.Map;
import java.util.Properties;
import java.util.concurrent.BlockingQueue;

import org.apache.axis2.AxisFault;
import org.apache.axis2.transport.http.HTTPConstants;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.thrift.protocol.TBinaryProtocol;
import org.apache.thrift.protocol.TProtocol;
import org.apache.thrift.transport.TSSLTransportFactory;
import org.apache.thrift.transport.TTransport;
import org.apache.thrift.transport.TTransportException;
import org.wso2.carbon.base.ServerConfiguration;
import org.wso2.carbon.cloud.gateway.common.thrift.gen.CloudGatewayService;
import org.wso2.carbon.cloud.gateway.common.thrift.gen.Message;
import org.wso2.carbon.core.RegistryResources;
import org.wso2.carbon.core.util.CryptoException;
import org.wso2.carbon.core.util.CryptoUtil;
import org.wso2.carbon.utils.NetworkUtils;

public class CGUtils {

    private static final Log log = LogFactory.getLog(CGUtils.class);

    private static Properties prop;

    static {
        prop = loadProperties("cg.properties");
    }

    /**
     * Prevents this utility class being instantiated
     */
    private CGUtils() {

    }

    /**
     * Returns an instance of CG_TRANSPORT_NAME thrift client
     *
     * @param hostName           thrift server host name
     * @param port               thrift server port client should connect to
     * @param timeOut            the thrift client timeout
     * @param trustStorePath     the trust store to use for this client
     * @param trustStorePassWord the password of the trust store
     * @return a CG_TRANSPORT_NAME thrift client
     */
    public static CloudGatewayService.Client getCGThriftClient(
            final String hostName,
            final int port,
            final int timeOut,
            final String trustStorePath,
            final String trustStorePassWord) {
        try {
            TSSLTransportFactory.TSSLTransportParameters params =
                    new TSSLTransportFactory.TSSLTransportParameters();

            params.setTrustStore(trustStorePath, trustStorePassWord);

            TTransport transport = TSSLTransportFactory.getClientSocket(
                    hostName,
                    port,
                    timeOut,
                    params);
            TProtocol protocol = new TBinaryProtocol(transport);

            return new CloudGatewayService.Client(protocol);
        } catch (TTransportException e) {
            handleException("Could not initialize the Thrift client. " + e.getMessage(), e);
        }
        return null;
    }

    /**
     * Move elements between buffers. No need of additional synchronization locks,
     * BlockingQueue#drainTo is thread safe, but not atomic, which is not a problem.
     * See {@link BlockingQueue#drainTo(java.util.Collection, int)}
     *
     * @param src       source buffer
     * @param dest      destination buffer
     * @param blockSize blockSize of message bulk that need to move
     * @throws AxisFault in case of drains fails
     */
    public static void moveElements(BlockingQueue<Message> src,
                                    List<Message> dest,
                                    final int blockSize) throws AxisFault {
        try {
            src.drainTo(dest, blockSize);
        } catch (Exception e) {
            throw new AxisFault(e.getMessage(), e);
        }
    }

    public static String getKeyStoreFilePath() {
        return CGUtils.getStringProperty(CGConstant.KEY_STORE_FILE_LOCATION, null) != null
                ? CGUtils.getStringProperty(CGConstant.KEY_STORE_FILE_LOCATION, null) :
                getWSO2KeyStoreFilePath();
    }

    public static String getWSO2KeyStoreFilePath() {
        ServerConfiguration config = ServerConfiguration.getInstance();
        return config.getFirstProperty(
                RegistryResources.SecurityManagement.SERVER_PRIMARY_KEYSTORE_FILE);
    }

    public static String getTrustStoreFilePath() {
        return CGUtils.getStringProperty(CGConstant.TRUST_STORE_FILE_LOCATION, null) != null
                ? CGUtils.getStringProperty(CGConstant.TRUST_STORE_FILE_LOCATION, null) :
                getWSO2TrustStoreFilePath();
    }

    public static String getWSO2TrustStoreFilePath() {
        ServerConfiguration config = ServerConfiguration.getInstance();
        return config.getFirstProperty("Security.TrustStore.Location");
    }

    public static String getKeyStorePassWord() {
        return CGUtils.getStringProperty(CGConstant.KEY_STORE_PASSWORD, null) != null
                ? CGUtils.getStringProperty(CGConstant.KEY_STORE_PASSWORD, null) :
                getWSO2KeyStorePassword();
    }

    public static String getWSO2KeyStorePassword() {
        ServerConfiguration config = ServerConfiguration.getInstance();
        String password = config.getFirstProperty(
                RegistryResources.SecurityManagement.SERVER_PRIVATE_KEY_PASSWORD);
        if (password == null) {
            password = "wso2carbon";
        }
        return password;
    }

    public static String getTrustStorePassWord() {
        return CGUtils.getStringProperty(CGConstant.TRUST_STORE_PASSWORD, null) != null
                ? CGUtils.getStringProperty(CGConstant.TRUST_STORE_PASSWORD, null) :
                getWSO2TrustStorePassword();
    }

    public static String getWSO2TrustStorePassword() {
        ServerConfiguration config = ServerConfiguration.getInstance();
        String password = config.getFirstProperty("Security.TrustStore.Password");
        if (password == null) {
            password = "wso2carbon";
        }
        return password;
    }

    public static void handleException(String msg, Throwable t) {
        log.error(msg, t);
        throw new RuntimeException(msg, t);
    }

    public static String getStringProperty(String name, String def) {
        String val = System.getProperty(name);
        return val == null ?
                (prop.get(name) == null ? def : (String) prop.get(name)) :
                val;
    }

    public static int getIntProperty(String name, int def) {
        String val = System.getProperty(name);
        return val == null ?
                (prop.get(name) == null ? def : Integer.parseInt((String) prop.get(name))) :
                Integer.parseInt(val);
    }

    public static long getLongProperty(String name, long def) {
        String val = System.getProperty(name);
        return val == null ?
                (prop.get(name) == null ? def : Long.parseLong((String) prop.get(name))) :
                Long.parseLong(val);
    }

    public static double getDoubleProperty(String name, double def) {
        String val = System.getProperty(name);
        return val == null ?
                (prop.get(name) == null ? def : Double.parseDouble((String) prop.get(name))) :
                Double.parseDouble(val);
    }

    public static Boolean getBooleanProperty(String name, boolean def) {
        String val = System.getProperty(name);
        return val == null ?
                (prop.get(name) == null ? def : Boolean.parseBoolean((String) prop.get(name))) :
                Boolean.parseBoolean(val);
    }

    public static String getQueueNameFromEPR(String targetEPR) {
        return targetEPR.substring(CGConstant.CG_TRANSPORT_PREFIX.length());
    }

    public static String getFullUserName(String userName, String domainName) {
        return domainName == null || "".equals(domainName) ? userName : userName + "@" + domainName;
    }

    public static String getCSGServiceName(String serviceName, String userName) {
        // default will be;
        // test1@test1.org-SimpleStockQuoteService-Proxy
        // admin-SimpleStockQuoteService-Proxy
        userName = getStringProperty(CGConstant.CG_PROXY_PREFIX, userName);
        String delimiter = getStringProperty(CGConstant.CG_PROXY_DELIMITER, "-");
        return userName + delimiter + serviceName;
    }

    public static String getCGEPR(String tenantName, String serverName, String serviceName) {
        // multi-tenant case -> cg://tenant-name/server-name/service-name
        // standalone case  -> cg://server-name/service-name
        return CGConstant.CG_TRANSPORT_PREFIX + (tenantName != null ? tenantName + "/" : "") +
                serverName + "/" + serviceName;
    }

    public static String getCGThriftServerHostName() throws SocketException {
        String hostName = CGUtils.getStringProperty(CGConstant.THRIFT_SERVER_HOST_NAME, null);
        if (hostName == null) {
            hostName = NetworkUtils.getLocalHostname();
        }
        return hostName;
    }

    public static int getCGThriftServerPort() {
        int port = CGUtils.getIntProperty(CGConstant.THRIFT_SERVER_PORT, -1);

        if (port == -1) {  // user haven't provided any port via a system property
            ServerConfiguration config = ServerConfiguration.getInstance();
            String portStr = config.getFirstProperty(CGConstant.CG_CARBON_PORT);
            if (!"".equals(portStr) && portStr != null) {
                try {
                    port = Integer.parseInt(portStr);
                } catch (NumberFormatException e) {
                    port = CGConstant.DEFAULT_PORT;
                }
            } else {
                port = CGConstant.DEFAULT_PORT;
            }
        }
        return port;
    }

    public static String getPortFromServerURL(String serverURL) {
        //https://localhost:9443/ or //https://localhost:9444
        String socket = getSocketStringFromServerURL(serverURL);
        return socket.substring(socket.indexOf(':') + 1);
    }

    public static String getHostFromServerURL(String serverURL) {
        String socket = getSocketStringFromServerURL(serverURL);
        return socket.substring(0, socket.indexOf(':'));
    }

    public static String getUserNameFromTenantUserName(String tenantUserName) {
        return tenantUserName.contains("@") ? tenantUserName.substring(0, tenantUserName.indexOf('@')) : tenantUserName;
    }

    public static String getDomainNameFromTenantUserName(String tenantUserName) {
        return tenantUserName.contains("@") ? tenantUserName.substring(tenantUserName.indexOf('@') + 1) : null;
    }

    public static String getTryItURLFromWSDLURL(String wsdlURL) {
        //http://localhost:8280/services/SimpleStockQuoteService?wsdl ->
        //http://localhost:8280/services/SimpleStockQuoteService?tryit
        return wsdlURL.substring(0, wsdlURL.indexOf("?wsdl")) + "?tryit";
    }

    private static String getSocketStringFromServerURL(String serverURL) {
        String socket = serverURL.substring("https://".length());
        if (socket.contains("/")) {
            socket = socket.replace("/", "");
        }
        return socket;
    }


    private static Properties loadProperties(String filePath) {
        Properties properties = new Properties();
        ClassLoader cl = Thread.currentThread().getContextClassLoader();

        if (log.isDebugEnabled()) {
            log.debug("Loading a file '" + filePath + "' from classpath");
        }

        InputStream in = cl.getResourceAsStream(filePath);
        if (in == null) {
            if (log.isDebugEnabled()) {
                log.debug("Unable to load file  ' " + filePath + " '");
            }

            filePath = "repository/conf" +
                    File.separatorChar + filePath;
            if (log.isDebugEnabled()) {
                log.debug("Loading a file '" + filePath + "' from classpath");
            }

            in = cl.getResourceAsStream(filePath);
            if (in == null) {
                if (log.isDebugEnabled()) {
                    log.debug("Unable to load file  ' " + filePath + " '");
                }
            }
        }
        if (in != null) {
            try {
                properties.load(in);
            } catch (IOException e) {
                String msg = "Error loading properties from a file at :" + filePath;
                log.error(msg, e);
            }
        }
        return properties;
    }

    public static String getContentType(Map<String, String> trpHeaders) {
        // Following constant seems to be incorrectly deprecated, see source,
        // org.apache.axis2.transport.http.HTTPConstants.CONTENT_TYPE
        return trpHeaders.get(HTTPConstants.CONTENT_TYPE);
    }

    public static String getPlainToken(String encryptedToken) throws CryptoException {
        CryptoUtil cryptoUtil = CryptoUtil.getDefaultCryptoUtil();
        String token = "";
        if (encryptedToken != null) {
            token = new String(cryptoUtil.base64DecodeAndDecrypt(encryptedToken));
        }
        return token;
    }

    public static boolean isServerAlive(String host, int port) {
        Socket s = null;
        boolean isAlive = true;
        try {
            s = new Socket(host, port);
        } catch (IOException e) {
            isAlive = false;
        } finally {
            if (s != null) {
                try {
                    s.close();
                } catch (IOException e) {
                    // ignore
                }
            }
        }
        return isAlive;
    }

    public static String[] getPermissionsList() {
        String[] permissionList;
        String permissionString = CGUtils.getStringProperty(
                CGConstant.CG_USER_PERMISSION_LIST, null);
        if (permissionString == null) {
            permissionList = CGConstant.CG_USER_DEFAULT_PERMISSION_LIST;
        } else {
            // permission string can be configured as a system property as
            // csg-user-permission-list=permission1,permission2,permission3 etc..
            permissionList = new String[]{};
            int i = 0;
            for (String permission : permissionString.split(",")) {
                permissionList[++i] = permission.trim();
            }
        }
        return permissionList;
    }

    public static void dumpBytesAsString(ByteArrayOutputStream baos) {
        byte b[] = baos.toByteArray();
        for (byte i : b) {
            System.out.print((char) i);
        }
        System.out.println();
    }

    public static void dumpInputStreamAsString(InputStream is) throws IOException {
        BufferedReader br = new BufferedReader(new InputStreamReader(is));
        StringBuilder sb = new StringBuilder();
        String line;
        while ((line = br.readLine()) != null) {
            sb.append(line);
        }
        System.out.println(sb.toString());
    }

    public static void dumpStringMap(Map<String, String> map) {
        for (Map.Entry<String, String> entry : map.entrySet()) {
            System.out.println(entry.getKey() + "/" + entry.getValue());
        }
    }
}
