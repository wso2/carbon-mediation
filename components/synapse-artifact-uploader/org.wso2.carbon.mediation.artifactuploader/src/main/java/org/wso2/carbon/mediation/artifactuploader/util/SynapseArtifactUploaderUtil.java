package org.wso2.carbon.mediation.artifactuploader.util;

import java.io.File;
import java.util.ArrayList;

/**
 * Utility cass for Synapse Artifact Uploader
 */
public class SynapseArtifactUploaderUtil {

    /**
     * Returns Existing uploaded artifacts to Server
     */
    public static String[] getArtifacts(String extensionsPath) {
        String[] artifactList = null;
        File artifactsRepo = new File(extensionsPath);
        if(!artifactsRepo.exists()){
            return null;
        }
        if (artifactsRepo.isDirectory()) {
            File[] files = artifactsRepo.listFiles();
            ArrayList<String> artifacts = new ArrayList<String>();
            for (File file : files) {
                if (file.isFile()) {
                    artifacts.add(file.getName());
                }
            }
            artifactList = new String[artifacts.size()];
            artifacts.toArray(artifactList);
        }
        return artifactList;
    }

}
