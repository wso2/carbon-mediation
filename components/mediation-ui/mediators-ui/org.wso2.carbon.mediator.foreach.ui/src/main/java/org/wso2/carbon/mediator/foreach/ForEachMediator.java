package org.wso2.carbon.mediator.foreach;

import org.apache.axiom.om.OMAttribute;
import org.apache.axiom.om.OMElement;
import org.apache.synapse.config.xml.SynapsePath;
import org.apache.synapse.config.xml.SynapsePathFactory;
import org.apache.synapse.config.xml.SynapsePathSerializer;
import org.jaxen.JaxenException;
import org.wso2.carbon.mediator.service.MediatorException;
import org.wso2.carbon.mediator.service.ui.AbstractListMediator;
import org.wso2.carbon.mediator.service.ui.Mediator;
import org.wso2.carbon.mediator.target.TargetMediator;

public class ForEachMediator extends AbstractListMediator {

	private SynapsePath expression = null;

	public ForEachMediator() {

	}

	public SynapsePath getExpression() {
		return expression;
	}

	public void setExpression(SynapsePath expression) {
		this.expression = expression;
	}

	public String getTagLocalName() {
		return "foreach";
	}

	public OMElement serialize(OMElement parent) {
		OMElement itrElem = fac.createOMElement("foreach", synNS);
		saveTracingState(itrElem, this);

		if (expression != null) {
			SynapsePathSerializer.serializePath(expression, itrElem, "expression");
		} else {
			throw new MediatorException("Missing expression of the ForEach which is required.");
		}

		serializeChildren(itrElem, getList());
		
		// attach the serialized element to the parent if specified
		if (parent != null) {
			parent.addChild(itrElem);
		}

		return itrElem;
	}

	public void build(OMElement elem) {

		processAuditStatus(this, elem);

		OMAttribute expression = elem.getAttribute(ATT_EXPRN);
		if (expression != null) {
			try {
				this.expression = SynapsePathFactory.getSynapsePath(elem, ATT_EXPRN);
			} catch (JaxenException e) {
				throw new MediatorException("Unable to build the ForEach Mediator. " +
				                            "Invalid XPATH or JsonPath " +
				                            expression.getAttributeValue());
			}
		} else {
			throw new MediatorException(
			                            "XPATH or JsonPath expression is required "
			                                    + "for a ForEach Mediator under the \"expression\" attribute");
		}
		OMElement targetElement = elem.getFirstChildWithName(TARGET_Q);
		if (targetElement != null) {
			addChildren(elem, this);
		} else {
			throw new MediatorException(
			                            "Target for an foreach mediator is required :: missing target");
		}

	}

}
