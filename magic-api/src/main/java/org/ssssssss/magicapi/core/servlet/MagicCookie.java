package org.ssssssss.magicapi.core.servlet;

public interface MagicCookie {

	String getName();

	String getValue();

	<T> T getCookie();
}
