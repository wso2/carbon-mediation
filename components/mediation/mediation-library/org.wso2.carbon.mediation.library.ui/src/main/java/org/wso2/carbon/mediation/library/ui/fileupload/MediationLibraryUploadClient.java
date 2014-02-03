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

import org.apache.axis2.context.ConfigurationContext;
import org.apache.axis2.AxisFault;
import org.apache.axis2.Constants;
import org.apache.axis2.client.Options;
import org.wso2.carbon.mediation.library.stub.upload.types.carbon.LibraryFileItem;
import org.wso2.carbon.mediation.library.stub.upload.MediationLibraryUploaderStub;

import javax.activation.DataHandler;
import java.rmi.RemoteException;
import java.util.List;
import java.util.ArrayList;

public class MediationLibraryUploadClient {

    private List<LibraryFileItem> uploadLibraryInfoList;
    private MediationLibraryUploaderStub stub;

    public MediationLibraryUploadClient(ConfigurationContext ctx,
                                        String serviceURL, String cookie)  throws AxisFault {
        stub = new MediationLibraryUploaderStub(ctx, serviceURL);
        Options options =  stub._getServiceClient().getOptions();
        options.setProperty(Constants.Configuration.ENABLE_MTOM, Constants.VALUE_TRUE);
        options.setProperty(org.apache.axis2.transport.http.HTTPConstants.COOKIE_STRING, cookie);
        options.setManageSession(true);
        //Increase the time out when sending large attachments
        options.setTimeOutInMilliSeconds(10000);
        uploadLibraryInfoList = new ArrayList<LibraryFileItem>();
    }

    public void addUploadedFileItem(DataHandler dataHandler, String fileName, String fileType) {
        LibraryFileItem uploadedFileItem = new LibraryFileItem();
        uploadedFileItem.setDataHandler(dataHandler);
        uploadedFileItem.setFileName(fileName);
        uploadedFileItem.setFileType(fileType);
        uploadLibraryInfoList.add(uploadedFileItem);
    }

    public void uploadFileItems() throws RemoteException {
        LibraryFileItem[] uploadServiceTypes = new LibraryFileItem[uploadLibraryInfoList.size()];
        uploadServiceTypes = uploadLibraryInfoList.toArray(uploadServiceTypes);
        stub.uploadLibrary(uploadServiceTypes);
    }
}
