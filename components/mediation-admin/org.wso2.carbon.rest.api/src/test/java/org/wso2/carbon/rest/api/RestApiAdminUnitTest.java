/*
 *  Copyright (c) 2021, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
 *
 *  WSO2 Inc. licenses this file to you under the Apache License,
 *  Version 2.0 (the "License"); you may not use this file except
 *  in compliance with the License.
 *  You may obtain a copy of the License at
 *
 *    http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied.  See the License for the
 * specific language governing permissions and limitations
 * under the License.
 */
package org.wso2.carbon.rest.api;

import com.google.gson.JsonElement;
import com.google.gson.JsonParser;
import junit.framework.Assert;
import junit.framework.TestCase;
import org.apache.axiom.om.OMElement;
import org.apache.axiom.om.util.AXIOMUtil;
import org.apache.commons.io.IOUtils;
import org.apache.synapse.api.API;
import org.apache.synapse.api.Resource;
import org.apache.synapse.config.xml.rest.APIFactory;

import org.wso2.carbon.mediation.commons.rest.api.swagger.APIGenException;
import org.wso2.carbon.rest.api.service.RestApiAdmin;
import org.yaml.snakeyaml.Yaml;

import java.io.IOException;
import java.io.InputStream;
import java.util.Map;

import javax.xml.stream.XMLStreamException;

/**
 * This class will test the functions provided by RestAPIAdmin.
 */
public class RestApiAdminUnitTest extends TestCase {

    RestApiAdmin restApiAdmin = new RestApiAdmin();
    ClassLoader classLoader = getClass().getClassLoader();
    JsonParser jsonParser = new JsonParser();

    private String readResourceFile(String fileName) throws IOException {
        InputStream inputStream = classLoader.getResourceAsStream(fileName);
        return IOUtils.toString(inputStream);
    }

    public void testAPIFromSwagger() throws IOException, APIException, XMLStreamException {
        String fileContent = readResourceFile("TestSwagger.yaml");
        String result = restApiAdmin.generateAPIFromSwaggerByFormat(fileContent, false);
        OMElement omElement = AXIOMUtil.stringToOM(result);
        API api = APIFactory.createAPI(omElement);
        assertEquals("Mismatch in the API context", "/first", api.getContext());
        assertEquals("Resource count mismatch", 3, api.getResources().length);
    }

    public void testAPIFromSwaggerWithTemplatedServers() throws IOException, APIException, XMLStreamException {
        String fileContent = readResourceFile("TestSwagger2.yaml");
        String result = restApiAdmin.generateAPIFromSwaggerByFormat(fileContent, false);
        OMElement omElement = AXIOMUtil.stringToOM(result);
        API api = APIFactory.createAPI(omElement);
        assertEquals("Mismatch in the API context", "/v2/bla/test1/hello", api.getContext());
    }

    // Test OpenApi definition generating from an API with context version strategy.
    public void testSwaggerFromAPIWithContextVersion() throws IOException, XMLStreamException, APIException {
        String fileContent = readResourceFile("firstApi.xml");
        OMElement omElement = AXIOMUtil.stringToOM(fileContent);
        API api = APIFactory.createAPI(omElement);
        String swagger = restApiAdmin.generateSwaggerFromSynapseAPI(api);
        JsonElement result = jsonParser.parse(swagger);
        fileContent = readResourceFile("SwaggerWithContext.json");
        JsonElement expected = jsonParser.parse(fileContent);
        Assert.assertEquals("Mismatch in the received swagger", expected.toString(), result.toString());
    }

    // Test OpenApi definition generating from an API with URL based version strategy.
    public void testSwaggerFromAPIWithUrlVersion() throws IOException, XMLStreamException, APIException {
        String fileContent = readResourceFile("secondApi.xml");
        OMElement omElement = AXIOMUtil.stringToOM(fileContent);
        API api = APIFactory.createAPI(omElement);
        String swagger = restApiAdmin.generateSwaggerFromSynapseAPI(api);
        JsonElement result = jsonParser.parse(swagger);
        fileContent = readResourceFile("SwaggerWithUrl.json");
        JsonElement expected = jsonParser.parse(fileContent);
        Assert.assertEquals("Mismatch in the received swagger", expected.toString(), result.toString());
    }

    public void testGenerateUpdatedAPIFromSwagger() throws IOException, XMLStreamException, APIException {
        String fileContent = readResourceFile("firstApi.xml");
        OMElement omElement = AXIOMUtil.stringToOM(fileContent);
        API oldApi = APIFactory.createAPI(omElement);
        fileContent = readResourceFile("ChangeApiSwagger.json");
        String newApiStr = restApiAdmin.generateUpdatedAPIFromSwaggerForAPI(fileContent, oldApi);
        omElement = AXIOMUtil.stringToOM(newApiStr);
        API newApi = APIFactory.createAPI(omElement);
        Assert.assertEquals("Version update failed", newApi.getVersion(), "1.2.4");
        Resource newResource = null;
        for (Resource resource : newApi.getResources()) {
            if (resource.getDispatcherHelper() != null) {
                if ("/newPath".equals(resource.getDispatcherHelper().getString())) {
                    newResource = resource;
                }
            }
        }
        Assert.assertNotNull("Could not find the new resource added from swagger", newResource);
    }

    public void testGenerateUpdatedSwaggerFromApi() throws IOException, XMLStreamException, APIGenException {

        String fileContent = readResourceFile("testAllApiChanged.xml");
        OMElement omElement = AXIOMUtil.stringToOM(fileContent);
        API newApi = APIFactory.createAPI(omElement);
        fileContent = readResourceFile("TestAllApiSwagger.yaml");
        String newApiStr = restApiAdmin.generateUpdatedSwaggerFromAPI(fileContent, false, true, newApi);
        String expectedResult = readResourceFile("TestAllApiChangedSwagger.json");
        JsonElement result = jsonParser.parse(newApiStr);
        JsonElement expected = jsonParser.parse(expectedResult);
        Assert.assertEquals("Did not received the expected swagger", expected.toString(), result.toString());
    }

    public void testUpdateNameInSwaggerJSON() throws IOException, APIException {
        String fileContent = readResourceFile("ChangeApiSwagger.json");
        String changed = restApiAdmin.updateNameInSwagger("firstApiChanged", fileContent);
        JsonElement jsonElement = jsonParser.parse(changed);
        String newName = jsonElement.getAsJsonObject().get("info").getAsJsonObject().get("title").getAsString();
        Assert.assertEquals("Swagger name not changed", "firstApiChanged", newName);
    }

    public void testUpdateNameInSwaggerYAML() throws IOException, APIException {
        String fileContent = readResourceFile("TestAllApiSwagger.yaml");
        String changed = restApiAdmin.updateNameInSwagger("testAllApiChanged", fileContent);
        Yaml yaml = new Yaml();
        Map<String, Object> obj = yaml.load(changed);
        Map<String, Object> infoMap = (Map<String, Object>) obj.get("info");
        Assert.assertEquals("Swagger title not changed",infoMap.get("title"),"testAllApiChanged");
    }

    public void testAPIFromSwaggerWithRelativeUrl() throws IOException, APIException, XMLStreamException {
        String fileContent = readResourceFile("TestSwaggerRelativeUrl.yaml");
        String result = restApiAdmin.generateAPIFromSwaggerByFormat(fileContent, false);
        OMElement omElement = AXIOMUtil.stringToOM(result);
        API api = APIFactory.createAPI(omElement);
        assertEquals("Mismatch in the API context", "/firstRelative", api.getContext());
        assertEquals("Resource count mismatch", 3, api.getResources().length);
    }

    public void testGenerateUpdatedSwaggerFromApiWithRelativeUrl() throws IOException, XMLStreamException,
            APIGenException {
        String fileContent = readResourceFile("testAllApiChanged.xml");
        OMElement omElement = AXIOMUtil.stringToOM(fileContent);
        API newApi = APIFactory.createAPI(omElement);
        fileContent = readResourceFile("TestAllApiSwaggerRelativeUrl.yaml");
        String newApiStr = restApiAdmin.generateUpdatedSwaggerFromAPI(fileContent, false, true, newApi);
        String expectedResult = readResourceFile("TestAllApiChangedSwaggerRelativePath.json");
        JsonElement result = jsonParser.parse(newApiStr);
        JsonElement expected = jsonParser.parse(expectedResult);
        Assert.assertEquals("Did not received the expected swagger", expected.toString(), result.toString());
    }
}
