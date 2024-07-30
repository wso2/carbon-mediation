package org.wso2.carbon.mediation.artifactuploader.util;

import java.io.File;
import java.io.IOException;
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

    /**
     * Finds the extension of a given file
     *
     * @param fileName - name of the file
     * @return - extension
     */
    public static String getFileExtension(String fileName) {
        int index = fileName.lastIndexOf('.');
        return fileName.substring(index + 1);
    }

    /**
     * Validates whether the destinationFile is copied to the target directory
     *
     * @param destinationFile - file to be uploaded or removed
     * @param targetDirectory - target directory
     * @return true if the destination file is copied to the target directory
     * @throws IOException
     */
    public static boolean validateFilePath(File destinationFile, File targetDirectory) throws IOException {
        String canonicalPathToFile = destinationFile.getCanonicalPath();
        String canonicalPathToArtifactDir = targetDirectory.getCanonicalPath();
        return canonicalPathToFile.startsWith(canonicalPathToArtifactDir);
    }

}
