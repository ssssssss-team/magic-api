package org.ssssssss.magicapi.utils;

import org.ssssssss.magicapi.core.exception.MagicAPIException;

import java.security.MessageDigest;

/**
 * MD5加密类
 *
 * @author mxd
 */
public class MD5Utils {

	private static final char[] HEX_CHARS = {'0', '1', '2', '3', '4', '5', '6', '7', '8', '9', 'a', 'b', 'c', 'd', 'e', 'f'};

	/**
	 * MD5加密
	 */
	public static String encrypt(String value) {
		return encrypt(value.getBytes());
	}

	/**
	 * MD5加密
	 */
	public static String encrypt(byte[] value) {
		try {
			byte[] bytes = MessageDigest.getInstance("MD5").digest(value);
			char[] chars = new char[32];
			for (int i = 0; i < chars.length; i = i + 2) {
				byte b = bytes[i / 2];
				chars[i] = HEX_CHARS[(b >>> 0x4) & 0xf];
				chars[i + 1] = HEX_CHARS[b & 0xf];
			}
			return new String(chars);
		} catch (Exception e) {
			throw new MagicAPIException("md5 encrypt error", e);
		}
	}
}
