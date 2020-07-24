package com.offcn.seckill.service.impl;

import com.alibaba.dubbo.config.annotation.Service;
import com.github.pagehelper.Page;
import com.github.pagehelper.PageHelper;
import com.offcn.entity.PageResult;
import com.offcn.mapper.TbSeckillGoodsMapper;
import com.offcn.mapper.TbSeckillOrderMapper;
import com.offcn.pojo.TbSeckillGoods;
import com.offcn.pojo.TbSeckillOrder;
import com.offcn.pojo.TbSeckillOrderExample;
import com.offcn.pojo.TbSeckillOrderExample.Criteria;
import com.offcn.seckill.service.SeckillOrderService;
import com.offcn.util.IdWorker;
import com.offcn.util.RedisLock;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.RedisTemplate;

import java.util.Date;
import java.util.List;

/**
 * seckill_order服务实现层
 * @author senqi
 *
 */
@Service
public class SeckillOrderServiceImpl implements SeckillOrderService {

	@Autowired
	private TbSeckillOrderMapper seckillOrderMapper;
	@Autowired
	private TbSeckillGoodsMapper seckillGoodsMapper;
	@Autowired
	private IdWorker idWorker;
	@Autowired
	private RedisTemplate redisTemplate;
	@Autowired
	private RedisLock redisLock;
	@Override
	public void submitOrder(Long skId, String userId) {
		String key ="seckillOrderTest";
		long time =1000;
		String val =String.valueOf(System.currentTimeMillis() + 1000);
		boolean lock =redisLock.lock(key,val);
		if (lock) {
			TbSeckillGoods skGood = (TbSeckillGoods) redisTemplate.boundHashOps("seckillGoods").get(skId);
			if (skGood == null) {
				throw new RuntimeException("该商品不存在");
			}
			if (skGood.getStockCount() == 0) {
				throw new RuntimeException("该商品已售罄");
			}
			skGood.setStockCount(skGood.getStockCount() - 1);
			redisTemplate.boundHashOps("seckillGoods").put(skId, skGood);
			if (skGood.getStockCount() == 0) {
				seckillGoodsMapper.updateByPrimaryKey(skGood);
				redisTemplate.boundHashOps("seckillGoods").delete(skId);
			}
			TbSeckillOrder order = new TbSeckillOrder();
			order.setId(idWorker.nextId());
			order.setSeckillId(skId);
			order.setMoney(skGood.getCostPrice());
			order.setUserId(userId);
			order.setSellerId(skGood.getSellerId());
			order.setCreateTime(new Date());
			// 未支付
			order.setStatus("0");
			redisTemplate.boundHashOps("seckillOrder").put(userId, order);
		}
		redisLock.unlock(key,val);
	}

	@Override
	public void saveOrderFromRedisToDb(String userId) {

		TbSeckillOrder order = (TbSeckillOrder) redisTemplate.boundHashOps("seckillOrder").get(userId);

		order.setPayTime(new Date());
		// 已支付
		order.setStatus("1");
		seckillOrderMapper.insert(order);

		// 保存订单成功后，从redis中移除该订单
		redisTemplate.boundHashOps("seckillOrder").delete(userId);
	}

	@Override
	public void deleteOrderFromRedis(String userId) {
		// 还原库存
		TbSeckillOrder order = (TbSeckillOrder) redisTemplate.boundHashOps("seckillOrder").get(userId);
		TbSeckillGoods good = (TbSeckillGoods) redisTemplate.boundHashOps("seckillGoods").get(order.getSeckillId());
		if(good == null) {
			good = seckillGoodsMapper.selectByPrimaryKey(order.getSeckillId());
		}
		good.setStockCount(good.getStockCount() + 1);
		redisTemplate.boundHashOps("seckillGoods").put(good.getId(), good);

		// 从redis中将该订单移除
		redisTemplate.boundHashOps("seckillOrder").delete(userId);
	}
}
