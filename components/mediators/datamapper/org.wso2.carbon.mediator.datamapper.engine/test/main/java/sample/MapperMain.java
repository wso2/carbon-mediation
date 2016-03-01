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

package sample;

import org.apache.avro.generic.GenericRecord;
import org.apache.avro.io.DatumWriter;
import org.apache.avro.io.Encoder;
import org.wso2.datamapper.engine.core.MappingHandler;
import org.wso2.datamapper.engine.core.MappingResourceLoader;
import org.wso2.datamapper.engine.inputAdapters.InputDataReaderAdapter;
import org.wso2.datamapper.engine.outputAdapters.DummyEncoder;
import org.wso2.datamapper.engine.outputAdapters.WriterRegistry;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.InputStream;

public class MapperMain {

    public static void main(String[] args) throws Exception {

        InputStream inStream = new FileInputStream(new File("./resources/test1/employeePayload.xml"));
        InputStream inputSchema = new FileInputStream(new File("./resources/test1/employee.avsc"));
        InputStream outputSchema = new FileInputStream(new File("./resources/test1/engineer.avsc"));
        InputStream config = new FileInputStream(new File("./resources/test1/employeeToEngineer.js"));

        //Contexts are anti-pattern and no need getters/setters for access static class, just used for code readability
        MappingContext context = new MappingContext();
        context.setInputStream(inStream);
        context.setConfig(config);
        context.setInputSchema(inputSchema);
        context.setOutputSchema(outputSchema);

        //CSV : text/csv
        //XML : application/xml
        //XML : application/json

        context.setInputType("application/xml");
        context.setOutputType("application/xml");

        String output = map(context);

        //Print output
        System.out.println("\n\n-- output -- \n");
        System.out.println(output.toString());

        inStream.close();

    }

    private static String map(MappingContext c) throws Exception {
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        Encoder encoder = new DummyEncoder(baos);

        MappingResourceLoader configModel = new MappingResourceLoader(c.getInputSchema(), c.getOutputSchema(),
                c.getConfig());
        InputDataReaderAdapter reader = ReaderRegistry.getInstance().get(c.getInputType()).newInstance();

        GenericRecord result = MappingHandler.doMap(c.getInputStream(), configModel, reader);

        DatumWriter<GenericRecord> writer = WriterRegistry.getInstance().get(c.getOutputType()).newInstance();
        writer.setSchema(result.getSchema());

        writer.write(result, encoder);
        encoder.flush();

        return baos.toString();
    }

    static class MappingContext {

        public InputStream getInputStream() {
            return inputStream;
        }

        public void setInputStream(InputStream inputStream) {
            this.inputStream = inputStream;
        }

        public InputStream getConfig() {
            return config;
        }

        public void setConfig(InputStream config) {
            this.config = config;
        }

        public InputStream getInputSchema() {
            return inputSchema;
        }

        public void setInputSchema(InputStream inputSchema) {
            this.inputSchema = inputSchema;
        }

        public InputStream getOutputSchema() {
            return outputSchema;
        }

        public void setOutputSchema(InputStream outputSchema) {
            this.outputSchema = outputSchema;
        }

        public String getInputType() {
            return inputType;
        }

        public void setInputType(String inputType) {
            this.inputType = inputType;
        }

        public String getOutputType() {
            return outputType;
        }

        public void setOutputType(String outputType) {
            this.outputType = outputType;
        }

        private InputStream inputStream;
        private InputStream config;
        private InputStream inputSchema;
        private InputStream outputSchema;
        private String inputType;
        private String outputType;
    }

}
