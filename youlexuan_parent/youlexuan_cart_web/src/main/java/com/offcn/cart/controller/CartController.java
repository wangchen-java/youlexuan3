package com.offcn.cart.controller;

import com.alibaba.dubbo.config.annotation.Reference;
import com.alibaba.fastjson.JSON;
import com.offcn.cart.service.CartService;
import com.offcn.entity.Result;
import com.offcn.group.Cart;
import com.offcn.pojo.TbOrderItem;
import com.offcn.util.CookieUtil;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.CookieValue;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.util.List;

@RestController
@RequestMapping("/cart")
public class CartController {
    @Reference
    private CartService cartService;
    @Autowired
    private HttpServletRequest request;
    @Autowired
    private HttpServletResponse response;
    @RequestMapping("/findCartList")
    public List<Cart> findCartList(){
        String loginName = SecurityContextHolder.getContext().getAuthentication().getName();
        System.out.println("loginName ====" + loginName);
        //不管登陆与否,都要取cookie的购物车
        String cookieStr = CookieUtil.getCookieValue(request, "cartList", "utf-8");
        if (cookieStr ==null || "".equals(cookieStr)){
            cookieStr ="[]";
        }
        List<Cart> cookie_cartList = JSON.parseArray(cookieStr, Cart.class);
        if ("anonymousUser".equals(loginName)){

            return cookie_cartList;
        }else {
            List<Cart> redis_cartList = cartService.findCartListFromRedis(loginName);
            if (cookie_cartList.size() > 0){
                 for (Cart cart :cookie_cartList){
                     for (TbOrderItem orderItem : cart.getOrderItemList()) {
                        redis_cartList = cartService.addGoodsToCartList(redis_cartList,orderItem.getItemId(),orderItem.getNum());
                     }
                 }
                System.out.println("he bi wan cheng");
            }
            CookieUtil.deleteCookie(request,response,"cartList");
            cartService.saveCartListToRedis(loginName,redis_cartList);
            return redis_cartList;
        }


    }
    @RequestMapping("/addCart")
    public Result addCart(Long skuId,Integer num){
        try {
            response.setHeader("Access-Control-Allow-Origin", "http://localhost:9009");
            response.setHeader("Access-Control-Allow-Credentials", "true");

            List<Cart> oldCartList = findCartList();
            List<Cart> newCartList = cartService.addGoodsToCartList(oldCartList, skuId, num);
            String loginName = SecurityContextHolder.getContext().getAuthentication().getName();
            if ("anonymousUser".equals(loginName)){
                CookieUtil.setCookie(request,response,"cartList",JSON.toJSONString(newCartList),7 * 3600 * 24,"utf-8");
                System.out.println("save cartList to cookie");
            }else {
                cartService.saveCartListToRedis(loginName,newCartList);
                System.out.println("save cartList to redis");
            }
            return new Result(true,"添加购物车成功");
        }catch (Exception e){
            e.printStackTrace();
            return new Result(false,e.getMessage());
        }
    }
}
