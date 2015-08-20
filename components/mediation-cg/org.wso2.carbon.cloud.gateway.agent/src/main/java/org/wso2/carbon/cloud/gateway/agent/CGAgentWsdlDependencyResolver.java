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

import java.io.ByteArrayOutputStream;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

import javax.xml.namespace.QName;
import javax.xml.stream.XMLStreamException;

import org.apache.axiom.om.OMElement;
import org.apache.axiom.om.OMNode;
import org.apache.axis2.description.AxisService;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.ws.commons.schema.XmlSchema;
import org.wso2.carbon.cloud.gateway.common.CGException;
//import org.wso2.carbon.cloud.gateway.common.CGServiceDependencyBean;
import org.wso2.carbon.cloud.gateway.stub.types.common.CGServiceDependencyBean;

/**
 * Scans a given wsdl (and axis service associated with it) for schama
 * import/includes and adjust them accordingly so that the given axis service
 * can be constructed using the wsdl outside the firewall. This is required
 * because, CSG server (running outside a firewall) doesn't have access to these
 * external dependencies.
 */
public class CGAgentWsdlDependencyResolver {

    /**
     * Local name of the sschema tag
     */
    private static final String SCHEMA_TAG = "schema";

    /**
     * Local name of wsdl:types tag
     */
    private static final String TYPES_TAG = "types";

    /**
     * QName to select 'schemaLocation' from a xsd:include or xsd:import tag.
     */
    private static final QName SCHEMA_LOCATION_QNAME = new QName(AxisService.SCHEMA_LOCATION);

    private static Log log = LogFactory.getLog(CGAgentWsdlDependencyResolver.class);

    /**
     * The Service that will be published onto a CSG server
     */
    private AxisService service;

    /**
     * Location of the Wsdl associated with the service (that needs to be
     * published onto a service)
     */
    private String wsdlLocation;

    /**
     * Instantiates an instance using specified arguments
     *
     * @param service      The service that needs to be checked for schema imports
     * @param wsdlLocation location of the wsdl
     */
    public CGAgentWsdlDependencyResolver(AxisService service, String wsdlLocation) {
        this.service = service;
        this.wsdlLocation = wsdlLocation;
    }

    /**
     * Scan the supplied wsdl (and Axis service) for schema imports and does
     * following:
     * <ul>
     * <li>Adjust the 'schemalocation' tags on each import/include element of
     * the wsdl so they will not point to locations that are not accessible.
     * <li>Prepare a list of {@link CGServiceDependencyBean} containing content
     * of imported schemas so they can be transmitted outside the firewall.
     * </ul>
     *
     * @param dependencies This list will be populated with
     *                     {@link CGServiceDependencyBean}s
     * @return The Wsdl with modified 'schemaLocations'
     * @throws CGException An error while retrieving the wsdl
     */
    @SuppressWarnings("unchecked")
    public OMElement parseWsdlDependencies(List<CGServiceDependencyBean> dependencies)
            throws CGException {

        // Force axis service to populate schema mappings
        service.populateSchemaMappings(false);

        // Retrieve the wsdl document as a OMElement
        OMElement wsdlElement = CGAgentUtils.getWSDLElement(wsdlLocation);

        try {
            String wsdlString = wsdlElement.toStringWithConsume();
            if (wsdlString.contains("Unable to generate WSDL 1.1 for this service") ||
                    wsdlString.contains("error")) {
                // axis2 doesn't generate a WSDL for WSDL 1.1 for REST service if useoriginalwsdl
                // parameter is not given
                return null;
            }
        } catch (XMLStreamException e) {
            log.error("Error while parsing the WSDL", e);
            return null;
        }

        Map<Object, Object> schemaMappingTable = service.getSchemaMappingTable();

        Map<String, OMElement> importedOrIncludedSchemaElements = new HashMap<String, OMElement>();

        // Search for wsdl:types tag
        OMElement typesElement =
                wsdlElement.getFirstChildWithName(
                        new QName(wsdlElement.getNamespace().getNamespaceURI(),
                                TYPES_TAG));

        if (typesElement != null) {

            Iterator<OMNode> schemaIterator = typesElement.getChildrenWithLocalName(SCHEMA_TAG);

            // traverse all the schema tags and find all the schema imports and
            // includes
            while (schemaIterator.hasNext()) {

                OMNode node = schemaIterator.next();

                if (node instanceof OMElement) {
                    OMElement schemaElement = (OMElement) node;

                    Iterator<OMNode> iterator = schemaElement.getChildren();

                    while (iterator.hasNext()) {
                        OMNode child = iterator.next();
                        if (isImportOrIncludeTag(child)) {
                            // Found a schema include or a import
                            String schemaLocation =
                                    ((OMElement) child).getAttributeValue(SCHEMA_LOCATION_QNAME);

                            if (log.isDebugEnabled()) {
                                log.debug("Found schema import or include : " + child.toString() +
                                        " in wsdl at :" + wsdlLocation);
                            }

                            importedOrIncludedSchemaElements.put(schemaLocation, (OMElement) child);
                        }
                    }

                }

            }

        }

        SimpleSchemaNameGenerator nameGenerator =
                new SimpleSchemaNameGenerator(SCHEMA_TAG,
                        service.getName());

        if (log.isDebugEnabled()) {
            log.debug("Traversing schemas in axis service: " + service.getName());
        }

        // 1. Retrieve content of all schemas from axis service and prepare
        // CSGServiceDependencyBeans
        // 2. Adjust the schemaLocation values so that they will correctly match
        // the keys in
        // CSGServiceDependencyBeans
        for (Entry<Object, Object> entry : schemaMappingTable.entrySet()) {

            ByteArrayOutputStream bao = new ByteArrayOutputStream();
            XmlSchema schema = (XmlSchema) entry.getValue();

            schema.write(bao);
            CGServiceDependencyBean dependency = new CGServiceDependencyBean();

            String keyInAxisServiceSchema = entry.getKey().toString();
            OMElement externalSchemaRefInWsdl =
                    getMatchingSchemaImportInWsdl(importedOrIncludedSchemaElements,
                            keyInAxisServiceSchema);

            if (externalSchemaRefInWsdl != null) {

                String generatedSchemaName = nameGenerator.getNext();
                externalSchemaRefInWsdl.getAttribute(SCHEMA_LOCATION_QNAME)
                        .setAttributeValue(generatedSchemaName);
                dependency.setKey(generatedSchemaName);
            } else {
                dependency.setKey(keyInAxisServiceSchema);
            }

            dependency.setContent(bao.toString());
            dependencies.add(dependency);

        }

        return wsdlElement;
    }

    /**
     * A simple utility class to generate schema names based on a counter.
     */
    private class SimpleSchemaNameGenerator {

        private int count;
        private String prefix;
        private String serviceName;

        public SimpleSchemaNameGenerator(String prefix, String serviceName) {
            this.prefix = prefix;
            this.serviceName = serviceName;
            count = 0;
        }

        public String getNext() {
            return String.format("%s%s_%d", this.prefix, this.serviceName, count++);
        }

    }

    /**
     * Matches schemalocation of a xsd import/include with a given key. This is
     * required since keys of schema mapping table in {@link AxisService}
     * doesn't correctly match the values of 'schemaLocations' in wsdl
     *
     * @param schemaImports          Contains schema import/include of elements extracted from wsdl
     * @param keyInAxisServiceSchema The key in schema mapping table in {@link AxisService} A key
     *                               returned by associated axis service.
     * @return the matching schema import/include tag or null if none matched
     *         with provided key
     */
    private OMElement getMatchingSchemaImportInWsdl(Map<String, OMElement> schemaImports,
                                                    String keyInAxisServiceSchema) {

        OMElement matchingSchemaElement = null;

        for (String schemaLocationKey : schemaImports.keySet()) {

            if (schemaLocationKey.contains(keyInAxisServiceSchema)) {
                matchingSchemaElement = schemaImports.get(schemaLocationKey);

                break;
            }

        }

        return matchingSchemaElement;
    }

    /**
     * Determines weather the specified node is a xsd:import or xsd:include tag
     *
     * @param node The xml node
     * @return True if the node is a import or a include (false otherwise)
     */
    private boolean isImportOrIncludeTag(OMNode node) {

        boolean isImportOrInclude = false;
        if (node instanceof OMElement) {

            OMElement element = (OMElement) node;
            isImportOrInclude =
                    element.getLocalName().equals(AxisService.IMPORT_TAG) ||
                            element.getLocalName().equals(AxisService.INCLUDE_TAG);

        }

        return isImportOrInclude;
    }

}
