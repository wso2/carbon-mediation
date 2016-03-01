package org.wso2.datamapper.engine.inputAdapters;

import org.wso2.datamapper.engine.datatypes.InputOutputDataTypes;

/**
 * Factory class for writer classes
 */
public class InputReaderFactory {

    public static InputDataReaderAdapter getReader(String dataType) {

        InputDataReaderAdapter inputReader = null;
        if (dataType != null) {
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
                    inputReader = new XmlInputReader();
            }
        } else {
            inputReader = new XmlInputReader();
        }
        return inputReader;


    }
}
