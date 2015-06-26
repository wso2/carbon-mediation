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

import java.io.InputStream;
import java.util.Map;
import java.util.Properties;

import javax.mail.internet.ContentType;
import javax.mail.internet.ParseException;

import org.apache.axiom.om.OMElement;
import org.apache.axiom.om.util.UUIDGenerator;
import org.apache.axis2.Constants;
import org.apache.axis2.builder.Builder;
import org.apache.axis2.builder.BuilderUtil;
import org.apache.axis2.builder.SOAPBuilder;
import org.apache.axis2.context.MessageContext;
import org.apache.axis2.format.DataSourceMessageBuilder;
import org.apache.axis2.format.ManagedDataSource;
import org.apache.axis2.format.ManagedDataSourceFactory;
import org.apache.axis2.transport.TransportUtils;
import org.apache.commons.io.input.AutoCloseInputStream;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.commons.vfs2.FileObject;
import org.apache.synapse.SynapseException;
import org.apache.synapse.commons.vfs.FileObjectDataSource;
import org.apache.synapse.commons.vfs.VFSConstants;
import org.apache.synapse.core.SynapseEnvironment;
import org.apache.synapse.mediators.base.SequenceMediator;
import org.wso2.carbon.context.PrivilegedCarbonContext;
import org.wso2.carbon.utils.multitenancy.MultitenantConstants;

public class FileInjectHandler {

	private static final Log log = LogFactory.getLog(FileInjectHandler.class);
	
	private String injectingSeq;
	private String onErrorSeq;
	private boolean sequential;
    private Properties vfsProperties;
    private SynapseEnvironment synapseEnvironment;
    private Map<String, Object> transportHeaders;
    
	public FileInjectHandler(String injectingSeq, String onErrorSeq, boolean sequential, SynapseEnvironment synapseEnvironment, Properties vfsProperties){
		this.injectingSeq = injectingSeq;
		this.onErrorSeq = onErrorSeq;
		this.sequential = sequential;
		this.synapseEnvironment = synapseEnvironment;
		this.vfsProperties = vfsProperties;
	}	 
	/**
	 * Inject the message to the sequence
	 * */
	public boolean invoke(Object object)throws SynapseException{
		
		ManagedDataSource dataSource = null;;
		FileObject file = (FileObject)object;
        try {
            org.apache.synapse.MessageContext msgCtx = createMessageContext();
            String contentType = vfsProperties.getProperty(VFSConstants.TRANSPORT_FILE_CONTENT_TYPE);
            if (contentType == null || contentType.trim().equals("")) {
                if (file.getName().getExtension().toLowerCase().endsWith("xml")) {
                    contentType = "text/xml";
                } else if (file.getName().getExtension().toLowerCase().endsWith("txt")) {
                    contentType = "text/plain";
                }
            } else {
                // Extract the charset encoding from the configured content type and
                // set the CHARACTER_SET_ENCODING property as e.g. SOAPBuilder relies on this.
                String charSetEnc = null;
                try {
                    if (contentType != null) {
                        charSetEnc = new ContentType(contentType).getParameter("charset");
                    }
                } catch (ParseException ex) {
                    // ignore
                }
                msgCtx.setProperty(Constants.Configuration.CHARACTER_SET_ENCODING, charSetEnc);
            }         	
            if (log.isDebugEnabled()) {
                log.debug("Processed file : " + file + " of Content-type : " + contentType);
            }             
    		MessageContext axis2MsgCtx = ((org.apache.synapse.core.axis2.Axis2MessageContext)msgCtx).getAxis2MessageContext();
    		// Determine the message builder to use
            Builder builder;
            if (contentType == null) {
                log.debug("No content type specified. Using SOAP builder.");
                builder = new SOAPBuilder();
            } else {
                int index = contentType.indexOf(';');
                String type = index > 0 ? contentType.substring(0, index) : contentType;
                builder = BuilderUtil.getBuilderFromSelector(type, axis2MsgCtx);
                if (builder == null) {
                    if (log.isDebugEnabled()) {
                        log.debug("No message builder found for type '" + type +
                                "'. Falling back to SOAP.");
                    }
                    builder = new SOAPBuilder();
                }
            }
    
            // set the message payload to the message context
            InputStream in;            
            String streaming = vfsProperties.getProperty(VFSConstants.STREAMING);
            
            if (builder instanceof DataSourceMessageBuilder && "true".equals(streaming)) {
                in = null;
                dataSource = ManagedDataSourceFactory.create(
                        new FileObjectDataSource(file, contentType));
                dataSource = null;
            } else {
                in = new AutoCloseInputStream(file.getContent().getInputStream());
                dataSource = null;
            }
        
            //Inject the message to the sequence.

            OMElement documentElement;
            if (in != null) {
                documentElement = builder.processDocument(in, contentType, axis2MsgCtx);
            } else {
                documentElement = ((DataSourceMessageBuilder)builder).processDocument(dataSource, contentType, axis2MsgCtx);
            }
            msgCtx.setEnvelope(TransportUtils.createSOAPEnvelope(documentElement));
            if (injectingSeq == null || injectingSeq.equals("")) {
                log.error("Sequence name not specified. Sequence : " + injectingSeq);
            }
            SequenceMediator seq = (SequenceMediator) synapseEnvironment.getSynapseConfiguration().getSequence(injectingSeq);            
            if (seq != null) {                
                if (log.isDebugEnabled()) {
                    log.debug("injecting message to sequence : " + injectingSeq);
                }
                seq.setErrorHandler(onErrorSeq);
                if(!synapseEnvironment.injectInbound(msgCtx, seq, sequential)){
                    return false;
                }
            } else {
                log.error("Sequence: " + injectingSeq + " not found");
            }     
        } catch (SynapseException se) {
            throw se;
        } catch (Exception e) {
            log.error("Error while processing the file/folder", e);
            throw new SynapseException("Error while processing the file/folder", e);
        } finally {
         if(dataSource != null) {
				dataSource.destroy();
			}
        }
        return true;
	}
	
    /**
     * @param transportHeaders the transportHeaders to set
     */
    public void setTransportHeaders(Map<String, Object> transportHeaders) {
        this.transportHeaders = transportHeaders;
    }
    /**
     * Create the initial message context for the file
     * */
    private org.apache.synapse.MessageContext createMessageContext() {
        org.apache.synapse.MessageContext msgCtx = synapseEnvironment.createMessageContext();
        MessageContext axis2MsgCtx = ((org.apache.synapse.core.axis2.Axis2MessageContext)msgCtx).getAxis2MessageContext();
        axis2MsgCtx.setServerSide(true);
        axis2MsgCtx.setMessageID(UUIDGenerator.getUUID());
        axis2MsgCtx.setProperty(MessageContext.TRANSPORT_HEADERS, transportHeaders);
        PrivilegedCarbonContext carbonContext = PrivilegedCarbonContext.getThreadLocalCarbonContext();
        axis2MsgCtx.setProperty(MultitenantConstants.TENANT_DOMAIN, carbonContext.getTenantDomain());
        // There is a discrepency in what I thought, Axis2 spawns a nes threads to
        // send a message is this is TRUE - and I want it to be the other way
        msgCtx.setProperty(MessageContext.CLIENT_API_NON_BLOCKING, true);
       
        return msgCtx;
    }
    
}

