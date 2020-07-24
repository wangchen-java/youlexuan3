var app = angular.module("youlexuan", []);

// angular的过滤器，了解
app.filter('trustHtml', ['$sce', function ($sce) {
    return function (data) {
        return $sce.trustAsHtml(data);
    }
}]);