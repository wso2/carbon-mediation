#!/bin/sh

if [ -z "$CONNECTOR_HOME" ]; then
  echo "You must set the CONNECTOR_HOME variable before running this script."
  exit 1
fi

#install archetype for mediation connectors
mvn install -f "$CONNECTOR_HOME/org.wso2.carbon.mediation.library.connectors.connector-archetype/pom.xml"

mvn archetype:generate -DarchetypeCatalog=local -DarchetypeGroupId=org.wso2.carbon -DarchetypeArtifactId=org.wso2.carbon.mediation.library.connectors.connector-archetype