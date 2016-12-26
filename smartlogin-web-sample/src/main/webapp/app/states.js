function States($stateProvider, $urlRouterProvider) {

    $stateProvider
        .state('home', {
            url: '/home',
            views: {
                '@': {
                    templateUrl: 'app/components/home/homeView.html',
                    controller: 'HomeCtrl'
                }
            }
        })
        .state('home.link', {
            url: '/link',
            views: {
                '@': {
                    templateUrl: 'app/components/link/linkView.html',
                    controller: 'LinkCtrl'
                }
            }
        })
        .state('home.login', {
            url: '/login',
            views: {
                '@': {
                    templateUrl: 'app/components/login/loginView.html',
                    controller: 'LoginCtrl'
                }
            }
        })

    $urlRouterProvider.otherwise('/home');

};

angular.module('sqrl.web')
    .config(States)

