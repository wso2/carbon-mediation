/*
*  Copyright (c) 2005-2013, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
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

import com.google.gdata.client.spreadsheet.SpreadsheetService;

/**
 * 
 * 
 * Class implemented for accessing google spreadsheet application programming interface (API) features related to a spreadsheet service
 *
 */

public class GoogleSpreadsheetService {
	
	private SpreadsheetService spreadsheetService;
	
	public SpreadsheetService getSpreadsheetService() {
		return spreadsheetService;
	}

	private static final String applicationName = "WSO2_ESB_SPREADSHEET_CONNECTOR_V1";
	
	public GoogleSpreadsheetService() {
		this.spreadsheetService = new SpreadsheetService(applicationName);
	}

}
