//package org.wso2.carbon.connector.twitter;
//
//import java.util.Collection;
//import java.util.Stack;
//
//import junit.framework.TestCase;
//
//import org.apache.axiom.om.OMAbstractFactory;
//import org.apache.axiom.soap.SOAPFactory;
//import org.apache.axis2.AxisFault;
//import org.apache.synapse.MessageContext;
//import org.apache.synapse.SynapseConstants;
//import org.apache.synapse.core.axis2.Axis2MessageContext;
//import org.apache.synapse.mediators.Value;
//import org.apache.synapse.mediators.template.TemplateContext;
//import org.wso2.carbon.connector.twitter.TwitterSearch;
//import org.wso2.carbon.connector.twitter.TwitterSearchPlaces;
//
//public class TwitterConnectorTest extends TestCase {
//
//	private static final String TEST_TEMPLATE = "test123";
//
//	protected void setUp() throws Exception {
//		super.setUp();
//	}
//
//	protected void tearDown() throws Exception {
//		super.tearDown();
//	}
//
//	/**
//	 * Method to test Twitter generic search
//	 * 
//	 * @throws AxisFault
//	 */
//	public static void testSearchTest() throws AxisFault {
//		org.apache.axis2.context.MessageContext axis2Ctx = new org.apache.axis2.context.MessageContext();
//		SOAPFactory fac = OMAbstractFactory.getSOAP11Factory();
//		org.apache.axiom.soap.SOAPEnvelope envelope = fac.getDefaultEnvelope();
//		axis2Ctx.setEnvelope(envelope);
//		MessageContext synCtx = new Axis2MessageContext(axis2Ctx, null, null);
//		Collection<String> collection = new java.util.ArrayList<String>();
//		collection.add("search");
//		synCtx.setProperty(TEST_TEMPLATE + ":" + "search", new Value("hotel"));
//		Stack<TemplateContext> stack = prepareMessageContext(synCtx, collection);
//
//		synCtx.setProperty(SynapseConstants.SYNAPSE__FUNCTION__STACK, stack);
//		TwitterSearch search = new TwitterSearch();
//		search.mediate(synCtx);
//		assertTrue(((Axis2MessageContext) synCtx).getAxis2MessageContext().getEnvelope()
//				.getFirstElement() != null);
//
//	}
//	
//	/**
//	 * Unit test to test search places functionality.
//	 * 	 
//	 * @throws AxisFault
//	 */
//	public static void testPlacesTest() throws AxisFault {
//		org.apache.axis2.context.MessageContext axis2Ctx = new org.apache.axis2.context.MessageContext();
//		SOAPFactory fac = OMAbstractFactory.getSOAP11Factory();
//		org.apache.axiom.soap.SOAPEnvelope envelope = fac.getDefaultEnvelope();
//		axis2Ctx.setEnvelope(envelope);
//		MessageContext synCtx = new Axis2MessageContext(axis2Ctx, null, null);
//		Collection<String> collection = new java.util.ArrayList<String>();
//		collection.add("latitude");
//		collection.add("longitude");
//		synCtx.setProperty(TEST_TEMPLATE + ":" + "latitude", new Value("40.71435"));
//		synCtx.setProperty(TEST_TEMPLATE + ":" + "longitude", new Value("-74.00597"));
//		Stack<TemplateContext> stack = prepareMessageContext(synCtx, collection);
//
//		synCtx.setProperty(SynapseConstants.SYNAPSE__FUNCTION__STACK, stack);
//		TwitterSearchPlaces search = new TwitterSearchPlaces();
//		search.mediate(synCtx);
//		assertTrue(((Axis2MessageContext) synCtx).getAxis2MessageContext().getEnvelope()
//				.getFirstElement() != null);
//
//	}
//
//	private static Stack<TemplateContext> prepareMessageContext(MessageContext synCtx,
//			Collection<String> collection) {
//
//		collection.add("oauth.accessTokenSecret");
//		collection.add("oauth.consumerSecret");
//		collection.add("oauth.accessToken");
//		collection.add("oauth.consumerKey");
//		TemplateContext context = new TemplateContext(TEST_TEMPLATE, collection);
//		Stack<TemplateContext> stack = new Stack<TemplateContext>();
//		stack.add(context);
//
//		synCtx.setProperty(TEST_TEMPLATE + ":" + "oauth.accessTokenSecret", new Value(
//				"vkpELc3OWK0TM0BjYcPLCn22Wm3HRliNUyx1QSxg4JI"));
//		synCtx.setProperty(TEST_TEMPLATE + ":" + "oauth.consumerSecret", new Value(
//				"EvTEzc3jj9Z1Kx58ylNfkpnuXYuCeGgKhkVkziYNMs"));
//		synCtx.setProperty(TEST_TEMPLATE + ":" + "oauth.accessToken", new Value(
//				"1114764380-JNGKRkrUFUDCHC0WdmjDurZ3wwi9BV6ysbDRYca"));
//		synCtx.setProperty(TEST_TEMPLATE + ":" + "oauth.consumerKey", new Value(
//				"6U5CNaHKh7hVSGpk1CXo6A"));
//
//		context.setupParams(synCtx);
//		return stack;
//	}
//
//}
