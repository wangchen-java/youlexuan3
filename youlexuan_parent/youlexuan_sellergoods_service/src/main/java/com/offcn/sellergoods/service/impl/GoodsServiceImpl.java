package com.offcn.sellergoods.service.impl;

import com.alibaba.dubbo.config.annotation.Service;
import com.alibaba.fastjson.JSON;
import com.github.pagehelper.Page;
import com.github.pagehelper.PageHelper;
import com.offcn.entity.PageResult;
import com.offcn.group.Goods;
import com.offcn.mapper.*;
import com.offcn.pojo.*;
import com.offcn.pojo.TbGoodsExample.Criteria;
import com.offcn.sellergoods.service.GoodsService;
import org.springframework.beans.factory.annotation.Autowired;

import java.util.Arrays;
import java.util.Date;
import java.util.List;
import java.util.Map;

/**
 * goods服务实现层
 * @author senqi
 *
 */
@Service
public class GoodsServiceImpl implements GoodsService {

	@Autowired
	private TbGoodsMapper goodsMapper;
	@Autowired
	private TbGoodsDescMapper goodsDescMapper;
	@Autowired
	private TbBrandMapper brandMapper;
	@Autowired
	private TbItemCatMapper itemCatMapper;
	@Autowired
	private TbSellerMapper sellerMapper;
	@Autowired
	private TbItemMapper itemMapper;
	/**
	 * 查询全部
	 */
	@Override
	public List<TbGoods> findAll() {
		return goodsMapper.selectByExample(null);
	}

	/**
	 * 分页
	 */
	@Override
	public PageResult findPage(int pageNum, int pageSize) {
		PageHelper.startPage(pageNum, pageSize);		
		Page<TbGoods> page = (Page<TbGoods>) goodsMapper.selectByExample(null);
		return new PageResult(page.getTotal(), page.getResult());
	}

	/**
	 * 增加
	 */
	@Override
	public void add(Goods goods) {
		goodsMapper.insert(goods.getGoods());
		goods.getGoodsDesc().setGoodsId(goods.getGoods().getId());
		goodsDescMapper.insert(goods.getGoodsDesc());
		saveItem(goods);
	}
	private void saveItem(Goods goods){
		if (goods.getGoods().getIsEnableSpec().equals("1")){
			for (TbItem item : goods.getItemList()){
				Map<String, String> map = JSON.parseObject(item.getSpec(), Map.class);
				String title = goods.getGoods().getGoodsName();
				for (String key : map.keySet()) {
					title += " " + map.get(key);
				}
				item.setTitle(title);

				setItem(goods, item);

				itemMapper.insert(item);
			}
		}
		else {
			TbItem item = new TbItem();

			//价格
			item.setPrice(goods.getGoods().getPrice());
			//库存数量
			item.setNum(999);
			//状态
			item.setStatus("1");
			//是否默认
			item.setIsDefault("1");
			item.setSpec("{}");

			// spu的名字
			item.setTitle(goods.getGoods().getGoodsName());

			// alt + shift + M：抽取方法
			setItem(goods, item);

			itemMapper.insert(item);
		}
	}

	private void setItem(Goods goods, TbItem item){
		List<Map> maps  = JSON.parseArray(goods.getGoodsDesc().getItemImages(), Map.class);
		if (maps.size()>0){
			item.setImage(maps.get(0).get("url")  +"");
		}
		item.setCategoryid(goods.getGoods().getCategory3Id());
		item.setCreateTime(new Date());
		item.setUpdateTime(new Date());
		item.setGoodsId(goods.getGoods().getId());
		item.setSellerId(goods.getGoods().getSellerId());
		String brandName = brandMapper.selectByPrimaryKey(goods.getGoods().getBrandId()).getName();
		item.setBrand(brandName);
		String catName =itemCatMapper.selectByPrimaryKey(item.getCategoryid()).getName();
		item.setCategory(catName);
		String sellerName =sellerMapper.selectByPrimaryKey(item.getSellerId()).getNickName();
		item.setSeller(sellerName);

	}
	
	/**
	 * 修改
	 */
	@Override
	public void update(Goods goods){
		goodsMapper.updateByPrimaryKey(goods.getGoods());
		goodsDescMapper.updateByPrimaryKey(goods.getGoodsDesc());
		TbItemExample ex = new TbItemExample();
		TbItemExample.Criteria criteria = ex.createCriteria();
		criteria.andGoodsIdEqualTo(goods.getGoods().getId());
		itemMapper.deleteByExample(ex);
		saveItem(goods);
	}	
	
	/**
	 * 根据ID获取实体
	 * @param id
	 * @return
	 */
	@Override
	public Goods findOne(Long id){
		TbGoods goods = goodsMapper.selectByPrimaryKey(id);
		TbGoodsDesc goodsDesc = goodsDescMapper.selectByPrimaryKey(id);
		TbItemExample ex =new TbItemExample();
		TbItemExample.Criteria criteria = ex.createCriteria();
		criteria.andGoodsIdEqualTo(id);
		List<TbItem> itemList = itemMapper.selectByExample(ex);

		return new Goods(goods,goodsDesc,itemList);
	}

	/**
	 * 批量删除
	 */
	@Override
	public void delete(Long[] ids) {
		for(Long id:ids){
			// 这次。我们要重新定义一下 删除
			// 使用逻辑删除，对商品进行屏蔽

			TbGoods goods = goodsMapper.selectByPrimaryKey(id);
			// 1表示删除
			goods.setIsDelete("1");
			goodsMapper.updateByPrimaryKey(goods);

			// 将对应的sku的状态改为 3
			TbItemExample ex = new TbItemExample();
			TbItemExample.Criteria c = ex.createCriteria();
			c.andGoodsIdEqualTo(id);
			List<TbItem> itemList = itemMapper.selectByExample(ex);

			for (TbItem item : itemList) {
				// 3表示删除
				item.setStatus("3");
				itemMapper.updateByPrimaryKey(item);
			}
		}
	}
	
	/**
	 * 分页+查询
	 */
	@Override
	public PageResult findPage(TbGoods goods, int pageNum, int pageSize) {
		PageHelper.startPage(pageNum, pageSize);

		TbGoodsExample example=new TbGoodsExample();
		Criteria criteria = example.createCriteria();

		if(goods != null){
			if(goods.getSellerId() != null && goods.getSellerId().length() > 0){

				criteria.andSellerIdEqualTo(goods.getSellerId());

			}			if(goods.getGoodsName() != null && goods.getGoodsName().length() > 0){
				criteria.andGoodsNameLike("%" + goods.getGoodsName() + "%");
			}			if(goods.getAuditStatus() != null && goods.getAuditStatus().length() > 0){
				criteria.andAuditStatusLike("%" + goods.getAuditStatus() + "%");
			}			if(goods.getIsMarketable() != null && goods.getIsMarketable().length() > 0){
				criteria.andIsMarketableLike("%" + goods.getIsMarketable() + "%");
			}			if(goods.getCaption() != null && goods.getCaption().length() > 0){
				criteria.andCaptionLike("%" + goods.getCaption() + "%");
			}			if(goods.getSmallPic() != null && goods.getSmallPic().length() > 0){
				criteria.andSmallPicLike("%" + goods.getSmallPic() + "%");
			}			if(goods.getIsEnableSpec() != null && goods.getIsEnableSpec().length() > 0){
				criteria.andIsEnableSpecLike("%" + goods.getIsEnableSpec() + "%");
			}			if(goods.getIsDelete() != null && goods.getIsDelete().length() > 0){
				criteria.andIsDeleteLike("%" + goods.getIsDelete() + "%");
			}
		}

		// 屏蔽已经删除的商品
		criteria.andIsDeleteIsNull();

		Page<TbGoods> page= (Page<TbGoods>)goodsMapper.selectByExample(example);
		return new PageResult(page.getTotal(), page.getResult());
	}
	@Override
	public void updateStatus(Long[] ids, String status) {
		for (Long id : ids) {
			TbGoods goods = goodsMapper.selectByPrimaryKey(id);
			goods.setAuditStatus(status);
			goodsMapper.updateByPrimaryKey(goods);
		}

	}

	@Override
	public List<TbItem> findItemListByGoodsId(Long[] ids, String status) {
		TbItemExample ex =new TbItemExample();
		TbItemExample.Criteria criteria = ex.createCriteria();
		criteria.andGoodsIdIn(Arrays.asList(ids));
		criteria.andStatusEqualTo(status);
		return itemMapper.selectByExample(ex);
	}

}
