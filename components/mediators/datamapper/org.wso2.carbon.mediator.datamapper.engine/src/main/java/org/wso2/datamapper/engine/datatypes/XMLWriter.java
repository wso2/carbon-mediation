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

package org.wso2.datamapper.engine.datatypes;

import org.apache.avro.generic.GenericRecord;
import org.apache.avro.io.DatumWriter;
import org.apache.avro.io.Encoder;
import org.apache.axiom.om.OMElement;
import org.apache.axiom.om.util.AXIOMUtil;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.synapse.SynapseException;
import org.wso2.datamapper.engine.outputAdapters.DummyEncoder;
import org.wso2.datamapper.engine.outputAdapters.WriterRegistry;

import javax.xml.stream.XMLStreamException;
import java.io.ByteArrayOutputStream;
import java.io.IOException;


/**
 * Generates the relevant output message when the data type is XML
 */

public class XMLWriter implements OutputWriter {

    private static final Log log = LogFactory.getLog(XMLWriter.class);

    /**
     * Gives the output message
     *
     * @param outputType output data type
     * @param result     mapping result
     * @return the output as an OMElement
     * @throws IOException
     */

    public OMElement getOutputMessage(String outputType,
                                      GenericRecord result) throws IOException {

        DatumWriter<GenericRecord> writer = null;
        ByteArrayOutputStream byteArrayOutputStream = new ByteArrayOutputStream();
        Encoder encoder = new DummyEncoder(byteArrayOutputStream);
        OMElement outMessage = null;
        try {

            writer = WriterRegistry.getInstance().get(outputType).newInstance();
            writer.setSchema(result.getSchema());
            writer.write(result, encoder);

            // Converts the result into the desired outputType
            outMessage = getOutputResult(byteArrayOutputStream.toString());

        } catch (Exception e) {
            handleException("Data coversion Failed", e);
        } finally {
            encoder.flush();
        }
        return outMessage;
    }

    /**
     *
     * @param inputType
     * @param inputMessage
     * @param mappingResourceLoader
     * @return
     * @throws IOException
     */

	/*public static OMElement getInputMessage(String inputType,OMElement inputMessage) throws IOException {

		DatumReader<GenericRecord> reader = null;
		Decoder decoder = null;
		OMElement inMessage = null;
		try {
		ByteArrayInputStream byteArrayInputStream = new ByteArrayInputStream(inputType.getBytes());
		reader = ReaderRegistry.getInstance().get(inputType).newInstance();
		reader.setSchema(mappingResourceLoader.getInputSchema());
		decoder = new DummyDecoder(byteArrayInputStream);
		reader.read(reuse, decoder);
	
	
		} catch (Exception e) {
			handleException("Data coversion Failed", e);
		} finally {
			decoder.flush();
		}
		return inputMessage;
		
		
	}*/

    /**
     * Gives the final output as an OMElement
     *
     * @param result mapping result
     * @return output message as an OMElement
     * @throws XMLStreamException
     */

    private static OMElement getOutputResult(String result)
            throws XMLStreamException {
        return AXIOMUtil.stringToOM(result);

    }

    private static void handleException(String message, Exception e) {
        log.error(message, e);
        throw new SynapseException(message, e);
    }

}



