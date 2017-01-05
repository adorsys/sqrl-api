var module = angular.module('sqrl.web', [
    'ui.router'
])

module.config(['$qProvider', function ($qProvider) {
    $qProvider.errorOnUnhandledRejections(false);
}]);

module.constant('constants', {
    SQRL_HOST: 'http://sqrl:8081'
});