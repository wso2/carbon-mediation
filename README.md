# WSO2 Carbon Mediation
Welcome to the WSO2 Carbon Mediation source repository. This repository contains the mediation features required by the WSO2 carbon platform. Specially the mediation features related to WSO2 ESB are developed in this repository. This also provides the bridging between [wso2-synapse](https://github.com/wso2/wso2-synapse) and the carbon platform. 

# WSO2 Carbon Mediation Features
- Providing implementations for Business Adapters like SAP and HL7
- Includes the framework and implementations of Inbound Endpoints (HTTP/S, JMS, File, MQTT, Kafka, HL7, RabbitMQ, Websocket/s)
- Includes the framework for WSO2 cloud connectors which used to build 150+ connectors
- Implements the admin services for WSO2 ESB level feature management
- Implements the management console UI features for WSO2 ESB features
- Provides the data publisher capabilities to WSO2 ESB for connecting with WSO2 DAS and ESB analytics
- Throttling implementation used by the WSO2 ESB
- Implements the adapter to connect with ntask framework (carbon ntask) and provides the capabilities to build top level components like inbound endpoints which uses the ntask for cluster coordination

# How to Contribute

* Please report issues at [WSO2 JIRA](https://wso2.org/jira/browse/ESBJAVA)
* Send your pull requests to [carbon-mediation](https://github.com/wso2/carbon-mediation) repository

# Contact us

WSO2 developers can be contacted via the mailing lists:

* WSO2 Developers List : dev@wso2.org
* WSO2 Architecture List : architecture@wso2.org

## Jenkins Build Status

|  Branch | Build Status |
| :------------ |:-------------
| carbon-mediation master      | [![Build Status](https://wso2.org/jenkins/job/carbon-mediation/badge/icon)](https://wso2.org/jenkins/job/carbon-mediation)
