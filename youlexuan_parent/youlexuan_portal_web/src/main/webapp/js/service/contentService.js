//content服务层
app.service('contentService', function($http){

	this.findContentByCatId =function(catId) {
		return $http.get('../content/findContentByCatId.do?catId=' + catId);
	}

});