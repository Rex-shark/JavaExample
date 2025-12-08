package com.rex.mytools;


import lombok.extern.slf4j.Slf4j;

import javax.crypto.Cipher;
import javax.crypto.KeyGenerator;
import javax.crypto.SecretKey;
import javax.crypto.spec.SecretKeySpec;
import java.nio.charset.StandardCharsets;
import java.security.SecureRandom;

@Slf4j
public class AESUtils {
    private static final String DEFAULT_KEY = "https://www.tist.com.tw/";

    /**
     * AES加密
     * @param content 要加密的内容
     * @return 加密后的字串
     */
    public static String aesEncode(String content) {
        return aesEncode(content, DEFAULT_KEY);
    }

    /**
     * AES加密
     * @param content 要加密的内容
     * @param key 加密使用的密鑰
     * @return 加密后的字串
     */
    public static String aesEncode(String content, String key) {
        try {
            key = key + DEFAULT_KEY;
            KeyGenerator keygen = KeyGenerator.getInstance("AES");
            SecureRandom random = SecureRandom.getInstance("SHA1PRNG");
            random.setSeed(key.getBytes(StandardCharsets.UTF_8));
            keygen.init(128, random);
            SecretKey original_key = keygen.generateKey();
            byte[] raw = original_key.getEncoded();
            SecretKey secretKey = new SecretKeySpec(raw, "AES");

            Cipher cipher = Cipher.getInstance("AES");
            cipher.init(Cipher.ENCRYPT_MODE, secretKey);
            byte[] byte_encode = content.getBytes(StandardCharsets.UTF_8);
            byte[] byte_AES = cipher.doFinal(byte_encode);

            return toHex(byte_AES); // 改成輸出 Hex
        } catch (Exception e) {
            log.error(e.getMessage(), e);
        }
        return null;
    }

    /**
     * AES解密
     * @param content 要解密的内容
     * @return 解密后的字串
     */
    public static String aesDecode(String content) {
        return aesDecode(content, DEFAULT_KEY);
    }

    /**
     * AES解密
     * @param content 要解密的内容
     * @param key 解密使用的密鑰
     * @return 解密后的字串
     */
    public static String aesDecode(String content, String key) {
        try {
            key = key + DEFAULT_KEY;
            KeyGenerator keygen = KeyGenerator.getInstance("AES");
            SecureRandom random = SecureRandom.getInstance("SHA1PRNG");
            random.setSeed(key.getBytes(StandardCharsets.UTF_8));
            keygen.init(128, random);
            SecretKey original_key = keygen.generateKey();
            byte[] raw = original_key.getEncoded();
            SecretKey secretKey = new SecretKeySpec(raw, "AES");

            Cipher cipher = Cipher.getInstance("AES");
            cipher.init(Cipher.DECRYPT_MODE, secretKey);

            byte[] byte_content = fromHex(content); // 先轉回 byte[]
            byte[] byte_decode = cipher.doFinal(byte_content);

            return new String(byte_decode, StandardCharsets.UTF_8);
        } catch (Exception e) {
            log.error(e.getMessage(), e);
        }
        return null;
    }

    // byte[] 轉 Hex 字串
    private static String toHex(byte[] bytes) {
        StringBuilder sb = new StringBuilder();
        for (byte b : bytes) {
            sb.append(String.format("%02X", b));
        }
        return sb.toString();
    }

    // Hex 字串轉回 byte[]
    private static byte[] fromHex(String hex) {
        int len = hex.length();
        byte[] data = new byte[len / 2];
        for (int i = 0; i < len; i += 2) {
            data[i / 2] = (byte) ((Character.digit(hex.charAt(i), 16) << 4)
                    + Character.digit(hex.charAt(i + 1), 16));
        }
        return data;
    }

    // 測試
    public static void main(String[] args) {

        String text = "秘密TEXT123!@#中文測試";

        String encrypted = aesEncode(text);
        System.out.println("加密 (Hex)：" + encrypted);

        String decrypted = aesDecode(encrypted);
        System.out.println("解密：" + decrypted);
    }
}


