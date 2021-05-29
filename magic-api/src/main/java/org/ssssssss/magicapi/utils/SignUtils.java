package org.ssssssss.magicapi.utils;

public class SignUtils {

	public static String sign(Long timestamp, String secretKey, String mode, byte[] bytes) {
		return MD5Utils.encrypt(String.format("%s|%s|%s|%s", timestamp, mode, MD5Utils.encrypt(bytes), secretKey));
	}
}
