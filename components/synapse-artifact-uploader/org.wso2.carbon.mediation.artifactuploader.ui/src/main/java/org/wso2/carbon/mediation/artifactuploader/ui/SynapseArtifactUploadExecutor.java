package org.wso2.carbon.mediation.artifactuploader.ui;

import org.wso2.carbon.CarbonConstants;
import org.wso2.carbon.CarbonException;
import org.wso2.carbon.ui.transports.fileupload.AbstractFileUploadExecutor;
import org.wso2.carbon.utils.FileItemData;
import org.wso2.carbon.utils.ServerConstants;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

/**
 * SynapseArtifactUPloader handles hot uploading synapse artifacts
 */
public class SynapseArtifactUploadExecutor extends AbstractFileUploadExecutor {
    @Override
    public boolean execute(HttpServletRequest request, HttpServletResponse response)
            throws CarbonException, IOException {

        String webContext = (String) request.getAttribute(CarbonConstants.WEB_CONTEXT);
        String serverURL = (String) request.getAttribute(CarbonConstants.SERVER_URL);
        String cookie = (String) request.getAttribute(ServerConstants.ADMIN_SERVICE_COOKIE);


        Map<String, ArrayList<FileItemData>> fileItemsMap = getFileItemsMap();
        if (fileItemsMap == null || fileItemsMap.isEmpty()) {
            String msg = "Artifact uploading failed. No file specified.";
            log.error(msg);
            return false;
        }

        SynapseArtifactUploaderClient client = new SynapseArtifactUploaderClient(cookie, serverURL, configurationContext, request.getLocale());
        List<FileItemData> fileItemDataList = fileItemsMap.get("synapseArtifactName");

        for (FileItemData fileData : fileItemDataList) {
            client.uploadArtifact(getFileName(fileData.getFileItem().getName()), fileData.getDataHandler());
            log.info("Uploaded Artifact : " + fileData.getFileItem().getName());
            response.sendRedirect("../" + webContext + "/artifactuploader/index.jsp?ordinal=1");
            return true;
        }
        return false;
    }
}