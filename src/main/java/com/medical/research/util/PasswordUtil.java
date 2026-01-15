package com.medical.research.util;

import org.springframework.util.DigestUtils;

/**
 * 密码工具类：MD5加密、验证
 */
public class PasswordUtil {

    /**
     * MD5加密
     */
    public static String encrypt(String userName, String password) {
        password = userName + ":" + password;
        return DigestUtils.md5DigestAsHex(password.getBytes());
    }

    /**
     * 验证密码
     */
    public static boolean verify(String userName, String rawPassword, String encryptedPassword) {
        return encrypt(userName, rawPassword).equals(encryptedPassword);
    }

    public static void main(String[] args) {
    	System.out.println(encrypt("test", "123456"));
    }
}