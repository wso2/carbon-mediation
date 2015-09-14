#!/bin/bash
# This script use to generate the required keys for Cloud Gateway.
# Open SSL and JDK 1.6 above should be installed in th system.

echo "Generating Cloud Gateway server key.."
openssl genrsa -out server-key.pem 2048

echo "Generating Cloud Gateway server certificate.."
openssl req -new -x509 -key server-key.pem -out server-cert.pem -days 10000

echo "Generating Cloud Gateway C++ Agent key.."
openssl genrsa -out client-key.pem 2048

echo "Generating Cloud Gateway C++ Agent certificate.." 
openssl req -new -x509 -key client-key.pem -out client-cert.pem -days 10000

openssl pkcs8 -topk8 -nocrypt -in server-key.pem  -inform PEM -out server-key.der -outform DER
openssl pkcs8 -topk8 -nocrypt -in client-key.pem  -inform PEM -out client-key.der -outform DER

openssl x509 -in client-cert.pem  -inform PEM -out client-cert.der -outform DER
openssl x509 -in server-cert.pem  -inform PEM -out server-cert.der -outform DER

echo "Generating Cloud Gateway Java keystore.."
java ImportKey server-key.der server-cert.der cg-keystore cg-keystore.jks cg-keystore

echo "Generating Cloud Gateway Java truststore.."
keytool -importcert -file client-cert.der -keystore cg-truststore.jks -storepass cg-truststore -alias cg-truststore -keypass cg-truststore

rm *.der 
echo "Done!"
