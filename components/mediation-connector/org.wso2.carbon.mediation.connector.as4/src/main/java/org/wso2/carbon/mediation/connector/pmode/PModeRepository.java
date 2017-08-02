/*
*  Copyright (c) 2017, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
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

package org.wso2.carbon.mediation.connector.pmode;

import org.apache.axis2.AxisFault;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.commons.vfs2.*;
import org.apache.commons.vfs2.impl.DefaultFileMonitor;
import org.wso2.carbon.mediation.connector.AS4Constants;
import org.wso2.carbon.mediation.connector.pmode.impl.PMode;

import javax.xml.bind.JAXBContext;
import javax.xml.bind.JAXBException;
import javax.xml.bind.Unmarshaller;
import java.io.File;
import java.io.InputStream;
import java.util.HashMap;
import java.util.Map;

/**
 * PMode repository implementation which will store current PModes. This will work as file listener as well. So adding
 * and editing PModes will get automatically synced with this in memory repository.
 */
public class PModeRepository implements FileListener {

    private static final Log log = LogFactory.getLog(PModeRepository.class);

    private static PModeRepository pmodeRepository;

    //key - agreement.name value - pMode
    private Map<String, PMode> pModeMap;
    //key - fileName, value - agreementRef
    private Map<String, String> fileNameRefMap;
    //key - fileName, value - agreementRef
    private Map<String, String> possibleNameChangeMap;
    private Unmarshaller pModeUnmarshaller;
    private int basePathLength;


    /**
     * Constructor for PMode repository implementation.
     *
     * @param pmodeRepositoryPath Path to the directory containing PMode files
     */
    private PModeRepository(String pmodeRepositoryPath) throws AxisFault {

        if (log.isDebugEnabled()) {
            log.debug("Initializing PMode repository for the location : " + pmodeRepositoryPath);
        }

        this.pModeMap = new HashMap<String, PMode>();
        this.fileNameRefMap = new HashMap<String, String>();
        this.possibleNameChangeMap = new HashMap<String, String>();
        File pmodeFolder;

        try {
            this.pModeUnmarshaller = JAXBContext.newInstance(PMode.class).createUnmarshaller();
        } catch (JAXBException e) {
            log.error("Unable to create JAXB unmarshaller for : " + PMode.class, e);
            throw new AxisFault("Unable to create JAXB unmarshaller for : " + PMode.class, e);
        }

        if (pmodeRepositoryPath != null) {
            pmodeFolder = new File(pmodeRepositoryPath);
            if (!pmodeFolder.exists() || !pmodeFolder.isDirectory()) {
                log.warn("Provided PMode directory is invalid, falling back to default PMode Directory : " +
                        AS4Constants.AS4_PMODE_LOCATION);
                pmodeFolder = new File(AS4Constants.AS4_PMODE_LOCATION);
            }
        } else {
            if (log.isDebugEnabled()) {
                log.debug("PMode directory not provided, falling back to default PMode Directory : " +
                        AS4Constants.AS4_PMODE_LOCATION);
            }
            pmodeFolder = new File(AS4Constants.AS4_PMODE_LOCATION);
        }

        traversePmodeDirectory(pmodeFolder);
        try {
            FileSystemManager fileSystemManager = VFS.getManager();
            FileObject listenDirectory = fileSystemManager.resolveFile(pmodeFolder.getAbsolutePath());
            this.basePathLength = listenDirectory.getName().getPathDecoded().length() + 1;
            DefaultFileMonitor fileMonitor = new DefaultFileMonitor(this);
            fileMonitor.addFile(listenDirectory);
            fileMonitor.start();
        } catch (FileSystemException e) {
            log.warn("Error registering PMode watcher, hence needs to restart the server when PModes " +
                    "change or added, " + e.getMessage(), e);
        }
    }

    /**
     * This will return the PModeRepository instance
     * @throws AxisFault
     */
    public static synchronized PModeRepository getInstance() throws AxisFault {

        if (pmodeRepository == null) {
            pmodeRepository = new PModeRepository(null);
        }
        return pmodeRepository;
    }

    /**
     * Helper method which traverse PMode directory and add PModes for the first time
     *
     * @param folder PMode directory File object
     */
    private void traversePmodeDirectory(final File folder) {

        if (folder.listFiles() == null || folder.listFiles().length == 0) {
            log.warn("No PMode files found in the directory : " + folder.getName());
            return;
        }
        for (final File file : folder.listFiles()) {
            if (file.isDirectory()) {
                log.warn("PMode directory has sub directory, skipping sub directory : " + file.getName());
            } else {
                if (!file.getName().endsWith("~")) {
                    processPModeFile(file);
                } else if (log.isDebugEnabled()) {
                    log.debug("Skipping backup file : " + file.getName());
                }
            }
        }
    }

    /**
     * Helper method to process PMode files
     *
     * @param pmodeFile PMode file File object
     */
    private void processPModeFile(File pmodeFile) {

        if(log.isDebugEnabled()) {
            log.debug("Processing PMode file : " + pmodeFile.getName());
        }
        try {
            PMode pmode = (PMode) this.pModeUnmarshaller.unmarshal(pmodeFile);
            if (pmode.getAgreement() == null || pmode.getAgreement().getName() == null ||
                    pmode.getAgreement().getName().isEmpty()) {
                log.warn("Agreement not found in the PMode file, hence ignoring : " + pmodeFile.getName());
                return;
            }
            validatePMode(pmode);
            addUpdateRemovePMode(Operation.ADD, pmodeFile.getAbsolutePath(), pmode);
        } catch (JAXBException e) {
            log.warn("Unable to unmarshall PMode file : " + pmodeFile.getName() + ", " + e.getMessage(), e);
        } catch (AxisFault axisFault) {
            log.warn("Error while validating PMode file : " + pmodeFile.getName() + ", " +
                    axisFault.getMessage(), axisFault);
        }
    }

    /**
     * Helper method to add, update or remove PMode files from this repository
     *
     * @param operation Operation to be performed
     * @param filePath  Path to PMode file
     * @param pMode     PMode object
     */
    private synchronized void addUpdateRemovePMode(Operation operation, String filePath, PMode pMode) {
        switch (operation) {
            case ADD:
                addPMode(filePath, pMode);
                break;
            case UPDATE:
                updatePMode(filePath, pMode);
                break;
            case REMOVE:
                removePMode(filePath);
                break;
        }
    }

    /**
     * Helper method to add PMode to the repository
     *
     * @param filePath path to PMode file
     * @param pMode    PMode object
     */
    private void addPMode(String filePath, PMode pMode) {

        if(log.isDebugEnabled()) {
            log.debug("Adding PMode file : " + filePath);
        }
        if (!pModeMap.containsKey(pMode.getAgreement().getName()) && !fileNameRefMap.containsKey(filePath)) {
            // Not in both maps -> implies new PMode
            pModeMap.put(pMode.getAgreement().getName(), pMode);
            fileNameRefMap.put(filePath, pMode.getAgreement().getName());
            if (log.isDebugEnabled()) {
                log.debug("PMode added with agreement : " + pMode.getAgreement().getName() + ", file : " + filePath);
            }
        } else if (!fileNameRefMap.containsKey(filePath)) {
            // Existing in PMode map but not in name map -> implies possible file name change
            String existingFilePath = getExistingPath(pMode.getAgreement().getName());
            if (existingFilePath != null && !existingFilePath.isEmpty()) {
                File existingFile = new File(existingFilePath);
                if (existingFile.exists()) {
                    log.warn("Duplicate PMode agreements found in two files, agreement : "
                            + pMode.getAgreement().getName() + ", ignoring : " + filePath);
                    return;
                }
            }
            fileNameRefMap.remove(existingFilePath);
            fileNameRefMap.put(filePath, pMode.getAgreement().getName());
            possibleNameChangeMap.put(existingFilePath, pMode.getAgreement().getName());
            if (log.isDebugEnabled()) {
                log.debug("File path updated for the renamed PMode, agreement : " + pMode.getAgreement().getName()
                        + ", previous file : " + existingFilePath + ", new file : " + filePath);
            }
        } else {
            /**
             * Comes to this for two conditions
             * 1 - not in PMode map and in fileMap
             * 2 - in PMode map and in fileMap
             *
             * Those two scenarios cannot happen with this implementation
             */
            log.warn("Duplicate PMode agreements found in two files, agreement : " + pMode.getAgreement().getName());
        }
    }

    /**
     * Helper method to update PMode in the repository
     *
     * @param filePath path to PMode file
     * @param pMode    PMode object
     */
    private void updatePMode(String filePath, PMode pMode) {

        if(log.isDebugEnabled()) {
            log.debug("Updating PMode File : " + filePath);
        }
        if (pModeMap.containsKey(pMode.getAgreement().getName())) {
            pModeMap.put(pMode.getAgreement().getName(), pMode);
            if (log.isDebugEnabled()) {
                log.debug("Updating existing PMode : " + pMode.getAgreement().getName());
            }
        } else {
            pModeMap.remove(fileNameRefMap.get(filePath));
            fileNameRefMap.put(filePath, pMode.getAgreement().getName());
            pModeMap.put(pMode.getAgreement().getName(), pMode);
            if (log.isDebugEnabled()) {
                log.debug("PMode agreement changed, updating PMode : " + pMode.getAgreement().getName());
            }
        }
    }

    /**
     * Helper method to remove PModes
     *
     * @param filePath path to PMode file
     */
    private void removePMode(String filePath) {

        if(log.isDebugEnabled()) {
            log.debug("Removing PMode file : " + filePath);
        }
        if (!possibleNameChangeMap.containsKey(filePath)) {
            if (fileNameRefMap.containsKey(filePath)) {
                pModeMap.remove(fileNameRefMap.get(filePath));
                fileNameRefMap.remove(filePath);
                if (log.isDebugEnabled()) {
                    log.debug("Removing PMode : " + filePath);
                }
            } else {
                //directory deletion will come to this or files within inner directories will come to this, hence ignore
            }
        } else {
            possibleNameChangeMap.remove(filePath);
            if (log.isDebugEnabled()) {
                log.debug("File name changed : " + filePath);
            }
        }
    }

    /**
     * Helper method to get previous file path.
     *
     * @param agreement PMode Agreement
     * @return previous file path
     */
    private String getExistingPath(String agreement) {

        for (Map.Entry entry : fileNameRefMap.entrySet()) {
            if (entry.getValue().equals(agreement)) {
                return entry.getKey().toString();
            }
        }
        return null;
    }

    /**
     * API method to get PMode using agreementRef
     *
     * @param agreement PMode Agreement
     * @return pMode PMode object
     */
    public PMode findPModeFromAgreement(String agreement) {
        return pModeMap.get(agreement);
    }

    @Override
    public void fileCreated(FileChangeEvent fileChangeEvent) throws Exception {

        try {
            if (isProcessingRequired(fileChangeEvent)) {
                InputStream inputStream = fileChangeEvent.getFile().getContent().getInputStream();
                PMode pMode = (PMode) this.pModeUnmarshaller.unmarshal(inputStream);
                validatePMode(pMode);
                addUpdateRemovePMode(Operation.ADD, fileChangeEvent.getFile().getName().getPathDecoded(), pMode);
            }
        } catch (FileSystemException e) {
            log.warn("File system exception occurred while dynamically updating PModes, " + e.getMessage(), e);
        } catch (JAXBException e) {
            log.warn("JAXB exception occurred while dynamically updating PModes, " + e.getMessage(), e);
        } catch (AxisFault e) {
            log.warn("SynapseException occurred while dynamically updating PModes, " + e.getMessage(), e);
        } catch (Exception e) {
            log.warn("exception occurred while dynamically updating PModes, " + e.getMessage(), e);
        }
    }

    @Override
    public void fileDeleted(FileChangeEvent fileChangeEvent) throws Exception {

        try {
            addUpdateRemovePMode(Operation.REMOVE, fileChangeEvent.getFile().getName().getPathDecoded(), null);
        } catch (Exception e) {
            log.warn("Exception occurred while dynamically updating PModes, " + e.getMessage(), e);
        }
    }

    @Override
    public void fileChanged(FileChangeEvent fileChangeEvent) throws Exception {

        try {
            if (isProcessingRequired(fileChangeEvent)) {
                InputStream inputStream = fileChangeEvent.getFile().getContent().getInputStream();
                PMode pMode = (PMode) this.pModeUnmarshaller.unmarshal(inputStream);
                validatePMode(pMode);
                addUpdateRemovePMode(Operation.UPDATE, fileChangeEvent.getFile().getName().getPathDecoded(), pMode);
            }
        } catch (FileSystemException e) {
            log.warn("File system exception occurred while dynamically updating PModes, " + e.getMessage(), e);
        } catch (JAXBException e) {
            log.warn("JAXB exception occurred while dynamically updating PModes, " + e.getMessage(), e);
        } catch (AxisFault e) {
            log.warn("AxisFault occurred while dynamically updating PModes, " + e.getMessage(), e);
        } catch (Exception e) {
            log.warn("Exception occurred while dynamically updating PModes, " + e.getMessage(), e);
        }
    }

    /**
     * Helper method to detect whether  the file corresponding to FileChangeEvent needs to be processed
     *
     * @param fileChangeEvent FileChangeEvent object
     * @return true if needs to process, false otherwise
     * @throws FileSystemException
     */
    private boolean isProcessingRequired(FileChangeEvent fileChangeEvent) throws FileSystemException {

        String path = fileChangeEvent.getFile().getName().getPathDecoded();
        File file = new File(path);
        if (file.isDirectory()) {
            return false;
        }
        String fileName = path.substring(basePathLength);
        if (fileName.contains(File.separator)) {
            return false;
        }
        if (fileChangeEvent.getFile().getName().getPath().endsWith("~")) {
            return false;
        }
        return true;
    }

    /**
     * Validation method to validate PMode files
     *
     * @param pmode PMode instance
     */
    private void validatePMode(PMode pmode) throws AxisFault {

        if (pmode == null) {
            throw new AxisFault("PMode not found");
        }
        if (pmode.getAgreement() == null || pmode.getAgreement().getName() == null
                || pmode.getAgreement().getName().isEmpty()) {
            throw new AxisFault("Invalid PMode Agreement");
        }
        if (pmode.getInitiator() == null) {
            throw new AxisFault("Initiator not found in PMode");
        }
        if (pmode.getInitiator().getParty() == null || pmode.getInitiator().getParty().isEmpty()) {
            throw new AxisFault("Invalid Initiator Party in PMode");
        }
        if(pmode.getInitiator().getRole() == null || pmode.getInitiator().getRole().isEmpty()) {
            throw new AxisFault("Invalid Initiator Role in PMode");
        }
        if(pmode.getProtocol() == null) {
            throw new AxisFault("Protocol not found in PMode");
        }
        if(pmode.getProtocol().getAddress() == null || pmode.getProtocol().getAddress().isEmpty()) {
            throw new AxisFault("Invalid Protocol Address found in PMode");
        }
    }

    /**
     * Operation type for PMode
     */
    enum Operation {
        ADD,
        REMOVE,
        UPDATE
    }
}
