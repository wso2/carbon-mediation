/*
 *  Copyright (c) 2005-2014, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
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
package org.wso2.carbon.inbound.endpoint.protocol.file;

import java.text.SimpleDateFormat;
import java.util.Arrays;
import java.util.Comparator;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;
import java.util.Properties;

import org.apache.axis2.AxisFault;
import org.apache.commons.lang.WordUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.commons.vfs2.FileContent;
import org.apache.commons.vfs2.FileNotFolderException;
import org.apache.commons.vfs2.FileNotFoundException;
import org.apache.commons.vfs2.FileObject;
import org.apache.commons.vfs2.FileSystemException;
import org.apache.commons.vfs2.FileSystemManager;
import org.apache.commons.vfs2.FileSystemOptions;
import org.apache.commons.vfs2.FileType;
import org.apache.commons.vfs2.impl.StandardFileSystemManager;
import org.apache.commons.vfs2.provider.UriParser;
import org.apache.synapse.commons.vfs.VFSConstants;
import org.apache.synapse.commons.vfs.VFSParamDTO;
import org.apache.synapse.commons.vfs.VFSUtils;
import org.apache.synapse.core.SynapseEnvironment;
import org.wso2.carbon.mediation.clustering.ClusteringServiceUtil;

/**
 * 
 * This class implement the processing logic related to inbound file protocol.
 * Common functinalities (with synapse vfs transport) are include in synapse
 * util that is found in synapse commons
 * 
 */
public class FilePollingConsumer {

    private static final Log log = LogFactory.getLog(FilePollingConsumer.class);
    private Properties vfsProperties;
    private boolean fileLock = true;
    private FileSystemManager fsManager = null;
    private String name;
    private SynapseEnvironment synapseEnvironment;
    private long scanInterval;
    private Long lastRanTime;
    private int lastCycle;
    private FileInjectHandler injectHandler;

    private String fileURI;
    private FileObject fileObject;
    private Integer iFileProcessingInterval = null;
    private Integer iFileProcessingCount = null;
    private int maxRetryCount;
    private long reconnectionTimeout;
    private String strFilePattern;
    private boolean autoLockRelease;
    private Boolean autoLockReleaseSameNode;
    private Long autoLockReleaseInterval;
    private boolean distributedLock;
    private Long distributedLockTimeout;
    private FileSystemOptions fso;
    
    public FilePollingConsumer(Properties vfsProperties, String name,
            SynapseEnvironment synapseEnvironment, long scanInterval) {
        this.vfsProperties = vfsProperties;
        this.name = name;
        this.synapseEnvironment = synapseEnvironment;
        this.scanInterval = scanInterval;
        this.lastRanTime = null;

        setupParams();
        try {
            StandardFileSystemManager fsm = new StandardFileSystemManager();
            fsm.setConfiguration(getClass().getClassLoader().getResource("providers.xml"));
            fsm.init();
            fsManager = fsm;
        } catch (Exception e) {
            log.error(e);
            throw new RuntimeException(e);
        }
        //Setup SFTP Options
        try {
            fso = VFSUtils.attachFileSystemOptions(parseSchemeFileOptions(fileURI),fsManager);
        } catch (Exception e) {
            log.warn("Unable to set the sftp Options", e);
            fso = null;
        }
    }

    /**
     * Register a handler to process the file stream after reading from the
     * source
     * 
     * @param injectHandler
     */
    public void registerHandler(FileInjectHandler injectHandler) {
        this.injectHandler = injectHandler;
    }

    /**
     * This will be called by the task scheduler. If a cycle execution takes
     * more than the schedule interval, tasks will call this method ignoring the
     * interval. Timestamp based check is done to avoid that.
     */
    public void execute() {
        try {
            if (log.isDebugEnabled()) {
                log.debug("Start : File Inbound EP : " + name);
            }
            // Check if the cycles are running in correct interval and start
            // scan
            long currentTime = (new Date()).getTime();
            if (lastRanTime == null || ((lastRanTime + (scanInterval)) <= currentTime)) {
                lastRanTime = currentTime;
                poll();
            } else if (log.isDebugEnabled()) {
                log.debug("Skip cycle since cuncurrent rate is higher than the scan interval : VFS Inbound EP : "
                        + name);
            }
            if (log.isDebugEnabled()) {
                log.debug("End : File Inbound EP : " + name);
            }
        } catch (Exception e) {
            log.error("Error while reading file. " + e.getMessage(), e);
        }
    }

    /**
     * 
     * Do the file processing operation for the given set of properties. Do the
     * checks and pass the control to processFile method
     * 
     * */
    public FileObject poll() {
        if (fileURI == null || fileURI.trim().equals("")) {
            log.error("Invalid file url. Check the inbound endpoint configuration. Endpoint Name : "
                    + name + ", File URL : " + fileURI);
            return null;
        }

        if (log.isDebugEnabled()) {
            log.debug("Start : Scanning directory or file : " + VFSUtils.maskURLPassword(fileURI));
        }

        if (!initFileCheck()) {
            // Unable to read from the source location provided.
            return null;
        }

        // If file/folder found proceed to the processing stage
        try {
            lastCycle = 0;
            if (fileObject.exists() && fileObject.isReadable()) {
                FileObject[] children = null;
                try {
                    children = fileObject.getChildren();
                } catch (FileNotFolderException ignored) {
                    if (log.isDebugEnabled()) {
                        log.debug("No Folder found. Only file found on : "
                                + VFSUtils.maskURLPassword(fileURI));
                    }
                } catch (FileSystemException ex) {
                    log.error(ex.getMessage(), ex);
                }

                // if this is a file that would translate to a single message
                if (children == null || children.length == 0) {
                    // Fail record is a one that is processed but was not moved
                    // or deleted due to an error.
                    boolean isFailedRecord = VFSUtils.isFailRecord(fsManager, fileObject);
                    if (!isFailedRecord) {
                        fileHandler();
                        if (injectHandler == null) {
                            return fileObject;
                        }
                    } else {
                        try {
                            lastCycle = 2;
                            moveOrDeleteAfterProcessing(fileObject);
                        } catch (AxisFault axisFault) {
                            log.error("File object '" + fileObject.getURL().toString() + "' "
                                    + "cloud not be moved after first attempt", axisFault);
                        }
                        if (fileLock) {
                            // TODO: passing null to avoid build break. Fix properly
                            VFSUtils.releaseLock(fsManager, fileObject, fso);
                        }
                        if (log.isDebugEnabled()) {
                            log.debug("File '" + fileObject.getURL()
                                    + "' has been marked as a failed"
                                    + " record, it will not process");
                        }
                    }
                } else {
                    FileObject fileObject = directoryHandler(children);
                    if (fileObject != null) {
                        return fileObject;
                    }
                }
            } else {
                log.warn("Unable to access or read file or directory : "
                        + VFSUtils.maskURLPassword(fileURI)
                        + "."
                        + " Reason: "
                        + (fileObject.exists() ? (fileObject.isReadable() ? "Unknown reason"
                                : "The file can not be read!") : "The file does not exists!"));
                return null;
            }
        } catch (FileSystemException e) {
            log.error(
                    "Error checking for existence and readability : "
                            + VFSUtils.maskURLPassword(fileURI), e);
            return null;
        } catch (Exception e) {
            log.error(
                    "Error while processing the file/folder in URL : "
                            + VFSUtils.maskURLPassword(fileURI), e);
            return null;
        } finally {
            try {
                fsManager.closeFileSystem(fileObject.getParent().getFileSystem());
                fileObject.close();
            } catch (Exception e) {
                log.error("Unable to close the file system. " + e.getMessage());
                log.error(e);
            }
        }
        if (log.isDebugEnabled()) {
            log.debug("End : Scanning directory or file : " + VFSUtils.maskURLPassword(fileURI));
        }
        return null;
    }

    /**
     * If not a folder just a file handle the flow
     * 
     * @throws FileSystemException
     */
    private void fileHandler() throws FileSystemException {
        if (fileObject.getType() == FileType.FILE) {
            if (!fileLock || (fileLock && acquireLock(fsManager, fileObject))) {
                boolean runPostProcess = true;
                try {
                    if (processFile(fileObject) == null) {
                        runPostProcess = false;
                    }
                    lastCycle = 1;
                } catch (AxisFault e) {
                    lastCycle = 2;
                    log.error("Error processing File URI : " + fileObject.getName(), e);
                }

                if (runPostProcess) {
                    try {
                        moveOrDeleteAfterProcessing(fileObject);
                    } catch (AxisFault axisFault) {
                        lastCycle = 3;
                        log.error("File object '" + fileObject.getURL().toString() + "' "
                                + "cloud not be moved", axisFault);
                        VFSUtils.markFailRecord(fsManager, fileObject);
                    }
                }

                if (fileLock) {
                    // TODO: passing null to avoid build break. Fix properly
                    VFSUtils.releaseLock(fsManager, fileObject, fso);
                    if (log.isDebugEnabled()) {
                        log.debug("Removed the lock file '" + fileObject.toString()
                                + ".lock' of the file '" + fileObject.toString());
                    }
                }

            } else {
                log.error("Couldn't get the lock for processing the file : " + fileObject.getName());
            }

        } else {
            if (log.isDebugEnabled()) {
                log.debug("Cannot find the file or failed file record. File : "
                        + VFSUtils.maskURLPassword(fileURI));
            }
        }
    }

    /**
     * Setup the required parameters
     */
    private void setupParams() {
        
        fileURI = vfsProperties.getProperty(VFSConstants.TRANSPORT_FILE_FILE_URI);
        
        String strFileLock = vfsProperties.getProperty(VFSConstants.TRANSPORT_FILE_LOCKING);
        if (strFileLock != null
                && strFileLock.toLowerCase().equals(VFSConstants.TRANSPORT_FILE_LOCKING_DISABLED)) {
            fileLock = false;
        }

        strFilePattern = vfsProperties.getProperty(VFSConstants.TRANSPORT_FILE_FILE_NAME_PATTERN);
        if (vfsProperties.getProperty(VFSConstants.TRANSPORT_FILE_INTERVAL) != null) {
            try {
                iFileProcessingInterval = Integer.valueOf(vfsProperties
                        .getProperty(VFSConstants.TRANSPORT_FILE_INTERVAL));
            } catch (NumberFormatException e) {
                log.warn("Invalid param value for transport.vfs.FileProcessInterval : "
                        + vfsProperties.getProperty(VFSConstants.TRANSPORT_FILE_INTERVAL)
                        + ". Expected numeric value.");
            }
        }
        if (vfsProperties.getProperty(VFSConstants.TRANSPORT_FILE_COUNT) != null) {
            try {
                iFileProcessingCount = Integer.valueOf(vfsProperties
                        .getProperty(VFSConstants.TRANSPORT_FILE_COUNT));
            } catch (NumberFormatException e) {
                log.warn("Invalid param value for transport.vfs.FileProcessCount : "
                        + vfsProperties.getProperty(VFSConstants.TRANSPORT_FILE_COUNT)
                        + ". Expected numeric value.");
            }
        }
        maxRetryCount = 0;
        if (vfsProperties.getProperty(VFSConstants.MAX_RETRY_COUNT) != null) {
            try {
                maxRetryCount = Integer.valueOf(vfsProperties
                        .getProperty(VFSConstants.MAX_RETRY_COUNT));
            } catch (NumberFormatException e) {
                log.warn("Invalid values for Max Retry Count");
                maxRetryCount = 0;
            }
        }

        reconnectionTimeout = 1;
        if (vfsProperties.getProperty(VFSConstants.RECONNECT_TIMEOUT) != null) {
            try {
                reconnectionTimeout = Long.valueOf(vfsProperties
                        .getProperty(VFSConstants.RECONNECT_TIMEOUT));
            } catch (NumberFormatException e) {
                log.warn("Invalid values for Reconnection Timeout");
                reconnectionTimeout = 1;
            }
        }
        
        String strAutoLock = vfsProperties.getProperty(VFSConstants.TRANSPORT_AUTO_LOCK_RELEASE);
        autoLockRelease = false;
        autoLockReleaseSameNode = true;
        autoLockReleaseInterval = null;
        if (strAutoLock != null) {
            try {
                autoLockRelease = Boolean.parseBoolean(strAutoLock);
            } catch (Exception e) {
                autoLockRelease = false;
                log.warn("VFS Auto lock removal not set properly. Current value is : "
                        + strAutoLock, e);
            }      
            if (autoLockRelease) {
                String strAutoLockInterval = vfsProperties
                        .getProperty(VFSConstants.TRANSPORT_AUTO_LOCK_RELEASE_INTERVAL);
                if (strAutoLockInterval != null) {
                    try {
                        autoLockReleaseInterval = Long.parseLong(strAutoLockInterval);
                    } catch (Exception e) {
                        autoLockReleaseInterval = null;
                        log.warn(
                                "VFS Auto lock removal property not set properly. Current value is : "
                                        + strAutoLockInterval, e);
                    }
                }
                String strAutoLockReleaseSameNode = vfsProperties
                        .getProperty(VFSConstants.TRANSPORT_AUTO_LOCK_RELEASE_SAME_NODE);
                if (strAutoLockReleaseSameNode != null) {
                    try {
                        autoLockReleaseSameNode = Boolean.parseBoolean(strAutoLockReleaseSameNode);
                    } catch (Exception e) {
                        autoLockReleaseSameNode = true;
                        log.warn(
                                "VFS Auto lock removal property not set properly. Current value is : "
                                        + autoLockReleaseSameNode, e);
                    }
                }
            }

        }        
        distributedLock = false;
        distributedLockTimeout = null;
        String strDistributedLock = vfsProperties
                .getProperty(VFSConstants.TRANSPORT_DISTRIBUTED_LOCK);        
        if (strDistributedLock != null) {
            try {
                distributedLock = Boolean.parseBoolean(strDistributedLock);
            } catch (Exception e) {
                autoLockRelease = false;
                log.warn("VFS Distributed lock not set properly. Current value is : "
                        + strDistributedLock, e);
            }

            if (distributedLock) {
                String strDistributedLockTimeout = vfsProperties
                        .getProperty(VFSConstants.TRANSPORT_DISTRIBUTED_LOCK_TIMEOUT);
                if (strDistributedLockTimeout != null) {
                    try {
                        distributedLockTimeout = Long.parseLong(strDistributedLockTimeout);
                    } catch (Exception e) {
                        distributedLockTimeout = null;
                        log.warn(
                                "VFS Distributed lock timeout property not set properly. Current value is : "
                                        + strDistributedLockTimeout, e);
                    }
                }

            }

        }
    }

    private Map<String, String> parseSchemeFileOptions(String fileURI) {
        String scheme = UriParser.extractScheme(fileURI);
        if (scheme == null) {
            return null;
        }
        HashMap<String, String> schemeFileOptions = new HashMap<String, String>();
        schemeFileOptions.put(VFSConstants.SCHEME, scheme);

        try {
            addOptions(scheme, schemeFileOptions);
        } catch (Exception e) {
            log.warn("Error while loading VFS parameter. " + e.getMessage());
        }
        return schemeFileOptions;
    }

    private void addOptions(String scheme, Map<String, String> schemeFileOptions) {
        if (scheme.equals(VFSConstants.SCHEME_SFTP)) {
            for (VFSConstants.SFTP_FILE_OPTION option : VFSConstants.SFTP_FILE_OPTION.values()) {
                String strValue = vfsProperties.getProperty(VFSConstants.SFTP_PREFIX
                        + WordUtils.capitalize(option.toString()));
                if (strValue != null && !strValue.equals("")) {
                    schemeFileOptions.put(option.toString(), strValue);
                }
            }
        }
    }    
    
    /**
     * 
     * Handle directory with chile elements
     * 
     * @param children
     * @return
     * @throws FileSystemException
     */
    private FileObject directoryHandler(FileObject[] children) throws FileSystemException {
        // Process Directory
        lastCycle = 0;
        int failCount = 0;
        int successCount = 0;
        int processCount = 0;

        if (log.isDebugEnabled()) {
            log.debug("File name pattern : "
                    + vfsProperties.getProperty(VFSConstants.TRANSPORT_FILE_FILE_NAME_PATTERN));
        }

        // Sort the files
        String strSortParam = vfsProperties.getProperty(VFSConstants.FILE_SORT_PARAM);
        if (strSortParam != null && !"NONE".equals(strSortParam)) {
            log.debug("Start Sorting the files.");
            String strSortOrder = vfsProperties.getProperty(VFSConstants.FILE_SORT_ORDER);
            boolean bSortOrderAsscending = true;
            if (strSortOrder != null && strSortOrder.toLowerCase().equals("false")) {
                bSortOrderAsscending = false;
            }
            if (log.isDebugEnabled()) {
                log.debug("Sorting the files by : " + strSortOrder + ". (" + bSortOrderAsscending
                        + ")");
            }
            if (strSortParam.equals(VFSConstants.FILE_SORT_VALUE_NAME) && bSortOrderAsscending) {
                Arrays.sort(children, new FileNameAscComparator());
            } else if (strSortParam.equals(VFSConstants.FILE_SORT_VALUE_NAME)
                    && !bSortOrderAsscending) {
                Arrays.sort(children, new FileNameDesComparator());
            } else if (strSortParam.equals(VFSConstants.FILE_SORT_VALUE_SIZE)
                    && bSortOrderAsscending) {
                Arrays.sort(children, new FileSizeAscComparator());
            } else if (strSortParam.equals(VFSConstants.FILE_SORT_VALUE_SIZE)
                    && !bSortOrderAsscending) {
                Arrays.sort(children, new FileSizeDesComparator());
            } else if (strSortParam.equals(VFSConstants.FILE_SORT_VALUE_LASTMODIFIEDTIMESTAMP)
                    && bSortOrderAsscending) {
                Arrays.sort(children, new FileLastmodifiedtimestampAscComparator());
            } else if (strSortParam.equals(VFSConstants.FILE_SORT_VALUE_LASTMODIFIEDTIMESTAMP)
                    && !bSortOrderAsscending) {
                Arrays.sort(children, new FileLastmodifiedtimestampDesComparator());
            }
            log.debug("End Sorting the files.");
        }      
        
        for (FileObject child : children) {
            // skipping *.lock / *.fail file
            if (child.getName().getBaseName().endsWith(".lock")
                    || child.getName().getBaseName().endsWith(".fail")) {
                continue;
            }
            boolean isFailedRecord = VFSUtils.isFailRecord(fsManager, child);
            
            // child's file name matches the file name pattern or process all
            // files now we try to get the lock and process
            if ((strFilePattern == null || child.getName().getBaseName().matches(strFilePattern))
                    && !isFailedRecord) {

                if (log.isDebugEnabled()) {
                    log.debug("Matching file : " + child.getName().getBaseName());
                }

                if ((!fileLock || (fileLock && acquireLock(fsManager, child)))) {
                    // process the file
                    boolean runPostProcess = true;
                    try {
                        if (log.isDebugEnabled()) {
                            log.debug("Processing file :" + child);
                        }
                        processCount++;
                        if (processFile(child) == null) {
                            runPostProcess = false;
                        } else {
                            successCount++;
                        }
                        // tell moveOrDeleteAfterProcessing() file was success
                        lastCycle = 1;
                    } catch (Exception e) {
                        if (e.getCause() instanceof FileNotFoundException) {
                            log.warn("Error processing File URI : " + child.getName()
                                    + ". This can be due to file moved from another process.");
                            runPostProcess = false;
                        } else {
                            log.error("Error processing File URI : " + child.getName(), e);
                            failCount++;
                            // tell moveOrDeleteAfterProcessing() file failed
                            lastCycle = 2;
                        }

                    }
                    // skipping un-locking file if failed to do delete/move
                    // after process
                    boolean skipUnlock = false;
                    if (runPostProcess) {
                        try {
                            moveOrDeleteAfterProcessing(child);
                        } catch (AxisFault axisFault) {
                            log.error("File object '" + child.getURL().toString()
                                    + "'cloud not be moved, will remain in \"locked\" state",
                                    axisFault);
                            skipUnlock = true;
                            failCount++;
                            lastCycle = 3;
                            VFSUtils.markFailRecord(fsManager, child);
                        }
                    }
                    // if there is a failure or not we'll try to release the
                    // lock
                    if (fileLock && !skipUnlock) {
                        // TODO: passing null to avoid build break. Fix properly
                        VFSUtils.releaseLock(fsManager, child, fso);
                    }
                    if (injectHandler == null) {
                        return child;
                    }
                }
            } else if (log.isDebugEnabled() && strFilePattern != null
                    && !child.getName().getBaseName().matches(strFilePattern) && !isFailedRecord) {
                // child's file name does not match the file name pattern
                log.debug("Non-Matching file : " + child.getName().getBaseName());
            } else if (isFailedRecord) {
                // it is a failed record
                try {
                    lastCycle = 1;
                    moveOrDeleteAfterProcessing(child);
                } catch (AxisFault axisFault) {
                    log.error("File object '" + child.getURL().toString()
                            + "'cloud not be moved, will remain in \"fail\" state", axisFault);
                }
                if (fileLock) {
                    // TODO: passing null to avoid build break. Fix properly
                    VFSUtils.releaseLock(fsManager, child, fso);
                    VFSUtils.releaseLock(fsManager, fileObject, fso);
                }
                if (log.isDebugEnabled()) {
                    log.debug("File '" + fileObject.getURL()
                            + "' has been marked as a failed record, it will not " + "process");
                }
            }

            // Manage throttling of file processing
            if (iFileProcessingInterval != null && iFileProcessingInterval > 0) {
                try {
                    if (log.isDebugEnabled()) {
                        log.debug("Put the VFS processor to sleep for : " + iFileProcessingInterval);
                    }
                    Thread.sleep(iFileProcessingInterval);
                } catch (InterruptedException ie) {
                    log.error("Unable to set the interval between file processors." + ie);
                }
            } else if (iFileProcessingCount != null && iFileProcessingCount <= processCount) {
                break;
            }
        }
        if (failCount == 0 && successCount > 0) {
            lastCycle = 1;
        } else if (successCount == 0 && failCount > 0) {
            lastCycle = 4;
        } else {
            lastCycle = 5;
        }
        return null;
    }

    /**
     * Check if the file/folder exists before proceeding and retrying
     */
    private boolean initFileCheck() {
        boolean wasError = true;
        int retryCount = 0;

        fileObject = null;
        while (wasError) {
            try {
                retryCount++;
                fileObject = fsManager.resolveFile(fileURI, fso);
                if (fileObject == null) {
                    log.error("fileObject is null");
                    throw new FileSystemException("fileObject is null");
                }
                wasError = false;
            } catch (FileSystemException e) {
                if (retryCount >= maxRetryCount) {
                    log.error(
                            "Repeatedly failed to resolve the file URI: "
                                    + VFSUtils.maskURLPassword(fileURI), e);
                    return false;
                } else {
                    log.warn("Failed to resolve the file URI: " + VFSUtils.maskURLPassword(fileURI)
                            + ", in attempt " + retryCount + ", " + e.getMessage()
                            + " Retrying in " + reconnectionTimeout + " milliseconds.");
                }
            }
            if (wasError) {
                try {
                    Thread.sleep(reconnectionTimeout);
                } catch (InterruptedException e2) {
                    log.error("Thread was interrupted while waiting to reconnect.", e2);
                }
            }
        }
        return true;
    }

    /**
     * 
     * Acquire distributed lock if required first, then do the file level locking
     * 
     * @param fsManager
     * @param fileObject
     * @return
     */
    private boolean acquireLock(FileSystemManager fsManager, FileObject fileObject) {
        String strContext = fileObject.getName().getURI();
        boolean rtnValue = false;
        try {
            if (distributedLock) {
                if (distributedLockTimeout != null) {
                    if (!ClusteringServiceUtil.setLock(strContext, distributedLockTimeout)) {
                        return false;
                    }
                } else if (!ClusteringServiceUtil.setLock(strContext)) {
                    return false;
                }
            }
            // When processing a directory list is fetched initially. Therefore
            // there is still a chance of file processed by another process.
            // Need to check the source file before processing.
            try {
                FileObject sourceFile = fsManager.resolveFile(strContext);
                if (!sourceFile.exists()) {
                    return false;
                }
            } catch (FileSystemException e) {
                return false;
            }         
            VFSParamDTO vfsParamDTO = new VFSParamDTO();
            vfsParamDTO.setAutoLockRelease(autoLockRelease);
            vfsParamDTO.setAutoLockReleaseSameNode(autoLockReleaseSameNode);
            vfsParamDTO.setAutoLockReleaseInterval(autoLockReleaseInterval);
            rtnValue = VFSUtils.acquireLock(fsManager, fileObject, vfsParamDTO, fso);
        } finally {
            if (distributedLock) {
                ClusteringServiceUtil.releaseLock(strContext);
            }
        }
        return rtnValue;
    }

    /**
     * Actual processing of the file/folder
     * 
     * @param file
     * @return
     * @throws AxisFault
     */
    private FileObject processFile(FileObject file) throws AxisFault {
        try {
            FileContent content = file.getContent();
            String fileName = file.getName().getBaseName();
            String filePath = file.getName().getPath();
            String fileURI = file.getName().getURI();

            if (injectHandler != null) {
                Map<String, Object> transportHeaders = new HashMap<String, Object>();
                transportHeaders.put(VFSConstants.FILE_PATH, filePath);
                transportHeaders.put(VFSConstants.FILE_NAME, fileName);
                transportHeaders.put(VFSConstants.FILE_URI, fileURI);

                try {
                    transportHeaders.put(VFSConstants.FILE_LENGTH, content.getSize());
                    transportHeaders.put(VFSConstants.LAST_MODIFIED, content.getLastModifiedTime());
                } catch (FileSystemException e) {
                    log.warn("Unable to set file length or last modified date header.", e);
                }

                injectHandler.setTransportHeaders(transportHeaders);
                // injectHandler
                if (!injectHandler.invoke(file)) {
                    return null;
                }
            }

        } catch (FileSystemException e) {
            log.error("Error reading file content or attributes : " + file, e);
        }
        return file;
    }

    /**
     * Do the post processing actions
     * 
     * @param fileObject
     * @throws AxisFault
     */
    private void moveOrDeleteAfterProcessing(FileObject fileObject) throws AxisFault {

        String moveToDirectoryURI = null;
        try {
            switch (lastCycle) {
            case 1:
                if ("MOVE".equals(vfsProperties
                        .getProperty(VFSConstants.TRANSPORT_FILE_ACTION_AFTER_PROCESS))) {
                    moveToDirectoryURI = vfsProperties
                            .getProperty(VFSConstants.TRANSPORT_FILE_MOVE_AFTER_PROCESS);
                    //Postfix the date given timestamp format
                    String strSubfoldertimestamp = vfsProperties
                            .getProperty(VFSConstants.SUBFOLDER_TIMESTAMP);
                    if (strSubfoldertimestamp != null) {
                        try {
                            SimpleDateFormat sdf = new SimpleDateFormat(strSubfoldertimestamp);
                            String strDateformat = sdf.format(new Date());
                            int iIndex = moveToDirectoryURI.indexOf("?");
                            if (iIndex > -1) {
                                moveToDirectoryURI = moveToDirectoryURI.substring(0, iIndex)
                                        + strDateformat
                                        + moveToDirectoryURI.substring(iIndex,
                                                moveToDirectoryURI.length());
                            }else{
                                moveToDirectoryURI += strDateformat;
                            }
                        } catch (Exception e) {
                            log.warn("Error generating subfolder name with date", e);
                        }
                    }
                }
                break;

            case 2:
                if ("MOVE".equals(vfsProperties
                        .getProperty(VFSConstants.TRANSPORT_FILE_ACTION_AFTER_FAILURE))) {
                    moveToDirectoryURI = vfsProperties
                            .getProperty(VFSConstants.TRANSPORT_FILE_MOVE_AFTER_FAILURE);
                }
                break;

            default:
                return;
            }

            if (moveToDirectoryURI != null) {                
                FileObject moveToDirectory = fsManager.resolveFile(moveToDirectoryURI, fso);
                String prefix;
                if (vfsProperties.getProperty(VFSConstants.TRANSPORT_FILE_MOVE_TIMESTAMP_FORMAT) != null) {
                    prefix = new SimpleDateFormat(
                            vfsProperties
                                    .getProperty(VFSConstants.TRANSPORT_FILE_MOVE_TIMESTAMP_FORMAT))
                            .format(new Date());
                } else {
                    prefix = "";
                }
                
                //Forcefully create the folder(s) if does not exists
                String strForceCreateFolder = vfsProperties.getProperty(VFSConstants.FORCE_CREATE_FOLDER);
                if(strForceCreateFolder != null && strForceCreateFolder.toLowerCase().equals("true") && !moveToDirectory.exists()){
                    moveToDirectory.createFolder();
                }
                
                FileObject dest = moveToDirectory.resolveFile(prefix
                        + fileObject.getName().getBaseName());
                if (log.isDebugEnabled()) {
                    log.debug("Moving to file :" + dest.getName().getURI());
                }
                try {
                    fileObject.moveTo(dest);
                    if (VFSUtils.isFailRecord(fsManager, fileObject)) {
                        VFSUtils.releaseFail(fsManager, fileObject);
                    }
                } catch (FileSystemException e) {
                    if (!VFSUtils.isFailRecord(fsManager, fileObject)) {
                        VFSUtils.markFailRecord(fsManager, fileObject);
                    }
                    log.error("Error moving file : " + fileObject + " to " + moveToDirectoryURI, e);
                }
            } else {
                try {
                    if (log.isDebugEnabled()) {
                        log.debug("Deleting file :" + fileObject);
                    }
                    fileObject.close();
                    if (!fileObject.delete()) {
                        String msg = "Cannot delete file : " + fileObject;
                        log.error(msg);
                        throw new AxisFault(msg);
                    }
                } catch (FileSystemException e) {
                    log.error("Error deleting file : " + fileObject, e);
                }
            }
        } catch (FileSystemException e) {
            if (!VFSUtils.isFailRecord(fsManager, fileObject)) {
                VFSUtils.markFailRecord(fsManager, fileObject);
                log.error("Error resolving directory to move after processing : "
                        + moveToDirectoryURI, e);
            }
        }
    }
    /**
     * Comparator classed used to sort the files according to user input
     * */
    class FileNameAscComparator implements Comparator<FileObject> {
        @Override
        public int compare(FileObject o1, FileObject o2) {
            return o1.getName().compareTo(o2.getName());
        }
    }

    class FileLastmodifiedtimestampAscComparator implements Comparator<FileObject> {
        @Override
        public int compare(FileObject o1, FileObject o2) {
            Long lDiff = 0l;
            try {
                lDiff = o1.getContent().getLastModifiedTime()
                        - o2.getContent().getLastModifiedTime();
            } catch (FileSystemException e) {
                log.warn("Unable to compare lastmodified timestamp of the two files.", e);
            }
            return lDiff.intValue();
        }
    }

    class FileSizeAscComparator implements Comparator<FileObject> {
        @Override
        public int compare(FileObject o1, FileObject o2) {
            Long lDiff = 0l;
            try {
                lDiff = o1.getContent().getSize() - o2.getContent().getSize();
            } catch (FileSystemException e) {
                log.warn("Unable to compare size of the two files.", e);
            }
            return lDiff.intValue();
        }
    }

    class FileNameDesComparator implements Comparator<FileObject> {
        @Override
        public int compare(FileObject o1, FileObject o2) {
            return o2.getName().compareTo(o1.getName());
        }
    }

    class FileLastmodifiedtimestampDesComparator implements Comparator<FileObject> {
        @Override
        public int compare(FileObject o1, FileObject o2) {
            Long lDiff = 0l;
            try {
                lDiff = o2.getContent().getLastModifiedTime()
                        - o1.getContent().getLastModifiedTime();
            } catch (FileSystemException e) {
                log.warn("Unable to compare lastmodified timestamp of the two files.", e);
            }
            return lDiff.intValue();
        }
    }

    class FileSizeDesComparator implements Comparator<FileObject> {
        @Override
        public int compare(FileObject o1, FileObject o2) {
            Long lDiff = 0l;
            try {
                lDiff = o2.getContent().getSize() - o1.getContent().getSize();
            } catch (FileSystemException e) {
                log.warn("Unable to compare size of the two files.", e);
            }
            return lDiff.intValue();
        }
    }     
    
}
