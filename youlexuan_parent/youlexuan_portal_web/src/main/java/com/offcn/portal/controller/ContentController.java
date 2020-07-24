package com.offcn.portal.controller;

import com.alibaba.dubbo.config.annotation.Reference;
import com.offcn.content.service.ContentService;
import com.offcn.pojo.TbContent;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/content")
public class ContentController {
    //v1.0.1升级版
    @Reference
    private ContentService contentService;

    @RequestMapping("/findContentByCatId")
    public List<TbContent> findContentByCatId(Long catId){
        return contentService.findContentByCatId(catId);
    }
}
