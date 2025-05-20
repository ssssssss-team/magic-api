package org.ssssssss.magicapi.utils;
import java.util.Base64;

public class ROT13Utils {

    /**
     * ROT13 函数：对给定字符串执行 ROT13 编码。
     * @param str - 要编码的字符串。
     * @return 编码后的字符串。
     */
    public static String rot13(String str) {
        StringBuilder result = new StringBuilder();

        for (char c : str.toCharArray()) {
            if (c >= 'a' && c <= 'z') {
                // 小写字母处理
                c = (char) (((c - 'a' + 13) % 26) + 'a');
            } else if (c >= 'A' && c <= 'Z') {
                // 大写字母处理
                c = (char) (((c - 'A' + 13) % 26) + 'A');
            }
            // 非字母字符保持不变
            result.append(c);
        }

        return result.toString();
    }

    /**
     * 加密流程：
     * 数据 -> Base64 编码 -> ROT13 替换
     * @param str 要加密的字符串。
     * @return 加密后的字符串。
     */
    public static String encrypt(String str) {
        try {
            String encode = encode(str);
            return rot13(encode);
        } catch (Exception e) {
            return str;
        }
    }

    /**
     * 解密流程：
     * ROT13 -> Base64 -> 数据对象
     * @param encrypted 要解密的字符串
     * @return 解密后的字符串
     */
    public static String decrypt(String encrypted) {
        try {
            String encrypt = rot13(encrypted.replaceFirst("^\"", "").replaceAll("\"$", ""));
            return decode(encrypt);
        } catch (Exception e) {
            return encrypted;
        }
    }

    /**
     * 对输入字符串进行 Base64 编码
     *
     * @param input 输入字符串
     * @return 编码后的字符串
     */
    public static String encode(String input) {
        // 将字符串转换为字节数组
        byte[] encodedBytes = Base64.getEncoder().encode(input.getBytes());
        // 转换回字符串并返回
        return new String(encodedBytes);
    }

    /**
     * 对 Base64 编码的字符串进行解码
     *
     * @param encodedString 已编码的字符串
     * @return 解码后的原始字符串
     */
    public static String decode(String encodedString) {
        // 将字符串转换为字节数组
        byte[] decodedBytes = Base64.getDecoder().decode(encodedString.getBytes());
        // 转换回字符串并返回
        return new String(decodedBytes);
    }
}