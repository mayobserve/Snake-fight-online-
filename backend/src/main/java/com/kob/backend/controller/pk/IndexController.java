package com.kob.backend.controller.pk;

import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;

@Controller
@RequestMapping("/pk/")
//加上父目录,链接会通过controller找这个链接

public class IndexController {

    @RequestMapping("index/")
    //url就是pk下 index/子目录
    public String index(){
        return "/pk/index.html";
        //返回resource/templates下对应html页面
    }
}
