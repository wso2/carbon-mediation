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


package org.wso2.datamapper.engine.outputAdapters;

import org.apache.avro.generic.GenericRecord;
import org.apache.avro.io.DatumWriter;

import java.util.HashMap;
import java.util.Map;

/**
 * A registry of writer.
 */
public class WriterRegistry {

    /**
     * Singleton instance.
     */
    private static WriterRegistry singleton;


    /**
     * writer map.
     */
    private Map<String, Class<? extends DatumWriter<GenericRecord>>> writerMap;

    /**
     *
     */
    private WriterRegistry() {
        writerMap = new HashMap<String, Class<? extends DatumWriter<GenericRecord>>>();

        // FIXME : use java service provider interface rather than hard-coding class names/ importing classes
        writerMap.put("CSV", CSVDatumWriter.class);
        writerMap.put("XML", XMLDatumWriter.class);
        writerMap.put("JSON", JSONDatumWriter.class);
    }

    /**
     * @return singleton instance.
     */
    public static WriterRegistry getInstance() {
        if (null == singleton) {
            singleton = new WriterRegistry();
        }
        return singleton;
    }

    @SuppressWarnings("unchecked")
    public Class<DatumWriter<GenericRecord>> get(String mediaType) {
        Class<DatumWriter<GenericRecord>> writer = null;
        if (writerMap.containsKey(mediaType)) {
            writer = (Class<DatumWriter<GenericRecord>>) writerMap.get(mediaType);
        } else {
            throw new RuntimeException("No writer found for " + mediaType);
        }
        //FIXME: use proper error handling
        return writer;
    }

}
