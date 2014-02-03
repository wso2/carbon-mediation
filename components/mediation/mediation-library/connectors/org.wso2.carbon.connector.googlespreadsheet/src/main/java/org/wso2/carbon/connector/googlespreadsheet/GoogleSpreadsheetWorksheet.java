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

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import com.google.gdata.client.spreadsheet.CellQuery;
import com.google.gdata.client.spreadsheet.FeedURLFactory;
import com.google.gdata.client.spreadsheet.SpreadsheetQuery;
import com.google.gdata.client.spreadsheet.SpreadsheetService;
import com.google.gdata.client.spreadsheet.WorksheetQuery;
import com.google.gdata.data.Link;
import com.google.gdata.data.Person;
import com.google.gdata.data.PlainTextConstruct;
import com.google.gdata.data.spreadsheet.Cell;
import com.google.gdata.data.spreadsheet.CellEntry;
import com.google.gdata.data.spreadsheet.CellFeed;
import com.google.gdata.data.spreadsheet.SpreadsheetEntry;
import com.google.gdata.data.spreadsheet.SpreadsheetFeed;
import com.google.gdata.data.spreadsheet.WorksheetEntry;
import com.google.gdata.data.spreadsheet.WorksheetFeed;
import com.google.gdata.util.ServiceException;

/**
 * 
 * 
 * Class implemented for accessing google spreadsheet application programming interface (API) features related to a worksheet
 *
 */

public class GoogleSpreadsheetWorksheet {
	
	private SpreadsheetService service;
	private URL worksheetFeedUrl;
	private FeedURLFactory factory;
	private static Log log = LogFactory
			.getLog(GoogleSpreadsheetBatchUpdater.class);
	
	public GoogleSpreadsheetWorksheet(SpreadsheetService service, URL worksheetFeedUrl) {
		this.service = service;
		this.worksheetFeedUrl = worksheetFeedUrl;
		this.factory = FeedURLFactory.getDefault();
	}
	
	/**
	   * Get Authors of the worksheet
	   * 
	   * @throws ServiceException when the request causes an error in the Google
	   *         Spreadsheets service.
	   * @throws IOException when an error occurs in communication with the Google
	   *         Spreadsheets service.
	   */
	  public List<Person> getAuthors(WorksheetEntry worksheet) throws IOException, ServiceException {
		  return worksheet.getAuthors();
	  }
	  
	
	  /**
	   * Lists all the worksheets in the loaded spreadsheet.
	   * 
	   * @throws ServiceException when the request causes an error in the Google
	   *         Spreadsheets service.
	   * @throws IOException when an error occurs in communication with the Google
	   *         Spreadsheets service.
	   */
	  public List<String> getAllWorksheets() throws IOException, ServiceException {
		List<String> result = new ArrayList<String>();
	    WorksheetFeed worksheetFeed = service.getFeed(worksheetFeedUrl,
	        WorksheetFeed.class);
	    for (WorksheetEntry worksheet : worksheetFeed.getEntries()) {
	    	result.add(worksheet.getTitle().getPlainText());	      
	    }
	    return result;
	  }
	  
	  
	  /**
	   * Get the worksheet by title in the loaded spreadsheet.
	   * 
	   * @throws ServiceException when the request causes an error in the Google
	   *         Spreadsheets service.
	   * @throws IOException when an error occurs in communication with the Google
	   *         Spreadsheets service.
	   */
	  public List<String> getWorksheetByTitleList(String worksheetName) throws IOException, ServiceException {
          List<String> resultSet = new ArrayList<String>();
	    WorksheetFeed worksheetFeed = service.getFeed(worksheetFeedUrl,
	        WorksheetFeed.class);
	    WorksheetEntry worksheetResult = null;
	    for (WorksheetEntry worksheet : worksheetFeed.getEntries()) {
	      if(worksheet.getTitle().getPlainText().equalsIgnoreCase(worksheetName)) {
              resultSet.add(worksheet.getTitle().getPlainText()) ;
	      }
	    }
	    return resultSet;
	  }


    /**
     * Get the worksheet by title in the loaded spreadsheet.
     *
     * @throws ServiceException when the request causes an error in the Google
     *         Spreadsheets service.
     * @throws IOException when an error occurs in communication with the Google
     *         Spreadsheets service.
     */
    public WorksheetEntry getWorksheetByTitle(String worksheetName) throws IOException, ServiceException {
        WorksheetFeed worksheetFeed = service.getFeed(worksheetFeedUrl,
                WorksheetFeed.class);
        WorksheetEntry worksheetResult = null;
        for (WorksheetEntry worksheet : worksheetFeed.getEntries()) {
            String title = worksheet.getTitle().getPlainText();
            int rowCount = worksheet.getRowCount();
            int colCount = worksheet.getColCount();
            System.out.println("\t" + title + " - rows:" + rowCount + " cols: "
                    + colCount);
            if(worksheet.getTitle().getPlainText().equalsIgnoreCase(worksheetName)) {
                worksheetResult = worksheet;
                break;
            }
        }
        return worksheetResult;
    }
	  
	  /**
	   * Deletes the worksheet specified by the title parameter. Note that worksheet
	   * titles are not unique, so this method just updates the first worksheet it
	   * finds.
	   * 
	   * @param title a String containing the name of the worksheet to delete.
	   * 
	   * @throws ServiceException when the request causes an error in the Google
	   *         Spreadsheets service.
	   * @throws IOException when an error occurs in communication with the Google
	   *         Spreadsheets service.
	   */
	  public void deleteWorksheet(String title) throws IOException,
	      ServiceException {
	    WorksheetFeed worksheetFeed = service.getFeed(worksheetFeedUrl,
	        WorksheetFeed.class);
	    for (WorksheetEntry worksheet : worksheetFeed.getEntries()) {
	      String currTitle = worksheet.getTitle().getPlainText();
	      if (currTitle.equals(title)) {
	        worksheet.delete();
	        log.info("Worksheet deleted.");
	        return;
	      }
	    }

	    // If it got this far, the worksheet wasn't found.
	    log.info("Worksheet not found: " + title);
	  }
	  
	  
	  /**
	   * Creates a new worksheet in the loaded spreadsheets, using the title and
	   * sizes given.
	   * 
	   * @param title a String containing a name for the new worksheet.
	   * @param rowCount the number of rows the new worksheet should have.
	   * @param colCount the number of columns the new worksheet should have.
	   * 
	   * @throws ServiceException when the request causes an error in the Google
	   *         Spreadsheets service.
	   * @throws IOException when an error occurs in communication with the Google
	   *         Spreadsheets service.
	   */
	  public void createWorksheet(String title, int rowCount, int colCount)
	      throws IOException, ServiceException {
	    WorksheetEntry worksheet = new WorksheetEntry();
	    worksheet.setTitle(new PlainTextConstruct(title));
	    worksheet.setRowCount(rowCount);
	    worksheet.setColCount(colCount);
	    service.insert(worksheetFeedUrl, worksheet);
	  }

	  /**
	   * Updates the worksheet meta data specified by the oldTitle parameter, with the given
	   * title and sizes. Note that worksheet titles are not unique, so this method
	   * just updates the first worksheet it finds. Hey, it's just sample code - no
	   * refunds!
	   * 
	   * @param oldTitle a String specifying the worksheet to update.
	   * @param newTitle a String containing the new name for the worksheet.
	   * @param rowCount the number of rows the new worksheet should have.
	   * @param colCount the number of columns the new worksheet should have.
	   * 
	   * @throws ServiceException when the request causes an error in the Google
	   *         Spreadsheets service.
	   * @throws IOException when an error occurs in communication with the Google
	   *         Spreadsheets service.
	   */
	  public void updateWorksheetMetadata(String oldTitle, String newTitle, int rowCount,
	      int colCount) throws IOException, ServiceException {
	    WorksheetFeed worksheetFeed = service.getFeed(worksheetFeedUrl,
	        WorksheetFeed.class);
	    for (WorksheetEntry worksheet : worksheetFeed.getEntries()) {
	      String currTitle = worksheet.getTitle().getPlainText();
	      if (currTitle.equals(oldTitle)) {
	        worksheet.setTitle(new PlainTextConstruct(newTitle));
	        if(rowCount >0) {
	        worksheet.setRowCount(rowCount);
	        }
	        if(colCount>0) {
	        worksheet.setColCount(colCount);
	        }
	        worksheet.update();
	        log.info("Worksheet updated.");
	        return;
	      }
	    }

	    // If it got this far, the worksheet wasn't found.
	    log.info("Worksheet not found: " + oldTitle);
	  }
	  
	  /**
	   * Clears all the cell entries in the worksheet.
	   *
	   * @param spreadsheet the name of the spreadsheet
	   * @param worksheet the name of the worksheet
	   * @throws Exception if error is encountered, such as bad permissions
	   */
	  public void purgeWorksheet(String spreadsheet, String worksheet) 
	      throws Exception {

	    WorksheetEntry worksheetEntry = getWorksheet(spreadsheet, worksheet);
	    CellFeed cellFeed = service.getFeed(worksheetEntry.getCellFeedUrl(), 
	        CellFeed.class);

	    List<CellEntry> cells = cellFeed.getEntries();
	    for (CellEntry cell : cells) {
	      Link editLink = cell.getEditLink();
	      service.setHeader("If-Match", "*");
	      service.delete(new URL(editLink.getHref()));
	      service.setHeader("If-Match", null);
	      
	    }
	  }
	  
	  /**
	   * Gets the SpreadsheetEntry for the first spreadsheet with that name
	   * retrieved in the feed.
	   *
	   * @param spreadsheet the name of the spreadsheet
	   * @return the first SpreadsheetEntry in the returned feed, so latest
	   * spreadsheet with the specified name
	   * @throws Exception if error is encountered, such as no spreadsheets with the
	   * name
	   */
	  public SpreadsheetEntry getSpreadsheet(String spreadsheet)
	      throws Exception {
	    
	      SpreadsheetQuery spreadsheetQuery 
	        = new SpreadsheetQuery(factory.getSpreadsheetsFeedUrl());
	      spreadsheetQuery.setTitleQuery(spreadsheet);
	      SpreadsheetFeed spreadsheetFeed = service.query(spreadsheetQuery, 
	          SpreadsheetFeed.class);
	      List<SpreadsheetEntry> spreadsheets = spreadsheetFeed.getEntries();
	      if (spreadsheets.isEmpty()) {
	        throw new Exception("No spreadsheets with that name");
	      }

	      return spreadsheets.get(0);
	  }

	  /**
	   * Get the WorksheetEntry for the worksheet in the spreadsheet with the
	   * specified name.
	   *
	   * @param spreadsheet the name of the spreadsheet
	   * @param worksheet the name of the worksheet in the spreadsheet
	   * @return worksheet with the specified name in the spreadsheet with the
	   * specified name
	   * @throws Exception if error is encountered, such as no spreadsheets with the
	   * name, or no worksheet wiht the name in the spreadsheet
	   */
	  public WorksheetEntry getWorksheet(String spreadsheet, String worksheet) 
	      throws Exception {

	    SpreadsheetEntry spreadsheetEntry = getSpreadsheet(spreadsheet);

	    WorksheetQuery worksheetQuery
	      = new WorksheetQuery(spreadsheetEntry.getWorksheetFeedUrl());

	    worksheetQuery.setTitleQuery(worksheet);
	    WorksheetFeed worksheetFeed = service.query(worksheetQuery,
	        WorksheetFeed.class);
	    List<WorksheetEntry> worksheets = worksheetFeed.getEntries();
	    if (worksheets.isEmpty()) {
	      throw new Exception("No worksheets with that name in spreadhsheet "
	          + spreadsheetEntry.getTitle().getPlainText());
	    }

	    return worksheets.get(0);
	  }
	  
	  /**
	   * Retrieves the columns headers from the cell feed of the worksheet
	   * entry.
	   *
	   * @param worksheet worksheet entry containing the cell feed in question
	   * @return a list of column headers
	   * @throws Exception if error in retrieving the spreadsheet information
	   */
	  public List<String> getColumnHeaders(WorksheetEntry worksheet)
	      throws Exception {
	    List<String> headers = new ArrayList<String>();

	    // Get the appropriate URL for a cell feed
	    URL cellFeedUrl = worksheet.getCellFeedUrl();

	    // Create a query for the top row of cells only (1-based)
	    CellQuery cellQuery = new CellQuery(cellFeedUrl);
	    cellQuery.setMaximumRow(1);

	    // Get the cell feed matching the query
	    CellFeed topRowCellFeed = service.query(cellQuery, CellFeed.class);

	    // Get the cell entries fromt he feed
	    List<CellEntry> cellEntries = topRowCellFeed.getEntries();
	    for (CellEntry entry : cellEntries) {

	      // Get the cell element from the entry
	      Cell cell = entry.getCell();
	      headers.add(cell.getValue());
	    }

	    return headers;
	  }


}
