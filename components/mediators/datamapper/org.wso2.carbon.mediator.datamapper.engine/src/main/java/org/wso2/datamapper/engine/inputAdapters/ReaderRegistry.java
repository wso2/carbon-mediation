/*
 * Copyright 2005,2014 WSO2, Inc. http://www.wso2.org
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */


package org.wso2.datamapper.engine.inputAdapters;

import java.util.HashMap;
import java.util.Map;

/**
 * A registry of readers.
 */
public class ReaderRegistry {
	/*
	 * This class should re-write with Apache Avro aware readers (stranded DatumReader )   
	 */
	
	/**
	 * Singleton instance.
	 */
	private static ReaderRegistry singleton;
	
	
	/**
	 * reader map.
	 */
	private Map<String, Class<?>> readerMap;
	
	/**
	 * 
	 */
	private ReaderRegistry() {
		readerMap = new HashMap<String, Class<?>>();
		
		// FIXME : use java service provider interface rather than hard-coding class names/ importing classes
		readerMap.put(CsvInputReader.getType(), CsvInputReader.class);
		readerMap.put(XmlInputReader.getType(), XmlInputReader.class);
		readerMap.put(JsonInputReader.getType(), JsonInputReader.class);
	}
	
	/**
	 * @return singleton instance.
	 */
	public static ReaderRegistry getInstance() {
		if (null == singleton) {
			singleton = new ReaderRegistry();
		}
		return singleton;
	}
	
	@SuppressWarnings("unchecked")
	public Class<InputDataReaderAdapter> get(String mediaType){
		Class<InputDataReaderAdapter> reader = null;
		if(readerMap.containsKey(mediaType)){
			reader = (Class<InputDataReaderAdapter>) readerMap.get(mediaType);
		} else {
			throw new RuntimeException("No reader found for " + mediaType);
		}
		//FIXME: use proper error handling 
		return reader;
	}

}
