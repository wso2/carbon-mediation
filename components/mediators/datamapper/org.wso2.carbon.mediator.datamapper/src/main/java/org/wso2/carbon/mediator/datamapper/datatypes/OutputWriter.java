package org.wso2.carbon.mediator.datamapper.datatypes;

import java.io.IOException;

import org.apache.avro.generic.GenericRecord;
import org.apache.axiom.om.OMElement;

/**
 * Parent class for writer classes
 *
 */
public interface OutputWriter {
	
	public abstract OMElement getOutputMessage(String outputType,
											   GenericRecord result)throws IOException;

}
