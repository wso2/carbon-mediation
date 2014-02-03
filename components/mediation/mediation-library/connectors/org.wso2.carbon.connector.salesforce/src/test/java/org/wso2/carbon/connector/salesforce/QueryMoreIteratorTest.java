package org.wso2.carbon.connector.salesforce;

import java.util.Collection;
import java.util.Iterator;
import java.util.Stack;

import javax.xml.namespace.QName;

import org.apache.axiom.om.OMAbstractFactory;
import org.apache.axiom.om.OMElement;
import org.apache.axiom.soap.SOAPEnvelope;
import org.apache.axiom.soap.SOAPFactory;
import org.apache.axis2.AxisFault;
import org.apache.axis2.context.ConfigurationContext;
import org.apache.axis2.engine.AxisConfiguration;
import org.apache.synapse.MessageContext;
import org.apache.synapse.SynapseConstants;
import org.apache.synapse.config.SynapseConfigUtils;
import org.apache.synapse.config.SynapseConfiguration;
import org.apache.synapse.core.axis2.Axis2MessageContext;
import org.apache.synapse.core.axis2.Axis2SynapseEnvironment;
import org.apache.synapse.mediators.template.TemplateContext;
import org.wso2.carbon.connector.salesforce.QueryMoreIterator;

import junit.framework.TestCase;

public class QueryMoreIteratorTest extends TestCase {

	private static final String TEST_TEMPLATE = "test123";
	private static MessageContext testCtx;
	
	protected void setUp() throws Exception {
		super.setUp();
        SynapseConfiguration synCfg = new SynapseConfiguration();
        AxisConfiguration config = new AxisConfiguration();
        testCtx = new Axis2MessageContext(new org.apache.axis2.context.MessageContext(),
            synCfg, new Axis2SynapseEnvironment(new ConfigurationContext(config), synCfg));
        ((Axis2MessageContext)testCtx).getAxis2MessageContext().setConfigurationContext(new ConfigurationContext(config));
        SOAPEnvelope envelope = OMAbstractFactory.getSOAP11Factory().getDefaultEnvelope();
        envelope.getBody().addChild(createOMElement("<queryResponse>" +
                "<result>" +
                "<done>false</done><queryLocator>01g9000000MVzggAAD-200</queryLocator>" +
                "<size>370</size>" +
                "</result>" +
                "</queryResponse>"));	       
        testCtx.setEnvelope(envelope);		
	}

	protected void tearDown() throws Exception {
		super.tearDown();
		testCtx = null;
	}

	public static void testConnect() throws AxisFault{
		
		org.apache.axis2.context.MessageContext axis2Ctx = new org.apache.axis2.context.MessageContext();
		SOAPFactory fac = OMAbstractFactory.getSOAP11Factory();
		org.apache.axiom.soap.SOAPEnvelope envelope = fac.getDefaultEnvelope();
		axis2Ctx.setEnvelope(envelope);
		Collection<String> collection = new java.util.ArrayList<String>();

		TemplateContext context = new TemplateContext(TEST_TEMPLATE, collection);
		Stack<TemplateContext> stack = new Stack<TemplateContext>();
		stack.add(context);				

		testCtx.setProperty(SynapseConstants.SYNAPSE__FUNCTION__STACK, stack);	
	
		testCtx.setProperty("salesforce.query.queryLocator", "01g9000000MVzggAAD-200");
		testCtx.setProperty("salesforce.query.recordSize", "370");
		
		QueryMoreIterator queryMoreIterator = new QueryMoreIterator();
		queryMoreIterator.connect(testCtx);
				
		Iterator iIteratorElements = testCtx.getEnvelope().getBody().getChildrenWithLocalName("queryResponse");		
		if(iIteratorElements.hasNext()){			
			OMElement element = (OMElement)iIteratorElements.next();
			iIteratorElements = element.getChildrenWithLocalName("iterators");
		}
		if(iIteratorElements.hasNext()){			
			OMElement element = (OMElement)iIteratorElements.next();
			iIteratorElements = element.getChildrenWithLocalName("iterator");
		}	
		if(iIteratorElements.hasNext()){			
			OMElement element = (OMElement)iIteratorElements.next();
			iIteratorElements = element.getChildrenWithLocalName("iterator");
			assertTrue(true);
		}else{
			assertTrue(false);
		}		
	}	
	
    private OMElement createOMElement(String xml) {
        return SynapseConfigUtils.stringToOM(xml);
    }
	
}
