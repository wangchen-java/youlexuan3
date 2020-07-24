//cart服务层
app.service('addrService', function($http){
	this.findAddrList =function () {
		return $http.get("/address/findByUserId.do");
	}


});