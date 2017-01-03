#!/bin/bash

rm -rf keycloak/standalone/data/h2

keycloak/bin/add-user-keycloak.sh --user kcadmin --password kcadmin123
keycloak/bin/add-user.sh admin admin123 --silent

exec /opt/jboss/keycloak/bin/standalone.sh $@
exit $?