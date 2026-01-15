package com.medical.research.util;

import javax.crypto.Cipher;
import javax.crypto.spec.IvParameterSpec;
import javax.crypto.spec.SecretKeySpec;
import java.util.Base64;

/**
 * @Auther: yilefeng
 * @Date: 2026/1/15 16:30
 * @Description:
 */
public class AESUtil {
    // 与前端完全一致的密钥（补全为16位后的值）
    private static final String AES_KEY = "shenrui123456789"; // 16位
    // 与前端完全一致的偏移量（16位）
    private static final String AES_IV = "0392039203920300";

    /**
     * AES-CBC解密（对应前端aesEncrypt）
     * @param encryptedText 前端传入的Base64密文
     * @return 明文密码
     * @throws Exception 解密异常
     */
    public static String decrypt(String encryptedText) throws Exception {
        // 1. 校验参数
        if (encryptedText == null || encryptedText.isEmpty()) {
            return "";
        }

        // 2. Base64解码密文
        byte[] encryptedBytes = Base64.getDecoder().decode(encryptedText);

        // 3. 初始化AES密钥和偏移量
        SecretKeySpec keySpec = new SecretKeySpec(AES_KEY.getBytes("UTF-8"), "AES");
        IvParameterSpec ivSpec = new IvParameterSpec(AES_IV.getBytes("UTF-8"));

        // 4. 初始化解密器（CBC模式 + PKCS5Padding，与前端PKCS7兼容）
        Cipher cipher = Cipher.getInstance("AES/CBC/PKCS5Padding");
        cipher.init(Cipher.DECRYPT_MODE, keySpec, ivSpec);

        // 5. 解密并返回明文
        byte[] decryptedBytes = cipher.doFinal(encryptedBytes);
        return new String(decryptedBytes, "UTF-8");
    }

    // 测试
    public static void main(String[] args) throws Exception {
        // 前端加密后的密文（示例）
        String cipherText = "5BqbCkRY0NeUBToNT1UB2g=="; // 替换为前端console输出的encryptPwd
        String plainText = decrypt(cipherText);
        System.out.println("后端解密结果：" + plainText); // 应输出原始密码
    }
}
