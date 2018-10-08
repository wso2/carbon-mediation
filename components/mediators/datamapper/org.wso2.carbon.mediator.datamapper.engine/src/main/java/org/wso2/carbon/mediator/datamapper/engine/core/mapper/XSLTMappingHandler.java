package org.wso2.carbon.mediator.datamapper.engine.core.mapper;

import javax.xml.transform.Result;
import javax.xml.transform.Source;
import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerException;
import javax.xml.transform.TransformerFactory;
import java.io.InputStream;
import java.io.StringWriter;
import java.util.Map;

import net.sf.saxon.TransformerFactoryImpl;

public class XSLTMappingHandler {

    private final XSLTMappingResource xsltMappingResource;
    private Source xsltSource;
    private Transformer transformer;

    public XSLTMappingHandler(XSLTMappingResource xsltMappingResource) throws TransformerException {
        this.xsltMappingResource = xsltMappingResource;
        xsltSource = new javax.xml.transform.stream.StreamSource(xsltMappingResource.getInputStream());
        TransformerFactory transFact = new TransformerFactoryImpl();
        transformer = transFact.newTransformer(xsltSource);
    }

    public String doMap(Map<String,Object> properties, InputStream inputXML) throws
            TransformerException{

        setParameters(properties);
        Source xmlSource = new javax.xml.transform.stream.StreamSource(inputXML);
        StringWriter sw = new StringWriter();
        Result result = new javax.xml.transform.stream.StreamResult(sw);
        transformer.transform(xmlSource, result);
        return sw.toString();

    }

    private void setParameters(Map<String,Object> properties){
        transformer.clearParameters();
        for(String key : properties.keySet())
            transformer.setParameter(key,properties.get(key));
    }

}
