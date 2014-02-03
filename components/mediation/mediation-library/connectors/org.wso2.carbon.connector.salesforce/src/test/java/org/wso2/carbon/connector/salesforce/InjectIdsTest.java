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

public class InjectIdsTest extends TestCase {

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

	public static void testDeleteConnect() throws AxisFault{
        SOAPEnvelope envelope = OMAbstractFactory.getSOAP11Factory().getDefaultEnvelope();
        envelope.getBody().addChild(createOMElement("<delete/>"));	       
        testCtx.setEnvelope(envelope);		
		testConnect("delete", SalesforceUtil.SALESFORCE_SOBJECTS, new SetupDeleteSobjects());
	}
	
	public static void testUndeleteConnect() throws AxisFault{
        SOAPEnvelope envelope = OMAbstractFactory.getSOAP11Factory().getDefaultEnvelope();
        envelope.getBody().addChild(createOMElement("<undelete/>"));	       
        testCtx.setEnvelope(envelope);		
		testConnect("undelete", SalesforceUtil.SALESFORCE_SOBJECTS, new SetupUndelete());
	}	
	
	public static void testRetriveConnect() throws AxisFault{
        SOAPEnvelope envelope = OMAbstractFactory.getSOAP11Factory().getDefaultEnvelope();
        envelope.getBody().addChild(createOMElement("<retrieve/>"));	       
        testCtx.setEnvelope(envelope);		
		testConnect("retrieve", SalesforceUtil.SALESFORCE_RETRIVE_OBJECTIDS, new SetupRetriveSobjects());
	}	

	public static void testEmptyRecycleBinConnect() throws AxisFault{
        SOAPEnvelope envelope = OMAbstractFactory.getSOAP11Factory().getDefaultEnvelope();
        envelope.getBody().addChild(createOMElement("<emptyRecycleBin/>"));	       
        testCtx.setEnvelope(envelope);		
		testConnect("emptyRecycleBin", SalesforceUtil.SALESFORCE_SOBJECTS, new SetupEmptyRecycleBin());
	}

	public static void testSendEmailMessageConnect() throws AxisFault{
        SOAPEnvelope envelope = OMAbstractFactory.getSOAP11Factory().getDefaultEnvelope();
        envelope.getBody().addChild(createOMElement("<sendEmailMessage/>"));	       
        testCtx.setEnvelope(envelope);		
		testConnect("sendEmailMessage", SalesforceUtil.SALESFORCE_EMAIL_SENDEMAILMESSAGE, new SetupSendEmailMessage());
	}
	
	public static void testConnect(String strOperation, String strParamName, AbstractConnector connector) throws AxisFault{
		
		org.apache.axis2.context.MessageContext axis2Ctx = new org.apache.axis2.context.MessageContext();
		SOAPFactory fac = OMAbstractFactory.getSOAP11Factory();
		org.apache.axiom.soap.SOAPEnvelope envelope = fac.getDefaultEnvelope();
		axis2Ctx.setEnvelope(envelope);
		Collection<String> collection = new java.util.ArrayList<String>();
		collection.add(strParamName);
		testCtx.setProperty(TEST_TEMPLATE + ":" + strParamName, 
					new Value("<sfdc:sObjects xmlns:sfdc='sfdc'><sfdc:Ids>0019000000QRCpT</sfdc:Ids></sfdc:sObjects>"));
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

