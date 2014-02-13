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

import org.apache.synapse.endpoints.AbstractEndpoint;
import org.apache.synapse.mediators.MediatorProperty;
import javax.net.ssl.SSLHandshakeException;
import java.net.ConnectException;
import java.net.MalformedURLException;
import java.net.URI;
import java.net.URISyntaxException;
import java.net.URL;
import java.net.UnknownHostException;
import java.net.UnknownServiceException;
import java.util.Iterator;
import java.util.List;

public class EndpointConfigurationHelper {

    public static final String ROUNDROBIN_ALGO_CLASS_NAME =
            "org.apache.synapse.endpoints.algorithms.RoundRobin";

    public static String getValidXMLString(String originalString) {
        // replace all the correct code with invalid code
        String validXMLString = originalString.replace("&amp;", "&");
        validXMLString = validXMLString.replace("&lt;", "<");
        validXMLString = validXMLString.replace("&gt;", ">");
        validXMLString = validXMLString.replace("&quot;", "\"");

        // now replace all the invalid code with correct one
        validXMLString = validXMLString.replace("&", "&amp;");
        validXMLString = validXMLString.replace("<", "&lt;");
        validXMLString = validXMLString.replace(">", "&gt;");
        validXMLString = validXMLString.replace("\"", "&quot;");

        return validXMLString;
    }

    public static String errorCodeListBuilder(List<Integer> errCodes) {
        String errorCodes = " ";
        for (Integer errCode : errCodes) {
            errorCodes += errCode;
            errorCodes += ",";
        }
        return errorCodes.substring(0, errorCodes.length() - 1);
    }

    public static String getMappingFrom(TemplateParameterContainer container,
                                        TemplateParameterContainer.EndpointDefKey key) {
        String mapping = container.getTemplateMapping(key);
        if (mapping != null) {
            return mapping;
        }
        if (key == TemplateParameterContainer.EndpointDefKey.suspendProgressionFactor) {
            return "1.0";
        } else if (key == TemplateParameterContainer.EndpointDefKey.retryDurationOnTimeout) {
            return "0";
        } else if (key == TemplateParameterContainer.EndpointDefKey.retriesOnTimeoutBeforeSuspend) {
            return "0";
        } else {
            return "";
        }
    }

    public static String buildPropertyString(
            AbstractEndpoint ep) {

        Iterator<MediatorProperty> itr = ep.getProperties().iterator();
        String ret = "";
        while (itr.hasNext()) {
            MediatorProperty prop = itr.next();
            if (ret.equals("")) {
                ret = prop.getName() + "," + prop.getValue() + "," + prop.getScope();
            } else {
                ret = ret + "::" + prop.getName() + "," + prop.getValue() + "," + prop.getScope();
            }
        }
        return ret;
    }

    public static String testAddressURL(String url) {
        String returnValue;
        if (url != null && !url.equals("")) {
            try {
                URL conn = new URL(url);
                conn.openConnection().connect();
                testWSDLURI(url + "?wsdl");
                returnValue = "success";
            } catch (UnknownHostException e) {
                returnValue = "unknown";
            } catch (MalformedURLException e) {
                returnValue = "malformed";
            } catch (ConnectException e) {
                returnValue = "Cannot establish connection to the provided address.";
            } catch (UnknownServiceException e) {
                returnValue = "unknown_service";
            } catch (SSLHandshakeException e) {
                returnValue = "ssl_error";
            } catch (Exception e) {
                returnValue = "Cannot establish connection to the provided address";
            }

        } else {
            returnValue = "Invalid address specified.";
        }
        //we cannot validate address EP other than HTTP and HTTPS. Also check for ':' in first few chars to distinguish
        // unsupported protocol and missing protocol identifier(malformed)
        if (url != null && !url.toUpperCase().startsWith("HTTP") && !url.toUpperCase().startsWith("HTTPS")) {
            if (url.contains(":") && url.indexOf(':') < 6) {
                returnValue = "unsupported";
            }
        }
        return returnValue;
    }

    public static String testWSDLURI(String wsdlUri) {
        String returnValue = "";
        if (wsdlUri != null && !wsdlUri.equals("")) {
            try {
                URI uri = new URI(wsdlUri);
                uri.toURL().getContent();
                returnValue = "success";
            } catch (URISyntaxException e) {
                returnValue = "malformed";
            } catch (MalformedURLException e) {
                returnValue = "malformed";
            } catch (UnknownHostException e) {
                returnValue = "unknown";
            } catch (ConnectException e) {
                // A HTTP 500 may result in an error here - But the address is considered valid
                returnValue = "Cannot establish connection to the provided address";
            } catch (SSLHandshakeException e) {
                returnValue = "ssl_error";
            } catch (Exception e) {
                returnValue = "Cannot establish connection to the provided address";
            }
        }
        return returnValue;
    }

}
