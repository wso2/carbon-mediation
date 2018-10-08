/*
 * Copyright (c) 2018, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
 *
 * WSO2 Inc. licenses this file to you under the Apache License,
 * Version 2.0 (the "License"); you may not use this file except
 * in compliance with the License.
 * You may obtain a copy of the License at
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied. See the License for the
 * specific language governing permissions and limitations
 * under the License.
 */
package org.wso2.carbon.mediator.datamapper.engine.core.mapper;

import javax.xml.transform.Result;
import javax.xml.transform.Source;
import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerException;
import javax.xml.transform.TransformerFactory;
import java.io.InputStream;
import java.io.StringWriter;
import java.util.Map;

import net.sf.saxon.TransformerFactoryImpl;

public class XSLTMappingHandler {

    private final Source xsltSource;
    private Transformer transformer;

    /**
     *
     * @param xsltMappingResource xslt mapping resource
     * @throws TransformerException errors arise from xslt transformation
     */
    public XSLTMappingHandler(XSLTMappingResource xsltMappingResource) throws TransformerException {
        xsltSource = new javax.xml.transform.stream.StreamSource(xsltMappingResource.getInputStream());
        TransformerFactory transFact = new TransformerFactoryImpl();
        transformer = transFact.newTransformer(xsltSource);
    }

    /**
     * This method performs xslt transformation from input xml to the output xml
     *
     * @param properties set of runtime properties with values
     * @param inputXML input stream of the input xml message
     * @return output xml as a string
     * @throws TransformerException errors arise from xslt transformation
     */
    public String doMap(Map<String,Object> properties, InputStream inputXML) throws
            TransformerException{

        setParameters(properties);
        Source xmlSource = new javax.xml.transform.stream.StreamSource(inputXML);
        StringWriter sw = new StringWriter();
        Result result = new javax.xml.transform.stream.StreamResult(sw);
        transformer.transform(xmlSource, result);
        return sw.toString();

    }

    /**
     * Set runtime properties as parameters in the transformer
     *
     * @param properties Map of properties with values
     */
    private void setParameters(Map<String,Object> properties){
        transformer.clearParameters();
        for(String key : properties.keySet())
            transformer.setParameter(key,properties.get(key));
    }

}
