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
EXECUTIONS=$(curl -s "http://$hostAndPort/auth/admin/realms/master/authentication/flows/sqrl/executions" -H "Authorization: Bearer $TKN")

EXECUTIONS=`echo $EXECUTIONS | sed 's/\[//' | sed 's/\}\]/\}/'`
SQRL_AUTH=`echo $EXECUTIONS | sed 's/,{"id.*//'`
UNAME_PWD=${EXECUTIONS#*\},}
UNAME_AUTH=`echo $UNAME_PWD | sed 's/,{"id.*//'`
PWD_AUTH=${UNAME_PWD#*\},}

# Enable authenticators 
SQRL_AUTH=`echo $SQRL_AUTH | sed 's/"requirement":"DISABLED"/"requirement":"REQUIRED"/'`
UNAME_AUTH=`echo $UNAME_AUTH | sed 's/"requirement":"DISABLED"/"requirement":"REQUIRED"/'`
PWD_AUTH=`echo $PWD_AUTH | sed 's/"requirement":"DISABLED"/"requirement":"REQUIRED"/'`

curl -s -X PUT "http://$hostAndPort/auth/admin/realms/master/authentication/flows/sqrl/executions" -H "Content-Type: application/json;charset=UTF-8" -H "Authorization: Bearer $TKN" -d "$SQRL_AUTH"
curl -s -X PUT "http://$hostAndPort/auth/admin/realms/master/authentication/flows/sqrl/executions" -H "Content-Type: application/json;charset=UTF-8" -H "Authorization: Bearer $TKN" -d "$UNAME_AUTH"
curl -s -X PUT "http://$hostAndPort/auth/admin/realms/master/authentication/flows/sqrl/executions" -H "Content-Type: application/json;charset=UTF-8" -H "Authorization: Bearer $TKN" -d "$PWD_AUTH"

# cleanup
# rm tmp.txt  
