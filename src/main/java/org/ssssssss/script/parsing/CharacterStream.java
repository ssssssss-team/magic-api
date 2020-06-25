package org.ssssssss.script.parsing;

import javax.xml.transform.Source;
import java.util.ArrayList;
import java.util.List;

/**
 * Wraps a the content of a {@link Source} and handles traversing the contained characters. Manages a current {@link Span} via
 * the {@link #startSpan()} and {@link #endSpan()} methods.
 */
public class CharacterStream {
	private final String source;
	private final int end;
	private int index = 0;
	private int spanStart = 0;

	private List<Integer> newLines = new ArrayList<>();

	public CharacterStream(String source) {
		this(source, 0, source.length());
	}

	public CharacterStream(String source, int start, int end) {
		if (start > end) {
			throw new IllegalArgumentException("Start must be <= end.");
		}
		if (start < 0) {
			throw new IndexOutOfBoundsException("Start must be >= 0.");
		}
		if (start > Math.max(0, source.length() - 1)) {
			throw new IndexOutOfBoundsException("Start outside of string.");
		}
		if (end > source.length()) {
			throw new IndexOutOfBoundsException("End outside of string.");
		}
		this.source = source;
		this.index = start;
		this.end = end;
		newLines.add(0);
		for (int i = index; i < end; i++) {
			if (this.source.charAt(i) == '\n') {
				newLines.add(i);
			}
		}
	}

	public int getRowIndex(int index) {
		int size = newLines.size();
		int rowIndex = 1;
		while (size > rowIndex) {
			if (newLines.get(rowIndex) > index) {
				break;
			}
			rowIndex++;
		}
		return rowIndex;
	}

	public int getCol(int row, int index) {
		return index - newLines.get(row - 1);
	}

	public String substring(int startIndex, int endIndex) {
		return this.source.substring(startIndex, endIndex);
	}

	/**
	 * Returns whether there are more characters in the stream
	 **/
	public boolean hasMore() {
		return index < end;
	}

	/**
	 * Returns the next character without advancing the stream
	 **/
	public char peek() {
		if (!hasMore()) {
			throw new RuntimeException("No more characters in stream.");
		}
		return source.charAt(index++);
	}

	public Span getSpan(int start, int end) {
		return new Span(this.source, start, end);
	}

	/**
	 * Returns the next character and advance the stream
	 **/
	public char consume() {
		if (!hasMore()) {
			throw new RuntimeException("No more characters in stream.");
		}
		return source.charAt(index++);
	}


	/**
	 * Matches the given needle with the next characters. Returns true if the needle is matched, false otherwise. If there's a
	 * match and consume is true, the stream is advanced by the needle's length.
	 */
	public boolean match(String needle, boolean consume) {
		int needleLength = needle.length();
		if (needleLength + index > end) {
			return false;
		}
		for (int i = 0, j = index; i < needleLength; i++, j++) {
			if (index >= end) {
				return false;
			}
			if (needle.charAt(i) != source.charAt(j)) {
				return false;
			}
		}
		if (consume) {
			index += needleLength;
		}
		return true;
	}

	/**
	 * Returns whether the next character is a digit and optionally consumes it.
	 **/
	public boolean matchDigit(boolean consume) {
		if (index >= end) {
			return false;
		}
		char c = source.charAt(index);
		if (Character.isDigit(c)) {
			if (consume) {
				index++;
			}
			return true;
		}
		return false;
	}

	/**
	 * Returns whether the next character is the start of an identifier and optionally consumes it. Adheres to
	 * {@link Character#isJavaIdentifierStart(char)}.
	 **/
	public boolean matchIdentifierStart(boolean consume) {
		if (index >= end) {
			return false;
		}
		char c = source.charAt(index);
		if (Character.isJavaIdentifierStart(c) || c == '@') {
			if (consume) {
				index++;
			}
			return true;
		}
		return false;
	}

	/**
	 * Returns whether the next character is the start of an identifier and optionally consumes it. Adheres to
	 * {@link Character#isJavaIdentifierPart(char)}.
	 **/
	public boolean matchIdentifierPart(boolean consume) {
		if (index >= end) {
			return false;
		}
		char c = source.charAt(index);
		if (Character.isJavaIdentifierPart(c)) {
			if (consume) {
				index++;
			}
			return true;
		}
		return false;
	}

	public void skipLine() {
		while (true) {
			if (index >= end) {
				return;
			}
			char c = source.charAt(index++);
			if (c == '\n') {
				break;
			}
		}
	}

	public void skipUntil(String chars) {
		while (true) {
			if (index >= end) {
				return;
			}
			boolean matched = true;
			for (int i = 0, len = chars.length(); i < len && index + i < end; i++) {
				if (chars.charAt(i) != source.charAt(index + i)) {
					matched = false;
					break;
				}
			}
			this.index += matched ? chars.length() : 1;
			if (matched) {
				break;
			}
		}
	}

	/**
	 * Skips any number of successive whitespace characters.
	 **/
	public void skipWhiteSpace() {
		while (true) {
			if (index >= end) {
				return;
			}
			char c = source.charAt(index);
			if (c == ' ' || c == '\n' || c == '\r' || c == '\t') {
				index++;
				continue;
			} else {
				break;
			}
		}
	}

	/**
	 * Start a new Span at the current stream position. Call {@link #endSpan()} to complete the span.
	 **/
	public void startSpan() {
		spanStart = index;
	}

	/**
	 * Completes the span started with {@link #startSpan()} at the current stream position.
	 **/
	public Span endSpan() {
		return new Span(source, spanStart, index);
	}

	public boolean isSpanEmpty() {
		return spanStart == this.index;
	}

	/**
	 * Returns the current character position in the stream.
	 **/
	public int getPosition() {
		return index;
	}
}
