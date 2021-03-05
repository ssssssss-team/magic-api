package org.ssssssss.magicapi.utils;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.regex.Pattern;

public class PatternUtils {

	private static final Map<String, Pattern> cachedPatterns = new ConcurrentHashMap<>();

	public static boolean match(String content,String regex){
		Pattern pattern = cachedPatterns.get(regex);
		if(pattern == null){
			pattern = Pattern.compile(regex);
			cachedPatterns.put(regex,pattern);
		}
		return pattern.matcher(content).find();
	}
}
