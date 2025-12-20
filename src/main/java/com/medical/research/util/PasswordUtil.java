package com.medical.research.util;

import org.springframework.util.DigestUtils;

/**
 * 密码工具类：MD5加密、验证
 */
public class PasswordUtil {

    /**
     * MD5加密
     */
    public static String encrypt(String password) {
        return DigestUtils.md5DigestAsHex(password.getBytes());
    }

    /**
     * 验证密码
     */
    public static boolean verify(String rawPassword, String encryptedPassword) {
        return encrypt(rawPassword).equals(encryptedPassword);
    }
}