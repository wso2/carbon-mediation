This is a C++ implementation of the CG Agent. Usage is given below. 
Usage: ./CGAgent user-name pass-word host port client-key-location client-certficate-location server-trust-store-location EPR-URL 

The required command line options has the following meaning,
user-name - the user name of the user in remote CG server
pass-word - the pass word of the user in remote CG server
host - the host name of the underline Thrift server in remote CG server
port - the port of the underline Thrift server in remote CG server(where the underlin)
client-key-location - the location of the client private key
client-certificate-location - the location of the client certficate key
server-trust-store-location - the location of the trust store that contains server's public key
EPR-URL - the endpoint defenition of the public CG service(proxy service endpoint)

Example:
./CGAgent admin admin 10.100.3.121 15001 /home/rajika/cg/jira-issues/keys/client-key.pem /home/rajika/cg/jira-issues/keys/client-cert.pem /home/rajika/cg/jira-issues/keys/server-cert.pem cg://server1/SimpleStockQuoteService

This contais the following files and folders.
1. README.txt - This file.
2. CGAgent.cpp - The source code of the C++ CG Agent. 
3. gen - The generated source code(API) from the Thrift interface definition file for C++ CG Agent.
4. Makefile - The build file.
5. build-keys.sh - The script which can be used to generate keys. This assume that you have
ImportKey tool as in http://www.agentbob.info/agentbob/79-AB.html.
