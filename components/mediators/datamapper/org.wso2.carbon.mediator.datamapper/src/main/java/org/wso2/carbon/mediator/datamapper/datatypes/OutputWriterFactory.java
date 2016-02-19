package org.wso2.carbon.mediator.datamapper.datatypes;

/**
 * Factory class for writer classes
 * 
 */
public class OutputWriterFactory {

	public static OutputWriter getWriter(String dataType) {
		if (dataType.equals(InputOutputDataTypes.DataType.CSV.toString())) {
			return new CSVWriter();
		} else if (dataType
				.equals(InputOutputDataTypes.DataType.XML.toString())) {
			return new XMLWriter();
		} else if (dataType.equals(InputOutputDataTypes.DataType.JSON
				.toString())) {
			return new JSONWriter();

		} else {
			return new JSONWriter();
		}
		

	}
}
