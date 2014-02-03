/*
 *  Copyright (c) 2012, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
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

package org.wso2.carbon.endpoint.ui.util;

import org.wso2.carbon.endpoint.ui.endpoints.Endpoint;
import org.wso2.carbon.endpoint.ui.endpoints.EndpointService;
import org.wso2.carbon.endpoint.ui.endpoints.EndpointStore;
import org.wso2.carbon.endpoint.ui.endpoints.ListEndpoint;
import javax.servlet.ServletConfig;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpSession;
import java.rmi.RemoteException;
import java.util.Locale;
public class ListEndpointDesignerHelper {

    public static Endpoint getEditingEndpoint(HttpServletRequest request, HttpSession session) {
        String childEndpointPosition = request.getParameter("childEndpointID");
        ListEndpoint endpoint = getEditingListEndpoint(session);
        if (childEndpointPosition != null && !"null".equals(childEndpointPosition)) {
            Endpoint editingChildEndpoint = ListEndpointDesignerHelper.getEndpointAt(
                    endpoint, childEndpointPosition.substring(14));
            session.setAttribute("editingchildEndpoint", editingChildEndpoint);
            session.setAttribute("editingchildEndpointPosition", childEndpointPosition);
            return editingChildEndpoint;
        } else {
            return (Endpoint) session.getAttribute("editingchildEndpoint");
        }
    }

    public static Endpoint getEndpointAt(ListEndpoint listEndpoint, String position) {
        int index;
        if (position != null && listEndpoint != null) {
            int i = position.indexOf(".");
            if (i == -1) {
                if ("00".equals(position)) {
                    return listEndpoint;
                } else {
                    int pos = Integer.parseInt(position);
                    if (pos < listEndpoint.getList().size()) {
                        return listEndpoint.getList().get(pos);
                    } else {
                        return listEndpoint;
                    }
                }
            } else {
                index = Integer.parseInt(position.substring(0, i));
                return getEndpointAt((ListEndpoint)
                                             listEndpoint.getList().get(index), position.substring(i + 1));
            }
        }
        return null;
    }

    public static ListEndpoint getEditingListEndpoint(HttpSession session) {
        return (ListEndpoint) session.getAttribute("editingListEndpoint");
    }

    public static String getEndpointHTML(Endpoint endpoint, boolean last, String position,
                                         ServletConfig config, Locale locale) {

        EndpointService endpointService = EndpointStore.getInstance().getEndpointService(endpoint.getTagLocalName());
        String endpointName = endpointService != null ? endpointService.getDisplayName() : endpoint.getTagLocalName();
        String endpointIconURL = "./images/node-normal.gif";

        String html = "<div class=\"minus-icon\" onclick=\"treeColapse(this)\"></div>";
        if (!(endpoint instanceof ListEndpoint) ||
            ((ListEndpoint) endpoint).getList().isEmpty()) {
            html = "<div class=\"dot-icon\"></div>";
        }

        html += "<div class=\"childEndpoints\" style=\"background-image: url(" + endpointIconURL
                + ") !important\" id=\"childEndpoint-" + position + "\">" +
                "<a class=\"endpointLink\" id=\"childEndpoint-" + position + "\">"
                + endpointName + "</a><div class=\"endpointToolbar\" style=\"display:none\" >";

        if (endpoint instanceof ListEndpoint) {
            html += "<div><a class=\"addChildStyle\">"
                    + "Add Child" + "</a></div>"
                    + "<div class=\"endpointSep\">&nbsp;</div>";

            html += "<div><a class=\"deleteStyle\">"
                    + "delete" + "</a></div>";

            html += "</div></div>";

            ListEndpoint listEndpoint = (ListEndpoint) endpoint;
            if (!listEndpoint.getList().isEmpty()) {
                if (last) {
                    html = "<li>" + html;
                } else {
                    html = "<li class=\"vertical-line\">" + html;
                }
                html += "<div class=\"branch-node\"></div>";
                html += "<ul class=\"child-list\">";
                int count = listEndpoint.getList().size();
                int endpointPosition = 0;
                for (Endpoint childEndpoint : listEndpoint.getList()) {
                    count--;
                    html += getEndpointHTML(childEndpoint, count == 0, position + "."
                                                             + endpointPosition, config, locale);
                    endpointPosition++;
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
            html += "<div><a class=\"deleteStyle\">"
                    + "delete" + "</a></div>";
            html += "</div></div>";

            if (!last) {
                html = "<li>" + html + "<div class=\"vertical-line-alone\"/>";
            } else {
                html = "<li>" + html;
            }
        }
        return html + "</li>";
    }

    public static Endpoint getNewEndpoint(String endpointName) throws RemoteException {
        EndpointStore store = EndpointStore.getInstance();
        EndpointService endpointService = store.getEndpointService(endpointName);
        if (endpointService != null) {
            return endpointService.getEndpoint();
        } else {
            throw new RuntimeException("Couldn't find the endpoint information in the " +
                                       "endpoint store for the endpoint with logical name " + endpointName);
        }
    }

    public static Endpoint removeEndpointAt(ListEndpoint listEndpoint, String position) {
        int index;
        if (position != null && listEndpoint != null) {
            int i = position.indexOf(".");
            if (i == -1) {
                if ("00".equals(position)) {
                    return null;
                } else {
                    return listEndpoint.getList().remove(Integer.parseInt(position));
                }
            } else {
                index = Integer.parseInt(position.substring(0, i));
                return removeEndpointAt((ListEndpoint)
                                                listEndpoint.getList().get(index), position.substring(i + 1));
            }
        }
        return null;
    }

    public static boolean deleteEndpointAt(String position, HttpSession session) {
        ListEndpoint listEndpoint = getEditingListEndpoint(session);
        return removeEndpointAt(listEndpoint, position.substring(14)) != null;
    }

    public static void clearSessionCache(HttpSession session) {
        session.removeAttribute("endpoint.position");
    }

}
