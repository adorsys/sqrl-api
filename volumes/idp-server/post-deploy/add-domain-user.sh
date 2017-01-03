#!/bin/bash

if [ -z $1 ] 
	then
		echo "Missing host and port"
		echo "Usage: add-domain-user <host:port> <username> <password>"
		exit 1
fi

if [ -z $2 ] 
	then
		echo "Missing parameter username"
		echo "Usage: add-domain-user <host:port> <username> <password>"
		exit 1
fi

if [ -z $3 ] 
	then
		echo "Missing parameter password"
		echo "Usage: add-domain-user <host:port> <username> <password>"
		exit 1
fi

hostAndPort=$1
username=$2
password=$3


TKN='test'
while [ "$TKN" == 'test' ]; do
	echo "Checking idp server"
	# Get and parse access token
	RESP=$(curl -s -X POST "http://$hostAndPort/auth/realms/master/protocol/openid-connect/token" -D tmp.txt -H "Content-Type: application/x-www-form-urlencoded" -d "username=kcadmin" -d 'password=kcadmin123' -d 'grant_type=password' -d 'client_id=admin-cli')
	if [[ "$RESP" == *"access_token"* ]]
	then
	  TKN=`echo $RESP | sed 's/.*access_token":"//g' | sed 's/".*//g'`
	  echo "Idp is ready"
	else
	  echo "Still waiting for idp to be ready"
	  sleep 2
	fi
done

# get user name from first parameter.
data="{\"enabled\":true,\"attributes\":{},\"requiredActions\":[],\"username\":\"$username\",\"email\":\"$username@sqrl.adorsys.de\",\"firstName\":\"$username\",\"lastName\":\"$username\",\"emailVerified\":true}"

# create user and parse result link. Http location header.
createUserTaget="http://$hostAndPort/auth/admin/realms/master/users/"
echo $createUserTaget
curl -s -X POST $createUserTaget -H "Content-Type: application/json;charset=UTF-8" -H "Authorization: Bearer $TKN" -d $data -D tmp.txt

# cat tmp.txt

location=`awk '/users/{print $NF}' tmp.txt`	 
location=`echo $location | tr -d '\r'`
# build reset password link 
location=${location}/reset-password

echo $location

# set user password
data="{\"type\":\"password\",\"value\":\"$password\",\"temporary\":false}"
curl -s -X PUT $location -H 'Content-Type: application/json;charset=UTF-8' -H "Authorization: Bearer $TKN" -d $data

# cleanup
rm tmp.txt  
