package org.wso2.carbon.mediator.bam.config.internal;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.osgi.framework.ServiceRegistration;
import org.osgi.service.component.ComponentContext;
import org.wso2.carbon.mediator.bam.config.services.utils.ServiceHolder;
import org.wso2.carbon.registry.core.service.RegistryService;
import org.osgi.service.component.annotations.Activate;
import org.osgi.service.component.annotations.Component;
import org.osgi.service.component.annotations.Deactivate;
import org.osgi.service.component.annotations.Reference;
import org.osgi.service.component.annotations.ReferenceCardinality;
import org.osgi.service.component.annotations.ReferencePolicy;

@Component(
        name = "org.wso2.carbon.mediator.bam.config",
        immediate = true)
public class RegistryManagementComponent {

    private static Log log = LogFactory.getLog(RegistryManagementComponent.class);

    private ServiceRegistration activityServiceRegistration = null;

    @Activate
    protected void activate(ComponentContext context) {
        // TODO: uncomment when the backend-frontend seperation when running in same vm is completed
        // activityServiceRegistration = context.getBundleContext().registerService(
        // IActivityService.class.getName(), new ActivityService(), null);
        log.debug("******* Registry Management bundle is activated ******* ");
    }

    @Deactivate
    protected void deactivate(ComponentContext context) {

        if (activityServiceRegistration != null) {
            activityServiceRegistration.unregister();
            activityServiceRegistration = null;
        }
        log.debug("******* Registry Management bundle is deactivated ******* ");
    }

    @Reference(
            name = "registry.service",
            service = org.wso2.carbon.registry.core.service.RegistryService.class,
            cardinality = ReferenceCardinality.MANDATORY,
            policy = ReferencePolicy.DYNAMIC,
            unbind = "unsetRegistryService")
    protected void setRegistryService(RegistryService registryService) {

        ServiceHolder.setRegistryService(registryService);
    }

    protected void unsetRegistryService(RegistryService registryService) {

        ServiceHolder.setRegistryService(null);
    }
}
