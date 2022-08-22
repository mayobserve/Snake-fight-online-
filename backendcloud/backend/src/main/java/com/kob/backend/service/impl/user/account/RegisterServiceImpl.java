package com.kob.backend.service.impl.user.account;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.kob.backend.mapper.UserMapper;
import com.kob.backend.pojo.User;
import com.kob.backend.service.user.account.RegisterService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Service
public class RegisterServiceImpl implements RegisterService {
    @Autowired
    private UserMapper userMapper;//调用数据库的一系列方法

    @Autowired
    private PasswordEncoder passwordEncoder;//加密

    @Override
    public Map<String, String> register(String username, String password, String confirmedPassword) {
        Map<String, String> map =  new HashMap<>();
        if(username == null) {
            map.put("error_message", "用户名不能为空");
            return map;
        }
        if(password == null || confirmedPassword == null) {
            map.put("error_message", "密码不能为空");
            return map;
        }

        username = username.trim();//删去一些特殊字符
        if(username.length() == 0) {
            map.put("error_message", "用户名不能为空");
            return map;
        }
        if(password.length() == 0 || confirmedPassword.length() == 0) {
            //密码为空
            map.put("error_message", "密码不能为空");
            return map;
        }

        if(username.length() > 100) {
            map.put("error_message", "用户名长度不能大于100");
            return map;
        }
        if(password.length() > 100 || confirmedPassword.length() > 100) {
            map.put("error_message", "密码长度不能大于100");
            return map;
        }
        if(!password.equals(confirmedPassword)) {
            map.put("error_message", "两次输入的密码不一致");
            return map;
        }

        QueryWrapper<User> queryWrapper = new QueryWrapper<User>();//生成条件构造器
        queryWrapper.eq("username", username);//条件
        List<User> users = userMapper.selectList(queryWrapper);
        //条件满足返回 信息
        if(!users.isEmpty()) {
            map.put("error_message", "用户名已存在");
            return map;
        }

        String encodePassword = passwordEncoder.encode(password);//密码加密保存
        String photo = "https://c-ssl.dtstatic.com/uploads/item/202005/29/20200529083215_quaod.thumb.1000_0.jpg";
        User user = new User(null, username, encodePassword, photo, 1500);
        userMapper.insert(user);

        map.put("error_message", "success");

        return map;
    }
}
