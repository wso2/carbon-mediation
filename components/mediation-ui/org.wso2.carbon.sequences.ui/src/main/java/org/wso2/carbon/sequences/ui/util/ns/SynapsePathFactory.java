package org.wso2.carbon.sequences.ui.util.ns;

import java.util.Iterator;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpSession;

import org.apache.axiom.om.xpath.AXIOMXPath;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.synapse.config.xml.SynapsePath;
import org.apache.synapse.util.xpath.SynapseJsonPath;
import org.apache.synapse.util.xpath.SynapseXPath;
import org.jaxen.JaxenException;
import org.wso2.carbon.sequences.ui.util.SequenceEditorHelper;

public class SynapsePathFactory {

	private static final Log log = LogFactory.getLog(SynapsePathFactory.class);

	private final static SynapsePathFactory instance = new SynapsePathFactory();

	public static SynapsePathFactory getInstance() {
		return instance;
	}

	private SynapsePathFactory() {
	}

	public SynapsePath createSynapsePath(String id, String source, HttpSession httpSession) {
		try {
			if (!assertIDNotEmpty(id) || !assertSourceNotEmpty(source)) {
				return null;
			}

			if (source.trim().startsWith("json-eval(")) {

				String json = source.trim().substring(10, (source.trim().length() - 1));

				SynapseJsonPath jsonPath = new SynapseJsonPath(json);
				return jsonPath;

			} else {
				SynapseXPath xPath = new SynapseXPath(source.trim());
				addNameSpaces(xPath, id, httpSession);
				return xPath;
			}

		} catch (JaxenException e) {
			String msg = "Error creating a path from text : " + source;
			throw new RuntimeException(msg, e);
		}
	}

	public SynapsePath createSynapsePath(String id, HttpServletRequest request,
	                                     HttpSession httpSession) {
		return createSynapsePath(id, request.getParameter(id), httpSession);
	}

	private AXIOMXPath addNameSpaces(AXIOMXPath xPath, String id, HttpSession httpSession) {

		NameSpacesInformationRepository repository =
		                                             (NameSpacesInformationRepository) httpSession.getAttribute(NameSpacesInformationRepository.NAMESPACES_INFORMATION_REPOSITORY);
		if (repository == null) {
			return xPath;
		}

		NameSpacesInformation information =
		                                    repository.getNameSpacesInformation(SequenceEditorHelper.getEditingMediatorPosition(httpSession),
		                                                                        id);
		if (information == null) {
			return xPath;
		}

		if (log.isDebugEnabled()) {
			log.debug("Getting NameSpaces :" + information + " for id :" + id);
		}

		Iterator<String> iterator = information.getPrefixes();
		while (iterator.hasNext()) {
			String prefix = iterator.next();
			String nsURI = information.getNameSpaceURI(prefix);
			try {
				xPath.addNamespace(prefix, nsURI);
			} catch (JaxenException je) {
				String msg =
				             "Error adding declared name space with prefix : " + prefix +
				                     "and uri : " + nsURI + " to the xPath : " + xPath;
				throw new RuntimeException(msg, je);
			}
		}
		information.removeAllNameSpaces();
		return xPath;
	}

	private static boolean assertIDNotEmpty(String id) {
		if (id == null || "".equals(id)) {
			if (log.isDebugEnabled()) {
				log.debug("Provided id is empty or null ,returning a null as path");
			}
			return false;
		}
		return true;
	}

	private static boolean assertSourceNotEmpty(String source) {
		if (source == null || "".equals(source)) {
			if (log.isDebugEnabled()) {
				log.debug("Provided source is empty or null ,returning a null as path");
			}
			return false;
		}
		return true;
	}

}
