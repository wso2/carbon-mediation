package org.wso2.datamapper.engine.datatypes;

import org.apache.avro.generic.GenericRecord;
import org.apache.axiom.om.OMElement;

import java.io.IOException;

/**
 * Parent class for writer classes
 *
 */
public interface OutputWriter {
	
	public abstract OMElement getOutputMessage(String outputType,
											   GenericRecord result)throws IOException;

}
