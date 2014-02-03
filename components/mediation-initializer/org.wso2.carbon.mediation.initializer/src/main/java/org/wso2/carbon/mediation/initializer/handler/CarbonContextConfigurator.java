package org.wso2.carbon.mediation.initializer.handler;

import org.apache.axis2.AxisFault;
import org.apache.axis2.addressing.EndpointReference;
import org.apache.axis2.context.MessageContext;
import org.apache.axis2.handlers.AbstractHandler;
import org.wso2.carbon.base.MultitenantConstants;
import org.wso2.carbon.context.PrivilegedCarbonContext;

/**
 * This handler is use to set ThreadLocal carbon context when message is comes via non-servlet transports
 */

public class CarbonContextConfigurator extends AbstractHandler {

    public InvocationResponse invoke(MessageContext messageContext) throws AxisFault {

        PrivilegedCarbonContext cc = PrivilegedCarbonContext.getThreadLocalCarbonContext();

        //if cc is already populated ( servlet transport ) just return
        //TODO: Ideally only tenant domain need to check, but there are some admin calls comes with null domain and tenant ID
        if (cc.getTenantDomain() != null || cc.getTenantId() != MultitenantConstants.INVALID_TENANT_ID) {
            return InvocationResponse.CONTINUE;
        }

        //for non-http we assume that it's for ST
        if (messageContext.getTransportIn() != null && messageContext.getTransportIn().getName() != null &&
                !messageContext.getTransportIn().getName().contains("http")) {
            cc.setTenantDomain(MultitenantConstants.SUPER_TENANT_DOMAIN_NAME);
            cc.setTenantId(MultitenantConstants.SUPER_TENANT_ID);
            return InvocationResponse.CONTINUE;
        }

        try {
            EndpointReference epr = messageContext.getTo();
            if (epr != null) {
                String to = epr.getAddress();
                if (to != null && to.indexOf("/" + MultitenantConstants.TENANT_AWARE_URL_PREFIX + "/") != -1) {
                    String str1 = to.substring(to.indexOf("/t/") + 3);
                    String domain = str1.substring(0, str1.indexOf("/"));
                    cc.setTenantDomain(domain, true);
                } else {
                    cc.setTenantDomain(MultitenantConstants.SUPER_TENANT_DOMAIN_NAME);
                    cc.setTenantId(MultitenantConstants.SUPER_TENANT_ID);
                }
            }
        } catch (Throwable ignore) {
            //don't care if anything failed
        }
        return InvocationResponse.CONTINUE;
    }
}
