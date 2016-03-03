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
package org.wso2.datamapper.engine.outputAdapters;

import org.apache.avro.Schema;
import org.apache.avro.generic.GenericData;
import org.apache.avro.generic.GenericDatumWriter;
import org.apache.avro.generic.GenericRecord;
import org.apache.avro.io.Encoder;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.synapse.SynapseException;

import javax.xml.stream.XMLOutputFactory;
import javax.xml.stream.XMLStreamException;
import javax.xml.stream.XMLStreamWriter;
import java.io.IOException;
import java.io.StringWriter;
import java.util.List;

import static org.wso2.datamapper.engine.utils.DataMapperEngineConstants.AVRO_ATTRIBUTE_FIELD_PREFIX;
import static org.wso2.datamapper.engine.utils.DataMapperEngineConstants.AVRO_RECORD_FIELD_POSTFIX;

/**
 * This class implement the xml datum writer for WSO2 data mapper
 */
public class XMLDatumWriter extends GenericDatumWriter<GenericRecord> {

    private static final Log log = LogFactory.getLog(XMLDatumWriter.class);

    @Override protected void writeRecord(Schema schema, Object datum, Encoder out) throws IOException {
        try {
            StringWriter stringWriter = new StringWriter();
            XMLOutputFactory xMLOutputFactory = XMLOutputFactory.newInstance();
            XMLStreamWriter xMLStreamWriter = xMLOutputFactory.createXMLStreamWriter(stringWriter);

            GenericRecord record = (GenericRecord) datum;
            String name = record.getSchema().getName();

            xMLStreamWriter.writeStartDocument();
            List<Schema.Field> fields = schema.getFields();
            xMLStreamWriter.writeStartElement(name);
            xMLStreamWriter = decodeFieldList(xMLStreamWriter, fields, record);
            xMLStreamWriter.writeEndElement();
            xMLStreamWriter.writeEndDocument();

            xMLStreamWriter.flush();
            xMLStreamWriter.close();

            String xmlString = stringWriter.getBuffer().toString();
            stringWriter.close();
            out.writeString(xmlString);

        } catch (XMLStreamException e) {
            throw new SynapseException("Unable to build xml document from the AVRO generic record");
        }
    }

    private XMLStreamWriter decodeFieldList(XMLStreamWriter xMLStreamWriter, List<Schema.Field> fieldList,
            GenericRecord record) throws XMLStreamException {
        for (Schema.Field field : fieldList) {
            Schema.Type fieldType = field.schema().getType();
            if (Schema.Type.ARRAY.equals(fieldType)) {
                xMLStreamWriter = decodeArrayTypeField(xMLStreamWriter, record, field);
            } else if (Schema.Type.RECORD.equals(fieldType)) {
                xMLStreamWriter = decodeRecordTypeField(xMLStreamWriter, (GenericRecord) record.get(field.name()),
                        field.schema());
            } else if (Schema.Type.STRING.equals(fieldType)) {
                xMLStreamWriter = decodeStringTypeField(xMLStreamWriter, record, field);
            } else {
                log.warn("unsupported AVRO schema type found : " + fieldType);
            }
        }
        return xMLStreamWriter;
    }

    private XMLStreamWriter decodeStringTypeField(XMLStreamWriter xMLStreamWriter, GenericRecord record,
            Schema.Field field) throws XMLStreamException {
        String fieldName = field.name();
        if (fieldName.startsWith(AVRO_ATTRIBUTE_FIELD_PREFIX)) {
            String stringFieldValue = (String) record.get(fieldName);
            xMLStreamWriter.writeAttribute(getValidAttributeFieldName(fieldName), stringFieldValue.toString());
        } else {
            xMLStreamWriter.writeStartElement(fieldName);
            String stringFieldValue = (String) record.get(fieldName);
            xMLStreamWriter.writeCharacters(stringFieldValue.toString());
            xMLStreamWriter.writeEndElement();
        }
        return xMLStreamWriter;
    }

    private String getValidAttributeFieldName(String attributeName) {
        return attributeName.replaceFirst(AVRO_ATTRIBUTE_FIELD_PREFIX, "");
    }

    private XMLStreamWriter decodeRecordTypeField(XMLStreamWriter xMLStreamWriter, GenericRecord record, Schema field)
            throws XMLStreamException {
        List<Schema.Field> fieldList = field.getFields();
        xMLStreamWriter.writeStartElement(getValidRecordFieldName(field.getName()));
        xMLStreamWriter = decodeFieldList(xMLStreamWriter, fieldList, record);
        xMLStreamWriter.writeEndElement();
        return xMLStreamWriter;
    }

    private String getValidRecordFieldName(String fieldName) {
        if (fieldName.endsWith(AVRO_RECORD_FIELD_POSTFIX)) {
            return fieldName.substring(0, fieldName.lastIndexOf(AVRO_RECORD_FIELD_POSTFIX));
        }
        return fieldName;
    }

    private XMLStreamWriter decodeArrayTypeField(XMLStreamWriter xMLStreamWriter, GenericRecord record,
            Schema.Field field) throws XMLStreamException {
        GenericData.Array<GenericData.Record> fieldValues = (GenericData.Array<GenericData.Record>) record
                .get(field.name());
        for (GenericData.Record recordValue : fieldValues) {
            xMLStreamWriter = decodeRecordTypeField(xMLStreamWriter, recordValue, field.schema().getElementType());
        }
        return xMLStreamWriter;
    }

}
