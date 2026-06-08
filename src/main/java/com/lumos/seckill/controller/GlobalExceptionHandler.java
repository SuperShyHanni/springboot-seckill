package com.lumos.seckill.controller;

import org.springframework.dao.DuplicateKeyException;
import org.springframework.web.bind.MissingRequestHeaderException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import io.jsonwebtoken.JwtException;

@RestControllerAdvice
public class GlobalExceptionHandler {
    
    @ExceptionHandler(DuplicateKeyException.class)
    public String handleDuplicate(DuplicateKeyException e) {
        return "请勿重复抢购";
    }

    @ExceptionHandler(JwtException.class)
    public String handleJwt(JwtException e) {
        return "登陆已失效，请重新登陆";
    }

    @ExceptionHandler(MissingRequestHeaderException.class)
    public String handleMissing(MissingRequestHeaderException e) {
        return "请先登录";
    }
}
