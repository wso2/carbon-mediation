package org.wso2.carbon.mediator.bam.config.internal;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.osgi.framework.ServiceRegistration;
import org.osgi.service.component.ComponentContext;
import org.wso2.carbon.mediator.bam.config.services.utils.ServiceHolder;
import org.wso2.carbon.registry.core.service.RegistryService;

/**
 * @scr.component name="org.wso2.carbon.mediator.bam.config" immediate="true"
 * @scr.reference name="registry.service"
 * interface="org.wso2.carbon.registry.core.service.RegistryService" cardinality="1..1"
 * policy="dynamic" bind="setRegistryService" unbind="unsetRegistryService"
 */
public class RegistryManagementComponent {

    private static Log log = LogFactory.getLog(RegistryManagementComponent.class);

    private ServiceRegistration activityServiceRegistration = null;

    protected void activate(ComponentContext context) {
        // TODO: uncomment when the backend-frontend seperation when running in same vm is completed
        //activityServiceRegistration = context.getBundleContext().registerService(
        //        IActivityService.class.getName(), new ActivityService(), null);
        log.debug("******* Registry Management bundle is activated ******* ");
    }

    protected void deactivate(ComponentContext context) {
        if (activityServiceRegistration != null) {
            activityServiceRegistration.unregister();
            activityServiceRegistration = null;
        }
        log.debug("******* Registry Management bundle is deactivated ******* ");
    }

    protected void setRegistryService(RegistryService registryService) {
        ServiceHolder.setRegistryService(registryService);
    }

    protected void unsetRegistryService(RegistryService registryService) {
        ServiceHolder.setRegistryService(null);
    }

}
