#!/bin/bash

rm -rf /opt/jboss/wildfly/standalone/data/h2

/opt/jboss/wildfly/bin/add-user.sh admin admin123 --silent

exec /opt/jboss/wildfly/bin/standalone.sh $@
exit $?