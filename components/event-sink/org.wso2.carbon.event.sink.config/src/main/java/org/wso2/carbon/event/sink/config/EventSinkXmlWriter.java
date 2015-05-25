/*
 * Copyright (c) 2014, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
 *
 * Licensed to the Apache Software Foundation (ASF) under one or more
 * contributor license agreements.  See the NOTICE file distributed with
 * this work for additional information regarding copyright ownership.
 * The ASF licenses this file to You under the Apache License, Version 2.0
 * (the "License"); you may not use this file except in compliance with
 * the License.  You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.wso2.carbon.event.sink.config;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.xml.serialize.OutputFormat;
import org.apache.xml.serialize.XMLSerializer;
import org.w3c.dom.Document;
import org.wso2.carbon.core.util.CryptoException;
import org.wso2.carbon.core.util.CryptoUtil;
import org.wso2.carbon.event.sink.EventSink;
import org.wso2.carbon.event.sink.EventSinkException;
import org.xml.sax.InputSource;
import org.xml.sax.SAXException;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import java.io.*;
import java.nio.charset.Charset;

/**
 * Creates Event Sink xml artifact and does operation on it
 */
public class EventSinkXmlWriter {
	private static final Log log = LogFactory.getLog(EventSinkXmlWriter.class);

	/**
	 * Writes given Event Sink details to xml file
	 *
	 * @param eventSink the Event Sink to be write
	 */
	public boolean writeEventSink(EventSink eventSink) throws EventSinkException {
		String filePath;
		filePath = EventSinkXmlReader.getTenantDeployementDirectoryPath();
		this.createEventSinkDirectory(filePath);
		EventSinkConfigXml eventSinkConfigXml = new EventSinkConfigXml();
		BufferedWriter bufferedWriter=null;
		try {
			bufferedWriter =
					new BufferedWriter(new FileWriter(new File(filePath, eventSink.getName() + ".xml")));
			String unFormattedXml = eventSinkConfigXml.buildEventSink(eventSink.getUsername(),CryptoUtil.
					                getDefaultCryptoUtil().encryptAndBase64Encode(eventSink.getPassword()
			                         .getBytes(Charset.forName("UTF-8"))), eventSink.getReceiverUrlSet(),
			                         eventSink.getAuthenticationUrlSet()).toString();

			///formatting xml
			DocumentBuilderFactory dbf = DocumentBuilderFactory.newInstance();
			DocumentBuilder db = dbf.newDocumentBuilder();
			InputSource is = new InputSource(new StringReader(unFormattedXml));
			final Document document = db.parse(is);
			OutputFormat format = new OutputFormat(document);
			format.setLineWidth(100);
			format.setIndenting(true);
			format.setIndent(4);
			Writer out = new StringWriter();
			XMLSerializer serializer = new XMLSerializer(out, format);
			serializer.serialize(document);
			bufferedWriter.write(out.toString());

		} catch (FileNotFoundException e) {
			throw new EventSinkException("Failed to open file to write event sink. File: " + filePath + ", " +
			                             "error: " + e);
		} catch (IOException e) {
			throw new EventSinkException("Failed to write event sink to file. File: " + filePath + ", error: " + e);
		} catch (ParserConfigurationException e) {
			throw new EventSinkException("Internal error occurred while writing event sink. Failed to format XML. " +
			                             "error: " + e);
		} catch (SAXException e) {
			throw new EventSinkException("Internal error occurred while writing event sink. Invalid XML. error: " + e);
		} catch (CryptoException e) {
			throw new EventSinkException("Password encryption failed. error: " + e);
		} finally {
			if (bufferedWriter!=null)
				try {
					bufferedWriter.flush();
					bufferedWriter.close();
					return true;
				} catch (IOException e) {
					throw new EventSinkException("Failed to close stream, error: " + e);
				}
		}
		return false;
	}

	/**
	 * Updates given Event Sink details
	 *
	 * @param eventSink the Event Sink to be updated
	 */
	public boolean updateEventSink(EventSink eventSink) throws EventSinkException {
		String filePath;
		filePath = EventSinkXmlReader.getTenantDeployementDirectoryPath();
		File eventSinkFile = new File(filePath + eventSink.getName() + ".xml");
		if (eventSinkFile.exists()) {
			eventSinkFile.delete();
			writeEventSink(eventSink);
			return true;
		} else {
			throw new EventSinkException(
					"Event Sink file cannot be found with name : " + eventSink.getName() + " in location " + filePath);
		}
	}

	/**
	 * Creates a directory in the specified location
	 *
	 * @param filePath location the directory should be created
	 */
	private boolean createEventSinkDirectory(String filePath) throws EventSinkException {
		File eventSinksDir = new File(filePath);

		// if the directory does not exist, create it
		if (!eventSinksDir.exists()) {
			try {
				boolean mkdir = eventSinksDir.mkdir();
				if (mkdir){
					return true;
				}
			} catch (SecurityException e) {
				throw new EventSinkException("Couldn't create event-Sinks directory in following location"+filePath+
				                             " with ERROR : "+e);
			}
		}
		return false;
	}
}