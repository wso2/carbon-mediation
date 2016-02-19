package org.wso2.carbon.mediator.datamapper;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.Calendar;
import java.util.HashMap;
import java.util.Map;

import org.apache.axiom.om.impl.llom.OMTextImpl;
import org.apache.axis2.context.ConfigurationContext;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.synapse.MessageContext;
import org.apache.synapse.SynapseException;
import org.apache.synapse.core.axis2.Axis2MessageContext;
import org.wso2.datamapper.engine.core.MappingResourceLoader;

/**
 * Handles caching of the mapping resources
 *
 */
public class CacheResources {
	private static final String CACHABLE_DURATION = "cachableDuration";
	private static final String DataMapperCacheMapKey = "dataMapperCacheMap";
	private static MappingResourceLoader mappingResourceLoader = null;
	private static int time = 10000;
	private static final Log log = LogFactory.getLog(CacheResources.class);
	
	/**
	 * Use to get the cached mapping resources
	 * @param context the message context
	 * @param configkey the location of the mapping configuration
	 * @param inSchemaKey the location of the input schema
	 * @param outSchemaKey the location of the output schema
	 * @param uuid the unique ID
	 * @return the mapping resource loader containing the mapping resources
	 * @throws IOException
	 */
	public static MappingResourceLoader getCachedResources(
			MessageContext context, String configkey, String inSchemaKey,
			String outSchemaKey, String datamapperMediatorUuid) throws IOException {
		
		ConfigurationContext configurationContext;
		DataMapperCacheContext dmcc = null;
		try {
			// gets the axis2 message context
			org.apache.axis2.context.MessageContext axis2MsgCtx = ((Axis2MessageContext) context)
					.getAxis2MessageContext();

			configurationContext = axis2MsgCtx.getConfigurationContext();

			String cacheDurable = context.getConfiguration().getRegistry().getConfigurationProperties().getProperty(CACHABLE_DURATION);
			long cacheTime = (cacheDurable != null && !cacheDurable.isEmpty()) ? Long
					.parseLong(cacheDurable) : time;

			
			// When proxy invokes initially this creates the cacheble object and creates a property in Axis2
			if (configurationContext.getProperty(DataMapperCacheMapKey) == null) {
				mappingResourceLoader = getMappingResourceLoader(context, configkey, inSchemaKey, outSchemaKey);
				dmcc = new DataMapperCacheContext(Calendar.getInstance().getTime(), mappingResourceLoader);
				Map<String, DataMapperCacheContext> mappingResourceMap = new HashMap<String, DataMapperCacheContext>();
				mappingResourceMap.put(datamapperMediatorUuid, dmcc);
				configurationContext.setProperty(DataMapperCacheMapKey, mappingResourceMap);
			} else {
				// Checks the property in Axis2 and get the map from Axis2
				Map<String, DataMapperCacheContext>  mappingResourceMapFromAxis = (Map<String, DataMapperCacheContext>) configurationContext.getProperty(DataMapperCacheMapKey);
				if (mappingResourceMapFromAxis.containsKey(datamapperMediatorUuid)){
					dmcc = (DataMapperCacheContext) mappingResourceMapFromAxis.get(datamapperMediatorUuid);		
					// Gets the cacheble limit
					long cachebleLimit = dmcc.getDateTime().getTime()+ cacheTime;
					// Checks for the cacheble limit against the current time
					if (cachebleLimit >= System.currentTimeMillis()) {
						mappingResourceLoader = dmcc.getCachedResources();
					}else{
						if (log.isDebugEnabled()) {
							log.debug("exceeds the cachebleLimit " + cachebleLimit);
						}
						// Removes the key from the map and insert the new data mapper cache context
						mappingResourceLoader = getMappingResourceLoader(context, configkey, inSchemaKey, outSchemaKey);
						dmcc = new DataMapperCacheContext(Calendar.getInstance().getTime(),dmcc.getCachedResources());
						mappingResourceMapFromAxis.put(datamapperMediatorUuid, dmcc);
					}
				} else {
					mappingResourceLoader = getMappingResourceLoader(context, configkey, inSchemaKey, outSchemaKey);
					dmcc = new DataMapperCacheContext(Calendar.getInstance().getTime(), mappingResourceLoader);
					mappingResourceMapFromAxis.put(datamapperMediatorUuid, dmcc);
				}
			}

		} catch (Exception e) {
			handleException("Caching failed", e);
		}
		return mappingResourceLoader;
	}


	/**
	 * 	When proxy invokes initially, this creates a new mapping resource loader
	 * 
	 * @param context message context
	 * @param configkey the location of the mapping configuration
	 * @param inSchemaKey the location of the input schema
	 * @param outSchemaKey the location of the output schema
	 * @return the MappingResourceLoader object
	 * @throws IOException
	 */
	 private static MappingResourceLoader getMappingResourceLoader(
			MessageContext context,String configkey, String inSchemaKey, String outSchemaKey) throws IOException {

		InputStream configFileInputStream = getInputStream(context, configkey);
		InputStream inputSchemaStream = getInputStream(context, inSchemaKey);
		InputStream outputSchemaStream = getInputStream(context, outSchemaKey);

		// Creates a new mappingResourceLoader
		mappingResourceLoader = new MappingResourceLoader(inputSchemaStream,
				outputSchemaStream, configFileInputStream);
		
		return mappingResourceLoader;
	}

	/**
	 * Input streams to create the the MappingResourceLoader object
	 * @param context Message context
	 * @param key registry key
	 * @return mapping configuration, inputSchema and outputSchema as inputStreams
	 */
	private static InputStream getInputStream(MessageContext context, String key) {

		InputStream inputStream = null;
		Object entry = context.getEntry(key);
		if (entry instanceof OMTextImpl) {
			if (log.isDebugEnabled()) {
				log.debug("Value for the key is ");
			}
			OMTextImpl text = (OMTextImpl) entry;
			String content = text.getText();
			inputStream = new ByteArrayInputStream(content.getBytes());
		}
		return inputStream;
	}

	private static void handleException(String message, Exception e) {
		log.error(message, e);
		throw new SynapseException(message, e);
	}

}
