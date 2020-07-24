//type_template控制层 
app.controller('typeTemplateController' ,function($scope, $controller, typeTemplateService, brandService, specificationService){
	
	// 继承
	$controller("baseController", {
		$scope : $scope
	});
	
	// 保存
	$scope.save = function() {
		typeTemplateService.save($scope.entity).success(function(response) {
			if (response.success) {
				// 重新加载
				$scope.reloadList();
			} else {
				alert(response.message);
			}
		});
	}
	
	//查询实体 
	$scope.findOne = function(id){				
		typeTemplateService.findOne(id).success(
			function(response){
				$scope.entity= response;
				$scope.entity.brandIds=  JSON.parse($scope.entity.brandIds);//转换品牌列表
				$scope.entity.specIds=  JSON.parse($scope.entity.specIds);//转换规格列表
				$scope.entity.customAttributeItems= JSON.parse($scope.entity.customAttributeItems);//转换扩展属性
			}
		);				
	}
	
	//批量删除 
	$scope.dele = function(){			
		//获取选中的复选框			
		typeTemplateService.dele($scope.selectIds).success(
			function(response){
				if(response.success){
					$scope.reloadList();
					$scope.selectIds=[];
				}						
			}		
		);				
	}
	
	// 定义搜索对象 
	$scope.searchEntity = {};
	// 搜索
	$scope.search = function(page,size){			
		typeTemplateService.search(page,size,$scope.searchEntity).success(
			function(response){
				$scope.list=response.rows;	
				$scope.paginationConf.totalItems=response.total;
			}			
		);
	}
	$scope.addRow = function () {
		$scope.entity.customAttributeItems.push({});
	}

	// 删除模板 扩展属性的表格行
	$scope.delRow = function (idx) {
		$scope.entity.customAttributeItems.splice(idx,1);
	}
	//$scope.brandList={data:[{id:1,text:'联想'},{id:2,text:'华为'},{id:3,text:'小米'}]};
	$scope.findBrandList = function() {
		brandService.findBrandList().success(function (resp) {
			$scope.brandList = {"data": resp};
		})
	}
	//$scope.specList={data:[{id:1,text:'屏幕尺寸'},{id:2,text:'网络制式'},{id:3,text:'尺码'}]};
	$scope.findSpecList = function() {
		specificationService.findSpecList().success(function (resp) {
			$scope.specList = {"data": resp};
		})
	}
});	
