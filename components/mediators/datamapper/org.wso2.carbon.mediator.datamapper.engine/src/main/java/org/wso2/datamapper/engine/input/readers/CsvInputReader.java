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

package org.wso2.datamapper.engine.input.readers;

import au.com.bytecode.opencsv.CSVReader;
import org.apache.avro.Schema;
import org.apache.avro.Schema.Field;
import org.apache.avro.generic.GenericData;
import org.apache.avro.generic.GenericRecord;
import org.apache.axiom.om.OMElement;
import org.apache.axiom.om.OMXMLBuilderFactory;
import org.apache.axiom.om.OMXMLParserWrapper;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.Iterator;
import java.util.List;

/**
 * Reading input CSV data and construct the Avro record
 */
public class CsvInputReader implements InputDataReaderAdapter {


    private InputStreamReader csvReader;
    private OMElement textElement;

    /**
     * @param in - input message stream
     * @throws IOException
     */
    public void setInputMsg(InputStream in) {
        OMXMLParserWrapper builder = OMXMLBuilderFactory.createOMBuilder(in);
        OMElement documentElement = builder.getDocumentElement();
        ;
        OMElement body = documentElement.getFirstElement().getFirstElement();
        getTextElement(body);
        String text = textElement.getText();
        InputStream is = new ByteArrayInputStream(text.getBytes());
        csvReader = new InputStreamReader(is);
    }

    // CSV dataset has been wrapped using <text> element
    private OMElement getTextElement(OMElement element) {
        @SuppressWarnings("unchecked")
        Iterator<OMElement> iter = element.getChildElements();
        while (iter.hasNext()) {
            OMElement childElement = iter.next();
            if ("text".equals(childElement.getLocalName())) {
                textElement = childElement;
                return textElement;
            } else {
                if (getTextElement(childElement) != null) {
                    break;
                }
            }
        }
        return null;
    }

    //TODO This is not the real implementation
    public GenericRecord getInputRecord(Schema input) {
        GenericRecord ParentRecord = new GenericData.Record(input);
        List<Field> fields = input.getFields();
        Field field = fields.get(0);// there should be a one  object
        //10 is the default size of the recordArray but this is a auto resizable array , size will be increased when pass default value
        GenericData.Array<GenericRecord> recordArray = new GenericData.Array<GenericRecord>(10, field.schema());
        CSVReader reader = new CSVReader(csvReader);
        String[] nextLine;
        try {
            Schema elementType = field.schema().getElementType();
            List<Field> fields2 = elementType.getFields();
            String[] fisrstLine = reader.readNext();
            GenericRecord headerRecord = new GenericData.Record(elementType);
            //FIXME I know this check is bad , do proper implementation in later
            for (int i = 0; i < fields2.size(); i++) {
                //matching the first line with given headers in the input Avro schema
                if (!fields2.get(i).name().equals(fields2.get(i).name())) {
                    headerRecord.put(fields2.get(i).name(), fisrstLine[i]);
                    if (i == fields2.size() - 1) {
                        recordArray.add(headerRecord);
                    }
                } else {
                    break;
                }
            }
            while ((nextLine = reader.readNext()) != null) {
                GenericRecord record = new GenericData.Record(elementType);
                for (int i = 0; i < fields2.size(); i++) {
                    record.put(fields2.get(i).name(), nextLine[i]);
                }
                recordArray.add(record);
            }
            ParentRecord.put(field.name(), recordArray);
            reader.close();
        } catch (IOException e) {
            // TODO: log unrecognized data set
        }

        return ParentRecord;
    }

    public static String getType() {
        return "text/csv";
    }
}
