package com.offcn.shop.controller;


import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/index")
public class IndexController {

    @RequestMapping("/getName")
    public String getName() {
        // 需要从 spring security 中获取登录名字
        return SecurityContextHolder.getContext().getAuthentication().getName();
    }

}
