package com.offcn.listen;

import com.alibaba.fastjson.JSON;
import com.offcn.pojo.TbItem;
import com.offcn.search.service.ItemSearchService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import javax.jms.JMSException;
import javax.jms.Message;
import javax.jms.MessageListener;
import javax.jms.TextMessage;
import java.util.List;

@Component
public class ImportSolrListener implements MessageListener {

    @Autowired
    private ItemSearchService itemSearchService;

    @Override
    public void onMessage(Message message) {
        try {
            TextMessage text = (TextMessage)message;
            String str = text.getText();

            List<TbItem> itemList = JSON.parseArray(str, TbItem.class);

            itemSearchService.importList(itemList);

            System.out.println(">>>>>receive import solr msg");
        } catch (JMSException e) {
            e.printStackTrace();
        }
    }

}
