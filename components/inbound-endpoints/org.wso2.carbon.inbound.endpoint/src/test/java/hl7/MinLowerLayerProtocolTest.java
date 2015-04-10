package hl7;

import ca.uhn.hl7v2.DefaultHapiContext;
import ca.uhn.hl7v2.HL7Exception;
import ca.uhn.hl7v2.HapiContext;
import ca.uhn.hl7v2.app.Connection;
import ca.uhn.hl7v2.app.Initiator;
import ca.uhn.hl7v2.app.SimpleServer;
import ca.uhn.hl7v2.model.Message;
import ca.uhn.hl7v2.model.v26.message.ADT_A01;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.charset.Charset;

import static org.junit.Assert.*;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;

public class MinLowerLayerProtocolTest {
    private static final Logger log = LoggerFactory.getLogger(MinLowerLayerProtocolTest.class);


    private MLLPContext context;
    private HL7Codec codec;
    private String message;
    private byte [] llpEncodedMessage;
    private ByteBuffer req;

    @Before
    public void config() throws Exception {
        System.setProperty("ca.uhn.hl7v2.llp.logBytesRead", "FALSE");
        System.setProperty("ca.uhn.hl7v2.util.status.out", "");
        context = new MLLPContext();
        codec = context.getCodec();
        message = "MSH|^~\\&|||||20150403091225.929+0530||ADT^A01^ADT_A01|208601|T|2.6";
        llpEncodedMessage = (MllpTestConstants.START_BYTE + message + MllpTestConstants.END_BYTE1 + MllpTestConstants.END_BYTE1 + MllpTestConstants.END_BYTE2).getBytes();
        req = ByteBuffer.allocate(llpEncodedMessage.length);
        req.put(llpEncodedMessage);
    }

    @After
    public void cleanup() throws Exception {
        context = null;
        codec = null;
        message = null;
        llpEncodedMessage = null;
    }


    /**
     * Testing constructor
     */
    @Test
    public void testConstructor() {
        assertNotNull("Codec object present", codec);
    }

    /**
     * Testing decode
     */
    @Test
    public void testDecode() throws HL7Exception, IOException, MLLProtocolException {
        assertNull(context.getHl7Message());
        codec.decode(req, context);
        req.flip();
        assertNotNull("Should have encoded HL7 message", context.getHl7Message());
    }

    /**
     * Testing encode
     */
    @Test
    public void testEncode() throws HL7Exception, IOException, MLLProtocolException {
        assertNull(context.getHl7Message());
        codec.decode(req, context);
        req.flip();
        assertNotNull(context.getHl7Message());
        ByteBuffer response = codec.encode(context.getHl7Message(), context);
        byte[] ack = new byte[response.capacity()];
        response.get(ack, 0, response.capacity());
        String v = new String( ack, Charset.forName("UTF-8"));
        log.info("ACK: " + v);
        assertTrue(v.contains("ACK^A01^ACK"));
    }

    @Test
    public void testReceiveWithDelayInBetween() throws Exception {

        int port = 9090;
        Listener listener = new Listener(port);

        try {
            listener.start();
        } catch (IOException e) {
            e.printStackTrace();
        }

        Thread.sleep(3000);

        HapiContext context = new DefaultHapiContext();
        Connection c = context.newClient("127.0.0.1", port, false);
        Initiator initiator = c.getInitiator();

        ADT_A01 msg = new ADT_A01();
        msg.initQuickstart("ADT", "A01", "T");
        Message resp1 = initiator.sendAndReceive(msg);
        log.info(resp1.encode());
        assertNotNull(resp1);

        Thread.sleep(SimpleServer.SO_TIMEOUT + 500);

        msg.initQuickstart("ADT", "A01", "T");
        Message resp2 = initiator.sendAndReceive(msg);
        log.info(resp2.encode());
        assertNotNull(resp2);

        c.close();
        Thread.sleep(SimpleServer.SO_TIMEOUT + 500);

        listener.shutdownReactor();
    }
}
