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


/**
 * A basic struct to store cell row/column information and the associated data
 */
public class GoogleSpreadsheetCellAddress {
  public final int row;
  public final int col;
  public final String idString;

  /**
   * Constructs a CellAddress representing the specified {@code row} and
   * {@code col}.  The idString will be set in 'RnCn' notation.
   */
  public GoogleSpreadsheetCellAddress(int row, int col, String idString) {
    this.row = row;
    this.col = col;
    this.idString = idString;
  }
}

