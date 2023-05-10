package org.ssssssss.magicapi.core.servlet;

import java.io.IOException;
import java.io.OutputStream;
import java.util.Collection;

public interface MagicHttpServletResponse {

	public void setHeader(String name, String value);

	public void addHeader(String name, String value);

	public void sendRedirect(String location) throws IOException;

	public void addCookie(MagicCookie cookie);

	public void setContentType(String contentType);

	public void setCharacterEncoding(String characterEncoding);

	public OutputStream getOutputStream() throws IOException;

	public Collection<String> getHeaderNames();

	public <T> T getResponse();
}
