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
import java.util.List;

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
import com.google.gdata.data.spreadsheet.CellEntry;
import com.google.gdata.data.spreadsheet.SpreadsheetEntry;
import com.google.gdata.data.spreadsheet.WorksheetEntry;
import com.google.gdata.util.ServiceException;

public class GoogleSpreadsheetGetCellRange extends AbstractConnector {

	public static final String WORKSHEET_NAME = "worksheetName";
	public static final String SPREADSHEET_NAME = "spreadsheetName";
	public static final String MIN_ROW = "minRow";
	public static final String MAX_ROW = "maxRow";
	public static final String MIN_COLUMN = "minColumn";
	public static final String MAX_COLUMN = "maxColumn";
	private static Log log = LogFactory
			.getLog(GoogleSpreadsheetGetAllCells.class);

	public void connect(MessageContext messageContext) throws ConnectException {
		try {
			String worksheetName = GoogleSpreadsheetUtils
					.lookupFunctionParam(messageContext, WORKSHEET_NAME);
			String spreadsheetName = GoogleSpreadsheetUtils
					.lookupFunctionParam(messageContext, SPREADSHEET_NAME);
			String minRow = GoogleSpreadsheetUtils
					.lookupFunctionParam(messageContext, MIN_ROW);
			String maxRow = GoogleSpreadsheetUtils
					.lookupFunctionParam(messageContext, MAX_ROW);
			String minColumn = GoogleSpreadsheetUtils
					.lookupFunctionParam(messageContext, MIN_COLUMN);
			String maxColumn = GoogleSpreadsheetUtils
					.lookupFunctionParam(messageContext, MAX_COLUMN);
			
			if (worksheetName == null || "".equals(worksheetName.trim())
					|| spreadsheetName == null
					|| "".equals(spreadsheetName.trim()) || minRow == null || "".equals(minRow.trim())
					|| maxRow == null || "".equals(maxRow.trim())
					|| minColumn == null || "".equals(minColumn.trim())
					|| maxColumn == null || "".equals(maxColumn.trim())
					) {
				log.error("Please make sure you have given the required parameters");
                ConnectException connectException = new ConnectException("Please make sure you have given the required parameters");
                GoogleSpreadsheetUtils.storeErrorResponseStatus(messageContext, connectException);
                return;
			}
			
			int minRowInt, maxRowInt, minColInt, maxColInt;
			
			try {
				minRowInt = Integer.parseInt(minRow);
				maxRowInt = Integer.parseInt(maxRow);
				minColInt = Integer.parseInt(minColumn);
				maxColInt = Integer.parseInt(maxColumn);
				
			} catch(NumberFormatException ex) {
				log.error("Specify a valid number for row/column parameters", ex);
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

			WorksheetEntry wsEntry = gssWorksheet
					.getWorksheetByTitle(worksheetName);

		
			GoogleSpreadsheetCellData gssData = new GoogleSpreadsheetCellData(
					ssService);
			
			List<CellEntry> resultData = gssData.getRange(wsEntry, minRowInt, maxRowInt, minColInt, maxColInt);
					
			int resultSize = resultData.size();

            if(messageContext.getEnvelope().getBody().getFirstElement() != null) {
                messageContext.getEnvelope().getBody().getFirstElement().detach();
            }
			
			OMFactory factory   = OMAbstractFactory.getOMFactory();
	        OMNamespace ns      = factory.createOMNamespace("http://org.wso2.esbconnectors.googlespreadsheet", "ns");
	        OMElement searchResult  = factory.createOMElement("getCellRangeResult", ns);        
	       
	        OMElement result      = factory.createOMElement("result", ns); 
	        searchResult.addChild(result);
	        result.setText("true");

            OMElement data      = factory.createOMElement("data", ns);
            searchResult.addChild(data);

            for(int iterateCount=0; iterateCount < resultSize; iterateCount++)
			{
				if(resultData.get(iterateCount) != null) {
						String cellNotation = "R"+resultData.get(iterateCount).getCell().getRow()+"C"+resultData.get(iterateCount).getCell().getCol();
					 	OMElement cellId      = factory.createOMElement(cellNotation, ns);        
				        data.addChild(cellId);
				        cellId.setText(resultData.get(iterateCount).getCell().getValue());
					
				}
			}
			
			messageContext.getEnvelope().getBody().addChild(searchResult);
			

		} catch (IOException te) {
			log.error("Failed to show status: " + te.getMessage(), te);
			GoogleSpreadsheetUtils.storeErrorResponseStatus(
					messageContext, te);
		} catch (ServiceException te) {
			log.error("Failed to show status: " + te.getMessage(), te);
			GoogleSpreadsheetUtils.storeErrorResponseStatus(
					messageContext, te);
		} catch (Exception te) {
			log.error("Failed to show status: " + te.getMessage(), te);
			GoogleSpreadsheetUtils.storeErrorResponseStatus(
					messageContext, te);
		}
	}


}
