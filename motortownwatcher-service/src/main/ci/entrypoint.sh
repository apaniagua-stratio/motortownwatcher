#!/bin/bash

set -e

source /data/stratio/kms_utils.sh
source /data/stratio/b-log.sh

DOCKER_LOG_LEVEL=${DOCKER_LOG_LEVEL:-DEBUG}
eval LOG_LEVEL_${DOCKER_LOG_LEVEL}
B_LOG --stdout true # enable logging over stdout

export PORT0=${PORT0:-"8080"}

export VAULT_PORT=${VAULT_PORT:-"8200"}
export VAULT_HOST=${VAULT_HOST:-"localhost"}

declare -a VAULT_HOSTS
IFS_OLD=$IFS
IFS=',' read -r -a VAULT_HOSTS <<< "$VAULT_HOST"


declare -a MARATHON_ARRAY
OLD_IFS=$IFS
IFS='/' read -r -a MARATHON_ARRAY <<< "$MARATHON_APP_ID"
IFS=$OLD_IFS


MARATHON_SERVICE_NAME=${MARATHON_ARRAY[-1]}
MARATHON_SERVICE_NAME=$(echo $MARATHON_SERVICE_NAME | sed -E 's/(.*)-[0-9]{13}//')

# Approle login from role_id, secret_id
if [[ -z "${VAULT_TOKEN}" ]];
then
   INFO "Login in vault..."
   login
   if [[ ${code} -ne 0 ]];
   then
       ERROR "Something went wrong log in in vault. Exiting..."
       return ${code}
   fi
fi

INFO "Motortownwatcher Login in vault OK"

#2--- GET SECRETS WITH APP TOKEN
getCert "userland" \
       "motortownwatcher" \
       "motortownwatcher" \
        "PEM" \
        "/data/stratio" \
&& echo "motortownwatcher OK: Getting certificate motortownwatcher" \
|| echo "motortownwatcher Error: Getting certificate motortownwatcher"

getCert "userland" \
       "motortown_sync" \
       "motortown_sync" \
        "PEM" \
        "/data/stratio" \
&& echo "motortownwatcher OK: Getting certificate" \
|| echo "motortownwatcher Error: Getting certificate"


#GET CA-BUNDLE for given CA $SSL_CERT_PATH/ca-bundle.pem
getCAbundle "/data/stratio" "PEM" \
    && echo "motortownimport OK: Getting ca-bundle"   \
    || echo "motortownimport Error: Getting ca-bundle"

CA_BUNDLE_PEM="/data/stratio/ca-bundle.pem"

openssl x509 -outform der -in ${CA_BUNDLE_PEM} -out ${CA_BUNDLE_PEM}.der

openssl pkcs8 -topk8 -inform PEM -outform DER -in /data/stratio/motortown_sync.key -out /data/stratio/motortown_sync.key.pk8 -nocrypt

${JAVA_HOME}/bin/keytool -noprompt -import -storepass "changeit" -file ${CA_BUNDLE_PEM}.der -alias ${MARATHON_SERVICE_NAME} -cacerts

HEAP_PERCENTAGE=${HEAP_PERCENTAGE:-"80"}
JAVA_TOOL_OPTIONS=${JAVA_TOOL_OPTIONS:-"-XX:+UseG1GC -XX:MaxRAMPercentage=${HEAP_PERCENTAGE} -XshowSettings:vm"}
java ${JAVA_TOOL_OPTIONS} -jar /data/app.jar ${JAVA_ARGS}
