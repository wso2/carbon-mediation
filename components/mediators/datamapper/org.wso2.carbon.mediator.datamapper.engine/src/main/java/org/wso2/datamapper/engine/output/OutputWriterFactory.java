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

package org.wso2.datamapper.engine.output;

import org.apache.avro.generic.GenericDatumWriter;
import org.wso2.datamapper.engine.types.InputOutputDataTypes;
import org.wso2.datamapper.engine.output.writers.CSVDatumWriter;
import org.wso2.datamapper.engine.output.writers.JSONDatumWriter;
import org.wso2.datamapper.engine.output.writers.XMLDatumWriter;

/**
 * Factory class for writer classes
 */
public class OutputWriterFactory {

    public static GenericDatumWriter getDatumWriter(String dataType) {
        if (dataType.equals(InputOutputDataTypes.DataType.CSV.toString())) {
            return new CSVDatumWriter();
        } else if (dataType
                .equals(InputOutputDataTypes.DataType.XML.toString())) {
            return new XMLDatumWriter();
        } else if (dataType.equals(InputOutputDataTypes.DataType.JSON
                .toString())) {
            return new JSONDatumWriter();
        } else {
            return new XMLDatumWriter();
        }
    }
}
