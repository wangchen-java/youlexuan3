//search控制层
app.controller('searchController', function ($scope,$location,searchService) {
    $scope.searchMap={'keywords':'','category':'','brand':'','spec':{},'price':'','pageNo':1,'pageSize':20,'sortField':'','sort':''};
    $scope.search = function () {
        searchService.search($scope.searchMap).success(function (resp) {
            if(resp) {
                // 后台返回的是一个map， 其中有一个键rows
                $scope.resultMap = resp;
                createPageItem();
            }
        });
    }
    //私有方法,只能在当前的controller中调用
    createPageItem =function(){
        // 用来生成页码
        $scope.pageLabel = [];
        var firstPage =1;
        var lastPage = $scope.resultMap.totalPages;
        $scope.firstDot=true;//前面有点
        $scope.lastDot=true;//后边有点
        if($scope.resultMap.totalPages >5){
            if ($scope.searchMap.pageNo <= 3){
                $scope.firstDot=false;
                lastPage =5;
            }else if ($scope.searchMap.pageNo >= $scope.resultMap.totalPages -2){
                $scope.lastDot=false;
                firstPage =$scope.resultMap.totalPages -4;
            }else{
                firstPage = $scope.searchMap.pageNo -2;
                lastPage =$scope.searchMap.pageNo +2;
            }
        }else{
            $scope.firstDot=false;
            $scope.lastDot=false;
        }



        for (var i=firstPage;i<=lastPage;i++){
            $scope.pageLabel.push(i);
        }
    }
    $scope.addSearch = function(key, val) {
        if(key == "category" || key == "brand" || key =="price") {
            $scope.searchMap[key] = val;
        } else {
            $scope.searchMap.spec[key] = val;
        }

        $scope.search();
    }

    // 移除搜索条件
    $scope.removeSearch = function (key) {
        if(key == "category" || key == "brand" || key =="price") {
            $scope.searchMap[key] = '';
        } else {
            delete $scope.searchMap.spec[key];
        }

        $scope.search();
    }
    $scope.queryByPage=function(pageNo){
        pageNo =parseInt(pageNo);
        //页码验证
        if(pageNo<1 || pageNo > $scope.resultMap.totalPages){
            return;
        }
        $scope.searchMap.pageNo=pageNo;
        $scope.search();
    }
    //排序方法
    $scope.addSort =function (sortField,sort) {
        $scope.searchMap.sortField =sortField;
        $scope.searchMap.sort =sort;
        $scope.search();
    }
    $scope.loadKeywords =function () {
       var kw =$location.search()['keywords'];
       $scope.searchMap.keywords =kw;
       $scope.search();
    }
});
