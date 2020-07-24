package com.offcn.search.service.impl;

import com.alibaba.dubbo.config.annotation.Service;
import com.alibaba.fastjson.JSON;
import com.github.promeg.pinyinhelper.Pinyin;
import com.offcn.pojo.TbItem;
import com.offcn.search.service.ItemSearchService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Sort;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.solr.core.SolrTemplate;
import org.springframework.data.solr.core.query.*;
import org.springframework.data.solr.core.query.result.*;
import org.springframework.web.bind.annotation.RequestBody;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Service
public class ItemSearchServiceImpl implements ItemSearchService {
    @Autowired
    private SolrTemplate solrTemplate;
    @Autowired
    private RedisTemplate redisTemplate;
    @Override
    public Map<String, Object> search(Map searchMap) {
        String keywords = (String) searchMap.get("keywords");
        String newKeywords = keywords.replaceAll(" ", "");
        searchMap.put("keywords",newKeywords);

        Map<String, Object> map = new HashMap<>();
        hiSearch(searchMap,map);
        categoryListSearch(searchMap,map);
        String cat = (String) searchMap.get("category");
        if (!"".equals(cat)){
            brandAndSpecSearch(cat,map);
        }else {
            List<String> catList = (List<String>) map.get("categoryList");
            if (catList.size()>0){
                brandAndSpecSearch(catList.get(0),map);
            }
        }
        return map;
    }

    @Override
    public void importList(List<TbItem> itemList) {
        for (TbItem item: itemList){
            Map<String,String> map = JSON.parseObject(item.getSpec(),Map.class);
            Map<String ,String> newMap =new HashMap<>();
            for (String key : map.keySet()){
                newMap.put(Pinyin.toPinyin(key,"").toLowerCase(),map.get(key));
            }
            item.setSpecMap(newMap);
        }
        solrTemplate.saveBeans(itemList);
        solrTemplate.commit();
    }

    @Override
    public void deleteList(Long[] ids) {
        Query query =new SimpleQuery();
        Criteria c =new Criteria("item_goodsid");
        c.in(ids);
        query.addCriteria(c);
        solrTemplate.delete(query);
        solrTemplate.commit();
    }

    private void hiSearch(Map searchMap,Map<String, Object> map) {

        HighlightQuery query = new SimpleHighlightQuery();
        HighlightOptions highlightOptions = new HighlightOptions().addField("item_title");
        highlightOptions.setSimplePrefix("<span style='color : red'>");
        highlightOptions.setSimplePostfix("</span>");
        query.setHighlightOptions(highlightOptions);
        Criteria criteria = new Criteria("item_keywords").is(searchMap.get("keywords"));
        query.addCriteria(criteria);
        String category=(String) searchMap.get("category");
        if (!category.equals("")){
            FilterQuery catFq =new SimpleFilterQuery();
            Criteria catCri =new Criteria("item_category").is(category);
            catFq.addCriteria(catCri);
            query.addFilterQuery(catFq);
        }
        String brand =(String) searchMap.get("brand");
        if (!brand.equals("")){
            FilterQuery brandFq =new SimpleFilterQuery();
            Criteria brandCri =new Criteria("item_brand").is(brand);
            brandFq.addCriteria(brandCri);
            query.addFilterQuery(brandFq);
        }
        Map<String,String> specMap = (Map<String, String>) searchMap.get("spec");
        if (specMap!=null){
            for (String key : specMap.keySet()){
                FilterQuery fq = new SimpleFilterQuery();
                Criteria cri =new Criteria("item_spec_"+ Pinyin.toPinyin(key,"").toLowerCase()).is(specMap.get(key));
                fq.addCriteria(cri);
                query.addFilterQuery(fq);
            }
        }
        //价格区间过滤
        String price =(String) searchMap.get("price");
        if (!price.equals("")) {
            String[] priceArr = price.split("-");
            //最低价
            FilterQuery minPriceFq = new SimpleFilterQuery();
            Criteria minPriceCri = new Criteria("item_price").greaterThanEqual(priceArr[0]);
            minPriceFq.addCriteria(minPriceCri);
            query.addFilterQuery(minPriceFq);
            //最高价
            if (!"*".equals(priceArr[1])){
                FilterQuery maxPriceFq = new SimpleFilterQuery();
                Criteria maxPriceCri = new Criteria("item_price").lessThanEqual(priceArr[1]);
                maxPriceFq.addCriteria(maxPriceCri);
                query.addFilterQuery(maxPriceFq);
            }
        }
        //分页
        Integer pageNo = (Integer) searchMap.get("pageNo");
        Integer pageSize = (Integer) searchMap.get("pageSize");
        query.setOffset((pageNo - 1) * pageSize);
        query.setRows(pageSize);
        //排序
        String sortField =(String) searchMap.get("sortField");
        String sortStr =(String) searchMap.get("sort");
        if (!"".equals(sortField)){
           Sort sort =null;
           if ("desc".equals(sortStr)){
               sort =new Sort(Sort.Direction.DESC,sortField);
           }else {
               sort =new Sort(Sort.Direction.ASC,sortField);
           }
           query.addSort(sort);

        }

        HighlightPage<TbItem> page = solrTemplate.queryForHighlightPage(query, TbItem.class);
        List<HighlightEntry<TbItem>> highlighted = page.getHighlighted();
        for (HighlightEntry<TbItem> h : highlighted) {
            TbItem item = h.getEntity();
            if (h.getHighlights().size() > 0 && h.getHighlights().get(0).getSnipplets().size() > 0) {
                item.setTitle(h.getHighlights().get(0).getSnipplets().get(0));

            }
        }
        map.put("rows", page.getContent());
        //返回总页数
        map.put("totalPages", page.getTotalPages());
        //返回总记录数
        map.put("total", page.getTotalElements());

    }
    private void categoryListSearch(Map searchMap, Map<String, Object> map){
        List<String> list=new ArrayList<>();
        Query query =new SimpleQuery();
        Criteria criteria =new Criteria("item_keywords").is(searchMap.get("keywords"));
        query.addCriteria(criteria);
        GroupOptions groupOptions =new GroupOptions().addGroupByField("item_category");
        query.setGroupOptions(groupOptions);
        GroupPage<TbItem> tbItems = solrTemplate.queryForGroupPage(query, TbItem.class);
        GroupResult<TbItem> groupResult = tbItems.getGroupResult("item_category");
        Page<GroupEntry<TbItem>> groupEntries = groupResult.getGroupEntries();
        for (GroupEntry<TbItem> entry: groupEntries) {
            list.add(entry.getGroupValue());
        }
        map.put("categoryList",list);
    }
    private void brandAndSpecSearch(String catName,Map<String, Object> map){

        Long typeId = (Long) redisTemplate.boundHashOps("itemCat").get(catName);
        List<Map> brandList = (List<Map>) redisTemplate.boundHashOps("brandList").get(typeId);
        List<Map> specList = (List<Map>) redisTemplate.boundHashOps("specList").get(typeId);
        map.put("brandList",brandList);
        map.put("specList",specList);

    }
}
