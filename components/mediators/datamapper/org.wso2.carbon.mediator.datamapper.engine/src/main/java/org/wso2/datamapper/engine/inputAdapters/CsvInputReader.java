package org.wso2.datamapper.engine.inputAdapters;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.Iterator;
import java.util.List;

import org.apache.avro.Schema;
import org.apache.avro.Schema.Field;
import org.apache.avro.generic.GenericData;
import org.apache.avro.generic.GenericRecord;
import org.apache.axiom.om.OMElement;
import org.apache.axiom.om.OMXMLBuilderFactory;
import org.apache.axiom.om.OMXMLParserWrapper;

import au.com.bytecode.opencsv.CSVReader;
/**
 * 
 * Reading input CSV data and construct the Avro record
 *
 */
public class CsvInputReader implements InputDataReaderAdapter {

	
	private InputStreamReader csvReader; 
	private OMElement textEement; 
	/**
	 * @param msg - Soap Envelop
	 * @throws IOException
	 */
	public void setInputMsg(InputStream in) {
		 OMXMLParserWrapper builder = OMXMLBuilderFactory.createOMBuilder(in);
		 OMElement documentElement = builder.getDocumentElement(); ;
		 OMElement body = documentElement.getFirstElement().getFirstElement();
		 getTextElement(body);
		 String text = textEement.getText();
		 InputStream is = new ByteArrayInputStream(text.getBytes());
		 csvReader =  new InputStreamReader(is);	
	}
	
   // CSV dataset has been wrapped using <text> element
	private  OMElement getTextElement(OMElement element){
		 @SuppressWarnings("unchecked")
		Iterator<OMElement> iter = element.getChildElements();
		 while(iter.hasNext()){
			 OMElement childElement = iter.next();
			  if("text".equals(childElement.getLocalName())){
				  textEement = childElement;
				  return textEement;
			  }else {
				 if (getTextElement(childElement)!=null){
					 break;
				 }
			  }
		 }
		 return null;
	}
	
	//TODO This is not the real implementation
	public GenericRecord getInputRecord(Schema input) {
		GenericRecord ParentRecord = new GenericData.Record(input);	
		List<Field> fields = input.getFields();
		Field field =fields.get(0);// there should be a one  object
		//10 is the default size of the recordArray but this is a auto resizable array , size will be increased when pass default value
		GenericData.Array<GenericRecord> recordArray = new GenericData.Array<GenericRecord>(10, field.schema());
		CSVReader reader = new CSVReader(csvReader);
	    String [] nextLine;
		    try {
		    	Schema elementType = field.schema().getElementType();
		    	List<Field> fields2 = elementType.getFields();	    	
		    	String[] fisrstLine  = reader.readNext();
		    	GenericRecord headerRecord = new GenericData.Record(elementType);
		    	//FIXME I know this check is bad , do proper implementation in later
		    	for (int i = 0; i < fields2.size(); i++) {	
		    		//matching the first line with given headers in the input Avro schema
		    		if(!fields2.get(i).name().equals(fields2.get(i).name())){
		    			headerRecord.put(fields2.get(i).name(), fisrstLine[i]);
		               if(i == fields2.size()-1){
		            	   recordArray.add(headerRecord);
		               }
		    		}else{
		    			break;
		    		}	
				}    	
				while ((nextLine = reader.readNext()) != null) {		
					GenericRecord record = new GenericData.Record(elementType);
					for (int i = 0; i < fields2.size(); i++) {	
						record.put(fields2.get(i).name(), nextLine[i]);
					}
					recordArray.add(record);
				}
				  ParentRecord.put(field.name(), recordArray);
				  reader.close();
			} catch (IOException e) {
				// TODO: log unrecognized data set
			}
 
		return ParentRecord;
	}

	public static String getType() {
		return "text/csv";
	}
}
