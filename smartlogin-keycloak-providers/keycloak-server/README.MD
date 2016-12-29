Configuration of the keycloak server

1. assembly.xml

This file is used to pull modules dependency that will be copied to keycloak.

2. docker-build-keycloak

Command line to build the productive keycloak image.

3. Dockerfile-keycloak

Docker file putting together the productive keycloak image.

4. keycloak-jboss-cli.txt

Jboss cli batch script used to configure standalone-ha.xml

5. Post deployment scrip to add domain users
usage>./post-deploy/add-domain-user.sh <username> <userpassword>

6. create an sqrl client with direct access grant.
usage> ./post-deploy/add-direct-grant-client.sh <client-id>




