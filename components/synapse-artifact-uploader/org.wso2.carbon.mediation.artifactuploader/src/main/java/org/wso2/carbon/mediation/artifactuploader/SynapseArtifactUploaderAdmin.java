package org.wso2.carbon.mediation.artifactuploader;

import org.apache.axis2.AxisFault;
import org.apache.commons.io.FileUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.wso2.carbon.core.AbstractAdmin;
import org.wso2.carbon.mediation.artifactuploader.util.SynapseArtifactUploaderUtil;
import org.wso2.carbon.utils.CarbonUtils;

import javax.activation.DataHandler;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;

/**
 * Synapse Artifact Uploader Service class
 */

public class SynapseArtifactUploaderAdmin extends AbstractAdmin {

    private static final Log log = LogFactory.getLog(SynapseArtifactUploaderAdmin.class);

    public boolean uploadArtifact(String fileName, DataHandler dataHandler) throws AxisFault {

        File artifactDir = new File(getExtensionRepoPath());
        File tempDir = new File(CarbonUtils.getCarbonHome() + File.separator + "tmp");
        File destinationTempFile = new File(tempDir, fileName);
        FileOutputStream fos = null;
        try {
            fos = FileUtils.openOutputStream(destinationTempFile);
            dataHandler.writeTo(fos);
        } catch (IOException e) {
            handleException("File Upload failed", e);
        } finally {
            try {
                if (fos != null) {
                    fos.close();
                }
            } catch (IOException e) {
                log.warn("Unable to close file " + destinationTempFile.getAbsolutePath(), e);
            }
        }

        try {
            FileUtils.copyFileToDirectory(destinationTempFile, artifactDir, true);
            FileUtils.deleteQuietly(destinationTempFile);
        } catch (IOException e) {
            handleException("Unable to move file to artifact directory :" + artifactDir.getAbsolutePath(), e);
        }
        return true;
    }

    public String[] getArtifacts() {
        return SynapseArtifactUploaderUtil.getArtifacts(getExtensionRepoPath());
    }

    public boolean removeArtifact(String fileName) throws AxisFault {
        File artifactFile = new File(getExtensionRepoPath() + File.separator + fileName);

        if (artifactFile.exists() && artifactFile.isFile()) {
            return artifactFile.delete();
        } else {
            handleException("Artifact " + artifactFile.getAbsolutePath() + " not found");
        }
        return false;
    }

    private String getExtensionRepoPath() {
        return getAxisConfig().getRepository().getFile() + File.separator + "extensions";
    }

    private void handleException(String msg, Throwable e) throws AxisFault {
        log.error(msg, e);
        throw new AxisFault(msg, e);
    }

    private void handleException(String msg) throws AxisFault {
        log.error(msg);
        throw new AxisFault(msg);
    }
}
