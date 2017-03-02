FROM jboss/keycloak:2.5.4.Final

ENV JBOSS_HOME /opt/jboss/keycloak

# THis is the keycloack extension for a modified direct grant module that supports
# sqrl nut and accessTokenId as alternative to username and password.
ADD direct-grant.tar.gz $JBOSS_HOME/modules/system/layers/base

# h2 for access to keycloak database. Just for debugging
ADD lib/h2console.war $JBOSS_HOME/standalone/deployments/
ADD lib/.h2.server.properties /opt/jboss/

# configuring wildfly over cli
ADD keycloak-jboss-cli.txt $JBOSS_HOME/bin/
RUN $JBOSS_HOME/bin/jboss-cli.sh --file=$JBOSS_HOME/bin/keycloak-jboss-cli.txt
RUN rm -rf $JBOSS_HOME/standalone/configuration/standalone_xml_history

ADD docker-entrypoint.sh $JBOSS_HOME
ADD docker-entrypoint-clean.sh $JBOSS_HOME

EXPOSE 8080

ENTRYPOINT [ "/opt/jboss/keycloak/docker-entrypoint.sh" ]

CMD ["-b", "0.0.0.0", "-c", "standalone-ha.xml"]