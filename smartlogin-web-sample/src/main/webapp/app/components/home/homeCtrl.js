function HomeCtrl($scope, $http, SQRLLoginService, $rootScope) {
	$scope.token = $rootScope.token;

}

angular.module('sqrl.web')
    .controller('HomeCtrl', HomeCtrl)