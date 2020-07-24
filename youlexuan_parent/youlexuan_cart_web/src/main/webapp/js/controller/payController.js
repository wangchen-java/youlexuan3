//cart控制层
app.controller('payController', function ($scope,$location, payService) {
    //进入页面,预下单请求
        $scope.createCode=function () {
            payService.createCode().success(function (resp) {
                if (resp){

                    $scope.out_trade_no= resp.out_trade_no;
                    $scope.total_fee=resp.total_fee;
                    var qr = new QRious({
                        element: document.getElementById('mycode'),
                        size: 250,
                        level: 'H',
                        value: resp.qrcode
                    })
                    queryPayStatus($scope.out_trade_no)
                }
            })
        }
        queryPayStatus=function(out_trade_no){
            payService.queryPayStatus(out_trade_no).success(function (resp) {
                if (resp.success){
                    location.href ="paysuccess.html#?money=" +$scope.total_fee;
                }else {
                   if (resp.message == '二维码超时'){
                       $scope.timeoutMsg = true;
                   } else {
                       alert(resp.message);
                   }
                }
            })
    }
        $scope.getMoney =function () {
            $scope.money=$location.search()['money'];
        }
});
