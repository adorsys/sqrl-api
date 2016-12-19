var SQRLLoginService = function ($http, $interval) {
    var service = {
        baseUri: undefined,
        lastState: undefined,
        prepareLinkingStarted: false,
        updateStateLoop: undefined,
        cachedResponseData: undefined,
        predefinedResponses: {
            errorResponse: {
                error: true
            }
        },
        provided: {
            responseData: undefined,
            preparedLogin: false,
            preparedLinking: false
        }
    };

    service.setup = function (authUri){
        service.baseUri = "/smartlogin-server/rest/auth";

        var qmIndex = authUri.indexOf("?");
        service.nut = authUri.substring(qmIndex + 1);
    };

    /* ##### ##### ##### */
    /* ##### LOGIN ##### */
    /* ##### ##### ##### */

    //Get login response token
    service.requestResponseData = function () {
        if (service.cachedResponseData) {
            service.provided.responseData = {
                accessToken: service.cachedResponseData.access_token,
                expirationDuration: service.cachedResponseData.expires_in
            };
        }
        else {
            $http.get(service.baseUri + "/sqrl-response?" + service.nut).then(function (response) {
                service.provided.responseData = angular.fromJson(response.data);
            });
        }
    };

    // check if the login worked
    service.pollLoginState = function () {
        $http.get(service.baseUri + "/sqrl-state?" + service.nut).then(function (response) {
            // ignore unchanged state
            if (service.lastState == response.data) {
                return;
            }

            var state = service.lastState = response.data;
            if (state == "LOGIN_SUCCEEDED") {
                //handle login finish
                service.stopPolling();
                service.requestResponseData();
            } else if (state == "PREPARED") {
                //handle preparation finish
                service.provided.preparedLogin = true;
            } else {
                // FAILED or unexpected state
                service.stopPolling();
                service.provided.responseData = service.predefinedResponses.errorResponse;
            }
        });
    };

    //prepare the login for the desired clientId
    service.prepareLogin = function (clientId) {
        var prepared = { data: { clientId: clientId } };
        var json = angular.toJson(prepared);
        service.updateStateLoop = $interval(function () {
            service.pollLoginState();
        }, 2000);
        $http.post(service.baseUri + "/sqrl-prepare?" + service.nut, json);
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

    /* ##### ##### ##### ##### */
    /* ##### SERVICE API ##### */
    /* ##### ##### ##### ##### */

    return {
        /**
         * Initialize the service with desired authentication base uri.
         *
         * @param authBaseUri
         */
        setup: function(authBaseUri){
            service.setup(authBaseUri);
        },
        /**
         * @returns {*} the response object from the login request
         *      {*}.error is set -> failed,
         *      {*}.create is set -> creation allowed,
         *      {*}.accessToken and
         *      {*}.expirationDuration is set -> succeeded
         */
        watchResponse: function () {
            return service.provided.responseData;
        },
        /**
         * Prepares the login via posting necessary client id to server, returns a reference to the prepared flag
         *
         * @param clientId, necessary target application client id, provided in window.location.search
         * @returns {boolean}, true if server signals that login is prepared -> SQRL Client can proceed now
         */
        watchLoginPreparation: function (clientId) {
            if (!service.updateStateLoop) {
                service.prepareLogin(clientId);
            }
            return service.provided.preparedLogin;
        },
        /**
         * Prepare to link the user account to the SQRL client provided identity.
         *
         * @param userId, the valid userId that identifies the user account to link to
         * @param clientId, the target application client id to proceed to
         * @returns {boolean}, true if server signals that linking is prepared -> SQRL Client can proceed now
         */
        watchLoginSuccess: function (userId, clientId, tempResponseData) {
            if (!service.prepareLinkingStarted) {
                //ensure default login state update loop is cancelled
                if (service.updateStateLoop) {
                    service.stopPolling();
                }

                service.cachedResponseData = angular.copy(tempResponseData);
                service.prepareLinking(userId, clientId);
                service.prepareLinkingStarted = true;
            }
            return service.provided.preparedLinking;
        }
    }
}

angular.module('sqrl.web').factory("SQRLLoginService", SQRLLoginService)