#!/bin/bash

exec /opt/jboss/wildfly/bin/standalone.sh $@
exit $?