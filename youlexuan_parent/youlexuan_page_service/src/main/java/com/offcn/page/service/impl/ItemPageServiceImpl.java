package com.offcn.page.service.impl;

import com.alibaba.dubbo.config.annotation.Service;
import com.offcn.mapper.TbGoodsDescMapper;
import com.offcn.mapper.TbGoodsMapper;
import com.offcn.mapper.TbItemCatMapper;
import com.offcn.mapper.TbItemMapper;
import com.offcn.page.service.ItemPageService;
import com.offcn.pojo.*;
import freemarker.template.Configuration;
import freemarker.template.Template;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.web.servlet.view.freemarker.FreeMarkerConfigurer;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Service
public class ItemPageServiceImpl implements ItemPageService {
    @Value("${pagedir}")
    private String pagedir;
    @Autowired
    private FreeMarkerConfigurer freeMarkerConfigurer;
    @Autowired
    private TbGoodsMapper goodsMapper;
    @Autowired
    private TbGoodsDescMapper goodsDescMapper;
    @Autowired
    private TbItemCatMapper itemCatMapper;
    @Autowired
    private TbItemMapper itemMapper;
    @Override
    public boolean createHtml(Long goodsId) {
        FileWriter out =null;
         try {
            Configuration conf = freeMarkerConfigurer.getConfiguration();
             Template template = conf.getTemplate("item.ftl");
             Map map =new HashMap();
             //goods信息
             TbGoods goods = goodsMapper.selectByPrimaryKey(goodsId);
             //goodsDesc信息
             TbGoodsDesc goodsDesc = goodsDescMapper.selectByPrimaryKey(goodsId);
             //3级分类的名称
             String cat1Name = itemCatMapper.selectByPrimaryKey(goods.getCategory1Id()).getName();
             String cat2Name = itemCatMapper.selectByPrimaryKey(goods.getCategory2Id()).getName();
             String cat3Name = itemCatMapper.selectByPrimaryKey(goods.getCategory3Id()).getName();

             //对应的sku
             TbItemExample ex =new TbItemExample();
             TbItemExample.Criteria criteria = ex.createCriteria();
             criteria.andGoodsIdEqualTo(goodsId);
             criteria.andStatusEqualTo("1");
             ex.setOrderByClause("is_default desc");
             List<TbItem> itemList = itemMapper.selectByExample(ex);
             map.put("goods",goods);
             map.put("goodsDesc",goodsDesc);
             map.put("itemCat1",cat1Name);
             map.put("itemCat2",cat2Name);
             map.put("itemCat3",cat3Name);
             map.put("itemList",itemList);


             out =new FileWriter(pagedir + goodsId + ".html");
             template.process(map,out);
             return true;
         } catch (Exception e) {
            e.printStackTrace();
            return false;
        }finally {
             if (out != null){
                 try {
                     out.close();
                 } catch (IOException e) {
                     e.printStackTrace();
                 }
             }
         }

    }

    @Override
    public boolean deleteHtml(Long[] goodsIds) {
        try {
            for (Long id : goodsIds) {
                new File(pagedir + id + ".html").delete();
            }
            return true;
        }catch (Exception e){
            e.printStackTrace();
            return false;
        }

    }
}
