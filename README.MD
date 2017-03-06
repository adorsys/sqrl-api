#Prototype of a standalone sqrl server working with keycloack

##Building 
 ```
 mvn clean install -f smartlogin-server/pom.xml
 mvn clean install -f smartlogin-keycloak-providers/pom.xml
 mvn clean install -f smartlogin-web-sample/pom.xml
 ```

##Build docker images
 ```
cd docker
./docker-build-smartlogin-server
./docker-build-keycloak
 ```

##Set up SQRL public url
```
cd -
vi docker-compose.yml
 ```
Modify SQRL_PUBLIC_BASE_URL environment variable to external IP of your machine. This url is part of published generated QR-Codes for scan with your phone


##Start the application
First start of server (simple restart with  $> docker-compose up)
 ```
sqrl$ docker-compose -f docker-compose-clean.yml up
 ```
Open in browser
 ```
http://localhost:8080/smartlogin-web-sample/
 ```
Got to login and login with francis@mail.de/francis or alex@mail.de/alex or roman@mail.de/roman 
Go to link and register your phone

Start a new Session
go to login and login with your phone.


#Just for development.

Application Development Cycle. In orther to enable faster roundtrip, all application and 
keycloak modules are installed thru de admin console. So changes can be applied using wildfly:deploy

##Install the sqrl server app
 ```
sqrl> cd smartlogin-server
smartlogin-server> mvn clean install wildfly:deploy 
 ```
##Install the sample web app
```
smartlogin-server> cd -
sqrl> cd smartlogin-web-sample
smartlogin-web-sample> mvn clean install wildfly:deploy
```

#Test the application:


##Log into keycloak admin console
browser> http://sqrl:8081
click on admin console
username: kcadmin password : admin123 => so you can see keycloak admin console

##Add domain users
example:
 ```
sqrl>docker/keycloak-server/volumes/post-deploy/add-domain-user.sh localhost:8081 francis francis
```
Got to build in account page and login with the added username (francis012) and password (francis)
http://sqrl:8081/auth/realms/master/account

##Create sqrl direct access grant public client for the smartlogin-web-sample standalone application
example:
 ```
sqrl>docker/keycloak-server/volumes/post-deploy/add-domain-user.sh sqrl:8081 sqrl-web-client
```

##Export public key of corresponding keycloak realm, so smart-login-server can verify authenticity of access token included in the account linking call 
example:
 ```
sqrl>sdocker/keycloak-server/volumes/post-deploy/read-realm-public-key.sh
```
After this step, the sqrl account linking client shall be configured to login the user and 
send the keycloak access token to the sqrl server in the registration process. The sqrl account
server can use the publickey of the keycloak server from file /volumes/idp-server/post-deploy/generated/realm-master-keys.json
to verify the signature of the token. 