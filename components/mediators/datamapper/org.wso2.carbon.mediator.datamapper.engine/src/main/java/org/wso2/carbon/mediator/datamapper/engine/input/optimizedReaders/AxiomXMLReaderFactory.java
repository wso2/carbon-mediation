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
package org.wso2.carbon.mediator.datamapper.engine.input.optimizedReaders;

import org.wso2.carbon.mediator.datamapper.engine.input.readers.JSONReader;
import org.wso2.carbon.mediator.datamapper.engine.input.readers.Reader;
import org.wso2.carbon.mediator.datamapper.engine.input.readers.XMLReader;
import org.wso2.carbon.mediator.datamapper.engine.utils.InputOutputDataType;

import java.io.IOException;

/**
 * This class is a factory class to get {@link Reader} needed by the data mapper engine
 */
public class AxiomXMLReaderFactory {

    public static AxiomXMLReader getReader(InputOutputDataType inputType) throws IOException {
        switch (inputType) {
            case XML:
                return new AxiomXMLReader();
//            case JSON:
//                return new JSONReader();
            default:
                throw new IllegalArgumentException("Input Reader for type " + inputType + " is not implemented.");
        }
    }
}
