package com.kob.matchingsystem;

import com.kob.matchingsystem.service.MatchingService;
import com.kob.matchingsystem.service.impl.MatchingServiceImpl;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

@SpringBootApplication
public class MatchingSystemApplication {
    public static void main(String[] args) {
        MatchingServiceImpl.matchingPool.start();
        //线程进入就绪态 与springboot一同启动
        SpringApplication.run(MatchingSystemApplication.class, args);
    }
}