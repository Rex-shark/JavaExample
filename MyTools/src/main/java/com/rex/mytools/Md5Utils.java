package com.rex.mytools;

import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.Base64;

public final class Md5Utils {
    private Md5Utils() {}
    /** 以 UTF-8 對字串取 MD5，回傳 32 碼十六進位 */
    public static String md5Hex(String input) {
        byte[] digest = md5Bytes(input);
        StringBuilder sb = new StringBuilder(digest.length * 2);
        for (byte b : digest) {
            sb.append(String.format("%02x", b));
        }
        return sb.toString();
    }

    /** 以 UTF-8 對字串取 MD5，回傳標準 Base64（會含 + / 及可能的 =） */
    public static String md5Base64(String input) {
        byte[] digest = md5Bytes(input);
        return Base64.getEncoder().encodeToString(digest);
    }

    /**
     * 以 UTF-8 對字串取 MD5，回傳 URL-safe Base64
     * @param withPadding 是否保留 '=' padding（預設很多場景可移除）
     */
    public static String md5Base64Url(String input, boolean withPadding) {
        byte[] digest = md5Bytes(input);
        Base64.Encoder enc = withPadding
                ? Base64.getUrlEncoder()
                : Base64.getUrlEncoder().withoutPadding();
        return enc.encodeToString(digest);
    }

    /** 驗證：把明文取 MD5 後與既有雜湊（hex/base64）比對 */
    public static boolean verifyHex(String plain, String expectedHex) {
        return md5Hex(plain).equalsIgnoreCase(expectedHex);
    }

    public static boolean verifyBase64(String plain, String expectedBase64) {
        return md5Base64(plain).equals(expectedBase64);
    }

    // -- internal --
    private static byte[] md5Bytes(String input) {
        try {
            MessageDigest md = MessageDigest.getInstance("MD5");
            md.update(input.getBytes(StandardCharsets.UTF_8));
            return md.digest();
        } catch (NoSuchAlgorithmException e) {
            // 在標準 JDK 幾乎不會發生
            throw new IllegalStateException("MD5 algorithm not available", e);
        }
    }

    // 簡易 CLI：java Md5Util "字串"
    public static void main(String[] args) {
        String s ="huiling071";
        System.out.println("Input:           " + s);
        String MD5Hex =  md5Hex(s);
        System.out.println("MD5Hex = " + MD5Hex);
        System.out.println("verifyHex(s, MD5Hex) = " + verifyHex(s, MD5Hex));
        System.out.println("MD5 Base64:      " + md5Base64(s));
        System.out.println("MD5 Base64-URL:  " + md5Base64Url(s, false) + "  (no padding)");
    }
}
