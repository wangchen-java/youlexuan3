package com.offcn.cart.service.impl;

import com.alibaba.dubbo.config.annotation.Service;
import com.offcn.cart.service.CartService;
import com.offcn.group.Cart;
import com.offcn.mapper.TbItemMapper;
import com.offcn.pojo.TbItem;
import com.offcn.pojo.TbOrderItem;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.RedisTemplate;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;
@Service
public class CartServiceImpl implements CartService {
    @Autowired
    private TbItemMapper itemMapper;
    @Autowired
    private RedisTemplate redisTemplate;
    @Override
    public List<Cart> addGoodsToCartList(List<Cart> cartList, Long skuId, Integer num) {
        TbItem item = itemMapper.selectByPrimaryKey(skuId);
        if (item == null){
            throw new RuntimeException("该商品不存在");
        }
        if (!"1".equals(item.getStatus())){
            throw new RuntimeException("该商品暂不支持购买");
        }
        String sellerId = item.getSellerId();
        Cart cart = searchSellerInCartList(cartList, sellerId);
        if (cart ==null){
            cart =new Cart();
            cart.setSellerId(sellerId);
            cart.setSellerName(item.getSeller());
            List<TbOrderItem> orderItemList =new ArrayList<>();
            TbOrderItem orderItem =createOrderItem(item,num);
            orderItemList.add(orderItem);
            cart.setOrderItemList(orderItemList);
            cartList.add(cart);
        }else {
           TbOrderItem orderItem= searchSkuInOrderItemList(cart.getOrderItemList(),skuId);
           if (orderItem ==null){
                orderItem =createOrderItem(item,num);
                cart.getOrderItemList().add(orderItem);
           }else{
                orderItem.setNum(orderItem.getNum() + num);
                orderItem.setTotalFee(new BigDecimal(orderItem.getNum() * orderItem.getPrice().doubleValue()));
                if (orderItem.getNum() <1){
                    cart.getOrderItemList().remove(orderItem);
                }
                if (cart.getOrderItemList().size() <1){
                    cartList.remove(cart);
                }
           }
        }


        return cartList;
    }

    @Override
    public List<Cart> findCartListFromRedis(String username) {
       List<Cart> cartList= (List<Cart>) redisTemplate.boundHashOps("cartList").get(username);
       if (cartList ==null){
           cartList =new ArrayList<>();
       }
        return cartList;
    }

    @Override
    public void saveCartListToRedis(String username, List<Cart> cartList) {
        redisTemplate.boundHashOps("cartList").put(username,cartList);
    }

    //查看某个列表是否存在
    private TbOrderItem searchSkuInOrderItemList(List<TbOrderItem> orderItemList, Long skuId) {
        for (TbOrderItem orderItem : orderItemList) {
            if (orderItem.getItemId().longValue() == skuId.longValue()){
                return orderItem;
            }
        }
        return null;
    }
//查找某个商家是否存在
    private Cart searchSellerInCartList(List<Cart> cartList,String sellerId){
        for (Cart cart :cartList){
            if (cart.getSellerId().equals(sellerId)){
                return cart;
            }
        }
        return null;
    }
    private TbOrderItem createOrderItem(TbItem item,Integer num){
        TbOrderItem orderItem =new TbOrderItem();
        orderItem.setItemId(item.getId());
        orderItem.setGoodsId(item.getGoodsId());
        orderItem.setTitle(item.getTitle());
        orderItem.setPrice(item.getPrice());
        orderItem.setNum(num);
        orderItem.setTotalFee(new BigDecimal(item.getPrice().doubleValue() * num));
        orderItem.setPicPath(item.getImage());
        orderItem.setSellerId(item.getSellerId());
        return orderItem;
    }

}
