app.controller("indexController", function ($scope, indexService) {

    $scope.getName = function () {
        indexService.getName().success(function (resp) {
            $scope.loginName = resp.substring(1, resp.length - 1);
        });
    }

});