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

package org.wso2.datamapper.engine.input;

import org.wso2.datamapper.engine.types.InputOutputDataTypes;
import org.wso2.datamapper.engine.input.readers.CsvInputReader;
import org.wso2.datamapper.engine.input.readers.InputDataReaderAdapter;
import org.wso2.datamapper.engine.input.readers.JsonInputReader;
import org.wso2.datamapper.engine.input.readers.XmlInputReader;

/**
 * Factory class for writer classes
 */
public class InputReaderFactory {

    /**
     * This method returns the matching {@link InputDataReaderAdapter} instance according to the passed data Type
     *
     * @param dataType
     * @return
     */
    public static InputDataReaderAdapter getInputDataReader(String dataType) {

        InputDataReaderAdapter inputReader = null;
        switch (InputOutputDataTypes.DataType.fromString(dataType)) {
        case CSV:
            inputReader = new CsvInputReader();
            break;
        case XML:
            inputReader = new XmlInputReader();
            break;
        case JSON:
            inputReader = new JsonInputReader();
            break;
        default:
            throw new IllegalArgumentException("Invalid data type found for Input Readers : " + dataType);
        }
        return inputReader;
    }
}
