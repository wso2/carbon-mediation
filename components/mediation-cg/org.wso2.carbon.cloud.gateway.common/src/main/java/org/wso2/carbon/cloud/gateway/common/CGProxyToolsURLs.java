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
package org.wso2.carbon.cloud.gateway.common;

/**
 * Various tools URLs such as the CG Service URL, CG Service WSDLs URLs etc..
 */
public class CGProxyToolsURLs {
    private String tryItURL;
    private String wsdl2URL;
    private String wsdl11URL;
    private String[] eprArray;

    public String getWsdl2URL() {
        return wsdl2URL;
    }

    public void setWsdl2URL(String wsdl2URL) {
        this.wsdl2URL = wsdl2URL;
    }

    public String getTryItURL() {
        return tryItURL;
    }

    public void setTryItURL(String tryItURL) {
        this.tryItURL = tryItURL;
    }

    public String getWsdl11URL() {
        return wsdl11URL;
    }

    public void setWsdl11URL(String wsdl11URL) {
        this.wsdl11URL = wsdl11URL;
    }

    public String[] getEprArray() {
        return eprArray;
    }

    public void setEprArray(String[] eprArray) {
        this.eprArray = eprArray;
    }
}
