Prototype of a standalone sqrl server working with keycloack

Building:

> mvn clean install

Prepare docker environment:

# Create a docker-host using docker machine. I named it sqrl to isolate it from other hosts on my development environment.
sqrl> docker-machine create -d virtualbox sqrl

# List hosts
sqrl> docker-machine ls
...
sqrl      -        virtualbox   Running   tcp://192.168.99.100:2376           v1.12.5
...

# Prepare access to host
# In my /etc/hosts I put this: 
192.168.99.100 sqrl

# attach this console to docker-host
sqrl> eval $(docker-machine env sqrl)

# Verify
sqrl> docker-machine ls
...
sqrl      *        virtualbox   Running   tcp://192.168.99.100:2376           v1.12.5 
...

# Start server farm
sqrl> docker-compose up

# Application Development Cycle. In orther to enable faster roundtrip, all application and 
# keycloak modules are installed trhu de admin console. So changes can be applied using wildfly:deploy

# Install the keycloak account-resource
sqrl> cd smartlogin-keycloak-providers/account-resource
account-resource> mvn clean install wildfly:deploy -Dhost.name=sqrl

# Install the sqrl server app
account-resource> cd -
sqrl> cd smartlogin-server
smartlogin-server> mvn clean install wildfly:deploy -Dhost.name=sqrl

# Install the sample web app
smartlogin-server> cd -
sqrl> cd smartlogin-web-sample
smartlogin-web-sample> mvn clean install wildfly:deploy -Dhost.name=sqrl

Test the application:

# Check deployments on wildfly
# browser> http://sqrl:9990
# username: admin password : admin123

# Check deployement on the keycloak server
# browser> http://sqrl:9991
# username: admin password : admin123

# Log into keycloak admin console
# browser> http://sqrl:8081
# click on admin console
# username: kcadmin password : admin123 => so you can see keycloak admin console

# Add domain users
# example: sqrl>smartlogin-keycloak-providers/keycloak-server/post-deploy/add-domain-user.sh sqrl:8081 francis francis

# Got to build in account page and login with the added username (francis012) and password (francis)
http://sqrl:8081/auth/realms/master/account

# Create sqrl direct access grant public client for the smartlogin-web-sample standalone application
# example: sqrl>smartlogin-keycloak-providers/keycloak-server/post-deploy/add-domain-user.sh sqrl:8081 sqrl-web-client
# the json configuration for the web client will be available under: /volumes/idp-server/post-deploy/generated/sqrl-web-client.json

# Export public key of corresponding keycloak realm, so smart-login-server can verify authenticity
# of access token included in the account linking call. 
# example: sqrl>smartlogin-keycloak-providers/keycloak-server/post-deploy/read-realm-public-key.sh

# After this step, the sqrl account linking client shall be configured to login the user and 
# send the keycloak access token to the sqrl server in the registration process. The sqrl account
# server can use the publickey of the keycloak server from file /volumes/idp-server/post-deploy/generated/realm-master-keys.json
# to verify the signature of the token. 

