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

import java.io.IOException;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;

import com.google.gdata.client.spreadsheet.SpreadsheetService;
import com.google.gdata.data.Person;
import com.google.gdata.data.spreadsheet.SpreadsheetEntry;
import com.google.gdata.data.spreadsheet.SpreadsheetFeed;
import com.google.gdata.util.ServiceException;

/**
 * 
 * Class implemented for accessing google spreadsheet application programming interface (API) features related to a spreadsheet
 *
 */

public class GoogleSpreadsheet {
	
	private SpreadsheetService service;
	
	public GoogleSpreadsheet(SpreadsheetService service) {
		this.service = service;
	}
	
	
	/**
	   * Lists all the spreadsheets for the user account
	   * 
	   * @throws ServiceException when the request causes an error in the Google
	   *         Spreadsheets service.
	   * @throws IOException when an error occurs in communication with the Google
	   *         Spreadsheets service.
	   */
	  public List<String> getAllSpreadsheets() throws IOException, ServiceException {
		  // Define the URL to request.  This should never change.
		    URL SPREADSHEET_FEED_URL = new URL(
		        "https://spreadsheets.google.com/feeds/spreadsheets/private/full");

		    List<String> resultSet = null;
		    // Make a request to the API and get all spreadsheets.
		    SpreadsheetFeed feed = service.getFeed(SPREADSHEET_FEED_URL, SpreadsheetFeed.class);
		    List<SpreadsheetEntry> spreadsheets = feed.getEntries();
		    
		    if(spreadsheets != null) {
		    	resultSet = new ArrayList<String>();
		    	// Iterate through all of the spreadsheets returned
		    	for (SpreadsheetEntry spreadsheet : spreadsheets) {
		    		resultSet.add(spreadsheet.getTitle().getPlainText());
		    	}
		    }
		    return resultSet;
	  }
	  
	  
	  /**
	   * Get the spreadsheets by title
	   * 
	   * @throws ServiceException when the request causes an error in the Google
	   *         Spreadsheets service.
	   * @throws IOException when an error occurs in communication with the Google
	   *         Spreadsheets service.
	   */
	  public List<String> getSpreadSheetsByTitleList(String title) throws IOException, ServiceException {

          List<String> resultSet = new ArrayList<String>();
		  // Define the URL to request.  This should never change.
		    URL SPREADSHEET_FEED_URL = new URL(
		        "https://spreadsheets.google.com/feeds/spreadsheets/private/full");

		    // Make a request to the API and get all spreadsheets.
		    SpreadsheetFeed feed = service.getFeed(SPREADSHEET_FEED_URL, SpreadsheetFeed.class);
		    List<SpreadsheetEntry> spreadsheets = feed.getEntries();


		    // Iterate through all of the spreadsheets returned
		    for (SpreadsheetEntry spreadsheet : spreadsheets) {
		    	if(spreadsheet.getTitle().getPlainText().equalsIgnoreCase(title)) {
		    		resultSet.add(spreadsheet.getTitle().getPlainText());
		    	}
		    }
		    
		    return resultSet;
		    
	  }


    /**
     * Get the spreadsheets by title
     *
     * @throws ServiceException when the request causes an error in the Google
     *         Spreadsheets service.
     * @throws IOException when an error occurs in communication with the Google
     *         Spreadsheets service.
     */
    public SpreadsheetEntry getSpreadSheetsByTitle(String title) throws IOException, ServiceException {

        SpreadsheetEntry spreadsheetResult = null;
        // Define the URL to request.  This should never change.
        URL SPREADSHEET_FEED_URL = new URL(
                "https://spreadsheets.google.com/feeds/spreadsheets/private/full");

        // Make a request to the API and get all spreadsheets.
        SpreadsheetFeed feed = service.getFeed(SPREADSHEET_FEED_URL, SpreadsheetFeed.class);
        List<SpreadsheetEntry> spreadsheets = feed.getEntries();

        // Iterate through all of the spreadsheets returned
        for (SpreadsheetEntry spreadsheet : spreadsheets) {
            if(spreadsheet.getTitle().getPlainText().equalsIgnoreCase(title)) {
                spreadsheetResult = spreadsheet;
                break;
            }
        }

        return spreadsheetResult;

    }
	  
	  /**
	   * Get Authors of the spreadsheet
	   * 
	   * @throws ServiceException when the request causes an error in the Google
	   *         Spreadsheets service.
	   * @throws IOException when an error occurs in communication with the Google
	   *         Spreadsheets service.
	   */
	  public List<Person> getAuthors(SpreadsheetEntry spreadsheet) throws IOException, ServiceException {
		  return spreadsheet.getAuthors();
	  }
	  
	
	

}
