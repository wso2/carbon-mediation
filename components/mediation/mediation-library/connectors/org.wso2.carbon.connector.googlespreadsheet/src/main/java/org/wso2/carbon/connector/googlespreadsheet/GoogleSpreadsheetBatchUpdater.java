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
import java.net.MalformedURLException;
import java.net.URL;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import com.google.gdata.client.spreadsheet.SpreadsheetService;
import com.google.gdata.data.Link;
import com.google.gdata.data.batch.BatchOperationType;
import com.google.gdata.data.batch.BatchStatus;
import com.google.gdata.data.batch.BatchUtils;
import com.google.gdata.data.spreadsheet.CellEntry;
import com.google.gdata.data.spreadsheet.CellFeed;
import com.google.gdata.data.spreadsheet.WorksheetEntry;
import com.google.gdata.util.AuthenticationException;
import com.google.gdata.util.ServiceException;

/**
 * 
 * 
 * Class implemented for handling batch requests with google spreadsheet API
 *
 */

public class GoogleSpreadsheetBatchUpdater {
	  
	  private SpreadsheetService service;	  
	  private static Log log = LogFactory
				.getLog(GoogleSpreadsheetBatchUpdater.class);

	  public GoogleSpreadsheetBatchUpdater(SpreadsheetService service) {
		  this.service = service;
		  
	  }
	  
	  /**
	   * update the specified {@link WorksheetEntry} with the given {@code cellAddrs} 
	   * data
	   * @param worksheet the worksheet to be updated	   
	   * @param cellAddrs list of cell addresses to be retrieved.	   
	   */

	  public void updateBatch(WorksheetEntry worksheet, List<GoogleSpreadsheetCellAddress> cellAddrs) 
	      throws AuthenticationException, MalformedURLException, IOException, ServiceException {

		
	    long startTime = System.currentTimeMillis();

	    URL cellFeedUrl = worksheet.getCellFeedUrl();
	    CellFeed cellFeed = service.getFeed(cellFeedUrl, CellFeed.class);
	    
	    CellFeed batchRequest = new CellFeed();
	   
	    for (GoogleSpreadsheetCellAddress cellAddr : cellAddrs) {
	      CellEntry batchEntry = new CellEntry(cellAddr.row, cellAddr.col, cellAddr.idString);	      
	      batchEntry.setId(String.format("%s/%s", cellFeedUrl.toString(), String.format("R%sC%s", cellAddr.row, cellAddr.col)));
	      BatchUtils.setBatchId(batchEntry, cellAddr.idString);
	      BatchUtils.setBatchOperationType(batchEntry, BatchOperationType.UPDATE);
	      batchRequest.getEntries().add(batchEntry);
	    }

	    // Submit the update
	    Link batchLink = cellFeed.getLink(Link.Rel.FEED_BATCH, Link.Type.ATOM);
	    service.setHeader("If-Match", "*");
	    CellFeed batchResponse = service.batch(new URL(batchLink.getHref()), batchRequest);
	    service.setHeader("If-Match", null);

	    // Check the results
	    boolean isSuccess = true;
	    for (CellEntry entry : batchResponse.getEntries()) {
	      String batchId = BatchUtils.getBatchId(entry);
	      if (!BatchUtils.isSuccess(entry)) {
	        isSuccess = false;
	        BatchStatus status = BatchUtils.getBatchStatus(entry);
	        log.info(String.format("%s failed (%s) %s", batchId, status.getReason(), status.getContent()));
	      }
	    }

	    log.info(isSuccess ? "\nBatch operations successful." : "\nBatch operations failed");
	    log.info(String.format("%s ms elapsed", System.currentTimeMillis() - startTime));
	  }

	  /**
	   * Connects to the specified {@link SpreadsheetService} and uses a batch
	   * request to retrieve a {@link CellEntry} for each cell enumerated in {@code
	   * cellAddrs}. Each cell entry is placed into a map keyed by its RnCn
	   * identifier.
	   *
	   * @param ssSvc the spreadsheet service to use.
	   * @param cellFeedUrl url of the cell feed.
	   * @param cellAddrs list of cell addresses to be retrieved.
	   * @return a map consisting of one {@link CellEntry} for each address in {@code
	   *         cellAddrs}
	   */
	  public Map<String, CellEntry> getCellEntryMap(
	      SpreadsheetService ssSvc, URL cellFeedUrl, List<GoogleSpreadsheetCellAddress> cellAddrs)
	      throws IOException, ServiceException {
	    CellFeed batchRequest = new CellFeed();
	    for (GoogleSpreadsheetCellAddress cellId : cellAddrs) {
	      CellEntry batchEntry = new CellEntry(cellId.row, cellId.col, cellId.idString);
	      batchEntry.setId(String.format("%s/%s", cellFeedUrl.toString(), cellId.idString));
	      BatchUtils.setBatchId(batchEntry, cellId.idString);
	      BatchUtils.setBatchOperationType(batchEntry, BatchOperationType.QUERY);
	      batchRequest.getEntries().add(batchEntry);
	    }

	    CellFeed cellFeed = ssSvc.getFeed(cellFeedUrl, CellFeed.class);
	    CellFeed queryBatchResponse =
	      ssSvc.batch(new URL(cellFeed.getLink(Link.Rel.FEED_BATCH, Link.Type.ATOM).getHref()),
	                  batchRequest);

	    Map<String, CellEntry> cellEntryMap = new HashMap<String, CellEntry>(cellAddrs.size());
	    for (CellEntry entry : queryBatchResponse.getEntries()) {
	      cellEntryMap.put(BatchUtils.getBatchId(entry), entry);
	      log.info(String.format("batch %s {CellEntry: id=%s editLink=%s inputValue=%s",
	          BatchUtils.getBatchId(entry), entry.getId(), entry.getEditLink().getHref(),
	          entry.getCell().getInputValue()));
	    }

	    return cellEntryMap;
	  }
	  
	  


}
