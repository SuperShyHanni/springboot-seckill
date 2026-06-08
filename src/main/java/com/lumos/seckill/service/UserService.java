package com.lumos.seckill.service;

import com.lumos.seckill.entity.User;
import com.lumos.seckill.mapper.UserMapper;
import com.lumos.seckill.util.JwtUtil;
import com.lumos.seckill.util.MD5Util;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

@Service
public class UserService {
    @Autowired
    private UserMapper userMapper;

    @Autowired
    private JwtUtil jwtUtil;

    public String login(String username,String formPass){
        User user = userMapper.selectByUsername(username);
        if (user == null) {return null;}
        String dbPass = MD5Util.formPasstoDbPass(formPass, user.getSalt());
        if (!dbPass.equals(user.getPassword())){
            return null;
        }

        return jwtUtil.generateToken(user.getId());
    }
    
}
