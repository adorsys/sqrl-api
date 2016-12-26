JBoss Module for the deployment of a keycloak AccountResource.

The AccountResource can be used to : 
- provide information on the existence of a user

- For rhe development
   	- Hotdeployment of module using: > mvn clean install wildfly:deploy -Dhost.name=sqrl
   	- List dependencies in src/main/resources/META-INF/jboss-deployment-structure.xml
- For Production
	- List dependencies in module.xml
	- mvn assembly plugins is used to generate jboss-module
	- docker-file is used to add module to default keycloak installation.
	
Ports (see de.adorsys.smartlogin.account.resource.AccountResourceProvider):
