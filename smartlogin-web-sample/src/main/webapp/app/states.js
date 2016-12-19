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


    $urlRouterProvider.otherwise('/home');

};

angular.module('sqrl.web')
    .config(States)

