function LoginCtrl($scope, $http, SQRLLoginService) {

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