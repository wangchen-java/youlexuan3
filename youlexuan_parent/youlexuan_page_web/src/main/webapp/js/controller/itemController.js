//content控制层 
app.controller('itemController', function ($scope,$http) {

    $scope.num = 1;
	//数量操作
	$scope.addNum=function(x){
		$scope.num=$scope.num+x;
		if($scope.num<1){
			$scope.num=1;
		}
    }
    //记录用户选择的规格
    $scope.specificationItems={};
    $scope.selectSpecification=function(specName,opName){
        $scope.specificationItems[specName]=opName;
        changeSku();
    }	
    changeSku =function(){
        for(var i =0;i<skuList.length;i++){
           if(matchObject($scope.specificationItems,skuList[i].spec)){
               $scope.sku = skuList[i];
               break;
           }
           
        }
    }
    matchObject=function(map1,map2){
		for(var k in map1){
			if(map1[k]!=map2[k]){
				return false;
			}
		}
		for(var k in map2){
			if(map2[k]!=map1[k]){
				return false;
			}
		}
		return true;
	}
	//判断某规格选项是否被用户选中
	$scope.isSelected=function(specName,opName){
		if($scope.specificationItems[specName]==opName){
			return true;
		}else{
			return false;
		}
	}
    $scope.loadDefaultSku =function(){
        $scope.sku =skuList[0];
        $scope.specificationItems =JSON.parse(JSON.stringify($scope.sku.spec));
    }
    //加入购物车
    $scope.addToCart =function(){
        //alert("加入购物车的sku为:"+$scope.sku.id + ",数量为:" + $scope.num);
		$http.get("http://localhost:9013/cart/addCart.do?skuId=" + $scope.sku.id + "&num=" + $scope.num, {'withCredentials':true}).success(function (resp) {
			if(resp.success) {
				location.href = "http://localhost:9013/cart.html";
			} else {
				alert(resp.message);
			}
		});

	}

});	
