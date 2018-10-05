package org.wso2.carbon.mediator.datamapper.engine.core.mapper;

import org.w3c.dom.Document;
import org.w3c.dom.Node;
import org.xml.sax.InputSource;
import org.xml.sax.SAXException;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.StringReader;
import java.nio.charset.StandardCharsets;
import java.util.HashMap;
import java.util.Map;

import static org.wso2.carbon.mediator.datamapper.engine.utils.DataMapperEngineConstants
        .EMPTY_STRING;
import static org.wso2.carbon.mediator.datamapper.engine.utils.DataMapperEngineConstants
        .FIRST_ELEMENT_OF_THE_INPUT;
import static org.wso2.carbon.mediator.datamapper.engine.utils.DataMapperEngineConstants
        .PARAMETER_FILE_ROOT;
import static org.wso2.carbon.mediator.datamapper.engine.utils.DataMapperEngineConstants
        .PROPERTY_SEPERATOR;
import static org.wso2.carbon.mediator.datamapper.engine.utils.DataMapperEngineConstants.RUN_TIME_PROPERTIES;
import static org.wso2.carbon.mediator.datamapper.engine.utils.DataMapperEngineConstants
        .NOT_XSLT_COMPATIBLE;

public class XSLTMappingResource {

    private final Map<String,String> runTimeProperties;
    private String name;
    private String content;
    private boolean notXSLTCompatible;

    public XSLTMappingResource(String content) throws SAXException,
            IOException,
            ParserConfigurationException {
        this.content = content;
        this.runTimeProperties = new HashMap<>();
        Document document = getDocument();
        notXSLTCompatible = processConfigurationDetails(document);
        if(notXSLTCompatible){
            this.content = null;
        }
    }

    InputStream getInputStream(){
        return new ByteArrayInputStream(content.getBytes(StandardCharsets.UTF_8));
    }

    InputSource getInputSource(){
        return new InputSource(new StringReader(content));
    }

    private Document getDocument() throws SAXException, IOException,
            ParserConfigurationException {
        DocumentBuilderFactory documentBuilderFactory = DocumentBuilderFactory.newInstance();
        DocumentBuilder documentBuilder = documentBuilderFactory.newDocumentBuilder();
        return documentBuilder.parse(getInputSource());
    }

    private boolean processConfigurationDetails(Document document){
        Node rootNode = document.getElementsByTagName(PARAMETER_FILE_ROOT).item(0);
        for(int j=0;j<rootNode.getAttributes().getLength();j++){
            Node propertyNode = rootNode.getAttributes().item(j);
            if(propertyNode.getNodeName().equals(RUN_TIME_PROPERTIES)){
                String runTimePropertyString = propertyNode.getNodeValue();
                if(!EMPTY_STRING.equals(runTimePropertyString)) {
                    String[] properties = runTimePropertyString.split(PROPERTY_SEPERATOR);
                    int currentIndex = 0;
                    while (currentIndex < properties.length) {
                        runTimeProperties.put(properties[currentIndex], properties[currentIndex + 1]);
                        currentIndex += 2;
                    }
                }
                break;
            }else if(propertyNode.getNodeName().equals(NOT_XSLT_COMPATIBLE)){
                return true;
            }else if(propertyNode.getNodeName().equals(FIRST_ELEMENT_OF_THE_INPUT)){
                this.name = propertyNode.getNodeValue();
            }
        }
        if(this.name==null){
            return true;
        }
        return false;

    }

    public String getName() {
        return name;
    }

    Map<String, String> getRunTimeProperties() {
        return runTimeProperties;
    }

    public boolean isNotXSLTCompatible() {
        return notXSLTCompatible;
    }
}
