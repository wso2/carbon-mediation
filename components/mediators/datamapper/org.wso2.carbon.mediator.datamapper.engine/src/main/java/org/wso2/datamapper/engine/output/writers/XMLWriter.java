/*
 * Copyright (c) 2016, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
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
package org.wso2.datamapper.engine.output.writers;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.synapse.SynapseException;
import org.wso2.datamapper.engine.core.Schema;
import org.wso2.datamapper.engine.output.Writable;

import javax.xml.stream.XMLOutputFactory;
import javax.xml.stream.XMLStreamException;
import javax.xml.stream.XMLStreamWriter;
import java.io.StringWriter;
import java.util.Iterator;
import java.util.Map;
import java.util.Stack;

import static org.wso2.datamapper.engine.utils.DataMapperEngineConstants.SCHEMA_ATTRIBUTE_FIELD_PREFIX;
import static org.wso2.datamapper.engine.utils.DataMapperEngineConstants.SCHEMA_ATTRIBUTE_PARENT_ELEMENT_POSTFIX;

/**
 * This class implements {@link Writable} interface and xml writer for data mapper engine using StAX
 */
public class XMLWriter implements Writable {

    private static final Log log = LogFactory.getLog(XMLWriter.class);
    private StringWriter stringWriter;
    private XMLStreamWriter xMLStreamWriter;
    private Schema outputSchema;
    private Stack<String> arrayElementStack;
    private String latestElementName;
    private String latestFieldName;
    private Map<String, String> namespaceMap;
    private static final String NAMESPACE_SEPERATOR = "_";

    public XMLWriter(Schema outputSchema) {
        this.outputSchema = outputSchema;
        arrayElementStack = new Stack<>();
        stringWriter = new StringWriter();
        XMLOutputFactory xMLOutputFactory = XMLOutputFactory.newInstance();
        try {
            xMLStreamWriter = xMLOutputFactory.createXMLStreamWriter(stringWriter);
            //creating root element of the xml message
            namespaceMap =  outputSchema.getNamespaceMap();
            writeStartElement(outputSchema.getName(), xMLStreamWriter);
            Iterator<String> namespaceKeyIterator= namespaceMap.keySet().iterator();
            while(namespaceKeyIterator.hasNext()){
                String key = namespaceKeyIterator.next();
                xMLStreamWriter.writeNamespace(namespaceMap.get(key), key);
            }
        } catch (XMLStreamException e) {
            log.error("Error while creating xml output factory");
        }
    }

    @Override
    public void writeStartObject(String name) {
        try {
            if(name.endsWith(SCHEMA_ATTRIBUTE_PARENT_ELEMENT_POSTFIX)){
                latestElementName = name.substring(0,name.lastIndexOf(SCHEMA_ATTRIBUTE_PARENT_ELEMENT_POSTFIX));
                writeStartElement(latestElementName, xMLStreamWriter);
            }else{
                writeStartElement(name, xMLStreamWriter);
                latestElementName = name;
            }
        } catch (XMLStreamException e) {
            throw new SynapseException(e.getMessage());
        }
    }

    @Override
    public void writeField(String name, String value) {
        try {
            //with in a element attributes must come first before any of other field values
            if (value != null) {
                if (name.startsWith(SCHEMA_ATTRIBUTE_FIELD_PREFIX)) {
                    xMLStreamWriter.writeAttribute(name.replaceFirst(SCHEMA_ATTRIBUTE_FIELD_PREFIX, ""), value);
                } else if(name.equals(latestElementName)){
                    xMLStreamWriter.writeCharacters(value);
                    xMLStreamWriter.writeEndElement();
                }else{
                    writeStartElement(name, xMLStreamWriter);
                    xMLStreamWriter.writeCharacters(value);
                    xMLStreamWriter.writeEndElement();
                }
            }
        } catch (XMLStreamException e) {
            throw new SynapseException(e.getMessage());
        }
    }

    @Override
    public void writeEndObject() {
        try {
            xMLStreamWriter.writeEndElement();
        } catch (XMLStreamException e) {
            throw new SynapseException(e.getMessage());
        }
    }

    @Override
    public String terminateMessageBuilding() {
        try {
            xMLStreamWriter.writeEndElement();
            xMLStreamWriter.flush();
            xMLStreamWriter.close();
            return stringWriter.getBuffer().toString();
        } catch (XMLStreamException e) {
            throw new SynapseException(e.getMessage());
        }
    }

    @Override
    public void writeStartArray() {
        arrayElementStack.push(latestElementName);
    }

    @Override
    public void writeEndArray() {
        arrayElementStack.pop();
    }

    @Override
    public void writeStartAnonymousObject() {
        try {
            writeStartElement(arrayElementStack.peek(),xMLStreamWriter);
        } catch (XMLStreamException e) {
            throw new SynapseException(e.getMessage());
        }
    }

    private void writeStartElement(String name, XMLStreamWriter xMLStreamWriter) throws XMLStreamException {
        String prefix = name.split(NAMESPACE_SEPERATOR)[0];
        if(namespaceMap.values().contains(prefix)){
            String nameWithoutPrefix = name.split(NAMESPACE_SEPERATOR)[1];
            Iterator<String> keyIterator = namespaceMap.keySet().iterator();
            while(keyIterator.hasNext()){
                String key = keyIterator.next();
                if(prefix.equals(namespaceMap.get(key))){
                    xMLStreamWriter.writeStartElement(prefix,nameWithoutPrefix,key);
                }
            }
        }else{
            xMLStreamWriter.writeStartElement(name);
        }
    }

}
