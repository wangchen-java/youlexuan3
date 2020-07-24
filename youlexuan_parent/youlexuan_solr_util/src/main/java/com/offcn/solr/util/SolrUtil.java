package com.offcn.solr.util;



import com.alibaba.fastjson.JSON;
import com.github.promeg.pinyinhelper.Pinyin;
import com.offcn.mapper.TbItemMapper;
import com.offcn.pojo.TbItem;
import com.offcn.pojo.TbItemExample;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.solr.core.SolrTemplate;
import org.springframework.data.solr.core.query.SimpleQuery;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;

import java.util.HashMap;
import java.util.List;
import java.util.Map;


@RunWith(SpringJUnit4ClassRunner.class)
@ContextConfiguration(locations = "classpath*:spring-*.xml")
public class SolrUtil {
    @Autowired
    private TbItemMapper itemMapper;
    @Autowired
    private SolrTemplate solrTemplate;
    @Test
    public void importItemData(){
        TbItemExample example =new TbItemExample();
        TbItemExample.Criteria criteria = example.createCriteria();
        criteria.andStatusEqualTo("1");
        List<TbItem> tbItems = itemMapper.selectByExample(example);
        for (TbItem item: tbItems){
            Map<String,String> map = JSON.parseObject(item.getSpec(),Map.class);
            Map<String ,String> newMap =new HashMap<>();
            for (String key : map.keySet()){
                newMap.put(Pinyin.toPinyin(key,"").toLowerCase(),map.get(key));
            }
            item.setSpecMap(newMap);
            System.out.println(item.getTitle() + ">>>>" + item.getSpec());
        }
        solrTemplate.saveBeans(tbItems);
        solrTemplate.commit();
    }

@Test
    public void delData(){
    SimpleQuery query = new SimpleQuery("*:*");
    solrTemplate.delete(query);
    solrTemplate.commit();
}
}
