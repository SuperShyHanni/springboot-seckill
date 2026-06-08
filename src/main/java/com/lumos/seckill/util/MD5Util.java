package com.lumos.seckill.util;

import org.springframework.util.DigestUtils;

public class MD5Util {
    public static String md5(String src) {
    return DigestUtils.md5DigestAsHex(src.getBytes());
    }
    
    public static String getPass(String password){
        return md5(password);
    }
    
    public static String formPasstoDbPass(String formPass,String salt){
        return md5(formPass + salt);
    }
}
