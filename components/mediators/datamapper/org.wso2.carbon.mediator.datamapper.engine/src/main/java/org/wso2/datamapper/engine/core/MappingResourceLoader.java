/*
 * Copyright 2005,2013 WSO2, Inc. http://www.wso2.org
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
package org.wso2.datamapper.engine.core;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.apache.avro.Schema;
import org.apache.avro.Schema.Parser;
import org.mozilla.javascript.Context;
import org.mozilla.javascript.Function;
import org.mozilla.javascript.Scriptable;

public class MappingResourceLoader {
	
	private Schema inputAvroSchema;
	private Schema outputAvroSchema;
	private InputStream mappingConfig;
	private String inputRootelement;
	private String outputRootelement;
	private Context context;
	private Scriptable scope;
	private MappingResourceLoader.JSFunction function;
 
	
	/**
	 * 
	 * @param inputSchema - Respective output Avro schema as a a stream of bytes
	 * @param outPutSchema -Respective output Avro schema as a a stream of bytes
	 * @param mappingConfig-Mapping configuration file as a stream of bytes
	 * @throws IOException - when input errors, If there any parser exception occur while passing above schemas method
	 *  will this exception
	 * 
	 */
	public MappingResourceLoader(InputStream inputSchema, InputStream outPutSchema,
			InputStream mappingConfig) throws IOException {

		this.inputAvroSchema = getAvroSchema(inputSchema);
		this.outputAvroSchema = getAvroSchema(outPutSchema);
		this.inputRootelement = inputAvroSchema.getName();
		this.outputRootelement = outputAvroSchema.getName();
		this.mappingConfig = mappingConfig;
		this.function = getFunction(mappingConfig);
	}

	public Schema getInputSchema() {
		return inputAvroSchema;
	}

	public Schema getOutputSchema() {
		return outputAvroSchema;
	}

	public InputStream getMappingConfig() {
		return mappingConfig;
	}

	public String getInputRootelement() {
		return inputRootelement;
	}

	public String getOutputRootelement() {
		return outputRootelement;
	}
	public Context getContext() {
		return context;
	}

	public Scriptable getScope() {
		return scope;
	}

	public Function getFunction() throws MappingResourceLoader.JSException{
		if(function!=null){
			initScriptEnviroment();
			context.evaluateString(scope, function.getFunctionBody(), "	", 1, null);
			return (Function) scope.get(function.getFunctioName(), scope);
		}else{
			throw new MappingResourceLoader.JSException("JS function not in a correct format");
		}
	}

	private Schema getAvroSchema(InputStream schema) throws IOException{
		return new Parser().parse(schema);
	}
	
	/**
	 * need to create java script function by passing the configuration file 
	 * Since this function going to execute every time when message hit the mapping backend
	 * so this function save in the resource model
	 * @param mappingConfig
	 * @return
	 * @throws IOException
	 */
	private MappingResourceLoader.JSFunction getFunction(InputStream mappingConfig) throws IOException {

		BufferedReader configReader = new BufferedReader(new InputStreamReader(mappingConfig));
       //need to identify the main method of the configuration because that method going to execute in engine		
		Pattern functionIdPattern = Pattern.compile("(function )(map_(L|S)_" + inputRootelement
				+ "_(L|S)_" + outputRootelement + ")");
		String fnName = null;
		String configLine = "";
		StringBuilder configScriptbuilder = new StringBuilder();
		while ((configLine = configReader.readLine()) != null) {
			configScriptbuilder.append(configLine);
			Matcher matcher = functionIdPattern.matcher(configLine);
			if (matcher.find()) {
				fnName = matcher.group(2);
			}
		}
		
		if (fnName != null) {
		    MappingResourceLoader.JSFunction jsfunction = new MappingResourceLoader.JSFunction(fnName,configScriptbuilder.toString());
			return jsfunction;
		
		}
		return null;
	}

	/**
	 * Before executing a script, an instance of Context must be created
	 * and associated with the thread that will be executing the script
	 */
	private void initScriptEnviroment() {
		context = Context.enter();
		context.setOptimizationLevel(-1);
		scope = context.initStandardObjects();
	}
	
  class JSFunction{
	  
	private String functioName;
	private  String functionBody;
	  
	  public JSFunction(String name,String body){
		  this.setFunctioName(name);
		  this.setFunctionBody(body);
	  }

	public String getFunctioName() {
		return functioName;
	}

	public void setFunctioName(String functioName) {
		this.functioName = functioName;
	}

	public String getFunctionBody() {
		return functionBody;
	}

	public void setFunctionBody(String functionBody) {
		this.functionBody = functionBody;
	}
	  
	  
  }
  class JSException extends Exception {
	  
	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;
		private String message = null;
	 
	    public JSException() {
	        super();
	    }
	 
	    public JSException(String message) {
	        super(message);
	        this.message = message;
	    }
	 
	    public JSException(Throwable cause) {
	        super(cause);
	    }
	 
	    @Override
	    public String toString() {
	        return message;
	    }
	 
	    @Override
	    public String getMessage() {
	        return message;
	    }
	}
}
