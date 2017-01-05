function LoginCtrl($scope, $http, SQRLLoginService, $state, $rootScope, constants) {

    var clientId = "sqrl-web-client"

    $scope.sqrl = {
        watcher: {
            unregisterResponse: undefined,
            unregisterLogin: undefined,
            unregisterLink: undefined
        }
    };

    function sqrlSetup() {
        var loginSucceededCallback = function () {
            $scope.sqrl.loginSucceeded = true;
            // get response data and use it to load token from idp.
            $http.get($scope.sqrl.sqrlResp).then(
            		function(response){
            			$scope.sqrlLogin($scope.sqrl.sqrlNut, response.data.accessToken, function(){
            				// state go to where ever after sqrl login.
            				$state.go('home');
            			});
            		}
            );
        };
        // prepare login with client id found
        $scope.sqrl.watcher.unregisterLogin = $scope.$watch(function () {
            return SQRLLoginService.watchLoginPreparation(clientId, loginSucceededCallback);
        }, function (newValue) {
            if (newValue) {
                $scope.sqrl.prepared = newValue;
                $scope.sqrl.watcher.unregisterLogin();
            }
        });
    }
    
	$scope.transformRequest = function(obj){
        var str = [];
        for(var p in obj){
        	if(obj[p]==undefined) continue;
        	str.push(encodeURIComponent(p) + "=" + encodeURIComponent(obj[p]));
        }
        return str.join("&");
	};
    
    $scope.login = function(username, password, successCallback){
        // Read json config of server
        $http.get(constants.SQRL_HOST+'/smartlogin-server/rest/idp/sqrl-web-client/client-config'
        ).then(
    		function(response){
    			$scope.clientConfig = response.data;
    			$scope.url = $scope.clientConfig['auth-server-url'];
    	    	$http(
	        		{
	                	method : 'POST',
	                	url : $scope.url+'/realms/master/protocol/openid-connect/token',
	                	data : {'username':username, 'password':password, 'grant_type':'password', 'client_id':clientId},
	                	headers : {'Content-Type': 'application/x-www-form-urlencoded', 'Accept': 'application/json'}, 
	                    transformRequest: $scope.transformRequest        				
	        		}
    	    	).then(
    	    		function(successResponse){
    	    			$rootScope.token = successResponse.data.access_token;
    	    			$scope.sqrl.watcher.unregisterLogin();
    	    			if(successCallback){
    	    				successCallback();
    	    			} else {
    	    				$state.go('home');
    	    			}
    	    		},
            		function (errorResponse) {
    	    			// todo show response on page.
    	    			$scope.status=errorResponse.status;
    	    			$scope.statusText=errorResponse.statusText;
                    }    		
    	    	);
    			
    		}
        );    	
    };
    
    $scope.sqrlLogin = function(nut, accessTokenId, successCallback){
        // Read json config of server
        $http.get(constants.SQRL_HOST+'/smartlogin-server/rest/idp/sqrl-web-client/client-config'
        ).then(
    		function(response){
    			$scope.clientConfig = response.data;
    			$scope.url = $scope.clientConfig['auth-server-url'];
    	    	$http(
	        		{
	                	method : 'POST',
	                	url : $scope.url+'/realms/master/protocol/openid-connect/token',
	                	data : {'username':'anonymous', 'password':'anonymous', 'nut':nut, 'accessTokenId':accessTokenId, 'grant_type':'password', 'client_id':clientId},
	                	headers : {'Content-Type': 'application/x-www-form-urlencoded', 'Accept': 'application/json'}, 
	                    transformRequest: $scope.transformRequest        				
	        		}
    	    	).then(
    	    		function(successResponse){
    	    			$rootScope.token = successResponse.data.access_token;
    	    			$scope.sqrl.watcher.unregisterLogin();
    	    			if(successCallback){
    	    				successCallback();
    	    			} else {
    	    				$state.go('home');
    	    			}
    	    		},
            		function (errorResponse) {
    	    			// todo show response on page.
    	    			$scope.status=errorResponse.status;
    	    			$scope.statusText=errorResponse.statusText;
                    }    		
    	    	);
    			
    		}
        );    	
    };    
    $http.get(constants.SQRL_HOST+'/smartlogin-server/rest/auth/sqrl-uri').then(
        function (response) {
            var nutParam = response.data.substring(response.data.indexOf("?") + 1);
            $scope.sqrl.sqrlNut = nutParam.substring(nutParam.indexOf('=') + 1);
            $scope.sqrl.sqrlUrl = response.data;
            $scope.sqrl.sqrlQr = constants.SQRL_HOST+'/smartlogin-server/rest/auth/sqrl-qr-code?' + nutParam;
            $scope.sqrl.sqrlResp = constants.SQRL_HOST+'/smartlogin-server/rest/auth/sqrl-response?' + nutParam;

            SQRLLoginService.setup($scope.sqrl.sqrlUrl);

            sqrlSetup();
        }
    );

}

angular.module('sqrl.web')
    .controller('LoginCtrl', LoginCtrl)