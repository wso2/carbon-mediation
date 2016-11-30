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
package org.wso2.carbon.mediation.library.service.upload;

import org.apache.axis2.AxisFault;
import org.apache.axis2.engine.AxisConfiguration;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.wso2.carbon.core.AbstractAdmin;
import org.wso2.carbon.utils.CarbonUtils;

import javax.activation.DataHandler;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.nio.channels.FileChannel;

/**
 * Carbon Application Uploader service.
 */
public class LibraryUploader extends AbstractAdmin {

    private static final Log log = LogFactory.getLog(LibraryUploader.class);

    public void uploadLibrary(LibraryFileItem[] fileItems) throws AxisFault {
        try {

            AxisConfiguration axisConfig = getAxisConfig();
            String repo = axisConfig.getRepository().getPath();

            //Writting the artifacts to the proper location
            String carbonAppDir = repo + File.separator + "synapse-libs";
            createDir(carbonAppDir);

            String carbonHomeTmp = CarbonUtils.getCarbonHome() + File.separator + "tmp";
            createDir(carbonHomeTmp);
            String carbonAppDirTemp = carbonHomeTmp + File.separator + "synapse-libsuploads";
            createDir(carbonAppDirTemp);

            for (LibraryFileItem libraryFile : fileItems) {
                String fileName = libraryFile.getFileName();
                if (fileName == null || fileName.equals(""))
                    throw new AxisFault("Invalid file name");

                if (fileName.endsWith(".zip")) {
                    writeResource(libraryFile.getDataHandler(), carbonAppDirTemp, carbonAppDir,
                            fileName);
                } else {
                    throw new AxisFault("Invalid file type : " + libraryFile.getFileType());
                }
            }
        } catch (Exception e) {
            String msg = "Error occurred while uploading Carbon App artifacts";
            log.error(msg, e);
            throw new AxisFault(msg, e);
        }
    }

    private void writeResource(DataHandler dataHandler, String tempDestPath, String destPath,
                               String fileName)
            throws IOException {

        File tempDestFile = new File(tempDestPath, fileName);
        FileChannel out = null;
        FileChannel in = null;
        FileOutputStream fos = null;

        try {
            fos = new FileOutputStream(tempDestFile);
            dataHandler.writeTo(fos);
            fos.flush();

            /* File stream is copied to a temp directory in order handle hot deployment issue
               occurred in windows */
            dataHandler.writeTo(fos);
            out = new FileOutputStream(destPath + File.separator + fileName).getChannel();
            in = new FileInputStream(tempDestFile).getChannel();
            out.write(in.map(FileChannel.MapMode.READ_ONLY, 0, in.size()));
        } finally {
            try {
                if (out != null) {
                    out.close();
                }
            } catch (IOException e) {
                log.warn("Can't close file streams.", e);
            }
            try {
                if (in != null) {
                    in.close();
                }
            } catch (IOException e) {
                log.warn("Can't close file streams.", e);
            }
            try {
                if (fos != null) {
                    fos.close();
                }
            } catch (IOException e) {
                log.warn("Can't close file streams.", e);
            }
        }

        if (!tempDestFile.delete()) {
            if (log.isDebugEnabled()) {
                log.debug("temp file: " + tempDestFile.getAbsolutePath() +
                          " deletion failed, scheduled deletion on server exit.");
            }
            tempDestFile.deleteOnExit();
        }
    }

    private void createDir(String path) throws Exception {
        File temp = new File(path);
        if (!temp.exists() && !temp.mkdir()) {
            String msg = "Error while creating directory : " + path;
            log.error(msg);
            throw new Exception(msg);
        }
    }

}
