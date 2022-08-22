package com.kob.backend.service.impl;

import com.kob.backend.service.impl.utils.UserDetailsImpl;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.kob.backend.mapper.UserMapper;
import com.kob.backend.pojo.User;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;

@Service//Autowired要配合注解service
public class UserDetailsServiceImpl implements UserDetailsService {
    //implements是接口的实例/具象化
    @Autowired
    private UserMapper userMapper;

    //快速弹出继承的方法：alt+inset键——》override
    @Override
    public UserDetails loadUserByUsername(String username) throws UsernameNotFoundException {
                                                         //抛异常是throw
        QueryWrapper<User> queryWrapper = new QueryWrapper<>();
        queryWrapper.eq("username", username);
        User user = userMapper.selectOne(queryWrapper);
        if(user == null) {//抛异常是throw
            throw new RuntimeException("用户不存在");
        }

        return new UserDetailsImpl(user);//返回一个创建的类（结构体）
    }
}
