package com.kob.backend.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.client.RestTemplate;

@Configuration
public class RestTemplateConfig {
    @Bean//想取得谁 就加一个bean注解,未来用的时候再用autowire注入
    public RestTemplate getRestTemplate() {
        return new RestTemplate();//返回一个实例
        //RestTemplate是HTTP请求工具，提供了常见的REST请求方案的模版
        //常用于两个进程间通信
        //如GET请求、POST请求、PUT请求、DELETE请求
        //以及一些通用的请求执行方法 exchange 以及 execute
    }
}
