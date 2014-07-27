package org.wso2.carbon.inbound.endpoint.protocol.http.core.impl;


import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.http.HttpException;
import org.apache.http.nio.ContentDecoder;
import org.apache.http.nio.ContentEncoder;
import org.apache.http.nio.NHttpServerConnection;
import org.apache.http.nio.NHttpServerEventHandler;
import org.apache.http.nio.util.ContentOutputBuffer;
import org.apache.http.nio.util.HeapByteBufferAllocator;
import org.apache.http.nio.util.SimpleOutputBuffer;
import org.apache.http.protocol.HttpContext;
import org.apache.synapse.core.SynapseEnvironment;
import org.apache.synapse.transport.passthru.PassThroughConstants;
import org.apache.synapse.transport.passthru.ProtocolState;
import org.apache.synapse.transport.passthru.SourceContext;
import org.apache.synapse.transport.passthru.SourceResponse;
import org.wso2.carbon.inbound.endpoint.protocol.http.utils.InboundConfiguration;
import org.wso2.carbon.inbound.endpoint.protocol.http.utils.InboundHttpConstants;
import org.wso2.carbon.inbound.endpoint.protocol.http.utils.InboundThreadFactory;

import java.io.IOException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class InboundHttpSourceHandler  implements NHttpServerEventHandler {

    protected Log log = LogFactory.getLog(this.getClass());

    private SynapseEnvironment synapseEnvironment;
    private String injectSeq;
    private String faultSeq;
    private String outSequence;
    private InboundConfiguration inboundConfiguration;
    private ExecutorService executorService;

   public InboundHttpSourceHandler(InboundConfiguration inboundConfiguration,SynapseEnvironment synapseEnvironment, String injectSeq, String faultSeq,String outSequence){
       this.synapseEnvironment=synapseEnvironment;
       this.injectSeq=injectSeq;
       this.faultSeq=faultSeq;
       this.outSequence=outSequence;
       this.inboundConfiguration=inboundConfiguration;
       this.executorService =Executors.newFixedThreadPool(InboundHttpConstants.WORKER_POOL_SIZE, new InboundThreadFactory("request"));
   }


    public void connected(NHttpServerConnection nHttpServerConnection) throws IOException, HttpException {
        inboundConfiguration.getSourceConnections().addConnection(nHttpServerConnection);
        InboundSourceContext.create(nHttpServerConnection, ProtocolState.REQUEST_READY, inboundConfiguration);
    }

    public void requestReceived(NHttpServerConnection nHttpServerConnection) throws IOException, HttpException {
        log.info("requestReceived");
       try{
        HttpContext _context = nHttpServerConnection.getContext();
        _context.setAttribute(PassThroughConstants.REQ_ARRIVAL_TIME, System.currentTimeMillis());

        if (!InboundSourceContext.assertState(nHttpServerConnection, ProtocolState.REQUEST_READY) && !InboundSourceContext.assertState(nHttpServerConnection, ProtocolState.WSDL_RESPONSE_DONE)) {
          //  handleInvalidState(conn, "Request received");
            return;
        }
        // we have received a message over this connection. So we must inform the pool
        inboundConfiguration.getSourceConnections().useConnection(nHttpServerConnection);

        // at this point we have read the HTTP Headers
           InboundSourceContext.updateState(nHttpServerConnection, ProtocolState.REQUEST_HEAD);

           InboundHttpSourceRequest request = new InboundHttpSourceRequest(
                inboundConfiguration, nHttpServerConnection.getHttpRequest(), nHttpServerConnection);

           InboundSourceContext.setRequest(nHttpServerConnection, request);
           request.setInjectSeq(injectSeq);
           request.setOutSeq(outSequence);
           request.setFaultSeq(faultSeq);

        request.start(nHttpServerConnection);



        /******/
        String method = request.getRequest() != null ? request.getRequest().getRequestLine().getMethod().toUpperCase():"";
        if ("GET".equals(method) || "HEAD".equals(method)) {
            HttpContext context = request.getConnection().getContext();
            ContentOutputBuffer outputBuffer = new SimpleOutputBuffer(8192,	new HeapByteBufferAllocator());
            // ContentOutputBuffer outputBuffer
            // = new SharedOutputBuffer(8192, conn, new
            // HeapByteBufferAllocator());
            context.setAttribute("synapse.response-source-buffer",outputBuffer);
           // os = new ContentOutputStream(outputBuffer);
        }

        executorService.execute(
                new InboundHttpSourceRequestWorker(request, inboundConfiguration,synapseEnvironment));
    } catch (HttpException e) {
        log.error(e.getMessage(), e);
           InboundSourceContext.updateState(nHttpServerConnection, ProtocolState.CLOSED);
           inboundConfiguration.getSourceConnections().shutDownConnection(nHttpServerConnection, true);
    } catch (IOException e) {
           log.error(e.getMessage(), e);
           InboundSourceContext.updateState(nHttpServerConnection, ProtocolState.CLOSED);
           inboundConfiguration.getSourceConnections().shutDownConnection(nHttpServerConnection, true);
    }
}


    public void inputReady(NHttpServerConnection nHttpServerConnection, ContentDecoder contentDecoder) throws IOException, HttpException {
        try {
            ProtocolState protocolState = InboundSourceContext.getState(nHttpServerConnection);

            if (protocolState != ProtocolState.REQUEST_HEAD
                    && protocolState != ProtocolState.REQUEST_BODY) {
             //   handleInvalidState(conn, "Request message body data received");
                log.error("Error In Input Ready");
                return;
            }

            InboundSourceContext.updateState(nHttpServerConnection, ProtocolState.REQUEST_BODY);

            InboundHttpSourceRequest request = InboundSourceContext.getRequest(nHttpServerConnection);

            int readBytes = request.read(nHttpServerConnection, contentDecoder);

        } catch (IOException e) {
           // logIOException(conn, e);

           // informReaderError(conn);
           log.error(e.getMessage(),e);
            InboundSourceContext.updateState(nHttpServerConnection, ProtocolState.CLOSED);
            inboundConfiguration.getSourceConnections().shutDownConnection(nHttpServerConnection, true);
        }

    }

    public void responseReady(NHttpServerConnection nHttpServerConnection) throws IOException, HttpException {
        try {
            ProtocolState protocolState = InboundSourceContext.getState(nHttpServerConnection);
            if (protocolState.compareTo(ProtocolState.REQUEST_DONE) < 0) {
                return;
            }

            if (protocolState.compareTo(ProtocolState.CLOSING) >= 0) {
                return;
            }

            if (protocolState != ProtocolState.REQUEST_DONE) {
               // handleInvalidState(conn, "Writing a response");
                log.error("Invalid Sate");
                return;
            }

            // because the duplex nature of http core we can reach hear without a actual response
            InboundHttpSourceResponse response = InboundSourceContext.getResponse(nHttpServerConnection);
            if (response != null) {
                response.start(nHttpServerConnection);

//                metrics.incrementMessagesSent();
//                if (!response.hasEntity()) {
//                    // Update stats as outputReady will not be triggered for no entity responses
//                    updateStatistics(conn);
//                }
            }
        } catch (IOException e) {
           // logIOException(conn, e);

           // informWriterError(conn);

            InboundSourceContext.updateState(nHttpServerConnection, ProtocolState.CLOSING);
            inboundConfiguration.getSourceConnections().shutDownConnection(nHttpServerConnection, true);
        } catch (HttpException e) {
            log.error(e.getMessage(), e);

          ///  informWriterError(conn);

            InboundSourceContext.updateState(nHttpServerConnection, ProtocolState.CLOSING);
            inboundConfiguration.getSourceConnections().shutDownConnection(nHttpServerConnection, true);
        }
    }

    public void outputReady(NHttpServerConnection nHttpServerConnection, ContentEncoder contentEncoder) throws IOException, HttpException {
        try {
            ProtocolState protocolState = InboundSourceContext.getState(nHttpServerConnection);

            //special case to handle WSDLs
            if(protocolState == ProtocolState.WSDL_RESPONSE_DONE){
                // we need to shut down if the shutdown flag is set
                HttpContext context = nHttpServerConnection.getContext();
                ContentOutputBuffer outBuf = (ContentOutputBuffer) context.getAttribute(
                        "synapse.response-source-buffer");
                int bytesWritten = outBuf.produceContent(contentEncoder);


                nHttpServerConnection.requestInput();
                if(outBuf instanceof SimpleOutputBuffer && !((SimpleOutputBuffer)outBuf).hasData()){
                    inboundConfiguration.getSourceConnections().releaseConnection(nHttpServerConnection);
                }

                return;
            }


            if (protocolState != ProtocolState.RESPONSE_HEAD
                    && protocolState != ProtocolState.RESPONSE_BODY) {
                log.warn("Illegal incoming connection state: "
                        + protocolState + " . Possibly two send backs " +
                        "are happening for the same request");

               // handleInvalidState(conn, "Trying to write response body");
                return;
            }

            InboundSourceContext.updateState(nHttpServerConnection, ProtocolState.RESPONSE_BODY);

            InboundHttpSourceResponse response = InboundSourceContext.getResponse(nHttpServerConnection);

            int bytesSent = response.write(nHttpServerConnection, contentEncoder);

            if (contentEncoder.isCompleted()) {
               // updateStatistics(conn);
            }

           // metrics.incrementBytesSent(bytesSent);
        } catch (IOException e) {
         //   logIOException(conn, e);
//
          //  informWriterError(conn);

            InboundSourceContext.updateState(nHttpServerConnection, ProtocolState.CLOSING);
            inboundConfiguration.getSourceConnections().shutDownConnection(nHttpServerConnection, true);
        }
    }

    public void endOfInput(NHttpServerConnection nHttpServerConnection) throws IOException {

    }

    public void timeout(NHttpServerConnection nHttpServerConnection) throws IOException {

    }

    public void closed(NHttpServerConnection nHttpServerConnection) {

    }

    public void exception(NHttpServerConnection nHttpServerConnection, Exception e) {

    }
}
