
Running the Sample Scenario
---------------------------
* WSO2 ESB acts as the intermediary between the client application and the Paypal server.

Message Mediation
-----------------
* In this example, the WSO2 ESB Paypal Proxy Service accepts a SOAP request, which Paypal API non-complience, using this the mediator extracts the required info to 
  construct a SOAP request, which is Paypal Service complient and mediates to the Paypal Service hosted in the Paypal sandbox environment. 
* This sample will use the GetBalance API operation from the PaypalAPI [1].
* This guide demonstrates the sample application covering the basic and the most common usage scenarios of the WSO2 ESB/Paypal mediator(exposed as a Proxy services)
  You will be guided through a step-by-step approach to build and deploy the mediator and configure ESB to run the sample.
  
  Content:
  1. Prerequisites
  2. Build and Deploy the Paypal Mediator
  3. Configure and Starting the WSO2ESB
  4. Running the client application
  5. Monitor the WSO2ESB 

[1] https://cms.paypal.com/us/cgi-bin/?&cmd=_render-content&content_ID=developer/e_howto_api_soap_r_GetBalance
=======================================================================================================================

1. Prerequisites
----------------
* You should have the following prerequisites installed on your system to follow this guide.
  - Java 2 SE - JDK or JRE version 1.5.x or higher
  - Apache Ant http://ant.apache.org
  - Define WSO2ESB_HOME environment variable pointing to your WSO2 ESB root directory.  
=======================================================================================================================

2. Build and Deploy the Paypal Mediator
---------------------------------------
* Go to the <download-folder>/paypal folder and execute the following command.
  - mvn clean package
* This will build all the modules under <download-folder>/paypal folder.
* After successfully executing the command, you will see the following a jar file in the format ${project.artifactId}-${project.version}.jar under the   
  <WSO2ESB_HOME>/repository/components/dropins folder.
---
* Copy this jar file to <WSO2ESB_HOME>/repository/components/dropins folder.
---
=======================================================================================================================

3. Configure and Starting the WSO2ESB
-------------------------------------
* Take a backup of <WSO2ESB_HOME>/conf/synapse.xml [e.i:synapse_yourcopy.xml]
* Copy the <download-folder>/paypal/org.wso2.carbon.business.messaging.paypal.samples/dist/conf/synapse.xml file to your <WSO2ESB_HOME>/conf folder.

Starting:
* Set the log level of org.apache.synapse and org.wso2.esb to DEBUG by editing the <esb-home>/lib/log4j.properties file and add the following entry. (Optional)
     log4j.category.org.wso2.carbon.business.messaging.paypal.mediator=DEBUG
   
* Start the WSO2 ESB. This will cause the ESB to output debug messages for the actions it is performing. We can inspect these logs later using the management console.   * Then go to the <esb-home>/bin folder and execute the following command.
     Linux: ./wso2server.sh cleanRegistry
     Windows: wso2server.bat cleanRegistry
   
* This will load the new configuration details from the synapse.xml.
* You will see the messages on the console to indicate that the ESB started successfully using the new configuration details.
* Login to the WSO2 ESB management console and a Proxy service by the name of 'PayPalProxy' should be listed in Mangement-> Service -> List.
=======================================================================================================================

4. Running the client application
---------------------------------
* Copy the <download-folder>/paypal/org.wso2.carbon.business.messaging.paypal.samples folder to <wso2esb_home>/samples folder.
  After copying: <wso2esb_home>/samples/org.wso2.carbon.business.messaging.paypal.samples
* Go to the <wso2esb_home>/samples/org.wso2.carbon.business.messaging.paypal.samples folder and execute the following command.
     ant help 
* This will provide you the infomation required to run the client application.
=======================================================================================================================

5. Monitor the WSO2ESB 
----------------------
* You will see the messages on the console to indicate the contents of the current MessageContext containing the response from the GetBalace operation in the Paypal API.