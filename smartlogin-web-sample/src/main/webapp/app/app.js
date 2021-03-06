var module = angular.module('sqrl.web', [
    'ui.router'
])

module.config(['$qProvider', function ($qProvider) {
    $qProvider.errorOnUnhandledRejections(false);
}]);

module.constant('constants', {
    SQRL_HOST: 'http://sqrl:8081',
    IDP_HOST: 'http://sqrl:8081/auth',
    IDP_REALM: 'master',
    IDP_CLIENT_ID: 'sqrl-web-client'
});