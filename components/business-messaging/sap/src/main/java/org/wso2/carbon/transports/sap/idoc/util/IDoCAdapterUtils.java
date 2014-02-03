/*
 * Copyright WSO2, Inc. (http://wso2.com)
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.wso2.carbon.transports.sap.idoc.util;

import org.apache.axiom.om.OMElement;
import org.apache.axiom.om.OMFactory;
import org.apache.axiom.om.OMAbstractFactory;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import javax.xml.namespace.QName;
import java.util.UUID;
import java.util.Iterator;

/**
 * <code> IDoCAdapterUtils </code> provides some utility method for SAP adapter code
 */
public class IDoCAdapterUtils {

    private static Log log = LogFactory.getLog(IDoCAdapterUtils.class);

    private static final QName IDOC_Q = new QName("IDOC");

    private static final QName ARCKEY_Q = new QName("ARCKEY");

    private static final QName EDI_DC40_Q = new QName("EDI_DC40");

    protected static final OMFactory fac = OMAbstractFactory.getOMFactory();

    /**
     * This method will stamp a ARCKEY into the control segment of the IDoC
     * see http://help.sap.com/saphelp_nw04/helpdata/en/13/95244269625633e10000000a155106/content.htm
     * for more information
     * @param element The idoc element 
     * @param msgId the message ID of the message 
     */
    public static void stampArcKey(OMElement element, String msgId) {
        String arcKey;
        boolean isStampArcKey = false;

        // msgId can be null if we disable addressing or use REST
        // in that case generate a UUID for the ARCKEY
        if (msgId == null) {
            arcKey = generateUUID();
        } else {
            arcKey = msgId;
        }
        Iterator itr = element.getChildrenWithName(IDOC_Q);
        while (itr.hasNext()){
            OMElement idocElement = (OMElement) itr.next();
            OMElement controlElement = idocElement.getFirstChildWithName(EDI_DC40_Q);
            OMElement arcKeyElement = controlElement.getFirstChildWithName(ARCKEY_Q);

            // there is no ARCKEY, so stamp a one
            if (arcKeyElement == null || arcKeyElement.getText().equals("")) {
                isStampArcKey = true;
                OMElement newArcKeyEle = fac.createOMElement(ARCKEY_Q);
                newArcKeyEle.setText(arcKey);
                controlElement.addChild(newArcKeyEle);
            }
        }
        if(log.isDebugEnabled() && isStampArcKey){
            log.debug("The ARCKEY ' "  + arcKey + " ', stamped into the control segment");
        }
    }

    public static String getProperty(String name){
        return System.getProperty(name);
    }

    private static String generateUUID() {
        return UUID.randomUUID().toString();
    }
}
