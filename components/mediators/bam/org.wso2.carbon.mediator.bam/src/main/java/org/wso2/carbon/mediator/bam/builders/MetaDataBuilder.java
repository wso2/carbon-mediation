package org.wso2.carbon.mediator.bam.builders;

import org.apache.axis2.engine.AxisConfiguration;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.synapse.MessageContext;
import org.apache.synapse.core.axis2.Axis2MessageContext;
import org.wso2.carbon.base.ServerConfiguration;
import org.wso2.carbon.context.PrivilegedCarbonContext;
import org.wso2.carbon.mediator.bam.config.BamMediatorException;
import org.wso2.carbon.mediator.bam.util.BamMediatorConstants;
import org.wso2.carbon.utils.CarbonUtils;

import java.net.Inet4Address;
import java.net.InetAddress;
import java.net.NetworkInterface;
import java.net.SocketException;
import java.util.Enumeration;

public class MetaDataBuilder {

    private static final Log log = LogFactory.getLog(MetaDataBuilder.class);
    private static final String PORTS_OFFSET = "Ports.Offset";
    private static final int CARBON_SERVER_DEFAULT_PORT = 9763;
    private String hostAddress = "";
    private boolean isHostAddressSet = false;
    public static final String HOST_NAME = "HostName";
    private static final String UNKNOWN_HOST = "UNKNOWN_HOST";

    public Object[] createMetadata(MessageContext messageContext, AxisConfiguration axisConfiguration)
            throws BamMediatorException {
        Object[] metaData = new Object[BamMediatorConstants.NUM_OF_CONST_META_PARAMS];
        int i = 0;
        try{
            if(!this.isHostAddressSet) {
                this.hostAddress = getHostAddress();
                this.isHostAddressSet = true;
            }
            metaData[i++] = PrivilegedCarbonContext.getThreadLocalCarbonContext().getTenantId();
            metaData[i++] = this.extractPropertyFromMessageContext(messageContext, "HTTP_METHOD");
            metaData[i++] = this.extractPropertyFromMessageContext(messageContext, "CHARACTER_SET_ENCODING");
            metaData[i++] = this.extractPropertyFromMessageContext(messageContext, "REMOTE_ADDR");
            metaData[i++] = this.extractPropertyFromMessageContext(messageContext, "TransportInURL");
            metaData[i++] = this.extractPropertyFromMessageContext(messageContext, "messageType");
            metaData[i++] = this.extractPropertyFromMessageContext(messageContext, "REMOTE_HOST");
            metaData[i++] = this.extractPropertyFromMessageContext(messageContext, "SERVICE_PREFIX");
            metaData[i] = this.hostAddress;
            return metaData;
        } catch (Exception e) {
            String errorMsg = "Error occurred while producing values for Meta Data. " + e.getMessage();
            log.error(errorMsg, e);
            throw new BamMediatorException(errorMsg, e);
        }
    }

    private static String getHostAddress() {
        try {
            String hostAddress = ServerConfiguration.getInstance().getFirstProperty(HOST_NAME);
            if(null == hostAddress){
                hostAddress = getLocalAddress().getHostName();
                if (hostAddress == null) {
                    hostAddress = UNKNOWN_HOST;
                }
                int portsOffset = Integer.parseInt(CarbonUtils.getServerConfiguration().getFirstProperty(
                        PORTS_OFFSET));
                int portValue = CARBON_SERVER_DEFAULT_PORT + portsOffset;
                return hostAddress +  ":" + portValue;
            }else {
                return hostAddress.trim();
            }
        } catch (Exception e){
            String errorMsg = "Error occurred while getting the Host Address. " + e.getMessage();
            log.error(errorMsg, e);
            return "";
        }
    }

    private static InetAddress getLocalAddress(){
        Enumeration<NetworkInterface> iFaces;
        try {
            iFaces = NetworkInterface.getNetworkInterfaces();
            if (iFaces != null) {
                while (iFaces.hasMoreElements()) {
                    NetworkInterface iFace = iFaces.nextElement();
                    Enumeration<InetAddress> addresses = iFace.getInetAddresses();

                    while (addresses.hasMoreElements()) {
                        InetAddress address = addresses.nextElement();
                        if (address instanceof Inet4Address && !address.isLoopbackAddress()) {
                            return address;
                        }
                    }
                }
            }
            return null;
        } catch (SocketException e) {
            log.error("Failed to get host address", e);
            return null;
        } catch (Exception e) {
            log.error("Error occurred while getting host address", e);
            return null;
        }
    }

    private Object extractPropertyFromMessageContext(MessageContext messageContext, String propertyName){
        try{
            org.apache.axis2.context.MessageContext msgCtx = ((Axis2MessageContext) messageContext).getAxis2MessageContext();
            String output = (String)msgCtx.getLocalProperty(propertyName);
            if(output != null && !output.equals("")){
                return output;
            } else {
                return "";
            }
        } catch (Exception e) {
            String errorMsg = "Error occurred while extracting a property from Message Context. " + e.getMessage();
            log.error(errorMsg, e);
            return "";
        }
    }
}
