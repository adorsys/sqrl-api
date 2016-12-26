var SQRLManagementService = function ($http, $interval) {
    var service = {
        baseUri: undefined,
        nut: "#",
        updateStateLoop: undefined,
        prepareStarted: false,
        provided: {
            uri: "#",
            qr: "#",
            exists: {
                userId: undefined,
                result: undefined
            },
            deletion: {
                requested: false,
                result: undefined
            },
            success: undefined,
            prepared: false
        }
    };

    service.setup = function () {
        service.baseUri = "/smartlogin-server/rest/auth";

        return $http.get(service.baseUri + "/sqrl-uri").then(function (response) {
            var qmIndex = response.data.indexOf("?");
            service.nut = response.data.substring(qmIndex + 1);

            service.checkIfSqrlIdentityExists();

            service.provided.uri = response.data;
            service.provided.qr = service.baseUri + "/sqrl-qr-code?" + service.nut;
            service.provided.state = "INITIALIZED";
        });
    }

    service.updateRemoteState = function () {
        $http.get(service.baseUri + "/sqrl-state?" + service.nut).then(function (response) {
            if (response.data == "PREPARED") {
                service.provided.prepared = true;
            } else {
                $interval.cancel(service.updateStateLoop);
                service.updateStateLoop = undefined;
                service.provided.success = response.data != "FAILED";
            }
        });
    };

    service.prepareRegister = function (userId, clientId) {
        var prepared = {
            data: {
                userId: userId,
                clientId: clientId
            }
        };
        var json = angular.toJson(prepared);
        $http.post(service.baseUri + "/sqrl-prepare?" + service.nut, json).then(function (response) {
            service.provided.prepared = true;
        });
    };

    service.startStatePolling = function () {
        service.updateStateLoop = $interval(function () {
            service.updateRemoteState();
        }, 2000);
    };

    service.rand = function () {
        var length = 48;
        var chars = '0123456789abcdefghijklmnopqrstuvwxyzABCDEFGHIJKLMNOPQRSTUVWXYZ';
        var result = '';
        for (var i = length; i > 0; --i) result += chars[Math.round(Math.random() * (chars.length - 1))];
        return result;
    };

    service.checkIfSqrlIdentityExists = function () {
        if (service.provided.exists.userId && service.nut.length > 1) {
            $http.get(service.baseUri + "/sqrl-exists?" + service.nut + "&userid=" + service.provided.exists.userId).then(function (response) {
                service.provided.exists.result = ( response.data === "true" );
            });
        }
    };

    service.requestDeletion = function (userId) {
        $http.delete(service.baseUri + "/sqrl?" + service.nut + "&userid=" + userId).then(function () {
            service.provided.deletion.result = true;
        });
    };

    /* ##### ##### ##### ##### ##### ##### ##### ##### ##### ### */
    /* ##### LINK EXISTING ACCOUNT TO SQRL CLIENT IDENTITY ##### */
    /* ##### ##### ##### ##### ##### ##### ##### ##### ##### ### */

    // check state of linking process
    service.pollLinkingState = function () {
        $http.get(service.baseUri + "/sqrl-state?" + service.nut).then(function (response) {
            // ignore unchanged state
            if (service.lastState == response.data) {
                return;
            }

            var state = service.lastState = response.data;
            if (state == "PREPARED") {
                service.provided.preparedLinking = true;
            }
            else if (state == "CREATE_SUCCEEDED") {
                service.stopPolling();
                service.provided.success = true;
            } else if (state == "FAILED") {
                service.stopPolling();
                service.provided.success = false;
            } else {
                // Ignore other states such as CREATE_SUCCEEDED or SUCCEEDED
            }
        });
    };

    //prepare the linking with userId and target clientId
    service.prepareLinking = function (userId) {
        service.provided.responseData = undefined;
        var prepared = {
            data: {
                userId: userId
            }
        };
        var json = angular.toJson(prepared);
        $http.post(service.baseUri + "/sqrl-prepare?" + service.nut, json).then(function () {
            service.updateStateLoop = $interval(function () {
                service.pollLinkingState();
            }, 2000);
        });
    };

    /* ##### ##### ##### ##### ##### ### */
    /* ##### COMMON SHARED HELPERS ##### */
    /* ##### ##### ##### ##### ##### ### */

    // stop poll loop
    service.stopPolling = function () {
        if (service.updateStateLoop) {
            $interval.cancel(service.updateStateLoop);
            service.updateStateLoop = service.lastState = undefined;
        }
    };

    /**
     * API
     */
    return {
        /**
         * Initialize the sqrl with desired authentication base uri.
         *
         * @param authBaseUri
         */
        setup: function(){
            return service.setup();
        },
        /**
         * Provides the sqrl-uri.
         * @returns {string}
         */
        getUri: function () {
            return service.provided.uri;
        },
        /**
         * Provides the sqrl-qr source url.
         * @returns {string}
         */
        getQrSrc: function () {
            return service.provided.qr;
        },
        /**
         * Prepare registration via SQRL through sending necessary data to the server.
         * @param userId - identify user
         * @param clientId - target application client id
         * @returns {boolean}, true if prepared
         */
        watchSqrlAccountRegistrationPreparation: function (userId, clientId) {
            if (!service.prepareStarted) {
                service.prepareRegister(userId, clientId);
                service.prepareStarted = true;
            }
            return service.provided.prepared;
        },
        /**
         * Starts the update loop, return value set on response received.
         * @returns {*} true->if register succeeded, false if not
         */
        watchSqrlStatus: function () {
            if (!service.updateStateLoop) {
                service.startStatePolling();
            }
            return service.provided.success;
        },
        /**
         * Delete sqrl keys for userid.
         * @param userId
         * @returns {bool} true->when ready
         */
        requestSqrlKeyDelete: function (userId) {
            if (!service.provided.deletion.requested) {
                service.requestDeletion(userId);
                service.provided.deletion.requested = true;
            }
            return service.provided.deletion.result;
        },
        /**
         * Check if userid has a linked sqrl key.
         * @param userId
         * @returns {bool} true->if exists
         */
        watchIfSqrlIdentityExists: function (userId) {
            if (userId && !service.provided.exists.userId) {
                service.provided.exists.userId = userId;
                service.checkIfSqrlIdentityExists();
            }
            return service.provided.exists.result;
        },
        /**
         * Provide random string generation to sqrl consumer.
         * @returns {string} means {char[]}
         */
        generateRandomString: function () {
            return service.rand();
        },
        /**
         * Prepare to link the user account to the SQRL client provided identity.
         *
         * @param userId, the valid userId that identifies the user account to link to
         * @param clientId, the target application client id to proceed to
         * @returns {boolean}, true if server signals that linking is prepared -> SQRL Client can proceed now
         */
        watchLinkAccountToSqrlPreparation: function (userId) {
            if (!service.prepareLinkingStarted) {
                //ensure default login state update loop is cancelled
                if (service.updateStateLoop) {
                    service.stopPolling();
                }

                service.prepareLinking(userId);
                service.prepareLinkingStarted = true;
            }
            return service.provided.preparedLinking;
        }
    }
}

angular.module('sqrl.web').factory("SQRLManagementService", SQRLManagementService)
