package org.wso2.carbon.connector.salesforce;

import java.util.Collection;
import java.util.Stack;

import org.apache.axiom.om.OMAbstractFactory;
import org.apache.axiom.soap.SOAPFactory;
import org.apache.axis2.AxisFault;
import org.apache.synapse.MessageContext;
import org.apache.synapse.SynapseConstants;
import org.apache.synapse.mediators.Value;
import org.apache.synapse.core.axis2.Axis2MessageContext;
import org.apache.synapse.mediators.template.TemplateContext;
import org.wso2.carbon.connector.salesforce.SalesforceUtil;
import org.wso2.carbon.connector.salesforce.SetupCRUDParams;

import junit.framework.TestCase;

public class SetupCRUDParamsTest extends TestCase {

	private static final String TEST_TEMPLATE = "test123";

	protected void setUp() throws Exception {
		super.setUp();
	}

	protected void tearDown() throws Exception {
		super.tearDown();
	}

	public static void testSetupCRUDDefaultParams() throws AxisFault {

		org.apache.axis2.context.MessageContext axis2Ctx =
		                                                   new org.apache.axis2.context.MessageContext();
		SOAPFactory fac = OMAbstractFactory.getSOAP11Factory();
		org.apache.axiom.soap.SOAPEnvelope envelope = fac.getDefaultEnvelope();
		axis2Ctx.setEnvelope(envelope);
		MessageContext synCtx = new Axis2MessageContext(axis2Ctx, null, null);
		Collection<String> collection = new java.util.ArrayList<String>();

		TemplateContext context = new TemplateContext(TEST_TEMPLATE, collection);
		Stack<TemplateContext> stack = new Stack<TemplateContext>();
		stack.add(context);

		context.setupParams(synCtx);

		synCtx.setProperty(SynapseConstants.SYNAPSE__FUNCTION__STACK, stack);

		SetupCRUDParams crudParams = new SetupCRUDParams();
		crudParams.connect(synCtx);

		assertEquals("1",
		             synCtx.getProperty(SalesforceUtil.SALESFORCE_CRUD_PREFIX +
		                                SalesforceUtil.SALESFORCE_CRUD_ALLORNONE));
		assertEquals("0",
		             synCtx.getProperty(SalesforceUtil.SALESFORCE_CRUD_PREFIX +
		                                SalesforceUtil.SALESFORCE_CRUD_ALLOWFIELDTRUNCATE));
		assertEquals("Id",
		             synCtx.getProperty(SalesforceUtil.SALESFORCE_CRUD_PREFIX +
		                                SalesforceUtil.SALESFORCE_EXTERNALID));

	}

	public static void testSetupCRUDParams() throws AxisFault {

		org.apache.axis2.context.MessageContext axis2Ctx =
		                                                   new org.apache.axis2.context.MessageContext();
		SOAPFactory fac = OMAbstractFactory.getSOAP11Factory();
		org.apache.axiom.soap.SOAPEnvelope envelope = fac.getDefaultEnvelope();
		axis2Ctx.setEnvelope(envelope);
		MessageContext synCtx = new Axis2MessageContext(axis2Ctx, null, null);
		Collection<String> collection = new java.util.ArrayList<String>();
		collection.add(SalesforceUtil.SALESFORCE_CRUD_ALLORNONE);
		collection.add(SalesforceUtil.SALESFORCE_CRUD_ALLOWFIELDTRUNCATE);
		collection.add(SalesforceUtil.SALESFORCE_EXTERNALID);
		synCtx.setProperty(TEST_TEMPLATE + ":" + SalesforceUtil.SALESFORCE_CRUD_ALLORNONE,
		                   new Value("0"));
		synCtx.setProperty(TEST_TEMPLATE + ":" + SalesforceUtil.SALESFORCE_CRUD_ALLOWFIELDTRUNCATE,
		                   new Value("1"));
		synCtx.setProperty(TEST_TEMPLATE + ":" + SalesforceUtil.SALESFORCE_EXTERNALID,
		                   new Value("Name"));

		TemplateContext context = new TemplateContext(TEST_TEMPLATE, collection);
		Stack<TemplateContext> stack = new Stack<TemplateContext>();
		stack.add(context);

		context.setupParams(synCtx);

		synCtx.setProperty(SynapseConstants.SYNAPSE__FUNCTION__STACK, stack);

		SetupCRUDParams crudParams = new SetupCRUDParams();
		crudParams.connect(synCtx);

		assertEquals("0",
		             synCtx.getProperty(SalesforceUtil.SALESFORCE_CRUD_PREFIX +
		                                SalesforceUtil.SALESFORCE_CRUD_ALLORNONE));
		assertEquals("1",
		             synCtx.getProperty(SalesforceUtil.SALESFORCE_CRUD_PREFIX +
		                                SalesforceUtil.SALESFORCE_CRUD_ALLOWFIELDTRUNCATE));
		assertEquals("Name",
		             synCtx.getProperty(SalesforceUtil.SALESFORCE_CRUD_PREFIX +
		                                SalesforceUtil.SALESFORCE_EXTERNALID));

	}

}
