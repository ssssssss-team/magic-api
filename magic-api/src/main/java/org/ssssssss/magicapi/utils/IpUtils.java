package org.ssssssss.magicapi.utils;

import org.apache.commons.lang3.StringUtils;

import java.util.List;
import java.util.function.Function;
import java.util.stream.Collectors;
import java.util.stream.Stream;

public class IpUtils {

	private static final String[] DEFAULT_IP_HEADER = new String[]{"X-Forwarded-For", "X-Real-IP", "Proxy-Client-IP", "WL-Proxy-Client-IP", "HTTP_CLIENT_IP", "HTTP_X_FORWARDED_FOR"};

	public static String getRealIP(String remoteAddr, Function<String, String> getHeader, String... otherHeaderNames) {
		String ip = null;
		List<String> headers = Stream.concat(Stream.of(DEFAULT_IP_HEADER), Stream.of(otherHeaderNames == null ? new String[0] : otherHeaderNames)).collect(Collectors.toList());
		for (String header : headers) {
			if ((ip = processIp(getHeader.apply(header))) != null) {
				break;
			}
		}
		return ip == null ? processIp(remoteAddr) : ip;
	}

	private static String processIp(String ip) {
		if (ip != null) {
			ip = ip.trim();
			if (isUnknown(ip)) {
				return null;
			}
			if (ip.contains(",")) {
				String[] ips = ip.split(",");
				for (String subIp : ips) {
					ip = processIp(subIp);
					if (ip != null) {
						return ip;
					}
				}
			}
			return ip;
		}
		return null;
	}

	private static boolean isUnknown(String ip) {
		return StringUtils.isBlank(ip) || "unknown".equalsIgnoreCase(ip.trim());
	}
}
