package com.offcn.order.service.impl;

import com.alibaba.dubbo.config.annotation.Service;
import com.offcn.group.Cart;
import com.offcn.mapper.TbOrderItemMapper;
import com.offcn.mapper.TbOrderMapper;
import com.offcn.mapper.TbPayLogMapper;
import com.offcn.order.service.OrderService;
import com.offcn.pojo.TbOrder;
import com.offcn.pojo.TbOrderItem;
import com.offcn.pojo.TbPayLog;
import com.offcn.util.IdWorker;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.RedisTemplate;

import java.math.BigDecimal;
import java.util.Date;
import java.util.List;

@Service
public class OrderServiceImpl implements OrderService {
    @Autowired
    private RedisTemplate redisTemplate;
    @Autowired
    private TbOrderMapper orderMapper;
    @Autowired
    private TbOrderItemMapper orderItemMapper;
    @Autowired
    private IdWorker idWorker;
    @Autowired
    private TbPayLogMapper payLogMapper;
    @Override
    public void add(TbOrder order) {
        List<Cart> cartList = (List<Cart>) redisTemplate.boundHashOps("cartList").get(order.getUserId());
        TbPayLog payLog =new TbPayLog();
        double total =0.0;
        StringBuilder sb =new StringBuilder("");
        for (Cart cart : cartList) {
            long orderId = idWorker.nextId();
            sb.append(orderId + ",");
            order.setOrderId(orderId);
            double sum = 0.0;
            for (TbOrderItem orderItem : cart.getOrderItemList()) {
                sum += orderItem.getTotalFee().doubleValue();
                orderItem.setId(idWorker.nextId());
                orderItem.setOrderId(orderId);
                orderItemMapper.insert(orderItem);
            }
            total += sum;
            order.setPayment(new BigDecimal(sum));
            order.setSellerId(cart.getSellerId());
            orderMapper.insert(order);
        }

        payLog.setOutTradeNo(idWorker.nextId() + "");
        payLog.setCreateTime(new Date());
        payLog.setUserId(order.getUserId());
        payLog.setTradeState("0");
        payLog.setPayType(order.getPaymentType());
        payLog.setTotalFee(new BigDecimal(total));
        sb.deleteCharAt(sb.length() - 1 );
        payLog.setOrderList(sb.toString());
        payLogMapper.insert(payLog);
        redisTemplate.boundHashOps("payLogList").put(order.getUserId(),payLog);
        redisTemplate.boundHashOps("cartList").delete(order.getUserId());
    }

    @Override
    public void updateTradeStatus(String out_trade_no) {
        TbPayLog payLog = payLogMapper.selectByPrimaryKey(out_trade_no);
        payLog.setPayTime(new Date());
        payLog.setTradeState("1");
        payLogMapper.updateByPrimaryKey(payLog);
        String orderList = payLog.getOrderList();
        String[] split = orderList.split(",");
        for (String s : split) {
            TbOrder tbOrder = orderMapper.selectByPrimaryKey(Long.parseLong(s));
            tbOrder.setPaymentTime(new Date());
            tbOrder.setStatus("1");
            orderMapper.updateByPrimaryKey(tbOrder);

        }


    }
}
