package org.ssssssss.magicapi.logging;

import java.io.IOException;
import java.io.PrintStream;
import java.io.PrintWriter;
import java.io.StringWriter;
import java.util.List;

public class LogInfo {

	private String level;

	private String message;

	private String throwable;

	public String getLevel() {
		return level;
	}

	public void setLevel(String level) {
		this.level = level;
	}

	public String getMessage() {
		return message;
	}

	public void setMessage(String message) {
		this.message = message;
	}

	public String getThrowable() {
		return throwable;
	}

	public void setThrowable(Throwable throwable) {
		if(throwable != null){
			try (StringWriter writer = new StringWriter();
				PrintWriter printWriter = new PrintWriter(writer)){
				throwable.printStackTrace(printWriter);
				this.throwable = writer.toString();
			} catch (IOException ignored){

			}
		}
	}
}
