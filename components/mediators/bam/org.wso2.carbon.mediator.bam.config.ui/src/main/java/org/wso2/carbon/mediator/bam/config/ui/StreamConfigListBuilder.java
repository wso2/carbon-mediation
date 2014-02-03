/*
*  Copyright (c) 2005-2010, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
*
*  WSO2 Inc. licenses this file to you under the Apache License,
*  Version 2.0 (the "License"); you may not use this file except
*  in compliance with the License.
*  You may obtain a copy of the License at
*
*    http://www.apache.org/licenses/LICENSE-2.0
*
* Unless required by applicable law or agreed to in writing,
* software distributed under the License is distributed on an
* "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
* KIND, either express or implied.  See the License for the
* specific language governing permissions and limitations
* under the License.
*/

package org.wso2.carbon.mediator.bam.config.ui;

import org.wso2.carbon.mediator.bam.config.stream.Property;
import org.wso2.carbon.mediator.bam.config.stream.StreamConfiguration;
import org.wso2.carbon.mediator.bam.config.stream.StreamEntry;

import java.util.ArrayList;
import java.util.List;

/**
 * Builds an ArrayList of StreamConfiguration objects from the given String
 */
public class StreamConfigListBuilder {

    private static final String STREAM_SEPARATOR = "~";
    private static final String STREAM_DATA_SEPARATOR = "\\^";
    private static final String PROPERTY_SEPARATOR = ";";
    private static final String PROPERTY_VALUE_SEPARATOR = "::";
    private static final String SOAP_HEADER_ENTRY_NAME = "SOAPHeader";
    private static final String SOAP_HEADER_ENTRY_VALUE = "$SOAPHeader";
    private static final String SOAP_HEADER_ENTRY_TYPE = "STRING";
    private static final String SOAP_BODY_ENTRY_NAME = "SOAPBody";
    private static final String SOAP_BODY_ENTRY_VALUE = "$SOAPBody";
    private static final String SOAP_BODY_ENTRY_TYPE = "STRING";
    private static final String PROPERTY_TYPE_VALUE = "value";
    private static final String PROPERTY_TYPE_EXPRESSION = "expression";
    private static final String PROPERTY_DUMP_SYMBOL = "dump";

    private List<StreamConfiguration> streamConfigurations;
    private StreamConfiguration currentStreamConfiguration;
    private String propertiesString;

    public StreamConfigListBuilder() {
        streamConfigurations = new ArrayList<StreamConfiguration>();
    }

    public List<StreamConfiguration> getStreamConfigurationListFromString(String streamConfigurationListString){
        String [] streams = streamConfigurationListString.split(STREAM_SEPARATOR);
        for (String stream : streams) {
            if(this.isNotNullOrEmpty(stream)){
                this.extractStream(stream);
            }
        }
        return streamConfigurations;
    }

    private void extractStream(String stream) {
        int i = 0, j;
        currentStreamConfiguration = new StreamConfiguration();
        currentStreamConfiguration.setName(stream.split(STREAM_DATA_SEPARATOR)[i++]);
        currentStreamConfiguration.setVersion(stream.split(STREAM_DATA_SEPARATOR)[i++]);
        currentStreamConfiguration.setNickname(stream.split(STREAM_DATA_SEPARATOR)[i++]);
        currentStreamConfiguration.setDescription(stream.split(STREAM_DATA_SEPARATOR)[i++]);
        j = i + 1;
        if(stream.split(STREAM_DATA_SEPARATOR).length > i && (stream.split(STREAM_DATA_SEPARATOR)[i].contains(PROPERTY_VALUE_SEPARATOR) || stream.split(STREAM_DATA_SEPARATOR)[j].contains(PROPERTY_VALUE_SEPARATOR))){ // Only when properties exist
            propertiesString = stream.split(STREAM_DATA_SEPARATOR)[i];
            this.extractProperties();
        }
        if(stream.split(STREAM_DATA_SEPARATOR)[stream.split(STREAM_DATA_SEPARATOR).length-1].contains(PROPERTY_SEPARATOR) && !stream.split(STREAM_DATA_SEPARATOR)[stream.split(STREAM_DATA_SEPARATOR).length-1].contains(PROPERTY_VALUE_SEPARATOR)){
            this.extractDumpData(stream);
        }
        streamConfigurations.add(currentStreamConfiguration);
    }

    private void extractProperties() {
        Property currentProperty;
        int i;
        String[] properties = propertiesString.split(PROPERTY_SEPARATOR);
        for (String property : properties) {
            if(this.isNotNullOrEmpty(property)){
                i = 0;
                currentProperty = new Property();
                currentProperty.setKey(property.split(PROPERTY_VALUE_SEPARATOR)[i++]);
                currentProperty.setValue(property.split(PROPERTY_VALUE_SEPARATOR)[i++]);
                currentProperty.setType(property.split(PROPERTY_VALUE_SEPARATOR)[i++]);
                if(PROPERTY_TYPE_VALUE.equals(property.split(PROPERTY_VALUE_SEPARATOR)[i])){
                    currentProperty.setExpression(false);
                } else if(PROPERTY_TYPE_EXPRESSION.equals(property.split(PROPERTY_VALUE_SEPARATOR)[i])){
                    currentProperty.setExpression(true);
                }
                currentStreamConfiguration.getProperties().add(currentProperty);
            }
        }
    }

    private void extractDumpData(String stream) {
        StreamEntry headerEntry, bodyEntry;
        String dump = stream.split(STREAM_DATA_SEPARATOR)[stream.split(STREAM_DATA_SEPARATOR).length - 1];
        boolean dumpHeader = dump.split(PROPERTY_SEPARATOR)[0].equals(PROPERTY_DUMP_SYMBOL);
        boolean dumpBody = dump.split(PROPERTY_SEPARATOR)[1].equals(PROPERTY_DUMP_SYMBOL);
        if(dumpHeader){
            headerEntry = new StreamEntry();
            headerEntry.setName(SOAP_HEADER_ENTRY_NAME);
            headerEntry.setValue(SOAP_HEADER_ENTRY_VALUE);
            headerEntry.setType(SOAP_HEADER_ENTRY_TYPE);
            currentStreamConfiguration.getEntries().add(headerEntry);
        }
        if(dumpBody){
            bodyEntry = new StreamEntry();
            bodyEntry.setName(SOAP_BODY_ENTRY_NAME);
            bodyEntry.setValue(SOAP_BODY_ENTRY_VALUE);
            bodyEntry.setType(SOAP_BODY_ENTRY_TYPE);
            currentStreamConfiguration.getEntries().add(bodyEntry);
        }
    }

    private boolean isNotNullOrEmpty(String string){
        return string != null && !string.equals("");
    }
}
