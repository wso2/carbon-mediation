package org.wso2.carbon.rest.api.service;

import com.google.gson.JsonElement;
import com.google.gson.JsonParser;
import net.minidev.json.JSONObject;
import org.apache.axiom.om.OMAttribute;
import org.apache.axiom.om.OMElement;
import org.apache.axiom.om.util.AXIOMUtil;
import org.apache.axis2.AxisFault;
import org.apache.axis2.Constants;
import org.apache.axis2.context.ConfigurationContext;
import org.apache.axis2.context.MessageContext;
import org.apache.axis2.description.Parameter;
import org.apache.axis2.description.TransportInDescription;
import org.apache.axis2.engine.AxisConfiguration;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.synapse.SynapseConstants;
import org.apache.synapse.aspects.AspectConfiguration;
import org.apache.synapse.config.SynapseConfiguration;
import org.apache.synapse.config.xml.XMLConfigConstants;
import org.apache.synapse.config.xml.rest.APIFactory;
import org.apache.synapse.config.xml.rest.APISerializer;
import org.apache.synapse.mediators.base.SequenceMediator;
import org.apache.synapse.rest.API;
import org.apache.synapse.rest.RESTConstants;
import org.apache.synapse.rest.Resource;
import org.apache.synapse.rest.dispatch.DispatcherHelper;
import org.apache.synapse.rest.dispatch.URITemplateHelper;
import org.apache.synapse.rest.dispatch.URLMappingHelper;
import org.wso2.carbon.context.PrivilegedCarbonContext;
import org.wso2.carbon.core.CarbonConfigurationContextFactory;
import org.wso2.carbon.core.multitenancy.utils.TenantAxisUtils;
import org.wso2.carbon.mediation.commons.rest.api.swagger.APIGenException;
import org.wso2.carbon.mediation.commons.rest.api.swagger.APIGenerator;
import org.wso2.carbon.mediation.commons.rest.api.swagger.GenericApiObjectDefinition;
import org.wso2.carbon.mediation.commons.rest.api.swagger.ServerConfig;
import org.wso2.carbon.mediation.commons.rest.api.swagger.SwaggerConstants;
import org.wso2.carbon.mediation.initializer.AbstractServiceBusAdmin;
import org.wso2.carbon.mediation.initializer.ServiceBusConstants;
import org.wso2.carbon.mediation.initializer.ServiceBusUtils;
import org.wso2.carbon.mediation.initializer.persistence.MediationPersistenceManager;
import org.wso2.carbon.mediation.registry.RegistryServiceHolder;
import org.wso2.carbon.registry.core.Registry;
import org.wso2.carbon.registry.core.exceptions.RegistryException;
import org.wso2.carbon.registry.core.service.RegistryService;
import org.wso2.carbon.rest.api.APIData;
import org.wso2.carbon.rest.api.APIDataSorter;
import org.wso2.carbon.rest.api.APIException;
import org.wso2.carbon.rest.api.CarbonServerConfig;
import org.wso2.carbon.rest.api.ConfigHolder;
import org.wso2.carbon.rest.api.ResourceData;
import org.wso2.carbon.rest.api.RestApiAdminUtils;
import org.wso2.carbon.utils.CarbonUtils;
import org.wso2.carbon.utils.NetworkUtils;
import org.wso2.carbon.utils.multitenancy.MultitenantConstants;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.MalformedURLException;
import java.net.SocketException;
import java.net.URL;
import java.net.URLConnection;
import java.nio.charset.Charset;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.concurrent.locks.Lock;
import javax.xml.namespace.QName;
import javax.xml.stream.XMLStreamException;

public class RestApiAdmin extends AbstractServiceBusAdmin{
	
    private static Log log = LogFactory.getLog(RestApiAdmin.class);
    private static final String TENANT_DELIMITER = "/t/";
    private static final String APPLICATION_JSON_TYPE = "application/json";
    /**
     * Registry path prefixes
     */
    private static final String CONFIG_REG_PREFIX = "conf:";
    private static final String GOV_REG_PREFIX = "gov:";
    private static final String FILE_PREFIX = "file:";

	public boolean addApi(APIData apiData) throws APIException {
		final Lock lock = getLock();
        try {
            lock.lock();
            String tenantDomain = PrivilegedCarbonContext.getThreadLocalCarbonContext().getTenantDomain(true);
            if (tenantDomain != null && !tenantDomain.isEmpty()
                    && !tenantDomain.equals(MultitenantConstants.SUPER_TENANT_DOMAIN_NAME)) {
                String tenantApiContext = apiData.getContext();
                apiData.setContext(TENANT_DELIMITER + tenantDomain + tenantApiContext);
            }
            addApi(RestApiAdminUtils.retrieveAPIOMElement(apiData), null, false);
            return true;
        } finally {
            lock.unlock();
        }
	}
	
	public boolean addApiFromString(String apiData) throws APIException {
		final Lock lock = getLock();
        try {
            lock.lock();
            OMElement apiElement = AXIOMUtil.stringToOM(apiData);
            addApi(apiElement, null, false);
            return true;
        } catch (XMLStreamException e) {
			handleException(log, "Could not parse String to OMElement", e);
			return false;
		} finally {
            lock.unlock();
        }
	}
	
	/**
	 * Set the tenant domain when a publisher publishes his API in MT mode. When publisher publishes 
	 * the API, we login the gateway as supretenant. But we need to publish the API in the particular 
	 * tenant domain.
	 *
	 * @param apiData
	 * @param tenantDomain
	 * @return
	 * @throws APIException
	 */
	public boolean addApiForTenant(String apiData, String tenantDomain) throws APIException {

		try {
			PrivilegedCarbonContext.startTenantFlow();
			PrivilegedCarbonContext.getThreadLocalCarbonContext().setTenantDomain(tenantDomain,
			                                                                      true);
			boolean status = addApiFromString(apiData);
			return status;
		} finally {
			PrivilegedCarbonContext.endTenantFlow();
		}
	}
	
	public boolean updateApi(String apiName, APIData apiData) throws APIException {
		
		final Lock lock = getLock();
        try {
            lock.lock();
            assertNameNotEmpty(apiName);
            
            API oldAPI = null;
            API api = APIFactory.createAPI(RestApiAdminUtils.retrieveAPIOMElement(apiData));
            SynapseConfiguration synapseConfiguration = getSynapseConfiguration();
            
            oldAPI = synapseConfiguration.getAPI(apiName);
            if (oldAPI != null) {
                oldAPI.destroy();
            	api.setFileName(oldAPI.getFileName());
            }
    		
            synapseConfiguration.updateAPI(apiName, api);
            api.init(getSynapseEnvironment());

            if (oldAPI.getArtifactContainerName() != null) {
                api.setIsEdited(true);
                api.setArtifactContainerName(oldAPI.getArtifactContainerName());
                apiData.setIsEdited(true);
            } else {
                if(!Boolean.parseBoolean(System.getProperty("NonRegistryMode"))) {
                    MediationPersistenceManager pm = getMediationPersistenceManager();
                    String fileName = api.getFileName();
                    pm.deleteItem(apiName, fileName, ServiceBusConstants.ITEM_TYPE_REST_API);
                    pm.saveItem(apiName, ServiceBusConstants.ITEM_TYPE_REST_API);
                }
            }
    		
    		return true;
        } finally {
            lock.unlock();
        }
	}
	
	public boolean updateApiFromString(String apiName, String apiData) throws APIException {
		
		final Lock lock = getLock();
        try {
            lock.lock();
            assertNameNotEmpty(apiName);
            
            OMElement apiElement = AXIOMUtil.stringToOM(apiData);
            
            //Set API name to old value since we do not allow editing the API name.
            OMAttribute nameAttribute = apiElement.getAttribute(new QName("name"));
            if (nameAttribute == null || nameAttribute.getAttributeValue().trim().isEmpty()) {
            	apiElement.addAttribute("name", apiName, null);
            }
            
            API api = APIFactory.createAPI(apiElement);
            
            SynapseConfiguration synapseConfiguration = getSynapseConfiguration();

            API oldAPI = synapseConfiguration.getAPI(apiName);
            if (oldAPI != null) {
                oldAPI.destroy();
            	api.setFileName(oldAPI.getFileName());
            }
                        
    		synapseConfiguration.removeAPI(apiName);
            synapseConfiguration.addAPI(api.getName(),api);
            api.init(getSynapseEnvironment());

            if (oldAPI.getArtifactContainerName() != null) {
                api.setArtifactContainerName(oldAPI.getArtifactContainerName());
                api.setIsEdited(true);
                getApiByName(api.getName()).setIsEdited(true);
            } else {
                if(!Boolean.parseBoolean(System.getProperty("NonRegistryMode"))) {
                    MediationPersistenceManager pm = getMediationPersistenceManager();
                    String fileName = api.getFileName();
                    pm.deleteItem(apiName, fileName, ServiceBusConstants.ITEM_TYPE_REST_API);
                    pm.saveItem(apiName, ServiceBusConstants.ITEM_TYPE_REST_API);
                }
            }
    		return true;
        } catch (XMLStreamException e) {
			handleException(log, "Could not parse String to OMElement", e);
			return false;
		} finally {
            lock.unlock();
        }
	}

	/**
	 * Set the tenant domain when a publisher updates his API in MT mode. When
	 * publisher updates
	 * the API, we login the gateway as supretenant. But we need to update the
	 * API,which is in the particular tenant domain.
	 * 
	 * @param apiName
	 * @param apiData
	 * @return
	 * @throws APIException
	 */
	public boolean updateApiForTenant(String apiName, String apiData, String tenantDomain) throws APIException {

		try {
			PrivilegedCarbonContext.startTenantFlow();
			PrivilegedCarbonContext.getThreadLocalCarbonContext().setTenantDomain(tenantDomain,
			                                                                      true);
			boolean status = updateApiFromString(apiName, apiData);
			return status;
		} finally {
			PrivilegedCarbonContext.endTenantFlow();
		}
	}
	
	public boolean deleteApi(String apiName) throws APIException {
		final Lock lock = getLock();
        try {
            lock.lock();
            assertNameNotEmpty(apiName);
            apiName = apiName.trim();

            SynapseConfiguration synapseConfiguration = getSynapseConfiguration();
            API api = synapseConfiguration.getAPI(apiName);

            if (api.getArtifactContainerName() == null) {
                if (log.isDebugEnabled()) {
                    log.debug("Deleting API : " + apiName + " from the configuration");
                }
                api.destroy();
                synapseConfiguration.removeAPI(apiName);

                if(!Boolean.parseBoolean(System.getProperty("NonRegistryMode"))) {
                    MediationPersistenceManager pm = getMediationPersistenceManager();
                    String fileName = api.getFileName();
                    pm.deleteItem(apiName, fileName, ServiceBusConstants.ITEM_TYPE_REST_API);
                }

                if (log.isDebugEnabled()) {
                    log.debug("Api : " + apiName + " removed from the configuration");
                }
            }
        } finally {
            lock.unlock();
        }
        return true;
	}

    /**
     * Delete Selected API
     * @param apiNames
     * @return
     * @throws APIException
     */
    public void deleteSelectedApi(String[] apiNames) throws APIException {
        final Lock lock = getLock();
        try {
            lock.lock();
            for (String apiName :apiNames ) {
                assertNameNotEmpty(apiName);
                apiName = apiName.trim();
                SynapseConfiguration synapseConfiguration = getSynapseConfiguration();
                API api = synapseConfiguration.getAPI(apiName);

                if (api.getArtifactContainerName() == null) {
                    if (log.isDebugEnabled()) {
                        log.debug("Deleting API : " + apiName + " from the configuration");
                    }

                    api.destroy();
                    synapseConfiguration.removeAPI(apiName);

                    if(!Boolean.parseBoolean(System.getProperty("NonRegistryMode"))) {
                        MediationPersistenceManager pm = getMediationPersistenceManager();
                        String fileName = api.getFileName();
                        pm.deleteItem(apiName, fileName, ServiceBusConstants.ITEM_TYPE_REST_API);
                    }

                    if (log.isDebugEnabled()) {
                        log.debug("Api : " + apiName + " removed from the configuration");
                    }
                }
            }
        } finally {
            lock.unlock();
        }

    }

    /**
     * Delete All API in the synapse configuration
     * @throws APIException
     */
    public void deleteAllApi() throws APIException {
        String[] allApiNames = this.getApiNames();
        this.deleteSelectedApi(allApiNames);
    }

	/**
	 * Set the tenant domain when a publisher deletes his API in MT mode. When
	 * publisher deletes
	 * the API, we login the gateway as supretenant. But we need to delete the
	 * API,which is in the particular tenant domain.
	 * 
	 * @param apiName
	 * @param tenantDomain
	 * @return
	 * @throws APIException
	 */
	public boolean deleteApiForTenant(String apiName, String tenantDomain) throws APIException {
		try {
			PrivilegedCarbonContext.startTenantFlow();
			PrivilegedCarbonContext.getThreadLocalCarbonContext().setTenantDomain(tenantDomain,
			                                                                      true);
			boolean status = deleteApi(apiName);
			return status;
		} finally {
			PrivilegedCarbonContext.endTenantFlow();
		}
	}
	
	
    public APIData[] getAPIsForListing(int pageNumber, int itemsPerPage) {
        final Lock lock = getLock();
        try {
            lock.lock();
            SynapseConfiguration synapseConfiguration = getSynapseConfiguration();
            Collection<API> apis = synapseConfiguration.getAPIs();

            List<APIData> apiDataList = null;
            if (apis != null) {
                apiDataList = new ArrayList<APIData>(apis.size());

                for (API api : apis) {
                    //Populate the fields we need to show
                    APIData apiData = new APIData();
                    apiData.setName(api.getName());
                    apiData.setContext(api.getContext());
                    if (api.getAspectConfiguration() != null
                        && api.getAspectConfiguration().isStatisticsEnable()) {
                        apiData.setStatisticsEnable(true);
                    } else {
                        apiData.setStatisticsEnable(false);
                    }
                    if (api.getAspectConfiguration() != null
                        && api.getAspectConfiguration().isTracingEnabled()) {
                        apiData.setTracingEnable(true);
                    } else {
                        apiData.setTracingEnable(false);
                    }
                    if (api.getArtifactContainerName() != null) {
                        apiData.setArtifactContainerName(api.getArtifactContainerName());
                    }
                    if (api.isEdited()) {
                        apiData.setIsEdited(true);
                    }
                    apiData.setSwaggerDefKey(api.getSwaggerResourcePath());
                    apiDataList.add(apiData);
                }
                //Sort APIs by name.
                Collections.sort(apiDataList, new APIDataSorter());

                int size = apiDataList.size();
                int startIndex = pageNumber * itemsPerPage;
                int endIndex = ((pageNumber + 1) * itemsPerPage);

                List<APIData> returnList = null;

                //We do not have enough APIs
                if (size <= startIndex) {
                    return null;
                }
                else if (size <= endIndex) {
                    returnList = apiDataList.subList(startIndex, size);
                }
                else {
                    returnList = apiDataList.subList(startIndex, endIndex);
                }
                return  returnList.toArray(new APIData[returnList.size()]);
            }
            return null;
        } finally {
            lock.unlock();
        }
    }

    public int getAPICount() {

        final Lock lock = getLock();
        try {
            lock.lock();
            SynapseConfiguration synapseConfiguration = getSynapseConfiguration();
            Collection<API> apis = synapseConfiguration.getAPIs();
            if(apis != null){
                return apis.size();
            }
            return 0;
        } finally {
            lock.unlock();
        }
    }
    
    public String getServerContext() throws APIException {
        AxisConfiguration configuration = null;

        try {
            configuration = ConfigHolder.getInstance().getAxisConfiguration();
        } catch (APIException e) {
            handleException(log, "Could not retrieve server context", e);
        }

        String portValue;
        String protocol;

        TransportInDescription transportInDescription = configuration.getTransportIn("http");
        if (transportInDescription == null) {
            transportInDescription = configuration.getTransportIn("https");
        }

        if (transportInDescription != null) {
            protocol = transportInDescription.getName();
            portValue = (String) transportInDescription.getParameter("port").getValue();
        } else {
            throw new APIException("http/https transport required");
        }
		
        String host;

        Parameter hostParam =  configuration.getParameter("hostname");

        if (hostParam != null) {
            host = (String)hostParam.getValue();
        }
        else {
            try {
                host = NetworkUtils.getLocalHostname();
            } catch (SocketException e) {
                log.warn("SocketException occured when trying to obtain IP address of local machine");
                host = "localhost";
            }
        }

        String serverContext = "";

        try {
            int port = Integer.parseInt(portValue);
            if ("http".equals(protocol) && port == 80) {
                port = -1;
            } else if ("https".equals(protocol) && port == 443) {
                port = -1;
            }
            URL serverURL = new URL(protocol, host, port, "");
            serverContext = serverURL.toExternalForm();
        } catch (MalformedURLException e) {
            handleException(log, "Error when generating server context URL", e);
        } catch (NumberFormatException e) {
            handleException(log, "Error when getting the port for server context URL", e);
        }

        return serverContext;
    }


	public String[] getApiNames() {
		final Lock lock = getLock();
        try {
            lock.lock();
            SynapseConfiguration synapseConfiguration = getSynapseConfiguration();
            Collection<API> apis = synapseConfiguration.getAPIs();
            return listToNames(apis.toArray(
                    new API[apis.size()]));
        } finally {
            lock.unlock();
        }
	}

	public APIData getApiByName(String apiName) {
		final Lock lock = getLock();
		try {
			lock.lock();
			SynapseConfiguration synapseConfiguration = getSynapseConfiguration();
			API api = synapseConfiguration.getAPI(apiName);
			return convertApiToAPIData(api);
		} finally {
			lock.unlock();
		}
	}

    /**
     * Function to retrieve synapse API object for given API name
     *
     * @param apiName API name
     * @return API object if exists, null otherwise
     */
    protected API getSynapseAPIByName(String apiName) {
        final Lock lock = getLock();
        try {
            lock.lock();
            SynapseConfiguration synapseConfiguration = getSynapseConfiguration();
            return synapseConfiguration.getAPI(apiName);
        } finally {
            lock.unlock();
        }
    }

	/**
	 * Set the tenant domain when a publisher tries to retrieve API his API in MT mode. When
	 * publisher gets
	 * the API, we login the gateway as supretenant. But we need to get the
	 * API,which is in the particular tenant domain.
	 * 
	 * @param apiName
	 * @param tenantDomain
	 * @return
	 * @throws APIException
	 */
	public APIData getApiForTenant(String apiName, String tenantDomain) throws APIException {

		try {
			PrivilegedCarbonContext.startTenantFlow();
			PrivilegedCarbonContext.getThreadLocalCarbonContext().setTenantDomain(tenantDomain, true);
			APIData data = getApiByName(apiName);
			return data;
		} finally {
			PrivilegedCarbonContext.endTenantFlow();
		}
	}
	
	public String[] getSequences() {
		final Lock lock = getLock();
		String[] sequenceNames = new String[0];
		try {
			lock.lock();
			SynapseConfiguration synapseConfiguration = getSynapseConfiguration();
			Map<String, SequenceMediator> sequences = synapseConfiguration.getDefinedSequences();

			if (sequences != null && !sequences.isEmpty()) {
				sequenceNames = new String[sequences.size()];
				return sequences.keySet().toArray(sequenceNames);
			}
			else {
				return sequenceNames;
			}
		} finally {
			lock.unlock();
		}
	}

    public String enableStatistics(String apiName) throws APIException {
        final Lock lock = getLock();
        try {
            lock.lock();
            SynapseConfiguration synapseConfiguration = getSynapseConfiguration();
            API api = synapseConfiguration.getAPI(apiName);
            if (api != null) {
                if (api.getAspectConfiguration() == null) {
                    AspectConfiguration config = new AspectConfiguration(apiName);
                    config.enableStatistics();
                    api.configure(config);
                } else {
                    api.getAspectConfiguration().enableStatistics();
                }

                /** Persist the api service if it is not deployed via an artifact container */
                if (api.getArtifactContainerName() == null) {
                    persistApi(api);
                }
                return apiName;
            } else {
                handleException(log, "No defined API with name " + apiName +
                                     " found to enable statistics in the Synapse configuration", null);
            }
        } catch (Exception fault) {
            handleException(log, "Couldn't enable statistics of the API " + apiName + " : " + fault.getMessage(),
                            fault);
        } finally {
            lock.unlock();
        }
        return null;
    }

    public String disableStatistics(String apiName) throws APIException {
        final Lock lock = getLock();
        try {
            lock.lock();
            SynapseConfiguration synapseConfiguration = getSynapseConfiguration();
            API api = synapseConfiguration.getAPI(apiName);
            if (api != null) {
                if (api.getAspectConfiguration() == null) {
                    AspectConfiguration config = new AspectConfiguration(apiName);
                    config.disableStatistics();
                    api.configure(config);
                } else {
                    api.getAspectConfiguration().disableStatistics();
                }

                /** Persist the api service if it is not deployed via an artifact container */
                if (api.getArtifactContainerName() == null) {
                    persistApi(api);
                }
                return apiName;
            } else {
                handleException(log, "No defined API with name " + apiName +
                                     " found to disable statistics in the Synapse configuration", null);
            }
        } catch (Exception fault) {
            handleException(log, "Couldn't disable statistics of the API " + apiName + " : " + fault.getMessage(),
                            fault);
        } finally {
            lock.unlock();
        }
        return null;
    }

    public String enableTracing(String apiName) throws APIException {
        final Lock lock = getLock();
        try {
            lock.lock();
            SynapseConfiguration synapseConfiguration = getSynapseConfiguration();
            API api = synapseConfiguration.getAPI(apiName);
            if (api != null) {
                if (api.getAspectConfiguration() == null) {
                    AspectConfiguration config = new AspectConfiguration(apiName);
                    config.enableTracing();
                    config.enableStatistics(); // Need to enable statistics for tracing
                    api.configure(config);
                } else {
                    api.getAspectConfiguration().enableTracing();
                    api.getAspectConfiguration().enableStatistics(); // Need to enable statistics for tracing
                }

                /** Persist the api service if it is not deployed via an artifact container */
                if (api.getArtifactContainerName() == null) {
                    persistApi(api);
                }

                return apiName;
            } else {
                handleException(log, "No defined API with name " + apiName +
                                     " found to enable tracing in the Synapse configuration", null);
            }
        } catch (Exception fault) {
            handleException(log, "Couldn't enable tracing of the API " + apiName + " : " + fault.getMessage(),
                            fault);
        } finally {
            lock.unlock();
        }
        return null;
    }

    public String disableTracing(String apiName) throws APIException {
        final Lock lock = getLock();
        try {
            lock.lock();
            SynapseConfiguration synapseConfiguration = getSynapseConfiguration();
            API api = synapseConfiguration.getAPI(apiName);
            if (api != null) {
                if (api.getAspectConfiguration() == null) {
                    AspectConfiguration config = new AspectConfiguration(apiName);
                    config.disableTracing();
                    api.configure(config);
                } else {
                    api.getAspectConfiguration().disableTracing();
                }

                /** Persist the api service if it is not deployed via an artifact container */
                if (api.getArtifactContainerName() == null) {
                    persistApi(api);
                }

                return apiName;
            } else {
                handleException(log, "No defined API with name " + apiName +
                                     " found to disable tracing in the Synapse configuration", null);
            }
        } catch (Exception fault) {
            handleException(log, "Couldn't disable tracing of the API " + apiName + " : " + fault.getMessage(),
                            fault);
        } finally {
            lock.unlock();
        }
        return null;
    }

	public String getApiSource(APIData apiData) {
		return RestApiAdminUtils.retrieveAPIOMElement(apiData).toString();
	}

    public String getResourceSource(ResourceData resourceData) {
        return RestApiAdminUtils.retrieveResourceOMElement(resourceData).toString();
    }

	private APIData convertApiToAPIData(API api) {
		if (api == null) {
			return null;
		}

		APIData apiData = new APIData();
		apiData.setName(api.getName());
		apiData.setContext(api.getContext());
		apiData.setHost(api.getHost());
		apiData.setPort(api.getPort());
		apiData.setFileName(api.getFileName());
		apiData.setVersion(api.getVersion());
		apiData.setVersionType(api.getVersionStrategy().getVersionType());

        if (api.getAspectConfiguration() != null && api.getAspectConfiguration().isStatisticsEnable()) {
            apiData.setStatisticsEnable(true);
        } else {
            apiData.setStatisticsEnable(false);
        }
        if (api.getAspectConfiguration() != null && api.getAspectConfiguration().isTracingEnabled()) {
            apiData.setTracingEnable(true);
        } else {
            apiData.setTracingEnable(false);
        }

        if (api.getSwaggerResourcePath() != null) {
            apiData.setSwaggerDefKey(api.getSwaggerResourcePath());
        }

		Resource[] resources = api.getResources();
		ResourceData[] resourceDatas = new ResourceData[resources.length];

        for (int i = 0; i < resources.length; i++) {

            Resource resource = resources[i];
            ResourceData data = new ResourceData();

            String[] methods = resource.getMethods();
            data.setMethods(methods);
            data.setContentType(resource.getContentType());
            data.setProtocol(resource.getProtocol());
            DispatcherHelper dispatcherHelper = resource.getDispatcherHelper();
            if (dispatcherHelper instanceof URITemplateHelper) {
                data.setUriTemplate(dispatcherHelper.getString());
            } else if (dispatcherHelper instanceof URLMappingHelper) {
                data.setUrlMapping(dispatcherHelper.getString());
            }

            if (resource.getInSequenceKey() != null) {
                data.setInSequenceKey(resource.getInSequenceKey());
            } else if (resource.getInSequence() != null) {
                data.setInSeqXml(RestApiAdminUtils.createAnonymousSequenceElement(
                        resource.getInSequence(), "inSequence").toString());
            }

            if (resource.getOutSequenceKey() != null) {
                data.setOutSequenceKey(resource.getOutSequenceKey());
            } else if (resource.getOutSequence() != null) {
                data.setOutSeqXml(RestApiAdminUtils.createAnonymousSequenceElement(
                        resource.getOutSequence(), "outSequence").toString());
            }

            if (resource.getFaultSequenceKey() != null) {
                data.setFaultSequenceKey(resource.getFaultSequenceKey());
            } else if (resource.getFaultSequence() != null) {
                data.setFaultSeqXml(RestApiAdminUtils.createAnonymousSequenceElement(
                        resource.getFaultSequence(), "faultSequence").toString());
            }
            data.setUserAgent(resource.getUserAgent());
            resourceDatas[i] = data;
        }
		apiData.setResources(resourceDatas);
		return apiData;
	}

	private String[] listToNames(API[] apis) {
        if (apis == null) {
            return null;
        } else {
            String[] datas = new String[apis.length];
            for (int i = 0; i < apis.length; i++) {
                API api = apis[i];
                datas[i] = api.getName();
            }
            return datas;
        }
    }

	/**
     * Add an api described by the given OMElement
     *
     * @param apiElement configuration of the api which needs to be added
     * @param fileName Name of the file in which this configuration should be saved or null
     * @throws APIException if the element is not an api or if an api with the
     *                   same name exists
     */
	private void addApi(OMElement apiElement, String fileName, boolean updateMode) throws APIException {

		try {
			if (apiElement.getQName().getLocalPart()
					.equals(XMLConfigConstants.API_ELT.getLocalPart())) {

				String apiName = apiElement.getAttributeValue(new QName("name"));
                String apiTransports = apiElement.getAttributeValue(new QName("transports"));

				if (getSynapseConfiguration().getAxisConfiguration().getService(
						apiName) != null) {
					handleException(log, "A service named " + apiName + " already exists", null);
				} else {
					API api = APIFactory.createAPI(apiElement);

					try {
						getSynapseConfiguration().addAPI(api.getName(), api);

						//addParameterObserver(api.getName());

						if (log.isDebugEnabled()) {
							log.debug("Added API : " + apiName);
                            log.debug("Authorized Transports : " + apiTransports);
						}

                        if (apiTransports != null) {
                            if (Constants.TRANSPORT_HTTP.equalsIgnoreCase(apiTransports)) {
                                api.setProtocol(RESTConstants.PROTOCOL_HTTP_ONLY);
                            } else if (Constants.TRANSPORT_HTTPS.equalsIgnoreCase(apiTransports)) {
                                api.setProtocol(RESTConstants.PROTOCOL_HTTPS_ONLY);
                            }
                        }

						if (updateMode) {
							api.setFileName(fileName);
						} else {
							if (fileName != null) {
								api.setFileName(fileName);
							} else {
								api.setFileName(ServiceBusUtils.generateFileName(api.getName()));
							}
						}
                        api.init(getSynapseEnvironment());
						persistApi(api);

					} catch (Exception e) {
                        api.destroy();
						getSynapseConfiguration().removeAPI(api.getName());
						try{
							if (getAxisConfig().getService(api.getName()) != null) {
								getAxisConfig().removeService(api.getName());
							}
						} catch (Exception ignore) {}
						handleException(log, "Error trying to add the API to the ESB " +
								"configuration : " + api.getName(), e);
					}
				}
			} else {
				handleException(log, "Invalid API definition", null);
			}
		} catch (AxisFault af) {
			handleException(log, "Invalid API definition", af);
		}
	}

	private void handleException(Log log, String message, Exception e) throws APIException {
        if (e == null) {
        	APIException apiException = new APIException(message);
            log.error(message, apiException);
            throw apiException;
        } else {
            message = message + " :: " + e.getMessage();
            log.error(message, e);
            throw new APIException(message, e);
        }
    }
	
	private void persistApi(API api) throws APIException {
        if(!Boolean.parseBoolean(System.getProperty("NonRegistryMode"))) {
            MediationPersistenceManager pm = getMediationPersistenceManager();
            if (pm != null) {
                pm.saveItem(api.getName(), ServiceBusConstants.ITEM_TYPE_REST_API);
            }
        }
    }
	
	private void assertNameNotEmpty(String apiName) throws APIException {
        if (apiName == null || "".equals(apiName.trim())) {
            handleFault("Invalid name : Name is empty.", null);
        }
    }
	
	private void handleFault(String message, Exception e) throws APIException {
        if (e != null) {
            log.error(message, e);
            throw new APIException(e.getMessage(), e);
        } else {
            log.error(message);
            throw new APIException(message);
        }
    }
	
	/**
	 * Override the parent's getSynapseconfig() method to retrieve the Synapse
	 * configuration from the relevant axis configuration
	 * 
	 * @return extracted SynapseConfiguration from the relevant
	 *         AxisConfiguration
	 */
	protected SynapseConfiguration getSynapseConfiguration() {	
		return (SynapseConfiguration)getAxisConfig().getParameter(SynapseConstants.SYNAPSE_CONFIG)
                .getValue() ;
	}

	/**
	 * Override the AbstarctAdmin.java's getAxisConfig() to create the CarbonContext from ThreadLoaclContext.
	 * We do this to support, publishing APIs as a supertenant but want to deploy that in tenant space.
	 * (This model is needed for APIManager)
	 */
	
	protected AxisConfiguration getAxisConfig() {
		return (axisConfig != null) ? axisConfig : getConfigContext().getAxisConfiguration();
	}

	protected ConfigurationContext getConfigContext() {
		if (configurationContext != null) {
			return configurationContext;
		}
		
		MessageContext msgContext = MessageContext.getCurrentMessageContext();
		if (msgContext != null) {
			ConfigurationContext mainConfigContext = msgContext.getConfigurationContext();

			// If a tenant has been set, then try to get the
			// ConfigurationContext of that tenant
			PrivilegedCarbonContext carbonContext =
			                                        PrivilegedCarbonContext.getThreadLocalCarbonContext();
			String domain = carbonContext.getTenantDomain();
			if (domain != null && !MultitenantConstants.SUPER_TENANT_DOMAIN_NAME.equals(domain)) {
				return TenantAxisUtils.getTenantConfigurationContext(domain, mainConfigContext);
			} else if (carbonContext.getTenantId() == MultitenantConstants.SUPER_TENANT_ID) {
				return mainConfigContext;
			} else {
				throw new UnsupportedOperationException(
				                                        "Tenant domain unidentified. "
				                                                + "Upstream code needs to identify & set the tenant domain & tenant ID. "
				                                                + " The TenantDomain SOAP header could be set by the clients or "
				                                                + "tenant authentication should be carried out.");
			}
		} else {
			return CarbonConfigurationContextFactory.getConfigurationContext();
		}
    }

    /**
     * Replace the swagger document in the registry with the given swagger json string
     *
     * @param apiName
     * @param swaggerJsonString
     * @param tenantId
     * @throws APIException
     */
    public void addSwaggerDocument(String apiName, String swaggerJsonString, int tenantId) throws APIException {

        String resourcePath =
                SwaggerConstants.DEFAULT_SWAGGER_REGISTRY_PATH + apiName + SwaggerConstants.SWAGGER_JSON_FILE_PATH;
        RegistryService registryService = RegistryServiceHolder.getInstance().getRegistryService();
        try {
            Registry registry = registryService.getConfigSystemRegistry(tenantId);

            org.wso2.carbon.registry.core.Resource resource;

            if (swaggerJsonString != null) {
                resource = registry.newResource();
                resource.setContent(swaggerJsonString);
                resource.setMediaType(APPLICATION_JSON_TYPE);
                registry.put(resourcePath, resource);

            }

        } catch (RegistryException e) {
            handleException(log, "Could not add swagger document", e);
        }

    }

    /**
     * Create a registry resource for the swagger document and update the registry resource with the default swagger
     *
     * @param apiName Name of the API
     * @param swaggerJsonString
     * @param tenantId
     * @throws APIException
     */
    public void updateSwaggerDocument(String apiName, String swaggerJsonString, int tenantId) throws APIException {

        API api = getSynapseAPIByName(apiName);
        if (api == null) {
            throw new APIException("API with name \"" + apiName + "\" does not exists.");
        }

        String resourcePath = getSwaggerResourcePath(api);
        RegistryService registryService = RegistryServiceHolder.getInstance().getRegistryService();
        try {
            Registry registry = registryService.getConfigSystemRegistry(tenantId);

            if (resourcePath.startsWith(CONFIG_REG_PREFIX)) {
                resourcePath = resourcePath.substring(5);
                registry = registryService.getConfigSystemRegistry(tenantId);
            } else if (resourcePath.startsWith(GOV_REG_PREFIX)) {
                resourcePath = resourcePath.substring(4);
                registry = registryService.getGovernanceSystemRegistry(tenantId);
            } else {
                throw new APIException("Unable to update other sources. Only capable of updating swagger definitions " +
                        "resides in configuration or governance registry");
            }

            org.wso2.carbon.registry.core.Resource resource;
            if (!registry.resourceExists(resourcePath)) {
                resource = registry.newResource();
                resource.setContent(swaggerJsonString);
                resource.setMediaType(APPLICATION_JSON_TYPE);
            } else {
                resource = registry.get(resourcePath);
                resource.setContent(swaggerJsonString);
            }
            registry.put(resourcePath, resource);

        } catch (RegistryException e) {
            handleException(log, "Could not update swagger document", e);
        }
    }



    /**
     * Return the registry resource for the provided location
     *
     * @param apiName
     * @param tenantId
     * @return
     * @throws APIException
     */
    public String getSwaggerDocument(String apiName, int tenantId) throws APIException {

        API api = getSynapseAPIByName(apiName);
        if (api == null) {
            throw new APIException("API with name \"" + apiName + "\" does not exists.");
        }
        String resourcePath = getSwaggerResourcePath(api);
        String swaggerJsonString = null;
        if (resourcePath.startsWith(CONFIG_REG_PREFIX) || resourcePath.startsWith(GOV_REG_PREFIX)) {
            try {
                swaggerJsonString = getResourceFromRegistry(resourcePath, tenantId);
            } catch (RegistryException e) {
                handleException(log, "Could not get swagger document", e);
            }
        } else {
            //Read from URI
            try {
                swaggerJsonString = readFromURI(resourcePath);
            } catch (IOException e) {
                log.error("Error occurred while reading swagger definition from: " + resourcePath, e);
            }
        }

        // Generate if not available
        if (swaggerJsonString == null) {
            if (log.isDebugEnabled()) {
                log.debug("Generate swagger definition for the API : " + apiName);
            }

            try {
                ServerConfig serverConfig = new CarbonServerConfig();
                JSONObject jsonDefinition = new JSONObject(new GenericApiObjectDefinition(api, serverConfig).getDefinitionMap());
                swaggerJsonString = jsonDefinition.toString();
            } catch (AxisFault axisFault) {
                handleException(log, "Error occurred while generating swagger definition", axisFault);
            }
        }
        return swaggerJsonString;
    }

    /**
     * Function to generate API from swagger definition (from JSON representation)
     *
     * @param swaggerJsonString swagger definition
     * @return generated synapse API
     * @throws APIException
     */
    public String generateAPIFromSwagger(String swaggerJsonString) throws APIException {

        if (swaggerJsonString == null || swaggerJsonString.isEmpty()) {
            handleException(log, "Swagger provided is empty, hence unable to generate API", null);
        }

        JsonParser jsonParser = new JsonParser();
        JsonElement swaggerJson = jsonParser.parse(swaggerJsonString);
        if (swaggerJson.isJsonObject()) {
            APIGenerator apiGenerator = new APIGenerator(swaggerJson.getAsJsonObject());
            try {
                API api = apiGenerator.generateSynapseAPI();
                return APISerializer.serializeAPI(api).toString();
            } catch (APIGenException e) {
                handleException(log, "Error occurred while generating API", e);
            }
        } else {
            handleException(log, "Error in swagger definition format: should be a json object", null);
        }
        // Definitely will not reach here
        return "";
    }

    /**
     * Function to generate updated existing API by referring to swagger definition (from JSON representation)
     *
     * @param swaggerJsonString swagger definition
     * @param existingApiName name of the existing API
     * @return generated synapse API
     * @throws APIException
     */
    public String generateUpdatedAPIFromSwagger(String swaggerJsonString, String existingApiName) throws APIException {

        if (swaggerJsonString == null || swaggerJsonString.isEmpty()) {
            handleException(log, "Provided swagger definition is empty, hence unable to generate API", null);
        }

        if (existingApiName == null || existingApiName.isEmpty()) {
            handleException(log, "Provided existing API name is empty, hence unable to generate API", null);
        }

        JsonParser jsonParser = new JsonParser();
        JsonElement swaggerJson = jsonParser.parse(swaggerJsonString);
        if (swaggerJson.isJsonObject()) {
            APIGenerator apiGenerator = new APIGenerator(swaggerJson.getAsJsonObject());
            try {
                API api = apiGenerator.generateUpdatedSynapseAPI(getSynapseAPIByName(existingApiName));
                return APISerializer.serializeAPI(api).toString();
            } catch (APIGenException e) {
                handleException(log, "Error occurred while generating API", e);
            }
        } else {
            handleException(log, "Error in swagger definition format: should be a json object", null);
        }
        // Definitely will not reach here
        return "";
    }

    /**
     * Function to retrieve registry resource
     *
     * @param resourcePath
     * @param tenantId
     * @return resource string if resource exists and null otherwise
     * @throws RegistryException When error occurred while retrieving config registry or while retrieving data from
     * registry
     */
    private String getResourceFromRegistry(String resourcePath, int tenantId) throws RegistryException {

        String swaggerJsonString = null;
        RegistryService registryService = RegistryServiceHolder.getInstance().getRegistryService();

        Registry registry;
        if (resourcePath.startsWith(CONFIG_REG_PREFIX)) {
            resourcePath = resourcePath.substring(5);
            registry = registryService.getConfigSystemRegistry(tenantId);
        } else {
            resourcePath = resourcePath.substring(4);
            registry = registryService.getGovernanceSystemRegistry(tenantId);
        }

        if (registry.resourceExists(resourcePath)) {
            org.wso2.carbon.registry.core.Resource resource = registry.get(resourcePath);
            if (resource.getContent() != null && (resource.getContent() instanceof byte[]) &&
                                                                        (((byte[])resource.getContent()).length > 0)) {
                swaggerJsonString = new String((byte[]) resource.getContent(), Charset.defaultCharset());
            }
        } else {
            if (log.isDebugEnabled()) {
                log.debug("No resource found in the path " + resourcePath);
            }
        }
        return swaggerJsonString;
    }

    private String getSwaggerResourcePath (API api) {
        String resourcePath;
        if (api.getSwaggerResourcePath() != null && !api.getSwaggerResourcePath().isEmpty()) {
            resourcePath = api.getSwaggerResourcePath();
        } else {
            resourcePath = CONFIG_REG_PREFIX + SwaggerConstants.DEFAULT_SWAGGER_REGISTRY_PATH +
                    api.getName() + SwaggerConstants.SWAGGER_JSON_FILE_PATH;
        }
        return resourcePath;
    }

    private String readFromURI(String resourcePath) throws IOException {

        URL url = new URL(resourcePath);
        if ("file".equals(url.getProtocol())) {
            //create url to absolute path from relative path (relative to carbon home)
            url = new URL(url.getProtocol() + ":" + CarbonUtils.getCarbonHome() + url.getPath());
            try {
                url.openStream();
            } catch (IOException e) {
                log.error("Error occurred while accessing file resource: " + url, e);
                return null;
            }
        }

        URLConnection urlConnection = url.openConnection();
        urlConnection.connect();

        try (InputStream inputStream = urlConnection.getInputStream()) {

            if (inputStream == null) {
                return null;
            }
            StringBuilder strBuilder = new StringBuilder();
            try (BufferedReader bReader = new BufferedReader(new InputStreamReader(inputStream))) {
                String line;
                while ((line = bReader.readLine()) != null) {
                    strBuilder.append(line);
                }
            }
            return strBuilder.toString();
        }
    }
}
