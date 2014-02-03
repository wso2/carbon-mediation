package org.wso2.carbon.connector.salesforce;

import java.util.Collection;
import java.util.Iterator;
import java.util.Stack;

import junit.framework.TestCase;

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
import org.apache.synapse.mediators.Value;
import org.apache.synapse.mediators.template.TemplateContext;
import org.wso2.carbon.connector.core.AbstractConnector;

public class InjectSobjectsTest extends TestCase {

	private static final String TEST_TEMPLATE = "test123";
	private static MessageContext testCtx;
	
	protected void setUp() throws Exception {
		super.setUp();
        SynapseConfiguration synCfg = new SynapseConfiguration();
        AxisConfiguration config = new AxisConfiguration();
        testCtx = new Axis2MessageContext(new org.apache.axis2.context.MessageContext(),
            synCfg, new Axis2SynapseEnvironment(new ConfigurationContext(config), synCfg));
        ((Axis2MessageContext)testCtx).getAxis2MessageContext().setConfigurationContext(new ConfigurationContext(config));		
	}

	protected void tearDown() throws Exception {
		super.tearDown();
		testCtx = null;
	}

	public static void testCreateConnect() throws AxisFault{
        SOAPEnvelope envelope = OMAbstractFactory.getSOAP11Factory().getDefaultEnvelope();
        envelope.getBody().addChild(createOMElement("<create/>"));	       
        testCtx.setEnvelope(envelope);		
		testConnect("create", SalesforceUtil.SALESFORCE_SOBJECTS, new SetupCreateSobjects());
	}
	
	public static void testUpdateConnect() throws AxisFault{
        SOAPEnvelope envelope = OMAbstractFactory.getSOAP11Factory().getDefaultEnvelope();
        envelope.getBody().addChild(createOMElement("<update/>"));	       
        testCtx.setEnvelope(envelope);		
		testConnect("update", SalesforceUtil.SALESFORCE_SOBJECTS, new SetupUpdateSobjects());
	}
	
	public static void testUpsertConnect() throws AxisFault{
        SOAPEnvelope envelope = OMAbstractFactory.getSOAP11Factory().getDefaultEnvelope();
        envelope.getBody().addChild(createOMElement("<upsert/>"));	       
        testCtx.setEnvelope(envelope);		
		testConnect("upsert", SalesforceUtil.SALESFORCE_SOBJECTS, new SetupUpsertSobjects());
	}
	
	public static void testConnect(String strOperation, String strParamName, AbstractConnector connector) throws AxisFault{
		
		org.apache.axis2.context.MessageContext axis2Ctx = new org.apache.axis2.context.MessageContext();
		SOAPFactory fac = OMAbstractFactory.getSOAP11Factory();
		org.apache.axiom.soap.SOAPEnvelope envelope = fac.getDefaultEnvelope();
		axis2Ctx.setEnvelope(envelope);
		Collection<String> collection = new java.util.ArrayList<String>();
		collection.add(strParamName);
		testCtx.setProperty(TEST_TEMPLATE + ":" + strParamName, 
					new Value("<sfdc:sObjects xmlns:sfdc='sfdc' type='Account'><sfdc:sObject><sfdc:Name>name01</sfdc:Name></sfdc:sObject></sfdc:sObjects>"));
		TemplateContext context = new TemplateContext(TEST_TEMPLATE, collection);
		Stack<TemplateContext> stack = new Stack<TemplateContext>();
		stack.add(context);				
		context.setupParams(testCtx);
		
		testCtx.setProperty(SynapseConstants.SYNAPSE__FUNCTION__STACK, stack);	
		try{
			connector.connect(testCtx);
		}catch(Exception e){
			assertTrue(false);
		}
					
		Iterator<OMElement> iIteratorElements = testCtx.getEnvelope().getBody().getChildrenWithLocalName(strOperation);	
		OMElement element = iIteratorElements.next();
		iIteratorElements = element.getChildren();
		if(iIteratorElements.hasNext()){			
			assertTrue(true);
		}else{
			assertTrue(false);
		}		
		
	}	
	
    private static OMElement createOMElement(String xml) {
        return SynapseConfigUtils.stringToOM(xml);
    }
	
}

