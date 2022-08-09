package com.kob.backend.controller.user.account;

import com.kob.backend.service.user.account.LoginService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.Map;

@RestController
//controller的注解
//某个类是否可以接收HTTP请求,直接返回数据
public class LoginController {
    @Autowired//注入定义的接口，autowired
    private LoginService loginService;

    @PostMapping("/user/account/token/")
    //用post命令 密文传输，安全
    public Map<String, String> getToken(@RequestParam Map<String, String> map) {
        String username = map.get("username");
        String password = map.get("password");
        return  loginService.getToken(username, password);
    }
}
