//cart服务层
app.service('cartService', function($http){
	this.findCartList =function () {
		return $http.get("/cart/findCartList.do");
	}
	this.changeNum =function (itemId,num) {
		return $http.post("/cart/addCart.do?skuId=" + itemId +"&num=" + num );
	}
	this.submitOrder = function (order) {
		return $http.post("/order/add.do" ,order);
	}
});