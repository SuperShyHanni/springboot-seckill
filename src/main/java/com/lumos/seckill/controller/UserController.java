package com.lumos.seckill.controller;

import com.lumos.seckill.service.UserService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
public class UserController {
    @Autowired private UserService userService;

    @PostMapping("/login")
    public String login(@RequestParam("username") String username,@RequestParam("password") String password){
        String token = userService.login(username, password);
        if (token != null){
            return "登录成功,token=" + token;
        } else {
            return "用户名或密码错误";
        }
    }
}
