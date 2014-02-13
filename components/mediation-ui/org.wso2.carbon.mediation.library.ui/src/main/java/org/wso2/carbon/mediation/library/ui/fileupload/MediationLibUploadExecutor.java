/*
 * Copyright 2005-2007 WSO2, Inc. (http://wso2.com)
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

package org.wso2.carbon.mediation.library.ui.fileupload;

import org.wso2.carbon.utils.ServerConstants;
import org.wso2.carbon.utils.FileItemData;
import org.wso2.carbon.CarbonConstants;
import org.wso2.carbon.CarbonException;
import org.wso2.carbon.ui.transports.fileupload.AbstractFileUploadExecutor;
import org.wso2.carbon.ui.CarbonUIMessage;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Map;

public class MediationLibUploadExecutor extends AbstractFileUploadExecutor {

    public boolean execute(HttpServletRequest request,
                           HttpServletResponse response) throws CarbonException, IOException {
        return uploadArtifacts(request, response, "dropins", new String[]{"zip"}, "Axis2");
    }

    protected boolean uploadArtifacts(HttpServletRequest request,
                                      HttpServletResponse response,
                                      String uploadDirName,
                                      String[] extensions,
                                      String utilityString)
            throws IOException {

        response.setContentType("text/html; charset=utf-8");

        String webContext = (String) request.getAttribute(CarbonConstants.WEB_CONTEXT);
        String serverURL = (String) request.getAttribute(CarbonConstants.SERVER_URL);
        String cookie = (String) request.getAttribute(ServerConstants.ADMIN_SERVICE_COOKIE);

        String msg;
        Map<String, ArrayList<FileItemData>> fileItemsMap = getFileItemsMap();
        if (fileItemsMap == null || fileItemsMap.isEmpty()) {
            msg = "File uploading failed. No files are specified";
            log.error(msg);
            CarbonUIMessage.sendCarbonUIMessage(msg, CarbonUIMessage.ERROR, request,
                    response, getContextRoot(request) + "/" + webContext + "/mediation_library/app_upload.jsp");
        }

        //Creating the stub to call the back-end service
        MediationLibraryUploadClient uploaderClient= new MediationLibraryUploadClient(
                configurationContext, serverURL + "MediationLibraryUploader", cookie);

        try {
            for (Object o : fileItemsMap.keySet()) {
                String fieldName = (String) o;
                FileItemData fileItemData = fileItemsMap.get(fieldName).get(0);
                String fileName = getFileName(fileItemData.getFileItem().getName());
                uploaderClient.addUploadedFileItem(fileItemData.getDataHandler(), fileName, "zip");
            }

            //Uploading files to back end service
            uploaderClient.uploadFileItems();
            msg = "Your Application has been uploaded successfully. Please refresh this page in a" +
                    " while to see the status of the new Application.";
            CarbonUIMessage.sendCarbonUIMessage(msg, CarbonUIMessage.INFO, request,
                    response, getContextRoot(request) + "/" + webContext + "/mediation_library/index.jsp" );
            return true;
        } catch (Exception e) {
            msg = "File upload failed.";
            log.error(msg, e);
            CarbonUIMessage.sendCarbonUIMessage(msg, CarbonUIMessage.ERROR, request,
                    response, getContextRoot(request) + "/" + webContext + "/mediation_library/app_upload.jsp");
        }
        return false;
    }
}

