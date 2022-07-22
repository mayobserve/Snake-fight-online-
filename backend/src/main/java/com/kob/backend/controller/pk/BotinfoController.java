package com.kob.backend.controller.pk;

import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
//默认返回一个数据，controller是返回一个模板
@RequestMapping("/pk/")
public class BotinfoController {

    @RequestMapping("getbotinfo/")
    public String getBotInfo(){
        return "hhh";
    }
}
