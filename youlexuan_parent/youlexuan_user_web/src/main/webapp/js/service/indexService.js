app.service("indexService", function ($http) {

    this.getName = function () {
        return $http.get("/index/getName.do");
    }

});

