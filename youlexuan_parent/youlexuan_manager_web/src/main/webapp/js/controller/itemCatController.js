//item_cat控制层 
app.controller('itemCatController' ,function($scope, $controller, itemCatService,typeTemplateService){
	
	// 继承
	$controller("baseController", {
		$scope : $scope
	});
	
	// 保存
	$scope.save = function() {
		itemCatService.save($scope.entity,$scope.parentId).success(function(response) {
			if (response.success) {
				// 重新加载
				$scope.findByParentId($scope.parentId);
			} else {
				alert(response.message);
			}
		});
	}
	
	//查询实体 
	$scope.findOne = function(id){				
		itemCatService.findOne(id).success(
			function(response){
				$scope.entity= response;					
			}
		);				
	}
	
	//批量删除 
	$scope.dele = function(){			
		//获取选中的复选框			
		itemCatService.dele($scope.selectIds).success(
			function(response){
				if(response.success){
					$scope.findByParentId($scope.parentId);
					$scope.selectIds=[];
				}
				alert(response.message);
			}		
		);				
	}
	
	// 定义搜索对象 
	$scope.searchEntity = {};
	// 搜索
	$scope.search = function(page,size){			
		itemCatService.search(page,size,$scope.searchEntity).success(
			function(response){
				$scope.list=response.rows;	
				$scope.paginationConf.totalItems=response.total;
			}			
		);
	}
	$scope.parentId=0;
	$scope.findByParentId=function (parentId) {
		$scope.parentId=parentId;
		itemCatService.findByParentId(parentId).success(function (resp) {
				$scope.list=resp;
		})
	}

	$scope.grade=1;
	$scope.setGrade=function (val) {
		$scope.grade=val;
	}
    $scope.selectList=function (pEntity) {
		if ($scope.grade==1){
			$scope.entity1=null;
			$scope.entity2=null;
		}
		if ($scope.grade==2){
			$scope.entity1=pEntity;
			$scope.entity2=null;
		}
		if ($scope.grade==3){
			$scope.entity2=pEntity;
		}
		$scope.findByParentId(pEntity.id);
	}
	$scope.findTypeTemplateList=function () {
		typeTemplateService.findTypeTemplateList().success(function (resp) {
			if (resp){
				$scope.typeTemplateList = {"data":resp};
			}
		})
	}
});	
