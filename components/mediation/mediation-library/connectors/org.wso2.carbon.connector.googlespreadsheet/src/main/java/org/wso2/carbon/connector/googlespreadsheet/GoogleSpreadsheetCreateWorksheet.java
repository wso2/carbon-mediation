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


package org.wso2.carbon.connector.googlespreadsheet;

import java.io.IOException;

import org.apache.axiom.om.OMAbstractFactory;
import org.apache.axiom.om.OMElement;
import org.apache.axiom.om.OMFactory;
import org.apache.axiom.om.OMNamespace;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.synapse.MessageContext;
import org.wso2.carbon.connector.core.AbstractConnector;
import org.wso2.carbon.connector.core.ConnectException;

import com.google.gdata.client.spreadsheet.SpreadsheetService;
import com.google.gdata.data.spreadsheet.SpreadsheetEntry;
import com.google.gdata.util.ServiceException;

public class GoogleSpreadsheetCreateWorksheet extends AbstractConnector {
	
	private static final int DEFAULT_ROW_COUNT = 100;
	private static final int DEFAULT_COLUMN_COUNT = 20;
	public static final String WORKSHEET_NAME = "worksheetName";
	public static final String SPREADSHEET_NAME = "spreadsheetName";
	public static final String WORKSHEET_ROWS = "worksheetRows";
	public static final String WORKSHEET_COLUMNS = "worksheetColumns";
	private int rowCount = DEFAULT_ROW_COUNT;
	private int columnCount = DEFAULT_COLUMN_COUNT;
	private static Log log = LogFactory
			.getLog(GoogleSpreadsheetCreateWorksheet.class);

	public void connect(MessageContext messageContext) throws ConnectException {
		
		try {
			String worksheetName = GoogleSpreadsheetUtils
					.lookupFunctionParam(messageContext, WORKSHEET_NAME);
			String spreadsheetName = GoogleSpreadsheetUtils
					.lookupFunctionParam(messageContext, SPREADSHEET_NAME);
			String worksheetRows = GoogleSpreadsheetUtils
					.lookupFunctionParam(messageContext, WORKSHEET_ROWS);
			String worksheetColumns = GoogleSpreadsheetUtils
					.lookupFunctionParam(messageContext, WORKSHEET_COLUMNS);
			if (worksheetName == null || "".equals(worksheetName.trim())
					|| spreadsheetName == null
					|| "".equals(spreadsheetName.trim())) {				
				log.error("Please make sure you have given a name for the new worksheet and spreadsheet");
                ConnectException connectException = new ConnectException("Please make sure you have given a name for the new spreadsheet");
                GoogleSpreadsheetUtils.storeErrorResponseStatus(messageContext, connectException);
                return;

			}

			try {
			if(worksheetRows != null) {
				rowCount = Integer.parseInt(worksheetRows);
			}
			if(worksheetColumns != null) {
				columnCount = Integer.parseInt(worksheetColumns);
			}
			} catch(NumberFormatException ex) {
				log.error("Please enter a valid number for worksheetRows and worksheetColumns parameter");
                GoogleSpreadsheetUtils.storeErrorResponseStatus(messageContext, ex);
                return;

			}
			
			SpreadsheetService ssService = new GoogleSpreadsheetClientLoader(
					messageContext).loadSpreadsheetService();

			GoogleSpreadsheet gss = new GoogleSpreadsheet(ssService);

			SpreadsheetEntry ssEntry = gss
					.getSpreadSheetsByTitle(spreadsheetName);			

			GoogleSpreadsheetWorksheet gssWorksheet = new GoogleSpreadsheetWorksheet(
					ssService, ssEntry.getWorksheetFeedUrl());
				
			gssWorksheet.createWorksheet(worksheetName, rowCount, columnCount);
			
			if(messageContext.getEnvelope().getBody().getFirstElement() != null) {
				messageContext.getEnvelope().getBody().getFirstElement().detach();
			}
			
			OMFactory factory   = OMAbstractFactory.getOMFactory();
	        OMNamespace ns      = factory.createOMNamespace("http://org.wso2.esbconnectors.googlespreadsheet", "ns");
	        OMElement searchResult  = factory.createOMElement("createWorksheetResult", ns);  
	        OMElement result      = factory.createOMElement("result", ns); 
	        searchResult.addChild(result);
	        result.setText("true");		
			messageContext.getEnvelope().getBody().addChild(searchResult);

		} catch (IOException te) {
			log.error("Failed to show status: " + te.getMessage(), te);
			GoogleSpreadsheetUtils.storeErrorResponseStatus(messageContext, te);
		} catch (ServiceException te) {
			log.error("Failed to show status: " + te.getMessage(), te);
			GoogleSpreadsheetUtils.storeErrorResponseStatus(messageContext, te);
		}
	}

}
