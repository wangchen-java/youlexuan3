//goods控制层 
app.controller('goodsController' ,function($scope, $controller,$location ,goodsService,uploadService,itemCatService,typeTemplateService){
	
	// 继承
	$controller("baseController", {
		$scope : $scope
	});
	
	// 保存
	$scope.save = function() {
		$scope.entity.goodsDesc.introduction = editor.html();

		goodsService.save($scope.entity).success(function (response) {
			if (response.success) {

				// 此处可以跳转到列表页：goods.html
				// location.href = "goods.html";

				// 如果不跳转，就清空entity
				$scope.entity = {goods: {isEnableSpec: 0}, goodsDesc: {itemImages: [], specificationItems: []}}

				// 清空富文本编辑器的内容
				editor.html("");

			} else {
				alert(response.message);
			}
		});
	}
	
	//查询实体 
	$scope.findOne = function(){
		var id = $location.search()["id"];

		if (id == null) {
			return;
		}

		goodsService.findOne(id).success(
			function (response) {
				$scope.entity = response;

				// 富文本编辑器
				editor.html($scope.entity.goodsDesc.introduction);

				//显示图片列表
				$scope.entity.goodsDesc.itemImages = JSON.parse($scope.entity.goodsDesc.itemImages);

				//显示扩展属性
				$scope.entity.goodsDesc.customAttributeItems = JSON.parse($scope.entity.goodsDesc.customAttributeItems);

				//规格
				$scope.entity.goodsDesc.specificationItems = JSON.parse($scope.entity.goodsDesc.specificationItems);
				// sku列表的 每个item 中的 spec属性也是json对象，需要转化
				for (var i = 0; i < $scope.entity.itemList.length; i++) {
					$scope.entity.itemList[i].spec = JSON.parse($scope.entity.itemList[i].spec);
				}

			}
		);
	}
	
	//批量删除 
	$scope.dele = function(){			
		//获取选中的复选框			
		goodsService.dele($scope.selectIds).success(
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
		goodsService.search(page,size,$scope.searchEntity).success(
			function(response){
				$scope.list=response.rows;	
				$scope.paginationConf.totalItems=response.total;
			}			
		);
	}
	$scope.upload=function () {
		uploadService.uploadFile().success(function (resp) {
			if (resp.success){
				$scope.image_entity.url=resp.message;

			}else{
				alert(resp.message);
			}
		})
	}
	$scope.entity = {goods: {isEnableSpec: 0}, goodsDesc: {itemImages: [], specificationItems: []}};
	$scope.saveImage =function () {
		$scope.entity.goodsDesc.itemImages.push($scope.image_entity);
	}
	$scope.removeImage =function (idx) {
		$scope.entity.goodsDesc.itemImages.splice(idx,1);
	}
	$scope.findItemCat1List =function (pId) {
		itemCatService.findByParentId(pId).success(function (resp) {
			if (resp){
				$scope.itemCat1List =resp;
			}
		})
	}
	//监听一级
	$scope.$watch("entity.goods.category1Id",function (newVal,oldVal) {
		if (newVal){
			itemCatService.findByParentId(newVal).success(function (resp) {
					if (resp){
						$scope.itemCat2List = resp;
						$scope.itemCat3List=[];
						$scope.entity.goods.typeTemplateId="";
					}
			})
		}
	})
	//监听二级
	$scope.$watch("entity.goods.category2Id",function (newVal,oldVal) {
		if (newVal){
			itemCatService.findByParentId(newVal).success(function (resp) {
				if (resp){
					$scope.itemCat3List = resp;
					$scope.entity.goods.typeTemplateId="";
				}
			})
		}
	})
	//监听三级
	$scope.$watch("entity.goods.category3Id",function (newVal,oldVal) {
		if (newVal){
			itemCatService.findOne(newVal).success(function (resp) {
				$scope.entity.goods.typeTemplateId =resp.typeId;
			})
		}
	})
	$scope.$watch("entity.goods.typeTemplateId", function (newVal, oldVal) {
		if (newVal) {
			typeTemplateService.findOne(newVal).success(function (resp) {
				if (resp) {
					$scope.typeTemplate = resp;
					// 进行json格式的转换
					$scope.typeTemplate.brandIds = JSON.parse($scope.typeTemplate.brandIds);//品牌列表

					if ($location.search()["id"] == null) {
						$scope.entity.goodsDesc.customAttributeItems = JSON.parse($scope.typeTemplate.customAttributeItems);//扩展属性
					}
				}
			});

			// 当模板id发生变化的时候，通过模板id获取规格、以及规格选项
			typeTemplateService.findSpecAndOptionList(newVal).success(function (resp) {
				if (resp) {
					$scope.specList = resp;
				}
			});

		} else {
			$scope.typeTemplate.brandIds = [];
		}
	})
	$scope.updateSpecAttribute = function ($event, specName, optionName) {
		// 需要添加的属性：entity.goodsDesc.specificationItems
		// [{"attributeName":"网络制式","attributeValue":["移动3G","移动4G","联通3G"]}]

		var obj = $scope.searchObjByKey($scope.entity.goodsDesc.specificationItems, "attributeName", specName);
		// 已经存在该specName
		if (obj != null) {
			if ($event.target.checked) {
				// 勾选
				obj.attributeValue.push(optionName);
			} else {
				obj.attributeValue.splice(obj.attributeValue.indexOf(optionName), 1);

				if (obj.attributeValue.length == 0) {
					$scope.entity.goodsDesc.specificationItems.splice($scope.entity.goodsDesc.specificationItems.indexOf(obj), 1);
				}
			}

		} else {
			$scope.entity.goodsDesc.specificationItems.push({
				"attributeName": specName,
				"attributeValue": [optionName]
			});
		}
	}
	$scope.createItemList =function () {
		$scope.entity.itemList = [{spec: {}, price: 0, num: 999, status: '1', isDefault: '0'}];
		var items = $scope.entity.goodsDesc.specificationItems;

		for (var i = 0; i < items.length; i++) {
			$scope.entity.itemList = addColumn($scope.entity.itemList, items[i].attributeName, items[i].attributeValue);
		}
	}

	//添加列值：不使用$scope,表示是该controller中的私有方法，页面不能调用
	addColumn = function (list, columnName, conlumnValues) {
		var newList = [];//新的集合
		for (var i = 0; i < list.length; i++) {
			var oldRow = list[i];
			for (var j = 0; j < conlumnValues.length; j++) {

				var newRow = JSON.parse(JSON.stringify(oldRow));//深克隆
				newRow.spec[columnName] = conlumnValues[j];

				newList.push(newRow);
			}
		}
		return newList;
	}
	// 商品状态的显示
	$scope.statusName = ['未审核', '已审核', '审核未通过', '关闭'];
	// 分类名字
	$scope.itemCatName = [];

	// 商品列表页面，显示分类的名字
	$scope.findItemCatList = function () {
		itemCatService.findAll().success(function (resp) {
			for (var i = 0; i < resp.length; i++) {
				$scope.itemCatName[resp[i].id] = resp[i].name;
			}
		});
	}
	$scope.isChecked = function (specName, optionName) {
		var obj = $scope.searchObjByKey($scope.entity.goodsDesc.specificationItems, "attributeName", specName);
		if(obj == null) {
			return false;
		} else {
			if(obj.attributeValue.indexOf(optionName) >= 0){
				return true;
			} else {
				return false;
			}
		}
	}
	$scope.updateStatus = function (status) {
		goodsService.updateStatus($scope.selectIds, status).success(function (resp) {
			if(resp.success) {
				$scope.reloadList();
			} else {
				alert(resp.message);
			}
		});
	}
});	
