/*
 *  Copyright (c) 2008, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
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

package org.wso2.carbon.rest.api.ui.client;

import org.apache.axiom.om.OMElement;
import org.apache.axiom.om.OMNode;
import org.apache.axiom.om.OMXMLBuilderFactory;
import org.apache.axiom.om.impl.llom.OMElementImpl;
import org.apache.axiom.om.xpath.AXIOMXPath;
import org.apache.axis2.AxisFault;
import org.apache.axis2.client.Options;
import org.apache.axis2.client.ServiceClient;
import org.apache.axis2.context.ConfigurationContext;
import org.apache.axis2.transport.http.HTTPConstants;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.wso2.carbon.rest.api.stub.RestApiAdminStub;
import org.wso2.carbon.rest.api.stub.types.carbon.APIData;
import org.wso2.carbon.rest.api.stub.types.carbon.ResourceData;

import java.io.FileInputStream;
import java.io.InputStream;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Locale;
import java.util.ResourceBundle;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class RestApiAdminClient {

	private static final Log log = LogFactory.getLog(RestApiAdminClient.class);

	private static final String BUNDLE = "org.wso2.carbon.rest.api.ui.i18n.Resources";
	private static String CONF_LOCATION = "conf.location";

	private ResourceBundle bundle;

	private RestApiAdminStub stub;

	public RestApiAdminClient(ConfigurationContext configCtx, String backendServerURL,
            String cookie, Locale locale) throws AxisFault {

		bundle = ResourceBundle.getBundle(BUNDLE, locale);
        String serviceURL = backendServerURL + "RestApiAdmin";
        stub = new RestApiAdminStub(configCtx, serviceURL);
        ServiceClient client = stub._getServiceClient();
        Options options = client.getOptions();
        options.setTimeOutInMilliSeconds(15 * 60 * 1000);
        options.setProperty(HTTPConstants.SO_TIMEOUT, 15 * 60 * 1000);
        options.setProperty(HTTPConstants.CONNECTION_TIMEOUT, 15 * 60 * 1000);
        options.setManageSession(true);
        options.setProperty(org.apache.axis2.transport.http.HTTPConstants.COOKIE_STRING, cookie);
	}

	public String[] getApiNames() throws AxisFault {
		try {
            String [] result = stub.getApiNames();
            if (result == null || result.length == 0 || result[0] == null) {
              return null;
            }
			return stub.getApiNames();
		} catch (Exception e) {
			handleException(bundle.getString("unable.to.get.declared.apis"), e);
		}
		return null;
	}

	public APIData getApiByName(String apiName) throws AxisFault {
		try {
			return stub.getApiByName(apiName);
		} catch (Exception e) {
			handleException(bundle.getString("failed.to.find.api"), e);
		}
		return null;
	}

	public APIData[] getAPIsForListing(int pageNumber, int itemsPerPage) throws AxisFault {
		try {
			return stub.getAPIsForListing(pageNumber, itemsPerPage);
		} catch (Exception e) {
			handleException(bundle.getString("failed.to.retrieve.apis"), e);
		}
		return null;
	}

    private boolean isApiSatisfySearchString(String searchString,String apiName) {
        if (searchString != null) {
            String regex = searchString.toLowerCase().
                    replace("..?", ".?").replace("..*", ".*").
                    replaceAll("\\?", ".?").replaceAll("\\*", ".*?");

            Pattern pattern = Pattern.compile(regex);
            Matcher matcher = pattern.matcher(apiName.toLowerCase());

            return regex.trim().length() == 0 || matcher.find();
        }
        return false;
    }

    public APIData[] getAPIsForSearchListing(int pageNumber, int itemsPerPage, String searchText) throws AxisFault {

        ArrayList<org.wso2.carbon.rest.api.stub.types.carbon.APIData> apis = new ArrayList<org.wso2.carbon.rest.api.stub.types.carbon.APIData>();
        try {
            APIData[] tempAPI =  stub.getAPIsForListing(pageNumber, stub.getAPICount());
            if (tempAPI == null || tempAPI.length == 0 || tempAPI[0] == null) {
                return null;
            }

            for (org.wso2.carbon.rest.api.stub.types.carbon.APIData info : tempAPI) {

                if (isApiSatisfySearchString(searchText, info.getName())) {
                    org.wso2.carbon.rest.api.stub.types.carbon.APIData api =
							new org.wso2.carbon.rest.api.stub.types.carbon.APIData();
                    api.setName(info.getName());
                    api.setContext(info.getContext());
                    api.setFileName(info.getFileName());
                    api.setHost(info.getHost());
                    api.setPort(info.getPort());
                    api.setResources(info.getResources());
                    apis.add(api);
                }
            }
        } catch (Exception e) {
            handleException(bundle.getString("failed.to.retrieve.apis"), e);
        }
        if (apis.size() > 0) {
            return apis.toArray(new APIData[apis.size()]);
        }
        return null;
    }



	public int getAPICount() throws AxisFault {
		try {
			return stub.getAPICount();
		} catch (Exception e) {
			handleException(bundle.getString("failed.to.get.api.count"), e);
		}
		return 0;
	}

	public void deleteApi(String apiName) throws AxisFault {
		try {
			stub.deleteApi(apiName);
		} catch (Exception e) {
			handleException(bundle.getString("could.not.delete.api"), e);
		}
	}

	public void addApi(APIData apiData) throws AxisFault {
		try {
			stub.addApi(apiData);
		} catch (Exception e) {
			handleException(bundle.getString("could.not.add.api"), e);
		}
	}

	public void updateApi(APIData apiData) throws AxisFault {
		try {
			String apiName = apiData.getName();
			if (apiData.getVersion() != null && !apiData.getVersion().isEmpty()) {
				apiName = apiName + ":v" + apiData.getVersion();
			}
			stub.updateApi(apiName, apiData);
		} catch (Exception e) {
			handleException(bundle.getString("could.not.update.api"), e);
		}
	}

	public String[] getDefinedSequences() throws AxisFault {
		try {
			String[] sequences = stub.getSequences();
			if(sequences != null && sequences.length != 0){
				Arrays.sort(sequences);
			}
			return sequences;
		} catch (Exception e) {
			handleException(bundle.getString("could.not.get.sequences"), e);
		}
		return null;
	}

	public String getApiSource(APIData apiData) throws AxisFault {
		try {
			return stub.getApiSource(apiData);
		} catch (Exception e) {
			handleException(bundle.getString("could.not.get.api.source"), e);
		}
		return null;
	}

    public String getResourceSource(ResourceData resourceData) throws AxisFault {
        try {
            return stub.getResourceSource(resourceData);
        } catch (Exception e) {
            handleException(bundle.getString("could.not.get.resource.source"), e);
        }
        return null;
    }

	public void addApiFromString(String apiData) throws AxisFault {
		try {
			stub.addApiFromString(apiData);
		} catch (AxisFault af) {
			handleException(af.getMessage(), af);
		} catch (Exception e) {
			handleException(bundle.getString("could.not.add.api"), e);
		}
	}

	public void updateApiFromString(String apiName, String apiData) throws AxisFault {
		try {
			stub.updateApiFromString(apiName, apiData);
		} catch (AxisFault af) {
			handleException(af.getMessage(), af);
		} catch (Exception e) {
			handleException(bundle.getString("could.not.update.api"), e);
		}
	}

	private String ReadWSDLPrefix() throws AxisFault {
		try {
			String confPath = System.getProperty(CONF_LOCATION);
			if (confPath == null) {
				confPath = Paths.get("repository", "conf").toString();
			}
			InputStream in = new FileInputStream(Paths.get(confPath, "axis2", "axis2.xml").toString());
			OMElement results = OMXMLBuilderFactory.createOMBuilder(in).getDocumentElement();

			AXIOMXPath xpathExpression =
					new AXIOMXPath("/axisconfig/transportReceiver/parameter[@name='WSDLEPRPrefix']");
			List nodeList = (List) xpathExpression.selectNodes(results);

			if (!nodeList.isEmpty()) {
				OMNode value = (OMNode) nodeList.get(0);
				String server = ((OMElementImpl) value).getText();

				if (server.contains("http") || server.contains("https")) {
					return server;
				}
			}
		} catch (Exception e) {
			handleException(bundle.getString("failed.to.read.wsdl.prefix"), e);
		}
		return null;
	}

	public String getServerContext() throws AxisFault {
		try {
			String returnValue = ReadWSDLPrefix();
			if (returnValue != null && returnValue.isEmpty()) {
				return returnValue;
			} else {
				return stub.getServerContext();
			}
		} catch (Exception e) {
			handleException(bundle.getString("failed.to.get.servercontext"), e);
		}
		return null;
	}

	private void handleException(String msg, Exception e) throws AxisFault {
		log.error(msg, e);
		throw new AxisFault(msg, e);
	}

	public void deleteSelectedApi(String[] apiNames) throws AxisFault {
		try {
			stub.deleteSelectedApi(apiNames);
		} catch (Exception e) {
			handleException(bundle.getString("could.not.selected.delete.api"), e);
		}
	}

	public void deleteAllApi() throws AxisFault {
		try {
			stub.deleteAllApi();
		} catch (Exception e) {
			handleException(bundle.getString("could.not.All.delete.api"), e);
		}
	}

	public String enableStatistics(String apiName) throws AxisFault {
		try {
			stub.enableStatistics(apiName);
		} catch (Exception e) {
			handleException(bundle.getString("could.not.enable.api.statistics"), e);
		}
		return null;
	}

	public String disableStatistics(String apiName) throws AxisFault {
		try {
			stub.disableStatistics(apiName);
		} catch (Exception e) {
			handleException(bundle.getString("could.not.disable.api.statistics"), e);
		}
		return null;
	}

	public String enableTracing(String apiName) throws AxisFault {
		try {
			stub.enableTracing(apiName);
		} catch (Exception e) {
			handleException(bundle.getString("could.not.enable.api.statistics"), e);
		}
		return null;
	}

	public String disableTracing(String apiName) throws AxisFault {
		try {
			stub.disableTracing(apiName);
		} catch (Exception e) {
			handleException(bundle.getString("could.not.disable.api.statistics"), e);
		}
		return null;
	}
}
