namespace java org.wso2.carbon.cloud.gateway.common.thrift.gen

/* A Message consists of followings
 * messageId - a unique ID for co-relation
 * contentType - to handover the message to correct message builder at the back end
 * message - the binary message content ( i.e. the SOAP Envelope as a byte array)
 * soapAction - to handover the message for dispatching by back end
 * epoch - to track dead messages for clean up task
 * transportHeaders - request/response http transport headers
 * requestURI - the resource uri
 * isDoingREST - is this a REST request?
 * httpMethod - the http method of this message
 */
struct Message {
    1: string messageId;
    2: string contentType;
    3: binary message;
    4: string soapAction;
    5: i64 epoch;
    6: map<string, string> transportHeaders;
    7: string requestURI;
    8: bool isDoingREST;
    9: string httpMethod;
    10: bool isDoingMTOM;
    11: bool isDoingSwA;
}

/*
    Throws in case of an illegal access to the server buffers
*/
exception NotAuthorizedException {
    1: string message;
}


/*
    The CSG service which can be consumed to login to access the buffers and exchange the buffers.
*/
service CloudGatewayService {
    /* The login operation. A user can specify username and the password to authorize to this queue
       given by queueName and upon successful login a SecureRandom token wil be given. This token
       is required for successive exchanged operations. If the login fails a detailed exception
       will be given for trouble shooting.
    */
    string login(1: string userName, 2: string password, 3: string queueName)
        throws (1: NotAuthorizedException e),

    /* The exchange operation will copy the client src buffer(which is the response buffer of client)
       into server and server will return the messages(of size 'size') in its input buffer to the
       client. The actual buffer will be looked up using the provided token.
    */
    list<Message> exchange(1: list<Message> src, 2: i32 size, 3: string token)
        throws (1: NotAuthorizedException e)
}