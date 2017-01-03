#!/bin/bash

if [ -z $1 ] 
	then
		echo "Missing host and port"
		echo "Usage: add-direct-grant-client <host:port> <client-id>"
		exit 1
fi

hostAndPort=$1


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

# read executions for flow
REALM_RECORD=$(curl -s "http://$hostAndPort/auth/admin/realms/master" -H "Authorization: Bearer $TKN")
REALM_RECORD=`echo $REALM_RECORD | sed 's/"directGrantFlow":"direct grant"/"directGrantFlow":"sqrl"/'`

curl -s -X PUT "http://$hostAndPort/auth/admin/realms/master" -H "Content-Type: application/json;charset=UTF-8" -H "Authorization: Bearer $TKN" -d "$REALM_RECORD"
