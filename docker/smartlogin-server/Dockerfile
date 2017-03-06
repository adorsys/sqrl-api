FROM jboss/wildfly:10.1.0.Final

ENV JBOSS_HOME /opt/jboss/wildfly

# configuring wildfly over cli
ADD smartlogin-server-jboss-cli.txt $JBOSS_HOME/bin/
RUN $JBOSS_HOME/bin/jboss-cli.sh --file=$JBOSS_HOME/bin/smartlogin-server-jboss-cli.txt
RUN rm -rf $JBOSS_HOME/standalone/configuration/standalone_xml_history

ADD smartlogin-server.war /opt/jboss/wildfly/standalone/deployments/
ADD smartlogin-web-sample.war /opt/jboss/wildfly/standalone/deployments/smartlogin-web-sample.war

ADD docker-entrypoint.sh $JBOSS_HOME
ADD docker-entrypoint-clean.sh $JBOSS_HOME

ENTRYPOINT [ "/opt/jboss/wildfly/docker-entrypoint.sh" ]

CMD ["-b", "0.0.0.0", "-c", "standalone-ha.xml"]