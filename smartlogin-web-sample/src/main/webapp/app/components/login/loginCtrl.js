function LoginCtrl($scope, $http, SQRLLoginService, $state, $rootScope) {

    var clientId = "sample-client"

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
    
    $scope.login = function(username, password){
        // Read json config of server
        $http.get('/smartlogin-server/rest/idp/sqrl-web-client/client-config'        
        ).then(
    		function(response){
    			$scope.clientConfig = response.data;
    			$scope.url = $scope.clientConfig['auth-server-url'];
    	    	$http(
	        		{
	                	method : 'POST',
	                	url : $scope.url+'/realms/master/protocol/openid-connect/token',
	                	data : {'username':username, 'password':password, 'grant_type':'password', 'client_id':'sqrl-web-client'},
	                	headers : {'Content-Type': 'application/x-www-form-urlencoded', 'Accept': 'application/json'}, 
	                    transformRequest: $scope.transformRequest        				
	        		}
    	    	).then(
    	    		function(successResponse){
//    	    			$scope.token=successResponse.data;
//    	    			$scope.accessToken=$scope.token.access_token;
    	    			$rootScope.token = successResponse.data.access_token;
    	    			$scope.sqrl.watcher.unregisterLogin();
    	    			$state.go('home.link');
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
    
    $http.get('/smartlogin-server/rest/auth/sqrl-uri').then(
        function (response) {
            var nutParam = response.data.substring(response.data.indexOf("?") + 1);
            $scope.sqrl.sqrlNut = nutParam.substring(nutParam.indexOf('=') + 1);
            $scope.sqrl.sqrlUrl = response.data;
            $scope.sqrl.sqrlQr = '/smartlogin-server/rest/auth/sqrl-qr-code?' + nutParam;

            SQRLLoginService.setup($scope.sqrl.sqrlUrl);

            sqrlSetup();
        }
    );

}

angular.module('sqrl.web')
    .controller('LoginCtrl', LoginCtrl)