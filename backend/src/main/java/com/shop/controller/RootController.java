package com.shop.controller;

import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;

/**
 * 根路径控制器
 * 用于处理根路径的访问请求
 */
@Controller
public class RootController {

    /**
     * 处理根路径请求，重定向到Swagger UI页面
     */
    @GetMapping("/")
    public String redirectToSwagger() {
        return "redirect:/swagger-ui.html";
    }
}