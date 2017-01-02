function LinkCtrl($scope, SQRLManagementService, $rootScope, $state) {

//    var userId = "test-user"

    $scope.sqrl = {
        prepared: false,
        uri: "#",
        qr: "#",
        exists: false,
        watcher: {
            deregisterUri: undefined,
            deregisterQrSrc: undefined,
            deregisterExists: undefined,
            deregisterDeletion: undefined,
            deregisterLinkPrepare: undefined,
            deregisterLinkFinish: undefined
        }
    };

    $scope.init = function() {
        SQRLManagementService.setup().then(function() {
            //watcher setup
            $scope.sqrl.watcher.deregisterUri = $scope.$watch(function() {
                return SQRLManagementService.getUri();
            }, function(newValue) {
                if (newValue === '#') {
                    return;
                }
                $scope.sqrl.uri = newValue;
                $scope.sqrl.watcher.deregisterUri();
            });
            $scope.sqrl.watcher.deregisterQrSrc = $scope.$watch(function() {
                return SQRLManagementService.getQrSrc();
            }, function(newValue) {
                if (newValue === '#') {
                    return;
                }
                $scope.sqrl.qr = newValue;
                $scope.sqrl.watcher.deregisterQrSrc();
            });
            scanSqrl();
            prepareSqrl();
        });
    };

    var prepareSqrl = function() {
        $scope.sqrl.watcher.deregisterLinkPrepare = $scope.$watch(function() {
            return SQRLManagementService.watchLinkAccountToSqrlPreparation($rootScope.token);
        }, function(newValue) {
            if (newValue !== undefined) {
                $scope.sqrl.prepared = newValue;
                $scope.sqrl.watcher.deregisterLinkPrepare();
                $scope.$watch(function() {
                    return SQRLManagementService.watchSqrlStatus();
                }, function(newValue) {
                    if (newValue !== undefined) {
                        if (newValue) {
                            $scope.sqrl.linkSucceeded = true;
                            $scope.sqrl.exists = true;
                        }
                        else {
                            scanSqrl();
                        }
                    }
                });
            }
        });
    };

    $scope.delete = function() {
        $scope.sqrl.watcher.deregisterDeletion = $scope.$watch(function() {
            return SQRLManagementService.requestSqrlKeyDelete();
        }, function(deleted) {
            if (deleted !== undefined) {
                $scope.sqrl.exists = !deleted;
                $scope.sqrl.watcher.deregisterDeletion();
                // AlertService.success($translate("manageAccount.form.sqrl.msgLinkDeleted"));
            }
        });
    };

    function scanSqrl() {
        // UserInfoService.reload().then(function() {
        //     UserInfoService.userInfo().then(function(newInfo) {
        //         $scope.sqrl.watcher.deregisterExists = $scope.$watch(function() {
        //             return SQRLManagementService.watchIfSqrlIdentityExists(newInfo.personId);
        //         }, function(exists) {
        //             if (exists !== undefined) {
        //                 $scope.sqrl.exists = exists;
        //                 $scope.sqrl.watcher.deregisterExists();
        //             }
        //         });
        //     });
        // });
    }

    $scope.$on('$destroy', SQRLManagementService.stopPolling);
    $scope.init();
}

angular.module('sqrl.web')
    .controller('LinkCtrl', LinkCtrl)