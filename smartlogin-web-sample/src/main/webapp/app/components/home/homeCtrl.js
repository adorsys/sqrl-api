function HomeCtrl($scope, $http, SQRLLoginService) {

    var clientId = "sample.client"
    $scope.sqrl = {};

    function sqrlLoginSetup() {
        var loginSucceededCallback = function () {
            // HACK: non-NG way to trigger the form submit
            // Create a separate form and post it immediately. That way we do not post any content of the username/password inputs.
            // The sqrlNut parameter will be picked up by de.adorsys.adscore.tenant.auth.SqrlLoginValve
            var actionUrl = kurzUtil.updateUrlParameter($scope.data.actionUrl, 'sqrlNut', $scope.bouncer.sqrlNut);
            $('<form method="POST" action="' + actionUrl + '"></form>').appendTo('body').submit();
        };
        // prepare login with client id found
        $scope.sqrl.watcher.unregisterLogin = $scope.$watch(function () {
            return SQRLLoginService.watchLoginPreparation(clientId, loginSucceededCallback);
        }, function (newValue) {
            if (newValue) {
                $scope.sqrl.prepared = newValue;
                $scope.sqrl.watcher.unregisterLogin();
                $('#un').focus();
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

            sqrlLoginSetup();

        },
        function (error) {
            console.log(error);
        }
    );

}

angular.module('sqrl.web')
    .controller('HomeCtrl', HomeCtrl)