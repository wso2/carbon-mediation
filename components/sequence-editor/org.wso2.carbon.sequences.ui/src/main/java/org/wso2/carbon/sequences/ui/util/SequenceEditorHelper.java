/**
 *  Copyright (c) 2009, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
 *
 *  Licensed under the Apache License, Version 2.0 (the "License");
 *  you may not use this file except in compliance with the License.
 *  You may obtain a copy of the License at
 *
 *        http://www.apache.org/licenses/LICENSE-2.0
 *
 *  Unless required by applicable law or agreed to in writing, software
 *  distributed under the License is distributed on an "AS IS" BASIS,
 *  WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *  See the License for the specific language governing permissions and
 *  limitations under the License.
 */

package org.wso2.carbon.sequences.ui.util;

import org.apache.axiom.om.OMElement;
import org.apache.axiom.om.impl.builder.StAXOMBuilder;
import org.wso2.carbon.mediator.service.MediatorService;
import org.wso2.carbon.mediator.service.MediatorStore;
import org.wso2.carbon.mediator.service.builtin.SequenceMediator;
import org.wso2.carbon.mediator.service.ui.AbstractListMediator;
import org.wso2.carbon.mediator.service.ui.Mediator;
import org.wso2.carbon.sequences.common.SequenceEditorException;
import org.wso2.carbon.sequences.ui.client.EditorUIClient;
import org.wso2.carbon.sequences.ui.factory.EditorUIClientFactory;
import org.wso2.carbon.sequences.ui.factory.impl.SequenceEditorClientFactory;
import org.wso2.carbon.sequences.ui.util.ns.NameSpacesRegistrar;
import org.wso2.carbon.utils.xml.XMLPrettyPrinter;

import javax.servlet.ServletConfig;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpSession;
import javax.xml.namespace.QName;
import javax.xml.stream.XMLStreamException;
import java.io.ByteArrayInputStream;
import java.rmi.RemoteException;
import java.util.Locale;
import java.util.ResourceBundle;
import java.util.Set;

/**
 *
 */
public class SequenceEditorHelper {

    public static final String BUNDLE = "org.wso2.carbon.sequences.ui.i18n.Resources";

    public static OMElement parseStringToElement(String xml) throws SequenceEditorException {
        OMElement elem;
        try {
            ByteArrayInputStream bais = new ByteArrayInputStream(xml.getBytes());
            StAXOMBuilder builder = new StAXOMBuilder(bais);
            elem = builder.getDocumentElement();
        } catch (XMLStreamException e) {
            throw new SequenceEditorException("Couldn't parse the sequence source as XML", e);
        }
        return elem;
    }

    public static SequenceMediator parseStringToSequence(String sequenceXML)
            throws SequenceEditorException {
        OMElement elem = parseStringToElement(sequenceXML);
        if (elem != null) {
            MediatorService service = MediatorStore.getInstance().getMediatorService(elem);
            if (service != null) {
                SequenceMediator sequence = (SequenceMediator) service.getMediator();
                sequence.build(elem);
                return sequence;
            } else {
                return null;
            }
        } else {
            return null;
        }
    }

    public static Mediator parseStringToMediator(String mediatorXML)
            throws SequenceEditorException {
        OMElement elem = parseStringToElement(mediatorXML);
        if (elem != null) {
            MediatorService service = MediatorStore.getInstance().getMediatorService(elem);
            if (service != null) {
                Mediator m = service.getMediator();
                m.build(elem);
                return m;
            } else {
                return null;
            }
        } else {
            return null;
        }
    }

    public static String parseSequenceToString(SequenceMediator sequence) {
        OMElement elem = sequence.serialize(null);
        if (elem != null) {
            return elem.toString();
        } else {
            return null;
        }
    }

    public static String parseSequenceToPrettyfiedString(SequenceMediator sequence) {
        ByteArrayInputStream byteArrayInputStream
                = new ByteArrayInputStream(parseSequenceToString(sequence).getBytes());
        XMLPrettyPrinter printer = new XMLPrettyPrinter(byteArrayInputStream);
        return printer.xmlFormat();
    }

    public static OMElement parseAnonSequenceToOM(SequenceMediator seqMediator) {
        OMElement elem = seqMediator.serialize(null);
        elem.removeAttribute(elem.getAttribute(new QName("name")));
        return elem;
    }

    public static String parseAnonSequenceToString(SequenceMediator seqMediator,
                                                   String targetSeqName) {
        OMElement elem = parseAnonSequenceToOM(seqMediator);
        elem.setLocalName(targetSeqName + "Sequence");
        return elem.toString();
    }

    public static String parseAnonSequenceToPrettyfiedString(SequenceMediator seqMediator,
                                                             String targetSeqName) {
        OMElement elem = parseAnonSequenceToOM(seqMediator);
        elem.setLocalName(targetSeqName + "Sequence");
        ByteArrayInputStream stream = new ByteArrayInputStream(elem.toString().getBytes());
        XMLPrettyPrinter printer = new XMLPrettyPrinter(stream);
        return printer.xmlFormat();
    }
    public static String parseAnonSequenceToPrettyfiedString(SequenceMediator seqMediator) {
        OMElement elem = parseAnonSequenceToOM(seqMediator);
        ByteArrayInputStream stream = new ByteArrayInputStream(elem.toString().getBytes());
        XMLPrettyPrinter printer = new XMLPrettyPrinter(stream);
        return printer.xmlFormat();
    }

   public static Mediator getMediatorAt(AbstractListMediator sequence, String position) {
        int index;
        if (position != null && sequence != null) {
            int i = position.indexOf(".");
            if (i == -1) {
                if ("00".equals(position)) {
                    return sequence;
                } else {
                    int pos = Integer.parseInt(position);
                    if (pos < sequence.getList().size()) {
                        return sequence.getList().get(pos);
                    } else {
                        return sequence;
                    }
                }
            } else {
                index = Integer.parseInt(position.substring(0, i));
                return getMediatorAt((AbstractListMediator)
                        sequence.getList().get(index), position.substring(i + 1));
            }
        }
        return null;
    }

    public static Mediator getEditingMediator(HttpServletRequest request, HttpSession session) {
        String mediatorPosition = request.getParameter("mediatorID");
        SequenceMediator sequence = getEditingSequence(session);
        if (mediatorPosition != null && !"null".equals(mediatorPosition)) {
            Mediator editingMediator = SequenceEditorHelper.getMediatorAt(
                    sequence, mediatorPosition.substring(9));
            session.setAttribute("editingMediator", editingMediator);
            session.setAttribute("editingMediatorPosition", mediatorPosition);
            return editingMediator;
        } else {
            return (Mediator) session.getAttribute("editingMediator");
        }
    }

    public static String getEditingMediatorPosition(HttpSession session) {
        return (String) session.getAttribute("editingMediatorPosition");
    }

    public static SequenceMediator getEditingSequence(HttpSession session) {
        return (SequenceMediator) session.getAttribute("editingSequence");
    }

    public static SequenceMediator getEditingActualSequence(HttpSession session) {
        return getEditingSequence(session);
    }

    public static void removeEditingSequence (HttpSession session) {
        session.removeAttribute("editingSequence");
    }

    public static String getEditingSequenceAction(HttpSession session) {
        return session.getAttribute("editingSequenceAction") != null ?
                session.getAttribute("editingSequenceAction").toString() : "anonify";
    }

    public static void removeEditingSequenceAction(HttpSession session) {
        session.removeAttribute("editingSequenceAction");
    }

    public static boolean isIconAvailable(MediatorService mediatorService, ServletConfig config) {
        Set resourcePaths = config.getServletContext().getResourcePaths(
                "/" + mediatorService.getUIFolderName() + "-mediator/images");
        return resourcePaths != null && resourcePaths.contains("/"
                + mediatorService.getUIFolderName() + "-mediator/images/mediator-icon.gif");
    }

    public static String getMediatorHTML(Mediator mediator, boolean last, String position,
                                         ServletConfig config, Mediator before, Mediator after,
                                         Locale locale) {
        ResourceBundle bundle = ResourceBundle.getBundle(BUNDLE, locale);

        MediatorService mediatorInfo
                = MediatorStore.getInstance().getMediatorService(mediator.getTagLocalName());
        String mediatorName = mediatorInfo != null ?
                mediatorInfo.getDisplayName() : mediator.getTagLocalName();
        String url = "../"  + mediatorInfo.getUIFolderName() + "-mediator/images/mediator-icon.gif";

        String mediatorIconURL = mediatorInfo != null && isIconAvailable(mediatorInfo, config) ?
                url : "./images/node-normal.gif";
        String html = "<div class=\"minus-icon\" onclick=\"treeColapse(this)\"></div>";
        if (!(mediator instanceof AbstractListMediator) ||
                ((AbstractListMediator) mediator).getList().isEmpty()) {
            html = "<div class=\"dot-icon\"></div>";
        }
        html += "<div class=\"mediators\" style=\"background-image: url(" + mediatorIconURL
                + ") !important\" id=\"mediator-" + position + "\">" +
                "<a class=\"mediatorLink\" id=\"mediator-" + position + "\">"
                + mediatorName + "</a><div class=\"sequenceToolbar\" style=\"display:none\" >";
        if (mediator instanceof AbstractListMediator) {
            if (mediatorInfo == null || mediatorInfo.isAddChildEnabled()) {
                html += "<div><a class=\"addChildStyle\">"
                        + bundle.getString("sequence.add.child.action") + "</a></div>"
                        + "<div class=\"sequenceSep\">&nbsp;</div>";
            }
            if (mediatorInfo == null || mediatorInfo.isAddSiblingEnabled()) {
                html += "<div><a class=\"addSiblingStyle\">"
                        + bundle.getString("sequence.add.sibling.action") + "</a></div>"
                        + "<div class=\"sequenceSep\">&nbsp;</div>";
            }

            html += "<div><a class=\"deleteStyle\">"
                    + bundle.getString("sequence.delete.action") + "</a></div>";

            if (before != null && MediatorStore.getInstance().getMediatorService(
                    before.getTagLocalName()).isMovingAllowed() && mediatorInfo.isMovingAllowed()) {
                html += "<div class=\"sequenceSep\">&nbsp;</div>"
                        + "<div><a class=\"moveUpStyle\" title=\""
                        + bundle.getString("mediator.move.up") + "\"></a></div>";
            }

            if (after != null && MediatorStore.getInstance().getMediatorService(
                    after.getTagLocalName()).isMovingAllowed() && mediatorInfo.isMovingAllowed()) {
                html += "<div class=\"sequenceSep\">&nbsp;</div>"
                        + "<div><a class=\"moveDownStyle\" title=\""
                        + bundle.getString("mediator.move.down") + "\"></a></div>";
            }

            html += "</div></div>";

            AbstractListMediator listMediator = (AbstractListMediator) mediator;
            if (!listMediator.getList().isEmpty()) {
                if (last) {
                    html = "<li>" + html;
                } else {
                    html = "<li class=\"vertical-line\">" + html;
                }
                html += "<div class=\"branch-node\"></div>";
                html += "<ul class=\"child-list\">";
                int count = listMediator.getList().size();
                int mediatorPosition = 0;
                for (Mediator med : listMediator.getList()) {
                    count--;
                    Mediator beforeMed = mediatorPosition > 0 ?
                            listMediator.getList().get(mediatorPosition - 1) : null;
                    Mediator afterMed = mediatorPosition + 1 < listMediator.getList().size() ?
                            listMediator.getList().get(mediatorPosition + 1) : null;
                    html += getMediatorHTML(med, count==0, position + "."
                            + mediatorPosition, config, beforeMed, afterMed, locale);
                    mediatorPosition++;
                }
                html += "</ul>";
            } else {
                if (!last) {
                    html = "<li>" + html + "<div class=\"vertical-line-alone\"/>";
                } else {
                    html = "<li>" + html;
                }
            }
        } else {
            if (mediatorInfo == null || mediatorInfo.isAddSiblingEnabled()) {
                html += "<div><a class=\"addSiblingStyle\">"
                        + bundle.getString("sequence.add.sibling.action") + "</a></div>"
                        + "<div class=\"sequenceSep\">&nbsp;</div>";
            }

            html += "<div><a class=\"deleteStyle\">"
                    + bundle.getString("sequence.delete.action") + "</a></div>";

            if (before != null && MediatorStore.getInstance().getMediatorService(
                    before.getTagLocalName()).isMovingAllowed() && mediatorInfo.isMovingAllowed()) {
                html += "<div class=\"sequenceSep\">&nbsp;</div>"
                        + "<div><a class=\"moveUpStyle\" title=\""
                        + bundle.getString("mediator.move.up") + "\"></a></div>";
            }

            if (after != null && MediatorStore.getInstance().getMediatorService(
                    after.getTagLocalName()).isMovingAllowed() && mediatorInfo.isMovingAllowed()) {
                html += "<div class=\"sequenceSep\">&nbsp;</div>"
                        + "<div><a class=\"moveDownStyle\" title=\""
                        + bundle.getString("mediator.move.down") + "\"></a></div>";
            }

            html += "</div></div>";

            if (!last) {
                html = "<li>" + html + "<div class=\"vertical-line-alone\"/>";
            } else {
                html = "<li>" + html;
            }
        }
        return html + "</li>";
    }

    public static Mediator getNewMediator(String mediatorName) throws RemoteException {

        MediatorStore store = MediatorStore.getInstance();
        MediatorService mediatorInfo = store.getMediatorService(mediatorName);
        if (mediatorInfo != null) {
            return mediatorInfo.getMediator();
        } else {
            throw new RuntimeException("Couldn't find the mediator information in the " +
                    "mediator store for the mediator with logical name " + mediatorName);
        }
    }

    public static Mediator removeMediatorAt(AbstractListMediator sequence, String position) {
        int index;
        if (position != null && sequence != null) {
            int i = position.indexOf(".");
            if (i == -1) {
                if ("00".equals(position)) {
                    return null;
                } else {
                    return sequence.getList().remove(Integer.parseInt(position));
                }
            } else {
                index = Integer.parseInt(position.substring(0, i));
                return removeMediatorAt((AbstractListMediator)
                        sequence.getList().get(index), position.substring(i + 1));
            }
        }
        return null;
    }

    public static boolean deleteMediatorAt(String position, HttpSession session) {
        SequenceMediator sequence = getEditingSequence(session);
        return removeMediatorAt(sequence, position.substring(9)) != null;
    }

    public static void moveMediatorDown(String position, HttpSession session) {
        SequenceMediator sequence = getEditingSequence(session);
        Mediator movingMediator = removeMediatorAt(sequence, position.substring(9));
        insertMediator(sequence, movingMediator, position.substring(9), 1);
    }

    public static void moveMediatorUp(String position, HttpSession session) {
        SequenceMediator sequence = getEditingSequence(session);
        Mediator movingMediator = removeMediatorAt(sequence, position.substring(9));
        insertMediator(sequence, movingMediator, position.substring(9), -1);
    }

    public static boolean insertMediator(AbstractListMediator listMediator,
                                         Mediator mediator, String position, int after) {
        int index;
        if (position != null && listMediator != null && mediator != null) {
            int i = position.indexOf(".");
            if (i == -1) {
                if ("00".equals(position)) {
                    listMediator.addChild(mediator);
                    return false;
                } else {
                    listMediator.getList().add(Integer.parseInt(position) + after, mediator);
                    return true;
                }
            } else {
                index = Integer.parseInt(position.substring(0, i));
                return insertMediator((AbstractListMediator) listMediator.getList().get(index),
                        mediator, position.substring(i + 1), after);
            }
        }
        return false;
    }

    public static String serializeMediator(Mediator mediator) throws SequenceEditorException {
        OMElement ele = mediator.serialize(null);
        if (ele != null) {
            ByteArrayInputStream byteArrayInputStream
                = new ByteArrayInputStream(ele.toString().getBytes());
            XMLPrettyPrinter printer = new XMLPrettyPrinter(byteArrayInputStream);
            return printer.xmlFormat();
        }
        return null;
    }

    public static boolean isRedirected(HttpServletRequest request) {
        String followupAction = request.getParameter("followupAction");
        return followupAction == null || "source".equals(followupAction);
    }

    public static void clearSessionCache(HttpSession session) {
        NameSpacesRegistrar.getInstance().unRegisterNameSpaces(session);
        session.removeAttribute("throttle_policy_map");
        session.removeAttribute("mediator.position");
    }

    public static EditorUIClient getClientForEditor(ServletConfig config, HttpSession session) {
        EditorUIClientFactory factory = getFactoryFrom(session);
        return factory.createClient(config, session);
    }

    public static SequenceMediator getSequenceForEditor(HttpSession session) {
        EditorUIClientFactory factory = getFactoryFrom(session);
        return (SequenceMediator) factory.createEditingMediator();
    }

    public static String getUIMetadataForEditor(String key, HttpSession session) {
        EditorUIClientFactory factory = getFactoryFrom(session);
        String tag = factory.getUIMetaInfo().get(key);
        if (tag != null && !"".equals(tag)) {
            return tag;
        }
        return key;
    }

    private static EditorUIClientFactory getFactoryFrom(HttpSession session) {
        EditorUIClientFactory factory = (EditorUIClientFactory) session.getAttribute("editorClientFactory");
        if (factory == null) {
            factory = new SequenceEditorClientFactory();
        }
        return factory;
    }
    public static String  getForwardToFrom(HttpSession session) {
        EditorUIClientFactory factory = getFactoryFrom(session);
        String mode = getEditorMode(factory);

        if (mode == null || ( mode != null && "sequence".equals(mode))) {
            return "list_sequences.jsp";
        }
        return factory.getUIMetaInfo().get("forwardPage");
    }

    private static String getEditorMode(EditorUIClientFactory factory) {
        return factory.getUIMetaInfo().get("editorMode");
    }

    public static String getEditorMode(HttpSession session) {
        return getEditorMode(getFactoryFrom(session));
    }
}
