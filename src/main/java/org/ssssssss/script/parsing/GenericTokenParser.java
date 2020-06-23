package org.ssssssss.script.parsing;

import java.util.function.Function;


public class GenericTokenParser {

	private String open;

	private String close;

	private boolean skipStr;

	public GenericTokenParser(String open, String close, boolean skipStr) {
		this.open = open;
		this.close = close;
		this.skipStr = skipStr;
	}

	public String parse(String source, Function<String, String> handler) {
		CharacterStream stream = new CharacterStream(source);
		StringBuilder builder = new StringBuilder();
		while (stream.hasMore()) {
			builder.append(parseStream(stream, null, handler));
		}
		return builder.toString();
	}

	private String parseStream(CharacterStream stream, String expect, Function<String, String> handler) {
		StringBuilder builder = new StringBuilder();
		while (stream.hasMore()) {
			if (expect != null && stream.match(expect, true)) {
				return builder.toString();
			}
			if (stream.match(open, true)) {
				String value = handler.apply(parseStream(stream, close, handler));
				if (value != null) {
					builder.append(value);
				}
			} else {
				char ch = stream.consume();
				builder.append(ch);
				if (skipStr && ch == '\'') {
					builder.append(consumeUntil(stream, "'"));
				} else if (skipStr && ch == '"') {
					builder.append(consumeUntil(stream, "\""));
				} else if (ch == '{') {
					builder.append(parseStream(stream, "}", handler)).append("}");
				}
			}
		}
		return builder.toString();
	}

	private String consumeUntil(CharacterStream stream, String str) {
		int start = stream.getPosition();
		while (stream.hasMore()) {
			if (stream.match("\\", true)) {
				stream.consume();
			}
			if (stream.match(str, true)) {
				break;
			}
			stream.consume();
		}
		return stream.substring(start, stream.getPosition());
	}
}
