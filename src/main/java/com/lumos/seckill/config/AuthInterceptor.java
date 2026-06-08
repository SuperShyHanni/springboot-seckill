package com.lumos.seckill.config;

import com.lumos.seckill.util.JwtUtil;
import com.lumos.seckill.util.UserContext;

import io.jsonwebtoken.JwtException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.stereotype.Component;
import org.springframework.web.servlet.HandlerInterceptor;

@Component
public class AuthInterceptor implements HandlerInterceptor{

    private final JwtUtil jwtUtil;
    public AuthInterceptor(JwtUtil jwtUtil) {
        this.jwtUtil = jwtUtil;
    }

    @Override
    public boolean preHandle(HttpServletRequest request,HttpServletResponse response,Object handler) {
        String token = request.getHeader("Token");
        if (token == null || token.isBlank()) {
            throw new JwtException("请先登录");
        }
        Long userId = jwtUtil.parseToken(token);
        UserContext.set(userId);
        return true;
    }

    @Override
    public void afterCompletion(HttpServletRequest request,HttpServletResponse response,Object handler,Exception ex) {
        UserContext.clear();
    }
}
