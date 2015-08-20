// An C++ implementation of the WSO2 Cloud Gateway Agent, this only has limited functionality. 
// It first log into public WSO2 Cloud Gateway(CG) poll the messages in a busy loop. Once the messages
// are avilable those will be logged into the STDOUT and a fixed response will be sent back to the 
// public WSO2 CG server. This should be extended to invoke the exisitng infrasture to invoke the 
// actual Web service to get the actual response.
// compiles with g++ with -c -g -Wall -DHAVE_INTTYPES_H -DHAVE_NETINET_IN_H

#define __STDC_FORMAT_MACROS
#include <inttypes.h>

#include <iostream>
#include <unistd.h>
#include <sys/time.h>
#include <stdlib.h>

#include <thrift/protocol/TBinaryProtocol.h>
#include <thrift/transport/TTransportUtils.h>
#include <thrift/transport/TSSLSocket.h>
#include <thrift/transport/TSocket.h>
#include <boost/shared_ptr.hpp>

#include "gen/CloudGatewayService.h"

#define BUF_SIZE 5  //the default buffer size of request/response messages

using namespace std;
using namespace boost;
using namespace apache::thrift;
using namespace apache::thrift::protocol;
using namespace apache::thrift::transport;

using namespace cg;


// the fixed response for request message
static string FIXED_RESPONSE =
 "<?xml version='1.0' encoding='UTF-8'?><soapenv:Envelope xmlns:soapenv=\"http://schemas.xmlsoap.org/soap/envelope/\"><soapenv:Body><ns:getQuoteResponse xmlns:ns=\"http://services.samples\"><ns:return xmlns:ax21=\"http://services.samples/xsd\" xmlns:xsi=\"http://www.w3.org/2001/XMLSchema-instance\" xsi:type=\"ax21:GetQuoteResponse\"><ax21:change>4.488785896287975</ax21:change><ax21:earnings>-8.497016143149477</ax21:earnings><ax21:high>81.76449382306811</ax21:high><ax21:last>78.4043278222019</ax21:last><ax21:lastTradeTimestamp>Thu Oct 13 17:15:45 IST 2011</ax21:lastTradeTimestamp><ax21:low>-78.18527519869033</ax21:low><ax21:marketCap>-7085449.084227343</ax21:marketCap><ax21:name>IBM Company</ax21:name><ax21:open>81.49020519650473</ax21:open><ax21:peRatio>25.32800360420289</ax21:peRatio><ax21:percentageChange>-5.996376774090883</ax21:percentageChange><ax21:prevClose>-74.85830302864058</ax21:prevClose><ax21:symbol>IBM</ax21:symbol><ax21:volume>17144</ax21:volume></ns:return></ns:getQuoteResponse></soapenv:Body></soapenv:Envelope>";

static string FIXED_JSON_RESPONSE =
"{\"Jobs\":{\"FileId\":\"file123\",\"EnduserId\":\"end_user401\",\"ApplicationId\":\"cspapp1101\"}}";


static void processRequestMessage(vector<Message> & requestMessages, vector<Message> & responseMessages){
	unsigned int i;
	Message message;
 	vector<Message> processedMessages;
	map<string, string>::iterator itr;

	cout << "request message buffer size : " << requestMessages.size() << endl;
	for(i = 0; i < requestMessages.size();i++){
		message = requestMessages[i];
		cout << endl;
		cout << "dumping message " << i+1 << endl;
		cout << "================= " << endl;
		// dump everything from the API
		cout << "message id -> " << message.messageId << endl;
		cout << "content type -> " << message.contentType << endl;
		cout << "request message -> " << endl << message.message << endl;
		cout << "soap action -> " << message.soapAction << endl;
		cout << "request URI -> " << message.requestURI << endl;
		cout << "is doing rest? -> " << message.isDoingREST << endl;
		cout << "HTTP method -> " << message.httpMethod << endl;
		cout << "HTTP headers -> " << endl;	
		for(itr = message.transportHeaders.begin(); itr != message.transportHeaders.end(); itr++){
			cout << itr->first << ":" << itr->second << endl;
		}

		// set the process messages
		Message response;

		// set the request message Id as the response message Id
		// this is required for correctly correlating the request message with response
		response.messageId = message.messageId;  

		//FIME: use the fixed response, this should be replaced with the existing infrasture to invoke the 
		//response.message = FIXED_RESPONSE;
		response.message = FIXED_JSON_RESPONSE;
		
		// use the incoming content type as the response content type
		response.contentType = message.contentType; 
		responseMessages.push_back(response);
	}	
}

int main(int argc, char **argv){

	if(argc != 9){
		cout << "Usage: ./CGAgent user-name pass-word host port client-key-location client-certficate-location server-trust-store-location EPR-URL" << endl;	
		return 1;
	}

	// holds the secure token need to read the buffers that contains request messages
	string token;

	// the host name of  the remoate WSO2 Cloud Gateway server.
	// the Java Thrift server runs on this port
	string host = argv[3]; 
	
	// the Java Thrift server port 
	int port = atoi(argv[4]); 

	// the key of the buffer that holds request messages for the service SimpleStockQuoteService,
	// this is normally calcualted when using the Agent UI, and need to calcualte manually when
	// using the C++ Agent. This key SHOULD be used as the endpoint in the public CG service
	// (proxy service) configuration. See below for example. 

    /*
    <?xml version="1.0" encoding="UTF-8"?>
    <proxy xmlns="http://ws.apache.org/ns/synapse" 
		name="SimpleStockQuoteService" startOnLoad="true" trace="disable">
    <target>
        <endpoint>
            <address uri="cg://server1/SimpleStockQuoteService">
                <suspendOnFailure>
                    <errorCodes>400207</errorCodes>
                    <initialDuration>1000</initialDuration>
                    <progressionFactor>2.0</progressionFactor>
                    <maximumDuration>64000</maximumDuration>
                </suspendOnFailure>
            </address>
        </endpoint>
        <inSequence>
            <class name="org.wso2.carbon.cloud.gateway.CGMEPHandlingMediator"/>
            <property name="transportNonBlocking" scope="axis2" action="remove"/>
            <property name="preserveProcessedHeaders" value="true"/>
        </inSequence>
        <outSequence>
            <send/>
        </outSequence>
        <faultSequence>
            <log level="full"/>
            <drop/>
        </faultSequence>
    </target>
		<publishWSDL uri="file:../sample_proxy_1.wsdl"/>
	</proxy>
	*/
	const string queueName = argv[8]; //example cg://server1/SimpleStockQuoteService

	// the list of request messages 
	vector<Message> requestMessages; 

	// the list of response messages(once the request message is processed)
	vector<Message> responseMessages;

	try{
		cout << "host:port -> " << host <<":" << port << endl;
		cout << "buffer key -> " << queueName << endl;
		cout << "using client key -> " << argv[5] << endl;
		cout << "using client certficate -> " << argv[6] << endl;
		cout << "using server trust store -> " << argv[7] << endl;
	
		boost::shared_ptr<TSSLSocketFactory> factory(new TSSLSocketFactory());

		// example /home/rajika/customer/jira-issues/mentor-graphics/keys/client-key-office.pem
		factory->loadPrivateKey(argv[5]);

		// example /home/rajika/customer/jira-issues/mentor-graphics/keys/client-cert-office.pem
		factory->loadCertificate(argv[6]);

		factory->authenticate(true);
		factory->ciphers("HIGH:!DSS:!aNULL@STRENGTH");  

		// example /home/rajika/customer/jira-issues/mentor-graphics/keys/server-cert-office.pem
		factory->loadTrustedCertificates(argv[7]); 

    	boost::shared_ptr<TSocket> socket = factory->createSocket( host, port);
		boost::shared_ptr<TTransport> transport(new TBufferedTransport(socket));
		boost::shared_ptr<TProtocol> protocol(new TBinaryProtocol(transport));
		cout << "protocol initilized" << endl;

		CloudGatewayServiceClient client(protocol);
		cout << "client initilized" << endl;

		transport->open();
		cout << "transport opened" << endl;

		// log into the remote Thrift server in WSO2 Cloud Gateway and retrive the token 
		client.login(token, argv[1], argv[2], queueName);
		cout << "secure token -> " << token << endl;

		// poll in a busy loop
		// this client is for demostration purposes, you can extend it so that it can be a multi-thread 
		// one to process the request messages and response messages in their respective threads for 
		// better performance
		while(true){
			vector<Message> drainsResponseMessages(responseMessages);
			responseMessages.clear();

			// exchange operation will hand over any response messages(i.e. processed by the actual 
			// back end service) to the Thrift server and will hand over any request messages to process by 
			// the actual back end service)
			client.exchange(requestMessages, drainsResponseMessages, BUF_SIZE, token);	
			
			if(requestMessages.size() > 0){
			// if there are messages we'll block and process them but a more 
			// suitable way to do the processing would be to hand over the processing 
			// to seperate worker threads
				processRequestMessage(requestMessages, responseMessages);	
			}
		}

		transport->close();
		cout << "done!" << endl;

		return 0;

	} catch(std::exception& e){
		cerr << "ERROR : " << e.what() << endl;
		return 1;
	}
}
